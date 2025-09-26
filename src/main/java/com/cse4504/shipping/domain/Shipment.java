package com.cse4504.shipping.domain;

import java.util.ArrayList;
import java.util.List;

public class Shipment {
    private List<CrateOrPallet> cratesOrPallets;
    private double totalWeight;

    public Shipment() {
        this.cratesOrPallets = new ArrayList<>();
        this.totalWeight = 0;
    }

    public void addCrateOrPallet(CrateOrPallet crateOrPallet) {
        cratesOrPallets.add(crateOrPallet);
        totalWeight += crateOrPallet.getTotalWeight();
    }

    // Getters
    public List<CrateOrPallet> getCratesOrPallets() { return new ArrayList<>(cratesOrPallets); }
    public double getTotalWeight() { return totalWeight; }
}