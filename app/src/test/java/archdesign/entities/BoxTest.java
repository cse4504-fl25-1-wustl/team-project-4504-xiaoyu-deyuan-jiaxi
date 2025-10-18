package archdesign.entities;

import archdesign.entities.enums.BoxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BoxTest {

    private archdesign.entities.Box box;
    private Art art1;
    private Art art2;

    @BeforeEach
    void setUp(){
        box = new Box("BOX-1", BoxType.STANDARD, 37, 11, 31);
        art1 = new Art("ART-1", 10, 10, 5, archdesign.entities.enums.Material.GLASS);
        art2 = new Art("ART-2", 15, 15, 8, archdesign.entities.enums.Material.ACRYLIC);
    }

    // constructor and getters test
    @Test
    void constructor_SetsPropertiesCorrectly() {
        archdesign.entities.Box box = new Box("TEST-BOX", BoxType.LARGE, 44, 13, 48);
        
        assertEquals("TEST-BOX", box.getId());
        assertEquals(BoxType.LARGE, box.getBoxType());
        assertEquals(44, box.getWidth());
        assertEquals(13, box.getLength());
        assertEquals(48, box.getCurrentHeight()); 
    }

    @Test
    void getters_ReturnCorrectValues() {
        assertEquals("BOX-1", box.getId());
        assertEquals(BoxType.STANDARD, box.getBoxType());
        assertEquals(37, box.getWidth());
        assertEquals(11, box.getLength());
    }
    //art management tests
    @Test
    void addArt_AddsArtToBox() {
        box.addArt(art1);
        
        List<Art> arts = box.getArtsInBox();
        assertEquals(1, arts.size());
        assertEquals(art1, arts.get(0));
    }

    @Test
    void addArt_MultipleArts_AddsAllToBox() {
        box.addArt(art1);
        box.addArt(art2);
        
        List<Art> arts = box.getArtsInBox();
        assertEquals(2, arts.size());
        assertTrue(arts.contains(art1));
        assertTrue(arts.contains(art2));
    }

    @Test
    void removeArt_ExistingArt_ReturnsTrueAndRemovesArt() {
        box.addArt(art1);
        box.addArt(art2);
        
        boolean removed = box.removeArt(art1);
        
        assertTrue(removed);
        List<Art> arts = box.getArtsInBox();
        assertEquals(1, arts.size());
        assertFalse(arts.contains(art1));
        assertTrue(arts.contains(art2));
    }

    @Test
    void removeArt_NonExistingArt_ReturnsFalse() {
        box.addArt(art1);
        
        boolean removed = box.removeArt(art2);
        
        assertFalse(removed);
        assertEquals(1, box.getArtsInBox().size());
        assertTrue(box.getArtsInBox().contains(art1));
    }

    //height calculation test
    @Test
    void getCurrentHeight_EmptyBox_ReturnsMinHeight() {
        assertEquals(31, box.getCurrentHeight());
    }

    @Test
    void getCurrentHeight_SingleArt_ReturnsArtHeight() {
        Art tallArt = new Art("TALL", 40, 10, 5, archdesign.entities.enums.Material.GLASS);
        box.addArt(tallArt);
        
        assertEquals(40, box.getCurrentHeight());
    }

    @Test
    void getCurrentHeight_ArtShorterThanMinHeight_ReturnsMinHeight() {
        Art shortArt = new Art("SHORT", 20, 10, 5, archdesign.entities.enums.Material.GLASS);
        box.addArt(shortArt);
        
        assertEquals(31, box.getCurrentHeight());
    }

    @Test
    void getCurrentHeight_MultipleArts_ReturnsMaxArtHeight() {
        Art art1 = new Art("ART1", 25, 10, 5, archdesign.entities.enums.Material.GLASS);
        Art art2 = new Art("ART2", 35, 10, 5, archdesign.entities.enums.Material.GLASS);
        Art art3 = new Art("ART3", 30, 10, 5, archdesign.entities.enums.Material.GLASS);
        
        box.addArt(art1);
        box.addArt(art2);
        box.addArt(art3);
        
        assertEquals(35, box.getCurrentHeight());
    }
    //total weight calculation test
    @Test
    void getTotalWeight_EmptyBox_ReturnsZero() {
        assertEquals(0.0, box.getTotalWeight(), 1e-12);
    }

    @Test
    void getTotalWeight_SingleArt_ReturnsArtWeight() {
        box.addArt(art1);
        
        double expectedWeight = art1.getWeight();
        assertEquals(expectedWeight, box.getTotalWeight(), 1e-12);
    }

    @Test
    void getTotalWeight_MultipleArts_ReturnsSumOfWeights() {
        box.addArt(art1);
        box.addArt(art2);
        
        double expectedWeight = art1.getWeight() + art2.getWeight();
        assertEquals(expectedWeight, box.getTotalWeight(), 1e-12);
    }

    // art list test
    @Test
    void getArtsInBox_ReturnsUnmodifiableList() {
        box.addArt(art1);
        
        List<Art> arts = box.getArtsInBox();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            arts.add(art2);
        });
        
        assertEquals(1, box.getArtsInBox().size());
    }

    @Test
    void getArtsInBox_EmptyBox_ReturnsEmptyUnmodifiableList() {
        List<Art> arts = box.getArtsInBox();
        
        assertTrue(arts.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> {
            arts.add(art1);
        });
    }

    //box type test
    @Test
    void differentBoxTypes_HaveCorrectDimensions() {
        archdesign.entities.Box standardBox = new archdesign.entities.Box("STD", BoxType.STANDARD, 37, 11, 31);
        archdesign.entities.Box largeBox = new archdesign.entities.Box("LRG", BoxType.LARGE, 44, 13, 48);
        archdesign.entities.Box upsSmallBox = new archdesign.entities.Box("UPS-S", BoxType.UPS_SMALL, 36, 6, 36);
        archdesign.entities.Box upsLargeBox = new archdesign.entities.Box("UPS-L", BoxType.UPS_LARGE, 44, 6, 35);
        archdesign.entities.Box crateBox = new archdesign.entities.Box("CRATE", BoxType.CRATE, 50, 38, 0);
        
        assertEquals(BoxType.STANDARD, standardBox.getBoxType());
        assertEquals(37, standardBox.getWidth());
        assertEquals(11, standardBox.getLength());
        assertEquals(31, standardBox.getCurrentHeight());
        
        assertEquals(BoxType.LARGE, largeBox.getBoxType());
        assertEquals(44, largeBox.getWidth());
        assertEquals(13, largeBox.getLength());
        assertEquals(48, largeBox.getCurrentHeight());
        
        assertEquals(BoxType.CRATE, crateBox.getBoxType());
        assertEquals(0, crateBox.getCurrentHeight());
    }

    @Test
    void crateBox_WithArt_ReturnsArtHeight() {
        archdesign.entities.Box crateBox = new archdesign.entities.Box("CRATE", BoxType.CRATE, 50, 38, 0);
        Art art = new Art("ART", 25, 25, 5, archdesign.entities.enums.Material.GLASS);
        
        crateBox.addArt(art);
        
        assertEquals(25, crateBox.getCurrentHeight());
    }
    //edge case tests
    @Test
    void boxWithNullId_IsAllowed() {
        archdesign.entities.Box nullIdBox = new archdesign.entities.Box(null, BoxType.STANDARD, 37, 11, 31);
        
        assertNull(nullIdBox.getId());
        assertEquals(BoxType.STANDARD, nullIdBox.getBoxType());
    }

    @Test
    void addNullArt_IsAllowed() {
        box.addArt(null);
        
        List<Art> arts = box.getArtsInBox();
        assertEquals(1, arts.size());
        assertNull(arts.get(0));
    }

    @Test
    void removeNullArt_FromBoxWithNullArt_ReturnsTrue() {
        box.addArt(null);
        
        boolean removed = box.removeArt(null);
        
        assertTrue(removed);
        assertTrue(box.getArtsInBox().isEmpty());
    }
    //state consistency test
    @Test
    void boxState_RemainsConsistentAfterMultipleOperations() {
        assertEquals(0, box.getArtsInBox().size());
        assertEquals(0.0, box.getTotalWeight(), 1e-12);
        assertEquals(31, box.getCurrentHeight());
        
        box.addArt(art1);
        assertEquals(1, box.getArtsInBox().size());
        assertEquals(art1.getWeight(), box.getTotalWeight(), 1e-12);
        assertEquals(31, box.getCurrentHeight());
        
        box.addArt(art2);
        assertEquals(2, box.getArtsInBox().size());
        assertEquals(art1.getWeight() + art2.getWeight(), box.getTotalWeight(), 1e-12);
        assertEquals(31, box.getCurrentHeight());
        
        box.removeArt(art1);
        assertEquals(1, box.getArtsInBox().size());
        assertEquals(art2.getWeight(), box.getTotalWeight(), 1e-12);
        assertEquals(31, box.getCurrentHeight());
    }

    @Test
    void toStringMethod_ReturnsNonNull() {
        String stringRepresentation = box.toString();
        
        assertNotNull(stringRepresentation);
        assertFalse(stringRepresentation.isEmpty());
    }



    
}
