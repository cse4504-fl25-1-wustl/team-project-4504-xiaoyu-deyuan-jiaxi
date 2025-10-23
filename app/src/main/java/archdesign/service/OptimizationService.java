package archdesign.service;

import archdesign.entities.Art;
import archdesign.entities.Box;
import archdesign.entities.Container;
import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import archdesign.interactor.PackingPlan;
import archdesign.interactor.UserConstraints;
import archdesign.service.costing.ShippingCostStrategy;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Optimization service - directly optimize selection of Container types
 * Objective: minimize total weight (container weight + arts weight)
 */
public class OptimizationService {

    private final FeasibilityService feasibilityService;
    private final ShippingCostStrategy costStrategy;
    private int boxIdCounter = 1;
    private int containerIdCounter = 1;

    public OptimizationService(FeasibilityService feasibilityService, ShippingCostStrategy costStrategy) {
        this.feasibilityService = feasibilityService;
        this.costStrategy = costStrategy;
    }

    public PackingPlan createOptimalPlan(List<Art> artsToPack, UserConstraints constraints) {
        if (artsToPack == null || artsToPack.isEmpty()) {
            return new PackingPlan(new ArrayList<>(), 0.0);
        }

        try {
            Loader.loadNativeLibraries();
            
            MPSolver solver = MPSolver.createSolver("SCIP");
            if (solver == null) {
                System.err.println("Could not create solver, falling back to heuristic algorithm");
                return fallbackHeuristic(artsToPack, constraints);
            }

            solver.setTimeLimit(60000); // 60 seconds time limit

            // Step 1: analyze the box requirements for each art
            List<ArtBoxRequirement> artRequirements = new ArrayList<>();
            for (Art art : artsToPack) {
                List<PackingOption> options = feasibilityService.getValidPackingOptions(art, constraints);
                if (options.isEmpty()) {
                    System.err.println("Art " + art.getId() + " not packable");
                    continue;
                }
                artRequirements.add(new ArtBoxRequirement(art, options.get(0)));
            }

            // Step 2: group arts by BoxType and compute the number of boxes needed
            Map<BoxType, Integer> boxesNeeded = calculateBoxesNeeded(artRequirements);
            // Step 3: obtain available container types and their capacities
            Map<ContainerType, Map<BoxType, Integer>> containerCapacities = 
                getContainerCapacities(boxesNeeded.keySet(), constraints);

            // Step 4: create decision variables for each container type
            Map<ContainerType, MPVariable> containerVars = new HashMap<>();
            for (ContainerType containerType : containerCapacities.keySet()) {
                MPVariable var = solver.makeIntVar(0, 100, "container_" + containerType.name());
                containerVars.put(containerType, var);
            }

            // Step 5: add constraints - each box type must have sufficient container capacity
            for (Map.Entry<BoxType, Integer> entry : boxesNeeded.entrySet()) {
                BoxType boxType = entry.getKey();
                int needed = entry.getValue();
                
                MPConstraint constraint = solver.makeConstraint(needed, Double.POSITIVE_INFINITY, 
                    "box_capacity_" + boxType.name());
                
                // for every container, if it can fit the box, add to capability 
                for (Map.Entry<ContainerType, Map<BoxType, Integer>> containerEntry : containerCapacities.entrySet()) {
                    ContainerType containerType = containerEntry.getKey();
                    Map<BoxType, Integer> capacities = containerEntry.getValue();
                    
                    Integer capacity = capacities.get(boxType);
                    if (capacity != null && capacity > 0) {
                        // if container can fit the box, add capacity to the box 
                        constraint.setCoefficient(containerVars.get(containerType), capacity);
                    }
                }
            }

            // Step 6: set objective - minimize total container weight
            MPObjective objective = solver.objective();
            double artsWeight = artsToPack.stream().mapToDouble(Art::getWeight).sum();
            
            for (Map.Entry<ContainerType, MPVariable> entry : containerVars.entrySet()) {
                ContainerType containerType = entry.getKey();
                MPVariable var = entry.getValue();
                
                // every container's cost = container's own weight
               
                // (arts's weight is fixed, does not affect optimization direction)
               
                objective.setCoefficient(var, containerType.getWeight());
            }
            objective.setMinimization();

            System.out.println("\nStarting solver...");

            // step 7: solve 
            final MPSolver.ResultStatus resultStatus = solver.solve();

            if (resultStatus == MPSolver.ResultStatus.OPTIMAL || resultStatus == MPSolver.ResultStatus.FEASIBLE) {


                // get solution
             
                Map<ContainerType, Integer> solution = new HashMap<>();
                double totalContainerWeight = 0;
                int totalContainers = 0;

                for (Map.Entry<ContainerType, MPVariable> entry : containerVars.entrySet()) {
                    ContainerType type = entry.getKey();
                    int count = (int) Math.round(entry.getValue().solutionValue());
                    if (count > 0)
                    {
                        solution.put(type, count);
                        totalContainerWeight += type.getWeight() * count;
                        totalContainers += count;
                    }
                }

                List<Container> containers = buildContainersFromSolution(
                    solution, artRequirements, boxesNeeded, containerCapacities, constraints);

                double totalCost = containers.stream()
                    .mapToDouble(costStrategy::calculateCost)
                    .sum();

                System.out.println("Total cost: $" + String.format("%.2f", totalCost));

                return new PackingPlan(containers, totalCost);
                
            } else {
                System.err.println("OR-Tools did not find a feasible solution: " + resultStatus);
                return fallbackHeuristic(artsToPack, constraints);
            }

        } catch (Exception e) {
            System.err.println("OR-Tools solver error: " + e.getMessage());
            e.printStackTrace();
            return fallbackHeuristic(artsToPack, constraints);
        }
    }

