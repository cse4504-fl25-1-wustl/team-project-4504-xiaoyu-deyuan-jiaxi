package com.cse4504.shipping.usecases;

import java.util.ArrayList;
import java.util.List;

import com.cse4504.shipping.domain.Box;
import com.cse4504.shipping.domain.CrateOrPallet;
import com.cse4504.shipping.domain.Item;

public class PackingService {
    private static final double DEFAULT_BOX_MAX_WEIGHT = 20.0; //kg
    private static final double DEFAULT_BOX_MAX_VOLUME = 0.1; //cubic meters
    private static final double DEFAULT_CRATE_MAX_WEIGHT = 100.0; // kg
    private static final double DEFAULT_PALLET_MAX_WEIGHT = 500.0; //kg

    public List<Box> packItemsIntoBoxes(List<Item> items) {
        List<Box> boxes = new ArrayList<>();
        int boxCount = 1;
        
        for (Item item : items) {
            boolean itemPacked = false;
            for (Box box : boxes) {
                if (box.canAddItem(item)) {
                    box.addItem(item);
                    itemPacked = true;
                    break;
                }
            }
            if (!itemPacked) {
                Box newBox = new Box("Box-" + boxCount++, DEFAULT_BOX_MAX_WEIGHT, DEFAULT_BOX_MAX_VOLUME);
                newBox.addItem(item);
                boxes.add(newBox);
            }
        }
        
        return boxes;
    }

    public List<CrateOrPallet> packBoxIntoCrates(List<Box> boxes) {
        List<CrateOrPallet> containers = new ArrayList<>();
        int crateCounter = 1;
        int palletCounter = 1;
        
        for (Box box : boxes) {
            boolean boxPacked = false;
            for (CrateOrPallet container : containers) {
                if (container.canAddBox(box)) {
                    container.addBox(box);
                    boxPacked = true;
                    break;
                }
            }
            if (!boxPacked) {
                // Decide whether to use crate or pallet based on box weight
                CrateOrPallet.Type type = box.getTotalWeight() > 50.0 ? 
                    CrateOrPallet.Type.PALLET : CrateOrPallet.Type.CRATE;
                
                double maxWeight = type == CrateOrPallet.Type.PALLET ? 
                    DEFAULT_PALLET_MAX_WEIGHT : DEFAULT_CRATE_MAX_WEIGHT;

                String id = type == CrateOrPallet.Type.PALLET ? 
                    "PALLET-" + palletCounter++ : "CRATE-" + crateCounter++;

                CrateOrPallet newContainer = new CrateOrPallet(id, type, maxWeight, 2.4);
                newContainer.addBox(box);
                containers.add(newContainer);
            }
        }

        return containers;
    }
    
    
}
