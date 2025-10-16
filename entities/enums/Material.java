package entities.enums;

/**
 * Defines the types of materials for an Art object.
 * This enum holds intrinsic properties of a material, like its name and weight factor,
 * but does not contain any business logic related to packing rules.
 */
public enum Material {
    GLASS("Glass", 0.0098),
    ACRYLIC("Acrylic", 0.0094),
    CANVAS_FRAMED("Canvas-Framed", 0.0085),
    CANVAS_GALLERY("Canvas-Gallery", 0.0061),
    MIRROR("Mirror", 0.0191),
    ACOUSTIC_PANEL("Acoustic Panel", 0.0038),
    ACOUSTIC_PANEL_FRAMED("Acoustic Panel-Framed", 0.0037),
    PATIENT_BOARD("Patient Board", 0.0347),
    UNKNOWN("Unknown", 0.0);

    private final String displayName;
    private final double weightFactor;

    Material(String displayName, double weightFactor)
    {
        this.displayName = displayName;
        this.weightFactor = weightFactor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getWeight() {
        return weightFactor;
    }

    /**
     * A utility method to find a Material enum constant from a given string.
     * This is useful for mapping input data (e.g., from a spreadsheet) to the correct enum type.
     * @param text The string representation of the material.
     * @return The corresponding Material enum, or UNKNOWN if no match is found.
     */
    public static Material fromString(String text)
    {
        if (text != null) {
            for (Material m : Material.values()) {
                if (text.equalsIgnoreCase(m.displayName)) {
                    return m;
                }
            }
        }
        return UNKNOWN;
    }
}