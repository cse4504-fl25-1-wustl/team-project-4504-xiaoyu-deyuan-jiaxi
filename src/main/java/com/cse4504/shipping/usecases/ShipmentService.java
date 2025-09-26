package com.cse4504.shipping.usecases;

import com.cse4504.shipping.domain.Shipment;
import com.cse4504.shipping.domain.CrateOrPallet;
import com.cse4504.shipping.domain.Box;
import com.cse4504.shipping.domain.Item;
import com.cse4504.shipping.interfaces.IInputParser;
import com.cse4504.shipping.interfaces.IOutputWriter;

import java.util.List;

public class ShipmentService {
    private final IInputParser inputParser;
    private final IOutputWriter outputWriter;
    private final PackingService packingService;
    private final WeightCalculator weightCalculator;

    public ShipmentService(IInputParser inputParser, IOutputWriter outputWriter) {
        this.inputParser = inputParser;
        this.outputWriter = outputWriter;
        this.packingService = new PackingService();
        this.weightCalculator = new WeightCalculator();
    }

    public void prepareShipment(String inputSource) {
        try {
            // Parse input
            List<Item> items = inputParser.parseInput(inputSource);

            // Pack items into boxes
            List<Box> boxes = packingService.packItemsIntoBoxes(items);

            // Pack boxes into crates/pallets
            List<CrateOrPallet> containers = packingService.packBoxesIntoCrates(boxes);

            // Create shipment
            Shipment shipment = new Shipment();
            for (CrateOrPallet container : containers) {
                shipment.addCrateOrPallet(container);
            }

            // Write output
            outputWriter.writeOutput("output.txt",shipment);

        } catch (Exception e) {
            throw new RuntimeException("Failed to prepare shipment: " + e.getMessage(), e);
        }
    }
}