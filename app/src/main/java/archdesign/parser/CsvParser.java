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
 *
 * Enhanced with input validation and error tracking.
 */
public class CsvParser implements ArtDataParser {

    private static final String CSV_DELIMITER = ",";
    private List<String> parseWarnings = new ArrayList<>();
    private int skippedLines = 0;

    @Override
    public List<ArtDataRecord> parse(String filePath) {
        List<ArtDataRecord> records = new ArrayList<>();
        parseWarnings.clear();
        skippedLines = 0;

        // The try-with-resources statement ensures the BufferedReader is closed automatically.
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Read and parse the header line to determine format
            String headerLine = br.readLine();
            if (headerLine == null) {
                return records;
            }

            FormatDetector detector = new FormatDetector(headerLine);

            int lineNumber = 2; // Start from 2 since header is line 1
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] values = line.split(CSV_DELIMITER, -1); // -1 keeps trailing empty strings
                    
                    if (values.length < detector.getMinRequiredColumns()) {
                        String warning = String.format("Line %d: Insufficient columns (expected %d, got %d). Skipping.",
                                lineNumber, detector.getMinRequiredColumns(), values.length);
                        parseWarnings.add(warning);
                        skippedLines++;
                        lineNumber++;
                        continue;
                    }

                    int quantity = Integer.parseInt(values[detector.getQuantityIndex()].trim());
                    
                    // Validate quantity (allow zero but reject negative)
                    if (quantity < 0) {
                        String warning = String.format("Line %d: Quantity must be non-negative (%d provided). Skipping.", lineNumber, quantity);
                        parseWarnings.add(warning);
                        skippedLines++;
                        lineNumber++;
                        continue;
                    }
                    
                    String tagNumber = values[detector.getTagNumberIndex()].trim();
                    String finalMedium = values[detector.getFinalMediumIndex()].trim();
                    double width = Double.parseDouble(values[detector.getWidthIndex()].trim());
                    double height = Double.parseDouble(values[detector.getHeightIndex()].trim());
                    
                    // Validate dimensions
                    if (width <= 0 || height <= 0) {
                        String warning = String.format("Line %d: Dimensions must be positive (%.2f x %.2f). Skipping.",
                                lineNumber, width, height);
                        parseWarnings.add(warning);
                        skippedLines++;
                        lineNumber++;
                        continue;
                    }
                    
                    // Extract Glazing column if it exists
                    String glazing = "";
                    if (detector.getGlazingIndex() >= 0 && values.length > detector.getGlazingIndex()) {
                        glazing = values[detector.getGlazingIndex()].trim();
                    }
                    
                    // Combine finalMedium and glazing for material detection
                    String combinedMaterialInfo = finalMedium + " " + glazing;

                    records.add(new ArtDataRecord(quantity, tagNumber, combinedMaterialInfo, width, height));
                    lineNumber++;
                } catch (NumberFormatException e) {
                    String warning = String.format("Line %d: Invalid number format. Details: %s. Skipping.",
                            lineNumber, e.getMessage());
                    parseWarnings.add(warning);
                    skippedLines++;
                } catch (IndexOutOfBoundsException e) {
                    String warning = String.format("Line %d: Column index out of bounds. Skipping.", lineNumber);
                    parseWarnings.add(warning);
                    skippedLines++;
                }
            }
        } catch (IOException e) {
            String errorMsg = String.format("Error reading file '%s': %s", filePath, e.getMessage());
            parseWarnings.add(errorMsg);
        }

        return records;
    }

    /**
     * Get all warnings accumulated during parsing.
     * @return list of warning messages
     */
    public List<String> getParseWarnings() {
        return new ArrayList<>(parseWarnings);
    }

    /**
     * Get the number of lines skipped during parsing.
     * @return number of skipped lines
     */
    public int getSkippedLineCount() {
        return skippedLines;
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
            boolean hasNamedColumns = false;
            
            for (String header : headers) {
                String normalized = header.trim().toLowerCase();
                // New format has headers like "Tag #", "new: Presentation Conversion", etc.
                if (normalized.contains("tag #") || normalized.contains("location") || normalized.contains("floor")) {
                    isNewFormat = true;
                    break;
                }
                // Check if headers contain recognizable column names (for column-order-independent parsing)
                // Supports both spaced ("tag number") and no-space ("tagnumber") formats
                if (normalized.contains("quantity") || normalized.contains("tag number") || normalized.equals("tagnumber") ||
                    normalized.contains("final medium") || normalized.equals("finalmedium") || 
                    normalized.contains("outside size") || normalized.equals("outsidesizewidth") || normalized.equals("outsidesizeheight")) {
                    hasNamedColumns = true;
                }
            }

            if (isNewFormat) {
                detectNewFormat(headers);
            } else if (hasNamedColumns) {
                detectNamedColumnFormat(headers);
            } else {
                detectOldFormat(headers);
            }
        }

        private void detectOldFormat(String[] headers) {
            // Old format: line number, quantity, tag number, Final medium, Outside Size Width, Outside Size Height, Glazing, ...
            // Fixed column indices for old format (headers parameter kept for consistency with detectNewFormat)
            quantityIndex = 1;
            tagNumberIndex = 2;
            finalMediumIndex = 3;
            widthIndex = 4;
            heightIndex = 5;
            glazingIndex = 6;
        }

        private void detectNamedColumnFormat(String[] headers) {
            // Parse columns by header name, order-independent
            quantityIndex = -1;
            tagNumberIndex = -1;
            finalMediumIndex = -1;
            widthIndex = -1;
            heightIndex = -1;
            glazingIndex = -1;

            for (int i = 0; i < headers.length; i++) {
                String normalized = headers[i].trim().toLowerCase();
                if (normalized.contains("quantity")) {
                    quantityIndex = i;
                } else if (normalized.contains("tag number") || normalized.equals("tagnumber")) {
                    tagNumberIndex = i;
                } else if (normalized.contains("final medium") || normalized.equals("finalmedium")) {
                    finalMediumIndex = i;
                } else if (normalized.contains("outside size width") || normalized.equals("outsidesizewidth")) {
                    widthIndex = i;
                } else if (normalized.contains("outside size height") || normalized.equals("outsidesizeheight")) {
                    heightIndex = i;
                } else if (normalized.contains("glazing")) {
                    glazingIndex = i;
                }
            }

            // Fallback to old format indices if any required column not found
            if (quantityIndex < 0) quantityIndex = 1;
            if (tagNumberIndex < 0) tagNumberIndex = 2;
            if (finalMediumIndex < 0) finalMediumIndex = 3;
            if (widthIndex < 0) widthIndex = 4;
            if (heightIndex < 0) heightIndex = 5;
            // Default glazing index for backward compatibility (data may have glazing at index 6 even without header)
            if (glazingIndex < 0) glazingIndex = 6;
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