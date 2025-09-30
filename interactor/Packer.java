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
        if (artsToPack == null) return;
        // Pack heavier items first (simple heuristic)
        List<Art> sorted = new ArrayList<>(artsToPack);
        sorted.sort((a, b) -> Float.compare(b.getWeight(), a.getWeight()));

        // simple strategy: try to put each art into an existing box; if none fit, create a new box
        int boxCounter = 1;
        for (Art art : sorted) {
            boolean placed = false;
            for (Box box : boxesUsed) {
                if (box.tryAddArt(art)) {
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                // choose box size based on art size
                Box newBox;
                if (art.fitStandardBox()) {
                    newBox = new Box("B" + boxCounter++, 10, 10, 10, 50.0f, new ArrayList<>());
                } else if (art.fitLargeBox()) {
                    newBox = new Box("B" + boxCounter++, 20, 20, 20, 200.0f, new ArrayList<>());
                } else {
                    // fallback large crate-sized box
                    newBox = new Box("B" + boxCounter++, 100, 100, 100, 1000.0f, new ArrayList<>());
                }
                newBox.tryAddArt(art); // try to add once
                boxesUsed.add(newBox);
            }
        }

        // Default container packing uses packContainer(false)
        packContainer(false);
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
        // For now, packBox delegates to pack()
        pack();
    }

    public void packContainer(boolean AcceptCrate) {
        containersUsed.clear();
        int containerCounter = 1;
        Container current = new Container("C" + containerCounter++, 100, 100, new ArrayList<>(), false);

        for (Box box : boxesUsed) {
            boolean added = current.tryAddBox(box);
            if (!added) {
                // container is full, start a new one
                containersUsed.add(current);
                current = new Container("C" + containerCounter++, 100, 100, new ArrayList<>(), false);
                // if still can't add, and AcceptCrate==true create a crate container for this box
                if (!current.tryAddBox(box)) {
                    if (AcceptCrate) {
                        Container crate = new Container("CR" + containerCounter++, 200, 200, new ArrayList<>(), true);
                        crate.tryAddBox(box);
                        containersUsed.add(crate);
                        // start a fresh non-crate container
                        current = new Container("C" + containerCounter++, 100, 100, new ArrayList<Box>(), false);
                    } else {
                        // fallback: force add to current (best-effort)
                        current.getBoxInContainer().add(box);
                    }
                }
            }
        }

        if (current.getBoxInContainer() != null && !current.getBoxInContainer().isEmpty()) containersUsed.add(current);

    }

    public void optimizePacking() {
        // Placeholder for future optimization (e.g., bin-packing improvements)

    }
}