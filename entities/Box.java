package entities;

import java.util.Iterator;
import java.util.List;
import interactor.PackingRules;

public class Box {
    private String id;
    private int width;
     private int height;
    private int length;
    private float weight;
    private boolean inContainer;
    private boolean isCrateBox;
    private int minNumPerBox;
    private List<Art> artsInBox;
    private PackingRules.BoxType boxType;

    public Box(String id, Art initialArt)
    {
        this.id = id;
        this.artsInBox = new java.util.ArrayList<>();
        this.inContainer = false;
        this.boxType = initialArt.getBoxType();
        switch (this.boxType) {
            case STANDARD:
                this.width = PackingRules.STANDARD_BOX_WIDTH;
                this.height = PackingRules.STANDARD_BOX_HEIGHT;
                this.length = PackingRules.STANDARD_BOX_LENGTH;
                this.isCrateBox = false;
                this.minNumPerBox = initialArt.getMaterial().getPiecePerBox();
                break;
            case LARGE:
                this.width = PackingRules.LARGE_BOX_WIDTH;
                this.height = PackingRules.LARGE_BOX_HEIGHT;
                this.length = PackingRules.LARGE_BOX_LENGTH;
                this.isCrateBox = false;
                this.minNumPerBox = initialArt.getMaterial().getPiecePerBox();
                break;
                case CRATE:
                this.width = PackingRules.CRATE_WIDTH;
                this.height = PackingRules.CRATE_HEIGHT;
                this.length = PackingRules.CRATE_LENGTH;
                this.isCrateBox = true;
                this.minNumPerBox = initialArt.getMaterial().getPiecePerCrate();
                break;
            case CRATE_LARGE:
                this.width = PackingRules.CRATE_WIDTH;
                this.height = PackingRules.CRATE_HEIGHT;
                this.length = PackingRules.CRATE_LENGTH;
                this.isCrateBox = true;
                this.minNumPerBox = initialArt.getMaterial().getPiecePerCrateLarge();
                break;
            case UNBOXABLE:
                throw new IllegalArgumentException("UNBOXABLE" + initialArt.getId());
        }
        this.artsInBox.add(initialArt);
        initialArt.setInBox(true);
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

    public boolean isCrateBox() {
        return isCrateBox;
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
        if (artsInBox.isEmpty() || minNumPerBox == 0) {
            return false;
        }
        return artsInBox.size() >= minNumPerBox;
    }
    
    public boolean tryAddArt(Art art) {
        if (isFull() || art.isInBox())
        {
            return false;
        }
        if (art.getBoxType() == this.boxType) {
            artsInBox.add(art);
            art.setInBox(true);
            return true;
        }
        return false;
    }
    
    //will be used in the future if we want to remove an art from a box
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

    public void setInContainer(boolean status)
    {
        inContainer = status;
    }
    
    public boolean isInContainer() {
        return inContainer;
    }
}