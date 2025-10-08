package entities;

public enum Material {
    GLASS_ACRYLIC_FRAMED("Glass/Acrylic Framed"),
    GLASS_ACRYLIC_SUNRISE("Glass/Acrylic (Sunrise)"),
    CANVAS_FRAMED_GALLERY("Canvas (Framed/Gallery)"),
    ACOUSTIC_PANELS("Acoustic Panels"),
    MIRRORS("Mirrors"),
    CANVAS("Canvas"),
    PAPER("Paper"),
    WOOD("Wood"),
    METAL("Metal"),
    SCULPTURE("Sculpture"),
    PHOTOGRAPH("Photograph"),
    UNKNOWN("Unknown");
    
    private final String displayName;
    
    Material(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
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
        
        // Handle common variations or abbreviations
        switch (normalized.toLowerCase()) {
            case "glass":
            case "acrylic":
            case "glass/acrylic":
                return GLASS_ACRYLIC_FRAMED;
            case "canvas":
            case "framed canvas":
                return CANVAS_FRAMED_GALLERY;
            case "acoustic":
            case "acoustic panel":
                return ACOUSTIC_PANELS;
            case "mirror":
                return MIRRORS;
            default:
                return UNKNOWN;
        }
    }
}