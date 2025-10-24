package archdesign.e2e;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;

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

    // Ensure unique art ids (quantity expansion produced distinct entries)
    int flattenedCount = vm.containers().stream()
        .mapToInt(c -> c.boxes().stream().mapToInt(b -> b.arts().size()).sum())
        .sum();
    assertEquals(flattenedCount, vmArtIds.size(), "Art ids should be unique after expansion");

        // Expected input art ids (after quantity expansion)
        Set<String> expected = Set.of(
                "TagINV1-Item1",
                "TagINV2-Item1",
                "TagINV2-Item2"
        );

        assertTrue(vmArtIds.containsAll(expected), "VM must contain all expected art ids");

        // Basic consistency: totalWeight reported should equal the sum of container weights
        double sumContainerWeights = vm.containers().stream().mapToDouble(c -> c.weight()).sum();
        assertEquals(vm.totalWeight(), sumContainerWeights, 1e-6, "Total weight must equal sum of container weights");

        // Cross-check against golden CSV to ensure exact layout (more strict)
        Path golden = resourcePath("golden/sample_input_small_golden.csv");
        String goldenCsv = Files.readString(golden).replace("\r\n", "\n").trim();
        // Build actual CSV from view model same as other golden test helper
        String actual = archdesign.e2e.testutils.ViewModelCsv.toCsv(vm).replace("\r\n", "\n").trim();
        assertEquals(goldenCsv, actual, "Small sample should match golden CSV output");
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

        // Additional consistency checks for large sample
        // 1) Each art should have a positive weight and sensible dimensions
        vm.containers().forEach(c -> c.boxes().forEach(b -> b.arts().forEach(a -> {
            assertNotNull(a.id());
            assertTrue(a.width() > 0, "Art width must be positive");
            assertTrue(a.height() > 0, "Art height must be positive");
            assertTrue(Double.isFinite(a.weight()), "Art weight must be finite");
            assertTrue(a.weight() > 0, "Art weight must be positive");
        })));

        // 2) IDs should follow the generated pattern (Tag<xx>-Item<yy>)
        vm.containers().forEach(c -> c.boxes().forEach(b -> b.arts().forEach(a -> {
            assertTrue(a.id().matches("Tag\\w+-Item\\d+"), "Art id should follow Tag<id>-Item<n> pattern");
        })));

        // 3) Verify the reported total boxes/containers equal the flattened counts
        int countedBoxes = vm.containers().stream().mapToInt(c -> c.boxes().size()).sum();
        assertEquals(vm.totalBoxes(), countedBoxes, "totalBoxes must equal the actual number of boxes");
        int countedContainers = vm.containers().size();
        assertEquals(vm.totalContainers(), countedContainers, "totalContainers must equal the actual number of containers");
    }
}
