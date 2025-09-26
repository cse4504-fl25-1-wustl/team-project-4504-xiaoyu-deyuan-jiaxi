
import java.util.List;

public class request {
    private List<Art> artsToPack;
    private boolean acceptCrate;

    public request(List<Art> artsToPack, boolean acceptCrate) {
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
