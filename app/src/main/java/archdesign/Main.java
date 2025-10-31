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
import archdesign.response.ContainerViewModel;
import archdesign.response.BoxViewModel;
import archdesign.response.ArtViewModel;
import archdesign.response.JsonOutputSchema;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * A simple command-line application to run the entire packing process.
 * This class acts as the final presentation layer (the CLI frontend).
 */
public class Main {

    /**
     * The main entry point for the Java application.
     * @param args Command line arguments, expecting the path to a CSV file and optionally an output JSON file.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Please provide the path to the CSV file as an argument.");
            System.err.println("Usage: java Main <path/to/your/file.csv> [optional-output.json]");
            return;
        }
        String filePath = args[0];
        String outputFilePath = args.length > 1 ? args[1] : null;
        
        System.out.println("--- Starting Packer Process for file: " + filePath + " ---");

        ShipmentViewModel viewModel = processFile(filePath);

        // display on console
        System.out.println("\n--- Displaying Packing Plan ---");
        displayViewModel(viewModel);
        
        // If output file is specified, write JSON output
        if (outputFilePath != null) {
            System.out.println("\n--- Writing JSON output to: " + outputFilePath + " ---");
            writeJsonOutput(viewModel, outputFilePath);
        }
    }

    /**
     * Public helper used by tests and the CLI: process the given CSV file and
     * return the generated ShipmentViewModel.
     * @param filePath path to CSV file
     * @return ShipmentViewModel (may be null)
     */
    public static ShipmentViewModel processFile(String filePath) {
        // --- "IN" PART ---
        ArtImporter importer = new ArtImporter(new CsvParser());
        List<Art> artsToPack = importer.importFromFile(filePath);
        System.out.println("Successfully imported " + artsToPack.size() + " total art items.");

        // --- "CORE" PART ---
        // Allow tests to request crate-preferred packing via system property
        String preferCrates = System.getProperty("packing.preferCrates");
        UserConstraints constraints;
        if ("true".equalsIgnoreCase(preferCrates)) {
            // Prefer crates but allow pallets as fallbacks so tests requesting
            // 'crate preference' don't force an infeasible-only-crate plan.
            constraints = UserConstraints.newBuilder()
                .withAllowedContainerTypes(java.util.List.of(
                    ContainerType.STANDARD_CRATE,
                    ContainerType.STANDARD_PALLET,
                    ContainerType.OVERSIZE_PALLET
                ))
                .build();
        } else {
            constraints = new UserConstraints();
        }

        ShippingProvider provider = ShippingProvider.PLACEHOLDER;
        System.out.println("\n--- Running Packer Algorithm... ---");

        // If tests requested crate preference, attempt a crate-only plan first
        // and only fall back to allowing pallets if the crate-only plan cannot
        // pack all items (avoids choosing pallets when crates are feasible).
        PackingPlan finalPlan;
        if ("true".equalsIgnoreCase(preferCrates)) {
            UserConstraints crateOnly = UserConstraints.newBuilder()
                .withAllowedContainerTypes(java.util.List.of(ContainerType.STANDARD_CRATE))
                .build();

            System.out.println("Attempting crate-only packing (test preference)...");
            finalPlan = Packer.pack(artsToPack, crateOnly, provider);

            // Count how many arts were actually placed in the plan
            int packedCount = 0;
            if (finalPlan != null && finalPlan.getContainers() != null) {
                for (archdesign.entities.Container c : finalPlan.getContainers()) {
                    for (archdesign.entities.Box b : c.getBoxesInContainer()) {
                        packedCount += b.getArtsInBox().size();
                    }
                }
            }

            if (packedCount < artsToPack.size()) {
                System.out.println("Crate-only plan packed " + packedCount + " of " + artsToPack.size() + " items — retrying with pallets allowed.");

                System.out.println("Crate-only plan packed " + packedCount + " of " + artsToPack.size() + " items — retrying with pallets allowed.");
                UserConstraints relaxed = UserConstraints.newBuilder()
                    .withAllowedContainerTypes(java.util.List.of(
                        ContainerType.STANDARD_CRATE,
                        ContainerType.STANDARD_PALLET,
                        ContainerType.OVERSIZE_PALLET
                    ))
                    .build();
                finalPlan = Packer.pack(artsToPack, relaxed, provider);
            }
        } else {
            finalPlan = Packer.pack(artsToPack, constraints, provider);
        }

        // --- "OUT" PART ---
        System.out.println("\n--- Generating Response ViewModel... ---");
        Response response = new Response(finalPlan);
        return response.generateViewModel();
    }

