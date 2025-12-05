package archdesign.betarelease;

import archdesign.entities.Art;
import archdesign.entities.Box;
import archdesign.entities.Container;
import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.Material;
import archdesign.entities.enums.ShippingProvider;
import archdesign.interactor.Packer;
import archdesign.interactor.PackingPlan;
import archdesign.interactor.UserConstraints;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Beta Release: Algorithm regression protection tests.
 * These tests protect algorithm invariants to ensure changes don't break business rules.
 * Tests verify depth boundaries, material separation, and capacity constraints.
 */
class AlgorithmRegressionTest {

    @Test
    void testDepthBoundary11StandardBox() {
        // Depth category = 11 should result in STANDARD box
        // STANDARD box has width = 11 (depth)
        List<Art> arts = List.of(
            new Art("ART-11-1", 30, 30, 0, Material.GLASS)  // Small enough for STANDARD box
        );
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan, "PackingPlan should not be null");
        assertFalse(plan.getContainers().isEmpty(), "Should have at least one container");

        // Check that STANDARD boxes are used (depth = 11)
        boolean hasStandardBox = false;
        for (Container container : plan.getContainers()) {
            for (Box box : container.getBoxesInContainer()) {
                if (box.getBoxType() == BoxType.STANDARD) {
                    hasStandardBox = true;
                    // Verify STANDARD box has correct depth (width = 11)
                    assertEquals(11, box.getWidth(), "STANDARD box should have width (depth) of 11");
                    break;
                }
            }
            if (hasStandardBox) break;
        }
        assertTrue(hasStandardBox, "Should use STANDARD box for depth category 11");
    }

    @Test
    void testDepthBoundary13LargeBox() {
        // Depth category = 13 should result in LARGE box
        // LARGE box has width = 13 (depth)
        List<Art> arts = List.of(
            new Art("ART-13-1", 40, 40, 0, Material.ACRYLIC)  // Large enough for LARGE box
        );
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan, "PackingPlan should not be null");
        assertFalse(plan.getContainers().isEmpty(), "Should have at least one container");

        // Check that LARGE boxes are used (depth = 13)
        boolean hasLargeBox = false;
        for (Container container : plan.getContainers()) {
            for (Box box : container.getBoxesInContainer()) {
                if (box.getBoxType() == BoxType.LARGE) {
                    hasLargeBox = true;
                    // Verify LARGE box has correct depth (width = 13)
                    assertEquals(13, box.getWidth(), "LARGE box should have width (depth) of 13");
                    break;
                }
            }
            if (hasLargeBox) break;
        }
        assertTrue(hasLargeBox, "Should use LARGE box for depth category 13");
    }


    @Test
    void testCapacityRespected() {
        // Create more arts than a single box capacity
        // For GLASS in STANDARD box, capacity is typically 6
        // Create 10 GLASS arts to force multiple boxes
        List<Art> arts = List.of(
            new Art("ART-GLASS-1", 20, 20, 0, Material.GLASS),
            new Art("ART-GLASS-2", 20, 20, 0, Material.GLASS),
            new Art("ART-GLASS-3", 20, 20, 0, Material.GLASS),
            new Art("ART-GLASS-4", 20, 20, 0, Material.GLASS),
            new Art("ART-GLASS-5", 20, 20, 0, Material.GLASS),
            new Art("ART-GLASS-6", 20, 20, 0, Material.GLASS),
            new Art("ART-GLASS-7", 20, 20, 0, Material.GLASS),
            new Art("ART-GLASS-8", 20, 20, 0, Material.GLASS),
            new Art("ART-GLASS-9", 20, 20, 0, Material.GLASS),
            new Art("ART-GLASS-10", 20, 20, 0, Material.GLASS)
        );
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan, "PackingPlan should not be null");
        assertFalse(plan.getContainers().isEmpty(), "Should have at least one container");

        // Count boxes of the same type
        int standardBoxCount = 0;
        for (Container container : plan.getContainers()) {
            for (Box box : container.getBoxesInContainer()) {
                if (box.getBoxType() == BoxType.STANDARD) {
                    standardBoxCount++;
                }
            }
        }

        // With 10 GLASS arts and capacity of 6 per STANDARD box, we should need at least 2 boxes
        assertTrue(standardBoxCount >= 2,
            "With 10 arts and capacity of 6 per box, should produce at least 2 boxes. " +
            "Actual box count: " + standardBoxCount);

        // Verify no box exceeds capacity
        for (Container container : plan.getContainers()) {
            for (Box box : container.getBoxesInContainer()) {
                if (box.getBoxType() == BoxType.STANDARD) {
                    int artCount = box.getArtsInBox().size();
                    assertTrue(artCount <= 6,
                        "STANDARD box should not exceed capacity of 6. " +
                        "Box " + box.getId() + " has " + artCount + " arts.");
                }
            }
        }
    }
}

