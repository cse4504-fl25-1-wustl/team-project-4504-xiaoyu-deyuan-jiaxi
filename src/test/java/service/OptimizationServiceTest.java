package service;

import config.RuleProvider;
import config.StrategyProvider;
import config.spec.BoxRuleSpecification;
import config.spec.ContainerRuleSpecification;
import entities.Art;
import entities.enums.Material;
import entities.enums.ShippingProvider;
import interactor.PackingPlan;
import interactor.UserConstraints;
import service.costing.ShippingCostStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class OptimizationServiceTest {

    private OptimizationService optimizationService;
    private FeasibilityService feasibilityService;
    private ShippingCostStrategy costStrategy;
    private UserConstraints constraints;

    @BeforeEach
    public void setUp() {
        constraints = new UserConstraints();
        List<BoxRuleSpecification> boxRules = RuleProvider.getBoxRules(constraints);
        List<ContainerRuleSpecification> containerRules = RuleProvider.getContainerRules();
        costStrategy = StrategyProvider.getCostStrategy(ShippingProvider.PLACEHOLDER);
        feasibilityService = new FeasibilityService(boxRules, containerRules);
        optimizationService = new OptimizationService(feasibilityService, costStrategy);
    }

    @Test
    public void testCreateOptimalPlan_WithEmptyList() {
        List<Art> emptyList = new ArrayList<>();
        PackingPlan plan = optimizationService.createOptimalPlan(emptyList, constraints);
        assertNotNull(plan);
        assertEquals(0, plan.getTotalContainerCount());
    }

    @Test
    public void testCreateOptimalPlan_WithSingleSmallArt() {
        List<Art> arts = new ArrayList<>();
        Art smallArt = new Art("Test-1", 30, 30, 0, Material.GLASS);
        arts.add(smallArt);
        PackingPlan plan = optimizationService.createOptimalPlan(arts, constraints);
        assertNotNull(plan);
        assertTrue(plan.getTotalContainerCount() > 0);
    }
}