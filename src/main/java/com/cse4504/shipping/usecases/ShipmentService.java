package com.cse4504.shipping.usecases;

import com.cse4504.shipping.domain.Box;
import com.cse4504.shipping.domain.CrateOrPallet;
import com.cse4504.shipping.domain.Item;
import com.cse4504.shipping.domain.Shipment;
import com.cse4504.shipping.domain.IInputParser;
import com.cse4504.shipping.domain.IOutputFormatter;

import java.util.List;

public class ShipmentService {
    private final IInputParser inputParser;
    private final IOutputFormatter outputFormatter;
    private final PackingService packingService;
    private final WeightCalculator weightCalculator;

    public ShipmentService(IInputParser inputParser, IOutputFormatter outputFormatter) {
        this.inputParser = inputParser;
        this.outputFormatter = outputFormatter;
        this.packingService = new PackingService();
        this.weightCalculator = new WeightCalculator();
    }

    public void prepareShipment(String inputSource){
        try {
            List <Item> items = inputParser.parseItems(inputSource);
            List <Box> boxes = packingService.packItemsIntoBoxes(items);
            List <CrateOrPallet> containers = packingService.packBoxIntoCrates(boxes);
            Shipment shipment = new Shipment();
            for (CrateOrPallet container : containers) {
                shipment.addCrateOrPallet(container);
            }
            outputWriter.writeOutput(shipment);
            
        } catch (Exception e) {
            throw new RuntimeException("Error preparing shipment: " + e.getMessage(), e);
        }
    }
}