    /**
     * The presentation layer logic. It consumes the pure data from the ViewModel
     * and decides exactly how to format and display it for the console.
     * @param viewModel The complete, read-only data package for the shipment.
     */
    private static void displayViewModel(ShipmentViewModel viewModel) {
        if (viewModel == null || viewModel.containers().isEmpty()) {
            System.out.println("No containers were used. The packing plan is empty.");
            return;
        }
    // The frontend is responsible for all formatting and display choices.
    System.out.println("--- Shipment Plan Summary ---");
    System.out.println("Total Estimated Cost: $" + String.format("%.2f", viewModel.totalCost()));
    System.out.println("Total Weight: " + String.format("%.2f", viewModel.totalWeight()) + " lbs");
    System.out.println("Total Containers: " + viewModel.totalContainers());
    System.out.println("Total Boxes: " + viewModel.totalBoxes());
    System.out.println("------------------------------------");

    // Gather all arts across the shipment so we can compute the requested work-order summary
    java.util.List<archdesign.response.ArtViewModel> allArts = new java.util.ArrayList<>();
        for (ContainerViewModel container : viewModel.containers()) {
            String containerDims = String.format("%dx%dx%d", container.length(), container.width(), container.currentHeight());
            System.out.println(
                "-> Container: " + container.id() +
                " (Type: " + container.type() + ")" +
                " | Dimensions: " + containerDims +
                " | Weight: " + String.format("%.2f", container.weight()) + " lbs"
            );

            for (BoxViewModel box : container.boxes()) {
                String boxDims = String.format("%dx%dx%d", box.length(), box.width(), box.currentHeight());
                System.out.println(
                    "   --> Box: " + box.id() +
                    " (Type: " + box.type() + ")" +
                    " | Dimensions: " + boxDims +
                    " | Weight: " + String.format("%.2f", box.weight()) + " lbs" +
                    " | Contains: " + box.arts().size() + " items"
                );

                // This section now displays all the detailed art info from the ArtViewModel.
                for (ArtViewModel art : box.arts()) {
                    // collect art for summary calculations
                    allArts.add(art);
                    System.out.println(
                        "       - Art: " + art.id() +
                        " | Material: " + art.material() +
                        " | Dims: " + art.width() + "x" + art.height() +
                        " | Weight: " + String.format("%.2f", art.weight()) + " lbs"
                    );
                }
            }
            System.out.println(); // Add a blank line for readability
        }

        // --- Work Order Summary ---
        // Compute totals and group oversized pieces
        int totalPieces = allArts.size();
        double totalArtworkWeight = 0.0;
        java.util.Map<String, OversizeGroup> oversizeMap = new java.util.LinkedHashMap<>();
        int standardPieces = 0;

        for (archdesign.response.ArtViewModel art : allArts) {
            totalArtworkWeight += art.weight();
            boolean isOversized = art.width() > 44 || art.height() > 44;
            if (!isOversized) {
                standardPieces++;
            } else {
                String dims = art.height() + "\" x " + art.width() + "\""; // e.g. 46" x 34"
                OversizeGroup g = oversizeMap.get(dims);
                if (g == null) {
                    g = new OversizeGroup(art.height(), art.width());
                    oversizeMap.put(dims, g);
                }
                g.add(art.weight());
            }
        }

        double finalShipmentWeight = viewModel.totalWeight();
        double totalPackagingWeight = finalShipmentWeight - totalArtworkWeight;

        System.out.println("Work Order Summary:");
        System.out.println("- Total Pieces: " + totalPieces);
        System.out.println("- Standard Size Pieces: " + standardPieces);
        System.out.println("- Oversized Pieces: " + oversizeMap.values().stream().mapToInt(g->g.qty).sum());
        if (!oversizeMap.isEmpty()) {
            for (java.util.Map.Entry<String, OversizeGroup> entry : oversizeMap.entrySet()) {
                OversizeGroup g = entry.getValue();
                System.out.println("   * " + entry.getKey() + " (Qty: " + g.qty + ") = " + String.format("%.0f", g.totalWeight) + " lbs");
            }
        }

        System.out.println();
        System.out.println("Total Artwork Weight: " + String.format("%.0f", totalArtworkWeight) + " lbs");
        System.out.println("Total Packaging Weight: " + String.format("%.0f", totalPackagingWeight) + " lbs");
        System.out.println("Final Shipment Weight: " + String.format("%.0f", finalShipmentWeight) + " lbs");
    }

