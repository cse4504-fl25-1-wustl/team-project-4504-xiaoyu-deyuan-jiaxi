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
        //crate need to modify the interface to work I guess?
    	if (isCrate) {
            if (boxInContainer.isEmpty() || boxInContainer.get(0).getArtsInBox().isEmpty()) {
                return false;
            }
            Art firstArt = boxInContainer.get(0).getArtsInBox().get(0);
            String material = firstArt.getMaterial();
            boolean isLarge = firstArt.getWidth() > 33 || firstArt.getHeight() > 33;
            int maxPieces;
            switch (material) {
                case "Glass/Acrylic":
                    maxPieces = isLarge ? 18 : 25;
                    break;
                case "Canvas":
                    maxPieces = isLarge ? 12 : 18;
                    break;
                case "Mirrors":
                    maxPieces = 25;
                    break;
                default:
                    return true; 
            }
            int currentPieces = 0;
            for (Box box : boxInContainer) {
                currentPieces += box.getArtsInBox().size();
            }
            return currentPieces >= maxPieces;
        }
    	else {
            int maxBoxes = 4;
            return boxInContainer.size() >= maxBoxes;
        }
    }
    public float getTotalWeight() {
        //calculate the total weight
    	float totalWeight = weight;
        for (Box box : boxInContainer) {
            totalWeight += box.getWeight();
        }
        return totalWeight;
    }

    public boolean tryAddBox(Box box) {
        //try to add box into the container
    	if (!isFull()) {
            boxInContainer.add(box);
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
