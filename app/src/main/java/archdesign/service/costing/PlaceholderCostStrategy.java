package archdesign.service.costing;

import archdesign.entities.Container;
import archdesign.entities.enums.ContainerType;

/**
 * A placeholder cost calculation strategy used for development and testing.
 * This strategy does not use weight classes. Instead, it creates a cost function
 * designed to guide the OptimizationService towards two specific goals:
 * 1. Minimizing the total weight of the shipment.
 * 2. Using the minimum number of containers possible.
 */
public class PlaceholderCostStrategy implements ShippingCostStrategy {

    /**
     * A factor representing the cost per unit of weight (e.g., $10 per lb).
     * This part of the cost function encourages the optimizer to keep the total weight low.
     */
    private static final double COST_PER_WEIGHT_UNIT = 10;

    /**
     * Calculates the cost for a single container based on a simple linear model.
     * The formula is: Cost = (Total Weight * Cost Per Weight Unit).
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
    
    /**
     * Estimates the shipping cost for a container type with a given estimated weight.
     * For this placeholder strategy, it's simply: weight * COST_PER_WEIGHT_UNIT.
     *
     * @param containerType The type of container being considered.
     * @param estimatedTotalWeight The estimated total weight (container + contents) in lbs.
     * @return The estimated shipping cost as a double.
     */
    @Override
    public double estimateCost(ContainerType containerType, double estimatedTotalWeight) {
        return estimatedTotalWeight * COST_PER_WEIGHT_UNIT;
    }
    
    /**
     * Returns the cost coefficient for a container type to be used in the optimization solver.
     * For this linear strategy, the coefficient is based on container weight * cost rate.
     * <p>
     * The solver will use: minimize(sum of containerCount * coefficient)
     *
     * @param containerType The type of container.
     * @param averageContentWeight The average expected weight of contents per container.
     * @return The cost coefficient (multiplied by 100 for integer arithmetic).
     */
    @Override
    public long getCostCoefficient(ContainerType containerType, double averageContentWeight) {
        // Total estimated weight = container base weight + average content weight
        double estimatedTotalWeight = containerType.getWeight() + averageContentWeight;
        // Cost = weight * rate, multiply by 100 for integer arithmetic, add 1 for tie-breaking
        return (long)(estimatedTotalWeight * COST_PER_WEIGHT_UNIT * 100) + 1;
    }
}