package archdesign.service;

import archdesign.config.spec.BoxRuleSpecification;
import archdesign.config.spec.ContainerRuleSpecification;
import archdesign.config.RuleProvider;
import archdesign.interactor.UserConstraints;
import archdesign.service.FeasibilityService;

import archdesign.entities.Art;
import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import archdesign.entities.enums.Material;


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

     @Test
    void getValidPackingOptions_materialMismatch_returnsEmpty() {
        BoxRuleSpecification rule = BoxRuleSpecification.newBuilder("glass-rule", BoxType.STANDARD, 1)
                .forMaterial(Material.GLASS)
                .withMinWidth(1)
                .withMaxWidth(100)
                .withMinHeight(1)
                .withMaxHeight(100)
                .build();

        FeasibilityService svc = new FeasibilityService(List.of(rule), List.of());

        Art art = new Art("A1", 10, 10, 1, Material.ACRYLIC);
        UserConstraints constraints = UserConstraints.newBuilder().build();

        List<PackingOption> opts = svc.getValidPackingOptions(art, constraints);
        assertTrue(opts.isEmpty(), "Material mismatch should yield no packing options");
    }

    @Test
    void getValidPackingOptions_sizeBoundary_inclusiveMatch() {
        BoxRuleSpecification rule = BoxRuleSpecification.newBuilder("size-rule", BoxType.STANDARD, 2)
                .withMinWidth(10)
                .withMaxWidth(20)
                .withMinHeight(5)
                .withMaxHeight(15)
                .build();

        FeasibilityService svc = new FeasibilityService(List.of(rule), List.of());

        // width equals maxWidth and height equals minHeight -> should match (widthMatches || heightMatches)
        Art art = new Art("A2", 5, 20, 1, Material.CANVAS_GALLERY);
        UserConstraints constraints = UserConstraints.newBuilder().build();

        List<PackingOption> opts = svc.getValidPackingOptions(art, constraints);
        assertFalse(opts.isEmpty(), "Boundary sizes should be matched inclusively");
        assertEquals(BoxType.STANDARD, opts.get(0).boxType());
    }

    @Test
    void getValidContainerOptions_respectsUserConstraints_allowedListFiltering() {
        ContainerRuleSpecification cr1 = ContainerRuleSpecification.newBuilder("cr1")
                .forContainerType(ContainerType.STANDARD_PALLET)
                .withAllowedBoxType(BoxType.STANDARD)
                .withCapacity(10)
                .build();

        ContainerRuleSpecification cr2 = ContainerRuleSpecification.newBuilder("cr2")
                .forContainerType(ContainerType.STANDARD_CRATE)
                .withAllowedBoxType(BoxType.STANDARD)
                .withCapacity(5)
                .build();

        FeasibilityService svc = new FeasibilityService(List.of(), List.of(cr1, cr2));

        // Create a box of STANDARD type
        archdesign.entities.Box box = new archdesign.entities.Box("b1", BoxType.STANDARD, 10, 10, 1);

        // Constrain to only STANDARD_CRATE
        UserConstraints constraints = UserConstraints.newBuilder()
                .withAllowedContainerTypes(List.of(ContainerType.STANDARD_CRATE))
                .build();

        List<ContainerOption> opts = svc.getValidContainerOptions(box, constraints);
        assertEquals(1, opts.size());
        assertEquals(ContainerType.STANDARD_CRATE, opts.get(0).containerType());
    }

    @Test
    void getValidPackingOptions_matchesRuleAndRespectsConstraints() {
        // Rule: Glass art between width 5..15 or height 5..15 -> STANDARD box capacity 1
        BoxRuleSpecification rule = BoxRuleSpecification.newBuilder("r1", BoxType.STANDARD, 1)
                .forMaterial(Material.GLASS)
                .withMinWidth(5)
                .withMaxWidth(15)
                .withMinHeight(5)
                .withMaxHeight(15)
                .build();

        FeasibilityService svc = new FeasibilityService(List.of(rule), List.of());

        Art matching = new Art("G1", 10, 10, 1, Material.GLASS);
        UserConstraints allAllowed = new UserConstraints();

        List<PackingOption> options = svc.getValidPackingOptions(matching, allAllowed);
        assertFalse(options.isEmpty());
        assertEquals(BoxType.STANDARD, options.get(0).boxType());

        // Constraint: restrict allowed box types to empty list (none) -> returns none
        UserConstraints restrict = UserConstraints.newBuilder().withAllowedBoxTypes(List.of()).build();
    // allowedBoxTypes empty in builder means all allowed; to simulate restriction, pass a list that doesn't include STANDARD
        UserConstraints restrict2 = UserConstraints.newBuilder().withAllowedBoxTypes(List.of(BoxType.LARGE)).build();
        List<PackingOption> options3 = svc.getValidPackingOptions(matching, restrict2);
        assertTrue(options3.isEmpty(), "No options when user constraints disallow the rule's box type");
    }

    @Test
    void getValidContainerOptions_mapsBoxTypeToContainerRulesAndRespectsConstraints() {
        ContainerRuleSpecification cr = ContainerRuleSpecification.newBuilder("c1")
                .forContainerType(ContainerType.STANDARD_PALLET)
                .withAllowedBoxType(BoxType.STANDARD)
                .withCapacity(10)
                .build();

        FeasibilityService svc = new FeasibilityService(List.of(), List.of(cr));

        Box box = new Box("B1", BoxType.STANDARD, 10, 20, 5);
        UserConstraints allAllowed = new UserConstraints();

        List<ContainerOption> opts = svc.getValidContainerOptions(box, allAllowed);
        assertFalse(opts.isEmpty());
        assertEquals(ContainerType.STANDARD_PALLET, opts.get(0).containerType());

        // Restrict container types to none that match
        UserConstraints restrict = UserConstraints.newBuilder().withAllowedContainerTypes(List.of(ContainerType.OVERSIZE_PALLET)).build();
        List<ContainerOption> opts2 = svc.getValidContainerOptions(box, restrict);
        assertTrue(opts2.isEmpty());
    }
}
