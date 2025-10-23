package archdesign.e2e;

import archdesign.Main;
import archdesign.e2e.testutils.ViewModelCsv;
import archdesign.response.ShipmentViewModel;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class EndToEndGoldenTest {

    private Path resourcePath(String name) throws URISyntaxException {
        URL res = this.getClass().getClassLoader().getResource("e2e/" + name);
        assertNotNull(res, "Test resource not found: " + name);
        return Path.of(res.toURI());
    }

    @Test
    public void golden_smallSample_matchesExpectedJson() throws Exception {
        Path input = resourcePath("sample_input_small.csv");
        ShipmentViewModel vm = Main.processFile(input.toString());
    String actual = ViewModelCsv.toCsv(vm).trim();

    URL goldenUrl = this.getClass().getClassLoader().getResource("e2e/golden/sample_input_small_golden.csv");
        assertNotNull(goldenUrl, "Golden file missing");
    String expected = Files.readString(Path.of(goldenUrl.toURI()));

    // Normalize newlines to LF and trim to avoid platform-dependent CRLF vs LF mismatches
    String normExpected = expected.replace("\r\n", "\n").replace("\r", "\n").trim();
    String normActual = actual.replace("\r\n", "\n").replace("\r", "\n").trim();

    assertEquals(normExpected, normActual);
    }

    @Test
    public void additional_input1_processesAndProducesCsv() throws Exception {
        
        Path input = null;
        try {
            input = resourcePath("Input1.csv");
        } catch (Throwable ignored) {
        }

        Path[] candidates = new Path[]{
                Path.of("Input1.csv"),
                Path.of("app", "Input1.csv"),
                Path.of(System.getProperty("user.dir"), "Input1.csv"),
                Path.of(System.getProperty("user.dir"), "app", "Input1.csv")
        };

        if (input == null || !Files.exists(input)) {
            for (Path c : candidates) {
                if (Files.exists(c)) {
                    input = c;
                    break;
                }
            }
        }

        assertNotNull(input, "Input1.csv could not be located in any of the candidate paths");
        assertTrue(Files.exists(input), "Input1.csv not found at " + input);

        ShipmentViewModel vm = Main.processFile(input.toString());
        assertNotNull(vm, "Processing returned null view model");

    String csv = ViewModelCsv.toCsv(vm);
    assertNotNull(csv, "CSV output should not be null");
    assertFalse(csv.trim().isEmpty(), "CSV output should not be empty for Input1.csv");

        java.util.List<archdesign.response.ArtViewModel> allArts = new java.util.ArrayList<>();
        vm.containers().forEach(container -> container.boxes().forEach(box -> allArts.addAll(box.arts())));

        int totalPieces = allArts.size();
        double totalArtworkWeight = allArts.stream().mapToDouble(a -> a.weight()).sum();
        int standardPieces = (int) allArts.stream().filter(a -> !(a.width() > 44 || a.height() > 44)).count();
        java.util.Map<String, int[]> oversize = new java.util.LinkedHashMap<>();
        java.util.Map<String, Double> oversizeWeight = new java.util.LinkedHashMap<>();
        for (archdesign.response.ArtViewModel a : allArts) {
            if (a.width() > 44 || a.height() > 44) {
                String dims = a.width() + "\" x " + a.height() + "\"";
                oversize.computeIfAbsent(dims, k -> new int[]{0})[0]++;
                oversizeWeight.put(dims, oversizeWeight.getOrDefault(dims, 0.0) + a.weight());
            }
        }

        assertEquals(55, totalPieces, "Total pieces should be 55");
        assertEquals(49, standardPieces, "Standard size pieces should be 49");
        int oversizedPieces = oversize.values().stream().mapToInt(arr -> arr[0]).sum();
        assertEquals(6, oversizedPieces, "Oversized pieces should be 6");

        // Check specific oversize groups and their total weights (rounded to integer)
        assertTrue(oversize.containsKey("34\" x 46\""), "Should contain oversize group 34\" x 46\"");
        assertEquals(2, oversize.get("34\" x 46\"")[0], "Qty for 34\" x 46\" should be 2");
        assertEquals(32, Math.round(oversizeWeight.get("34\" x 46\"") == null ? 0 : oversizeWeight.get("34\" x 46\"").doubleValue()), "Total weight for 34\" x 46\" should be 32 lbs");

        assertTrue(oversize.containsKey("32\" x 56\""), "Should contain oversize group 32\" x 56\"");
        assertEquals(1, oversize.get("32\" x 56\"")[0], "Qty for 32\" x 56\" should be 1");
        assertEquals(18, Math.round(oversizeWeight.get("32\" x 56\"") == null ? 0 : oversizeWeight.get("32\" x 56\"").doubleValue()), "Total weight for 32\" x 56\" should be 18 lbs");

        assertTrue(oversize.containsKey("32\" x 48\""), "Should contain oversize group 32\" x 48\"");
        assertEquals(3, oversize.get("32\" x 48\"")[0], "Qty for 32\" x 48\" should be 3");
        assertEquals(48, Math.round(oversizeWeight.get("32\" x 48\"") == null ? 0 : oversizeWeight.get("32\" x 48\"").doubleValue()), "Total weight for 32\" x 48\" should be 48 lbs");

        assertEquals(784, Math.round(totalArtworkWeight), "Total Artwork Weight should be 784 lbs");
        double finalShipmentWeight = vm.totalWeight();
        double totalPackagingWeight = finalShipmentWeight - totalArtworkWeight;
        assertEquals(150, Math.round(totalPackagingWeight), "Total Packaging Weight should be 150 lbs");
        assertEquals(934, Math.round(finalShipmentWeight), "Final Shipment Weight should be 934 lbs");
    }
}