    /**
     * Calculate how many boxes are needed
     */
    private Map<BoxType, Integer> calculateBoxesNeeded(List<ArtBoxRequirement> requirements) {
        // group by Boxtype and capacity
        Map<BoxType, Map<Integer, List<Art>>> groupedByTypeAndCapacity = new HashMap<>();
        
        for (ArtBoxRequirement req : requirements) {
            groupedByTypeAndCapacity
                .computeIfAbsent(req.option.boxType(), k -> new HashMap<>())
                .computeIfAbsent(req.option.capacity(), k -> new ArrayList<>())
                .add(req.art);
        }

        // calculate every BoxType needs how many boxes
        
        Map<BoxType, Integer> boxesNeeded = new HashMap<>();
        
        for (Map.Entry<BoxType, Map<Integer, List<Art>>> typeEntry : groupedByTypeAndCapacity.entrySet()) {
            BoxType boxType = typeEntry.getKey();
            int totalBoxes = 0;
            
            for (Map.Entry<Integer, List<Art>> capacityEntry : typeEntry.getValue().entrySet()) {
                int capacity = capacityEntry.getKey();
                int artCount = capacityEntry.getValue().size();
                int boxes = (int) Math.ceil((double) artCount / capacity);
                totalBoxes += boxes;
            }
            
            boxesNeeded.put(boxType, totalBoxes);
        }
        
        return boxesNeeded;
    }

    /**
     * Get the capacity (in boxes) of each container type for each box type
     */
    private Map<ContainerType, Map<BoxType, Integer>> getContainerCapacities(
            Set<BoxType> boxTypes, UserConstraints constraints) {
        
        Map<ContainerType, Map<BoxType, Integer>> result = new HashMap<>();
        
        for (BoxType boxType : boxTypes) {
            Box testBox = new Box("temp", boxType, 1, 1, 1);
            List<ContainerOption> options = feasibilityService.getValidContainerOptions(testBox, constraints);
            
            for (ContainerOption option : options) {
                result.computeIfAbsent(option.containerType(), k -> new HashMap<>())
                      .put(boxType, option.capacity());
            }
        }
        
        return result;
    }

