package archdesign.service;

import archdesign.entities.Art;
import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import archdesign.entities.enums.Material;
import archdesign.interactor.PackingPlan;
import archdesign.interactor.UserConstraints;
import archdesign.service.costing.ShippingCostStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.atLeast;

@ExtendWith(MockitoExtension.class)
class OptimizationServiceTest {

    @Mock
    private FeasibilityService feasibilityService;
    
    @Mock
    private ShippingCostStrategy costStrategy;
    
    private OptimizationService optimizationService;
    private UserConstraints constraints;
    
    @BeforeEach
    void setUp() {
        optimizationService = new OptimizationService(feasibilityService, costStrategy);
        constraints = new UserConstraints(/* initialize with test data */);
    }

    //test empty arts list
    @Test
    void createOptimalPlan_WithEmptyArtsList_ReturnsEmptyPlan() {
        List<Art> emptyArts = Collections.emptyList();

        PackingPlan plan = optimizationService.createOptimalPlan(emptyArts, constraints);

        assertNotNull(plan);
        assertTrue(plan.getContainers().isEmpty());
        assertEquals(0.0, plan.getTotalCost());
    }

    //test single art packing
    @Test
    void createOptimalPlan_WithSingleArt_PacksCorrectly(){
        // Use the correct Art constructor based on your actual Art class
        Art art = new Art("ART-1", 10, 10, 5, Material.GLASS);
        List<Art> arts = List.of(art);

        // Use actual BoxType enum values from your BoxType class
        PackingOption packingOption = new PackingOption(BoxType.STANDARD, 1);
        when(feasibilityService.getValidPackingOptions(eq(art), any(UserConstraints.class)))
            .thenReturn(List.of(packingOption));
        
        // Use actual ContainerType enum values - you'll need to check your ContainerType class
        ContainerOption containerOption = new ContainerOption(ContainerType.GLASS_PALLET, 10);
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(List.of(containerOption));

        when(costStrategy.calculateCost(any(archdesign.entities.Container.class))).thenReturn(50.0);

        PackingPlan plan = optimizationService.createOptimalPlan(arts, constraints);

        assertNotNull(plan);
        assertEquals(50.0, plan.getTotalCost());
        assertEquals(1, plan.getContainers().size());

        archdesign.entities.Container container = plan.getContainers().get(0);
        assertEquals(1, container.getBoxesInContainer().size());

        archdesign.entities.Box box = container.getBoxesInContainer().get(0);
        assertEquals(1, box.getArtsInBox().size());
        assertEquals(art, box.getArtsInBox().get(0));
    }

    //test multiple arts with same box type
    @Test
    void createOptimalPlan_WithMultipleArtsSameBoxType_PacksIntoSameBox() {
        Art art1 = new Art("ART-1", 10, 10, 5, Material.GLASS);
        Art art2 = new Art("ART-2", 10, 10, 5, Material.GLASS);
        List<Art> arts = Arrays.asList(art1, art2);

        PackingOption packingOption = new PackingOption(BoxType.STANDARD, 2);
        when(feasibilityService.getValidPackingOptions(any(Art.class), any(UserConstraints.class)))
            .thenReturn(List.of(packingOption));
        
        ContainerOption containerOption = new ContainerOption(ContainerType.GLASS_PALLET, 10);
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(List.of(containerOption));

        when(costStrategy.calculateCost(any(archdesign.entities.Container.class))).thenReturn(50.0);

        PackingPlan plan = optimizationService.createOptimalPlan(arts, constraints);

        assertNotNull(plan);
        assertEquals(1, plan.getContainers().size());
        
        archdesign.entities.Container container = plan.getContainers().get(0);
        assertEquals(1, container.getBoxesInContainer().size());
        
        archdesign.entities.Box box = container.getBoxesInContainer().get(0);
        assertEquals(2, box.getArtsInBox().size());
        assertTrue(box.getArtsInBox().contains(art1));
        assertTrue(box.getArtsInBox().contains(art2));
    }

