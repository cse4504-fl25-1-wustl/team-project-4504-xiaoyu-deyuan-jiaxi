package entities;

import java.util.Iterator;
import java.util.List;

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
        for(Art i: artsInBox) {
            temp += i.getWeight();
        }
        return temp;
    }
    
    public List<Art> getArtsInBox() {
        return artsInBox;
    }
    
    public boolean isFull() {
        if (artsInBox.isEmpty()) {
            return false;
        }
        Material materialType = artsInBox.get(0).getMaterial();
        int maxPieces;
        switch (materialType) {
            case GLASS_ACRYLIC_FRAMED:
                maxPieces = 6;
                break;
            case GLASS_ACRYLIC_SUNRISE:
                maxPieces = 8;
                break;
            case CANVAS_FRAMED_GALLERY:
                maxPieces = 4;
                break;
            case ACOUSTIC_PANELS:
                maxPieces = 4;
                break;
            case CANVAS:
            case PAPER:
            case WOOD:
            case METAL:
                maxPieces = 4; 
                break;
            case MIRRORS:
            case SCULPTURE:
            case PHOTOGRAPH:
            case UNKNOWN:
            default:
                maxPieces = 0; // Special handling materials not in standard boxes
                break;
        }
        return artsInBox.size() >= maxPieces;
    }
    
    public boolean tryAddArt(Art art) {
        if (!isFull() && !art.isInBox()) {
            if (artsInBox.isEmpty() || artsInBox.get(0).getMaterial().equals(art.getMaterial())) {
                artsInBox.add(art);
                art.setInBox(true);
                return true;
            }
        }
        return false;
    }
    
    public boolean remove(Art art) {
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
    
    public boolean isInContainer() {
        return inContainer;
    }
}