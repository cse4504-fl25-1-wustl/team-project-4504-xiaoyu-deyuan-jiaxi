package archdesign.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A concrete implementation of the ArtDataParser for CSV files.
 * Supports two CSV formats:
 * - Old format: line number, quantity, tag number, Final medium, Outside Size Width, Outside Size Height, Glazing, ...
 * - New format: Line Number, Quantity, Location, Floor, Tag #, Outside Size Width, Outside Size Height, Final Medium, ..., Glazing, ...
 */
public class CsvParser implements ArtDataParser {

    private static final String CSV_DELIMITER = ",";

    @Override
    public List<ArtDataRecord> parse(String filePath) {
        List<ArtDataRecord> records = new ArrayList<>();
        String line = "";

        // The try-with-resources statement ensures the BufferedReader is closed automatically.
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Read and parse the header line to determine format
            String headerLine = br.readLine();
            if (headerLine == null) {
                return records;
            }

            FormatDetector detector = new FormatDetector(headerLine);

            while ((line = br.readLine()) != null) {
                try {
                    String[] values = line.split(CSV_DELIMITER, -1); // -1 keeps trailing empty strings
                    
                    if (values.length < detector.getMinRequiredColumns()) {
                        continue; // Skip malformed lines
                    }

                    int quantity = Integer.parseInt(values[detector.getQuantityIndex()].trim());
                    String tagNumber = values[detector.getTagNumberIndex()].trim();
                    String finalMedium = values[detector.getFinalMediumIndex()].trim();
                    double width = Double.parseDouble(values[detector.getWidthIndex()].trim());
                    double height = Double.parseDouble(values[detector.getHeightIndex()].trim());
                    
                    // Extract Glazing column if it exists
                    String glazing = "";
                    if (detector.getGlazingIndex() >= 0 && values.length > detector.getGlazingIndex()) {
                        glazing = values[detector.getGlazingIndex()].trim();
                    }
                    
                    // Combine finalMedium and glazing for material detection
                    String combinedMaterialInfo = finalMedium + " " + glazing;

                    records.add(new ArtDataRecord(quantity, tagNumber, combinedMaterialInfo, width, height));
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Could not parse a number on line: " + line + ". Skipping.");
                } catch (IndexOutOfBoundsException e) {
                    System.err.println("Warning: Line does not have expected columns: " + line + ". Skipping.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error: Failed to read the file at " + filePath);
            e.printStackTrace();
        }

        return records;
    }

    /**
     * Inner class to detect and handle different CSV format column mappings.
     */
    private static class FormatDetector {
        private int quantityIndex;
        private int tagNumberIndex;
        private int finalMediumIndex;
        private int widthIndex;
        private int heightIndex;
        private int glazingIndex;

        FormatDetector(String headerLine) {
            String[] headers = headerLine.split(CSV_DELIMITER, -1);
            detectFormat(headers);
        }

        private void detectFormat(String[] headers) {
            // Try to detect format by looking for key header names
            boolean isNewFormat = false;
            for (String header : headers) {
                String normalized = header.trim().toLowerCase();
                // New format has headers like "Tag #", "new: Presentation Conversion", etc.
                if (normalized.contains("tag #") || normalized.contains("location") || normalized.contains("floor")) {
                    isNewFormat = true;
                    break;
                }
            }

            if (isNewFormat) {
                detectNewFormat(headers);
            } else {
                detectOldFormat(headers);
            }
        }

        private void detectOldFormat(String[] headers) {
            // Old format: line number, quantity, tag number, Final medium, Outside Size Width, Outside Size Height, Glazing, ...
            quantityIndex = 1;
            tagNumberIndex = 2;
            finalMediumIndex = 3;
            widthIndex = 4;
            heightIndex = 5;
            glazingIndex = 6;
        }

        private void detectNewFormat(String[] headers) {
            // New format: Line Number, Quantity, Location, Floor, Tag #, Outside Size Width, Outside Size Height, Final Medium, ..., Glazing, ...
            quantityIndex = 1;
            tagNumberIndex = 4;
            widthIndex = 5;
            heightIndex = 6;
            finalMediumIndex = 7;
            glazingIndex = -1; // Will search for it by header name

            // Search for glazing column by header name
            for (int i = 0; i < headers.length; i++) {
                String normalized = headers[i].trim().toLowerCase();
                if (normalized.contains("glazing")) {
                    glazingIndex = i;
                    break;
                }
            }
        }

        int getQuantityIndex() { return quantityIndex; }
        int getTagNumberIndex() { return tagNumberIndex; }
        int getFinalMediumIndex() { return finalMediumIndex; }
        int getWidthIndex() { return widthIndex; }
        int getHeightIndex() { return heightIndex; }
        int getGlazingIndex() { return glazingIndex; }

        int getMinRequiredColumns() {
            // Minimum columns needed for parsing
            int maxIndex = Math.max(
                Math.max(quantityIndex, tagNumberIndex),
                Math.max(finalMediumIndex, Math.max(widthIndex, heightIndex))
            );
            return maxIndex + 1;
        }
    }
}