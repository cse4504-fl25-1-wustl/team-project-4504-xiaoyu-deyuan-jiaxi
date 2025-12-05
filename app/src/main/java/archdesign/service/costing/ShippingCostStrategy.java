package archdesign.service.costing;

import archdesign.entities.Container;
import archdesign.entities.enums.ContainerType;

/**
 * Defines the contract for all shipping cost calculation strategies.
 * This interface is the core of the Strategy Pattern for costing. It allows
 * the OptimizationService to be decoupled from the specific details of how
 * shipping costs are calculated for different providers.
 * <p>
 * Implementations can support:
 * - Simple linear pricing (PlaceholderCostStrategy)
 * - Tiered/weight-class pricing (future TieredCostStrategy)
 * - Provider-specific pricing (future FedExCostStrategy, UPSCostStrategy, etc.)
 */
public interface ShippingCostStrategy
{
    /**
     * Calculates the total shipping cost for a single, fully packed container.
     * <p>
     * Each concrete implementation of this interface will contain the specific
     * business logic and rate tables (e.g., weight classes) for a particular
     * shipping provider like FedEx or UPS.
     *
     * @param container The fully packed container whose cost needs to be calculated.
     * @return The calculated shipping cost as a double.
     */
    double calculateCost(Container container);
    
    /**
     * Estimates the shipping cost for a container type with a given estimated weight.
     * This method is used by the optimization solver to make cost-based decisions
     * BEFORE containers are actually created.
     * <p>
     * For tiered pricing strategies, this should return the cost based on the
     * weight tier that the estimated weight falls into.
     *
     * @param containerType The type of container being considered.
     * @param estimatedTotalWeight The estimated total weight (container + contents) in lbs.
     * @return The estimated shipping cost as a double.
     */
    double estimateCost(ContainerType containerType, double estimatedTotalWeight);
    
    /**
     * Returns the cost coefficient for a container type to be used in the optimization solver.
     * This value is used in the CP-SAT objective function: minimize(sum of containerCount * coefficient).
     * <p>
     * The coefficient is multiplied by 100 internally for integer arithmetic precision.
     * <p>
     * For simple linear strategies (like PlaceholderCostStrategy), this is based on weight.
     * For tiered strategies, this should reflect the marginal cost impact of adding one container.
     *
     * @param containerType The type of container.
     * @param averageContentWeight The average expected weight of contents per container.
     * @return The cost coefficient (will be multiplied by 100 for integer arithmetic).
     */
    long getCostCoefficient(ContainerType containerType, double averageContentWeight);
}