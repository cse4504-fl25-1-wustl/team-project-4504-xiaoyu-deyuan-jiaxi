package requests;

import entities.Art;
import java.util.List;

public class Request {
    private List<Art> artObjects;   // 要打包的艺术品
    private boolean acceptCrates;   // 客户是否接受 crate

    public Request(List<Art> artObjects, boolean acceptCrates) {
        this.artObjects = artObjects;
        this.acceptCrates = acceptCrates;
    }

    public List<Art> getArtObjects() {
        return artObjects;
    }

    public void setArtObjects(List<Art> artObjects) {
        this.artObjects = artObjects;
    }

    public boolean isAcceptCrates() {
        return acceptCrates;
    }

    public void setAcceptCrates(boolean acceptCrates) {
        this.acceptCrates = acceptCrates;
    }
}
