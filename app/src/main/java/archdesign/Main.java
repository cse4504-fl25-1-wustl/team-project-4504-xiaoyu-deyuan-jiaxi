package archdesign;

import archdesign.entities.Art;
import archdesign.entities.enums.ShippingProvider;
import archdesign.entities.enums.ContainerType;
import archdesign.interactor.Packer;
import archdesign.interactor.PackingPlan;
import archdesign.interactor.UserConstraints;
import archdesign.request.ArtImporter;
import archdesign.parser.CsvParser;
import archdesign.response.Response;
import archdesign.response.ShipmentViewModel;
import archdesign.output.ConsoleOutputFormatter;
import archdesign.output.JsonOutputWriter;

import java.util.List;

/**
 * A simple command-line application to run the entire packing process.
 * This class acts as the final presentation layer (the CLI frontend).
 */
public class Main {

    /**
     * The main entry point for the Java application.
     * @param args Command line arguments:
     *             args[0]: path to CSV file (required)
     *             args[1]: optional output JSON file path
     *             args[2]: optional packing mode: "box-only", "crate-only", or "default" (uses both boxes and crates)
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Please provide the path to the CSV file as an argument.");
            System.err.println("Usage: java Main <path/to/your/file.csv> [optional-output.json] [packing-mode]");
            System.err.println("Packing modes: box-only, crate-only, default (default uses both boxes and crates)");
            return;
        }
        
        String filePath = args[0];
        String outputFilePath = args.length > 1 && !args[1].equals("box-only") && !args[1].equals("crate-only") && !args[1].equals("default") ? args[1] : null;
        
        // Determine packing mode from arguments
        String packingMode = "default";
        if (args.length > 1) {
            String lastArg = args[args.length - 1];
            if (lastArg.equals("box-only") || lastArg.equals("crate-only") || lastArg.equals("default")) {
                packingMode = lastArg;
            }
        }
        
        System.out.println("--- Starting Packer Process for file: " + filePath + " ---");
        System.out.println("Packing mode: " + packingMode);

        ShipmentViewModel viewModel = processFile(filePath, packingMode);

        // Display on console using the formatter
        System.out.println("\n--- Displaying Packing Plan ---");
        ConsoleOutputFormatter consoleFormatter = new ConsoleOutputFormatter();
        consoleFormatter.display(viewModel);
        
        // If output file is specified, write JSON output
        if (outputFilePath != null) {
            System.out.println("\n--- Writing JSON output to: " + outputFilePath + " ---");
            JsonOutputWriter jsonWriter = new JsonOutputWriter();
            jsonWriter.write(viewModel, outputFilePath);
        }
    }

    /**
     * Public helper used by tests and the CLI: process the given CSV file and
     * return the generated ShipmentViewModel.
     * @param filePath path to CSV file
     * @return ShipmentViewModel (may be null)
     */
    public static ShipmentViewModel processFile(String filePath) {
        return processFile(filePath, "default");
    }

    /**
     * Process the given CSV file with a specific packing mode.
     * @param filePath path to CSV file
     * @param packingMode "box-only", "crate-only", or "default"
     * @return ShipmentViewModel (may be null)
     */
    public static ShipmentViewModel processFile(String filePath, String packingMode) {
        // --- "IN" PART ---
        ArtImporter importer = new ArtImporter(new CsvParser());
        List<Art> artsToPack = importer.importFromFile(filePath);
        System.out.println("Successfully imported " + artsToPack.size() + " total art items.");

        // --- "CORE" PART ---
        UserConstraints constraints = buildConstraints(packingMode);

        ShippingProvider provider = ShippingProvider.PLACEHOLDER;
        System.out.println("\n--- Running Packer Algorithm... ---");

        // Normal flow: use the constraints based on packing mode
        PackingPlan finalPlan = Packer.pack(artsToPack, constraints, provider);

        // --- "OUT" PART ---
        System.out.println("\n--- Generating Response ViewModel... ---");
        Response response = new Response(finalPlan);
        return response.generateViewModel();
    }

    /**
     * Build user constraints based on the specified packing mode.
     * @param packingMode "box-only", "crate-only", or "default"
     * @return UserConstraints object configured for the specified mode
     */
    private static UserConstraints buildConstraints(String packingMode) {
        switch (packingMode.toLowerCase()) {
            case "box-only":
                // Only use STANDARD and LARGE boxes with pallets
                return UserConstraints.newBuilder()
                    .withAllowedBoxTypes(java.util.List.of(
                        archdesign.entities.enums.BoxType.STANDARD,
                        archdesign.entities.enums.BoxType.LARGE
                    ))
                    .withAllowedContainerTypes(java.util.List.of(
                        ContainerType.STANDARD_PALLET,
                        ContainerType.OVERSIZE_PALLET
                    ))
                    .build();
            
            case "crate-only":
                // Only use CRATE boxes with crates
                return UserConstraints.newBuilder()
                    .withAllowedBoxTypes(java.util.List.of(
                        archdesign.entities.enums.BoxType.CRATE
                    ))
                    .withAllowedContainerTypes(java.util.List.of(
                        ContainerType.STANDARD_CRATE
                    ))
                    .build();
            
            case "default":
            default:
                // Use default: both boxes (STANDARD, LARGE, CRATE) and all containers
                return new UserConstraints();
        }
    }
}