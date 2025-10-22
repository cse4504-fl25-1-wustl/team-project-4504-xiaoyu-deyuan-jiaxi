package archdesign.service.costing;

import static org.junit.jupiter.api.Assertions.*;

import archdesign.entities.Art;
import archdesign.entities.Box;
import archdesign.entities.Container;
import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import archdesign.entities.enums.Material;
import org.junit.jupiter.api.Test;

class PlaceholderCostStrategyTest {

    @Test
    void calculateCost_nonNullContainer_calculatesExpected() {
        // Create a single Art item: height 2, width 3, thickness ignored here, material WOOD
    Art art = new Art("A1", 2, 3, 1, Material.GLASS);

        // Create a box and add the art
    Box box = new Box("box1", BoxType.STANDARD, 10, 10, 1);
        box.addArt(art);

        // Create a container and add the box
    Container container = new Container("c1", ContainerType.STANDARD_CRATE, 100, 200, 1, 5.0, 2);
        container.addBox(box);

        PlaceholderCostStrategy strat = new PlaceholderCostStrategy();
        double cost = strat.calculateCost(container);

        // According to the strategy, cost = container.getTotalWeight() * COST_PER_WEIGHT_UNIT
        // The art weight calculation: ceil(2*3*materialWeight). For Material.WOOD weight factor we rely on enum.
        double expectedWeight = container.getTotalWeight();
        double expectedCost = expectedWeight * 10; // matches the constant in PlaceholderCostStrategy

        assertEquals(expectedCost, cost, 1e-6);
    }

    @Test
    void calculateCost_nullContainer_returnsZero() {
        PlaceholderCostStrategy strat = new PlaceholderCostStrategy();
        assertEquals(0.0, strat.calculateCost(null), 0.0);
    }
}
