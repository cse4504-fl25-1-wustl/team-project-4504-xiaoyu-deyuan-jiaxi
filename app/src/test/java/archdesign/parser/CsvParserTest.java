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
}
