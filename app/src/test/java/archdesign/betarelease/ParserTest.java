package archdesign.betarelease;

import archdesign.parser.ArtDataRecord;
import archdesign.parser.CsvParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Beta Release: Tests for new CSV format support in CsvParser.
 * These tests verify that the parser can handle the client-provided CSV format
 * with named columns while maintaining backward compatibility with the old format.
 */
class ParserTest {

    @Test
    void testParseClientFormat() throws Exception {
        // Client format with named columns
        File tmp = Files.createTempFile("client_format", ".csv").toFile();
        try (FileWriter w = new FileWriter(tmp)) {
            w.write("line number, quantity, tag number, Final medium, Outside Size Width, Outside Size Height, Glazing, Frame 1 Moulding, Hardware\n");
            w.write("1, 13, TAG-001, Paper Print - Framed, 43, 43, Regular Glass, N/A, N/A\n");
            w.write("2, 2, TAG-002, Canvas - Gallery, 33, 43, N/A, N/A, N/A\n");
        }

        CsvParser parser = new CsvParser();
        List<ArtDataRecord> records = parser.parse(tmp.getAbsolutePath());

        // Assert that records are parsed correctly
        assertNotNull(records, "Records should not be null");
        assertEquals(2, records.size(), "Should parse 2 records");

        // Check first record
        ArtDataRecord first = records.get(0);
        assertEquals(13, first.quantity(), "First record quantity should be 13");
        assertEquals("TAG-001", first.tagNumber(), "First record tag number should be TAG-001");
        assertTrue(first.finalMedium().contains("Paper Print - Framed"), "First record should contain material name");
        assertTrue(first.finalMedium().contains("Regular Glass"), "First record should contain glazing");
        assertEquals(43.0, first.width(), 0.01, "First record width should be 43");
        assertEquals(43.0, first.height(), 0.01, "First record height should be 43");

        // Check second record
        ArtDataRecord second = records.get(1);
        assertEquals(2, second.quantity(), "Second record quantity should be 2");
        assertEquals("TAG-002", second.tagNumber(), "Second record tag number should be TAG-002");
        assertTrue(second.finalMedium().contains("Canvas - Gallery"), "Second record should contain material name");
        assertEquals(33.0, second.width(), 0.01, "Second record width should be 33");
        assertEquals(43.0, second.height(), 0.01, "Second record height should be 43");
    }

    @Test
    void testParseOldFormat() throws Exception {
        // Old/test format with fixed positions
        File tmp = Files.createTempFile("old_format", ".csv").toFile();
        try (FileWriter w = new FileWriter(tmp)) {
            w.write("Header\n");
            w.write(",2,TAG-OLD-1,Canvas,30,40\n");
            w.write(",1,TAG-OLD-2,Oil,20,10\n");
        }

        CsvParser parser = new CsvParser();
        List<ArtDataRecord> records = parser.parse(tmp.getAbsolutePath());

        // Assert that old format still works
        assertNotNull(records, "Records should not be null");
        assertEquals(2, records.size(), "Should parse 2 records");

        ArtDataRecord first = records.get(0);
        assertEquals(2, first.quantity(), "First record quantity should be 2");
        assertEquals("TAG-OLD-1", first.tagNumber(), "First record tag number should be TAG-OLD-1");
        assertEquals(30.0, first.width(), 0.01, "First record width should be 30");
        assertEquals(40.0, first.height(), 0.01, "First record height should be 40");

        ArtDataRecord second = records.get(1);
        assertEquals(1, second.quantity(), "Second record quantity should be 1");
        assertEquals("TAG-OLD-2", second.tagNumber(), "Second record tag number should be TAG-OLD-2");
        assertEquals(20.0, second.width(), 0.01, "Second record width should be 20");
        assertEquals(10.0, second.height(), 0.01, "Second record height should be 10");
    }

