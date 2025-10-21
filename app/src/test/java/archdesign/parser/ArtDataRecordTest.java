package archdesign.parser;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArtDataRecordTest {

	@Test
	void recordAccessorsReturnProvidedValues() {
		ArtDataRecord r = new ArtDataRecord(3, "TAG-001", "Canvas", 30, 40);

		assertEquals(3, r.quantity());
		assertEquals("TAG-001", r.tagNumber());
		assertEquals("Canvas", r.finalMedium());
		assertEquals(30, r.width());
		assertEquals(40, r.height());
	}
}
