package com.cse4504.shipping.domain;

public class Item {
    private String id;
    private double weight;
    private double length;
    private double width;
    private double height;

    public Item(String id, double weight, double length, double width, double height){
        this.id=id;
        this.weight = weight;
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public String getId()
    {
        return id;
    }

    public double getWeight(){
        return weight;
    }

    public double getLength(){
        return length;
    }

    public double getWidth(){
        return width;
    }

    public double getHeight(){
        return height;
    }

    public double getVolume(){
        return length*width*height;
    }
}
