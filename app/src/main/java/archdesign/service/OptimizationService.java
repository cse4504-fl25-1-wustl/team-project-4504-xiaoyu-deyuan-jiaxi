package archdesign.service;

import archdesign.config.RuleProvider;
import archdesign.entities.Art;
import archdesign.entities.Box;
import archdesign.entities.Container;
import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import archdesign.interactor.PackingPlan;
import archdesign.interactor.UserConstraints;
import archdesign.service.costing.ShippingCostStrategy;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;

import java.util.*;
import java.util.stream.Collectors;
import archdesign.entities.enums.Material;

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
            
            CpModel model = new CpModel();
            CpSolver solver = new CpSolver();
            solver.getParameters().setMaxTimeInSeconds(60.0); // 60 seconds time limit

            // Step 1: analyze the box requirements for each art with smart material grouping
            List<ArtBoxRequirement> artRequirements = new ArrayList<>();
            List<Art> unpackedArts = new ArrayList<>();
            
            // First pass: collect all arts with their options
            Map<Art, List<PackingOption>> artOptionsMap = new LinkedHashMap<>();
            for (Art art : artsToPack) {
                // First check if art is physically packable based on hard limits
                if (!RuleProvider.isPackable(art)) {
                    System.err.println("Art " + art.getId() + " (" + art.getWidth() + "x" + art.getHeight() + 
                                     ") exceeds physical packaging limits and will be counted as custom piece");
                    unpackedArts.add(art);
                    continue;
                }
                
                // Then check if there are valid packing options
                List<PackingOption> options = feasibilityService.getValidPackingOptions(art, constraints);
                if (options.isEmpty()) {
                    System.err.println("Art " + art.getId() + " not packable");
                    unpackedArts.add(art);
                    continue;
                }
                artOptionsMap.put(art, options);
            }
            
            // Smart optimization: try to find common box types for same material
            artRequirements = optimizeBoxSelectionByMaterial(artOptionsMap);
            

            // Step 2: group arts by BoxType and compute the number of boxes needed
            Map<BoxType, Integer> boxesNeeded = calculateBoxesNeeded(artRequirements);
            // Step 3: obtain available container types and their capacities
            Map<ContainerType, Map<BoxType, Integer>> containerCapacities = 
                getContainerCapacities(boxesNeeded.keySet(), constraints);

            // Step 4: create decision variables for each container type
            Map<ContainerType, IntVar> containerVars = new HashMap<>();
            for (ContainerType containerType : containerCapacities.keySet()) {
                IntVar var = model.newIntVar(0, 100, "container_" + containerType.name());
                containerVars.put(containerType, var);
            }

            // Step 5: add constraints - each box type must have sufficient container capacity
            for (Map.Entry<BoxType, Integer> entry : boxesNeeded.entrySet()) {
                BoxType boxType = entry.getKey();
                int needed = entry.getValue();
                
                // Build linear expression: sum of (container_count * capacity for this box type)
                LinearExprBuilder expr = LinearExpr.newBuilder();
                
                for (Map.Entry<ContainerType, Map<BoxType, Integer>> containerEntry : containerCapacities.entrySet()) {
                    ContainerType containerType = containerEntry.getKey();
                    Map<BoxType, Integer> capacities = containerEntry.getValue();
                    
                    Integer capacity = capacities.get(boxType);
                    if (capacity != null && capacity > 0) {
                        expr.addTerm(containerVars.get(containerType), capacity);
                    }
                }
                
                model.addGreaterOrEqual(expr, needed);
            }

            // Add constraint: total boxes cannot exceed total container capacity
            // Use conservative estimate: if mixing box types, capacity = min capacity
            int totalBoxes = boxesNeeded.values().stream().mapToInt(Integer::intValue).sum();
            boolean hasMixedBoxTypes = boxesNeeded.size() > 1;
            
            if (hasMixedBoxTypes) {
                // When we have mixed box types (e.g., Standard + Large),
                // each container can only hold min(capacity) boxes total
                LinearExprBuilder totalCapExpr = LinearExpr.newBuilder();
                
                for (Map.Entry<ContainerType, Map<BoxType, Integer>> containerEntry : containerCapacities.entrySet()) {
                    ContainerType containerType = containerEntry.getKey();
                    Map<BoxType, Integer> capacities = containerEntry.getValue();
                    
                    // Find minimum capacity for box types we're actually packing
                    int minCapacity = capacities.entrySet().stream()
                        .filter(e -> boxesNeeded.containsKey(e.getKey()) && e.getValue() > 0)
                        .mapToInt(Map.Entry::getValue)
                        .min()
                        .orElse(0);
                    
                    if (minCapacity > 0) {
                        totalCapExpr.addTerm(containerVars.get(containerType), minCapacity);
                    }
                }
                
                model.addGreaterOrEqual(totalCapExpr, totalBoxes);
            }

            // Step 6: set objective - minimize total container weight
            LinearExprBuilder objectiveExpr = LinearExpr.newBuilder();
            
            for (Map.Entry<ContainerType, IntVar> entry : containerVars.entrySet()) {
                ContainerType containerType = entry.getKey();
                IntVar var = entry.getValue();
                
                // Objective = container weight * 100 + 1 (small penalty for count)
                // Multiply by 100 to keep integer arithmetic while maintaining precision
                long coefficient = (long)(containerType.getWeight() * 100) + 1;
                objectiveExpr.addTerm(var, coefficient);
            }
            
            model.minimize(objectiveExpr);

            System.out.println("\nStarting solver...");

            // step 7: solve 
            CpSolverStatus status = solver.solve(model);

            if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {

                // get solution
                Map<ContainerType, Integer> solution = new HashMap<>();

                for (Map.Entry<ContainerType, IntVar> entry : containerVars.entrySet()) {
                    ContainerType type = entry.getKey();
                    long count = solver.value(entry.getValue());
                    if (count > 0)
                    {
                        solution.put(type, (int)count);
                    }
                }

                List<Container> containers = buildContainersFromSolution(
                    solution, artRequirements, boxesNeeded, containerCapacities, constraints);

                double totalCost = containers.stream()
                    .mapToDouble(costStrategy::calculateCost)
                    .sum();

                System.out.println("Total cost: $" + String.format("%.2f", totalCost));

                return new PackingPlan(containers, totalCost, unpackedArts);
                
            } else {
                System.err.println("CP-SAT solver did not find a feasible solution: " + status);
                return fallbackHeuristic(artsToPack, constraints);
            }

        } catch (Exception e) {
            System.err.println("OR-Tools solver error: " + e.getMessage());
            e.printStackTrace();
            return fallbackHeuristic(artsToPack, constraints);
        }
    }

    /**
     * Optimize box selection by grouping arts by material and finding common box types.
     * This allows different-sized artwork of the same material to be packed in larger boxes together.
     */
    private List<ArtBoxRequirement> optimizeBoxSelectionByMaterial(Map<Art, List<PackingOption>> artOptionsMap) {
        List<ArtBoxRequirement> requirements = new ArrayList<>();
        
        // Group arts by material
        Map<Material, List<Art>> artsByMaterial = new LinkedHashMap<>();
        for (Art art : artOptionsMap.keySet()) {
            artsByMaterial.computeIfAbsent(art.getMaterial(), k -> new ArrayList<>()).add(art);
        }
        
        // For each material group, try to find a common box type
        for (Map.Entry<Material, List<Art>> entry : artsByMaterial.entrySet()) {
            List<Art> artsOfMaterial = entry.getValue();
            
            // Check if we have mixed sizes (different dimensions) for this material
            boolean hasMixedSizes = artsOfMaterial.stream()
                .map(art -> art.getWidth() + "x" + art.getHeight())
                .distinct()
                .count() > 1;
            
            // Only apply optimization if we have mixed sizes
            if (!hasMixedSizes) {
                // All same size, use first option for each art (original behavior)
                for (Art art : artsOfMaterial) {
                    List<PackingOption> options = artOptionsMap.get(art);
                    requirements.add(new ArtBoxRequirement(art, options.get(0)));
                }
                continue;
            }
            
            // Find common box types that can fit ALL arts of this material
            Set<BoxType> commonBoxTypes = null;
            for (Art art : artsOfMaterial) {
                Set<BoxType> artBoxTypes = artOptionsMap.get(art).stream()
                    .map(PackingOption::boxType)
                    .collect(Collectors.toSet());
                
                if (commonBoxTypes == null) {
                    commonBoxTypes = new HashSet<>(artBoxTypes);
                } else {
                    commonBoxTypes.retainAll(artBoxTypes);
                }
            }
            
            // If there's a common box type, prefer the largest one (LARGE > STANDARD)
            // This allows us to pack different sizes together
            if (commonBoxTypes != null && !commonBoxTypes.isEmpty()) {
                // Priority order: LARGE > STANDARD > CRATE
                BoxType finalChosenBoxType;
                int chosenCapacity;
                
                if (commonBoxTypes.contains(BoxType.LARGE)) {
                    finalChosenBoxType = BoxType.LARGE;
                } else if (commonBoxTypes.contains(BoxType.STANDARD)) {
                    finalChosenBoxType = BoxType.STANDARD;
                } else if (commonBoxTypes.contains(BoxType.CRATE)) {
                    finalChosenBoxType = BoxType.CRATE;
                } else {
                    finalChosenBoxType = commonBoxTypes.iterator().next();
                }
                
                // Special handling for CRATE: don't merge mixed sizes
                // because CRATE has multiple sub-rules with different capacities
                boolean shouldSkipMerging = (finalChosenBoxType == BoxType.CRATE && hasMixedSizes);
                
                // Get the capacity for this box type from the first art's options
                chosenCapacity = artOptionsMap.get(artsOfMaterial.get(0)).stream()
                    .filter(opt -> opt.boxType() == finalChosenBoxType)
                    .findFirst()
                    .map(PackingOption::capacity)
                    .orElse(Integer.MAX_VALUE);
                
                // Only use common box type if it helps consolidation
                // i.e., if all arts can fit in fewer boxes of this type than separate types
                boolean useCommonBoxType = !shouldSkipMerging && artsOfMaterial.size() <= chosenCapacity;
                
                // Assign box type to all arts of this material
                for (Art art : artsOfMaterial) {
                    List<PackingOption> options = artOptionsMap.get(art);
                    PackingOption selectedOption;
                    
                    if (useCommonBoxType) {
                        // Find the option matching our chosen box type
                        selectedOption = options.stream()
                            .filter(opt -> opt.boxType() == finalChosenBoxType)
                            .findFirst()
                            .orElse(options.get(0)); // Fallback to first option if not found
                    } else {
                        // Too many arts for consolidation, use first available option per art
                        selectedOption = options.get(0);
                    }
                    
                    requirements.add(new ArtBoxRequirement(art, selectedOption));
                }
            } else {
                // No common box type, use first available option for each art
                for (Art art : artsOfMaterial) {
                    List<PackingOption> options = artOptionsMap.get(art);
                    requirements.add(new ArtBoxRequirement(art, options.get(0)));
                }
            }
        }
        
        return requirements;
    }

    /**
     * Calculate how many boxes are needed
     */
    private Map<BoxType, Integer> calculateBoxesNeeded(List<ArtBoxRequirement> requirements) {
        // SMART LOGIC: For each BoxType, decide whether to:
        // 1. Group by capacity separately (better when capacities are well-distributed)
        // 2. Merge all with minCapacity (better when mixing saves boxes)
        // Choose the strategy that minimizes total boxes needed
        
        Map<BoxType, List<ArtBoxRequirement>> groupedByType = requirements.stream()
            .collect(Collectors.groupingBy(req -> req.option.boxType()));
        
        Map<BoxType, Integer> boxesNeeded = new HashMap<>();
        
        for (Map.Entry<BoxType, List<ArtBoxRequirement>> entry : groupedByType.entrySet()) {
            BoxType boxType = entry.getKey();
            List<ArtBoxRequirement> reqs = entry.getValue();
            
            // Group by capacity
            Map<Integer, List<ArtBoxRequirement>> byCapacity = reqs.stream()
                .collect(Collectors.groupingBy(r -> r.option.capacity()));
            
            // Strategy 1: Calculate boxes if we keep each capacity group separate
            int boxesSeparate = 0;
            for (Map.Entry<Integer, List<ArtBoxRequirement>> capEntry : byCapacity.entrySet()) {
                int capacity = capEntry.getKey();
                int artCount = capEntry.getValue().size();
                boxesSeparate += (int) Math.ceil((double) artCount / capacity);
            }
            
            // Strategy 2: Calculate boxes if we merge all with minCapacity
            int minCapacity = reqs.stream()
                .mapToInt(r -> r.option.capacity())
                .min()
                .orElse(1);
            int boxesMerged = (int) Math.ceil((double) reqs.size() / minCapacity);
            
            // Choose the strategy that uses fewer boxes
            int totalBoxes = Math.min(boxesSeparate, boxesMerged);
            
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

            // create count number of this container type
           
            for (int i = 0; i < count; i++) {
                Container container = createNewContainer(containerType);
                containers.add(container);
            }
        }

    // Now assign boxes to containers
    // Use a First-Fit strategy
        for (Map.Entry<BoxType, List<ArtBoxRequirement>> entry : artsByBoxType.entrySet()) {
            List<ArtBoxRequirement> arts = entry.getValue();
            if (arts.isEmpty()) continue;
            
            BoxType boxType = entry.getKey();
            
            // Determine strategy: should we group by capacity or merge with minCapacity?
            Map<Integer, List<ArtBoxRequirement>> byCapacity = arts.stream()
                .collect(Collectors.groupingBy(r -> r.option.capacity()));
            
            int boxesSeparate = 0;
            for (Map.Entry<Integer, List<ArtBoxRequirement>> capEntry : byCapacity.entrySet()) {
                int capacity = capEntry.getKey();
                int artCount = capEntry.getValue().size();
                boxesSeparate += (int) Math.ceil((double) artCount / capacity);
            }
            
            int minCapacity = arts.stream()
                .mapToInt(r -> r.option.capacity())
                .min()
                .orElse(1);
            int boxesMerged = (int) Math.ceil((double) arts.size() / minCapacity);
            
            List<Box> boxes = new ArrayList<>();
            
            // Use the same strategy as calculateBoxesNeeded
            if (boxesSeparate <= boxesMerged) {
                // Strategy 1: Keep capacity groups separate
                for (Map.Entry<Integer, List<ArtBoxRequirement>> capEntry : byCapacity.entrySet()) {
                    int capacity = capEntry.getKey();
                    List<ArtBoxRequirement> capArts = capEntry.getValue();
                    
                    for (int i = 0; i < capArts.size(); i += capacity) {
                        Box box = createNewBox(boxType);
                        int end = Math.min(i + capacity, capArts.size());
                        for (int j = i; j < end; j++) {
                            box.addArt(capArts.get(j).art);
                        }
                        boxes.add(box);
                    }
                }
            } else {
                // Strategy 2: Merge all with minCapacity
                arts.sort(Comparator.comparingInt(r -> r.option.capacity()));
                for (int i = 0; i < arts.size(); i += minCapacity) {
                    Box box = createNewBox(boxType);
                    int end = Math.min(i + minCapacity, arts.size());
                    for (int j = i; j < end; j++) {
                        box.addArt(arts.get(j).art);
                    }
                    boxes.add(box);
                }
            }

            // divide boxes into suitable containers
           
            for (Box box : boxes) {
                boolean placed = false;
                
                for (Container container : containers) {
                    Map<BoxType, Integer> capacities = containerCapacities.get(container.getContainerType());
                    Integer maxCapacity = capacities.get(boxType);
                    
                    if (maxCapacity != null && maxCapacity > 0) {
                        // Calculate effective capacity based on what's already in the container
                        // If container has Large boxes, capacity = 3
                        // If container has only Standard boxes, capacity = 4 or 5 (depending on pallet type)
                        int effectiveLimit = maxCapacity;
                        
                        // Check if container already has boxes
                        if (!container.getBoxesInContainer().isEmpty()) {
                            // Find the minimum capacity among all box types already in container
                            int minExistingCapacity = Integer.MAX_VALUE;
                            for (Box existingBox : container.getBoxesInContainer()) {
                                BoxType existingType = existingBox.getBoxType();
                                Integer existingCap = capacities.get(existingType);
                                if (existingCap != null && existingCap < minExistingCapacity) {
                                    minExistingCapacity = existingCap;
                                }
                            }
                            // Use minimum of existing capacity and new box capacity
                            effectiveLimit = Math.min(minExistingCapacity, maxCapacity);
                        }
                        
                        if (container.getBoxesInContainer().size() < effectiveLimit) {
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

        // Filter out empty containers (containers with no boxes)
        // This can happen when OR-Tools overestimates the number of containers needed
        List<Container> nonEmptyContainers = containers.stream()
            .filter(container -> !container.getBoxesInContainer().isEmpty())
            .collect(Collectors.toList());

        return nonEmptyContainers;
    }

    // Fallback method with unpacked arts tracking
    private PackingPlan fallbackHeuristic(List<Art> artsToPack, UserConstraints constraints) {
        List<Container> containers = new ArrayList<>();
        List<Art> unpackedArts = new ArrayList<>();
        List<Art> sortedArts = new ArrayList<>(artsToPack);
        sortedArts.sort(Comparator.comparingDouble(Art::getWeight).reversed());
        
        for (Art art : sortedArts) {
            // First check if art is physically packable based on hard limits
            if (!RuleProvider.isPackable(art)) {
                System.err.println("Art " + art.getId() + " (" + art.getWidth() + "x" + art.getHeight() + 
                                 ") exceeds physical packaging limits (fallback heuristic)");
                unpackedArts.add(art);
                continue;
            }
            
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
                } else {
                    // Art could not be packed
                    System.err.println("Art " + art.getId() + " not packable (fallback heuristic)");
                    unpackedArts.add(art);
                }
            }
        }
        
        double totalCost = containers.stream()
            .mapToDouble(costStrategy::calculateCost)
            .sum();
        
        return new PackingPlan(containers, totalCost, unpackedArts);
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