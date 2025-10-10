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

                
                String id = get(tokens, headerIndex, "tag number", "UNKNOWN");
                int quantity = parseInt(get(tokens, headerIndex, "quantity", "1"));

                int width = (int) Math.ceil(parseDouble(get(tokens, headerIndex, "Outside Size Width", "0")));
                int height = (int) Math.ceil(parseDouble(get(tokens, headerIndex, "Outside Size Height", "0")));
                int length = 0; 

                
                String medium = get(tokens, headerIndex, "Final medium", "");
                String glazing = get(tokens, headerIndex, "Glazing", "");
                Material material = determineMaterial(medium, glazing);

                
                float weight = calculateWeight(material, width, height, length);

                boolean inBox = false;

                
                for (int i = 0; i < quantity; i++) {
                    String uniqueId = quantity > 1 ? id + "_" + (i + 1) : id;
                    arts.add(new Art(uniqueId, weight, height, width, length, inBox, material));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return arts;
    }

    private Material determineMaterial(String medium, String glazing) {
        if (medium.contains("Paper Print") && medium.contains("Framed")) {
            if (glazing.contains("Glass")) {
                return Material.GLASS; 
            } else if (glazing.contains("Acrylic")) {
                return Material.ACRYLIC; 
            }
        }
        else if (medium.contains("Acoustic Panel") && medium.contains("Framed")) {
            return Material.ACOUSTIC_PANEL_FRAMED;
        } else if (medium.contains("Acoustic Panel")) {
            return Material.ACOUSTIC_PANEL;
        } else if (medium.contains("Patient Board")) {
            return Material.PATIENT_BOARD;
        }
        else if (medium.contains("Canvas") && medium.contains("Gallery")) {
            return Material.CANVAS_GALLERY;
        } else if (medium.contains("Canvas") && medium.contains("Framed")) {
            return Material.CANVAS_FRAMED;
        } else if (medium.contains("Mirror")) {
            return Material.MIRROR;
        }
        return Material.UNKNOWN;
    }

    private float calculateWeight(Material material, int width, int height, int length) {
        // Calculate area in square inches
        double area = width * height;
        // Calculate volume in cubic inches (if we had depth)
        double volume = width * height * (length == 0 ? 1 : length);
        
        // Use material's weight per square inch to calculate base weight
        double baseWeight = area * material.getWeight();
        
        // Add some base weight for framing/packaging
        double totalWeight = baseWeight + 2.0; // 2kg base for framing
        
        return (float) totalWeight;
    }

    
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

    private int parseInt(String val) {
        try { return Integer.parseInt(val); } catch (Exception e) { return 1; }
    }
}