package parser;

import entities.Art;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {

    /**
     * Expected CSV format (one item per line):
     * id,weight,height,width,length[,inBox]
     * inBox is optional (true/false) and defaults to false.
     */
    public List<Art> parse(String filePath)
    {
        List<Art> items = new ArrayList<>();
        System.out.println("Parsing CSV file from: " + filePath);
        if (filePath == null) return items;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // skip common header lines if detected
                if (line.toLowerCase().startsWith("id,") || line.toLowerCase().startsWith("item")) continue;

                String[] parts = line.split(",");
                if (parts.length < 5) {
                    System.out.println("Skipping malformed line (not enough columns): " + line);
                    continue;
                }

                try {
                    String id = parts[0].trim();
                    float weight = Float.parseFloat(parts[1].trim());
                    int height = Integer.parseInt(parts[2].trim());
                    int width = Integer.parseInt(parts[3].trim());
                    int length = Integer.parseInt(parts[4].trim());
                    boolean inBox = false;
                    if (parts.length >= 6) {
                        inBox = Boolean.parseBoolean(parts[5].trim());
                    }
                    Art art = new Art(id, weight, height, width, length, inBox);
                    items.add(art);
                } catch (NumberFormatException nfe) {
                    System.out.println("Skipping malformed numeric data in line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }

        return items;
    }
}