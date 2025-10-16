package parser;

/**
 * A simple, immutable data record to hold the raw data parsed from a single
 * row of an input file (e.g., a CSV). This serves as an intermediate DTO.
 */
public record ArtDataRecord(
    int quantity,
    String tagNumber,
    String finalMedium,
    int width,
    int height
) {
}