package archdesign.integrationBox.SameMediumMixedSize;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.BoxViewModel;
import archdesign.response.ArtViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class StandardBox_1Standard2LargeTest {

    @Test
    public void test_1Standard2Large() throws Exception {
        URL resource = getClass().getResource("/box_packing/SameMediumMixedSize/1Standard2Large/input.csv");
        assertNotNull(resource, "input.csv not found");
        Path path = Paths.get(resource.toURI());

        ShipmentViewModel vm = Main.processFile(path.toString());

        int totalPieces = 0;
        int standardBoxCount = 0;
        int largeBoxCount = 0;
        int crateCount = 0;
        int standardPalletCount = 0;
        int oversizedPalletCount = 0;

        for (ContainerViewModel container : vm.containers()) {
            String containerType = container.type();
            // Count container types
            if ("STANDARD_PALLET".equals(containerType)) standardPalletCount++;
            else if ("OVERSIZE_PALLET".equals(containerType)) oversizedPalletCount++;

            // Count box types within each container
            for (BoxViewModel box : container.boxes()) {
                String boxType = box.type();
                if ("STANDARD".equals(boxType)) standardBoxCount++;
                else if ("LARGE".equals(boxType)) largeBoxCount++;
                else if ("CRATE".equals(boxType)) crateCount++;
                
                totalPieces += box.arts().size();
            }
        }

        URL expectedUrl = getClass().getResource("/box_packing/SameMediumMixedSize/1Standard2Large/expected_output.json");
        assertNotNull(expectedUrl, "expected_output.json missing");
        Path expectedPath = Paths.get(expectedUrl.toURI());
        String json = Files.readString(expectedPath);
        JsonObject obj = new Gson().fromJson(json, JsonObject.class);

        if (obj.has("total_pieces")) {
            assertEquals(obj.get("total_pieces").getAsInt(), totalPieces, "total_pieces mismatch");
        }
        if (obj.has("standard_box_count")) {
            assertEquals(obj.get("standard_box_count").getAsInt(), standardBoxCount, "standard_box_count mismatch");
        }
        if (obj.has("large_box_count")) {
            assertEquals(obj.get("large_box_count").getAsInt(), largeBoxCount, "large_box_count mismatch");
        }
        if (obj.has("standard_pallet_count")) {
            assertEquals(obj.get("standard_pallet_count").getAsInt(), standardPalletCount, "standard_pallet_count mismatch");
        }
        if (obj.has("oversized_pallet_count")) {
            assertEquals(obj.get("oversized_pallet_count").getAsInt(), oversizedPalletCount, "oversized_pallet_count mismatch");
        }
        if (obj.has("crate_count")) {
            assertEquals(obj.get("crate_count").getAsInt(), crateCount, "crate_count mismatch");
        }
    }
}