    /**
     * Writes the packing results to a JSON file.
     * @param viewModel The ShipmentViewModel containing all packing data.
     * @param outputFilePath The path to the output JSON file.
     */
    private static void writeJsonOutput(ShipmentViewModel viewModel, String outputFilePath) {
        if (viewModel == null || viewModel.containers().isEmpty()) {
            System.err.println("Warning: No packing data to write to JSON file.");
            return;
        }

        // Collect all arts across the shipment
        List<ArtViewModel> allArts = new ArrayList<>();
        for (ContainerViewModel container : viewModel.containers()) {
            for (BoxViewModel box : container.boxes()) {
                allArts.addAll(box.arts());
            }
        }

        // Create JSON output object
        JsonOutputSchema jsonOutput = new JsonOutputSchema();

        // Calculate total pieces and standard vs oversized
        int totalPieces = allArts.size();
        int standardSizePieces = 0;
        java.util.Map<String, OversizeGroup> oversizeMap = new java.util.LinkedHashMap<>();

        for (ArtViewModel art : allArts) {
            boolean isOversized = art.width() > 44 || art.height() > 44;
            if (!isOversized) {
                standardSizePieces++;
            } else {
                String key = art.height() + "x" + art.width();
                OversizeGroup g = oversizeMap.get(key);
                if (g == null) {
                    g = new OversizeGroup(art.height(), art.width());
                    oversizeMap.put(key, g);
                }
                g.add(art.weight());
            }
        }

        // Convert oversized map to list
        List<JsonOutputSchema.OversizedPiece> oversizedPieces = new ArrayList<>();
        for (OversizeGroup g : oversizeMap.values()) {
            oversizedPieces.add(new JsonOutputSchema.OversizedPiece(g.h, g.w, g.qty));
        }

        // Count boxes by type
        int standardBoxCount = 0;
        int largeBoxCount = 0;
        int customPieceCount = 0; // UPS boxes and CRATE boxes

        for (ContainerViewModel container : viewModel.containers()) {
            for (BoxViewModel box : container.boxes()) {
                String boxType = box.type();
                if (boxType.equals("STANDARD")) {
                    standardBoxCount++;
                } else if (boxType.equals("LARGE")) {
                    largeBoxCount++;
                } else {
                    // UPS_SMALL, UPS_LARGE, or CRATE
                    customPieceCount++;
                }
            }
        }

        // Count containers by type
        int standardPalletCount = 0;
        int oversizedPalletCount = 0;
        int crateCount = 0;

        for (ContainerViewModel container : viewModel.containers()) {
            String containerType = container.type();
            if (containerType.equals("STANDARD_PALLET") || containerType.equals("GLASS_PALLET")) {
                standardPalletCount++;
            } else if (containerType.equals("OVERSIZE_PALLET")) {
                oversizedPalletCount++;
            } else if (containerType.equals("STANDARD_CRATE")) {
                crateCount++;
            }
        }

        // Calculate weights
        double totalArtworkWeight = allArts.stream().mapToDouble(ArtViewModel::weight).sum();
        double finalShipmentWeight = viewModel.totalWeight();
        double totalPackagingWeight = finalShipmentWeight - totalArtworkWeight;

        // Populate JSON output
        jsonOutput.setTotalPieces(totalPieces);
        jsonOutput.setStandardSizePieces(standardSizePieces);
        jsonOutput.setOversizedPieces(oversizedPieces);
        jsonOutput.setStandardBoxCount(standardBoxCount);
        jsonOutput.setLargeBoxCount(largeBoxCount);
        jsonOutput.setCustomPieceCount(customPieceCount);
        jsonOutput.setStandardPalletCount(standardPalletCount);
        jsonOutput.setOversizedPalletCount(oversizedPalletCount);
        jsonOutput.setCrateCount(crateCount);
        jsonOutput.setTotalArtworkWeight(totalArtworkWeight);
        jsonOutput.setTotalPackagingWeight(totalPackagingWeight);
        jsonOutput.setFinalShipmentWeight(finalShipmentWeight);

        // Write to file using Gson
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonOutput, writer);
            System.out.println("Successfully wrote JSON output to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error writing JSON output file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // small helper to group oversize pieces
    private static class OversizeGroup {
        final int w;
        final int h;
        int qty = 0;
        double totalWeight = 0.0;
        OversizeGroup(int w, int h) { this.w = w; this.h = h; }
        void add(double weight) { qty++; totalWeight += weight; }
    }
}