package entities;

public class Art {
    private String id;
    private float weight;
    private int height;
    private int width;
    private int length;
    private boolean inBox;
    private String material;

    public Art(String id, float weight, int height, int width, int length, boolean inBox) {
        this.id = id;
        this.weight = weight;
        this.height = height;
        this.width = width;
        this.length = length;
        this.inBox = inBox;
        this.material = "Glass/Acrylic Framed";
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
    
    public String getMaterial() {
        return material;
    }

    
    public boolean fitStandardBox() {
        //check the rules
        return (width <= 36 && height <= 36 && length <= 36);
    }

    public boolean fitLargeBox() {
        //check the rules
        return (width <= 43 && height <= 43 && length <= 43);
    }
    public boolean fitCrate() {
        //check the rules
        return (width <= 46 && height <= 46 && length <= 46);
    }

    public boolean fitHeight() {
        //check the rules
        return (width <= 84 && height <= 84 && length <= 84);
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
