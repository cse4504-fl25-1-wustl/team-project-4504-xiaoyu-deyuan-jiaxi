package entities;


import java.util.List;
import java.util.Iterator;

public class Box {
    private String id;
    private int width;
    private int height;
    private int length;
    private float weight;
    private boolean inContainer;
    private List<Art> artsInBox;

    public Box(String id, int width, int height, int length, float weight, List<Art> artsInBox) {
    	this.id = id;
        this.width = width;
        this.height = height;
        this.length = length;
        this.weight = weight;
        this.artsInBox = artsInBox;
        this.inContainer = false;
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
    	float temp = weight;
    	for(Art i: artsInBox)
    	{
    		temp += i.getWeight();
    	}
        return temp;
    }
    public List<Art> getArtsInBox() {
        return artsInBox;
    }
    public boolean isFull() {
        //check the rules
    	if (artsInBox.isEmpty()) {
            return false;
        }
        String materialType = artsInBox.get(0).getMaterial();
        int maxPieces;
        switch (materialType) {
            case "Glass/Acrylic Framed":
                maxPieces = 6;
                break;
            case "Glass/Acrylic (Sunrise)":
                maxPieces = 8;
                break;
            case "Canvas (Framed/Gallery)":
                maxPieces = 4; //????
                break;
            case "Acoustic Panels":
                maxPieces = 4;
                break;
            default:
                maxPieces = 0; // Mirrors and other types not in standard boxes
                break;
        }
        return artsInBox.size() >= maxPieces;
    }
    public boolean tryAddArt(Art art) {
        //try to add art into the box
    	if (!isFull() && !art.isInBox()) {
            if (artsInBox.isEmpty() || artsInBox.get(0).getMaterial().equals(art.getMaterial())) {
                artsInBox.add(art);
                art.setInBox(true);
                return true;
            }
        }
        return false;
    }
    
    public boolean remove(Art art)
    {
        //remove a specific art from the box
    	Iterator<Art> iterator = artsInBox.iterator();

        while (iterator.hasNext()) {
            Art currentArt = iterator.next();
            if (currentArt.equals(art)) {
                currentArt.setInBox(false);
                iterator.remove();
                return true;
            }
        }
        return false;
    }   

    public void setInContainer(boolean status) {
    	inContainer = status;
    }

}