    //test arts with different box types
    @Test
    void createOptimalPlan_WithMultipleArtsDifferentBoxTypes_CreatesSeparateBoxes() {
        Art smallArt = new Art("ART-SMALL", 5, 5, 2, Material.CANVAS_FRAMED);
        Art largeArt = new Art("ART-LARGE", 20, 20, 10, Material.GLASS);

        PackingOption smallOption = new PackingOption(BoxType.UPS_SMALL, 1);
        PackingOption largeOption = new PackingOption(BoxType.LARGE, 1);

        when(feasibilityService.getValidPackingOptions(smallArt, constraints))
            .thenReturn(List.of(smallOption));
        when(feasibilityService.getValidPackingOptions(largeArt, constraints))
            .thenReturn(List.of(largeOption));
        
        ContainerOption containerOption = new ContainerOption(ContainerType.GLASS_PALLET, 10);
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(List.of(containerOption));

        when(costStrategy.calculateCost(any(archdesign.entities.Container.class))).thenReturn(50.0);

        PackingPlan plan = optimizationService.createOptimalPlan(Arrays.asList(smallArt, largeArt), constraints);

        assertNotNull(plan);
        assertEquals(1, plan.getContainers().size());
        
        archdesign.entities.Container container = plan.getContainers().get(0);
        assertEquals(2, container.getBoxesInContainer().size());
        
        // Verify we have two boxes with different types
        archdesign.entities.Box box1 = container.getBoxesInContainer().get(0);
        archdesign.entities.Box box2 = container.getBoxesInContainer().get(1);
        
        assertNotEquals(box1.getBoxType(), box2.getBoxType());
        assertTrue(box1.getBoxType() == BoxType.UPS_SMALL || box1.getBoxType() == BoxType.LARGE);
        assertTrue(box2.getBoxType() == BoxType.UPS_SMALL || box2.getBoxType() == BoxType.LARGE);
    }


    //test unpackable art handling
    @Test
    void createOptimalPlan_WithUnpackableArt_ReturnsPlanWithoutUnpackableArt() {
        Art packableArt = new Art("ART-1", 10, 10, 5, Material.GLASS);
        Art unpackableArt = new Art("ART-UNPACKABLE", 100, 100, 100, Material.ACRYLIC);
        
        PackingOption packingOption = new PackingOption(BoxType.STANDARD, 1);
        when(feasibilityService.getValidPackingOptions(packableArt, constraints))
            .thenReturn(List.of(packingOption));
        when(feasibilityService.getValidPackingOptions(unpackableArt, constraints))
            .thenReturn(Collections.emptyList());
        
        ContainerOption containerOption = new ContainerOption(ContainerType.GLASS_PALLET, 10);
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(List.of(containerOption));

        when(costStrategy.calculateCost(any(archdesign.entities.Container.class))).thenReturn(50.0);

        PackingPlan plan = optimizationService.createOptimalPlan(Arrays.asList(packableArt, unpackableArt), constraints);

        assertNotNull(plan);
        assertEquals(1, plan.getContainers().size());
        
        archdesign.entities.Container container = plan.getContainers().get(0);
        archdesign.entities.Box box = container.getBoxesInContainer().get(0);
        
        // Should only contain the packable art
        assertEquals(1, box.getArtsInBox().size());
        assertEquals(packableArt, box.getArtsInBox().get(0));
        assertFalse(box.getArtsInBox().contains(unpackableArt));
    }

    //test container capacity enforcement
    @Test
    void createOptimalPlan_WhenBoxesExceedContainerCapacity_CreatesMultipleContainers() {
        Art art1 = new Art("ART-1", 10, 10, 5, Material.GLASS);
        Art art2 = new Art("ART-2", 10, 10, 5, Material.GLASS);
        Art art3 = new Art("ART-3", 10, 10, 5, Material.GLASS);
        
        // Force separate boxes by using capacity 1
        PackingOption packingOption = new PackingOption(BoxType.STANDARD, 1);
        when(feasibilityService.getValidPackingOptions(any(Art.class), any(UserConstraints.class)))
            .thenReturn(List.of(packingOption));
        
        // Force separate containers by using capacity 1
        ContainerOption containerOption = new ContainerOption(ContainerType.GLASS_PALLET, 1);
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(List.of(containerOption));

        when(costStrategy.calculateCost(any(archdesign.entities.Container.class))).thenReturn(50.0);

        PackingPlan plan = optimizationService.createOptimalPlan(Arrays.asList(art1, art2, art3), constraints);

        assertNotNull(plan);
       
        assertEquals(3, plan.getContainers().size());
        assertEquals(150.0, plan.getTotalCost()); 
    }

