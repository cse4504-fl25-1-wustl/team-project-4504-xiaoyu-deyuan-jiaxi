package parser;

import entities.Art;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {

    public List<Art> parse(String filePath) {
        List<Art> items = new ArrayList<>();
        if (filePath == null) return items;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // Skip header line
                if (isFirstLine && (line.toLowerCase().startsWith("id") || line.toLowerCase().startsWith("item"))) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length < 5) {
                    System.out.println("Skipping line (not enough data): " + line);
                    continue;
                }

                try {
                    String id = parts[0].trim();
                    float weight = Float.parseFloat(parts[1].trim());
                    int height = Integer.parseInt(parts[2].trim());
                    int width = Integer.parseInt(parts[3].trim());
                    int length = Integer.parseInt(parts[4].trim());
                    boolean inBox = parts.length >= 6 && Boolean.parseBoolean(parts[5].trim());
                    
                    Art art = new Art(id, weight, height, width, length, inBox);
                    items.add(art);
                    
                } catch (NumberFormatException e) {
                    System.out.println("Skipping line (invalid number): " + line);
                }
                isFirstLine = false;
            }
            
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        System.out.println("Parsed " + items.size() + " items from CSV");
        return items;
    }
}