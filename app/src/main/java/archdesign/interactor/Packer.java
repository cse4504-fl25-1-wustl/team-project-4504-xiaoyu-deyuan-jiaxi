package archdesign.interactor;

import archdesign.config.RuleProvider;
import archdesign.config.StrategyProvider;
import archdesign.config.spec.BoxRuleSpecification;
import archdesign.config.spec.ContainerRuleSpecification;
import archdesign.entities.Art;
import archdesign.entities.enums.ShippingProvider;
import archdesign.service.FeasibilityService;
import archdesign.service.OptimizationService;
import archdesign.service.costing.ShippingCostStrategy;

import java.util.List;

/**
 * The public facade for the entire packing system.
 * This is the single entry point for any external program that needs to run the
 * packing algorithm. It hides the internal complexity of service initialization
 * and orchestration.
 */
public final class Packer {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Packer() {}

    /**
     * The main public method to execute the packing process.
     * It takes a list of arts and all necessary constraints/strategies,
     * runs the entire optimization process, and returns a complete, immutable packing plan.
     *
     * @param artsToPack The list of Art objects that need to be packed.
     * @param constraints The user-defined constraints for this specific packing run.
     * @param provider The shipping provider to use, which determines the cost strategy.
     * @return A complete PackingPlan object containing the results of the optimization.
     */
    public static PackingPlan pack(List<Art> artsToPack, UserConstraints constraints, ShippingProvider provider) {
        
        // --- Step 1: Load all configurations and strategies ---
        
        // Get the static business rules for art-to-box packing.
        List<BoxRuleSpecification> boxRules = RuleProvider.getBoxRules(constraints);
        
        // Get the static business rules for box-to-container packing.
        List<ContainerRuleSpecification> containerRules = RuleProvider.getContainerRules();
        
        // Get the appropriate cost calculation strategy based on the selected provider.
        ShippingCostStrategy costStrategy = StrategyProvider.getCostStrategy(provider);

        // --- Step 2: Initialize the core services with the loaded configurations ---

        // Create the rule checker, providing it with the rulebooks it needs.
        FeasibilityService feasibilityService = new FeasibilityService(boxRules, containerRules);
        
        // Create the optimization engine, providing it with the rule checker and cost strategy.
        OptimizationService optimizationService = new OptimizationService(feasibilityService, costStrategy);

        // --- Step 3: Execute the core logic and get the final plan ---
        
        // Call the optimization service to perform the complex calculations.
        PackingPlan finalPlan = optimizationService.createOptimalPlan(artsToPack, constraints);

        // --- Step 4: Return the result ---
        
        return finalPlan;
    }
}