package archdesign;

import archdesign.entities.Art;
import archdesign.entities.enums.ShippingProvider;
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

import java.util.List;

/**
 * A simple command-line application to run the entire packing process.
 * This class acts as the final presentation layer (the CLI frontend).
 */
public class Main {

    /**
     * The main entry point for the Java application.
     * @param args Command line arguments, expecting the path to a CSV file.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Please provide the path to the CSV file as an argument.");
            System.err.println("Usage: java Main <path/to/your/file.csv>");
            return;
        }
        String filePath = args[0];
        
        System.out.println("--- Starting Packer Process for file: " + filePath + " ---");

        ShipmentViewModel viewModel = processFile(filePath);

        // display on console
        System.out.println("\n--- Displaying Packing Plan ---");
        displayViewModel(viewModel);
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
        UserConstraints constraints = new UserConstraints();
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;
        System.out.println("\n--- Running Packer Algorithm... ---");
        PackingPlan finalPlan = Packer.pack(artsToPack, constraints, provider);

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
    var allArts = new java.util.ArrayList<archdesign.response.ArtViewModel>();
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
                String dims = art.width() + "\" x " + art.height() + "\""; // e.g. 46" x 34"
                OversizeGroup g = oversizeMap.get(dims);
                if (g == null) {
                    g = new OversizeGroup(art.width(), art.height());
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
            for (var entry : oversizeMap.entrySet()) {
                OversizeGroup g = entry.getValue();
                System.out.println("   * " + entry.getKey() + " (Qty: " + g.qty + ") = " + String.format("%.0f", g.totalWeight) + " lbs");
            }
        }

        System.out.println();
        System.out.println("Total Artwork Weight: " + String.format("%.0f", totalArtworkWeight) + " lbs");
        System.out.println("Total Packaging Weight: " + String.format("%.0f", totalPackagingWeight) + " lbs");
        System.out.println("Final Shipment Weight: " + String.format("%.0f", finalShipmentWeight) + " lbs");
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