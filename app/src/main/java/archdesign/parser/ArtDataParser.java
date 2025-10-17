package parser;

import java.util.List;

/**
 * An interface for all file parsers. It defines a contract for converting
 * a file at a given path into a list of intermediate ArtDataRecord objects.
 */
public interface ArtDataParser {

    /**
     * Parses a file and converts its contents into a list of ArtDataRecord objects.
     * @param filePath The path to the file to be parsed.
     * @return A list of ArtDataRecord, representing the rows in the file.
     */
    List<ArtDataRecord> parse(String filePath);
}