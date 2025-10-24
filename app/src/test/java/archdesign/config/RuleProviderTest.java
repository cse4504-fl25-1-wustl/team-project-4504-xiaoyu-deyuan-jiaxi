package archdesign.config;

import archdesign.config.spec.BoxRuleSpecification;
import archdesign.config.spec.ContainerRuleSpecification;
import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import archdesign.entities.enums.Material;
import archdesign.interactor.UserConstraints;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuleProviderTest {

    @Test
    void testBoxRules_GeneratedCorrectly() {
        UserConstraints constraints = new UserConstraints();
        List<BoxRuleSpecification> boxRules = RuleProvider.getBoxRules(constraints);

        assertNotNull(boxRules);
        assertFalse(boxRules.isEmpty(), "Box rules should not be empty");
        assertTrue(boxRules.size() >= 20, "Expected multiple box rules for different materials and box types");
        
        // Check that all rules have required properties
        for (BoxRuleSpecification rule : boxRules) {
            assertNotNull(rule.getDescription());
            assertNotNull(rule.getAllowedBoxType());
            assertTrue(rule.getCapacity() > 0);
        }
    }

    @Test
    void testBoxRules_ContainsAllBoxTypes() {
        UserConstraints constraints = new UserConstraints();
        List<BoxRuleSpecification> boxRules = RuleProvider.getBoxRules(constraints);

        // Check that we have rules for all box types
        boolean hasStandard = boxRules.stream().anyMatch(r -> r.getAllowedBoxType() == BoxType.STANDARD);
        boolean hasLarge = boxRules.stream().anyMatch(r -> r.getAllowedBoxType() == BoxType.LARGE);
        boolean hasCrate = boxRules.stream().anyMatch(r -> r.getAllowedBoxType() == BoxType.CRATE);

        assertTrue(hasStandard, "Should have rules for STANDARD boxes");
        assertTrue(hasLarge, "Should have rules for LARGE boxes");
        assertTrue(hasCrate, "Should have rules for CRATE boxes");
    }

    @Test
    void testBoxRules_ContainsMaterialSpecificRules() {
        UserConstraints constraints = new UserConstraints();
        List<BoxRuleSpecification> boxRules = RuleProvider.getBoxRules(constraints);

        // Check that we have material-specific rules
        boolean hasGlassRules = boxRules.stream().anyMatch(r -> r.getMaterial() == Material.GLASS);
        boolean hasAcrylicRules = boxRules.stream().anyMatch(r -> r.getMaterial() == Material.ACRYLIC);
        boolean hasCanvasRules = boxRules.stream().anyMatch(r -> r.getMaterial() == Material.CANVAS_FRAMED);

        assertTrue(hasGlassRules, "Should have rules for GLASS material");
        assertTrue(hasAcrylicRules, "Should have rules for ACRYLIC material");
        assertTrue(hasCanvasRules, "Should have rules for CANVAS_FRAMED material");
    }

    @Test
    void testBoxRules_PriorityOrdering() {
        UserConstraints constraints = new UserConstraints();
        List<BoxRuleSpecification> boxRules = RuleProvider.getBoxRules(constraints);

        // Find first STANDARD box rule and first LARGE box rule
        BoxRuleSpecification firstStandardRule = boxRules.stream()
            .filter(r -> r.getAllowedBoxType() == BoxType.STANDARD)
            .findFirst()
            .orElse(null);
        
        BoxRuleSpecification firstLargeRule = boxRules.stream()
            .filter(r -> r.getAllowedBoxType() == BoxType.LARGE)
            .findFirst()
            .orElse(null);

        assertNotNull(firstStandardRule);
        assertNotNull(firstLargeRule);

        // STANDARD rules should come before LARGE rules (higher priority)
        int standardIndex = boxRules.indexOf(firstStandardRule);
        int largeIndex = boxRules.indexOf(firstLargeRule);
        assertTrue(standardIndex < largeIndex, "STANDARD box rules should have higher priority than LARGE box rules");
    }

    @Test
    void testBoxRules_StandardBoxConstraints() {
        UserConstraints constraints = new UserConstraints();
        List<BoxRuleSpecification> boxRules = RuleProvider.getBoxRules(constraints);

        // Check STANDARD box rules have correct constraints
        List<BoxRuleSpecification> standardRules = boxRules.stream()
            .filter(r -> r.getAllowedBoxType() == BoxType.STANDARD)
            .toList();

        assertFalse(standardRules.isEmpty());
        
        for (BoxRuleSpecification rule : standardRules) {
            assertTrue(rule.getMaxWidth() <= 36, "STANDARD box rules should have max width <= 36");
            assertTrue(rule.getMaxHeight() <= 36, "STANDARD box rules should have max height <= 36");
        }
    }

    @Test
    void testBoxRules_LargeBoxConstraints() {
        UserConstraints constraints = new UserConstraints();
        List<BoxRuleSpecification> boxRules = RuleProvider.getBoxRules(constraints);

        // Check LARGE box rules have correct constraints
        List<BoxRuleSpecification> largeRules = boxRules.stream()
            .filter(r -> r.getAllowedBoxType() == BoxType.LARGE)
            .toList();

        assertFalse(largeRules.isEmpty());
        
        for (BoxRuleSpecification rule : largeRules) {
            assertTrue(rule.getMinWidth() >= 37, "LARGE box rules should have min width >= 37");
            assertTrue(rule.getMinHeight() >= 37, "LARGE box rules should have min height >= 37");
            assertTrue(rule.getMaxWidth() <= 43, "LARGE box rules should have max width <= 43");
            assertTrue(rule.getMaxHeight() <= 43, "LARGE box rules should have max height <= 43");
        }
    }

    @Test
    void testBoxRules_CrateBoxConstraints() {
        UserConstraints constraints = new UserConstraints();
        List<BoxRuleSpecification> boxRules = RuleProvider.getBoxRules(constraints);

        // Check CRATE box rules have correct constraints
        List<BoxRuleSpecification> crateRules = boxRules.stream()
            .filter(r -> r.getAllowedBoxType() == BoxType.CRATE)
            .toList();

        assertFalse(crateRules.isEmpty());
        
        // Check small art crate rules
        List<BoxRuleSpecification> smallCrateRules = crateRules.stream()
            .filter(r -> r.getMaxWidth() <= 33 && r.getMaxHeight() <= 33)
            .toList();
        assertFalse(smallCrateRules.isEmpty(), "Should have crate rules for small art");

        // Check large art crate rules
        List<BoxRuleSpecification> largeCrateRules = crateRules.stream()
            .filter(r -> r.getMinWidth() >= 34 && r.getMinHeight() >= 34)
            .toList();
        assertFalse(largeCrateRules.isEmpty(), "Should have crate rules for large art");
    }

    @Test
    void testContainerRules_GeneratedCorrectly() {
        List<ContainerRuleSpecification> containerRules = RuleProvider.getContainerRules();
        
        assertNotNull(containerRules);
        assertFalse(containerRules.isEmpty(), "Container rules should be defined");
        assertTrue(containerRules.size() >= 5, "Expected multiple container rules");
        
        // Check that all rules have required properties
        for (ContainerRuleSpecification rule : containerRules) {
            assertNotNull(rule.getDescription());
            assertNotNull(rule.getContainerType());
            assertNotNull(rule.getAllowedBoxType());
            assertTrue(rule.getCapacity() > 0);
        }
    }

    @Test
    void testContainerRules_ContainsAllContainerTypes() {
        List<ContainerRuleSpecification> containerRules = RuleProvider.getContainerRules();

        // Check that we have rules for all container types
        boolean hasStandardPallet = containerRules.stream()
            .anyMatch(r -> r.getContainerType() == ContainerType.STANDARD_PALLET);
        boolean hasOversizePallet = containerRules.stream()
            .anyMatch(r -> r.getContainerType() == ContainerType.OVERSIZE_PALLET);
        boolean hasStandardCrate = containerRules.stream()
            .anyMatch(r -> r.getContainerType() == ContainerType.STANDARD_CRATE);

        assertTrue(hasStandardPallet, "Should have rules for STANDARD_PALLET");
        assertTrue(hasOversizePallet, "Should have rules for OVERSIZE_PALLET");
        assertTrue(hasStandardCrate, "Should have rules for STANDARD_CRATE");
    }

    @Test
    void testContainerRules_StandardPalletRules() {
        List<ContainerRuleSpecification> containerRules = RuleProvider.getContainerRules();

        List<ContainerRuleSpecification> standardPalletRules = containerRules.stream()
            .filter(r -> r.getContainerType() == ContainerType.STANDARD_PALLET)
            .toList();

        assertEquals(2, standardPalletRules.size(), "Should have exactly 2 STANDARD_PALLET rules");
        
        // Check STANDARD box rule
        ContainerRuleSpecification standardBoxRule = standardPalletRules.stream()
            .filter(r -> r.getAllowedBoxType() == BoxType.STANDARD)
            .findFirst()
            .orElse(null);
        assertNotNull(standardBoxRule);
        assertEquals(4, standardBoxRule.getCapacity());

        // Check LARGE box rule
        ContainerRuleSpecification largeBoxRule = standardPalletRules.stream()
            .filter(r -> r.getAllowedBoxType() == BoxType.LARGE)
            .findFirst()
            .orElse(null);
        assertNotNull(largeBoxRule);
        assertEquals(3, largeBoxRule.getCapacity());
    }

    @Test
    void testContainerRules_OversizePalletRules() {
        List<ContainerRuleSpecification> containerRules = RuleProvider.getContainerRules();

        List<ContainerRuleSpecification> oversizePalletRules = containerRules.stream()
            .filter(r -> r.getContainerType() == ContainerType.OVERSIZE_PALLET)
            .toList();

        assertEquals(2, oversizePalletRules.size(), "Should have exactly 2 OVERSIZE_PALLET rules");
        
        // Check STANDARD box rule
        ContainerRuleSpecification standardBoxRule = oversizePalletRules.stream()
            .filter(r -> r.getAllowedBoxType() == BoxType.STANDARD)
            .findFirst()
            .orElse(null);
        assertNotNull(standardBoxRule);
        assertEquals(5, standardBoxRule.getCapacity());

        // Check LARGE box rule
        ContainerRuleSpecification largeBoxRule = oversizePalletRules.stream()
            .filter(r -> r.getAllowedBoxType() == BoxType.LARGE)
            .findFirst()
            .orElse(null);
        assertNotNull(largeBoxRule);
        assertEquals(3, largeBoxRule.getCapacity());
    }

    @Test
    void testContainerRules_StandardCrateRules() {
        List<ContainerRuleSpecification> containerRules = RuleProvider.getContainerRules();

        List<ContainerRuleSpecification> standardCrateRules = containerRules.stream()
            .filter(r -> r.getContainerType() == ContainerType.STANDARD_CRATE)
            .toList();

        assertEquals(1, standardCrateRules.size(), "Should have exactly 1 STANDARD_CRATE rule");
        
        ContainerRuleSpecification crateRule = standardCrateRules.get(0);
        assertEquals(BoxType.CRATE, crateRule.getAllowedBoxType());
        assertEquals(1, crateRule.getCapacity());
    }

    @Test
    void testBoxRules_WithDifferentConstraints() {
        // Test with default constraints
        UserConstraints defaultConstraints = new UserConstraints();
        List<BoxRuleSpecification> defaultRules = RuleProvider.getBoxRules(defaultConstraints);
        
        // Test with custom constraints (should return same rules for now)
        UserConstraints customConstraints = UserConstraints.newBuilder()
            .withSunriseFlag(true)
            .build();
        List<BoxRuleSpecification> customRules = RuleProvider.getBoxRules(customConstraints);
        
        // Currently, both should return the same rules since sunriseFlag is not implemented
        assertEquals(defaultRules.size(), customRules.size());
    }

    @Test
    void testBoxRules_Consistency() {
        UserConstraints constraints = new UserConstraints();
        
        // Multiple calls should return consistent results
        List<BoxRuleSpecification> rules1 = RuleProvider.getBoxRules(constraints);
        List<BoxRuleSpecification> rules2 = RuleProvider.getBoxRules(constraints);
        
        assertEquals(rules1.size(), rules2.size());
        
        // Check that the rules are identical
        for (int i = 0; i < rules1.size(); i++) {
            BoxRuleSpecification rule1 = rules1.get(i);
            BoxRuleSpecification rule2 = rules2.get(i);
            
            assertEquals(rule1.getDescription(), rule2.getDescription());
            assertEquals(rule1.getMaterial(), rule2.getMaterial());
            assertEquals(rule1.getAllowedBoxType(), rule2.getAllowedBoxType());
            assertEquals(rule1.getCapacity(), rule2.getCapacity());
        }
    }

    @Test
    void testContainerRules_Consistency() {
        // Multiple calls should return consistent results
        List<ContainerRuleSpecification> rules1 = RuleProvider.getContainerRules();
        List<ContainerRuleSpecification> rules2 = RuleProvider.getContainerRules();
        
        assertEquals(rules1.size(), rules2.size());
        
        // Check that the rules are identical
        for (int i = 0; i < rules1.size(); i++) {
            ContainerRuleSpecification rule1 = rules1.get(i);
            ContainerRuleSpecification rule2 = rules2.get(i);
            
            assertEquals(rule1.getDescription(), rule2.getDescription());
            assertEquals(rule1.getContainerType(), rule2.getContainerType());
            assertEquals(rule1.getAllowedBoxType(), rule2.getAllowedBoxType());
            assertEquals(rule1.getCapacity(), rule2.getCapacity());
        }
    }
}
