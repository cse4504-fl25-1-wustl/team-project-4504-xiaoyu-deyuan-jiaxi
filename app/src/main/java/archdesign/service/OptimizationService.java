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

            // Step 6: set objective - minimize total cost using the cost strategy
            // This allows the optimizer to work with different pricing models:
            // - Linear pricing (PlaceholderCostStrategy): minimizes weight since cost ∝ weight
            // - Tiered pricing (future): will minimize actual cost considering price brackets
            LinearExprBuilder objectiveExpr = LinearExpr.newBuilder();
            
            // Calculate average content weight per container for cost estimation
            double totalArtWeight = artRequirements.stream()
                .mapToDouble(req -> req.art.getWeight())
                .sum();
            int estimatedContainerCount = Math.max(1, totalBoxes / 4); // Rough estimate: ~4 boxes per container
            double averageContentWeight = totalArtWeight / estimatedContainerCount;
            
            for (Map.Entry<ContainerType, IntVar> entry : containerVars.entrySet()) {
                ContainerType containerType = entry.getKey();
                IntVar var = entry.getValue();
                
                // Use cost strategy to get the coefficient for optimization
                // For PlaceholderCostStrategy: this equals (weight * 10 * 100) + 1, same behavior as before
                // For TieredCostStrategy: this will reflect the marginal cost of adding a container
                long coefficient = costStrategy.getCostCoefficient(containerType, averageContentWeight);
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

                // Post-processing: consolidate underutilized containers
                containers = consolidateUnderutilizedContainers(containers, constraints);

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

        // Post-processing: Consolidate under-utilized containers
        List<Container> optimizedContainers = consolidateContainers(nonEmptyContainers, containerCapacities, constraints);

        return optimizedContainers;
    }

    /**
     * Post-processing optimization: Consolidate under-utilized containers.
     * 
     * Strategy:
     * 1. Find containers with low utilization (e.g., only 1-2 boxes when capacity is 3-4)
     * 2. Try to merge boxes from multiple under-utilized containers into fewer containers
     * 3. When needed, upgrade STANDARD_PALLET to OVERSIZE_PALLET to gain extra capacity
     * 
     * Example optimization:
     * - Container A (STANDARD_PALLET): 2 STANDARD + 1 LARGE (3 boxes, but could fit more with upgrade)
     * - Container B (STANDARD_PALLET): 1 LARGE (very under-utilized)
     * After optimization:
     * - Container A becomes OVERSIZE_PALLET: 2 LARGE + 1 STANDARD (consolidate the LARGE boxes)
     * - Upgrade another STANDARD_PALLET to OVERSIZE_PALLET to hold the extra STANDARD box
     * - Remove Container B
     */
    private List<Container> consolidateContainers(
            List<Container> containers,
            Map<ContainerType, Map<BoxType, Integer>> containerCapacities,
            UserConstraints constraints) {
        
        // Check if OVERSIZE_PALLET is allowed
        List<ContainerType> allowedTypes = constraints.getAllowedContainerTypes();
        boolean oversizeAllowed = allowedTypes.isEmpty() || allowedTypes.contains(ContainerType.OVERSIZE_PALLET);
        
        if (!oversizeAllowed) {
            return containers; // Can't optimize without OVERSIZE_PALLET option
        }
        
        List<Container> result = new ArrayList<>(containers);
        boolean improved = true;
        
        while (improved) {
            improved = false;
            
            // Find under-utilized STANDARD_PALLETs (containers that could benefit from consolidation)
            List<Container> underUtilizedStandard = result.stream()
                .filter(c -> c.getContainerType() == ContainerType.STANDARD_PALLET)
                .filter(c -> {
                    int boxCount = c.getBoxesInContainer().size();
                    boolean hasLarge = c.getBoxesInContainer().stream()
                        .anyMatch(b -> b.getBoxType() == BoxType.LARGE);
                    // Under-utilized if: has LARGE boxes but less than 3, or has few boxes overall
                    return (hasLarge && boxCount < 3) || (!hasLarge && boxCount < 3);
                })
                .collect(Collectors.toList());
            
            if (underUtilizedStandard.size() < 2) {
                break; // Need at least 2 under-utilized containers to consolidate
            }
            
            // Try to find two containers that can be merged
            for (int i = 0; i < underUtilizedStandard.size() && !improved; i++) {
                Container containerA = underUtilizedStandard.get(i);
                
                for (int j = i + 1; j < underUtilizedStandard.size() && !improved; j++) {
                    Container containerB = underUtilizedStandard.get(j);
                    
                    // Count boxes by type across both containers
                    int largeBoxCount = 0;
                    int standardBoxCount = 0;
                    List<Box> allBoxes = new ArrayList<>();
                    
                    for (Box box : containerA.getBoxesInContainer()) {
                        allBoxes.add(box);
                        if (box.getBoxType() == BoxType.LARGE) largeBoxCount++;
                        else if (box.getBoxType() == BoxType.STANDARD) standardBoxCount++;
                    }
                    for (Box box : containerB.getBoxesInContainer()) {
                        allBoxes.add(box);
                        if (box.getBoxType() == BoxType.LARGE) largeBoxCount++;
                        else if (box.getBoxType() == BoxType.STANDARD) standardBoxCount++;
                    }
                    
                    // Check if we can fit everything in fewer containers with OVERSIZE_PALLET
                    // OVERSIZE_PALLET: LARGE capacity = 3, STANDARD capacity = 5
                    // STANDARD_PALLET: LARGE capacity = 3, STANDARD capacity = 4
                    
                    // Case 1: All boxes can fit in one OVERSIZE_PALLET
                    if (largeBoxCount <= 3 && standardBoxCount == 0) {
                        // Merge into one OVERSIZE_PALLET with all LARGE boxes
                        Container newContainer = createNewContainer(ContainerType.OVERSIZE_PALLET);
                        for (Box box : allBoxes) {
                            newContainer.addBox(box);
                        }
                        result.remove(containerA);
                        result.remove(containerB);
                        result.add(newContainer);
                        improved = true;
                    }
                    // Case 2: Mixed boxes - can we consolidate with OVERSIZE upgrade?
                    else if (largeBoxCount > 0 && standardBoxCount > 0) {
                        // Total boxes in mixed mode = min(LARGE_cap, STANDARD_cap) = 3 for both pallet types
                        int totalBoxes = largeBoxCount + standardBoxCount;
                        
                        if (totalBoxes <= 3) {
                            // All can fit in one container (either type works)
                            Container newContainer = createNewContainer(ContainerType.STANDARD_PALLET);
                            for (Box box : allBoxes) {
                                newContainer.addBox(box);
                            }
                            result.remove(containerA);
                            result.remove(containerB);
                            result.add(newContainer);
                            improved = true;
                        } else if (totalBoxes <= 4 && largeBoxCount <= 3) {
                            // Need to redistribute: put LARGE boxes in one container, STANDARD in another
                            // But with OVERSIZE, we can fit 5 STANDARD boxes
                            // Strategy: Use OVERSIZE_PALLET for the extra capacity
                            
                            List<Box> largeBoxes = allBoxes.stream()
                                .filter(b -> b.getBoxType() == BoxType.LARGE)
                                .collect(Collectors.toList());
                            List<Box> standardBoxes = allBoxes.stream()
                                .filter(b -> b.getBoxType() == BoxType.STANDARD)
                                .collect(Collectors.toList());
                            
                            // Put LARGE boxes in one container, STANDARD in OVERSIZE
                            if (largeBoxCount <= 3 && standardBoxCount <= 5) {
                                Container largeContainer = createNewContainer(ContainerType.STANDARD_PALLET);
                                for (Box box : largeBoxes) {
                                    largeContainer.addBox(box);
                                }
                                
                                Container standardContainer = createNewContainer(ContainerType.OVERSIZE_PALLET);
                                for (Box box : standardBoxes) {
                                    standardContainer.addBox(box);
                                }
                                
                                // Only consolidate if we reduce container count
                                if (largeBoxes.isEmpty() || standardBoxes.isEmpty()) {
                                    // One of them is empty, just use the non-empty one
                                    result.remove(containerA);
                                    result.remove(containerB);
                                    if (!largeBoxes.isEmpty()) result.add(largeContainer);
                                    if (!standardBoxes.isEmpty()) result.add(standardContainer);
                                    improved = true;
                                }
                            }
                        }
                    }
                    // Case 3: Only STANDARD boxes - check if OVERSIZE helps
                    else if (largeBoxCount == 0 && standardBoxCount > 0) {
                        // STANDARD_PALLET holds 4, OVERSIZE_PALLET holds 5
                        if (standardBoxCount <= 5) {
                            // Can fit in one OVERSIZE_PALLET
                            Container newContainer = createNewContainer(ContainerType.OVERSIZE_PALLET);
                            for (Box box : allBoxes) {
                                newContainer.addBox(box);
                            }
                            result.remove(containerA);
                            result.remove(containerB);
                            result.add(newContainer);
                            improved = true;
                        }
                    }
                }
            }
        }
        
        return result;
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
     * Post-processing optimization: consolidate underutilized containers.
     * 
     * Strategy:
     * 1. Find STANDARD_PALLETs with mixed box types (STANDARD + LARGE) that have low utilization
     * 2. Try to move LARGE boxes from underutilized pallets to other pallets with LARGE boxes
     * 3. If a STANDARD_PALLET has only 1 LARGE box left, try merging with another underutilized pallet
     * 4. Convert some STANDARD_PALLETs to OVERSIZE_PALLETs when it saves containers
     */
    private List<Container> consolidateUnderutilizedContainers(List<Container> containers, UserConstraints constraints) {
        // Check if OVERSIZE_PALLET is allowed
        List<ContainerType> allowedTypes = constraints.getAllowedContainerTypes();
        boolean oversizeAllowed = allowedTypes.isEmpty() || allowedTypes.contains(ContainerType.OVERSIZE_PALLET);
        
        if (!oversizeAllowed) {
            return containers; // Can't optimize if OVERSIZE_PALLET not allowed
        }
        
        List<Container> result = new ArrayList<>(containers);
        boolean improved = true;
        
        while (improved) {
            improved = false;
            
            // Find underutilized STANDARD_PALLETs (those with capacity to spare)
            List<Container> standardPallets = result.stream()
                .filter(c -> c.getContainerType() == ContainerType.STANDARD_PALLET)
                .collect(Collectors.toList());
            
            // Try to consolidate pallets with LARGE boxes
            improved = tryConsolidateLargeBoxPallets(result, standardPallets);
            
            if (!improved) {
                // Try to upgrade a STANDARD_PALLET to OVERSIZE_PALLET to absorb extra boxes
                improved = tryUpgradeToOversizePallet(result, standardPallets);
            }
        }
        
        return result;
    }
    
    /**
     * Try to consolidate LARGE boxes from underutilized pallets.
     * If one pallet has 1-2 LARGE boxes and another pallet has room, move them.
     */
    private boolean tryConsolidateLargeBoxPallets(List<Container> containers, List<Container> standardPallets) {
        // Find pallets with LARGE boxes that aren't full (capacity 3 for LARGE)
        List<Container> palletsWithLargeBoxes = standardPallets.stream()
            .filter(c -> c.getBoxesInContainer().stream().anyMatch(b -> b.getBoxType() == BoxType.LARGE))
            .collect(Collectors.toList());
        
        if (palletsWithLargeBoxes.size() < 2) {
            return false; // Need at least 2 pallets to consolidate
        }
        
        // Sort by number of LARGE boxes (ascending) - try to empty the one with fewest first
        palletsWithLargeBoxes.sort(Comparator.comparingInt(c -> 
            (int) c.getBoxesInContainer().stream().filter(b -> b.getBoxType() == BoxType.LARGE).count()));
        
        for (int i = 0; i < palletsWithLargeBoxes.size(); i++) {
            Container source = palletsWithLargeBoxes.get(i);
            List<Box> sourceLargeBoxes = source.getBoxesInContainer().stream()
                .filter(b -> b.getBoxType() == BoxType.LARGE)
                .collect(Collectors.toList());
            
            if (sourceLargeBoxes.isEmpty()) continue;
            
            // Find a target pallet that can accept these LARGE boxes
            for (int j = i + 1; j < palletsWithLargeBoxes.size(); j++) {
                Container target = palletsWithLargeBoxes.get(j);
                long targetLargeCount = target.getBoxesInContainer().stream()
                    .filter(b -> b.getBoxType() == BoxType.LARGE).count();
                
                int availableSpace = 3 - (int) targetLargeCount; // LARGE capacity is 3
                
                if (availableSpace > 0 && sourceLargeBoxes.size() <= availableSpace) {
                    // Move all LARGE boxes from source to target
                    for (Box box : sourceLargeBoxes) {
                        source.removeBox(box);
                        target.addBox(box);
                    }
                    
                    // If source is now empty, remove it
                    if (source.getBoxesInContainer().isEmpty()) {
                        containers.remove(source);
                    }
                    
                    return true; // Made an improvement
                }
            }
        }
        
        return false;
    }
    
    /**
     * Try to upgrade a STANDARD_PALLET to OVERSIZE_PALLET to absorb extra STANDARD boxes.
     * OVERSIZE_PALLET can hold 5 STANDARD boxes vs 4 for STANDARD_PALLET.
     */
    private boolean tryUpgradeToOversizePallet(List<Container> containers, List<Container> standardPallets) {
        // Find pallets with only STANDARD boxes that are full (4 boxes)
        List<Container> fullStandardOnlyPallets = standardPallets.stream()
            .filter(c -> c.getBoxesInContainer().stream().allMatch(b -> b.getBoxType() == BoxType.STANDARD))
            .filter(c -> c.getBoxesInContainer().size() == 4)
            .collect(Collectors.toList());
        
        // Find pallets with STANDARD boxes that are underutilized (1-3 boxes, no LARGE)
        List<Container> underutilizedStandardPallets = standardPallets.stream()
            .filter(c -> c.getBoxesInContainer().stream().allMatch(b -> b.getBoxType() == BoxType.STANDARD))
            .filter(c -> c.getBoxesInContainer().size() > 0 && c.getBoxesInContainer().size() < 4)
            .collect(Collectors.toList());
        
        if (fullStandardOnlyPallets.isEmpty() || underutilizedStandardPallets.isEmpty()) {
            return false;
        }
        
        // Check if upgrading one full pallet to OVERSIZE would allow absorbing an underutilized pallet
        for (Container fullPallet : fullStandardOnlyPallets) {
            for (Container underutilized : underutilizedStandardPallets) {
                int underutilizedCount = underutilized.getBoxesInContainer().size();
                
                // After upgrade, OVERSIZE has 5 capacity. Full pallet has 4.
                // So we have 1 extra slot. If underutilized has exactly 1 box, we can absorb it.
                if (underutilizedCount == 1) {
                    // Upgrade fullPallet to OVERSIZE_PALLET
                    Container oversizePallet = new Container(
                        fullPallet.getId(), 
                        ContainerType.OVERSIZE_PALLET,
                        ContainerType.OVERSIZE_PALLET.getWidth(),
                        ContainerType.OVERSIZE_PALLET.getLength(),
                        ContainerType.OVERSIZE_PALLET.getMinHeight(),
                        ContainerType.OVERSIZE_PALLET.getWeight(),
                        ContainerType.OVERSIZE_PALLET.getBaseHeight()
                    );
                    
                    // Move all boxes from full pallet to new oversize pallet
                    for (Box box : new ArrayList<>(fullPallet.getBoxesInContainer())) {
                        fullPallet.removeBox(box);
                        oversizePallet.addBox(box);
                    }
                    
                    // Move the 1 box from underutilized pallet
                    for (Box box : new ArrayList<>(underutilized.getBoxesInContainer())) {
                        underutilized.removeBox(box);
                        oversizePallet.addBox(box);
                    }
                    
                    // Replace fullPallet with oversizePallet, remove underutilized
                    int idx = containers.indexOf(fullPallet);
                    containers.set(idx, oversizePallet);
                    containers.remove(underutilized);
                    
                    return true;
                }
            }
        }
        
        return false;
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