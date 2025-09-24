
package com.cse4504.shipping.usecases;

import com.cse4504.shipping.domain.Box;
import com.cse4504.shipping.domain.CrateOrPallet;
import com.cse4504.shipping.domain.Item;
import com.cse4504.shipping.domain.Shipment;

public class WeightCalculator {
    public double calculateItemWeight(Item item){
        return item.getWeight();
    }

    public double calculateBoxWeight(Box box){
        return box.getTotalWeight();
    }

    public double calculateCrateOrPalletWeight(CrateOrPallet crateOrPallet){
        return crateOrPallet.getTotalWeight();
    }

    public double calculateShipmentWeight(Shipment shipment){
        return shipment.getTotalWeight();
    }
}


