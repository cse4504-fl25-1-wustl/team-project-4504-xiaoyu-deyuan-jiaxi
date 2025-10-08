package responses;

import java.util.List;

public class Response
{
    private List<ArtInfo> arts;
    private List<BoxInfo> boxes;
    private List<ContainerInfo> containers;
    private float totalWeight;

    public Response(List<ArtInfo> arts, List<BoxInfo> boxes, List<ContainerInfo> containers, float totalWeight) {
        this.arts = arts;
        this.boxes = boxes;
        this.containers = containers;
        this.totalWeight = totalWeight;
    }

    public List<ArtInfo> getArts() {
        return arts;
    }

    public List<BoxInfo> getBoxes() {
        return boxes;
    }

    public List<ContainerInfo> getContainers() {
        return containers;
    }

    public float getTotalWeight() {
        return totalWeight;
    }


    public void setArts(List<ArtInfo> arts) {
        this.arts = arts;
    }

    public void setBoxes(List<BoxInfo> boxes) {
        this.boxes = boxes;
    }

    public void setContainers(List<ContainerInfo> containers) {
        this.containers = containers;
    }

    public void setTotalWeight(float totalWeight) {
        this.totalWeight = totalWeight;
    }
}
