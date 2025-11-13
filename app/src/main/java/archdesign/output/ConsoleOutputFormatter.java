package archdesign.output;

import archdesign.response.ShipmentViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.BoxViewModel;
import archdesign.response.ArtViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles formatting and displaying packing results to the console.
 * This class is responsible for all presentation logic for the command-line interface.
 */
public class ConsoleOutputFormatter {

    /**
     * Display the complete shipment view model to the console.
     * @param viewModel The complete, read-only data package for the shipment.
     */
    public void display(ShipmentViewModel viewModel) {
        if (viewModel == null || (viewModel.containers().isEmpty() && viewModel.unpackedArts().isEmpty())) {
            System.out.println("No containers were used. The packing plan is empty.");
            return;
        }

        displaySummary(viewModel);
        displayContainers(viewModel);
        displayWorkOrderSummary(viewModel);
        displayUnpackedArts(viewModel);
    }

    /**
     * Display the shipment summary section.
     * @param viewModel The shipment view model.
     */
    private void displaySummary(ShipmentViewModel viewModel) {
        System.out.println("--- Shipment Plan Summary ---");
        System.out.println("Total Estimated Cost: $" + String.format("%.2f", viewModel.totalCost()));
        System.out.println("Total Weight: " + String.format("%.2f", viewModel.totalWeight()) + " lbs");
        System.out.println("Total Containers: " + viewModel.totalContainers());
        System.out.println("Total Boxes: " + viewModel.totalBoxes());
        System.out.println("------------------------------------");
    }

