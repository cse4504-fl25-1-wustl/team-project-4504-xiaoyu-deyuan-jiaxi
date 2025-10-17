package service;

import config.RuleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import config.RuleProvider;
import config.spec.BoxRuleSpecification;
import config.spec.ContainerRuleSpecification;
import entities.Art;
import entities.enums.Material;
import interactor.UserConstraints;
import java.util.*;

public class FeasibilityServiceTest {

    private FeasibilityService feasibilityService;
    private List<BoxRuleSpecification> boxRules;
    private List<ContainerRuleSpecification> containerRules;
    private UserConstraints constraints;

    @BeforeEach
    void setUp() {
        constraints = new UserConstraints();
        boxRules = RuleProvider.getBoxRules(constraints);
        containerRules = RuleProvider.getContainerRules();
        feasibilityService = new FeasibilityService(boxRules, containerRules);
        constraints = new UserConstraints(); // 假设该类有默认构造函数
    }

    @Test
    void testValidOptions_ForGlassArt() {
        Art art = new Art("A001", 20, 30, 40, Material.GLASS);

        var validOptions = feasibilityService.getValidPackingOptions(art, constraints);
        assertNotNull(validOptions, "Returned packing options should not be null");
        assertFalse(validOptions.isEmpty(), "GLASS art should have at least one valid packing option");
    }

    @Test
    void testValidOptions_ForOversizedArt() {
        Art art = new Art("A002", 999, 999, 999, Material.PATIENT_BOARD);

        var validOptions = feasibilityService.getValidPackingOptions(art, constraints);
        assertNotNull(validOptions, "Returned packing options should not be null");
        assertTrue(validOptions.isEmpty(), "Oversized art should not have any valid packing options");
    }
}
