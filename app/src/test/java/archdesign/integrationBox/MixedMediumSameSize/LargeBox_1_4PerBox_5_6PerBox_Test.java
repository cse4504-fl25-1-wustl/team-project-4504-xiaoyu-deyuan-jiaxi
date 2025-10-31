package archdesign.integrationBox.MixedMediumSameSize;

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

public class LargeBox_1_4PerBox_5_6PerBox_Test {

    @Test
    public void testSingleCase() throws Exception {
        URL resource = getClass().getClassLoader().getResource(
                "box_packing/MixedMediumSameSize/LargeBox/1_4PerBox-5_6PerBox/input.csv");
        assertNotNull(resource, "input.csv not found");
        Path inputPath = Paths.get(resource.toURI());
        System.out.println("Input file: " + inputPath.toAbsolutePath());

        ShipmentViewModel vm = Main.processFile(inputPath.toString());

        int totalPieces = 0;
        int standardPalletCount = 0;
        int oversizedPalletCount = 0;
        int crateCount = 0;

        for (ContainerViewModel container : vm.containers()) {
            String type = container.type();
            if ("STANDARD_PALLET".equals(type)) standardPalletCount++;
            else if ("OVERSIZE_PALLET".equals(type)) oversizedPalletCount++;
            else if ("STANDARD_CRATE".equals(type)) crateCount++;

            for (BoxViewModel box : container.boxes()) {
                for (ArtViewModel art : box.arts()) {
                    totalPieces++;
                }
            }
        }

        URL expectedUrl = getClass().getClassLoader().getResource(
                "box_packing/MixedMediumSameSize/LargeBox/1_4PerBox-5_6PerBox/expected_output.json");
        assertNotNull(expectedUrl, "expected_output.json not found");
        Path expectedPath = Paths.get(expectedUrl.toURI());

        String json = Files.readString(expectedPath);
        JsonObject expected = new Gson().fromJson(json, JsonObject.class);

        if (expected.has("total_pieces"))
            assertEquals(expected.get("total_pieces").getAsInt(), totalPieces, "total_pieces mismatch");
        if (expected.has("standard_pallet_count"))
            assertEquals(expected.get("standard_pallet_count").getAsInt(), standardPalletCount, "standard_pallet_count mismatch");
        if (expected.has("oversized_pallet_count"))
            assertEquals(expected.get("oversized_pallet_count").getAsInt(), oversizedPalletCount, "oversized_pallet_count mismatch");
        if (expected.has("crate_count"))
            assertEquals(expected.get("crate_count").getAsInt(), crateCount, "crate_count mismatch");

        System.out.println("Test passed: 1_4PerBox-5_6PerBox");
    }
}
