package archdesign.integration;

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

public class PalletPackingStandardBox15Test {

    @Test
    public void testStandardBox15() throws Exception {
        URL resource = getClass().getResource("/pallet_packing/oversized_pallet/standard_box_15/input.csv");
        Path path = Paths.get(resource.toURI());

        ShipmentViewModel vm = Main.processFile(path.toString());

        int totalPieces = 0;
        int standardPalletCount = 0;
        int oversizedPalletCount = 0;
        int crateCount = 0;

        for (ContainerViewModel container : vm.containers()) {
            String t = container.type();
            if ("STANDARD_PALLET".equals(t)) standardPalletCount++;
            else if ("OVERSIZE_PALLET".equals(t)) oversizedPalletCount++;
            else if ("STANDARD_CRATE".equals(t)) crateCount++;

            for (BoxViewModel box : container.boxes()) {
                for (ArtViewModel art : box.arts()) {
                    totalPieces++;
                }
            }
        }

        URL expectedUrl = getClass().getResource("/pallet_packing/oversized_pallet/standard_box_15/expected_output.json");
        assertNotNull(expectedUrl, "expected_output.json missing");
        Path expectedPath = Paths.get(expectedUrl.toURI());
        String json = Files.readString(expectedPath);
        JsonObject obj = new Gson().fromJson(json, JsonObject.class);

        if (obj.has("total_pieces")) {
            assertEquals(obj.get("total_pieces").getAsInt(), totalPieces, "total_pieces mismatch");
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
