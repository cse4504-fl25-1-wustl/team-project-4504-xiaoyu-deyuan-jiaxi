package archdesign.integrationBox.MixedMediumSameSize;

import archdesign.Main;
import archdesign.response.*;
import com.google.gson.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.nio.file.*;
import java.util.stream.*;
import static org.junit.jupiter.api.Assertions.*;

public class LargeBoxAllTests {

    static Stream<Path> provideInputFiles() throws Exception {
        URL base = LargeBoxAllTests.class.getClassLoader()
                .getResource("box_packing/MixedMediumSameSize/LargeBox");
        assertNotNull(base, "Test folder not found");
        Path root = Paths.get(base.toURI());
        return Files.walk(root)
                .filter(p -> p.getFileName().toString().equals("input.csv"))
                .sorted();
    }

    @ParameterizedTest
    @MethodSource("provideInputFiles")
    public void testAllLargeBoxCases(Path inputPath) throws Exception {
        ShipmentViewModel vm = Main.processFile(inputPath.toString());

        int totalPieces = 0, standardPalletCount = 0, oversizedPalletCount = 0, crateCount = 0;
        for (ContainerViewModel c : vm.containers()) {
            if ("STANDARD_PALLET".equals(c.type())) standardPalletCount++;
            else if ("OVERSIZE_PALLET".equals(c.type())) oversizedPalletCount++;
            else if ("STANDARD_CRATE".equals(c.type())) crateCount++;
            for (BoxViewModel b : c.boxes())
                for (ArtViewModel a : b.arts()) totalPieces++;
        }

        Path expectedPath = inputPath.getParent().resolve("expected_output.json");
        assertTrue(Files.exists(expectedPath), "expected_output.json missing for " + inputPath);
        JsonObject expected = new Gson().fromJson(Files.readString(expectedPath), JsonObject.class);

        if (expected.has("total_pieces")) assertEquals(expected.get("total_pieces").getAsInt(), totalPieces);
        if (expected.has("standard_pallet_count")) assertEquals(expected.get("standard_pallet_count").getAsInt(), standardPalletCount);
        if (expected.has("oversized_pallet_count")) assertEquals(expected.get("oversized_pallet_count").getAsInt(), oversizedPalletCount);
        if (expected.has("crate_count")) assertEquals(expected.get("crate_count").getAsInt(), crateCount);
    }
}
