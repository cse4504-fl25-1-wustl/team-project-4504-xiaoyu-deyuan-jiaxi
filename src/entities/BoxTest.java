package entities;

import entities.enums.BoxType;
import entities.enums.Material;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoxTest {

    @Test
    void testGetCurrentHeight_EmptyBox() {
        Box box = new Box("B1", BoxType.STANDARD, 37, 31, 10);
        assertEquals(10, box.getCurrentHeight());
    }

    @Test
    void testGetCurrentHeight_WithArts() {
        Box box = new Box("B2", BoxType.STANDARD, 37, 31, 10);
        box.addArt(new Art("A1", 8, 20, 5, Material.GLASS));
        box.addArt(new Art("A2", 15, 20, 5, Material.GLASS));
        assertEquals(15, box.getCurrentHeight());
    }

    @Test
    void testGetTotalWeight() {
        Box box = new Box("B3", BoxType.STANDARD, 37, 31, 10);
        box.addArt(new Art("A1", 5, 10, 5, Material.ACRYLIC));
        box.addArt(new Art("A2", 5, 5, 5, Material.GLASS));
        double expected = 5 * 10 * Material.ACRYLIC.getWeight() + 5 * 5 * Material.GLASS.getWeight();
        assertEquals(expected, box.getTotalWeight(), 0.0001);
    }
}
