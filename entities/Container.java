package entities;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import interactor.PackingRules;

public class Container {
    private String id;
    private int width;
    private int length;
    private int height;
    private float weight;
    private List<Box> boxInContainer;
    private int minNumPerContainer;
    private boolean isCrate;
    private PackingRules.BoxType boxType;

    public Container(String id, int width, int length, Box initialBox, boolean isCrate)
    {
        this.id = id;
        this.boxInContainer = new java.util.ArrayList<>();
        this.weight = 0;
        this.boxType = initialBox.getBoxType();
        switch (this.boxType) {
            case STANDARD:
                this.width = PackingRules.STANDARD_PALLET_WIDTH;
                this.height = PackingRules.STANDARD_BOX_HEIGHT;
                this.length = PackingRules.STANDARD_PALLET_LENGTH;
                this.isCrate = false;
                this.minNumPerContainer = PackingRules.STANDARD_BOX; 
                break;
            case LARGE:
                this.width = PackingRules.STANDARD_PALLET_WIDTH;
                this.height = PackingRules.LARGE_BOX_HEIGHT;
                this.length = PackingRules.STANDARD_PALLET_WIDTH;
                this.isCrate = false;
                this.minNumPerContainer = PackingRules.LARGE_BOX;
                break;
                case CRATE:
            case CRATE_LARGE:
                this.width = PackingRules.CRATE_WIDTH;
                this.height = PackingRules.CRATE_HEIGHT;
                this.length = PackingRules.CRATE_LENGTH;
                this.weight = PackingRules.CRATE_WEIGHT;
                this.isCrate = true;
                this.minNumPerContainer = 1;
                break;
            case UNBOXABLE:
                throw new IllegalArgumentException("UNBOXABLE" + initialBox.getId());    
        }
        this.boxInContainer.add(initialBox);
        initialBox.setInContainer(true);
    }
    
    public String getId() {
        return id;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getLength() {
        return length;
    }
    
    public List<Box> getBoxInContainer() {
        return boxInContainer;
    }
    
    public boolean isCrate() {
        return isCrate;
    }
    
    public boolean isFull() {
        if (boxInContainer.isEmpty() || minNumPerContainer == 0) {
            return false;
        }
        return boxInContainer.size() >= minNumPerContainer;
    }
    
    public float getTotalWeight() {
        float totalWeight = weight;
        for (Box box : boxInContainer) {
            totalWeight += box.getWeight();
        }
        return totalWeight;
    }

    public boolean tryAddBox(Box box) {
        if (isFull() || box.isInContainer() || box.getBoxType() != this.boxType) {
            return false;
        }

        boxInContainer.add(box);
        box.setInContainer(true);
        return true;
    }
    
    public boolean remove(Box box) {
        Iterator<Box> iterator = boxInContainer.iterator();

        while (iterator.hasNext()) {
            Box currentBox = iterator.next();
            if (currentBox.equals(box)) {
                currentBox.setInContainer(false);
                iterator.remove();
                return true;
            }
        }
        return false;
    }
}