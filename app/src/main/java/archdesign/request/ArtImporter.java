package request;

import entities.Art;
import entities.enums.Material;
import parser.ArtDataParser;
import parser.ArtDataRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for converting intermediate data records into a final list of Art domain objects.
 * This class handles the "Transform" and "Load" part of the ETL process, including
 * expanding quantities and generating unique IDs.
 */
public class ArtImporter {
    
    private final ArtDataParser parser;

    public ArtImporter(ArtDataParser parser) {
        this.parser = parser;
    }

    /**
     * Imports arts from a file, creating a list of Art objects ready for the Packer.
     * @param filePath The path to the data file.
     * @return A list of fully formed Art objects.
     */
    public List<Art> importFromFile(String filePath) {
        // Step 1: Use the parser to get raw data records.
        List<ArtDataRecord> records = parser.parse(filePath);

        // Step 2: Transform the raw records into Art domain objects.
        List<Art> arts = new ArrayList<>();
        for (ArtDataRecord record : records) {
            // The core logic for handling the 'quantity' field.
            for (int i = 0; i < record.quantity(); i++) {
                // Generate a unique ID for EACH individual piece of art.
                // We combine the tag number with a unique counter for clarity.
                String uniqueId = "Tag" + record.tagNumber() + "-Item" + (i + 1);
                
                // Map the string from the file to our Material enum.
                Material material = Material.fromString(record.finalMedium());
                if (material == Material.UNKNOWN) {
                    // It's good practice to try to infer the material if possible,
                    // e.g., from the "Glazing" column in your sample.
                    // For now, we'll just log a warning.
                    System.out.println("Warning: Unknown material '" + record.finalMedium() + "' for " + uniqueId);
                }

                arts.add(new Art(uniqueId, record.height(), record.width(), 0, material));
            }
        }

        return arts;
    }
}