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
 * Test for VaryingSizes/37x88
 * Expected: Should use LARGE box (since width >= 37 and height >= 37)
 */
public class Size37x88Test {

    private static final String BASE_DIR = "/box_packing/VaryingSizes/37x88";

    @Test
    public void test_37x88_LargeBox() throws Exception {
        System.out.println(">>> Running test for folder: 37x88");

        URL inputUrl = getClass().getResource(BASE_DIR + "/input.csv");
        assertNotNull(inputUrl, "input.csv missing in 37x88");
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

        System.out.println("--- Shipment Debug (37x88) ---");
        debug.forEach(System.out::println);
        System.out.println("standard_box_count = " + standardBoxCount);
        System.out.println("large_box_count = " + largeBoxCount);
        System.out.println("crate_count = " + crateCount);
        System.out.println("total_pieces = " + totalPieces);
        System.out.println("------------------------");

        // ✅ Expected results (from expected_output.json)
        assertEquals(0, standardBoxCount, "37x88 should not use standard box");
        assertEquals(1, largeBoxCount, "Expected one large box for 37x88 art piece");
        assertEquals(0, crateCount, "37x88 should not use crate");
        assertEquals(1, totalPieces, "Should have exactly one art piece");
    }
}
