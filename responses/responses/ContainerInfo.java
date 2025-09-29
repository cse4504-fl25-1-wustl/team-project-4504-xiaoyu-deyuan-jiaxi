package responses;

import java.util.List;

public class ContainerInfo {
    private String id;
    private List<String> boxIds;
    private float weight;
    private int height;
    private String type; // "Crate" or "Pallet"

    public ContainerInfo(String id, List<String> boxIds, float weight, int height, String type) {
        this.id = id;
        this.boxIds = boxIds;
        this.weight = weight;
        this.height = height;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public List<String> getBoxIds() {
        return boxIds;
    }

    public float getWeight() {
        return weight;
    }

    public int getHeight() {
        return height;
    }

    public String getType() {
        return type;
    }
}