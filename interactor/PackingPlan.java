package interactor;
import entities.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the final, complete output of the packing process.
 * This class is an immutable, self-contained "report" or "instruction set" that
 * describes exactly how all items should be packed and provides a cost estimation.
 * It is created by the OptimizationService and is intended to be read-only by the caller.
 */
public class PackingPlan {

    // --- Core Data: The hierarchical structure of the packed items ---
    private final List<Container> containers;

    // --- Summary Metadata: A high-level overview of the optimization results ---
    private final double totalCost;

    /**
     * Constructs a new PackingPlan.
     * The constructor is the only place where the plan's state is set.
     *
     * @param containers The finalized list of containers, fully packed with boxes and art.
     * @param totalCost The total shipping cost, calculated by the OptimizationService using a specific strategy.
     */
    public PackingPlan(List<Container> containers, double totalCost) {
        // Ensure the list is never null and make it final.
        this.containers = containers != null ? containers : new ArrayList<>();
        this.totalCost = totalCost;
    }

    // --- Public Getters ---

    /**
     * Returns an unmodifiable view of the list of containers.
     * This prevents the caller from altering the plan after its creation, ensuring data integrity.
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
}