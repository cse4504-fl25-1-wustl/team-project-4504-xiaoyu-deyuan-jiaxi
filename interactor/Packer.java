package interactor;

import entities.Art;
import entities.Box;
import entities.Container;
import entities.Material;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<Material, List<Art>> artsByMaterial = new HashMap<>();
        for (Art art : artsToPack) {
            artsByMaterial.computeIfAbsent(art.getMaterial(), k -> new ArrayList<>()).add(art);
        }

        int boxCounter = 1;

        for(List<Art> materialArts : artsByMaterial.values()) {
            materialArts.sort((a,b)->Float.compare(b.getWeight(), a.getWeight()));
            for (Art art : materialArts) {
                boolean placed = false;

                // Try existing boxes first
                for (Box box : boxesUsed) {
                    if (!box.isFull() && 
                        !box.getArtsInBox().isEmpty() && 
                        box.getArtsInBox().get(0).getMaterial() == art.getMaterial() && 
                        box.tryAddArt(art)) {
                        placed = true;
                        break;
                    }
                }

                // Create new box if needed
                if (!placed) {
                    Box newBox = new Box("B" + boxCounter++, 0, 0, 0, 0f, new ArrayList<>());
                    if (newBox.tryAddArt(art)) {
                        boxesUsed.add(newBox);
                    }
                }
            }
            
        }

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
        pack();
    }

    public void packContainer(boolean acceptCrate) {
        containersUsed.clear();
        if (boxesUsed.isEmpty()) return;

        int containerCounter = 1;
        Container current = new Container("C" + containerCounter++, 0, 0, new ArrayList<>(), false);

        for (Box box : boxesUsed) {
            if (!current.tryAddBox(box)) {
                containersUsed.add(current);
                current = new Container("C" + containerCounter++, 0, 0, new ArrayList<>(), false);
                
                current.tryAddBox(box);
            }
        }

        if (!current.getBoxInContainer().isEmpty()) {
            containersUsed.add(current);
        }
    }

    public void optimizePacking() {
        // Re-pack with material grouping
        List<Art> originalArts = new ArrayList<>(artsToPack);
        artsToPack.clear();
        boxesUsed.clear();
        containersUsed.clear();
        artsToPack = originalArts;
        pack();
    }
}