package archdesign.integrationBox.VaryingSizes;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.BoxViewModel;
import archdesign.response.ArtViewModel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.nio.file.Files;
import com.google.gson.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ Automatically tests all subfolders under /box_packing/VaryingSizes/
 * Each folder must contain: input.csv + expected_output.json
 */
public class VaryingSizesAllTests {

    private static final String BASE_DIR = "/box_packing/VaryingSizes";

    /**
     * Provides all subfolder names (e.g., "35.5x35.5", "36x36", "43.5x88")
     */
    static Stream<String> folderProvider() throws Exception {
        URL baseUrl = VaryingSizesAllTests.class.getResource(BASE_DIR);
        assertNotNull(baseUrl, "❌ Base directory not found: " + BASE_DIR);
        Path basePath = Paths.get(baseUrl.toURI());

        try (Stream<Path> stream = Files.list(basePath)) {
            return stream
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList())
                    .stream();
        }
    }

    @ParameterizedTest(name = "Test VaryingSizes case: {0}")
    @MethodSource("folderProvider")
    public void testAllVaryingSizes(String folderName) throws Exception {
        System.out.println(">>> Running test for folder: " + folderName);

        // 1️⃣ Locate input and expected files
        URL inputUrl = getClass().getResource(BASE_DIR + "/" + folderName + "/input.csv");
        URL expectedUrl = getClass().getResource(BASE_DIR + "/" + folderName + "/expected_output.json");

        assertNotNull(inputUrl, "❌ input.csv missing in " + folderName);
        assertNotNull(expectedUrl, "❌ expected_output.json missing in " + folderName);

        Path inputPath = Paths.get(inputUrl.toURI());
        Path expectedPath = Paths.get(expectedUrl.toURI());

        // 2️⃣ Run main packing algorithm
        ShipmentViewModel vm = Main.processFile(inputPath.toString());

        // 3️⃣ Read expected_output.json
        JsonObject expected = JsonParser.parseString(Files.readString(expectedPath)).getAsJsonObject();
        int expectedStandard = expected.get("standard_box_count").getAsInt();
        int expectedLarge = expected.get("large_box_count").getAsInt();
        int expectedCrate = expected.has("crate_count") ? expected.get("crate_count").getAsInt() : 0;
        int expectedTotal = expected.get("total_pieces").getAsInt();

        // 4️⃣ Gather actual result
        int standardBoxCount = 0;
        int largeBoxCount = 0;
        int crateCount = 0;
        int totalPieces = 0;
        List<String> debug = new ArrayList<>();

        for (ContainerViewModel container : vm.containers()) {
            debug.add("  Container: " + container.type());
            for (BoxViewModel box : container.boxes()) {
                String bt = box.type();
                if ("STANDARD".equals(bt)) standardBoxCount++;
                else if ("LARGE".equals(bt)) largeBoxCount++;
                else if ("CRATE".equals(bt)) crateCount++;

                debug.add("    Box: " + bt + " (" + box.arts().size() + " art pieces)");
                for (ArtViewModel art : box.arts()) totalPieces++;
            }
        }

        // 5️⃣ Print debug info
        System.out.println("--- Shipment Debug (" + folderName + ") ---");
        debug.forEach(System.out::println);
        System.out.println("standard_box_count = " + standardBoxCount);
        System.out.println("large_box_count = " + largeBoxCount);
        System.out.println("crate_count = " + crateCount);
        System.out.println("total_pieces = " + totalPieces);
        System.out.println("------------------------");

        // 6️⃣ Assertions
        assertEquals(expectedStandard, standardBoxCount, "standard_box_count mismatch in " + folderName);
        assertEquals(expectedLarge, largeBoxCount, "large_box_count mismatch in " + folderName);
        assertEquals(expectedCrate, crateCount, "crate_count mismatch in " + folderName);
        assertEquals(expectedTotal, totalPieces, "total_pieces mismatch in " + folderName);
    }
}
