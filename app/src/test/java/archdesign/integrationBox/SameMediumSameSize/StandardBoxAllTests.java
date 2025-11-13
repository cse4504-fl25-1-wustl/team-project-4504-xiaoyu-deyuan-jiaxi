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
 * ✅ StandardBoxAllTests
 * Automatically scans and runs all folders in:
 *   resources/box_packing/SameMediumSameSize/StandardBox/
 *
 * Each folder may contain:
 *   - multiple input files (e.g., input.csv, input1.csv, etc.)
 *   - one expected_output.json file
 */
public class StandardBoxAllTests {

    @ParameterizedTest
    @MethodSource("standardBoxDirectories")
    public void testStandardBoxCase(String folderName) throws Exception {
        System.out.println("\n>>> Running test for folder: " + folderName);

        // 1️⃣ Locate test folder
        URL folderUrl = getClass().getResource("/box_packing/SameMediumSameSize/StandardBox/" + folderName);
        assertNotNull(folderUrl, "Folder not found: " + folderName);
        Path folderPath = Paths.get(folderUrl.toURI());

        // 2️⃣ Find all input files
        List<Path> inputFiles = Files.list(folderPath)
                .filter(p -> p.getFileName().toString().startsWith("input") && p.getFileName().toString().endsWith(".csv"))
                .sorted()
                .collect(Collectors.toList());

        assertFalse(inputFiles.isEmpty(), "No input files found in " + folderName);

        // 3️⃣ Load expected_output.json
        Path expectedPath = folderPath.resolve("expected_output.json");
        assertTrue(Files.exists(expectedPath), "expected_output.json not found in " + folderName);
        String json = Files.readString(expectedPath);
        JsonObject expected = new Gson().fromJson(json, JsonObject.class);

        int expectedTotal = expected.get("total_pieces").getAsInt();
        int expectedStandardBox = expected.get("standard_box_count").getAsInt();

        // 4️⃣ Run each input file
        for (Path input : inputFiles) {
            System.out.println("Running " + input.getFileName());

            ShipmentViewModel vm = Main.processFile(input.toString());
            int totalPieces = 0;
            int standardBoxCount = 0;

            for (ContainerViewModel container : vm.containers()) {
                totalPieces += countTotalArts(container);
                standardBoxCount += countBoxesByType(container, "STANDARD");
            }

            // 5️⃣ Print Debug
            System.out.println("--- Shipment Debug (" + folderName + " / " + input.getFileName() + ") ---");
            for (ContainerViewModel container : vm.containers()) {
                exploreContainer(container, 1);
            }
            System.out.println("standard_box_count = " + standardBoxCount);
            System.out.println("total_pieces = " + totalPieces);
            System.out.println("------------------------");

            // 6️⃣ Validate
            assertEquals(expectedStandardBox, standardBoxCount,
                    "standard_box_count mismatch in " + folderName + " (" + input.getFileName() + ")");
            assertEquals(expectedTotal, totalPieces,
                    "total_pieces mismatch in " + folderName + " (" + input.getFileName() + ")");
        }
    }

    // ---------------------- Auto Directory Discovery ----------------------
    private static Stream<String> standardBoxDirectories() {
        try {
            URL baseUrl = StandardBoxAllTests.class.getResource("/box_packing/SameMediumSameSize/StandardBox/");
            if (baseUrl == null) {
                throw new IllegalStateException("Base folder not found: StandardBox");
            }
            Path basePath = Paths.get(baseUrl.toURI());
            return Files.list(basePath)
                    .filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .peek(name -> System.out.println("Discovered folder: " + name))
                    .collect(Collectors.toList())
                    .stream();
        } catch (Exception e) {
            e.printStackTrace();
            return Stream.empty();
        }
    }

    // ---------------------- Utility Methods ----------------------
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
