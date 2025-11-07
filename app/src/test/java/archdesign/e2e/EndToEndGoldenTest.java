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

        // Collect packed and unpacked arts
        java.util.List<archdesign.response.ArtViewModel> packedArts = new java.util.ArrayList<>();
        vm.containers().forEach(container -> container.boxes().forEach(box -> packedArts.addAll(box.arts())));
        java.util.List<archdesign.response.ArtViewModel> unpackedArts = vm.unpackedArts();

        int packedPieces = packedArts.size();
        int unpackedPieces = unpackedArts.size();
        int totalPieces = packedPieces + unpackedPieces;
        
        double packedArtworkWeight = packedArts.stream().mapToDouble(a -> a.weight()).sum();
        double unpackedArtworkWeight = unpackedArts.stream().mapToDouble(a -> a.weight()).sum();
        double totalArtworkWeight = packedArtworkWeight + unpackedArtworkWeight;
        
        int standardPieces = (int) packedArts.stream().filter(a -> !(a.width() > 44 || a.height() > 44)).count();
        java.util.Map<String, int[]> oversizePacked = new java.util.LinkedHashMap<>();
        java.util.Map<String, Double> oversizePackedWeight = new java.util.LinkedHashMap<>();
        for (archdesign.response.ArtViewModel a : packedArts) {
            if (a.width() > 44 || a.height() > 44) {
                String dims = a.width() + "\" x " + a.height() + "\"";
                oversizePacked.computeIfAbsent(dims, k -> new int[]{0})[0]++;
                oversizePackedWeight.put(dims, oversizePackedWeight.getOrDefault(dims, 0.0) + a.weight());
            }
        }

        // With new rules: all Input1 arts are packable (max dimension is 56" which is < 88", and at least one dimension <= 46")
        assertEquals(55, totalPieces, "Total pieces should be 55");
        assertEquals(55, packedPieces, "All pieces should be packed (largest is 32x56, one dimension <= 46)");
        assertEquals(0, unpackedPieces, "No unpacked pieces (all dimensions meet packaging limits)");
        
        assertEquals(49, standardPieces, "Standard size packed pieces should be 49");
        int oversizedPackedPieces = oversizePacked.values().stream().mapToInt(arr -> arr[0]).sum();
        assertEquals(6, oversizedPackedPieces, "Oversized packed pieces should be 6");

        // Check specific packed oversize groups - all can be packed with crate rotation
        assertTrue(oversizePacked.containsKey("34.0\" x 46.0\""), "Should contain packed oversize group 34\" x 46\"");
        assertEquals(2, oversizePacked.get("34.0\" x 46.0\"")[0], "Qty for packed 34\" x 46\" should be 2");

        // Weight validation
        assertEquals(784, Math.round(totalArtworkWeight), "Total Artwork Weight should be 784 lbs");
        double finalShipmentWeight = vm.totalWeight();
        double totalPackagingWeight = finalShipmentWeight - totalArtworkWeight;
        assertEquals(150, Math.round(totalPackagingWeight), "Total Packaging Weight should be 150 lbs");
        assertEquals(934, Math.round(finalShipmentWeight), "Final Shipment Weight should be 934 lbs");
    }

    @Test
    public void additional_input2_processesAndProducesCsv() throws Exception {
        
        Path input = null;
        try {
            input = resourcePath("Input2.csv");
        } catch (Throwable ignored) {
        }

        Path[] candidates = new Path[]{
                Path.of("Input2.csv"),
                Path.of("app", "Input2.csv"),
                Path.of(System.getProperty("user.dir"), "Input2.csv"),
                Path.of(System.getProperty("user.dir"), "app", "Input2.csv")
        };

        if (input == null || !Files.exists(input)) {
            for (Path c : candidates) {
                if (Files.exists(c)) {
                    input = c;
                    break;
                }
            }
        }

        assertNotNull(input, "Input2.csv could not be located in any of the candidate paths");
        assertTrue(Files.exists(input), "Input2.csv not found at " + input);

        ShipmentViewModel vm = Main.processFile(input.toString());
        assertNotNull(vm, "Processing returned null view model for Input2.csv");

        java.util.List<archdesign.response.ArtViewModel> allArts = new java.util.ArrayList<>();
        vm.containers().forEach(container -> container.boxes().forEach(box -> allArts.addAll(box.arts())));

        int totalPieces = allArts.size();
        double totalArtworkWeight = allArts.stream().mapToDouble(a -> a.weight()).sum();
        int standardPieces = (int) allArts.stream().filter(a -> !(a.width() > 44 || a.height() > 44)).count();

        // Assertions per user's expected summary for Input2.csv
        assertEquals(70, totalPieces, "Total pieces should be 70 for Input2.csv");
        // The user expects all to be standard size (estimated at 44" x 36")
        assertEquals(70, standardPieces, "Standard size pieces should be 70 for Input2.csv");
        // Artwork weight rounded up per piece — assert rounded total
        assertEquals(1120, Math.round(totalArtworkWeight), "Total Artwork Weight should be 1120 lbs (rounded)");

        double finalShipmentWeight = vm.totalWeight();
        double totalPackagingWeight = finalShipmentWeight - totalArtworkWeight;
        assertEquals(180, Math.round(totalPackagingWeight), "Total Packaging Weight should be 180 lbs");
        assertEquals(1300, Math.round(finalShipmentWeight), "Final Shipment Weight should be 1300 lbs");
    }

    @Test
    public void additional_input3_processesAndProducesCsv() throws Exception {
        
        Path input = null;
        try {
            input = resourcePath("Input3.csv");
        } catch (Throwable ignored) {
        }

        Path[] candidates = new Path[]{
                Path.of("Input3.csv"),
                Path.of("app", "Input3.csv"),
                Path.of(System.getProperty("user.dir"), "Input3.csv"),
                Path.of(System.getProperty("user.dir"), "app", "Input3.csv")
        };

        if (input == null || !Files.exists(input)) {
            for (Path c : candidates) {
                if (Files.exists(c)) {
                    input = c;
                    break;
                }
            }
        }

        assertNotNull(input, "Input3.csv could not be located in any of the candidate paths");
        assertTrue(Files.exists(input), "Input3.csv not found at " + input);

        ShipmentViewModel vm = Main.processFile(input.toString());
        assertNotNull(vm, "Processing returned null view model for Input3.csv");

        // Collect packed and unpacked arts
        java.util.List<archdesign.response.ArtViewModel> packedArts = new java.util.ArrayList<>();
        vm.containers().forEach(container -> container.boxes().forEach(box -> packedArts.addAll(box.arts())));
        java.util.List<archdesign.response.ArtViewModel> unpackedArts = vm.unpackedArts();

        int packedPieces = packedArts.size();
        int unpackedPieces = unpackedArts.size();
        int totalPieces = packedPieces + unpackedPieces;
        
        double totalArtworkWeight = packedArts.stream().mapToDouble(a -> a.weight()).sum() + 
                                   unpackedArts.stream().mapToDouble(a -> a.weight()).sum();
        
        int standardPieces = (int) packedArts.stream().filter(a -> !(a.width() > 44 || a.height() > 44)).count();

        // With new rules: all Input3 arts are packable (max dimension is 55" which is < 88", and at least one dimension <= 46")
        // 11 standard (33x43), 1 oversized (31x55), 1 oversized (34x47) - all packable
        assertEquals(13, totalPieces, "Total pieces should be 13 for Input3.csv");
        assertEquals(13, packedPieces, "All pieces should be packed (largest is 31x55, one dimension <= 46)");
        assertEquals(0, unpackedPieces, "No unpacked pieces (all dimensions meet packaging limits)");
        assertEquals(11, standardPieces, "Standard size pieces should be 11 for Input3.csv");

        assertEquals(187, Math.round(totalArtworkWeight), "Total Artwork Weight should be 187 lbs");
        double finalShipmentWeight = vm.totalWeight();
        double totalPackagingWeight = finalShipmentWeight - totalArtworkWeight;
        assertEquals(60, Math.round(totalPackagingWeight), "Total Packaging Weight should be 60 lbs");
        assertEquals(247, Math.round(finalShipmentWeight), "Final Shipment Weight should be 247 lbs");
    }

    @Test
    public void additional_input4_processesAndProducesCsv() throws Exception {
     
        Path input = null;
        try {
            input = resourcePath("Input4.csv");
        } catch (Throwable ignored) {
        }

        Path[] candidates = new Path[]{
                Path.of("Input4.csv"),
                Path.of("app", "Input4.csv"),
                Path.of(System.getProperty("user.dir"), "Input4.csv"),
                Path.of(System.getProperty("user.dir"), "app", "Input4.csv")
        };

        if (input == null || !Files.exists(input)) {
            for (Path c : candidates) {
                if (Files.exists(c)) {
                    input = c;
                    break;
                }
            }
        }

        assertNotNull(input, "Input4.csv could not be located in any of the candidate paths");
        assertTrue(Files.exists(input), "Input4.csv not found at " + input);

        ShipmentViewModel vm = Main.processFile(input.toString());
        assertNotNull(vm, "Processing returned null view model for Input4.csv");

        java.util.List<archdesign.response.ArtViewModel> allArts = new java.util.ArrayList<>();
        vm.containers().forEach(container -> container.boxes().forEach(box -> allArts.addAll(box.arts())));

        int totalPieces = allArts.size();
        double totalArtworkWeight = allArts.stream().mapToDouble(a -> a.weight()).sum();
        int standardPieces = (int) allArts.stream().filter(a -> !(a.width() > 44 || a.height() > 44)).count();

        assertEquals(18, totalPieces, "Total pieces should be 18 for Input4.csv");
        assertEquals(18, standardPieces, "Standard size pieces should be 18 for Input4.csv");
        assertEquals(234, Math.round(totalArtworkWeight), "Total Artwork Weight should be 234 lbs");

        double finalShipmentWeight = vm.totalWeight();
        double totalPackagingWeight = finalShipmentWeight - totalArtworkWeight;
        assertEquals(60, Math.round(totalPackagingWeight), "Total Packaging Weight should be 60 lbs");
        assertEquals(294, Math.round(finalShipmentWeight), "Final Shipment Weight should be 294 lbs");
    }
}
