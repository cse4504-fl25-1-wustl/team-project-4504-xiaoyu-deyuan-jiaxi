package entities.enums;

/**
 * Defines the types of boxes available for packing.
 * Since the company uses a fixed set of boxes with unchanging properties,
 * this enum acts as the single source of truth for the core physical attributes
 * of each box type, such as its dimensions.
 */
public enum BoxType {

    // Enum constants are defined with their inherent, fixed properties.
    // Dimensions are in inches as per the provided table.
    STANDARD(37, 11, 31),
    LARGE(44, 13, 48),
    UPS_SMALL(36, 6, 36),
    UPS_LARGE(44, 6, 35),
    CRATE(50, 38, 0);

    private final int length;
    private final int width;
    private final int minHeight;
    /**
     * Constructor for the enum.
     * @param length The fixed length of the box type in inches.
     * @param width The fixed width of the box type in inches.
     * @param minHeight The fixed (minimum) height of the box type in inches.
     */
    BoxType(int length, int width, int minHeight) {
        this.length = length;
        this.width = width;
        this.minHeight = minHeight;
    }

    // --- Public Getters for Inherent Properties ---

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getMinHeight() {
        return minHeight;
    }
}