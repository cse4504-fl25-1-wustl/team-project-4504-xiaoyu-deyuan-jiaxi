package com.cse4504.shipping.domain;

import java.util.ArrayList;
import java.util.List;

public class Shipment {
    private List<CrateOrPallet> crateOrPallets;
    private double totalWeight;

    public Shipment() {
        this.cratesOrPallets = new ArrayList<>();
        this.totalWeight = 0;
    }

    public void addContainer(CrateOrPallet crateOrPallet) {
        crateOrPallet.add(crateOrPallet);
        totalWeight += crateOrPallet.getTotalWeight();
    }

    public List<CrateOrPallet> getContainers() 
    { 
        return new ArrayList<>(cratesOrPallets) 
    }
    public double getTotalWeight() { 
        return totalWeight; 
    }
}
