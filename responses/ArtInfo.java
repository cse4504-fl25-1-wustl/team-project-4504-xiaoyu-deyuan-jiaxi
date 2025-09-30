package responses;

public class ArtInfo {
    private String id;
    private float weight;

    public ArtInfo(String id, float weight) {
        this.id = id;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public float getWeight() {
        return weight;
    }
}