    //test total cost calculation
    @Test
    void createOptimalPlan_WithMultipleContainers_CalculatesCorrectTotalCost() {
        Art art1 = new Art("ART-1", 10, 10, 5, Material.GLASS);
        Art art2 = new Art("ART-2", 10, 10, 5, Material.GLASS);
        
        PackingOption packingOption = new PackingOption(BoxType.STANDARD, 1); // Force separate boxes
        when(feasibilityService.getValidPackingOptions(any(Art.class), any(UserConstraints.class)))
            .thenReturn(List.of(packingOption));
        
        ContainerOption containerOption = new ContainerOption(ContainerType.GLASS_PALLET, 1); // Force separate containers
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(List.of(containerOption));

        // Different costs for different containers
        when(costStrategy.calculateCost(any(archdesign.entities.Container.class)))
            .thenReturn(50.0)  
            .thenReturn(75.0); 

        PackingPlan plan = optimizationService.createOptimalPlan(Arrays.asList(art1, art2), constraints);

        assertNotNull(plan);
        assertEquals(2, plan.getContainers().size());
        assertEquals(125.0, plan.getTotalCost()); 
    }

    //edge case tests
    @Test
    void createOptimalPlan_WithNullArtsList_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            optimizationService.createOptimalPlan(null, constraints);
        });
    }

    // box capacity limit tests
    @Test
    void createOptimalPlan_WhenArtsExceedBoxCapacity_CreatesMultipleBoxes() {
        Art art1 = new Art("ART-1", 10, 10, 5, Material.GLASS);
        Art art2 = new Art("ART-2", 10, 10, 5, Material.GLASS);
        Art art3 = new Art("ART-3", 10, 10, 5, Material.GLASS);
        
        
        PackingOption packingOption = new PackingOption(BoxType.STANDARD, 2);
        when(feasibilityService.getValidPackingOptions(any(Art.class), any(UserConstraints.class)))
            .thenReturn(List.of(packingOption));
        
        ContainerOption containerOption = new ContainerOption(ContainerType.GLASS_PALLET, 10);
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(List.of(containerOption));

        when(costStrategy.calculateCost(any(archdesign.entities.Container.class))).thenReturn(50.0);

        PackingPlan plan = optimizationService.createOptimalPlan(Arrays.asList(art1, art2, art3), constraints);

        assertNotNull(plan);
        assertEquals(1, plan.getContainers().size());
        
        archdesign.entities.Container container = plan.getContainers().get(0);
        assertEquals(2, container.getBoxesInContainer().size());
        
        // Verify box distribution (one box with 2 arts, one with 1 art)
        int totalArts = container.getBoxesInContainer().stream()
            .mapToInt(box -> box.getArtsInBox().size())
            .sum();
        assertEquals(3, totalArts);
    }

    // mixed capcacity arts test
    @Test
    void createOptimalPlan_WithMixedCapacityArts_RespectsCapacityRules() {
        Art highCapacityArt = new Art("ART-HIGH", 15, 15, 8, Material.GLASS);
        Art lowCapacityArt = new Art("ART-LOW", 5, 5, 2, Material.CANVAS_FRAMED);
        
        
        PackingOption highCapacityOption = new PackingOption(BoxType.STANDARD, 1);
        PackingOption lowCapacityOption = new PackingOption(BoxType.STANDARD, 2);
        
        when(feasibilityService.getValidPackingOptions(highCapacityArt, constraints))
            .thenReturn(List.of(highCapacityOption));
        when(feasibilityService.getValidPackingOptions(lowCapacityArt, constraints))
            .thenReturn(List.of(lowCapacityOption));
        
        ContainerOption containerOption = new ContainerOption(ContainerType.GLASS_PALLET, 10);
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(List.of(containerOption));

        when(costStrategy.calculateCost(any(archdesign.entities.Container.class))).thenReturn(50.0);

        PackingPlan plan = optimizationService.createOptimalPlan(Arrays.asList(highCapacityArt, lowCapacityArt), constraints);

        assertNotNull(plan);
        assertEquals(1, plan.getContainers().size());
        
        archdesign.entities.Container container = plan.getContainers().get(0);
        
        assertEquals(2, container.getBoxesInContainer().size());
    }

    //container type selection test
    @Test
    void createOptimalPlan_WithMultipleContainerOptions_SelectsFirstValidOption() {
        Art art = new Art("ART-1", 10, 10, 5, Material.GLASS);
        
        PackingOption packingOption = new PackingOption(BoxType.STANDARD, 1);
        when(feasibilityService.getValidPackingOptions(eq(art), any(UserConstraints.class)))
            .thenReturn(List.of(packingOption));
        
       
        ContainerOption option1 = new ContainerOption(ContainerType.GLASS_PALLET, 5);
        ContainerOption option2 = new ContainerOption(ContainerType.STANDARD_PALLET, 10);
        ContainerOption option3 = new ContainerOption(ContainerType.STANDARD_CRATE, 8);
        
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(Arrays.asList(option1, option2, option3));

        when(costStrategy.calculateCost(any(archdesign.entities.Container.class))).thenReturn(50.0);

        PackingPlan plan = optimizationService.createOptimalPlan(List.of(art), constraints);

        assertNotNull(plan);
        
        assertEquals(ContainerType.GLASS_PALLET, plan.getContainers().get(0).getContainerType());
    }

    //id generation test
    @Test
    void createOptimalPlan_MultipleCalls_GeneratesUniqueIds() {
        Art art = new Art("ART-1", 10, 10, 5, Material.GLASS);
        
        PackingOption packingOption = new PackingOption(BoxType.STANDARD, 1);
        when(feasibilityService.getValidPackingOptions(any(Art.class), any(UserConstraints.class)))
            .thenReturn(List.of(packingOption));
        
        ContainerOption containerOption = new ContainerOption(ContainerType.GLASS_PALLET, 10);
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(List.of(containerOption));

        when(costStrategy.calculateCost(any(archdesign.entities.Container.class))).thenReturn(50.0);

        // First call
        PackingPlan plan1 = optimizationService.createOptimalPlan(List.of(art), constraints);
        archdesign.entities.Container container1 = plan1.getContainers().get(0);
        archdesign.entities.Box box1 = container1.getBoxesInContainer().get(0);

        // Second call
        PackingPlan plan2 = optimizationService.createOptimalPlan(List.of(art), constraints);
        archdesign.entities.Container container2 = plan2.getContainers().get(0);
        archdesign.entities.Box box2 = container2.getBoxesInContainer().get(0);

        // Verify IDs are unique and follow expected patterns
        assertNotEquals(box1.getId(), box2.getId());
        assertNotEquals(container1.getId(), container2.getId());
        assertTrue(box1.getId().startsWith("Box-"));
        assertTrue(box2.getId().startsWith("Box-"));
        assertTrue(container1.getId().startsWith("Container-"));
        assertTrue(container2.getId().startsWith("Container-"));
    }

    //large scale test
    @Test
    void createOptimalPlan_WithLargeNumberOfArts_CompletesSuccessfully() {
        // Create 10 identical arts
        List<Art> arts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            arts.add(new Art("ART-" + i, 10, 10, 5, Material.GLASS));
        }

        PackingOption packingOption = new PackingOption(BoxType.STANDARD, 5); 
        when(feasibilityService.getValidPackingOptions(any(Art.class), any(UserConstraints.class)))
            .thenReturn(List.of(packingOption));
        
        ContainerOption containerOption = new ContainerOption(ContainerType.GLASS_PALLET, 3); 
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(List.of(containerOption));

        when(costStrategy.calculateCost(any(archdesign.entities.Container.class))).thenReturn(100.0);

        PackingPlan plan = optimizationService.createOptimalPlan(arts, constraints);

        assertNotNull(plan);
        
        assertEquals(1, plan.getContainers().size());
        
        archdesign.entities.Container container = plan.getContainers().get(0);
        assertEquals(2, container.getBoxesInContainer().size());
        
        
        int totalPackedArts = container.getBoxesInContainer().stream()
            .mapToInt(box -> box.getArtsInBox().size())
            .sum();
        assertEquals(10, totalPackedArts);
    }

    //verify service interactions
    @Test
    void createOptimalPlan_VerifiesServiceInteractions() {
        Art art1 = new Art("ART-1", 10, 10, 5, Material.GLASS);
        Art art2 = new Art("ART-2", 10, 10, 5, Material.GLASS);
        
        PackingOption packingOption = new PackingOption(BoxType.STANDARD, 2);
        when(feasibilityService.getValidPackingOptions(any(Art.class), any(UserConstraints.class)))
            .thenReturn(List.of(packingOption));
        
        ContainerOption containerOption = new ContainerOption(ContainerType.GLASS_PALLET, 10);
        when(feasibilityService.getValidContainerOptions(any(archdesign.entities.Box.class), any(UserConstraints.class)))
            .thenReturn(List.of(containerOption));

        when(costStrategy.calculateCost(any(archdesign.entities.Container.class))).thenReturn(50.0);

        PackingPlan plan = optimizationService.createOptimalPlan(Arrays.asList(art1, art2), constraints);

        assertNotNull(plan);
        
        verify(feasibilityService, atLeast(2)).getValidPackingOptions(any(Art.class), eq(constraints));
        verify(feasibilityService, times(1)).getValidContainerOptions(any(archdesign.entities.Box.class), eq(constraints));
        verify(costStrategy, times(1)).calculateCost(any(archdesign.entities.Container.class));
    }

}