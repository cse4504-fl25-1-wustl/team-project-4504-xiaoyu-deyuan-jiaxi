package entities;


import java.util.List;

public class Box {
    private String id;
    private int width;
    private int height;
    private int length;
    private float weight;
    private List<Art> artsInBox;

    public Box(String id, int width, int height, int length, float weight, List<Art> artsInBox) {
    	this.id = id;
        this.width = width;
        this.height = height;
        this.length = length;
        this.weight = weight;
        this.artsInBox = artsInBox;
    }
    
    public String getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    public int getLength() {
        return length;
    }
    public float getWeight() {
        return weight;
    }
    public List<Art> getArtsInBox() {
        return artsInBox;
    }
    public boolean isFull() {
        //check the rules
        return true;
    }
    public boolean tryAddArt(Art art) {
        //try to add art into the box
        return true;
    }
    public boolean remove(Art art)
    {
        //remove a specific art from the box
        return true;
    }   
 

}
