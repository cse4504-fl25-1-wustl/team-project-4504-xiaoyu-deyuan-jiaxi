package entities;

import interactor.PackingRules;

public class Art {
    private String id;
    private float weight;
    private int height;
    private int width;
    private int length;
    private boolean inBox;
    private Material material;

    public Art(String id, float weight, int height, int width, int length, boolean inBox, Material material) {
        this.id = id;
        this.weight = weight;
        this.height = height;
        this.width = width;
        this.length = length;
        this.inBox = inBox;
        this.material = material;
    }

    public String getId() {
        return id;
    }

    public float getWeight() {
        return weight;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public float getLength() {
        return length;
    }
    
    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    
    public boolean fitStandardBox() {
        //check the rules
        return (width <= PackingRules.STANDARD_BOX_LIMIT && height <= PackingRules.STANDARD_BOX_LIMIT && length <= PackingRules.STANDARD_BOX_LIMIT);
    }

    public boolean fitLargeBox() {
        //check the rules
        return (width <= PackingRules.LARGE_BOX_LIMIT && height <= PackingRules.LARGE_BOX_LIMIT && length <= PackingRules.LARGE_BOX_LIMIT);
    }
    public boolean fitCrate() {
        //check the rules
        return (width <= PackingRules.CRATE_LIMIT && height <= PackingRules.CRATE_LIMIT && length <= PackingRules.CRATE_LIMIT);
    }

    public boolean fitHeight() {
        //check the rules
        return (width <= PackingRules.MAX_RECOMMENDED_HEIGHT && height <= PackingRules.MAX_RECOMMENDED_HEIGHT && length <= PackingRules.MAX_RECOMMENDED_HEIGHT);
    }
    public boolean specialHandle() {
        //check the rules
        return false;
    }   

    public boolean isInBox() {
        //check the rules
        return inBox;
    }

    public void setInBox(boolean status) {
    	inBox = status;
    }


}
