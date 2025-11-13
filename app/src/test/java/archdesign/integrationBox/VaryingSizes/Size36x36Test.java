package archdesign.integrationBox.VaryingSizes;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.BoxViewModel;
import archdesign.response.ArtViewModel;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import com.google.gson.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for VaryingSizes/36x36
 * ✅ Expected: STANDARD box (since 36 ≤ 43 rule applies)
 */
public class Size36x36Test {

    private static final String BASE_DIR = "/box_packing/VaryingSizes/36x36";

    @Test
    public void test_36x36_StandardBox() throws Exception {
        System.out.println(">>> Running test for folder: 36x36");

        // 1️⃣ 获取资源文件路径
        URL inputUrl = getClass().getResource(BASE_DIR + "/input.csv");
        URL expectedUrl = getClass().getResource(BASE_DIR + "/expected_output.json");
        assertNotNull(inputUrl, "input.csv missing in 36x36");
        assertNotNull(expectedUrl, "expected_output.json missing in 36x36");

        Path inputPath = Paths.get(inputUrl.toURI());
        Path expectedPath = Paths.get(expectedUrl.toURI());

        // 2️⃣ 调用主程序生成结果
        ShipmentViewModel vm = Main.processFile(inputPath.toString());

        // 3️⃣ 读取 expected_output.json
        JsonObject expected = JsonParser.parseString(Files.readString(expectedPath)).getAsJsonObject();
        int expectedStandard = expected.get("standard_box_count").getAsInt();
        int expectedLarge = expected.get("large_box_count").getAsInt();
        int expectedCrate = expected.has("crate_count") ? expected.get("crate_count").getAsInt() : 0;
        int expectedTotal = expected.get("total_pieces").getAsInt();

        // 4️⃣ 遍历 ShipmentViewModel，统计实际结果
        int standardBoxCount = 0;
        int largeBoxCount = 0;
        int crateCount = 0;
        int totalPieces = 0;
        List<String> debug = new ArrayList<>();

        for (ContainerViewModel container : vm.containers()) {
            debug.add("  Container: " + container.type());
            for (BoxViewModel box : container.boxes()) {
                String bt = box.type();
                if ("STANDARD".equals(bt)) standardBoxCount++;
                else if ("LARGE".equals(bt)) largeBoxCount++;
                else if ("CRATE".equals(bt)) crateCount++;

                debug.add("    Box: " + bt + " (" + box.arts().size() + " art pieces)");
                for (ArtViewModel art : box.arts()) totalPieces++;
            }
        }

        // 5️⃣ 打印调试输出
        System.out.println("--- Shipment Debug (36x36) ---");
        debug.forEach(System.out::println);
        System.out.println("standard_box_count = " + standardBoxCount);
        System.out.println("large_box_count = " + largeBoxCount);
        System.out.println("crate_count = " + crateCount);
        System.out.println("total_pieces = " + totalPieces);
        System.out.println("------------------------");

        // 6️⃣ 验证
        assertEquals(expectedStandard, standardBoxCount, "standard_box_count mismatch");
        assertEquals(expectedLarge, largeBoxCount, "large_box_count mismatch");
        assertEquals(expectedCrate, crateCount, "crate_count mismatch");
        assertEquals(expectedTotal, totalPieces, "total_pieces mismatch");
    }
}
