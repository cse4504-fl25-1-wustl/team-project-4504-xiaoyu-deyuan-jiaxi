package archdesign.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ArtDataParserTest {

	@Test
	void csvParserImplementsInterface() {
		ArtDataParser p = new CsvParser();
		List<ArtDataRecord> out = p.parse("");
		assertNotNull(out);
	}

	@Test
	void parseEmptyPathReturnsEmptyList() {
		ArtDataParser p = new CsvParser();
		List<ArtDataRecord> out = p.parse("");
		assertNotNull(out);
		assertTrue(out.isEmpty() || out.size() == 0);
	}
}
