package archdesign.integrationBox.MixedMediumSameSize;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.BoxViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StandardBox_1_4PerBox_5_6PerBoxTest {

    @Test
    public void test_1_4PerBox_5_6PerBox() throws Exception {
        // 1 Load input file
        URL resource = getClass().getResource("/box_packing/MixedMediumSameSize/StandardBox/1_4PerBox-5_6PerBox/input.csv");
        assertNotNull(resource, "input.csv not found");
        Path path = Paths.get(resource.toURI());

        // 2 Run packing logic
        ShipmentViewModel vm = Main.processFile(path.toString());

        // 3 Initialize counters
        int totalPieces = 0;
        int standardBoxCount = 0;
        int largeBoxCount = 0;
        int customPieceCount = 0;

        // 4 Traverse recursively to count all boxes and art pieces
        for (ContainerViewModel container : vm.containers()) {
            totalPieces += countTotalArts(container);
            standardBoxCount += countBoxesByType(container, "STANDARD");
            largeBoxCount += countBoxesByType(container, "LARGE");
        }

        // 5 Print hierarchy for debug
        System.out.println("--- Shipment Debug ---");
        for (ContainerViewModel container : vm.containers()) {
            exploreContainer(container, 1);
        }
        System.out.println("standard_box_count = " + standardBoxCount);
        System.out.println("large_box_count = " + largeBoxCount);
        System.out.println("total_pieces = " + totalPieces);
        System.out.println("------------------------");

        // 6 Load expected output
        URL expectedUrl = getClass().getResource("/box_packing/MixedMediumSameSize/StandardBox/1_4PerBox-5_6PerBox/expected_output.json");
        assertNotNull(expectedUrl, "expected_output.json missing");
        Path expectedPath = Paths.get(expectedUrl.toURI());
        String json = Files.readString(expectedPath);
        JsonObject obj = new Gson().fromJson(json, JsonObject.class);

        // 7 Assertions
        if (obj.has("total_pieces")) {
            assertEquals(obj.get("total_pieces").getAsInt(), totalPieces, "total_pieces mismatch");
        }
        if (obj.has("standard_box_count")) {
            assertEquals(obj.get("standard_box_count").getAsInt(), standardBoxCount, "standard_box_count mismatch");
        }
        if (obj.has("large_box_count")) {
            assertEquals(obj.get("large_box_count").getAsInt(), largeBoxCount, "large_box_count mismatch");
        }
        if (obj.has("custom_piece_count")) {
            assertEquals(obj.get("custom_piece_count").getAsInt(), customPieceCount, "custom_piece_count mismatch");
        }
    }

    //  Count boxes of a specific type recursively
    private int countBoxesByType(ContainerViewModel container, String type) {
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

    //  Count all artworks recursively
    private int countTotalArts(ContainerViewModel container) {
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

    //  Access nested container list safely
    @SuppressWarnings("unchecked")
    private List<ContainerViewModel> getNestedContainers(ContainerViewModel container) {
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

    //  Recursively print containers and boxes
    private void exploreContainer(ContainerViewModel container, int depth) {
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
