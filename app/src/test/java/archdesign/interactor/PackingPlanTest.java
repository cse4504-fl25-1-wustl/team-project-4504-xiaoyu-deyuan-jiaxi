package archdesign.interactor;

import archdesign.entities.Container;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PackingPlan class.
 * Covers constructor behavior, total cost, and container-related logic.
 * Does NOT depend on ContainerType enum.
 */
class PackingPlanTest {

    @Test
    void testConstructor_WithEmptyContainers() {
        List<Container> emptyContainers = new ArrayList<>();
        double totalCost = 100.0;

        PackingPlan plan = new PackingPlan(emptyContainers, totalCost);

        assertNotNull(plan, "PackingPlan should not be null");
        assertNotNull(plan.getContainers(), "Container list should not be null");
        assertTrue(plan.getContainers().isEmpty(), "Container list should be empty");
        assertEquals(100.0, plan.getTotalCost(), 0.001, "Total cost should match input value");
        assertEquals(0.0, plan.getTotalWeight(), 0.001, "Total weight should be 0 for empty containers");
        assertEquals(0, plan.getTotalContainerCount(), "Total container count should be 0");
        assertEquals(0, plan.getTotalBoxCount(), "Total box count should be 0");
    }

    @Test
    void testConstructor_WithNullContainers() {
        double totalCost = 50.0;
        PackingPlan plan = new PackingPlan(null, totalCost);

        assertNotNull(plan, "PackingPlan should not be null even if containers are null");
        assertEquals(50.0, plan.getTotalCost(), 0.001, "Total cost should match input");
        assertEquals(0.0, plan.getTotalWeight(), 0.001, "Weight should be 0 if containers are null");
        assertEquals(0, plan.getTotalContainerCount(), "Container count should be 0 if null");
        assertEquals(0, plan.getTotalBoxCount(), "Box count should be 0 if null");
    }

    @Test
    void testConstructor_WithValidContainers() {
        List<Container> containers = new ArrayList<>();
        containers.add(new Container("C001", null, 100, 80, 50, 20.0, 5));
        containers.add(new Container("C002", null, 120, 100, 60, 25.0, 6));

        double totalCost = 250.0;
        PackingPlan plan = new PackingPlan(containers, totalCost);

        assertNotNull(plan, "PackingPlan should not be null");
        assertEquals(2, plan.getContainers().size(), "Container count should match input list");
        assertEquals(250.0, plan.getTotalCost(), 0.001, "Total cost should match input");
    }

    @Test
    void testAggregateValues_AfterAddingContainers() {
        List<Container> containers = new ArrayList<>();
        containers.add(new Container("C001", null, 100, 80, 50, 20.0, 5));
        containers.add(new Container("C002", null, 120, 100, 60, 25.0, 6));

        PackingPlan plan = new PackingPlan(containers, 400.0);

        assertEquals(2, plan.getTotalContainerCount(), "Should count 2 containers");
        assertEquals(400.0, plan.getTotalCost(), 0.001, "Total cost should be positive");
        assertNotNull(plan.toString(), "toString should not return null");
    }


}
