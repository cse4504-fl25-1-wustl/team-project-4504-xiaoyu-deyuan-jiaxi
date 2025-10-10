package entities;
import interactor.PackingRules;
import interactor.PackingRules.BoxType;
import entities.Material;


public class Art {
    private String id;
    private float weight;
    private int height;
    private int width;
    private int thickness;
    private boolean inBox;
    private Material material;

    public Art(String id, float weight, int height, int width, int thickness, boolean inBox, Material material) {
        this.id = id;
        this.weight = height * width * (float)material.getWeight();
        this.height = height;
        this.width = width;
        this.thickness = thickness; //thickness is not provided in the excel, should be default to 0?
        this.inBox = inBox;
        this.material = material;
    }

    public String getId() {
        return id;
    }

    public float getWeight() {
        return weight;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public float getThickness() {
        return thickness;
    }
    
    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public boolean fitStandardBox() {
          return (width <= PackingRules.STANDARD_BOX_LIMIT && height <= PackingRules.STANDARD_BOX_LIMIT && thickness <= PackingRules.STANDARD_BOX_LIMIT);
    }

    public boolean fitLargeBox() {
        return (width <= PackingRules.LARGE_BOX_LIMIT && height <= PackingRules.LARGE_BOX_LIMIT && thickness <= PackingRules.LARGE_BOX_LIMIT);
    }

    public boolean fitSmallCrate() {
        return (width <= PackingRules.CRATE_SMALL_LIMIT && height <= PackingRules.CRATE_SMALL_LIMIT && thickness <= PackingRules.CRATE_SMALL_LIMIT);
    }

    public boolean fitCrate() {
        return (width <= PackingRules.CRATE_LIMIT && height <= PackingRules.CRATE_LIMIT && thickness <= PackingRules.CRATE_LIMIT);
    }
    //may never need it?
    public boolean fitHeight() {
        //check the rules
        return (width <= PackingRules.MAX_RECOMMENDED_HEIGHT && height <= PackingRules.MAX_RECOMMENDED_HEIGHT && thickness <= PackingRules.MAX_RECOMMENDED_HEIGHT);
    }


    public BoxType getBoxType()
    {
        Material material = this.getMaterial();
        if (fitStandardBox() && material.getPiecePerBox() > 0) {
            return BoxType.STANDARD;
        } 
        else if (fitLargeBox() && material.getPiecePerBox() > 0) {
            return BoxType.LARGE;
        } 
        else if (fitCrate())
        {
            if (fitSmallCrate() && material.getPiecePerCrate() > 0)
            {
                return BoxType.CRATE;
            }
            else if(material.getPiecePerCrateLarge() > 0)
            {
                return BoxType.CRATE_LARGE;
            }
        }
        return BoxType.UNBOXABLE;
    }

    public boolean specialHandle() {
        return false;
    }   

    public boolean isInBox() {
        return inBox;
    }

    public void setInBox(boolean status)
    {
    	inBox = status;
    }
}
