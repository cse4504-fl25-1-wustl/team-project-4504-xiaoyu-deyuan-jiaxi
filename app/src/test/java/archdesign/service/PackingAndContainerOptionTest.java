package archdesign.service;

import static org.junit.jupiter.api.Assertions.*;

import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import org.junit.jupiter.api.Test;

class PackingAndContainerOptionTest {

    @Test
    void packingOption_record_accessors() {
    PackingOption opt = new PackingOption(BoxType.STANDARD, 3);
    assertEquals(BoxType.STANDARD, opt.boxType());
        assertEquals(3, opt.capacity());
    }

    @Test
    void containerOption_record_accessors() {
    ContainerOption opt = new ContainerOption(ContainerType.STANDARD_CRATE, 10);
    assertEquals(ContainerType.STANDARD_CRATE, opt.containerType());
        assertEquals(10, opt.capacity());
    }
}
