package entities;

public class Art {
    private String id;
    private float weight;
    private int height;
    private int width;
    private int length;
    private boolean inBox;

    public Art(String id, float weight, int height, int width, int length, boolean inBox) {
        this.id = id;
        this.weight = weight;
        this.height = height;
        this.width = width;
        this.length = length;
        this.inBox = inBox;
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

    
    public boolean fitStandardBox() {
        //check the rules
        return true;
    }

    public boolean fitLargeBox() {
        //check the rules
        return true;
    }
    public boolean fitCrate() {
        //check the rules
        return true;
    }

    public boolean fitHeight() {
        //check the rules
        return true;
    }
    public boolean specialHandle() {
        //check the rules
        return true;
    }   

    public boolean isInBox() {
        //check the rules
        return true;
    }   

}
