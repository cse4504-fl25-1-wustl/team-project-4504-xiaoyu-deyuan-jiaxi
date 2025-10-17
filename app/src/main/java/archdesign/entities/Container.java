package archdesign.entities;

import archdesign.entities.enums.ContainerType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container (Refactored)
 * This is a data model representing a specific shipping container instance.
 * It manages the list of Box objects it contains and does not include any decision-making logic.
 */
public class Container {
    // Core properties are final to ensure a container's attributes do not change after creation.
    private final String id;
    private final ContainerType containerType;
    private final int width;
    private final int length;
    private final double weight;

    // Height-related properties are now clearly separated.
    private final int minHeight;              // The minimum USABLE height for the contents.
    private final int bottomClearanceHeight;  // The fixed height of the base/bottom clearance.
    private final List<Box> boxesInContainer;

    /**
     * Constructor now accepts all core physical properties, including its own
     * weight, a minimum height constraint, and the distinct 'bottomClearanceHeight' property.
     */
    public Container(String id, ContainerType containerType, int width, int length, int minHeight, double weight, int bottomClearanceHeight) {
        this.id = id;
        this.containerType = containerType;
        this.width = width;
        this.length = length;
        this.minHeight = minHeight;
        this.weight = weight;
        this.bottomClearanceHeight = bottomClearanceHeight;
        this.boxesInContainer = new ArrayList<>();
    }

    // --- Public Methods for State Mutation ---

    /**
     * Adds a Box object to the container.
     * @param box The Box to add.
     */
    public void addBox(Box box) {
        this.boxesInContainer.add(box);
    }

    /**
     * Removes a Box object from the container.
     * @param box The Box to remove.
     * @return true if the box was found and removed, false otherwise.
     */
    public boolean removeBox(Box box) {
        return this.boxesInContainer.remove(box);
    }

    // --- Getters for Accessing State ---

    public String getId() {
        return id;
    }
    
    public ContainerType getContainerType() {
        return containerType;
    }

    public int getWidth() {
        return width;
    }
    
    public int getLength() {
        return length;
    }

    /**
     * Dynamically calculates and returns the total external height of the container.
     * The total height is the sum of its fixed bottom clearance and the effective height
     * of its content area.
     * @return The total external height of the container.
     */
    public int getCurrentHeight() {
        // Step 1: Calculate the effective height of the content area.
        // This is the greater of the container's minimum usable height and the height of the tallest box inside.
        int maxBoxHeight = boxesInContainer.stream()
                                           .mapToInt(Box::getCurrentHeight)
                                           .max()
                                           .orElse(0);
        int contentHeight = Math.max(this.minHeight, maxBoxHeight);

        // Step 2: Add the fixed bottom clearance height to get the total external height.
        return contentHeight + this.bottomClearanceHeight;
    }

    /**
     * Dynamically calculates and returns the current total weight of the container.
     * @return The container's own weight plus the sum of the total weights of all Box objects inside.
     */
    public double getTotalWeight() {
        // Start with the container's own base weight.
        double boxesWeight = boxesInContainer.stream()
                                             .mapToDouble(Box::getTotalWeight)
                                             .sum();
        return this.weight + boxesWeight;
    }
    
    /**
     * Returns an unmodifiable view of the internal list of Boxes.
     * @return An unmodifiable view of the list of Boxes in the container.
     */
    public List<Box> getBoxesInContainer() {
        return Collections.unmodifiableList(boxesInContainer);
    }
}