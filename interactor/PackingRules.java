package interactor;

/**
 * Centralized constants for packing and container rules.
 * Derived from Brianna’s Packing Rules & Box Specifications.
 */
public final class PackingRules {

    // SIZE LIMITS (for Art.java)

    /** Standard box limit: ≤ 36" per side */
    public static final int STANDARD_BOX_LIMIT = 36;

    /** Large box limit: ≤ 43.5" per side */
    public static final float LARGE_BOX_LIMIT = 43.5f;

    /** Crate threshold: ≤ 46" per side */
    public static final int CRATE_LIMIT = 46;

    /** Recommended maximum height: ≤ 84" */
    public static final int MAX_RECOMMENDED_HEIGHT = 84;


    // MATERIAL PIECE RULES (for Container.java)

    /** Threshold to define a "large" art piece */
    public static final int LARGE_ART_SIZE_THRESHOLD = 33;

    /** Glass / Acrylic */
    public static final int MAX_PIECES_GLASS_SMALL = 25;
    public static final int MAX_PIECES_GLASS_LARGE = 18;

    /** Canvas */
    public static final int MAX_PIECES_CANVAS_SMALL = 18;
    public static final int MAX_PIECES_CANVAS_LARGE = 12;

    /** Mirrors */
    public static final int MAX_PIECES_MIRROR = 25;

    /** Other materials (wood, paper, metal, etc.) */
    public static final int MAX_PIECES_OTHER_SMALL = 18;
    public static final int MAX_PIECES_OTHER_LARGE = 12;

    /** Max number of boxes per standard pallet/container */
    public static final int MAX_BOXES_PER_PALLET = 4;



    //  BOX DIMENSIONS (for box.java)


    public static final int STANDARD_BOX_LENGTH = 37;
    public static final int STANDARD_BOX_WIDTH  = 11;
    public static final int STANDARD_BOX_HEIGHT = 31;

    public static final int LARGE_BOX_LENGTH = 44;
    public static final int LARGE_BOX_WIDTH  = 13;
    public static final int LARGE_BOX_HEIGHT = 48;

    public static final int CRATE_LENGTH = 50;
    public static final int CRATE_WIDTH  = 38;
    public static final int CRATE_HEIGHT = 38;


    // =============================
    //  Prevent instantiation
    // =============================
    private PackingRules() {}
}
