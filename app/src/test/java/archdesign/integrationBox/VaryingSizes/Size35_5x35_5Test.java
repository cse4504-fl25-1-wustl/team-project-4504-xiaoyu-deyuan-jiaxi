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
 * Test for VaryingSizes/35.5x35.5
 * Expected behavior:
 * - width & height <= 36 → should fit STANDARD box
 */
public class Size35_5x35_5Test {

    private static final String BASE_DIR = "/box_packing/VaryingSizes/35.5x35.5";

    @Test
    public void test_35_5x35_5_StandardBox() throws Exception {
        System.out.println(">>> Running test for folder: 35.5x35.5");

        URL inputUrl = getClass().getResource(BASE_DIR + "/input.csv");
        assertNotNull(inputUrl, "input.csv missing in 35.5x35.5");
        Path inputPath = Paths.get(inputUrl.toURI());

        ShipmentViewModel vm = Main.processFile(inputPath.toString());

        int standardBoxCount = 0;
        int largeBoxCount = 0;
        int crateCount = 0;
        int totalPieces = 0;
        List<String> debug = new ArrayList<>();

        for (ContainerViewModel container : vm.containers()) {
            debug.add("  Container: " + container.type());
            if ("STANDARD_CRATE".equals(container.type())) crateCount++;

            for (BoxViewModel box : container.boxes()) {
                String bt = box.type();
                if ("STANDARD".equals(bt)) standardBoxCount++;
                else if ("LARGE".equals(bt)) largeBoxCount++;
                else if ("CRATE".equals(bt)) crateCount++;

                debug.add("    Box: " + bt + " (" + box.arts().size() + " art pieces)");
                for (ArtViewModel art : box.arts()) totalPieces++;
            }
        }

        System.out.println("--- Shipment Debug (35.5x35.5) ---");
        debug.forEach(System.out::println);
        System.out.println("standard_box_count = " + standardBoxCount);
        System.out.println("large_box_count = " + largeBoxCount);
        System.out.println("crate_count = " + crateCount);
        System.out.println("total_pieces = " + totalPieces);
        System.out.println("------------------------");

        // ✅ Expected behavior
        assertEquals(1, standardBoxCount, "Expected one standard box for 35.5x35.5 art piece");
        assertEquals(0, largeBoxCount, "Should not use large box for 35.5x35.5");
        assertEquals(0, crateCount, "Should not use crate for 35.5x35.5");
        assertEquals(1, totalPieces, "Should have exactly one art piece in shipment");
    }
}
