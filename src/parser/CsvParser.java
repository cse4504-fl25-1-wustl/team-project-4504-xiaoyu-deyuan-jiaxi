package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A concrete implementation of the ArtDataParser for CSV files.
 */
public class CsvParser implements ArtDataParser {

    private static final String CSV_DELIMITER = ",";

    @Override
    public List<ArtDataRecord> parse(String filePath) {
        List<ArtDataRecord> records = new ArrayList<>();
        String line = "";

        // The try-with-resources statement ensures the BufferedReader is closed automatically.
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip the header line
            br.readLine();

            while ((line = br.readLine()) != null) {
                try {
                    String[] values = line.split(CSV_DELIMITER);
                    
                    if (values.length < 6) continue; // Skip malformed lines

                    // Extract data from the correct columns based on the sample
                    int quantity = Integer.parseInt(values[1].trim());
                    String tagNumber = values[2].trim();
                    String finalMedium = values[3].trim();
                    int width = Integer.parseInt(values[4].trim());
                    int height = Integer.parseInt(values[5].trim());

                    records.add(new ArtDataRecord(quantity, tagNumber, finalMedium, width, height));
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Could not parse a number on line: " + line + ". Skipping.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error: Failed to read the file at " + filePath);
            e.printStackTrace();
        }

        return records;
    }
}