    /**
     * Display all containers, boxes, and arts with their details.
     * @param viewModel The shipment view model.
     */
    private void displayContainers(ShipmentViewModel viewModel) {
        for (ContainerViewModel container : viewModel.containers()) {
            String containerDims = String.format("%dx%dx%d", 
                container.length(), container.width(), container.currentHeight());
            System.out.println(
                "-> Container: " + container.id() +
                " (Type: " + container.type() + ")" +
                " | Dimensions: " + containerDims +
                " | Weight: " + String.format("%.2f", container.weight()) + " lbs"
            );

            for (BoxViewModel box : container.boxes()) {
                String boxDims = String.format("%dx%dx%d", 
                    box.length(), box.width(), box.currentHeight());
                System.out.println(
                    "   --> Box: " + box.id() +
                    " (Type: " + box.type() + ")" +
                    " | Dimensions: " + boxDims +
                    " | Weight: " + String.format("%.2f", box.weight()) + " lbs" +
                    " | Contains: " + box.arts().size() + " items"
                );

                for (ArtViewModel art : box.arts()) {
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
    }

    /**
     * Display the work order summary including piece counts and weights.
     * @param viewModel The shipment view model.
     */
    private void displayWorkOrderSummary(ShipmentViewModel viewModel) {
        // Gather all arts across the shipment
        List<ArtViewModel> allArts = new ArrayList<>();
        for (ContainerViewModel container : viewModel.containers()) {
            for (BoxViewModel box : container.boxes()) {
                allArts.addAll(box.arts());
            }
        }

        // Compute totals and group oversized pieces
        int totalPieces = allArts.size();
        double totalArtworkWeight = 0.0;
        Map<String, OversizeGroup> oversizeMap = new LinkedHashMap<>();
        int standardPieces = 0;

        for (ArtViewModel art : allArts) {
            totalArtworkWeight += art.weight();
            boolean isOversized = art.width() > 44 || art.height() > 44;
            if (!isOversized) {
                standardPieces++;
            } else {
                String dims = art.height() + "\" x " + art.width() + "\"";
                OversizeGroup g = oversizeMap.get(dims);
                if (g == null) {
                    g = new OversizeGroup(art.height(), art.width());
                    oversizeMap.put(dims, g);
                }
                g.add(art.weight());
            }
        }

        // Count box types (excluding crate boxes as they are virtual)
        int standardBoxCount = 0;
        int largeBoxCount = 0;
        
        for (ContainerViewModel container : viewModel.containers()) {
            for (BoxViewModel box : container.boxes()) {
                String boxType = box.type();
                if ("STANDARD".equals(boxType)) {
                    standardBoxCount++;
                } else if ("LARGE".equals(boxType)) {
                    largeBoxCount++;
                }
                // CRATE boxes are not counted as they are virtual
            }
        }
        
        // Count container types
        int standardPalletCount = 0;
        int oversizedPalletCount = 0;
        int crateContainerCount = 0;
        
        for (ContainerViewModel container : viewModel.containers()) {
            String containerType = container.type();
            if ("STANDARD_PALLET".equals(containerType) || "GLASS_PALLET".equals(containerType)) {
                standardPalletCount++;
            } else if ("OVERSIZE_PALLET".equals(containerType)) {
                oversizedPalletCount++;
            } else if ("STANDARD_CRATE".equals(containerType)) {
                crateContainerCount++;
            }
        }

        // Get unpacked arts (custom pieces)
        int customPieceCount = viewModel.unpackedArts().size();
        
        // Add unpacked arts weight to total artwork weight
        for (ArtViewModel unpackedArt : viewModel.unpackedArts()) {
            totalArtworkWeight += unpackedArt.weight();
        }

        double finalShipmentWeight = viewModel.totalWeight();
        double totalPackagingWeight = finalShipmentWeight - totalArtworkWeight;

        System.out.println("\n=== SHIPMENT SUMMARY ===");
        System.out.println();
        
        // Piece counts
        System.out.println("ARTWORK PIECES:");
        System.out.println("  Total Pieces: " + (totalPieces + customPieceCount));
        System.out.println("  Standard Size Pieces: " + standardPieces);
        System.out.println("  Oversized Pieces: " + 
            oversizeMap.values().stream().mapToInt(g -> g.qty).sum());
        
        if (!oversizeMap.isEmpty()) {
            for (Map.Entry<String, OversizeGroup> entry : oversizeMap.entrySet()) {
                OversizeGroup g = entry.getValue();
                System.out.println("    - " + entry.getKey() + 
                    " (Quantity: " + g.qty + ", Weight: " + 
                    String.format("%.0f", g.totalWeight) + " lbs)");
            }
        }
        System.out.println();
        
        // Box counts
        System.out.println("PACKAGING:");
        System.out.println("  Standard Box Count: " + standardBoxCount);
        System.out.println("  Large Box Count: " + largeBoxCount);
        System.out.println("  Custom Piece Count: " + customPieceCount);
        System.out.println();
        
        // Container counts
        System.out.println("CONTAINERS:");
        System.out.println("  Standard Pallet Count: " + standardPalletCount);
        System.out.println("  Oversized Pallet Count: " + oversizedPalletCount);
        System.out.println("  Crate Container Count: " + crateContainerCount);
        System.out.println();
        
        // Weights
        System.out.println("WEIGHT BREAKDOWN:");
        System.out.println("  Total Artwork Weight: " + 
            String.format("%.0f", totalArtworkWeight) + " lbs");
        System.out.println("  Total Packaging Weight: " + 
            String.format("%.0f", totalPackagingWeight) + " lbs");
        System.out.println("  Final Shipment Weight: " + 
            String.format("%.0f", finalShipmentWeight) + " lbs");
        System.out.println();
        System.out.println("========================");
    }

    /**
     * Display information about unpacked arts that could not be fitted into containers.
     * @param viewModel The shipment view model.
     */
    private void displayUnpackedArts(ShipmentViewModel viewModel) {
        List<ArtViewModel> unpackedArts = viewModel.unpackedArts();
        
        if (unpackedArts == null || unpackedArts.isEmpty()) {
            return; // No unpacked arts to display
        }
        
        System.out.println("\n!!! WARNING: UNPACKED ITEMS !!!");
        System.out.println("The following " + unpackedArts.size() + " art piece(s) could not be packed:");
        System.out.println("These items are too large for available boxes/containers and are counted as custom pieces.");
        System.out.println();
        
        for (ArtViewModel art : unpackedArts) {
            System.out.println("   - Art: " + art.id() +
                " | Material: " + art.material() +
                " | Dims: " + art.width() + "x" + art.height() +
                " | Weight: " + String.format("%.2f", art.weight()) + " lbs");
        }
        
        double unpackedWeight = unpackedArts.stream().mapToDouble(ArtViewModel::weight).sum();
        System.out.println();
        System.out.println("Total Unpacked Artwork Weight: " + String.format("%.0f", unpackedWeight) + " lbs");
        System.out.println("These items are included in the JSON output as 'custom_piece_count' and 'total_artwork_weight'.");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
    }

    /**
     * Helper class to group oversize pieces by dimensions.
     */
    private static class OversizeGroup {
        final double w;
        final double h;
        int qty = 0;
        double totalWeight = 0.0;

        OversizeGroup(double h, double w) {
            this.h = h;
            this.w = w;
        }

        void add(double weight) {
            qty++;
            totalWeight += weight;
        }
    }
}
