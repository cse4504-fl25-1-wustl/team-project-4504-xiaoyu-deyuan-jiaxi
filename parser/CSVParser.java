package parser;

import entities.Art;
import entities.Material;
import java.io.*;
import java.util.*;

public class CSVParser {

    public List<Art> parse(String filePath) {
        List<Art> arts = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine();
            if (headerLine == null) return arts;
            String[] headers = headerLine.split(",");
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].trim(), i);
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] tokens = line.split(",");

                String id = get(tokens, headerIndex, "Tag #", "UNKNOWN");

                int width = (int) Math.ceil(parseDouble(get(tokens, headerIndex, "Outside Size Width", "0")));
                int height = (int) Math.ceil(parseDouble(get(tokens, headerIndex, "Outside Size Height", "0")));
                int length = 0; // Some CSVs may not have depth/thickness, default to 0

                // Weight: use the "Weight" column if it exists, otherwise 0
                float weight = parseFloat(get(tokens, headerIndex, "Weight", "0"));

                String materialStr = get(tokens, headerIndex, "Material", "UNKNOWN");
                Material material = Material.fromString(materialStr);

                // inBox: usually decided later by Packer, default to false
                boolean inBox = false;

                arts.add(new Art(id, weight, height, width, length, inBox, material));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return arts;
    }

    // Helper: safely get a column value, return default if missing or out of bounds
    private String get(String[] tokens, Map<String, Integer> idx, String col, String def) {
        Integer i = idx.get(col);
        if (i == null || i >= tokens.length) return def;
        String v = tokens[i].trim();
        return v.isEmpty() ? def : v;
    }

    private double parseDouble(String val) {
        try { return Double.parseDouble(val); } catch (Exception e) { return 0.0; }
    }

    private float parseFloat(String val) {
        try { return Float.parseFloat(val); } catch (Exception e) { return 0f; }
    }
}
