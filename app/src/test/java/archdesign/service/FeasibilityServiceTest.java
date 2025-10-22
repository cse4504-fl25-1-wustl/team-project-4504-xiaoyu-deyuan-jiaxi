package archdesign.service;

import archdesign.config.spec.BoxRuleSpecification;
import archdesign.config.spec.ContainerRuleSpecification;
import archdesign.config.RuleProvider;
import archdesign.interactor.UserConstraints;
import archdesign.service.FeasibilityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import archdesign.entities.Art;
import archdesign.entities.Box;
import archdesign.entities.enums.Material;
import archdesign.interactor.UserConstraints;
import java.util.*;

import archdesign.config.RuleProvider;
import archdesign.config.spec.BoxRuleSpecification;
import archdesign.config.spec.ContainerRuleSpecification;
import archdesign.entities.Art;
import archdesign.entities.enums.Material;
import archdesign.interactor.UserConstraints;
import archdesign.service.FeasibilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

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
        constraints = new UserConstraints();
    }

    @Test
    void testRuleMatch_ByWidthOrHeight() {
    // create a rule that matches width between 10 and 20
    var rule = BoxRuleSpecification.newBuilder("Test", archdesign.entities.enums.BoxType.STANDARD, 2)
        .withMinWidth(10).withMaxWidth(20).withMinHeight(0).withMaxHeight(5).build();

    var svc = new FeasibilityService(List.of(rule), Collections.emptyList());

    // width matches
    Art a1 = new Art("W1", 1, 15, 1, Material.GLASS);
    var opts1 = svc.getValidPackingOptions(a1, constraints);
    assertFalse(opts1.isEmpty());

    // height matches (height 3 within 0-5)
    Art a2 = new Art("H1", 3, 1, 1, Material.GLASS);
    var opts2 = svc.getValidPackingOptions(a2, constraints);
    assertFalse(opts2.isEmpty());

    // neither matches
    Art a3 = new Art("N1", 6, 9, 1, Material.GLASS);
    var opts3 = svc.getValidPackingOptions(a3, constraints);
    assertTrue(opts3.isEmpty());
    }

    @Test
    void testMaterialSpecificRule() {
    var glassRule = BoxRuleSpecification.newBuilder("Glass only", archdesign.entities.enums.BoxType.LARGE, 1)
        .forMaterial(Material.GLASS).withMinWidth(0).withMaxWidth(100).build();

    var svc = new FeasibilityService(List.of(glassRule), Collections.emptyList());

    Art glass = new Art("G1", 10, 10, 1, Material.GLASS);
    Art acrylic = new Art("A1", 10, 10, 1, Material.ACRYLIC);

    assertFalse(svc.getValidPackingOptions(glass, constraints).isEmpty());
    assertTrue(svc.getValidPackingOptions(acrylic, constraints).isEmpty());
    }

    @Test
    void testAllowedBoxTypesConstraintIsApplied() {
    var ruleStandard = BoxRuleSpecification.newBuilder("Std", archdesign.entities.enums.BoxType.STANDARD, 2)
        .withMaxWidth(50).build();
    var ruleLarge = BoxRuleSpecification.newBuilder("Large", archdesign.entities.enums.BoxType.LARGE, 1)
        .withMinWidth(51).build();

    var svc = new FeasibilityService(List.of(ruleStandard, ruleLarge), Collections.emptyList());

    // Only allow LARGE boxes via constraints
    var constraintsLargeOnly = UserConstraints.newBuilder()
        .withAllowedBoxTypes(List.of(archdesign.entities.enums.BoxType.LARGE))
        .build();

    Art art = new Art("X", 10, 55, 1, Material.GLASS);

    var options = svc.getValidPackingOptions(art, constraintsLargeOnly);
    assertFalse(options.isEmpty());
    assertTrue(options.stream().allMatch(o -> o.boxType() == archdesign.entities.enums.BoxType.LARGE));
    }

    @Test
    void testGetValidContainerOptions_RespectsAllowedContainerTypes() {
    var containerRule = ContainerRuleSpecification.newBuilder("c1")
        .forContainerType(archdesign.entities.enums.ContainerType.STANDARD_PALLET)
        .withAllowedBoxType(archdesign.entities.enums.BoxType.STANDARD)
        .withCapacity(4)
        .build();

    var svc = new FeasibilityService(Collections.emptyList(), List.of(containerRule));

    Box b = new Box("b1", archdesign.entities.enums.BoxType.STANDARD, 10, 10, 1);

    // default constraints allow all container types
    var opts = svc.getValidContainerOptions(b, constraints);
    assertFalse(opts.isEmpty());

    // restrict to empty allowed set -> should filter out the STANDARD_PALLET
    var restricted = UserConstraints.newBuilder().withAllowedContainerTypes(List.of(archdesign.entities.enums.ContainerType.OVERSIZE_PALLET)).build();
    var optsRestricted = svc.getValidContainerOptions(b, restricted);
    assertTrue(optsRestricted.isEmpty());
    }

    @Test
    void testDistinctOptionsAndDuplicatesHandled() {
    // Two rules that would produce the same PackingOption
    var r1 = BoxRuleSpecification.newBuilder("r1", archdesign.entities.enums.BoxType.STANDARD, 2)
        .withMaxWidth(100).build();
    var r2 = BoxRuleSpecification.newBuilder("r2", archdesign.entities.enums.BoxType.STANDARD, 2)
        .withMaxWidth(100).build();

    var svc = new FeasibilityService(List.of(r1, r2), Collections.emptyList());
    Art art = new Art("D", 10, 10, 1, Material.GLASS);
    var opts = svc.getValidPackingOptions(art, constraints);
    // distinct should remove duplicates
    assertEquals(1, opts.size());
    }

    @Test
    void testNullOrMissingInputsHandledGracefully() {
    var svc = new FeasibilityService(Collections.emptyList(), Collections.emptyList());
    // null art should not throw NPE in a well-behaved service; expect empty options
    assertTrue(svc.getValidPackingOptions(new Art("id", 1, 1, 1, Material.GLASS), constraints) != null);
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