    @Test
    void testParseColumnOrderIrrelevant() throws Exception {
        // Test that parser can handle different column orders
        File tmp = Files.createTempFile("shuffled_columns", ".csv").toFile();
        try (FileWriter w = new FileWriter(tmp)) {
            w.write("tag number, Final medium, quantity, Outside Size Height, Outside Size Width, Glazing\n");
            w.write("TAG-SHUFFLE, Glass, 5, 20, 30, Regular Glass\n");
        }

        CsvParser parser = new CsvParser();
        List<ArtDataRecord> records = parser.parse(tmp.getAbsolutePath());

        // Parser should detect columns by name regardless of order
        assertNotNull(records, "Records should not be null");
        assertEquals(1, records.size(), "Should parse 1 record");

        ArtDataRecord record = records.get(0);
        assertEquals(5, record.quantity(), "Quantity should be 5");
        assertEquals("TAG-SHUFFLE", record.tagNumber(), "Tag number should be TAG-SHUFFLE");
        assertTrue(record.finalMedium().contains("Glass"), "Should contain Glass material");
        assertEquals(30.0, record.width(), 0.01, "Width should be 30 (Outside Size Width)");
        assertEquals(20.0, record.height(), 0.01, "Height should be 20 (Outside Size Height)");
    }

    @Test
    void testParseExtraColumnsIgnored() throws Exception {
        // Test that extra columns don't break parsing
        File tmp = Files.createTempFile("extra_columns", ".csv").toFile();
        try (FileWriter w = new FileWriter(tmp)) {
            w.write("line number, quantity, tag number, Final medium, Outside Size Width, Outside Size Height, Glazing, Extra1, Extra2, Extra3, RandomColumn\n");
            w.write("1, 2, TAG-EXTRA, Oil, 15, 25, Varnished, Data1, Data2, Data3, RandomValue\n");
        }

        CsvParser parser = new CsvParser();
        List<ArtDataRecord> records = parser.parse(tmp.getAbsolutePath());

        // Parser should ignore extra columns and parse required fields
        assertNotNull(records, "Records should not be null");
        assertEquals(1, records.size(), "Should parse 1 record");

        ArtDataRecord record = records.get(0);
        assertEquals(2, record.quantity(), "Quantity should be 2");
        assertEquals("TAG-EXTRA", record.tagNumber(), "Tag number should be TAG-EXTRA");
        assertTrue(record.finalMedium().contains("Oil"), "Should contain Oil material");
        assertTrue(record.finalMedium().contains("Varnished"), "Should contain Varnished glazing");
        assertEquals(15.0, record.width(), 0.01, "Width should be 15");
        assertEquals(25.0, record.height(), 0.01, "Height should be 25");
    }

    @Test
    void testParseMissingOptionalFields() throws Exception {
        // Test that missing optional fields (like Glazing) don't break parsing
        File tmp = Files.createTempFile("missing_optional", ".csv").toFile();
        try (FileWriter w = new FileWriter(tmp)) {
            w.write("line number, quantity, tag number, Final medium, Outside Size Width, Outside Size Height\n");
            w.write("1, 1, TAG-NO-GLAZE, Canvas, 10, 20\n");
            w.write("2, 1, TAG-WITH-GLAZE, Glass, 15, 25, Regular Glass\n");
        }

        CsvParser parser = new CsvParser();
        List<ArtDataRecord> records = parser.parse(tmp.getAbsolutePath());

        // Parser should handle missing Glazing column gracefully
        assertNotNull(records, "Records should not be null");
        assertEquals(2, records.size(), "Should parse 2 records");

        ArtDataRecord first = records.get(0);
        assertEquals("TAG-NO-GLAZE", first.tagNumber(), "First record tag should be TAG-NO-GLAZE");
        assertTrue(first.finalMedium().contains("Canvas"), "First record should contain Canvas");
        assertEquals(10.0, first.width(), 0.01, "First record width should be 10");
        assertEquals(20.0, first.height(), 0.01, "First record height should be 20");

        ArtDataRecord second = records.get(1);
        assertEquals("TAG-WITH-GLAZE", second.tagNumber(), "Second record tag should be TAG-WITH-GLAZE");
        assertTrue(second.finalMedium().contains("Glass"), "Second record should contain Glass");
        assertTrue(second.finalMedium().contains("Regular Glass"), "Second record should contain glazing");
        assertEquals(15.0, second.width(), 0.01, "Second record width should be 15");
        assertEquals(25.0, second.height(), 0.01, "Second record height should be 25");
    }
}

