package archdesign.config;

import archdesign.config.spec.BoxRuleSpecification;
import archdesign.config.spec.ContainerRuleSpecification;
import archdesign.entities.Art;
import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import archdesign.entities.enums.Material;
import archdesign.interactor.UserConstraints;

import java.util.List;

/**
 * Acts as the single source of truth for all hard-coded business rules.
 */
public final class RuleProvider {

    // Hard physical limit for all box types (including large boxes)
    private static final int MAX_BOX_DIMENSION = 88; // Any dimension over 88" cannot be packed in any box

    private RuleProvider() {}

    /**
     * Checks if an art piece is physically packable based on hard dimensional limits.
     * 
     * Rule:
     * - If ANY dimension exceeds 88", the art is UNPACKABLE (exceeds maximum box height limit)
     * - The 88" limit applies to all box types (STANDARD, LARGE) after rotation
     * - Width limits are already enforced by box rules (STANDARD: 36.5", LARGE: 43.5")
     * 
     * @param art The art piece to check
     * @return true if the art can be packed, false if it should be counted as custom piece
     */
    public static boolean isPackable(Art art) {
        double width = art.getWidth();
        double height = art.getHeight();
        
        // Hard limit: any dimension over 88" is unpackable
        // This ensures that even after rotation, the art won't exceed box height limits
        if (width > MAX_BOX_DIMENSION || height > MAX_BOX_DIMENSION) {
            return false;
        }
        
        return true;
    }

    public static List<BoxRuleSpecification> getBoxRules(UserConstraints constraints) {
        // For now, we only have one set of rules.
        // The structure is ready for the "sunriseFlag" if those rules are ever defined.
        return getDefaultBoxRules();
    }

    public static List<ContainerRuleSpecification> getContainerRules() {
        return List.of(
            // --- Standard Pallet Rules ---
            ContainerRuleSpecification.newBuilder("Standard boxes on standard pallets")
                .forContainerType(ContainerType.STANDARD_PALLET)
                .withAllowedBoxType(BoxType.STANDARD)
                .withCapacity(4)
                .build(),
            
            ContainerRuleSpecification.newBuilder("Large boxes on standard pallets")
                .forContainerType(ContainerType.STANDARD_PALLET)
                .withAllowedBoxType(BoxType.LARGE)
                .withCapacity(3)
                .build(),

            // --- Oversize Pallet Rules ---
            ContainerRuleSpecification.newBuilder("Standard boxes on oversize pallets")
                .forContainerType(ContainerType.OVERSIZE_PALLET)
                .withAllowedBoxType(BoxType.STANDARD)
                .withCapacity(5)
                .build(),

            ContainerRuleSpecification.newBuilder("Large boxes on oversize pallets")
                .forContainerType(ContainerType.OVERSIZE_PALLET)
                .withAllowedBoxType(BoxType.LARGE)
                .withCapacity(3)
                .build(),

            // --- Crate Rules ---
            ContainerRuleSpecification.newBuilder("Crate box in standard crate")
                .forContainerType(ContainerType.STANDARD_CRATE)
                .withAllowedBoxType(BoxType.CRATE)
                .withCapacity(1)
                .build()
        );
    }



    /**
     * Defines the default set of rules for packing art into boxes.
     * The list is ordered by priority, from MOST specific to MOST general.
     * The FeasibilityService will use the first rule that matches an Art object.
     */
    private static List<BoxRuleSpecification> getDefaultBoxRules() {
        return List.of(
           // --- PRIORITY 1: Standard Box Rules (Attempt this first for smallest items) ---
            generateStandardBoxRule(Material.GLASS, 6),
            generateStandardBoxRule(Material.ACRYLIC, 6),
            generateStandardBoxRule(Material.CANVAS_FRAMED, 4),
            generateStandardBoxRule(Material.CANVAS_GALLERY, 4),
            generateStandardBoxRule(Material.ACOUSTIC_PANEL, 4),
            generateStandardBoxRule(Material.ACOUSTIC_PANEL_FRAMED, 4),
            generateStandardBoxRule(Material.PATIENT_BOARD, 4),
            generateStandardBoxRule(Material.MIRROR, 8),

            // --- PRIORITY 2: Large Box Rules (Attempt this for medium-sized items) ---
            generateLargeBoxRule(Material.GLASS, 6),
            generateLargeBoxRule(Material.ACRYLIC, 6),
            generateLargeBoxRule(Material.CANVAS_FRAMED, 4),
            generateLargeBoxRule(Material.CANVAS_GALLERY, 4),
            generateLargeBoxRule(Material.ACOUSTIC_PANEL, 4),
            generateLargeBoxRule(Material.ACOUSTIC_PANEL_FRAMED, 4),
            generateLargeBoxRule(Material.PATIENT_BOARD, 4),
            generateLargeBoxRule(Material.MIRROR, 8),

            // --- PRIORITY 3: Crate Rules (Last resort for the largest items) ---
            // Within the Crate category, the more specific "Large Art" rule still comes first.

                        // This is now the final catch-all for any cratable art that didn't fit boxes.
            generateCrateRuleForSmallArt(Material.GLASS, 25),
            generateCrateRuleForSmallArt(Material.ACRYLIC, 25),
            generateCrateRuleForSmallArt(Material.CANVAS_FRAMED, 18),
            generateCrateRuleForSmallArt(Material.CANVAS_GALLERY, 18),
            generateCrateRuleForSmallArt(Material.MIRROR, 24),

            generateCrateRuleForLargeArt(Material.GLASS, 19),
            generateCrateRuleForLargeArt(Material.ACRYLIC, 19),
            generateCrateRuleForLargeArt(Material.CANVAS_FRAMED, 14),
            generateCrateRuleForLargeArt(Material.CANVAS_GALLERY, 14)

        );
    }

    // --- Private Helper Methods to build rules cleanly ---

    private static BoxRuleSpecification generateStandardBoxRule(Material material, int capacity) {
        return BoxRuleSpecification.newBuilder(material.name() + " in STANDARD Box", BoxType.STANDARD, capacity)
                .forMaterial(material)
                .withMaxWidth(36.5)
                .withMaxHeight(36.5)
                .build();
    }
    
    private static BoxRuleSpecification generateLargeBoxRule(Material material, int capacity) {
        return BoxRuleSpecification.newBuilder(material.name() + " in LARGE Box", BoxType.LARGE, capacity)
                .forMaterial(material)
                .withMinWidth(37)
                .withMinHeight(37)
                .withMaxWidth(43.5) // If it's 44x44, it must be a crate.
                .withMaxHeight(43.5)
                .build();
    }
    

    //bug高发地警告
    private static BoxRuleSpecification generateCrateRuleForSmallArt(Material material, int capacity) {
        return BoxRuleSpecification.newBuilder(material.name() + " in CRATE (small)", BoxType.CRATE, capacity)
                .forMaterial(material)
                .withMaxWidth(36)
                .withMaxHeight(36)
                .build();
    }
    
    private static BoxRuleSpecification generateCrateRuleForLargeArt(Material material, int capacity) {
        return BoxRuleSpecification.newBuilder(material.name() + " in CRATE (large)", BoxType.CRATE, capacity)
                .forMaterial(material)
                .withMinWidth(36.01) // Corresponds to > 36"
                .withMinHeight(36.01)
                .withMaxWidth(46) // If >46", it is unboxable and requires manual handling
                .withMaxHeight(46)
                .build();
    }
}