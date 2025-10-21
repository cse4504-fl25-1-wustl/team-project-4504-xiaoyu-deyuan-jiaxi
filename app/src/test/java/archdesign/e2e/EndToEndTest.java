package archdesign.e2e;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class EndToEndTest {

    private Path resourcePath(String name) throws URISyntaxException {
        URL res = this.getClass().getClassLoader().getResource("e2e/" + name);
        assertNotNull(res, "Test resource not found: " + name);
        return Path.of(res.toURI());
    }

    @Test
    public void e2e_smallInput_generatesExpectedCounts() throws Exception {
        Path p = resourcePath("sample_input_small.csv");
        ShipmentViewModel vm = Main.processFile(p.toString());
        assertNotNull(vm);

        // small sample: T1 (1 art), T2 (2 arts) => total arts = 3
        // Expect totalBoxes >= 1 and totalContainers >= 1
        assertTrue(vm.totalBoxes() >= 1);
        assertTrue(vm.totalContainers() >= 1);

        // verify total weight and cost are positive
        assertTrue(vm.totalWeight() > 0);
        assertTrue(vm.totalCost() >= 0);
    }

    @Test
    public void e2e_largeInput_generatesFiniteWeight() throws Exception {
        Path p = resourcePath("sample_input_large.csv");
        ShipmentViewModel vm = Main.processFile(p.toString());
        assertNotNull(vm);

    // large items should produce a finite total weight (may be 0 if items couldn't be packed)
    assertTrue(Double.isFinite(vm.totalWeight()));
    assertTrue(vm.totalWeight() >= 0);
    }
}
