package interactor;

import entities.Art;
import entities.Box;
import entities.Container;
import java.util.ArrayList;
import java.util.List;

public class Packer {
    private List<Art> artsToPack;
    private List<Box> boxesUsed;
    private List<Container> containersUsed;

    public Packer(List<Art> artsToPack) {
        this.artsToPack = artsToPack;
        this.boxesUsed = new ArrayList<>();
        this.containersUsed = new ArrayList<>();
    }

    public void pack() {
        if (artsToPack == null || artsToPack.isEmpty()) return;
        
        // Sort by weight (heavier first)
        List<Art> sorted = new ArrayList<>(artsToPack);
        sorted.sort((a, b) -> Float.compare(b.getWeight(), a.getWeight()));

        int boxCounter = 1;
        for (Art art : sorted) {
            boolean placed = false;
            
            // Try existing boxes first
            for (Box box : boxesUsed) {
                if (!box.isFull() && box.tryAddArt(art)) {
                    placed = true;
                    break;
                }
            }
            
            // Create new box if needed
            if (!placed) {
                Box newBox = createAppropriateBox(boxCounter++);
                if (newBox.tryAddArt(art)) {
                    boxesUsed.add(newBox);
                }
            }
        }

        packContainer(false);
    }

    private Box createAppropriateBox(int counter) {
        if (artsToPack.get(0).fitStandardBox()) {
            return new Box("B" + counter, 10, 10, 10, 50.0f, new ArrayList<>());
        } else if (artsToPack.get(0).fitLargeBox()) {
            return new Box("B" + counter, 20, 20, 20, 200.0f, new ArrayList<>());
        } else {
            return new Box("B" + counter, 100, 100, 100, 1000.0f, new ArrayList<>());
        }
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
        pack();
    }

    public void packContainer(boolean acceptCrate) {
        containersUsed.clear();
        if (boxesUsed.isEmpty()) return;

        int containerCounter = 1;
        Container current = new Container("C" + containerCounter++, 100, 100, new ArrayList<>(), false);

        for (Box box : boxesUsed) {
            if (!current.tryAddBox(box)) {
                containersUsed.add(current);
                current = new Container("C" + containerCounter++, 100, 100, new ArrayList<>(), false);
                
                if (!current.tryAddBox(box) && acceptCrate) {
                    Container crate = new Container("CR" + containerCounter++, 200, 200, new ArrayList<>(), true);
                    crate.tryAddBox(box);
                    containersUsed.add(crate);
                    current = new Container("C" + containerCounter++, 100, 100, new ArrayList<>(), false);
                } else {
                    current.tryAddBox(box);
                }
            }
        }

        if (!current.getBoxInContainer().isEmpty()) {
            containersUsed.add(current);
        }
    }

    public void optimizePacking() {
        // Simple optimization: re-pack containers
        List<Box> boxesCopy = new ArrayList<>(boxesUsed);
        boxesUsed.clear();
        containersUsed.clear();
        
        boxesUsed = boxesCopy;
        packContainer(true);
    }
}