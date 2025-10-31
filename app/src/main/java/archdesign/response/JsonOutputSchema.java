package archdesign.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Schema class for JSON output format.
 * This class defines the structure of the JSON output file.
 */
public class JsonOutputSchema {
    
    @SerializedName("total_pieces")
    private int totalPieces;
    
    @SerializedName("standard_size_pieces")
    private int standardSizePieces;
    
    @SerializedName("oversized_pieces")
    private List<OversizedPiece> oversizedPieces;
    
    @SerializedName("standard_box_count")
    private int standardBoxCount;
    
    @SerializedName("large_box_count")
    private int largeBoxCount;
    
    @SerializedName("custom_piece_count")
    private int customPieceCount;
    
    @SerializedName("standard_pallet_count")
    private int standardPalletCount;
    
    @SerializedName("oversized_pallet_count")
    private int oversizedPalletCount;
    
    @SerializedName("crate_count")
    private int crateCount;
    
    @SerializedName("total_artwork_weight")
    private double totalArtworkWeight;
    
    @SerializedName("total_packaging_weight")
    private double totalPackagingWeight;
    
    @SerializedName("final_shipment_weight")
    private double finalShipmentWeight;
    
    /**
     * Nested class for oversized piece information.
     */
    public static class OversizedPiece {
        @SerializedName("side1")
        private double side1;
        
        @SerializedName("side2")
        private double side2;
        
        @SerializedName("quantity")
        private int quantity;
        
        public OversizedPiece(double side1, double side2, int quantity) {
            this.side1 = side1;
            this.side2 = side2;
            this.quantity = quantity;
        }
        
        public double getSide1() { return side1; }
        public double getSide2() { return side2; }
        public int getQuantity() { return quantity; }
    }
    
    // Getters and setters
    public int getTotalPieces() { return totalPieces; }
    public void setTotalPieces(int totalPieces) { this.totalPieces = totalPieces; }
    
    public int getStandardSizePieces() { return standardSizePieces; }
    public void setStandardSizePieces(int standardSizePieces) { this.standardSizePieces = standardSizePieces; }
    
    public List<OversizedPiece> getOversizedPieces() { return oversizedPieces; }
    public void setOversizedPieces(List<OversizedPiece> oversizedPieces) { this.oversizedPieces = oversizedPieces; }
    
    public int getStandardBoxCount() { return standardBoxCount; }
    public void setStandardBoxCount(int standardBoxCount) { this.standardBoxCount = standardBoxCount; }
    
    public int getLargeBoxCount() { return largeBoxCount; }
    public void setLargeBoxCount(int largeBoxCount) { this.largeBoxCount = largeBoxCount; }
    
    public int getCustomPieceCount() { return customPieceCount; }
    public void setCustomPieceCount(int customPieceCount) { this.customPieceCount = customPieceCount; }
    
    public int getStandardPalletCount() { return standardPalletCount; }
    public void setStandardPalletCount(int standardPalletCount) { this.standardPalletCount = standardPalletCount; }
    
    public int getOversizedPalletCount() { return oversizedPalletCount; }
    public void setOversizedPalletCount(int oversizedPalletCount) { this.oversizedPalletCount = oversizedPalletCount; }
    
    public int getCrateCount() { return crateCount; }
    public void setCrateCount(int crateCount) { this.crateCount = crateCount; }
    
    public double getTotalArtworkWeight() { return totalArtworkWeight; }
    public void setTotalArtworkWeight(double totalArtworkWeight) { this.totalArtworkWeight = totalArtworkWeight; }
    
    public double getTotalPackagingWeight() { return totalPackagingWeight; }
    public void setTotalPackagingWeight(double totalPackagingWeight) { this.totalPackagingWeight = totalPackagingWeight; }
    
    public double getFinalShipmentWeight() { return finalShipmentWeight; }
    public void setFinalShipmentWeight(double finalShipmentWeight) { this.finalShipmentWeight = finalShipmentWeight; }
}
