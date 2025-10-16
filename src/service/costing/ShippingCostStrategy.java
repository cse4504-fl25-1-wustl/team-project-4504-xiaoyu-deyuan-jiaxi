package service.costing;

import entities.Container;

/**
 * Defines the contract for all shipping cost calculation strategies.
 * This interface is the core of the Strategy Pattern for costing. It allows
 * the OptimizationService to be decoupled from the specific details of how
 * shipping costs are calculated for different providers.
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
}