package interactor;

import entities.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the final, complete output of the packing process.
 * This class is an immutable, self-contained "report" or "instruction set" that
 * describes exactly how all items should be packed and provides a summary of the results.
 * It is created by the OptimizationService and is intended to be read-only by the caller.
 */
public class PackingPlan {

    // --- Core Data: The hierarchical structure of the packed items ---
    private final List<Container> containers;

    // --- Summary Metadata: A high-level, pre-calculated overview of the results ---
    private final double totalCost;
    private final double totalWeight;
    private final int totalContainerCount;
    private final int totalBoxCount;

    /**
     * Constructs a new PackingPlan.
     * The constructor is the only place where the plan's state is set.
     * It immediately calculates all summary metadata based on the provided container list.
     *
     * @param containers The finalized list of containers, fully packed with boxes and art.
     * @param totalCost The total shipping cost, calculated by the OptimizationService.
     */
    public PackingPlan(List<Container> containers, double totalCost) {
        this.containers = containers != null ? containers : new ArrayList<>();
        this.totalCost = totalCost;

        // --- Calculate summary data once upon creation ---
        this.totalContainerCount = this.containers.size();
        this.totalWeight = this.containers.stream()
                                          .mapToDouble(Container::getTotalWeight)
                                          .sum();
        this.totalBoxCount = this.containers.stream()
                                            .mapToInt(c -> c.getBoxesInContainer().size())
                                            .sum();
    }

    // --- Public Getters ---

    /**
     * Returns an unmodifiable view of the list of containers.
     * @return A read-only list of the containers in this plan.
     */
    public List<Container> getContainers() {
        return Collections.unmodifiableList(containers);
    }

    /**
     * Gets the total calculated cost for shipping everything in this plan.
     * @return The total shipping cost.
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Gets the pre-calculated total weight of all containers in this plan.
     * @return The total weight.
     */
    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Gets the pre-calculated total number of containers used in this plan.
     * @return The total container count.
     */
    public int getTotalContainerCount() {
        return totalContainerCount;
    }

    /**
     * Gets the pre-calculated total number of boxes used across all containers.
     * @return The total box count.
     */
    public int getTotalBoxCount() {
        return totalBoxCount;
    }
}