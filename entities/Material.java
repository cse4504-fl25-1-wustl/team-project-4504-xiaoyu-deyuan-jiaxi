package entities;

public enum Material {
	GLASS("Glass", 0.0098),
    ACRYLIC("Acrylic", 0.0094),
    CANVAS_FRAMED("Canvas-Framed", 0.0085),
    CANVAS_GALLERY("Canvas-Gallery", 0.0061),
    MIRROR("Mirror", 0.0191),
    ACOUSTIC_PANEL("Acoustic Panel", 0.0038),
    ACOUSTIC_PANEL_FRAMED("Acoustic Panel-Framed", 0.0037),
    PATIENT_BOARD("Patient Board", 0.0347),
    UNKNOWN("Unknown", 0.0); // A default value for unknown or unsupported materials.

    private final String displayName;
    private final double weightPerSquareInch;
    
    Material(String displayName, double weightPerSquareInch)
    {
        this.displayName = displayName;
        this.weightPerSquareInch = weightPerSquareInch;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getWeight() {
        return weight;
    }
    
    // Convert from CSV string to enum
    public static Material fromString(String materialStr) {
        if (materialStr == null || materialStr.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        String normalized = materialStr.trim();
        for (Material material : values()) {
            if (material.displayName.equalsIgnoreCase(normalized)) {
                return material;
            }
        }
    }
}