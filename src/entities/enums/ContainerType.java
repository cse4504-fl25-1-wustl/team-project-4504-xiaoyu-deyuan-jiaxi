package entities.enums;

/**
 * Defines the types of containers available for shipping.
 * This enum acts as the single source of truth for the core physical attributes
 * of each standard container type, such as dimensions, weight, and height constraints.
 */
public enum ContainerType {

    // --- Pallet Types ---
    // Defined with (length, width, weight, minHeight, baseHeight)
    // For Pallets, minHeight and baseHeight are 0.
    STANDARD_PALLET(48, 40, 60.0, 0, 0),
    GLASS_PALLET(43, 35, 60.0, 0, 0),
    OVERSIZE_PALLET(60, 40, 75.0, 0, 0),

    // --- Crate Types ---
    // For Crates, minHeight and baseHeight are 8 inches.
    STANDARD_CRATE(50, 38, 125.0, 8, 8);

    private final int length;
    private final int width;
    private final double weight; // Base weight (tare weight) of the container itself.
    private final int minHeight; // The minimum usable height for the contents.
    private final int baseHeight; // The fixed height of the base/bottom clearance (bottomClearanceHeight).

    /**
     * Constructor for the enum.
     * @param length The fixed length of the container type in inches.
     * @param width The fixed width of the container type in inches.
     * @param weight The fixed base weight of the container in lbs.
     * @param minHeight The minimum usable height for contents in inches.
     * @param baseHeight The fixed height of the base/clearance in inches.
     */
    ContainerType(int length, int width, double weight, int minHeight, int baseHeight) {
        this.length = length;
        this.width = width;
        this.weight = weight;
        this.minHeight = minHeight;
        this.baseHeight = baseHeight;
    }

    // --- Public Getters for Inherent Properties ---

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public double getWeight() {
        return weight;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getBaseHeight() {
        return baseHeight;
    }
}