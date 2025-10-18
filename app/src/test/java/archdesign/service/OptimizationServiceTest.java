package archdesign.service;

import java.awt.Container;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
            .thenReturn(50.0)  // First container
            .thenReturn(75.0); // Second container

        PackingPlan plan = optimizationService.createOptimalPlan(Arrays.asList(art1, art2), constraints);

        assertNotNull(plan);
        assertEquals(2, plan.getContainers().size());
        assertEquals(125.0, plan.getTotalCost()); // 50.0 + 75.0
    }




}