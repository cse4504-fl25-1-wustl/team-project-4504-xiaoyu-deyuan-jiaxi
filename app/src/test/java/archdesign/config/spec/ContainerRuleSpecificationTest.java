package archdesign.config.spec;

import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerRuleSpecificationTest {

    @Test
    void buildAndGetters() {
        ContainerRuleSpecification rule = ContainerRuleSpecification.newBuilder("cdesc")
                .forContainerType(ContainerType.STANDARD_PALLET)
                .withAllowedBoxType(BoxType.CRATE)
                .withCapacity(5)
                .build();

        assertEquals("cdesc", rule.getDescription());
        assertEquals(ContainerType.STANDARD_PALLET, rule.getContainerType());
        assertEquals(BoxType.CRATE, rule.getAllowedBoxType());
        assertEquals(5, rule.getCapacity());
    }
}
