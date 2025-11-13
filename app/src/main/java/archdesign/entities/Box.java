package archdesign.entities;

import archdesign.entities.enums.BoxType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Box {
    private final String id;
    private final BoxType boxType;
    private final int width;
    private final int minHeight; 
    private final int length;
    private final List<Art> artsInBox;

    /**
     * Constructor. Note that it only accepts properties and makes no decisions.
     * The decision of "what size of box should be created" is made in the service layer.
     */
    public Box(String id, BoxType boxType, int width, int length, int minHeight) {
        this.id = id;
        this.boxType = boxType;
        this.width = width;
        this.length = length;
        this.minHeight = minHeight;
        this.artsInBox = new ArrayList<>();
    }

    // --- Public Methods for State Mutation ---
    // These methods only perform "add" or "remove" operations, without containing
    // any "can it be added?" validation logic. That logic is handled by the
    // FeasibilityService and OptimizationService.

    /**
     * Adds an Art object to the box.
     * This method is called by an external service that has already confirmed this action is valid.
     * @param art The Art to add.
     */
    public void addArt(Art art)
    {
        this.artsInBox.add(art);
    }

    /**
     * Removes an Art object from the box.
     * @param art The Art to remove.
     * @return true if the art was found and removed, false otherwise.
     */
    public boolean removeArt(Art art)
    {
        return this.artsInBox.remove(art);
    }


    // --- Getters for Accessing State ---

    public String getId() {
        return id;
    }

    public BoxType getBoxType() {
        return boxType;
    }

    public int getWidth() {
        return width;
    }
    
    public int getLength() {
        return length;
    }

    /**

     * Dynamically calculates and returns the current total height of the box.
     * This assumes the box's height is equal to the height of the tallest Art inside it.
     * @return The maximum height of any Art within the box.
     */
    public int getCurrentHeight() {
        // Step 1: Find the maximum height among all arts in the box.
        // Since Art height is now double, we keep it as double for precision.
        // If the box is empty, .max() returns an empty Optional, and .orElse(0.0) provides a default of 0.0.
        double maxArtHeight = artsInBox.stream().mapToDouble(Art::getHeight).max().orElse(0.0);
        // Step 2: Return the greater of the box's minimum height and the max art height, ceiled to ensure sufficient space.
        return (int) Math.ceil(Math.max(this.minHeight, maxArtHeight));
    }

    /**
     * Dynamically calculates and returns the current total weight of the box.
     * @return The sum of the weights of all Art objects inside.
     */

    public double getTotalWeight()
    {
        // Using Java Stream API for a more concise calculation.
        return artsInBox.stream().mapToDouble(Art::getWeight).sum();
    }

    /**
     * Returns an unmodifiable view of the internal list of Arts.
     * This prevents external code from accidentally modifying the list while allowing
     * it to be iterated over.
     * @return An unmodifiable view of the list of Arts in the box.
     */
    public List<Art> getArtsInBox() {
        return Collections.unmodifiableList(artsInBox);
    }
}