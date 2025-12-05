package archdesign.stress;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stress tests using test cases from stress_tests resource folder.
 * Tests the parser and processing pipeline with various input formats.
 */
public class StressTest {

    private Path resourcePath(String name) throws Exception {
        URL res = getClass().getResource("/stress_tests/" + name);
        assertNotNull(res, "Test resource not found: stress_tests/" + name);
        return Paths.get(res.toURI());
    }

    // Test "all" folder
    static Stream<Arguments> allTestCases() {
        return Stream.of(
            Arguments.of("all/test1"),
            Arguments.of("all/test2")
        );
    }

    @ParameterizedTest(name = "all/{0}")
    @MethodSource("allTestCases")
    void testAllCategory(String testFolder) throws Exception {
        Path inputPath = resourcePath(testFolder + "/input.csv");
        
        ShipmentViewModel vm = Main.processFile(inputPath.toString());
        assertNotNull(vm, "ShipmentViewModel should not be null for " + testFolder);
        
        // Verify basic sanity checks
        assertTrue(Double.isFinite(vm.totalWeight()), "Total weight should be finite");
        assertTrue(vm.totalWeight() >= 0.0, "Total weight should be non-negative");
        assertTrue(Double.isFinite(vm.totalCost()), "Total cost should be finite");
        assertTrue(vm.totalCost() >= 0.0, "Total cost should be non-negative");
    }

    // Test "strictest_constraint" folder
    static Stream<Arguments> strictestConstraintTestCases() {
        return Stream.of(
            Arguments.of("strictest_constraint/test1"),
            Arguments.of("strictest_constraint/test2"),
            Arguments.of("strictest_constraint/test3")
        );
    }

    @ParameterizedTest(name = "strictest_constraint/{0}")
    @MethodSource("strictestConstraintTestCases")
    void testStrictestConstraint(String testFolder) throws Exception {
        Path inputPath = resourcePath(testFolder + "/input.csv");
        
        ShipmentViewModel vm = Main.processFile(inputPath.toString());
        assertNotNull(vm, "ShipmentViewModel should not be null for " + testFolder);
        
        // Verify basic sanity checks
        assertTrue(Double.isFinite(vm.totalWeight()), "Total weight should be finite");
        assertTrue(vm.totalWeight() >= 0.0, "Total weight should be non-negative");
        assertTrue(Double.isFinite(vm.totalCost()), "Total cost should be finite");
        assertTrue(vm.totalCost() >= 0.0, "Total cost should be non-negative");
        
        // Verify at least some arts are processed
        int totalArts = vm.containers().stream()
            .flatMap(c -> c.boxes().stream())
            .flatMap(b -> b.arts().stream())
            .mapToInt(a -> 1)
            .sum();
        assertTrue(totalArts > 0, "Should have processed at least one art");
    }

    @Test
    void testStrictestConstraintTest1_ParsesCorrectly() throws Exception {
        // This test specifically verifies the no-space header format parsing
        // Header: linenumber,quantity,tagnumber,finalmedium,outsidesizewidth,outsidesizeheight,glazing,...
        Path inputPath = resourcePath("strictest_constraint/test1/input.csv");
        
        ShipmentViewModel vm = Main.processFile(inputPath.toString());
        assertNotNull(vm, "ShipmentViewModel should not be null");
        
        // Verify arts were parsed (test1 has 403 data lines)
        int totalArts = vm.containers().stream()
            .flatMap(c -> c.boxes().stream())
            .flatMap(b -> b.arts().stream())
            .mapToInt(a -> 1)
            .sum();
        
        // Should have parsed a significant number of arts
        assertTrue(totalArts > 100, "Should have parsed many arts from stress test file, got: " + totalArts);
        
        // Check that specific art IDs were parsed correctly (from the no-space header format)
        boolean hasBndId = vm.containers().stream()
            .flatMap(c -> c.boxes().stream())
            .flatMap(b -> b.arts().stream())
            .anyMatch(a -> a.id().contains("BND"));
        assertTrue(hasBndId, "Should have arts with BND tag prefix");
    }
}
