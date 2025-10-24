package archdesign.request;

import archdesign.parser.ArtDataParser;
import archdesign.parser.ArtDataRecord;
import archdesign.entities.Art;
import archdesign.entities.enums.Material;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class ArtImporterTest {

	private static class StubParser implements ArtDataParser {
		private final List<ArtDataRecord> records;
		public StubParser(List<ArtDataRecord> records) { this.records = records; }
		@Override
		public List<ArtDataRecord> parse(String filePath) { return records; }
	}

	@Test
	void importExpandsQuantitiesAndGeneratesUniqueIds() {
		List<ArtDataRecord> recs = new ArrayList<>();
		recs.add(new ArtDataRecord(2, "T1", "Canvas", 10, 20));

		ArtImporter importer = new ArtImporter(new StubParser(recs));
		List<Art> arts = importer.importFromFile("unused");

		assertEquals(2, arts.size());
		assertEquals("TagT1-Item1", arts.get(0).getId());
		assertEquals("TagT1-Item2", arts.get(1).getId());
		assertEquals(20, arts.get(0).getHeight());
		assertEquals(10, arts.get(0).getWidth());
	}

	@Test
	void fuzzyMatchingIdentifiesKnownMaterials() {
		List<ArtDataRecord> recs = new ArrayList<>();
		recs.add(new ArtDataRecord(1, "G1", "Tempered glass", 5, 5));
		recs.add(new ArtDataRecord(1, "A1", "Acrylic sheet", 5, 5));
		recs.add(new ArtDataRecord(1, "C1", "Canvas framed", 5, 5));

		ArtImporter importer = new ArtImporter(new StubParser(recs));
		List<Art> arts = importer.importFromFile("x");

		assertEquals(Material.GLASS, arts.get(0).getMaterial());
		assertEquals(Material.ACRYLIC, arts.get(1).getMaterial());
		assertEquals(Material.CANVAS_FRAMED, arts.get(2).getMaterial());
	}

	@Test
	void unknownMaterialYieldsUnknownEnumAndStillCreatesArt() {
		List<ArtDataRecord> recs = new ArrayList<>();
		recs.add(new ArtDataRecord(1, "U1", "mystery-material", 2, 3));

		ArtImporter importer = new ArtImporter(new StubParser(recs));
		List<Art> arts = importer.importFromFile("x");

		assertEquals(1, arts.size());
		assertEquals(Material.UNKNOWN, arts.get(0).getMaterial());
	}

	@Test
	void emptyParserResultReturnsEmptyList() {
		ArtImporter importer = new ArtImporter(new StubParser(new ArrayList<>()));
		List<Art> arts = importer.importFromFile("x");
		assertNotNull(arts);
		assertTrue(arts.isEmpty());
	}
}
