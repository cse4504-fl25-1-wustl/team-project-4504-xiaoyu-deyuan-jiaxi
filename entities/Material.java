package entities;

public enum Material {
	GLASS("Glass", 0.0098, 6, 25, 18),
    ACRYLIC("Acrylic", 0.0094, 6, 25, 18),
    CANVAS_FRAMED("Canvas-Framed", 0.0085, 4, 18, 12),
    CANVAS_GALLERY("Canvas-Gallery", 0.0061, 4, 18, 12),
    MIRROR("Mirror", 0.0191, 0, 24, 0),
    ACOUSTIC_PANEL("Acoustic Panel", 0.0038, 4, 0, 0),
    ACOUSTIC_PANEL_FRAMED("Acoustic Panel-Framed", 0.0037, 4, 0, 0),
    PATIENT_BOARD("Patient Board", 0.0347, 4, 0, 0),
    UNKNOWN("Unknown", 0.0, 0, 0, 0); // A default value for unknown or unsupported materials.

    private final String displayName;
    private final double weight;
    private final int piecePerBox;
    private final int piecePerCrate;
    private final int piecePerCrateLarge;
    
    Material(String displayName, double weight, int piecePerBox, int piecePerCrate, int piecePerCrateLarge) {
        this.displayName = displayName;
        this.weight = weight;
        this.piecePerBox = piecePerBox;
        this.piecePerCrate = piecePerCrate;
        this.piecePerCrateLarge = piecePerCrateLarge;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getWeight() {
        return weight;
    }

    public int getPiecePerBox() {
        return piecePerBox;
    }
    
    public int getPiecePerCrate() {
        return piecePerCrate;
    }

    public int getPiecePerCrateLarge() {
        return piecePerCrateLarge;
    }

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
        return UNKNOWN;
    }
}