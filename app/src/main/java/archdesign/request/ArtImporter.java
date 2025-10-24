package archdesign.request;

import archdesign.entities.Art;
import archdesign.entities.enums.Material;
import archdesign.parser.ArtDataParser;
import archdesign.parser.ArtDataRecord;

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
                
                // Fuzzy match material from combined material info
                Material material = fuzzyMatchMaterial(record.finalMedium());
                if (material == Material.UNKNOWN) {
                    System.out.println("Warning: Unknown material '" + record.finalMedium() + "' for " + uniqueId);
                }

                arts.add(new Art(uniqueId, record.height(), record.width(), 0, material));
            }
        }

        return arts;
    }
    
    /**
     * Performs fuzzy matching to determine material type based on keywords.
     * The combined material info from both "Final medium" and "Glazing" columns
     * is searched for specific keywords to identify the material.
     * 
     * @param materialInfo Combined string from finalMedium and glazing columns
     * @return The matched Material enum, or Material.UNKNOWN if no match
     */
    private Material fuzzyMatchMaterial(String materialInfo) {
        if (materialInfo == null || materialInfo.trim().isEmpty()) {
            return Material.UNKNOWN;
        }
        
        // Convert to lowercase for case-insensitive matching
        String lowerInfo = materialInfo.toLowerCase();
        
        // Check for each material type using fuzzy/substring matching
        // Order matters: check more specific terms first
        
        // Check for framed canvas types first (more specific)
        if (lowerInfo.contains("canvas") && lowerInfo.contains("framed")) {
            return Material.CANVAS_FRAMED;
        }
        if (lowerInfo.contains("canvas") && lowerInfo.contains("gallery")) {
            return Material.CANVAS_GALLERY;
        }
        
        // Check for acoustic panel types
        if (lowerInfo.contains("acoustic") && lowerInfo.contains("framed")) {
            return Material.ACOUSTIC_PANEL_FRAMED;
        }
        if (lowerInfo.contains("acoustic")) {
            return Material.ACOUSTIC_PANEL;
        }
        
        // Check for patient board
        if (lowerInfo.contains("patient") && lowerInfo.contains("board")) {
            return Material.PATIENT_BOARD;
        }
        
        // Check for mirror
        if (lowerInfo.contains("mirror")) {
            return Material.MIRROR;
        }
        
        // Check for acrylic (before glass, as acrylic might be mentioned with glass)
        if (lowerInfo.contains("acrylic") || lowerInfo.contains("plexiglass") || lowerInfo.contains("plexi")) {
            return Material.ACRYLIC;
        }
        
        // Check for glass (most common, check last among materials)
        if (lowerInfo.contains("glass")) {
            return Material.GLASS;
        }
        
        // Check for general canvas (after specific types)
        if (lowerInfo.contains("canvas")) {
            return Material.CANVAS_FRAMED; // Default to framed if no other qualifier
        }
        
        return Material.UNKNOWN;
    }
}