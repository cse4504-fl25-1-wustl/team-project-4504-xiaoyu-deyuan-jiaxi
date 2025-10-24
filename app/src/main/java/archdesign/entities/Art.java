package archdesign.entities;

import archdesign.entities.enums.Material;

public class Art {
    private final String id;
    private final double weight;
    private final int height;
    private final int width;
    private final Material material;

    public Art(String id, int height, int width, int thickness, Material material) {
        this.id = id;
        this.height = height;
        this.width = width;
        this.material = material;
        this.weight = (int) Math.ceil(height * width * (double)material.getWeight());
    }

    public String getId() {
        return id;
    }

    public double getWeight() {
        return weight;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Material getMaterial() {
        return material;
    }
}