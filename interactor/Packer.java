package interactor;

import entities.Art;
import entities.Box;
import entities.Container;
import java.util.ArrayList; // <-- Import ArrayList
import java.util.List;

public class Packer {
    private List<Art> artsToPack;
    private List<Box> boxesUsed;
    private List<Container> containersUsed;

    public Packer(List<Art> artsToPack) {
        this.artsToPack = artsToPack;
        this.boxesUsed = new ArrayList<>(); // <-- Initialize the list
        this.containersUsed = new ArrayList<>(); // <-- Initialize the list
    }

    public void pack() {
        // Packing logic to be implemented
    }

    public List<Art> getArtsToPack() {
        return artsToPack;
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

    public void packContainer(boolean AcceptCrate) {
        // Container packing logic to be implemented
    }

    public void optimizePacking() {
        // Optimization logic to be implemented
    }
}