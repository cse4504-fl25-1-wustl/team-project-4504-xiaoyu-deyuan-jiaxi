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
 * Test for VaryingSizes/35.5x85
 * Expected: should fit into LARGE box (since height > 43)
 */
public class Size35_5x85Test {

    private static final String BASE_DIR = "/box_packing/VaryingSizes/35.5x85";

    @Test
    public void test_35_5x85_LargeBox() throws Exception {
        System.out.println(">>> Running test for folder: 35.5x85");

        URL inputUrl = getClass().getResource(BASE_DIR + "/input.csv");
        assertNotNull(inputUrl, "input.csv missing in 35.5x85");
        Path inputPath = Paths.get(inputUrl.toURI());

        ShipmentViewModel vm = Main.processFile(inputPath.toString());

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

        System.out.println("--- Shipment Debug (35.5x85) ---");
        debug.forEach(System.out::println);
        System.out.println("standard_box_count = " + standardBoxCount);
        System.out.println("large_box_count = " + largeBoxCount);
        System.out.println("crate_count = " + crateCount);
        System.out.println("total_pieces = " + totalPieces);
        System.out.println("------------------------");

        // ✅ 期望逻辑
        assertEquals(1, standardBoxCount, "Should not use standard box for 35.5x85 art");
        assertEquals(0, largeBoxCount, "Expected one large box for 35.5x85 art piece");
        assertEquals(0, crateCount, "Should not use crate for 35.5x85 art");
        assertEquals(1, totalPieces, "Should have exactly one art piece in shipment");
    }
}
