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

	@Test
	void parseIgnoresBlankLines() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			w.write("\n");
			w.write(",1,TAG-A,Canvas,10,10\n");
			w.write("\n\n");
			w.write(",2,TAG-B,Oil,5,5\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		assertEquals(2, records.size());
	}

	@Test
	void parsePreservesQuantityGreaterThanOne() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			w.write(",3,TAG-Q,Canvas,10,10\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		assertEquals(1, records.size());
		assertEquals(3, records.get(0).quantity());
	}

	@Test
	void parseHandlesCRLFLineEndings() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\r\n");
			w.write(",1,TAG-CRLF,Canvas,7,8\r\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		assertEquals(1, records.size());
		assertEquals("TAG-CRLF", records.get(0).tagNumber());
	}

	@Test
	void parseHandlesBomInHeader() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
		Files.write(tmp.toPath(), bom); // write BOM first
		try (FileWriter w = new FileWriter(tmp, true)) {
			w.write("Header\n");
			w.write(",1,TAG-BOM,Canvas,3,4\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());
		// Parser currently uses readLine; BOM presence should not prevent parsing rows
		assertEquals(1, records.size());
		assertEquals("TAG-BOM", records.get(0).tagNumber());
	}

	@Test
	void parseConcurrentCallsAreSafe() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			w.write(",1,TAG-1,Canvas,10,20\n");
		}

		CsvParser p = new CsvParser();

		java.util.concurrent.ExecutorService ex = java.util.concurrent.Executors.newFixedThreadPool(4);
		try {
			java.util.List<java.util.concurrent.Future<List<ArtDataRecord>>> futures = new java.util.ArrayList<>();
			for (int i = 0; i < 4; i++) {
				futures.add(ex.submit(() -> p.parse(tmp.getAbsolutePath())));
			}

			for (var f : futures) {
				List<ArtDataRecord> recs = f.get();
				assertEquals(1, recs.size());
				assertEquals("TAG-1", recs.get(0).tagNumber());
			}
		} finally {
			ex.shutdown();
		}
	}

	@Test
	void parseReturnedListsAreIndependent() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			w.write("Header\n");
			w.write(",1,TAG-1,Canvas,10,20\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> a = p.parse(tmp.getAbsolutePath());
		a.clear();
		List<ArtDataRecord> b = p.parse(tmp.getAbsolutePath());
		assertEquals(1, b.size());
	}

	@Test
	void parseSupportsNewFormatWithDifferentColumnOrder() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			// New format: Line Number, Quantity, Location, Floor, Tag #, Outside Size Width, Outside Size Height, Final Medium, ...
			w.write("Line Number,Quantity,Location,Floor,Tag #,Outside Size Width,Outside Size Height,Final Medium,new: Presentation Conversion,Item #,new: Item URL,Glazing,Frame 1 Moulding,Hardware\n");
			w.write("1,1,,,1,31.375,45.375,Paper Print - Framed,,,,Regular Glass,475130-BX,4 pt Sec\n");
			w.write("2,1,,,2,27,27,Canvas - Float Frame,,,,No Glass,313-301-BX,3 pt Sec\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());

		assertEquals(2, records.size());
		// Tag # is at index 4 in new format
		assertEquals("1", records.get(0).tagNumber());
		assertEquals(1, records.get(0).quantity());
		assertEquals(31.375, records.get(0).width());
		assertEquals(45.375, records.get(0).height());
		assertEquals("2", records.get(1).tagNumber());
		assertEquals(27, records.get(1).width());
	}

	@Test
	void parseSupportsOldFormatConsistently() throws Exception {
		File tmp = Files.createTempFile("arts", ".csv").toFile();
		try (FileWriter w = new FileWriter(tmp)) {
			// Old format: line number, quantity, tag number, Final medium, Outside Size Width, Outside Size Height, Glazing, ...
			w.write("line number,quantity,tag number,Final medium,Outside Size Width,Outside Size Height,Glazing,Frame 1 Moulding,Hardware\n");
			w.write("1,49,1,Paper Print - Framed,33,43,Regular Glass,N/A,N/A\n");
			w.write("2,2,2,Paper Print - Framed,34,46,Regular Glass,N/A,N/A\n");
		}

		CsvParser p = new CsvParser();
		List<ArtDataRecord> records = p.parse(tmp.getAbsolutePath());

		assertEquals(2, records.size());
		// tag number is at index 2 in old format
		assertEquals("1", records.get(0).tagNumber());
		assertEquals(49, records.get(0).quantity());
		assertEquals(33, records.get(0).width());
		assertEquals(43, records.get(0).height());
		assertEquals("2", records.get(1).tagNumber());
		assertEquals(2, records.get(1).quantity());
	}
}
