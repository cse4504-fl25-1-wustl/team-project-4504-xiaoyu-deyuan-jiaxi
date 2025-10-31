package archdesign.entities;

import archdesign.entities.enums.Material;

public class Art {
    private final String id;
    private final double weight;
    private final double height;
    private final double width;
    private final Material material;

    public Art(String id, double height, double width, int thickness, Material material) {
        this.id = id;
        this.height = height;
        this.width = width;
        this.material = material;
        this.weight = Math.ceil(height * width * (double)material.getWeight());
    }

    public String getId() {
        return id;
    }

    public double getWeight() {
        return weight;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public Material getMaterial() {
        return material;
    }
}