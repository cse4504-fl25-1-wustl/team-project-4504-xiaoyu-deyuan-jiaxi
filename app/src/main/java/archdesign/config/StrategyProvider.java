package config;

import entities.enums.ShippingProvider;
import service.costing.PlaceholderCostStrategy;
import service.costing.ShippingCostStrategy;
import java.util.Map;

/**
 * Provides the correct, currently implemented business strategy based on a given identifier.
 * This class acts as a factory for strategy objects. Its primary role is to decouple
 * the rest of the application (like the Packer) from the concrete implementation of the
 * various cost calculation strategies.
 */
public final class StrategyProvider {

    /**
     * A static, immutable map that serves as the central registry for all *currently available*
     * cost strategies.
     * <p>
     * Entries for FEDEX and UPS are intentionally omitted. They should be added here
     * only after their corresponding strategy classes (e.g., FedExCostStrategy.java)
     * have been fully implemented.
     */
    private static final Map<ShippingProvider, ShippingCostStrategy> COST_STRATEGIES = Map.of(
        ShippingProvider.PLACEHOLDER, new PlaceholderCostStrategy()
    );

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private StrategyProvider() {}

    /**
     * Retrieves the cost calculation strategy for a given shipping provider.
     * This is the single public entry point for accessing strategies.
     *
     * @param provider The ShippingProvider enum constant representing the desired provider.
     * @return The concrete implementation of ShippingCostStrategy for that provider.
     * @throws IllegalArgumentException if a strategy for the given provider has not yet been
     * implemented and registered in the COST_STRATEGIES map.
     */
    public static ShippingCostStrategy getCostStrategy(ShippingProvider provider) {
        ShippingCostStrategy strategy = COST_STRATEGIES.get(provider);
        if (strategy == null) {
            // This provides a robust, "fail-fast" error message for developers.
            throw new IllegalArgumentException("No cost strategy has been implemented for provider: " + provider);
        }
        return strategy;
    }
}