package responses;

import java.util.List;

public class BoxInfo {
    private String id;
    private List<String> itemIds;
    private float weight;

    public BoxInfo(String id, List<String> itemIds, float weight) {
        this.id = id;
        this.itemIds = itemIds;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<String> itemIds) {
        this.itemIds = itemIds;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}