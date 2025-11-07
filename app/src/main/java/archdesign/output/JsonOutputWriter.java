package archdesign.output;

import archdesign.response.ShipmentViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.BoxViewModel;
import archdesign.response.ArtViewModel;
import archdesign.response.JsonOutputSchema;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles writing packing results to JSON format.
 * This class is responsible for converting the shipment view model to JSON output.
 */
public class JsonOutputWriter {

    /**
     * Write the packing results to a JSON file.
     * @param viewModel The ShipmentViewModel containing all packing data.
     * @param outputFilePath The path to the output JSON file.
     */
    public void write(ShipmentViewModel viewModel, String outputFilePath) {
        if (viewModel == null) {
            System.err.println("Warning: No packing data to write to JSON file.");
            return;
        }

        // Even if containers are empty, we may have unpacked arts (custom pieces)
        // So we should still write the JSON file
        JsonOutputSchema jsonOutput = buildJsonOutput(viewModel);
        writeToFile(jsonOutput, outputFilePath);
    }

    /**
     * Build the JSON output schema from the view model.
     * @param viewModel The shipment view model.
     * @return The populated JsonOutputSchema object.
     */
    private JsonOutputSchema buildJsonOutput(ShipmentViewModel viewModel) {
        // Collect all arts across the shipment
        List<ArtViewModel> allArts = collectAllArts(viewModel);
        
        // Get unpacked arts (custom pieces - oversized items packed individually)
        List<ArtViewModel> unpackedArts = viewModel.unpackedArts();

        // Create JSON output object
        JsonOutputSchema jsonOutput = new JsonOutputSchema();

        // Calculate pieces and oversized information (including unpacked arts)
        // Combine all arts and unpacked arts for accurate total count
        List<ArtViewModel> allArtsIncludingUnpacked = new ArrayList<>(allArts);
        allArtsIncludingUnpacked.addAll(unpackedArts);
        
        PieceStatistics pieceStats = calculatePieceStatistics(allArtsIncludingUnpacked);
        jsonOutput.setTotalPieces(pieceStats.totalPieces);
        jsonOutput.setStandardSizePieces(pieceStats.standardSizePieces);
        jsonOutput.setOversizedPieces(pieceStats.oversizedPieces);

        // Count boxes by type
        BoxCounts boxCounts = countBoxes(viewModel);
        jsonOutput.setStandardBoxCount(boxCounts.standardBoxCount);
        jsonOutput.setLargeBoxCount(boxCounts.largeBoxCount);
        // custom_piece_count is ONLY unpacked arts (not CRATE boxes)
        jsonOutput.setCustomPieceCount(unpackedArts.size());

        // Count containers by type
        ContainerCounts containerCounts = countContainers(viewModel);
        jsonOutput.setStandardPalletCount(containerCounts.standardPalletCount);
        jsonOutput.setOversizedPalletCount(containerCounts.oversizedPalletCount);
        jsonOutput.setCrateCount(containerCounts.crateCount);

        // Calculate weights including unpacked arts
        WeightStatistics weights = calculateWeights(allArts, unpackedArts, viewModel);
        jsonOutput.setTotalArtworkWeight(weights.totalArtworkWeight);
        jsonOutput.setTotalPackagingWeight(weights.totalPackagingWeight);
        jsonOutput.setFinalShipmentWeight(weights.finalShipmentWeight);

        return jsonOutput;
    }

    /**
     * Collect all arts from all containers and boxes in the shipment.
     * @param viewModel The shipment view model.
     * @return List of all ArtViewModel objects.
     */
    private List<ArtViewModel> collectAllArts(ShipmentViewModel viewModel) {
        List<ArtViewModel> allArts = new ArrayList<>();
        for (ContainerViewModel container : viewModel.containers()) {
            for (BoxViewModel box : container.boxes()) {
                allArts.addAll(box.arts());
            }
        }
        return allArts;
    }

    /**
     * Calculate piece statistics including total, standard, and oversized pieces.
     * @param allArts List of all arts in the shipment.
     * @return PieceStatistics object containing counts and oversized piece list.
     */
    private PieceStatistics calculatePieceStatistics(List<ArtViewModel> allArts) {
        int totalPieces = allArts.size();
        int standardSizePieces = 0;
        Map<String, OversizeGroup> oversizeMap = new LinkedHashMap<>();

        for (ArtViewModel art : allArts) {
            boolean isOversized = art.width() > 44 || art.height() > 44;
            if (!isOversized) {
                standardSizePieces++;
            } else {
                // Ensure side1 is the longer side
                double longerSide = Math.max(art.height(), art.width());
                double shorterSide = Math.min(art.height(), art.width());
                String key = longerSide + "x" + shorterSide;
                OversizeGroup g = oversizeMap.get(key);
                if (g == null) {
                    g = new OversizeGroup(longerSide, shorterSide);
                    oversizeMap.put(key, g);
                }
                g.add(art.weight());
            }
        }

        // Convert oversized map to list
        // side1 is stored in h (first dimension), side2 in w (second dimension)
        List<JsonOutputSchema.OversizedPiece> oversizedPieces = new ArrayList<>();
        for (OversizeGroup g : oversizeMap.values()) {
            oversizedPieces.add(new JsonOutputSchema.OversizedPiece(g.side1, g.side2, g.qty));
        }

        return new PieceStatistics(totalPieces, standardSizePieces, oversizedPieces);
    }

