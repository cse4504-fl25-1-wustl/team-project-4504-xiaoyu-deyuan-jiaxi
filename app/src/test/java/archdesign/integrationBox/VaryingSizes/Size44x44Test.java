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
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for VaryingSizes/44x44
 * Expected: Should be UNPACKABLE (width 44 > 43.5 large box limit, and height 44 > 43.5)
 * Since neither dimension fits large box width constraint, it becomes a custom piece.
 */
public class Size44x44Test {

    private static final String BASE_DIR = "/box_packing/VaryingSizes/44x44";

    @Test
    public void test_44x44_CustomPiece() throws Exception {
        System.out.println(">>> Running test for folder: 44x44");

        URL inputUrl = getClass().getResource(BASE_DIR + "/input.csv");
        assertNotNull(inputUrl, "input.csv missing in 44x44");
        Path inputPath = Paths.get(inputUrl.toURI());

        // Integration box tests should ONLY use STANDARD and LARGE boxes, NOT crates
        ShipmentViewModel vm = Main.processFile(inputPath.toString(), "box-only");

        int standardBoxCount = 0;
        int largeBoxCount = 0;
        int crateCount = 0;
        int totalPieces = 0;
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
                for (ArtViewModel art : box.arts()) totalPieces++;
            }
        }

        System.out.println("--- Shipment Debug (44x44) ---");
        debug.forEach(System.out::println);
        System.out.println("standard_box_count = " + standardBoxCount);
        System.out.println("large_box_count = " + largeBoxCount);
        System.out.println("crate_count = " + crateCount);
        System.out.println("custom_piece_count = " + customPieceCount);
        System.out.println("total_pieces (packed) = " + totalPieces);
        System.out.println("------------------------");

        // ✅ Assertions according to expected_output.json
        assertEquals(0, standardBoxCount, "44x44 should not use standard box");
        assertEquals(0, largeBoxCount, "44x44 should not use large box");
        assertEquals(0, crateCount, "44x44 should not use crate (integration box test)");
        assertEquals(1, customPieceCount, "44x44 should be counted as custom piece");
        // Note: totalPieces only counts packed arts, not custom pieces
        // expected_output.json says total_pieces=1, which likely includes custom_piece_count
        assertEquals(1, customPieceCount + totalPieces, "Total should be 1 (all custom pieces)");
    }
}
