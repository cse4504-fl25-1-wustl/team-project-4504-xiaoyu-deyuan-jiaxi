package com.cse4504.shipping.domain;

import java.util.ArrayList;
import java.util.List;


public class Box {
    private String id;
    private List<Item> items;
    private double totalWeight;
    private double maxWeight;
    private double maxVolume;
    
    public Box(String id, double maxWeight, double maxVolume){
        this.id = id;
        this.items = new ArrayList<>();
        this.totalWeight = 0.0;
        this.maxWeight=maxWeight;
        this.maxVolume=maxVolume;
    }

    public boolean canAddItem(Item item){
        double newWeight = totalWeight+item.getWeight();
        double newVolume = items.stream().mapToDouble(Item::getVolume).sum() + item.getVolume();
        
        return newWeight <=maxWeight && newVolume <=maxVolume;
    }

    public void addItem(Item item){
        if(canAddItem(item)){
            items.add(item);
            totalWeight+=item.getWeight();
        }
    }

    public String getId(){
        return id;
    }

    public List<Item> getItems(){
        return new ArrayList<>(items);
    }

    public double getTotalWeight(){
        return totalWeight;
    }

    public double getMaxWeight(){
        return maxWeight;
    }

    public double getMaxVolume(){
        return maxVolume;
    }
        
}
