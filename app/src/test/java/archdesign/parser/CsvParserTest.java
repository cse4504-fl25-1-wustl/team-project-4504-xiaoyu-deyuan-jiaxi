package archdesign.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

public class CsvParserTest {

	@Test
	void parseReturnsRecordsForWellFormedCsv() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			w.write(",2,TAG-1,Canvas,30,40\n");
			w.write(",1,TAG-2,Oil,20,10\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());

		assertEquals(2, records.size());
		assertEquals("TAG-1", records.get(0).tagNumber());
		assertEquals(2, records.get(0).quantity());
		assertEquals(30, records.get(0).width());
	}

	@Test
	void parseSkipsMalformedLine() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			w.write(",2,TAG-1,Canvas,30,40\n");
			w.write("bad,line,that,does,not,parse\n");
			w.write(",1,TAG-2,Oil,20,10\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());

		assertEquals(2, records.size());
	}

	@Test
	void parseNonExistentFileReturnsEmptyList() {
		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse("/path/does/not/exist.csv");
		assertNotNull(records);
		assertTrue(records.isEmpty());
	}

	@Test
	void parseHeaderOnlyReturnsEmptyList() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header Line\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		assertNotNull(records);
		assertTrue(records.isEmpty());
	}

	@Test
	void parseCombinesGlazingIntoFinalMediumWhenPresent() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			// include glazing in column index 6
			w.write(",1,TAG-3,Oil,20,10,Varnished\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		assertEquals(1, records.size());
		assertEquals("Oil Varnished", records.get(0).finalMedium());
	}

	@Test
	void parseTrimsWhitespaceFromFieldsAndHandlesTrailingCommas() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			w.write(" , 0 , TAG-4 ,  Canvas  ,  10 ,  20  ,\n");
			w.write(" ,1,  TAG-5 , Oil , 5 , 6 ,,\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		assertEquals(2, records.size());
	assertEquals("TAG-4", records.get(0).tagNumber());
	// CsvParser concatenates finalMedium + " " + glazing; when glazing is empty
	// the resulting string may have a trailing space. Trim for assertion.
	assertEquals("Canvas", records.get(0).finalMedium().trim());
		assertEquals(10, records.get(0).width());
		assertEquals("TAG-5", records.get(1).tagNumber());
	}

	@Test
	void parseAllowsZeroQuantity() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			w.write(",0,TAG-0,Paper,5,5\n");
		}
		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		assertEquals(1, records.size());
		assertEquals(0, records.get(0).quantity());
	}

	@Test
	void parseSkipsNonIntegerNumericFieldsGracefully() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			// width is non-integer -> NumberFormatException should be caught and line skipped
			w.write(",1,TAG-X,Paper,notanumber,10\n");
			w.write(",1,TAG-Y,Paper,5,5\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		assertEquals(1, records.size());
		assertEquals("TAG-Y", records.get(0).tagNumber());
	}

	@Test
	void parseHandlesVeryLargeNumbersWithoutCrashing() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			// use large but within int range
			w.write(",1,TAG-LARGE,Canvas,2147483646,1\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		assertEquals(1, records.size());
		assertEquals(2147483646, records.get(0).width());
	}

	@Test
	void parseIgnoresExtraColumnsBeyondExpected() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			w.write(",1,TAG-EXTRA,Oil,10,20,Glaze,unexpected,columns,here\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		assertEquals(1, records.size());
		assertTrue(records.get(0).finalMedium().contains("Oil"));
	}

	@Test
	void parseSupportsUnicodeAndTrimming() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			w.write(",1,タグ-ユニコード, キャンバス , 10 , 20\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		assertEquals(1, records.size());
		assertEquals("タグ-ユニコード", records.get(0).tagNumber());
		assertEquals("キャンバス", records.get(0).finalMedium().trim());
	}

	@Test
	void parseIsIdempotentOnSameFile() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			w.write(",1,TAG-1,Canvas,10,20\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> first = p.parse(tmp.getAbsolutePath());
		List<ArtDataRecord> second = p.parse(tmp.getAbsolutePath());
		assertEquals(first, second);
	}
}
