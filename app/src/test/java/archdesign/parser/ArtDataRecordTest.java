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

	@Test
	void recordsWithSameValuesAreEqualAndHaveSameHash() {
		ArtDataRecord a = new ArtDataRecord(2, "ID", "Medium", 10, 20);
		ArtDataRecord b = new ArtDataRecord(2, "ID", "Medium", 10, 20);

		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void toStringIsNonEmpty() {
		ArtDataRecord a = new ArtDataRecord(1, "X", "M", 1, 1);
		assertNotNull(a.toString());
		assertFalse(a.toString().isEmpty());
	}
}
