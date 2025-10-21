package archdesign.response;

import archdesign.entities.Art;
import archdesign.entities.Box;
import archdesign.entities.Container;
import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import archdesign.entities.enums.Material;
import archdesign.interactor.PackingPlan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseTest {

    @Test
    public void generateViewModel_happyPath_convertsAllLevels() {
        // create one art with known dimensions and material
        Art art = new Art("a1", 10, 10, 1, Material.GLASS); // weight = ceil(100 * 0.0098) = 1

        // create a box and add the art
        Box box = new Box("b1", BoxType.STANDARD, BoxType.STANDARD.getWidth(), BoxType.STANDARD.getLength(), BoxType.STANDARD.getMinHeight());
        box.addArt(art);

        // create a container and add the box
        Container container = new Container(
                "c1",
                ContainerType.STANDARD_CRATE,
                ContainerType.STANDARD_CRATE.getWidth(),
                ContainerType.STANDARD_CRATE.getLength(),
                ContainerType.STANDARD_CRATE.getMinHeight(),
                ContainerType.STANDARD_CRATE.getWeight(),
                ContainerType.STANDARD_CRATE.getBaseHeight()
        );
        container.addBox(box);

        // build packing plan
        PackingPlan plan = new PackingPlan(List.of(container), 123.45);

        // generate view model
        Response resp = new Response(plan);
        ShipmentViewModel vm = resp.generateViewModel();

        assertNotNull(vm, "ShipmentViewModel should not be null");

        // totals
        assertEquals(plan.getTotalWeight(), vm.totalWeight(), 1e-6);
        assertEquals(plan.getTotalCost(), vm.totalCost(), 1e-6);
        assertEquals(plan.getContainers().size(), vm.totalContainers());
        assertEquals(plan.getContainers().stream().mapToInt(c -> c.getBoxesInContainer().size()).sum(), vm.totalBoxes());

        // container level
        assertEquals(1, vm.containers().size());
        ContainerViewModel cvm = vm.containers().get(0);
        assertEquals(container.getId(), cvm.id());
        assertEquals(container.getContainerType().name(), cvm.type());
        assertEquals(container.getTotalWeight(), cvm.weight(), 1e-6);

        // box level
        assertEquals(1, cvm.boxes().size());
        BoxViewModel bvm = cvm.boxes().get(0);
        assertEquals(box.getId(), bvm.id());
        assertEquals(box.getTotalWeight(), bvm.weight(), 1e-6);

        // art level
        assertEquals(1, bvm.arts().size());
        ArtViewModel avm = bvm.arts().get(0);
        assertEquals(art.getId(), avm.id());
        assertEquals(art.getWidth(), avm.width());
        assertEquals(art.getHeight(), avm.height());
        assertEquals(art.getMaterial().getDisplayName(), avm.material());
        assertEquals(art.getWeight(), avm.weight(), 1e-6);
    }

    @Test
    public void generateViewModel_nullPlan_returnsNull() {
        Response r = new Response(null);
        assertNull(r.generateViewModel(), "Response with null plan should return null view model");
    }

    @Test
    public void generateViewModel_multipleContainers_aggregatesTotals() {
        // create two arts
        Art art1 = new Art("a1", 10, 10, 1, Material.ACRYLIC); // weight small
        Art art2 = new Art("a2", 20, 5, 1, Material.CANVAS_GALLERY); // different weight

        // box for container 1
        Box box1 = new Box("b1", BoxType.STANDARD, BoxType.STANDARD.getWidth(), BoxType.STANDARD.getLength(), BoxType.STANDARD.getMinHeight());
        box1.addArt(art1);

        // box for container 2
        Box box2 = new Box("b2", BoxType.LARGE, BoxType.LARGE.getWidth(), BoxType.LARGE.getLength(), BoxType.LARGE.getMinHeight());
        box2.addArt(art2);

        // container 1
        Container c1 = new Container("c1", ContainerType.STANDARD_PALLET, ContainerType.STANDARD_PALLET.getWidth(), ContainerType.STANDARD_PALLET.getLength(), ContainerType.STANDARD_PALLET.getMinHeight(), ContainerType.STANDARD_PALLET.getWeight(), ContainerType.STANDARD_PALLET.getBaseHeight());
        c1.addBox(box1);

        // container 2
        Container c2 = new Container("c2", ContainerType.OVERSIZE_PALLET, ContainerType.OVERSIZE_PALLET.getWidth(), ContainerType.OVERSIZE_PALLET.getLength(), ContainerType.OVERSIZE_PALLET.getMinHeight(), ContainerType.OVERSIZE_PALLET.getWeight(), ContainerType.OVERSIZE_PALLET.getBaseHeight());
        c2.addBox(box2);

        double cost = 10.0;
        PackingPlan plan = new PackingPlan(List.of(c1, c2), cost);

        ShipmentViewModel vm = new Response(plan).generateViewModel();
        assertNotNull(vm);

        // total containers and boxes
        assertEquals(2, vm.totalContainers());
        assertEquals(2, vm.totalBoxes());

        // total weight should equal plan total weight
        assertEquals(plan.getTotalWeight(), vm.totalWeight(), 1e-9);
        assertEquals(cost, vm.totalCost(), 1e-9);
    }

    @Test
    public void generateViewModel_handlesEmptyContainersAndBoxes_andPrecision() {
        // Create an empty box (no arts)
        Box emptyBox = new Box("eb", BoxType.UPS_SMALL, BoxType.UPS_SMALL.getWidth(), BoxType.UPS_SMALL.getLength(), BoxType.UPS_SMALL.getMinHeight());

        // Create a container with one empty box
        Container containerWithEmptyBox = new Container("cc1", ContainerType.STANDARD_PALLET, ContainerType.STANDARD_PALLET.getWidth(), ContainerType.STANDARD_PALLET.getLength(), ContainerType.STANDARD_PALLET.getMinHeight(), ContainerType.STANDARD_PALLET.getWeight(), ContainerType.STANDARD_PALLET.getBaseHeight());
        containerWithEmptyBox.addBox(emptyBox);

        // Create an entirely empty container (no boxes)
        Container emptyContainer = new Container("cc2", ContainerType.GLASS_PALLET, ContainerType.GLASS_PALLET.getWidth(), ContainerType.GLASS_PALLET.getLength(), ContainerType.GLASS_PALLET.getMinHeight(), ContainerType.GLASS_PALLET.getWeight(), ContainerType.GLASS_PALLET.getBaseHeight());

        // Plan with both
        PackingPlan plan = new PackingPlan(List.of(containerWithEmptyBox, emptyContainer), 0.123456);

        ShipmentViewModel vm = new Response(plan).generateViewModel();
        assertNotNull(vm);

        // containers should be 2, boxes should count the single empty box
        assertEquals(2, vm.totalContainers());
        assertEquals(1, vm.totalBoxes());

        // weights: empty box contributes 0, container weights include their own tare weights
        double expectedTotalWeight = plan.getTotalWeight();
        assertEquals(expectedTotalWeight, vm.totalWeight(), 1e-9);

        // Check precision/rounding: ensure small cost preserved precisely within double tolerance
        assertEquals(0.123456, vm.totalCost(), 1e-9);
    }
}
