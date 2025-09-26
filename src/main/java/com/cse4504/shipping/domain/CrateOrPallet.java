package com.cse4504.shipping.domain;

import java.util.ArrayList;
import java.util.List;


public class CrateOrPallet {
    public enum Type {CRATE, PALLET}

    private String id;
    private List<Box> boxes;
    private double totalWeight;
    private double height;
    private Type type;
    private double maxWeight;
    private double maxHeight;

    public CrateOrPallet(String id, Type type, double maxWeight, double maxHeight){
        this.id=id;
        this.boxes = new ArrayList<>();
        this.totalWeight=0.0;
        this.type = type;
        this.maxWeight=maxWeight;
        this.maxHeight=maxHeight;

    }

    public boolean canAddBox(Box box){
        double newWeight = totalWeight+box.getTotalWeight();

        return newWeight <= maxWeight;
    }

    public void addBox(Box box){
        if (canAddBox(box)){
            boxes.add(box);
            totalWeight+=box.getTotalWeight();
        }
    }

    public String getId(){
        return id;
    }

    public List<Box> getBoxs(){
        return new ArrayList<>(boxes);
    }
    public double getTotalWeight(){
        return totalWeight;
    }
    public double getHeight(){
        return height;
    }
    public Type getType(){
        return type;
    }
    public double getMaxWeight(){
        return maxWeight;
    }
    public double getMaxHeight(){
        return maxHeight;
    }

    public void setHeight(double height){
        this.height=height;
    }
}
