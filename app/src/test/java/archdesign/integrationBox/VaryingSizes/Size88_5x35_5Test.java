package archdesign.integrationBox.VaryingSizes;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.BoxViewModel;
import archdesign.response.ArtViewModel;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.nio.file.Files;
import com.google.gson.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for VaryingSizes/88.5x35.5
 * ✅ Expected: UNPACKABLE (width 88.5 > 88" hard limit)
 * Should be counted as custom_piece
 */
public class Size88_5x35_5Test {

    private static final String BASE_DIR = "/box_packing/VaryingSizes/88.5x35.5";

    @Test
    public void test_88_5x35_5_CustomPiece() throws Exception {
        System.out.println(">>> Running test for folder: 88.5x35.5");

        // 1️⃣ Load input and expected_output
        URL inputUrl = getClass().getResource(BASE_DIR + "/input.csv");
        URL expectedUrl = getClass().getResource(BASE_DIR + "/expected_output.json");
        assertNotNull(inputUrl, "input.csv missing in 88.5x35.5");
        assertNotNull(expectedUrl, "expected_output.json missing in 88.5x35.5");

        Path inputPath = Paths.get(inputUrl.toURI());
        Path expectedPath = Paths.get(expectedUrl.toURI());

        // 2️⃣ Run program with box-only mode (no crates for integration box tests)
        ShipmentViewModel vm = Main.processFile(inputPath.toString(), "box-only");

        // 3️⃣ Read expected_output.json
        JsonObject expected = JsonParser.parseString(Files.readString(expectedPath)).getAsJsonObject();
        int expectedStandard = expected.get("standard_box_count").getAsInt();
        int expectedLarge = expected.get("large_box_count").getAsInt();
        int expectedCrate = expected.has("crate_count") ? expected.get("crate_count").getAsInt() : 0;
        int expectedCustom = expected.has("custom_piece_count") ? expected.get("custom_piece_count").getAsInt() : 0;
        int expectedTotal = expected.get("total_pieces").getAsInt();

        // 4️⃣ Collect actual stats
        int standardBoxCount = 0;
        int largeBoxCount = 0;
        int crateCount = 0;
        int totalPackedPieces = 0;
        int customPieceCount = vm.unpackedArts().size();
        List<String> debug = new ArrayList<>();

        for (ContainerViewModel container : vm.containers()) {
            debug.add("  Container: " + container.type());
            for (BoxViewModel box : container.boxes()) {
                String bt = box.type();
                if ("STANDARD".equals(bt)) standardBoxCount++;
                else if ("LARGE".equals(bt)) largeBoxCount++;
                else if ("CRATE".equals(bt)) crateCount++;

                debug.add("    Box: " + bt + " (" + box.arts().size() + " art pieces)");
                for (ArtViewModel art : box.arts()) totalPackedPieces++;
            }
        }

        // 5️⃣ Print debug output
        System.out.println("--- Shipment Debug (88.5x35.5) ---");
        debug.forEach(System.out::println);
        System.out.println("standard_box_count = " + standardBoxCount);
        System.out.println("large_box_count = " + largeBoxCount);
        System.out.println("crate_count = " + crateCount);
        System.out.println("custom_piece_count = " + customPieceCount);
        System.out.println("total_packed_pieces = " + totalPackedPieces);
        System.out.println("------------------------");

        // 6️⃣ Assertions
        assertEquals(expectedStandard, standardBoxCount, "standard_box_count mismatch");
        assertEquals(expectedLarge, largeBoxCount, "large_box_count mismatch");
        assertEquals(expectedCrate, crateCount, "crate_count mismatch");
        assertEquals(expectedCustom, customPieceCount, "custom_piece_count mismatch");
        // total_pieces in expected_output.json includes both packed and custom pieces
        assertEquals(expectedTotal, totalPackedPieces + customPieceCount, "total_pieces mismatch");
    }
}
