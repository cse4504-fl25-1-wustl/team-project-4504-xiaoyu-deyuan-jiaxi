package archdesign.e2e;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests that run the full pipeline on sample CSV inputs.
 * These tests check high-level invariants: total counts are non-negative, every
 * input art id appears somewhere in the produced view model, and totals make sense.
 */
public class IntegrationTest {

    private Path resourcePath(String name) throws URISyntaxException {
        URL res = this.getClass().getClassLoader().getResource("e2e/" + name);
        assertNotNull(res, "Test resource not found: " + name);
        return Path.of(res.toURI());
    }

    @Test
    public void smallSample_EndToEnd_HappyPath() throws Exception {
        Path input = resourcePath("sample_input_small.csv");
        ShipmentViewModel vm = Main.processFile(input.toString());

        // Basic invariants
        assertNotNull(vm);
        assertTrue(vm.totalBoxes() >= 0);
        assertTrue(vm.totalContainers() >= 0);
        assertTrue(vm.totalWeight() >= 0.0);

        // Collect all art ids from the VM
        Set<String> vmArtIds = new HashSet<>();
        vm.containers().forEach(c -> c.boxes().forEach(b -> b.arts().forEach(a -> vmArtIds.add(a.id()))));

        // Expected input art ids (after quantity expansion)
        Set<String> expected = Set.of(
                "TagINV1-Item1",
                "TagINV2-Item1",
                "TagINV2-Item2"
        );

        assertTrue(vmArtIds.containsAll(expected), "VM must contain all expected art ids");
    }

    @Test
    public void largeSample_EndToEnd_ProducesFiniteTotals() throws Exception {
        Path input = resourcePath("sample_input_large.csv");
        ShipmentViewModel vm = Main.processFile(input.toString());

        assertNotNull(vm);
        // Totals should be finite numbers (not NaN or infinite) and non-negative
        assertTrue(Double.isFinite(vm.totalWeight()));
        assertTrue(vm.totalWeight() >= 0.0);
        assertTrue(Double.isFinite(vm.totalCost()));
        assertTrue(vm.totalCost() >= 0.0);

        // Ensure at least one container when there are arts
        boolean hasArts = vm.containers().stream().anyMatch(c -> c.boxes().stream().anyMatch(b -> !b.arts().isEmpty()));
        if (hasArts) {
            assertTrue(vm.totalContainers() > 0);
        }
    }
}
