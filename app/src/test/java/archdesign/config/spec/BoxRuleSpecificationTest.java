package archdesign.config.spec;

import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoxRuleSpecificationTest {

    @Test
    void buildWithAllFields_setsGetters() {
        BoxRuleSpecification rule = BoxRuleSpecification.newBuilder("desc", BoxType.STANDARD, 2)
                .forMaterial(Material.GLASS)
                .withMinWidth(5)
                .withMaxWidth(10)
                .withMinHeight(6)
                .withMaxHeight(12)
                .build();

        assertEquals("desc", rule.getDescription());
        assertEquals(Material.GLASS, rule.getMaterial());
        assertEquals(5, rule.getMinWidth());
        assertEquals(10, rule.getMaxWidth());
        assertEquals(6, rule.getMinHeight());
        assertEquals(12, rule.getMaxHeight());
        assertEquals(BoxType.STANDARD, rule.getAllowedBoxType());
        assertEquals(2, rule.getCapacity());
    }

    @Test
    void defaultsWhenNotSet() {
        BoxRuleSpecification rule = BoxRuleSpecification.newBuilder("minimal", BoxType.LARGE, 1).build();

        assertEquals("minimal", rule.getDescription());
        assertNull(rule.getMaterial());
        assertEquals(0, rule.getMinWidth());
        assertEquals(Integer.MAX_VALUE, rule.getMaxWidth());
        assertEquals(0, rule.getMinHeight());
        assertEquals(Integer.MAX_VALUE, rule.getMaxHeight());
        assertEquals(BoxType.LARGE, rule.getAllowedBoxType());
        assertEquals(1, rule.getCapacity());
    }
}
