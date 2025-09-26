
import java.util.List;

public class Container {
    private String id;
    private int width;
    private int length;
    private List<Box> boxInContainer;
    private boolean isCrate;

    public Container(String id, int width, int length, List<Box> boxInContainer, boolean isCrate) {
        this.id = id;
        this.width = width;
        this.length = length;
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
        //check the rules
        return true;
    }
    public float getTotalWeight() {
        //calculate the total weight
        return 0;
    }

}
