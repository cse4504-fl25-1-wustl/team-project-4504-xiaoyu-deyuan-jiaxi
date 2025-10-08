package entities;
import java.util.Iterator;
import java.util.List;

public class Container {
    private String id;
    private int width;
    private int length;
    private float weight;
    private List<Box> boxInContainer;
    private boolean isCrate;

    public Container(String id, int width, int length, List<Box> boxInContainer, boolean isCrate) {
        this.id = id;
        this.width = width;
        this.length = length;
        this.weight = 0;
        this.boxInContainer = boxInContainer;
        this.isCrate = isCrate;
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
        if (isCrate) {
            if (boxInContainer.isEmpty() || boxInContainer.get(0).getArtsInBox().isEmpty()) {
                return false;
            }
            Art firstArt = boxInContainer.get(0).getArtsInBox().get(0);
            Material material = firstArt.getMaterial();
            boolean isLarge = firstArt.getWidth() > 33 || firstArt.getHeight() > 33;
            int maxPieces;
            
            switch (material) {
                case GLASS_ACRYLIC_FRAMED:
                case GLASS_ACRYLIC_SUNRISE:
                    maxPieces = isLarge ? 18 : 25;
                    break;
                case CANVAS_FRAMED_GALLERY:
                case CANVAS:
                    maxPieces = isLarge ? 12 : 18;
                    break;
                case MIRRORS:
                    maxPieces = 25;
                    break;
                case ACOUSTIC_PANELS:
                case PAPER:
                case WOOD:
                case METAL:
                    maxPieces = isLarge ? 12 : 18; // Default for other materials
                    break;
                case SCULPTURE:
                case PHOTOGRAPH:
                case UNKNOWN:
                default:
                    return true; // Special handling materials
            }
            
            int currentPieces = 0;
            for (Box box : boxInContainer) {
                currentPieces += box.getArtsInBox().size();
            }
            return currentPieces >= maxPieces;
        } else {
            int maxBoxes = 4;
            return boxInContainer.size() >= maxBoxes;
        }
    }
    
    public float getTotalWeight() {
        float totalWeight = weight;
        for (Box box : boxInContainer) {
            totalWeight += box.getWeight();
        }
        return totalWeight;
    }

    public boolean tryAddBox(Box box) {
        if (!isFull()) {
            boxInContainer.add(box);
            box.setInContainer(true);
            return true;
        }
        return false;
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