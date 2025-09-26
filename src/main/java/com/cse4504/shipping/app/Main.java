package com.cse4504.shipping.app;

import com.cse4504.shipping.adapters.CLIInputParser;
import com.cse4504.shipping.adapters.FileOutputWriter;
import com.cse4504.shipping.interfaces.IInputParser;
import com.cse4504.shipping.interfaces.IOutputWriter;
import com.cse4504.shipping.usecases.ShipmentService;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <input_file_path>");
            System.out.println("Example: java Main input.txt");
            return;
        }

        String inputFilePath = args[0];

        try {
            // 1. Initialize Adapters (Input and Output)
            IInputParser inputParser = new CLIInputParser();
            IOutputWriter outputWriter = new FileOutputWriter();

            // 2. Initialize the main Use Case/Service with dependencies
            ShipmentService shipmentService = new ShipmentService(inputParser, outputWriter);

            // 3. Execute the main application logic
            System.out.println("Starting shipment preparation for input file: " + inputFilePath);
            shipmentService.prepareShipment(inputFilePath);
            System.out.println("Shipment prepared successfully. Report written to output.txt");

        } catch (Exception e) {
            System.err.println("An error occurred during shipment preparation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}