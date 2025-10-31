package archdesign.integrationBox.MixedMediumSameSize;

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
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class StandardBoxAllTests {

    @ParameterizedTest
    @MethodSource("standardBoxDirectories")
    public void testStandardBoxCase(String folderName) throws Exception {
        System.out.println("\n>>> Running test for folder: " + folderName);

        // 1 load document
        URL resource = getClass().getResource("/box_packing/MixedMediumSameSize/StandardBox/" + folderName + "/input.csv");
        assertNotNull(resource, "input.csv not found in " + folderName);
        Path path = Paths.get(resource.toURI());

        // 2 run packing logic
        ShipmentViewModel vm = Main.processFile(path.toString());

        // 3 initialize counters
        int totalPieces = 0;
        int standardBoxCount = 0;
        int largeBoxCount = 0;

        // 4 recursively traverse to count boxes and art pieces
        for (ContainerViewModel container : vm.containers()) {
            totalPieces += countTotalArts(container);
            standardBoxCount += countBoxesByType(container, "STANDARD");
            largeBoxCount += countBoxesByType(container, "LARGE");
        }

        // 5 output hierarchy for debugging
        System.out.println("--- Shipment Debug (" + folderName + ") ---");
        for (ContainerViewModel container : vm.containers()) {
            exploreContainer(container, 1);
        }
        System.out.println("standard_box_count = " + standardBoxCount);
        System.out.println("large_box_count = " + largeBoxCount);
        System.out.println("total_pieces = " + totalPieces);
        System.out.println("------------------------");

        // 6 read expected_output.json
        URL expectedUrl = getClass().getResource("/box_packing/MixedMediumSameSize/StandardBox/" + folderName + "/expected_output.json");
        assertNotNull(expectedUrl, "expected_output.json missing in " + folderName);
        Path expectedPath = Paths.get(expectedUrl.toURI());
        String json = Files.readString(expectedPath);
        JsonObject obj = new Gson().fromJson(json, JsonObject.class);

        // 7 verify results
        if (obj.has("total_pieces")) {
            assertEquals(obj.get("total_pieces").getAsInt(), totalPieces, "total_pieces mismatch in " + folderName);
        }
        if (obj.has("standard_box_count")) {
            assertEquals(obj.get("standard_box_count").getAsInt(), standardBoxCount, "standard_box_count mismatch in " + folderName);
        }
        if (obj.has("large_box_count")) {
            assertEquals(obj.get("large_box_count").getAsInt(), largeBoxCount, "large_box_count mismatch in " + folderName);
        }
    }

    // all standard box test directories
    private static Stream<String> standardBoxDirectories() {
        return Stream.of(
                "1_4PerBox-4_6PerBox",
                "1_4PerBox-5_6PerBox",
                "1_4PerBox-6_6PerBox",
                "2_4PerBox-3_6PerBox",
                "2_4PerBox-4_6PerBox",
                "3_4PerBox-1_6PerBox",
                "3_4PerBox-2_6PerBox",
                "3_4PerBox-3_6PerBox"
        );
    }

    // recursively count boxes of a specific type
    private static int countBoxesByType(ContainerViewModel container, String type) {
        int count = 0;
        if (container.boxes() != null) {
            for (BoxViewModel box : container.boxes()) {
                String boxType = box.type().toUpperCase();
                String normalizedType = type.toUpperCase().replace("_BOX", "");
                if (boxType.equals(normalizedType)) count++;
            }
        }
        List<?> nestedContainers = getNestedContainers(container);
        if (nestedContainers != null) {
            for (Object child : nestedContainers) {
                if (child instanceof ContainerViewModel cvm) {
                    count += countBoxesByType(cvm, type);
                }
            }
        }
        return count;
    }

    // recursively count all artworks
    private static int countTotalArts(ContainerViewModel container) {
        int total = 0;
        if (container.boxes() != null) {
            for (BoxViewModel box : container.boxes()) {
                if (box.arts() != null) total += box.arts().size();
            }
        }
        List<?> nestedContainers = getNestedContainers(container);
        if (nestedContainers != null) {
            for (Object child : nestedContainers) {
                if (child instanceof ContainerViewModel cvm) {
                    total += countTotalArts(cvm);
                }
            }
        }
        return total;
    }

    // visit nested containers via reflection
    @SuppressWarnings("unchecked")
    private static List<ContainerViewModel> getNestedContainers(ContainerViewModel container) {
        try {
            Field field = container.getClass().getDeclaredField("containers");
            field.setAccessible(true);
            Object value = field.get(container);
            if (value instanceof List<?>) {
                return (List<ContainerViewModel>) value;
            }
        } catch (NoSuchFieldException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // print container and box hierarchy for debugging
    private static void exploreContainer(ContainerViewModel container, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + "Container: " + container.type());

        if (container.boxes() != null && !container.boxes().isEmpty()) {
            for (BoxViewModel box : container.boxes()) {
                System.out.println(indent + "  Box: " + box.type() + " (" + box.arts().size() + " art pieces)");
            }
        }

        List<?> nested = getNestedContainers(container);
        if (nested != null && !nested.isEmpty()) {
            for (Object child : nested) {
                if (child instanceof ContainerViewModel cvm) {
                    exploreContainer(cvm, depth + 1);
                }
            }
        }
    }
}
