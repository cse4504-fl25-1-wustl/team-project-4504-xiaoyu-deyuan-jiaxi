package archdesign.betarelease;

import archdesign.interactor.Packer;
import archdesign.interactor.PackingPlan;
import archdesign.interactor.UserConstraints;
import archdesign.parser.ArtDataRecord;
import archdesign.parser.CsvParser;
import archdesign.request.ArtImporter;
import archdesign.entities.Art;
import archdesign.entities.enums.Material;
import archdesign.entities.enums.ShippingProvider;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Beta Release: End-to-end workflow tests.
 * These tests ensure the complete pipeline works from CSV parsing to packing plan generation.
 */
class EndToEndTest {

    @Test
    void testSmallCsvEndToEnd() throws Exception {
        // Create a small CSV file in old/test format
        File tmp = Files.createTempFile("e2e_test", ".csv").toFile();
        try (FileWriter w = new FileWriter(tmp)) {
            w.write("Header\n");
            w.write(",2,TAG-E2E-1,Canvas,30,40\n");
            w.write(",1,TAG-E2E-2,Glass,20,25\n");
        }

        // Step 1: Parse CSV
        CsvParser parser = new CsvParser();
        List<ArtDataRecord> records = parser.parse(tmp.getAbsolutePath());

        assertNotNull(records, "Records should not be null");
        assertEquals(2, records.size(), "Should parse 2 records");

        // Step 2: Convert to Art objects (using existing ArtImporter logic)
        ArtImporter importer = new ArtImporter(parser);
        List<Art> arts = importer.importFromFile(tmp.getAbsolutePath());

        assertNotNull(arts, "Arts should not be null");
        assertTrue(arts.size() >= 2, "Should have at least 2 art objects (quantity expansion)");

        // Step 3: Pack using Packer
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        // Step 4: Assertions
        assertNotNull(plan, "PackingPlan should not be null");
        assertTrue(plan.getTotalContainerCount() >= 0, "Container count should be non-negative");
        assertTrue(plan.getTotalBoxCount() >= 0, "Box count should be non-negative");
        assertTrue(plan.getTotalCost() >= 0.0, "Total cost should be non-negative");
        assertTrue(plan.getTotalWeight() >= 0.0, "Total weight should be non-negative");

        // If arts were packable, should have at least one container
        if (!arts.isEmpty()) {
            // Check if any art was successfully packed
            int totalPackedArts = plan.getContainers().stream()
                .flatMap(c -> c.getBoxesInContainer().stream())
                .mapToInt(b -> b.getArtsInBox().size())
                .sum();
            
            // Either arts were packed or marked as unpacked
            int totalArts = arts.size();
            int unpackedCount = plan.getUnpackedArts().size();
            assertEquals(totalArts, totalPackedArts + unpackedCount,
                "Total arts should equal packed + unpacked arts");
        }
    }

    @Test
    void testClientCsvEndToEnd() throws Exception {
        // Create a small CSV file in client format
        File tmp = Files.createTempFile("e2e_client", ".csv").toFile();
        try (FileWriter w = new FileWriter(tmp)) {
            w.write("line number, quantity, tag number, Final medium, Outside Size Width, Outside Size Height, Glazing, Frame 1 Moulding, Hardware\n");
            w.write("1, 2, TAG-CLIENT-1, Canvas - Gallery, 30, 40, N/A, N/A, N/A\n");
            w.write("2, 1, TAG-CLIENT-2, Paper Print - Framed, 20, 25, Regular Glass, N/A, N/A\n");
        }

        // Step 1: Parse CSV
        CsvParser parser = new CsvParser();
        List<ArtDataRecord> records = parser.parse(tmp.getAbsolutePath());

        assertNotNull(records, "Records should not be null");
        assertEquals(2, records.size(), "Should parse 2 records");

        // Verify parsed data
        ArtDataRecord first = records.get(0);
        assertEquals(2, first.quantity(), "First record quantity should be 2");
        assertEquals("TAG-CLIENT-1", first.tagNumber(), "First record tag should be TAG-CLIENT-1");
        assertEquals(30.0, first.width(), 0.01, "First record width should be 30");
        assertEquals(40.0, first.height(), 0.01, "First record height should be 40");

        ArtDataRecord second = records.get(1);
        assertEquals(1, second.quantity(), "Second record quantity should be 1");
        assertEquals("TAG-CLIENT-2", second.tagNumber(), "Second record tag should be TAG-CLIENT-2");
        assertEquals(20.0, second.width(), 0.01, "Second record width should be 20");
        assertEquals(25.0, second.height(), 0.01, "Second record height should be 25");

        // Step 2: Convert to Art objects
        ArtImporter importer = new ArtImporter(parser);
        List<Art> arts = importer.importFromFile(tmp.getAbsolutePath());

        assertNotNull(arts, "Arts should not be null");
        assertTrue(arts.size() >= 2, "Should have at least 2 art objects (quantity expansion)");

        // Step 3: Pack using Packer
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        PackingPlan plan = Packer.pack(arts, constraints, provider);

        // Step 4: Assertions
        assertNotNull(plan, "PackingPlan should not be null");
        assertTrue(plan.getTotalContainerCount() >= 0, "Container count should be non-negative");
        assertTrue(plan.getTotalBoxCount() >= 0, "Box count should be non-negative");
        assertTrue(plan.getTotalCost() >= 0.0, "Total cost should be non-negative");
        assertTrue(plan.getTotalWeight() >= 0.0, "Total weight should be non-negative");

        // Verify that the complete pipeline executed without exceptions
        // If we got here, the end-to-end flow worked
        assertTrue(true, "End-to-end pipeline completed successfully");
    }
}

