package requests;

import entities.Art;
import java.util.List;

public class Request {
    private List<Art> artsToPack;
    private boolean acceptCrate;

    public Request(List<Art> artsToPack, boolean acceptCrate) {
        this.artsToPack = artsToPack;
        this.acceptCrate = acceptCrate;
    }

    public List<Art> getArtsToPack() {
        return artsToPack;
    }
    public boolean isAcceptCrate() {
        return acceptCrate;
    }
    
}
