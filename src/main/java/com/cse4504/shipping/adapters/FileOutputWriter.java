package com.cse4504.shipping.adapters;

import com.cse4504.shipping.domain.*;
import com.cse4504.shipping.interfaces.IOutputWriter;

import java.io.FileWriter;
import java.io.IOException;

public class FileOutputWriter implements IOutputWriter {

    @Override
    public void writeOutput(String outputFile, Shipment shipment) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("Shipment Report\n");
            writer.write("====================\n");

            for (CrateOrPallet cp : shipment.getCratesOrPallets()) {
                writer.write("Crate/Pallet " + cp.getId() + ": \n");
                writer.write("  Total Weight: " + cp.getTotalWeight() + " kg\n");
                writer.write("  Height: " + cp.getHeight() + " m\n");

                for (Box box : cp.getBoxs()) {
                    writer.write("   Box " + box.getId() + " weight: " + box.getTotalWeight() + " kg\n");
                    writer.write("    Items: ");
                    for (Item item : box.getItems()) {
                        writer.write(item.getId() + "(" + item.getWeight() + "kg) ");
                    }
                    writer.write("\n");
                }
            }
            writer.write("\nShipment Total Weight: " + shipment.getTotalWeight() + " kg\n");
        }
    }
}
