
import java.util.List;

public class Packer {
    private List<Art> artsToPack;
    private List<Box> boxesUsed;
    private List<Container> containersUsed;

    public Packer(List<Art> artsToPack) {
        this.artsToPack = artsToPack;
    }
    public void pack() {
        // Packing logic to be implemented
    }
    public List<Box> getBoxesUsed() {
        return boxesUsed;
    }
    public List<Container> getContainersUsed() {
        return containersUsed;
    }
    public void packBox() {
        // Box packing logic to be implemented
    }
    public void packContainer() {
        // Container packing logic to be implemented
    }
    public void optimizePacking() {
        // Optimization logic to be implemented
    }
                  

}
