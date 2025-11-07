package archdesign.integration;

import archdesign.Main;
import archdesign.response.ArtViewModel;
import archdesign.response.BoxViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.ShipmentViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CratePackingShort18_2_NewTest {

    @Test
    void testShort18PerCrates2_matchesExpectedJson() throws Exception {
        URL inUrl = getClass().getResource("/crate_packing/SameSizeSameMedium/pack_short_direction/18_per_crates_2/input.csv");
        assertNotNull(inUrl, "Input resource missing");
        Path inPath = Paths.get(inUrl.toURI());

        
        ShipmentViewModel vm = Main.processFile(inPath.toString(), "crate-only");
        assertNotNull(vm, "ShipmentViewModel should not be null");

        int totalPieces = 0;
        int standardSizePieces = 0;
        Map<String, Integer> oversizeMap = new LinkedHashMap<>();
        int standardPalletCount = 0;
        int oversizedPalletCount = 0;
        int crateCount = 0;
        double totalArtworkWeight = 0.0;

        for (ContainerViewModel container : vm.containers()) {
            String t = container.type();
            if ("STANDARD_PALLET".equals(t)) standardPalletCount++;
            else if ("OVERSIZE_PALLET".equals(t)) oversizedPalletCount++;
            else if ("STANDARD_CRATE".equals(t)) crateCount++;

            for (BoxViewModel box : container.boxes()) {
                for (ArtViewModel art : box.arts()) {
                    totalPieces++;
                    double w = art.width();
                    double h = art.height();
                    if (w <= 44 && h <= 44) {
                        standardSizePieces++;
                    } else {
                        double side1 = Math.max(w, h);
                        double side2 = Math.min(w, h);
                        String key = side1 + "x" + side2;
                        oversizeMap.put(key, oversizeMap.getOrDefault(key, 0) + 1);
                    }
                    totalArtworkWeight += art.weight();
                }
            }
        }

        double finalShipmentWeight = vm.totalWeight();
        double totalPackagingWeight = finalShipmentWeight - totalArtworkWeight;

        URL expectedUrl = getClass().getResource("/crate_packing/SameSizeSameMedium/pack_short_direction/18_per_crates_2/expected_output.json");
        assertNotNull(expectedUrl, "expected_output.json missing");
        Path expectedPath = Paths.get(expectedUrl.toURI());
        String json = Files.readString(expectedPath);
        JsonObject obj = new Gson().fromJson(json, JsonObject.class);

        if (obj.has("total_pieces")) {
            assertEquals(obj.get("total_pieces").getAsInt(), totalPieces, "total_pieces mismatch");
        }
        if (obj.has("standard_size_pieces")) {
            assertEquals(obj.get("standard_size_pieces").getAsInt(), standardSizePieces, "standard_size_pieces mismatch");
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

        if (obj.has("total_artwork_weight")) {
            assertEquals(obj.get("total_artwork_weight").getAsDouble(), totalArtworkWeight, 0.5, "total_artwork_weight mismatch");
        }
        if (obj.has("total_packaging_weight")) {
            assertEquals(obj.get("total_packaging_weight").getAsDouble(), totalPackagingWeight, 0.5, "total_packaging_weight mismatch");
        }
        if (obj.has("final_shipment_weight")) {
            assertEquals(obj.get("final_shipment_weight").getAsDouble(), finalShipmentWeight, 0.5, "final_shipment_weight mismatch");
        }

        
    }
}
