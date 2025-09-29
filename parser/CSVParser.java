package parser;

import entities.Art;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {

    /**
     * Parses a CSV file from the given file path and returns a list of Art objects.
     *
     * @param filePath The path to the CSV file.
     * @return A list of Art objects parsed from the CSV file.
     */
    public List<Art> parse(String filePath)
    {
        // Dummy implementation
        // In a real implementation, this method would read the CSV file at the given filePath,
        // parse its contents, and create a list of Art objects.
        System.out.println("Parsing CSV file from: " + filePath);
        return new ArrayList<>();
    }
}