    /**
     * Build actual Container instances from the OR-Tools solution
     */
    private List<Container> buildContainersFromSolution(
            Map<ContainerType, Integer> solution,
            List<ArtBoxRequirement> artRequirements,
            Map<BoxType, Integer> boxesNeeded,
            Map<ContainerType, Map<BoxType, Integer>> containerCapacities,
            UserConstraints constraints) {

        List<Container> containers = new ArrayList<>();

        // Group arts by BoxType
        Map<BoxType, List<ArtBoxRequirement>> artsByBoxType = artRequirements.stream()
            .collect(Collectors.groupingBy(req -> req.option.boxType()));

    // Instantiate containers for each required container type
        for (Map.Entry<ContainerType, Integer> entry : solution.entrySet()) {
            ContainerType containerType = entry.getKey();
            int count = entry.getValue();
            Map<BoxType, Integer> capacities = containerCapacities.get(containerType);

            // create count number of this container type
           
            for (int i = 0; i < count; i++) {
                Container container = createNewContainer(containerType);
                containers.add(container);
            }
        }

    // Now assign boxes to containers
    // Use a First-Fit strategy
        for (Map.Entry<BoxType, List<ArtBoxRequirement>> entry : artsByBoxType.entrySet()) {
            BoxType boxType = entry.getKey();
            List<ArtBoxRequirement> arts = entry.getValue();

            // get capacity
            int capacity = arts.isEmpty() ? 1 : arts.get(0).option.capacity();

            // create boxes
            List<Box> boxes = new ArrayList<>();
            for (int i = 0; i < arts.size(); i += capacity) {
                Box box = createNewBox(boxType);
                int end = Math.min(i + capacity, arts.size());
                for (int j = i; j < end; j++) {
                    box.addArt(arts.get(j).art);
                }
                boxes.add(box);
            }

            // divide boxes into suitable containers
           
            for (Box box : boxes) {
                boolean placed = false;
                
                for (Container container : containers) {
                    Map<BoxType, Integer> capacities = containerCapacities.get(container.getContainerType());
                    Integer maxCapacity = capacities.get(boxType);
                    
                    if (maxCapacity != null && maxCapacity > 0) {
                        // check whether the container can still fit this box
                   
                        long currentBoxCount = container.getBoxesInContainer().stream()
                            .filter(b -> b.getBoxType() == boxType)
                            .count();
                        
                        if (currentBoxCount < maxCapacity) {
                            container.addBox(box);
                            placed = true;
                            break;
                        }
                    }
                }
                
                if (!placed) {
                    System.err.println("Cannot place box " + box.getId());
                }
            }
        }

        return containers;
    }

    // Fallback method remains unchanged
    private PackingPlan fallbackHeuristic(List<Art> artsToPack, UserConstraints constraints) {
        List<Container> containers = new ArrayList<>();
        List<Art> sortedArts = new ArrayList<>(artsToPack);
        sortedArts.sort(Comparator.comparingDouble(Art::getWeight).reversed());
        
        for (Art art : sortedArts) {
            boolean placed = false;
            
            for (Container container : containers) {
                if (tryAddArtToContainer(container, art, constraints)) {
                    placed = true;
                    break;
                }
            }
            
            if (!placed) {
                Container newContainer = createNewContainerForArt(art, constraints);
                if (newContainer != null) {
                    containers.add(newContainer);
                }
            }
        }
        
        double totalCost = containers.stream()
            .mapToDouble(costStrategy::calculateCost)
            .sum();
        
        return new PackingPlan(containers, totalCost);
    }

    private boolean tryAddArtToContainer(Container container, Art art, UserConstraints constraints) {
        List<PackingOption> options = feasibilityService.getValidPackingOptions(art, constraints);
        if (options.isEmpty()) return false;
        
        Box targetBox = null;
        for (Box box : container.getBoxesInContainer()) {
            if (box.getBoxType() == options.get(0).boxType() && 
                box.getArtsInBox().size() < options.get(0).capacity()) {
                targetBox = box;
                break;
            }
        }
        
        if (targetBox != null) {
            targetBox.addArt(art);
            return true;
        }
        
        List<ContainerOption> containerOptions = feasibilityService.getValidContainerOptions(
            createNewBox(options.get(0).boxType()), constraints);
        
        for (ContainerOption option : containerOptions) {
            if (option.containerType() == container.getContainerType() &&
                container.getBoxesInContainer().size() < option.capacity()) {
                Box newBox = createNewBox(options.get(0).boxType());
                newBox.addArt(art);
                container.addBox(newBox);
                return true;
            }
        }
        
        return false;
    }

    private Container createNewContainerForArt(Art art, UserConstraints constraints) {
        List<PackingOption> options = feasibilityService.getValidPackingOptions(art, constraints);
        if (options.isEmpty()) return null;
        
        Box box = createNewBox(options.get(0).boxType());
        box.addArt(art);
        
        List<ContainerOption> containerOptions = feasibilityService.getValidContainerOptions(box, constraints);
        if (containerOptions.isEmpty()) return null;
        
        Container container = createNewContainer(containerOptions.get(0).containerType());
        container.addBox(box);
        return container;
    }

    private Box createNewBox(BoxType type) {
        String id = "Box-" + boxIdCounter++;
        return new Box(id, type, type.getWidth(), type.getLength(), type.getMinHeight());
    }

    private Container createNewContainer(ContainerType type) {
        String id = "Container-" + containerIdCounter++;
        return new Container(id, type, type.getWidth(), type.getLength(), 
            type.getMinHeight(), type.getWeight(), type.getBaseHeight());
    }

    /**
     * Artand its required box option
     */
    private static class ArtBoxRequirement {
        final Art art;
        final PackingOption option;
        
        ArtBoxRequirement(Art art, PackingOption option) {
            this.art = art;
            this.option = option;
        }
    }
}