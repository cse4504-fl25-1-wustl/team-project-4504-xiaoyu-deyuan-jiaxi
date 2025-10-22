package archdesign.service;

import archdesign.config.spec.BoxRuleSpecification;
import archdesign.config.spec.ContainerRuleSpecification;
import archdesign.entities.Art;
import archdesign.entities.Box;
import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import archdesign.entities.enums.Material;
import archdesign.interactor.UserConstraints;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FeasibilityServiceSpecTest {

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
