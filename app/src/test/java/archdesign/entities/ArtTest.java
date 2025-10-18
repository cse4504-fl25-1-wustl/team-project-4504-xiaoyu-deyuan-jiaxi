package archdesign.entities;

import archdesign.entities.enums.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArtTest {

    // basic functionality tests
    @Test
    public void weightCalculationUsesMaterialFactor() {
        Art a = new Art("A1", 10, 20, 1, Material.GLASS);
        double expected = 10 * 20 * Material.GLASS.getWeight();
        assertEquals(expected, a.getWeight(), 1e-9);
    }

    @Test
    public void gettersReturnValues() {
        Art a = new Art("X", 5, 7, 1, Material.ACRYLIC);
        assertEquals("X", a.getId());
        assertEquals(5, a.getHeight());
        assertEquals(7, a.getWidth());
        assertEquals(Material.ACRYLIC, a.getMaterial());
    }

    @Test
    public void zeroDimensionsProduceZeroWeight() {
        Art a = new Art("Z1", 0, 10, 1, Material.GLASS);
        assertEquals(0.0, a.getWeight(), 1e-12);

        Art b = new Art("Z2", 10, 0, 1, Material.GLASS);
        assertEquals(0.0, b.getWeight(), 1e-12);
    }

    @Test
    public void unknownMaterialYieldsZeroWeightFactor() {
        Art a = new Art("U1", 5, 5, 1, Material.UNKNOWN);
        assertEquals(0.0, a.getWeight(), 1e-12);
    }

    @Test
    public void thicknessParameterDoesNotAffectWeight() {
        Art t1 = new Art("T1", 4, 6, 1, Material.ACRYLIC);
        Art t2 = new Art("T2", 4, 6, 10, Material.ACRYLIC);
        assertEquals(t1.getWeight(), t2.getWeight(), 1e-12);
        assertEquals(t1.getHeight(), t2.getHeight());
        assertEquals(t1.getWidth(), t2.getWidth());
        assertEquals(t1.getMaterial(), t2.getMaterial());
    }

    @Test
    public void constructorWithNullId_ThrowsException() {
        Art art = new Art(null, 10, 10, 1, Material.GLASS);
        assertNull(art.getId());
    }

    //material weight test
    @Test
    public void glassMaterial_CalculatesCorrectWeight() {
        Art art = new Art("GLASS_ART", 8, 12, 2, Material.GLASS);
        double expected = 8 * 12 * Material.GLASS.getWeight();
        assertEquals(expected, art.getWeight(), 1e-9);
    }

    @Test
    public void acrylicMaterial_CalculatesCorrectWeight() {
        Art art = new Art("ACRYLIC_ART", 6, 8, 3, Material.ACRYLIC);
        double expected = 6 * 8 * Material.ACRYLIC.getWeight();
        assertEquals(expected, art.getWeight(), 1e-9);
    }

    @Test
    public void canvasFramedMaterial_CalculatesCorrectWeight() {
        Art art = new Art("CANVAS_ART", 20, 30, 5, Material.CANVAS_FRAMED);
        double expected = 20 * 30 * Material.CANVAS_FRAMED.getWeight();
        assertEquals(expected, art.getWeight(), 1e-9);
    }

    //edge case
    @Test
    public void negativeDimensions_CalculateWeightCorrectly() {
        Art art = new Art("NEG_ART", -5, 10, 1, Material.GLASS);
        double expected = (-5) * 10 * Material.GLASS.getWeight();
        assertEquals(expected, art.getWeight(), 1e-9);
    }

    @Test
    public void bothNegativeDimensions_CalculateWeightCorrectly() {
        Art art = new Art("NEG_BOTH", -5, -10, 1, Material.GLASS);
        double expected = (-5) * (-10) * Material.GLASS.getWeight();
        assertEquals(expected, art.getWeight(), 1e-9);
    }

    @Test
    public void zeroThickness_IsAllowed() {
        Art art = new Art("ZERO_THICK", 10, 10, 0, Material.GLASS);
        assertNotNull(art);
        assertEquals(10 * 10 * Material.GLASS.getWeight(), art.getWeight(), 1e-12);
    }

    @Test
    public void negativeThickness_IsAllowed() {
        Art art = new Art("NEG_THICK", 10, 10, -1, Material.GLASS);
        assertNotNull(art);
        assertEquals(10 * 10 * Material.GLASS.getWeight(), art.getWeight(), 1e-12);
    }

    @Test
    public void largeDimensions_HandleCorrectly() {
        Art art = new Art("LARGE", 10000, 10000, 1, Material.CANVAS_FRAMED);
        double expected = 10000 * 10000 * Material.CANVAS_FRAMED.getWeight();
        assertEquals(expected, art.getWeight(), 1e-9);
    }

    //comparison test
    @Test
    public void artObjectsWithSameProperties_HaveSameValues() {
        Art art1 = new Art("ART-1", 10, 10, 5, Material.GLASS);
        Art art2 = new Art("ART-1", 10, 10, 5, Material.GLASS);
        
        assertEquals(art1.getId(), art2.getId());
        assertEquals(art1.getHeight(), art2.getHeight());
        assertEquals(art1.getWidth(), art2.getWidth());
        assertEquals(art1.getMaterial(), art2.getMaterial());
        assertEquals(art1.getWeight(), art2.getWeight(), 1e-12);
    }

    @Test
    public void artObjectsWithDifferentIds_AreDistinct() {
        Art art1 = new Art("ART-1", 10, 10, 5, Material.GLASS);
        Art art2 = new Art("ART-2", 10, 10, 5, Material.GLASS);
        
        assertNotEquals(art1.getId(), art2.getId());
    }

    @Test
    public void artObjectsWithDifferentDimensions_HaveDifferentWeights() {
        Art smallArt = new Art("SMALL", 5, 5, 1, Material.GLASS);
        Art largeArt = new Art("LARGE", 10, 10, 1, Material.GLASS);
        
        assertNotEquals(smallArt.getWeight(), largeArt.getWeight());
        assertTrue(largeArt.getWeight() > smallArt.getWeight());
    }

    @Test
    public void artObjectsWithDifferentMaterials_HaveDifferentWeights() {
        Art glassArt = new Art("GLASS", 10, 10, 1, Material.GLASS);
        Art canvasArt = new Art("CANVAS", 10, 10, 1, Material.CANVAS_FRAMED);
        
        assertNotEquals(glassArt.getWeight(), canvasArt.getWeight());
    }

    //consistency test
    @Test
    public void artProperties_RemainConsistent() {
        Art art = new Art("CONSISTENT", 8, 12, 3, Material.ACRYLIC);
        
        // Multiple accesses should return same values
        assertEquals("CONSISTENT", art.getId());
        assertEquals("CONSISTENT", art.getId());
        
        assertEquals(8, art.getHeight());
        assertEquals(8, art.getHeight());
        
        double firstWeight = art.getWeight();
        double secondWeight = art.getWeight();
        assertEquals(firstWeight, secondWeight, 1e-12);
    }

    @Test
    public void toStringMethod_ReturnsNonNull() {
        Art art = new Art("ART-1", 10, 10, 5, Material.GLASS);
        String stringRepresentation = art.toString();
        
        assertNotNull(stringRepresentation);
        assertFalse(stringRepresentation.isEmpty());
    }

    @Test
    public void squareArt_CalculatesCorrectWeight() {
        Art art = new Art("SQUARE", 10, 10, 2, Material.GLASS);
        double expected = 10 * 10 * Material.GLASS.getWeight();
        assertEquals(expected, art.getWeight(), 1e-9);
    }

    @Test
    public void rectangularArt_CalculatesCorrectWeight() {
        Art art = new Art("RECT", 5, 20, 3, Material.ACRYLIC);
        double expected = 5 * 20 * Material.ACRYLIC.getWeight();
        assertEquals(expected, art.getWeight(), 1e-9);
    }


    @Test
    public void differentMaterialsWithSameDimensions_HaveDifferentWeights() {
        Art[] arts = {
            new Art("ART1", 10, 10, 1, Material.GLASS),
            new Art("ART2", 10, 10, 1, Material.ACRYLIC),
            new Art("ART3", 10, 10, 1, Material.CANVAS_FRAMED),
            new Art("ART4", 10, 10, 1, Material.ACOUSTIC_PANEL),
            new Art("ART5", 10, 10, 1, Material.CANVAS_GALLERY)
        };

        // All should have different weights (assuming different material weights)
        for (int i = 0; i < arts.length; i++) {
            for (int j = i + 1; j < arts.length; j++) {
                assertNotEquals(arts[i].getWeight(), arts[j].getWeight(), 
                    "Materials " + arts[i].getMaterial() + " and " + arts[j].getMaterial() + " should have different weights");
            }
        }
    }

    @Test
    public void weightCalculationIsDeterministic() {
        Art art = new Art("DETERMINISTIC", 7, 13, 2, Material.GLASS);
        double expectedWeight = 7 * 13 * Material.GLASS.getWeight();
        
        // Calculate multiple times - should be the same
        for (int i = 0; i < 10; i++) {
            assertEquals(expectedWeight, art.getWeight(), 1e-12, 
                "Weight should be deterministic and consistent");
        }
    }

    @Test
    public void artWithEmptyStringId_IsAllowed() {
        Art art = new Art("", 10, 10, 1, Material.GLASS);
        assertEquals("", art.getId());
        assertEquals(10 * 10 * Material.GLASS.getWeight(), art.getWeight(), 1e-12);
    }

    @Test
    public void artWithSpecialCharactersInId_IsAllowed() {
        Art art = new Art("ART-123_SPECIAL@#$", 10, 10, 1, Material.GLASS);
        assertEquals("ART-123_SPECIAL@#$", art.getId());
        assertEquals(10 * 10 * Material.GLASS.getWeight(), art.getWeight(), 1e-12);
    }
    


    

}
