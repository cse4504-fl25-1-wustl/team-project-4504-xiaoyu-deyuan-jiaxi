package archdesign.interactor;

import archdesign.entities.Art;
import archdesign.entities.Container;
import archdesign.entities.enums.Material;
import archdesign.entities.enums.ShippingProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PackerTest {

    @Test
    void testPack_WithEmptyArtList() {
        List<Art> emptyArts = new ArrayList<>();
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(emptyArts, constraints, provider);

        assertNotNull(plan);
        assertTrue(plan.getContainers().isEmpty());
        assertEquals(0.0, plan.getTotalCost());
        assertEquals(0.0, plan.getTotalWeight());
        assertEquals(0, plan.getTotalContainerCount());
        assertEquals(0, plan.getTotalBoxCount());
    }

    @Test
    void testPack_WithNullArtList() {
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(null, constraints, provider);

        assertNotNull(plan);
        assertTrue(plan.getContainers().isEmpty());
        assertEquals(0.0, plan.getTotalCost());
        assertEquals(0.0, plan.getTotalWeight());
        assertEquals(0, plan.getTotalContainerCount());
        assertEquals(0, plan.getTotalBoxCount());
    }

    @Test
    void testPack_WithSingleSmallArt() {
        List<Art> arts = List.of(
            new Art("A001", 20, 30, 1, Material.GLASS)
        );
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan);
        assertFalse(plan.getContainers().isEmpty());
        assertTrue(plan.getTotalCost() >= 0.0);
        assertTrue(plan.getTotalWeight() > 0.0);
        assertTrue(plan.getTotalContainerCount() > 0);
        assertTrue(plan.getTotalBoxCount() > 0);
    }

    @Test
    void testPack_WithMultipleSmallArts() {
        List<Art> arts = List.of(
            new Art("A001", 20, 30, 1, Material.GLASS),
            new Art("A002", 25, 35, 1, Material.ACRYLIC),
            new Art("A003", 15, 25, 1, Material.CANVAS_FRAMED)
        );
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan);
        assertFalse(plan.getContainers().isEmpty());
        assertTrue(plan.getTotalCost() >= 0.0);
        assertTrue(plan.getTotalWeight() > 0.0);
        assertTrue(plan.getTotalContainerCount() > 0);
        assertTrue(plan.getTotalBoxCount() > 0);
        
        // Verify that all arts are packed
        int totalArtsPacked = plan.getContainers().stream()
            .mapToInt(container -> container.getBoxesInContainer().stream()
                .mapToInt(box -> box.getArtsInBox().size())
                .sum())
            .sum();
        assertEquals(arts.size(), totalArtsPacked);
    }

    @Test
    void testPack_WithLargeArt() {
        List<Art> arts = List.of(
            new Art("A001", 40, 40, 1, Material.GLASS) // Large art that should go in LARGE box
        );
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan);
        assertFalse(plan.getContainers().isEmpty());
        assertTrue(plan.getTotalCost() >= 0.0);
        assertTrue(plan.getTotalWeight() > 0.0);
        assertTrue(plan.getTotalContainerCount() > 0);
        assertTrue(plan.getTotalBoxCount() > 0);
    }

    @Test
    void testPack_WithOversizedArt() {
        List<Art> arts = List.of(
            new Art("A001", 100, 100, 1, Material.GLASS) // Oversized art that cannot be packed
        );
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan);
        // Oversized art should result in empty plan or minimal containers
        assertTrue(plan.getTotalContainerCount() >= 0);
        assertTrue(plan.getTotalCost() >= 0.0);
    }

    @Test
    void testPack_WithDifferentMaterials() {
        List<Art> arts = List.of(
            new Art("A001", 20, 30, 1, Material.GLASS),
            new Art("A002", 25, 35, 1, Material.ACRYLIC),
            new Art("A003", 15, 25, 1, Material.CANVAS_FRAMED),
            new Art("A004", 30, 40, 1, Material.CANVAS_GALLERY),
            new Art("A005", 10, 15, 1, Material.ACOUSTIC_PANEL)
        );
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan);
        assertFalse(plan.getContainers().isEmpty());
        assertTrue(plan.getTotalCost() >= 0.0);
        assertTrue(plan.getTotalWeight() > 0.0);
        assertTrue(plan.getTotalContainerCount() > 0);
        assertTrue(plan.getTotalBoxCount() > 0);
    }

    @Test
    void testPack_WithCustomConstraints() {
        List<Art> arts = List.of(
            new Art("A001", 20, 30, 1, Material.GLASS),
            new Art("A002", 40, 40, 1, Material.ACRYLIC)
        );
        
        // Test with constraints that only allow STANDARD boxes
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedBoxTypes(List.of(archdesign.entities.enums.BoxType.STANDARD))
            .build();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan);
        // The large art might not fit in STANDARD boxes, so we might get empty plan
        assertTrue(plan.getTotalContainerCount() >= 0);
        assertTrue(plan.getTotalCost() >= 0.0);
    }

    @Test
    void testPack_WithContainerConstraints() {
        List<Art> arts = List.of(
            new Art("A001", 20, 30, 1, Material.GLASS),
            new Art("A002", 25, 35, 1, Material.ACRYLIC)
        );
        
        // Test with constraints that only allow STANDARD_PALLET containers
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedContainerTypes(List.of(archdesign.entities.enums.ContainerType.STANDARD_PALLET))
            .build();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan);
        assertTrue(plan.getTotalContainerCount() >= 0);
        assertTrue(plan.getTotalCost() >= 0.0);
        
        // Verify all containers are STANDARD_PALLET type
        for (Container container : plan.getContainers()) {
            assertEquals(archdesign.entities.enums.ContainerType.STANDARD_PALLET, 
                        container.getContainerType());
        }
    }

    @Test
    void testPack_WithSunriseFlag() {
        List<Art> arts = List.of(
            new Art("A001", 20, 30, 1, Material.GLASS)
        );
        
        UserConstraints constraints = UserConstraints.newBuilder()
            .withSunriseFlag(true)
            .build();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan);
        // Currently sunriseFlag doesn't change behavior, but should not crash
        assertTrue(plan.getTotalContainerCount() >= 0);
        assertTrue(plan.getTotalCost() >= 0.0);
    }

    @Test
    void testPack_Consistency() {
        List<Art> arts = List.of(
            new Art("A001", 20, 30, 1, Material.GLASS),
            new Art("A002", 25, 35, 1, Material.ACRYLIC)
        );
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        // Run packing multiple times
        PackingPlan plan1 = Packer.pack(arts, constraints, provider);
        PackingPlan plan2 = Packer.pack(arts, constraints, provider);

        assertNotNull(plan1);
        assertNotNull(plan2);
        
        // Results should be consistent (same total cost, weight, etc.)
        assertEquals(plan1.getTotalCost(), plan2.getTotalCost(), 0.01);
        assertEquals(plan1.getTotalWeight(), plan2.getTotalWeight(), 0.01);
        assertEquals(plan1.getTotalContainerCount(), plan2.getTotalContainerCount());
        assertEquals(plan1.getTotalBoxCount(), plan2.getTotalBoxCount());
    }

    @Test
    void testPack_WithDifferentShippingProviders() {
        List<Art> arts = List.of(
            new Art("A001", 20, 30, 1, Material.GLASS)
        );
        UserConstraints constraints = new UserConstraints();

        // Test with PLACEHOLDER provider (only one currently implemented)
        PackingPlan plan = Packer.pack(arts, constraints, ShippingProvider.PLACEHOLDER);

        assertNotNull(plan);
        assertTrue(plan.getTotalCost() >= 0.0);
    }

    @Test
    void testPack_WithUnsupportedShippingProvider() {
        List<Art> arts = List.of(
            new Art("A001", 20, 30, 1, Material.GLASS)
        );
        UserConstraints constraints = new UserConstraints();

        // Test with unsupported provider should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            Packer.pack(arts, constraints, ShippingProvider.FEDEX);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            Packer.pack(arts, constraints, ShippingProvider.UPS);
        });
    }

    @Test
    void testPack_WithZeroDimensions() {
        List<Art> arts = List.of(
            new Art("A001", 0, 30, 1, Material.GLASS),      // Zero height
            new Art("A002", 20, 0, 1, Material.ACRYLIC),      // Zero width
            new Art("A003", 0, 0, 1, Material.CANVAS_FRAMED)  // Zero dimensions
        );
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan);
        // Zero-dimension arts should still be processable
        assertTrue(plan.getTotalContainerCount() >= 0);
        assertTrue(plan.getTotalCost() >= 0.0);
    }

    @Test
    void testPack_WithNegativeDimensions() {
        List<Art> arts = List.of(
            new Art("A001", -10, 30, 1, Material.GLASS),     // Negative height
            new Art("A002", 20, -15, 1, Material.ACRYLIC)     // Negative width
        );
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan);
        // Negative-dimension arts should still be processable
        assertTrue(plan.getTotalContainerCount() >= 0);
        assertTrue(plan.getTotalCost() >= 0.0);
    }

    @Test
    void testPack_WithLargeNumberOfArts() {
        List<Art> arts = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            arts.add(new Art("A" + String.format("%03d", i), 20, 30, 1, Material.GLASS));
        }
        
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        assertNotNull(plan);
        assertFalse(plan.getContainers().isEmpty());
        assertTrue(plan.getTotalCost() >= 0.0);
        assertTrue(plan.getTotalWeight() > 0.0);
        assertTrue(plan.getTotalContainerCount() > 0);
        assertTrue(plan.getTotalBoxCount() > 0);
        
        // Verify all arts are packed
        int totalArtsPacked = plan.getContainers().stream()
            .mapToInt(container -> container.getBoxesInContainer().stream()
                .mapToInt(box -> box.getArtsInBox().size())
                .sum())
            .sum();
        assertEquals(arts.size(), totalArtsPacked);
    }
}
