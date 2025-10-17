package entities;

import entities.enums.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArtTest {

    @Test
    public void weightCalculationUsesMaterialFactor() {
        Art a = new Art("A1", 10, 20, 1, Material.GLASS);
        // weight = height * width * material.getWeight()
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
        // The constructor accepts a thickness parameter but weight is computed
        // only from height, width, and material weight factor.
        Art t1 = new Art("T1", 4, 6, 1, Material.ACRYLIC);
        Art t2 = new Art("T2", 4, 6, 10, Material.ACRYLIC);
        assertEquals(t1.getWeight(), t2.getWeight(), 1e-12);
        assertEquals(t1.getHeight(), t2.getHeight());
        assertEquals(t1.getWidth(), t2.getWidth());
        assertEquals(t1.getMaterial(), t2.getMaterial());
    }
}