    /**
     * Count boxes by type in the shipment.
     * @param viewModel The shipment view model.
     * @return BoxCounts object containing counts for each box type.
     */
    private BoxCounts countBoxes(ShipmentViewModel viewModel) {
        int standardBoxCount = 0;
        int largeBoxCount = 0;

        for (ContainerViewModel container : viewModel.containers()) {
            for (BoxViewModel box : container.boxes()) {
                String boxType = box.type();
                if (boxType.equals("STANDARD")) {
                    standardBoxCount++;
                } else if (boxType.equals("LARGE")) {
                    largeBoxCount++;
                }
                // CRATE boxes are not counted in standard/large box counts
            }
        }

        return new BoxCounts(standardBoxCount, largeBoxCount);
    }

    /**
     * Count containers by type in the shipment.
     * @param viewModel The shipment view model.
     * @return ContainerCounts object containing counts for each container type.
     */
    private ContainerCounts countContainers(ShipmentViewModel viewModel) {
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

        return new ContainerCounts(standardPalletCount, oversizedPalletCount, crateCount);
    }

    /**
     * Calculate weight statistics for the shipment.
     * @param allArts List of all packed arts in the shipment.
     * @param unpackedArts List of unpacked arts.
     * @param viewModel The shipment view model.
     * @return WeightStatistics object containing artwork, packaging, and final weights.
     */
    private WeightStatistics calculateWeights(List<ArtViewModel> allArts, List<ArtViewModel> unpackedArts, ShipmentViewModel viewModel) {
        // Calculate total artwork weight including both packed and unpacked arts
        double packedArtworkWeight = allArts.stream().mapToDouble(ArtViewModel::weight).sum();
        double unpackedArtworkWeight = unpackedArts.stream().mapToDouble(ArtViewModel::weight).sum();
        double totalArtworkWeight = packedArtworkWeight + unpackedArtworkWeight;
        
        // The final shipment weight from viewModel only includes packed items
        double finalShipmentWeight = viewModel.totalWeight();
        double totalPackagingWeight = finalShipmentWeight - packedArtworkWeight;

        return new WeightStatistics(totalArtworkWeight, totalPackagingWeight, finalShipmentWeight);
    }

    /**
     * Write the JSON output to a file.
     * @param jsonOutput The populated JsonOutputSchema object.
     * @param outputFilePath The path to the output file.
     */
    private void writeToFile(JsonOutputSchema jsonOutput, String outputFilePath) {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonOutput, writer);
            System.out.println("Successfully wrote JSON output to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error writing JSON output file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper class to group oversize pieces by dimensions.
     */
    private static class OversizeGroup {
        final double side1; // Longer side
        final double side2; // Shorter side
        int qty = 0;
        double totalWeight = 0.0;

        OversizeGroup(double side1, double side2) {
            this.side1 = side1;
            this.side2 = side2;
        }

        void add(double weight) {
            qty++;
            totalWeight += weight;
        }
    }

    /**
     * Data class for piece statistics.
     */
    private static class PieceStatistics {
        final int totalPieces;
        final int standardSizePieces;
        final List<JsonOutputSchema.OversizedPiece> oversizedPieces;

        PieceStatistics(int totalPieces, int standardSizePieces, 
                       List<JsonOutputSchema.OversizedPiece> oversizedPieces) {
            this.totalPieces = totalPieces;
            this.standardSizePieces = standardSizePieces;
            this.oversizedPieces = oversizedPieces;
        }
    }

    /**
     * Data class for box counts.
     */
    private static class BoxCounts {
        final int standardBoxCount;
        final int largeBoxCount;

        BoxCounts(int standardBoxCount, int largeBoxCount) {
            this.standardBoxCount = standardBoxCount;
            this.largeBoxCount = largeBoxCount;
        }
    }

    /**
     * Data class for container counts.
     */
    private static class ContainerCounts {
        final int standardPalletCount;
        final int oversizedPalletCount;
        final int crateCount;

        ContainerCounts(int standardPalletCount, int oversizedPalletCount, int crateCount) {
            this.standardPalletCount = standardPalletCount;
            this.oversizedPalletCount = oversizedPalletCount;
            this.crateCount = crateCount;
        }
    }

    /**
     * Data class for weight statistics.
     */
    private static class WeightStatistics {
        final double totalArtworkWeight;
        final double totalPackagingWeight;
        final double finalShipmentWeight;

        WeightStatistics(double totalArtworkWeight, double totalPackagingWeight, 
                        double finalShipmentWeight) {
            this.totalArtworkWeight = totalArtworkWeight;
            this.totalPackagingWeight = totalPackagingWeight;
            this.finalShipmentWeight = finalShipmentWeight;
        }
    }
}
