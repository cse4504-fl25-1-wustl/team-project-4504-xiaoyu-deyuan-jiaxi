package archdesign.integrationBox.SameMediumSameSize;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.BoxViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ LargeBoxAllTests
 * Tests all SameMediumSameSize/LargeBox scenarios.
 * Each folder may contain multiple input files (input.csv, input1.csv, etc.),
 * and they should all produce the same expected output.
 */
public class LargeBoxAllTests {

    @ParameterizedTest
    @MethodSource("largeBoxDirectories")
    public void testLargeBoxCases(String folderName) throws Exception {
        System.out.println("\n>>> Running test for folder: " + folderName);

        // 1️⃣ Locate all input files in the directory
        URL folderUrl = getClass().getResource("/box_packing/SameMediumSameSize/LargeBox/" + folderName);
        assertNotNull(folderUrl, "Folder not found: " + folderName);
        Path folderPath = Paths.get(folderUrl.toURI());

        List<Path> inputFiles = Files.list(folderPath)
                .filter(p -> p.getFileName().toString().startsWith("input") && p.getFileName().toString().endsWith(".csv"))
                .sorted()
                .collect(Collectors.toList());

        assertFalse(inputFiles.isEmpty(), "No input.csv files found in " + folderName);

        // 2️⃣ Load expected_output.json
        Path expectedPath = folderPath.resolve("expected_output.json");
        String json = Files.readString(expectedPath);
        JsonObject expected = new Gson().fromJson(json, JsonObject.class);

        int expectedTotal = expected.get("total_pieces").getAsInt();
        int expectedLargeBox = expected.get("large_box_count").getAsInt();

        // 3️⃣ Run through all input files
        for (Path input : inputFiles) {
            System.out.println("Running " + input.getFileName());

            ShipmentViewModel vm = Main.processFile(input.toString());

            int totalPieces = 0;
            int largeBoxCount = 0;

            for (ContainerViewModel container : vm.containers()) {
                totalPieces += countTotalArts(container);
                largeBoxCount += countBoxesByType(container, "LARGE");
            }

            // 4️⃣ Print structure
            System.out.println("--- Shipment Debug (" + folderName + " / " + input.getFileName() + ") ---");
            for (ContainerViewModel container : vm.containers()) {
                exploreContainer(container, 1);
            }
            System.out.println("large_box_count = " + largeBoxCount);
            System.out.println("total_pieces = " + totalPieces);
            System.out.println("------------------------");

            // 5️⃣ Assert expected values
            assertEquals(expectedLargeBox, largeBoxCount,
                    "large_box_count mismatch in " + folderName + " (" + input.getFileName() + ")");
            assertEquals(expectedTotal, totalPieces,
                    "total_pieces mismatch in " + folderName + " (" + input.getFileName() + ")");
        }
    }

    private static Stream<String> largeBoxDirectories() {
        return Stream.of(
                "4PerLarge4",
                "4PerLarge5",
                "4PerLarge6",
                "6PerLarge6",
                "6PerLarge8",
                "8PerLarge8",
                "8PerLarge10"
        );
    }

    // ---------------- Utility Methods ----------------

    private static int countBoxesByType(ContainerViewModel container, String type) {
        int count = 0;
        if (container.boxes() != null) {
            for (BoxViewModel box : container.boxes()) {
                if (box.type().equalsIgnoreCase(type)) count++;
            }
        }
        List<?> nested = getNestedContainers(container);
        if (nested != null) {
            for (Object obj : nested) {
                if (obj instanceof ContainerViewModel cvm)
                    count += countBoxesByType(cvm, type);
            }
        }
        return count;
    }

    private static int countTotalArts(ContainerViewModel container) {
        int total = 0;
        if (container.boxes() != null) {
            for (BoxViewModel box : container.boxes()) {
                if (box.arts() != null) total += box.arts().size();
            }
        }
        List<?> nested = getNestedContainers(container);
        if (nested != null) {
            for (Object obj : nested) {
                if (obj instanceof ContainerViewModel cvm)
                    total += countTotalArts(cvm);
            }
        }
        return total;
    }

    @SuppressWarnings("unchecked")
    private static List<ContainerViewModel> getNestedContainers(ContainerViewModel container) {
        try {
            Field field = container.getClass().getDeclaredField("containers");
            field.setAccessible(true);
            Object value = field.get(container);
            if (value instanceof List<?> list)
                return (List<ContainerViewModel>) list;
        } catch (NoSuchFieldException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void exploreContainer(ContainerViewModel container, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + "Container: " + container.type());
        if (container.boxes() != null) {
            for (BoxViewModel box : container.boxes()) {
                System.out.println(indent + "  Box: " + box.type() + " (" + box.arts().size() + " art pieces)");
            }
        }
        List<?> nested = getNestedContainers(container);
        if (nested != null) {
            for (Object obj : nested) {
                if (obj instanceof ContainerViewModel cvm)
                    exploreContainer(cvm, depth + 1);
            }
        }
    }
}
