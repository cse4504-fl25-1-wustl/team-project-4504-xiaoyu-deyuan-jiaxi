package archdesign.service.costing;
import archdesign.entities.Container;

/**
 * A placeholder cost calculation strategy used for development and testing.
 * This strategy does not use weight classes. Instead, it creates a cost function
 * designed to guide the OptimizationService towards two specific goals:
 * 1. Minimizing the total weight of the shipment.
 * 2. Using the minimum number of containers possible.
 */
public class PlaceholderCostStrategy implements ShippingCostStrategy {

    /**
     * A factor representing the cost per unit of weight (e.g., $0.50 per lb).
     * This part of the cost function encourages the optimizer to keep the total weight low.
     */
    private static final double COST_PER_WEIGHT_UNIT = 10;

    /**
     * Calculates the cost for a single container based on a simple linear model.
     * The formula is: Cost = (Total Weight * Cost Per Weight Unit) + Fixed Fee Per Container.
     *
     * @param container The container to evaluate.
     * @return The calculated cost for this container.
     */
    @Override
    public double calculateCost(Container container) {
        if (container == null) {
            return 0.0;
        }
        // Calculate the variable cost based on the container's total weight.
        double variableWeightCost = container.getTotalWeight() * COST_PER_WEIGHT_UNIT;

        // The final cost is the sum of the variable weight cost and the fixed usage fee.
        return variableWeightCost;
    }
}