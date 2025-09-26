package com.cse4504.shipping.adapters;

import com.cse4504.shipping.domain.Item;
import com.cse4504.shipping.interfaces.IInputParser;

import java.io.*;
import java.util.*;

public class CLIInputParser implements IInputParser {

    @Override
    public List<Item> parseInput(String inputFile) throws IOException {
        List<Item> items = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // assume input format: id, weight, length, width, height
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String id = parts[0].trim();
                    double weight = Double.parseDouble(parts[1].trim());
                    double length = Double.parseDouble(parts[2].trim());
                    double width  = Double.parseDouble(parts[3].trim());
                    double height = Double.parseDouble(parts[4].trim());

                    items.add(new Item(id, weight, length, width, height));
                }
            }
        }
        return items;
    }
}
