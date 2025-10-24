package archdesign.entities;

import archdesign.entities.enums.ContainerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ContainerTest {
    private archdesign.entities.Container container;
    private archdesign.entities.Box box1;
    private archdesign.entities.Box box2;
    private Art art1;
    private Art art2;

    @BeforeEach
    void setUp() {
        container = new archdesign.entities.Container("CONT-1", ContainerType.GLASS_PALLET, 48, 40, 30, 100.0, 6);
        box1 = new archdesign.entities.Box("BOX-1", archdesign.entities.enums.BoxType.STANDARD, 37, 11, 31);
        box2 = new archdesign.entities.Box("BOX-2", archdesign.entities.enums.BoxType.LARGE, 44, 13, 48);
        art1 = new Art("ART-1", 10, 10, 5, archdesign.entities.enums.Material.GLASS);
        art2 = new Art("ART-2", 15, 15, 8, archdesign.entities.enums.Material.ACRYLIC);
    }

    //constructor and getters test
    @Test
    void constructor_SetsPropertiesCorrectly() {
        archdesign.entities.Container container = new archdesign.entities.Container(
            "TEST-CONT", ContainerType.STANDARD_PALLET, 48, 40, 32, 150.0, 8
        );
        
        assertEquals("TEST-CONT", container.getId());
        assertEquals(ContainerType.STANDARD_PALLET, container.getContainerType());
        assertEquals(48, container.getWidth());
        assertEquals(40, container.getLength());
        assertEquals(40, container.getCurrentHeight()); 
    }

    @Test
    void getters_ReturnCorrectValues() {
        assertEquals("CONT-1", container.getId());
        assertEquals(ContainerType.GLASS_PALLET, container.getContainerType());
        assertEquals(48, container.getWidth());
        assertEquals(40, container.getLength());
    }

    //box management tests
    @Test
    void addBox_AddsBoxToContainer() {
        container.addBox(box1);
        
        List<archdesign.entities.Box> boxes = container.getBoxesInContainer();
        assertEquals(1, boxes.size());
        assertEquals(box1, boxes.get(0));
    }

    @Test
    void addBox_MultipleBoxes_AddsAllToContainer() {
        container.addBox(box1);
        container.addBox(box2);
        
        List<archdesign.entities.Box> boxes = container.getBoxesInContainer();
        assertEquals(2, boxes.size());
        assertTrue(boxes.contains(box1));
        assertTrue(boxes.contains(box2));
    }

    @Test
    void removeBox_ExistingBox_ReturnsTrueAndRemovesBox() {
        container.addBox(box1);
        container.addBox(box2);
        
        boolean removed = container.removeBox(box1);
        
        assertTrue(removed);
        List<archdesign.entities.Box> boxes = container.getBoxesInContainer();
        assertEquals(1, boxes.size());
        assertFalse(boxes.contains(box1));
        assertTrue(boxes.contains(box2));
    }

    @Test
    void removeBox_NonExistingBox_ReturnsFalse() {
        container.addBox(box1);
        
        boolean removed = container.removeBox(box2);
        
        assertFalse(removed);
        assertEquals(1, container.getBoxesInContainer().size());
        assertTrue(container.getBoxesInContainer().contains(box1));
    }

    @Test
    void removeBox_FromEmptyContainer_ReturnsFalse() {
        boolean removed = container.removeBox(box1);
        
        assertFalse(removed);
        assertTrue(container.getBoxesInContainer().isEmpty());
    }

    //current height calculation tests
    @Test
    void getCurrentHeight_EmptyContainer_ReturnsMinHeightPlusBottomClearance() {
        assertEquals(36, container.getCurrentHeight());
    }

    @Test
    void getCurrentHeight_SingleBox_ReturnsBoxHeightPlusBottomClearance() {
        box1.addArt(art1); 
        container.addBox(box1);
        
        assertEquals(37, container.getCurrentHeight());
    }

    @Test
    void getCurrentHeight_BoxShorterThanMinHeight_ReturnsMinHeightPlusBottomClearance() {
        archdesign.entities.Box shortBox = new archdesign.entities.Box("SHORT", archdesign.entities.enums.BoxType.STANDARD, 37, 11, 31);
        shortBox.addArt(new Art("SHORT-ART", 20, 10, 5, archdesign.entities.enums.Material.GLASS));
        container.addBox(shortBox);
        
        
        assertEquals(37, container.getCurrentHeight());
    }

    @Test
    void getCurrentHeight_MultipleBoxes_ReturnsMaxBoxHeightPlusBottomClearance() {
        archdesign.entities.Box box1 = new archdesign.entities.Box("BOX1", archdesign.entities.enums.BoxType.STANDARD, 37, 11, 31);
        archdesign.entities.Box box2 = new archdesign.entities.Box("BOX2", archdesign.entities.enums.BoxType.LARGE, 44, 13, 48);
        archdesign.entities.Box box3 = new archdesign.entities.Box("BOX3", archdesign.entities.enums.BoxType.UPS_SMALL, 36, 6, 36);
        
        container.addBox(box1);
        container.addBox(box2);
        container.addBox(box3);
        
        
        assertEquals(54, container.getCurrentHeight());
    }

    @Test
    void getCurrentHeight_BoxWithTallArt_ReturnsArtHeightPlusBottomClearance() {
        archdesign.entities.Box box = new archdesign.entities.Box("TALL-BOX", archdesign.entities.enums.BoxType.STANDARD, 37, 11, 31);
        Art tallArt = new Art("TALL-ART", 50, 10, 5, archdesign.entities.enums.Material.GLASS); // height 50 > box minHeight 31
        box.addArt(tallArt);
        container.addBox(box);
        
       
        assertEquals(56, container.getCurrentHeight());
    }

    @Test
    void getCurrentHeight_ZeroBottomClearance_ReturnsContentHeightOnly() {
        archdesign.entities.Container zeroClearanceContainer = new archdesign.entities.Container(
            "ZERO-CLEAR", ContainerType.STANDARD_CRATE, 48, 40, 30, 100.0, 0
        );
        zeroClearanceContainer.addBox(box1);
        
        
        assertEquals(31, zeroClearanceContainer.getCurrentHeight());
    }

    // total weight calculation tests
    @Test
    void getTotalWeight_EmptyContainer_ReturnsContainerWeight() {
        assertEquals(100.0, container.getTotalWeight(), 1e-12);
    }

    @Test
    void getTotalWeight_SingleBox_ReturnsContainerWeightPlusBoxWeight() {
        box1.addArt(art1);
        container.addBox(box1);
        
        double expectedWeight = 100.0 + box1.getTotalWeight();
        assertEquals(expectedWeight, container.getTotalWeight(), 1e-12);
    }

    @Test
    void getTotalWeight_MultipleBoxes_ReturnsContainerWeightPlusAllBoxWeights() {
        box1.addArt(art1);
        box2.addArt(art2);
        container.addBox(box1);
        container.addBox(box2);
        
        double expectedWeight = 100.0 + box1.getTotalWeight() + box2.getTotalWeight();
        assertEquals(expectedWeight, container.getTotalWeight(), 1e-12);
    }

    @Test
    void getTotalWeight_AfterRemovingBox_UpdatesCorrectly() {
        box1.addArt(art1);
        box2.addArt(art2);
        container.addBox(box1);
        container.addBox(box2);
        double initialWeight = 100.0 + box1.getTotalWeight() + box2.getTotalWeight();
        
        container.removeBox(box1);
        double remainingWeight = 100.0 + box2.getTotalWeight();
        
        assertEquals(remainingWeight, container.getTotalWeight(), 1e-12);
        assertNotEquals(initialWeight, container.getTotalWeight());
    }
    //box list test
    @Test
    void getBoxesInContainer_ReturnsUnmodifiableList() {
        container.addBox(box1);
        
        List<archdesign.entities.Box> boxes = container.getBoxesInContainer();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            boxes.add(box2); // Should throw exception
        });
        
        // Original container should still only have one box
        assertEquals(1, container.getBoxesInContainer().size());
    }

    @Test
    void getBoxesInContainer_EmptyContainer_ReturnsEmptyUnmodifiableList() {
        List<archdesign.entities.Box> boxes = container.getBoxesInContainer();
        
        assertTrue(boxes.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> {
            boxes.add(box1); // Should throw exception
        });
    }

    //container type test
    @Test
    void differentContainerTypes_HaveCorrectProperties() {
        archdesign.entities.Container glassPallet = new archdesign.entities.Container(
            "GLASS", ContainerType.GLASS_PALLET, 48, 40, 30, 100.0, 6
        );
        archdesign.entities.Container standardPallet = new archdesign.entities.Container(
            "STD", ContainerType.STANDARD_PALLET, 48, 40, 32, 120.0, 8
        );
        archdesign.entities.Container standardCrate = new archdesign.entities.Container(
            "CRATE", ContainerType.STANDARD_CRATE, 36, 36, 24, 60.0, 2
        );
        
        // Verify GLASS_PALLET
        assertEquals(ContainerType.GLASS_PALLET, glassPallet.getContainerType());
        assertEquals(36, glassPallet.getCurrentHeight()); // minHeight 30 + clearance 6
        
        // Verify STANDARD_PALLET
        assertEquals(ContainerType.STANDARD_PALLET, standardPallet.getContainerType());
        assertEquals(40, standardPallet.getCurrentHeight()); // minHeight 32 + clearance 8
        
        // Verify STANDARD_CRATE with zero minHeight scenario
        assertEquals(ContainerType.STANDARD_CRATE, standardCrate.getContainerType());
        assertEquals(26, standardCrate.getCurrentHeight()); // minHeight 24 + clearance 2
    }

    //edge case test
    @Test
    void containerWithNullId_IsAllowed() {
        archdesign.entities.Container nullIdContainer = new archdesign.entities.Container(
            null, ContainerType.GLASS_PALLET, 48, 40, 30, 100.0, 6
        );
        
        assertNull(nullIdContainer.getId());
        assertEquals(ContainerType.GLASS_PALLET, nullIdContainer.getContainerType());
    }

    @Test
    void containerWithZeroDimensions_IsAllowed() {
        archdesign.entities.Container zeroContainer = new archdesign.entities.Container(
            "ZERO", ContainerType.STANDARD_CRATE, 0, 0, 0, 50.0, 0
        );
        
        assertEquals(0, zeroContainer.getWidth());
        assertEquals(0, zeroContainer.getLength());
        assertEquals(0, zeroContainer.getCurrentHeight()); // minHeight 0 + clearance 0
    }

    @Test
    void containerWithNegativeWeight_IsAllowed() {
        archdesign.entities.Container negativeWeightContainer = new archdesign.entities.Container(
            "NEG-WEIGHT", ContainerType.STANDARD_CRATE, 36, 36, 24, -10.0, 2
        );
        
        assertEquals(-10.0, negativeWeightContainer.getTotalWeight(), 1e-12);
    }

    @Test
    void addNullBox_IsAllowed() {
        container.addBox(null);
        
        List<archdesign.entities.Box> boxes = container.getBoxesInContainer();
        assertEquals(1, boxes.size());
        assertNull(boxes.get(0));
    }

    @Test
    void removeNullBox_FromContainerWithNullBox_ReturnsTrue() {
        container.addBox(null);
        
        boolean removed = container.removeBox(null);
        
        assertTrue(removed);
        assertTrue(container.getBoxesInContainer().isEmpty());
    }

    @Test
    void containerWithLargeBottomClearance_CalculatesHeightCorrectly() {
        archdesign.entities.Container highClearanceContainer = new archdesign.entities.Container(
            "HIGH-CLEAR", ContainerType.STANDARD_PALLET, 48, 40, 20, 100.0, 50
        );
        highClearanceContainer.addBox(box1);
        
        // box height 31 + clearance 50 = 81
        assertEquals(81, highClearanceContainer.getCurrentHeight());
    }

    // state consistency test
    @Test
    void containerState_RemainsConsistentAfterMultipleOperations() {
        // Initial state
        assertEquals(0, container.getBoxesInContainer().size());
        assertEquals(100.0, container.getTotalWeight(), 1e-12);
        assertEquals(36, container.getCurrentHeight());
        
        // Add first box
        box1.addArt(art1);
        container.addBox(box1);
        assertEquals(1, container.getBoxesInContainer().size());
        assertEquals(100.0 + box1.getTotalWeight(), container.getTotalWeight(), 1e-12);
        assertEquals(37, container.getCurrentHeight()); // box height 31 + clearance 6
        
        // Add second box
        box2.addArt(art2);
        container.addBox(box2);
        assertEquals(2, container.getBoxesInContainer().size());
        assertEquals(100.0 + box1.getTotalWeight() + box2.getTotalWeight(), container.getTotalWeight(), 1e-12);
        assertEquals(54, container.getCurrentHeight()); // max box height 48 + clearance 6
        
        // Remove first box
        container.removeBox(box1);
        assertEquals(1, container.getBoxesInContainer().size());
        assertEquals(100.0 + box2.getTotalWeight(), container.getTotalWeight(), 1e-12);
        assertEquals(54, container.getCurrentHeight()); // box2 height 48 + clearance 6
    }

    @Test
    void multipleContainerInstances_AreIndependent() {
        archdesign.entities.Container container1 = new archdesign.entities.Container(
            "CONT-1", ContainerType.GLASS_PALLET, 48, 40, 30, 100.0, 6
        );
        archdesign.entities.Container container2 = new archdesign.entities.Container(
            "CONT-2", ContainerType.STANDARD_PALLET, 48, 40, 32, 120.0, 8
        );
        
        container1.addBox(box1);
        container2.addBox(box2);
        
        assertEquals(1, container1.getBoxesInContainer().size());
        assertEquals(1, container2.getBoxesInContainer().size());
        assertEquals(box1, container1.getBoxesInContainer().get(0));
        assertEquals(box2, container2.getBoxesInContainer().get(0));
        assertNotEquals(container1.getTotalWeight(), container2.getTotalWeight());
        assertNotEquals(container1.getCurrentHeight(), container2.getCurrentHeight());
    }

    @Test
    void toStringMethod_ReturnsNonNull() {
        String stringRepresentation = container.toString();
        
        assertNotNull(stringRepresentation);
        assertFalse(stringRepresentation.isEmpty());
    }

    
    @Test
    void containerWithNestedArts_CalculatesCorrectHeightAndWeight() {
        // Create boxes with arts
        box1.addArt(art1); // art1 height 10, box1 minHeight 31 → box height 31
        box2.addArt(art2); // art2 height 15, box2 minHeight 48 → box height 48
        
        Art tallArt = new Art("TALL", 60, 20, 5, archdesign.entities.enums.Material.GLASS);
        archdesign.entities.Box box3 = new archdesign.entities.Box("BOX-3", archdesign.entities.enums.BoxType.STANDARD, 37, 11, 31);
        box3.addArt(tallArt); // art height 60 > box minHeight 31 → box height 60
        
        container.addBox(box1);
        container.addBox(box2);
        container.addBox(box3);
        
        // Height should be max box height (60) + bottom clearance (6) = 66
        assertEquals(66, container.getCurrentHeight());
        
        // Weight should be container weight + sum of all box weights
        double expectedWeight = 100.0 + box1.getTotalWeight() + box2.getTotalWeight() + box3.getTotalWeight();
        assertEquals(expectedWeight, container.getTotalWeight(), 1e-12);
    }

    @Test
    void emptyBoxesInContainer_ContributeToHeightCalculation() {
        // Empty boxes still have their minHeight
        container.addBox(box1); // empty box1 height = 31
        container.addBox(box2); // empty box2 height = 48
        
        // Max box height is 48 + clearance 6 = 54
        assertEquals(54, container.getCurrentHeight());
        
        // Weight should be just container weight since boxes are empty
        assertEquals(100.0, container.getTotalWeight(), 1e-12);
    }


}
