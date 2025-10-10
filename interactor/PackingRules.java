package interactor;

/**
 * Centralized constants for packing and container rules.
 * Derived from Brianna’s Packing Rules & Box Specifications.
 */
public final class PackingRules {
    /** Standard box limit: ≤ 36" per side */
    public static final int STANDARD_BOX_LIMIT = 36;

    /** Large box limit: ≤ 43.5" per side */
    public static final float LARGE_BOX_LIMIT = 43.5f;

    /** Crate threshold: ≤ 46" per side */
    public static final int CRATE_LIMIT = 46;

    /** Recommended maximum height: ≤ 84" */
    public static final int MAX_RECOMMENDED_HEIGHT = 84;
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
