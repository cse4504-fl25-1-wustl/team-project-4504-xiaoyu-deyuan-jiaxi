package service;

import entities.Art;
import entities.Box;
import entities.Container;
import entities.enums.BoxType;
import entities.enums.ContainerType;
import interactor.PackingPlan;
import interactor.UserConstraints;
import service.costing.ShippingCostStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The "brain" of the packing system. This service is responsible for making
 * the optimal decisions about how to pack items, based on the valid options
 * provided by the FeasibilityService and the cost model provided by a ShippingCostStrategy.
 */
public class OptimizationService {

    private final FeasibilityService feasibilityService;
    private final ShippingCostStrategy costStrategy;
    private int boxIdCounter = 1;
    private int containerIdCounter = 1;

    /**
     * Constructs an OptimizationService.
     * @param feasibilityService The rule checker used to find all valid options.
     * @param costStrategy The strategy used to evaluate the cost of a packing plan.
     */
    public OptimizationService(FeasibilityService feasibilityService, ShippingCostStrategy costStrategy) {
        this.feasibilityService = feasibilityService;
        this.costStrategy = costStrategy;
    }

    /**
     * Creates an optimal packing plan using a simple greedy algorithm as a prototype.
     * @param artsToPack The list of all Art objects to be packed.
     * @param constraints The user-defined constraints for this run.
     * @return A complete, optimized PackingPlan.
     */
    public PackingPlan createOptimalPlan(List<Art> artsToPack, UserConstraints constraints) {
        
        // --- Phase 1: Pack Arts into Boxes ---
        List<Box> packedBoxes = packArtsIntoBoxes(artsToPack, constraints);

        // --- Phase 2: Pack Boxes into Containers ---
        List<Container> packedContainers = packBoxesIntoContainers(packedBoxes, constraints);
        
        // --- Phase 3: Calculate Total Cost ---
        // The total cost is the sum of the costs of each individual container.
        double totalCost = packedContainers.stream()
                                           .mapToDouble(costStrategy::calculateCost)
                                           .sum();

        // --- Phase 4: Create and Return the Final Plan ---
        return new PackingPlan(packedContainers, totalCost);
    }

    /**
     * Implements a greedy algorithm to pack a list of arts into boxes.
     */
    private List<Box> packArtsIntoBoxes(List<Art> arts, UserConstraints constraints) {
        List<Box> boxes = new ArrayList<>();
        Map<Art, PackingOption> artOptions = new HashMap<>();

        // First, determine the chosen packing option for each art piece.
        for (Art art : arts) {
            // FeasibilityService returns ALL valid options, ordered by priority.
            List<PackingOption> options = feasibilityService.getValidPackingOptions(art, constraints);
            
            if (!options.isEmpty()) {
                // For the prototype, our "optimization" is to simply choose the first option.
                // This is the highest-priority option according to the RuleProvider's order.
                PackingOption chosenOption = options.get(0); 
                artOptions.put(art, chosenOption);
            } else {
                // It's good practice to log or warn about unboxable items.
                System.out.println("Warning: Art with ID " + art.getId() + " is unboxable and will be ignored.");
            }
        }

        // Now, iterate and pack using a greedy "best-fit" approach.
        for (Art art : arts) {
            PackingOption chosenOption = artOptions.get(art);
            if (chosenOption == null) continue; // Skip unboxable arts.

            // Try to find an existing box that this art can be added to.
            Optional<Box> suitableBox = findSuitableExistingBox(boxes, art, chosenOption, constraints);

            if (suitableBox.isPresent()) {
                // If a suitable box is found, add the art to it.
                suitableBox.get().addArt(art);
            } else {
                // Otherwise, create a new box for this art.
                Box newBox = createNewBox(chosenOption.boxType());
                newBox.addArt(art);
                boxes.add(newBox);
            }
        }
        return boxes;
    }

    /**
     * Implements a greedy algorithm to pack a list of boxes into containers.
     */
    private List<Container> packBoxesIntoContainers(List<Box> boxes, UserConstraints constraints) {
        List<Container> containers = new ArrayList<>();
        if (boxes.isEmpty()) {
            return containers;
        }

        for (Box box : boxes) {
            // FeasibilityService returns ALL valid container options for this box type.
            List<ContainerOption> options = feasibilityService.getValidContainerOptions(box, constraints);
            
            if (options.isEmpty()) {
                System.out.println("Warning: Box with ID " + box.getId() + " cannot be containerized and will be ignored.");
                continue;
            }
            
            // For the prototype, our "optimization" is to simply choose the first valid container option.
            // A future, more advanced optimizer could evaluate all options based on cost.
            ContainerOption chosenOption = options.get(0);

            // Try to find an existing container that this box can be added to.
            Optional<Container> suitableContainer = findSuitableExistingContainer(containers, box, chosenOption);
            
            if (suitableContainer.isPresent()) {
                suitableContainer.get().addBox(box);
            } else {
                // No suitable container found, create a new one.
                Container newContainer = createNewContainer(chosenOption.containerType());
                newContainer.addBox(box);
                containers.add(newContainer);
            }
        }
        return containers;
    }

    /**
     * Helper to find an existing box that an art piece can be added to.
     * This enforces the "same capacity can be mixed" rule.
     */
    private Optional<Box> findSuitableExistingBox(List<Box> boxes, Art art, PackingOption newArtOption, UserConstraints constraints) {
        for (Box box : boxes) {
            // Condition 1: Must be the same type of box.
            if (box.getBoxType() == newArtOption.boxType()) {
                
                // Condition 2: The box must not be full.
                if (box.getArtsInBox().size() >= newArtOption.capacity()) {
                    continue; // Box is full, try the next one.
                }

                // Condition 3: Check mixability based on capacity rules.
                if (box.getArtsInBox().isEmpty()) {
                    // Box is of the right type but empty, so it's always suitable.
                    return Optional.of(box);
                }
                
                // If the box is not empty, we must ensure the capacity rules are compatible.
                Art existingArt = box.getArtsInBox().get(0);
                // This is a bit inefficient, but acceptable for a prototype. A more advanced
                // version might cache these options.
                Optional<PackingOption> existingArtOption = feasibilityService.getValidPackingOptions(existingArt, constraints).stream().findFirst();

                if (existingArtOption.isPresent() && existingArtOption.get().capacity() == newArtOption.capacity()) {
                    // Capacities match, so they can be mixed. This is a suitable box.
                    return Optional.of(box);
                }
            }
        }
        // No suitable box was found in the entire list.
        return Optional.empty();
    }

    /**
     * Helper to find an existing container that a box can be added to.
     */
    private Optional<Container> findSuitableExistingContainer(List<Container> containers, Box box, ContainerOption chosenOption) {
         for (Container container : containers) {
            // Check if the container is of the right type AND is not yet full.
            if (container.getContainerType() == chosenOption.containerType() && 
                container.getBoxesInContainer().size() < chosenOption.capacity()) {
                return Optional.of(container);
            }
         }
         return Optional.empty();
    }

    /**
     * Factory method to create a new Box instance using properties from its enum type.
     */
    private Box createNewBox(BoxType type) {
        String id = "Box-" + boxIdCounter++;
        return new Box(id, type, type.getWidth(), type.getLength(), type.getMinHeight());
    }

    /**
     * Factory method to create a new Container instance using properties from its enum type.
     */
    private Container createNewContainer(ContainerType type) {
        String id = "Container-" + containerIdCounter++;
        return new Container(id, type, type.getWidth(), type.getLength(), type.getMinHeight(), type.getWeight(), type.getBaseHeight());
    }
}