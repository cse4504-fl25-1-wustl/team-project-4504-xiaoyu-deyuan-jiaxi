package responses;

import entities.Art;
import entities.Box;
import entities.Container;
import interactor.Packer;
import java.util.ArrayList;
import java.util.List;

public class Response {
    private List<Art> arts;
    private List<Box> boxes;
    private List<Container> containers;
    private float totalWeight;

    public Response(Packer packer) {
        this.arts = packer.getArtsToPack();
        this.boxes = packer.getBoxesUsed();
        this.containers = packer.getContainersUsed();
        this.totalWeight = calculateTotalWeight();
    }

    private float calculateTotalWeight() {
        float weight = 0;
        for (Container container : containers) {
            weight += container.getTotalWeight();
        }
        return weight;
    }

    public void displayShipmentDetails() {
        System.out.println("--- Shipment Details ---");
        System.out.println("Total Weight: " + totalWeight);
        
        System.out.println("\n--- Items ---");
        for (Art art : arts) {
            System.out.println("ID: " + art.getId() + ", Weight: " + art.getWeight());
        }
        
        System.out.println("\n--- Boxes ---");
        for (Box box : boxes) {
            System.out.println("ID: " + box.getId() + ", Weight: " + box.getWeight());
            System.out.print("  Items: ");
            List<String> itemIds = new ArrayList<>();
            for (Art art : box.getArtsInBox()) {
                itemIds.add(art.getId());
            }
            System.out.println(String.join(", ", itemIds));
        }
        
        System.out.println("\n--- Containers ---");
        for (Container container : containers) {
            String type = container.isCrate() ? "Crate" : "Pallet";
            System.out.println("ID: " + container.getId() + ", Type: " + type + 
                             ", Weight: " + container.getTotalWeight() + 
                             ", Height: " + container.getHeight());
            System.out.print("  Boxes: ");
            List<String> boxIds = new ArrayList<>();
            for (Box box : container.getBoxInContainer()) {
                boxIds.add(box.getId());
            }
            System.out.println(String.join(", ", boxIds));
        }
    }

    // Getters for the response data
    public List<Art> getArts() {
        return arts;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public List<Container> getContainers() {
        return containers;
    }

    public float getTotalWeight() {
        return totalWeight;
    }
}