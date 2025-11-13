package archdesign.integrationBox.MixedMediumSameSize;

import archdesign.Main;
import archdesign.response.*;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.net.URL;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

public class LargeBox_1_4PerBox_6_6PerBox_Test {

    @Test
    public void testSingleCase() throws Exception {
        URL resource = getClass().getClassLoader().getResource(
                "box_packing/MixedMediumSameSize/LargeBox/1_4PerBox-6_6PerBox/input.csv");
        assertNotNull(resource, "input.csv not found");
        Path inputPath = Paths.get(resource.toURI());

        ShipmentViewModel vm = Main.processFile(inputPath.toString());
        int totalPieces = 0, standardPalletCount = 0, oversizedPalletCount = 0, crateCount = 0;
        for (ContainerViewModel c : vm.containers()) {
            if ("STANDARD_PALLET".equals(c.type())) standardPalletCount++;
            else if ("OVERSIZE_PALLET".equals(c.type())) oversizedPalletCount++;
            else if ("STANDARD_CRATE".equals(c.type())) crateCount++;
            for (BoxViewModel b : c.boxes())
                for (ArtViewModel a : b.arts()) totalPieces++;
        }

        URL expectedUrl = getClass().getClassLoader().getResource(
                "box_packing/MixedMediumSameSize/LargeBox/1_4PerBox-6_6PerBox/expected_output.json");
        assertNotNull(expectedUrl, "expected_output.json not found");
        JsonObject expected = new Gson().fromJson(Files.readString(Paths.get(expectedUrl.toURI())), JsonObject.class);

        if (expected.has("total_pieces")) assertEquals(expected.get("total_pieces").getAsInt(), totalPieces);
        if (expected.has("standard_pallet_count")) assertEquals(expected.get("standard_pallet_count").getAsInt(), standardPalletCount);
        if (expected.has("oversized_pallet_count")) assertEquals(expected.get("oversized_pallet_count").getAsInt(), oversizedPalletCount);
        if (expected.has("crate_count")) assertEquals(expected.get("crate_count").getAsInt(), crateCount);
    }
}
