package archdesign.interactor;

import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserConstraintsTest {

    @Test
    void testDefaultConstructor() {
        UserConstraints constraints = new UserConstraints();

        assertFalse(constraints.isSunriseFlag());
        assertNotNull(constraints.getAllowedBoxTypes());
        assertNotNull(constraints.getAllowedContainerTypes());
        
        // Check default box types
        List<BoxType> expectedBoxTypes = Arrays.asList(
            BoxType.STANDARD, 
            BoxType.LARGE, 
            BoxType.CRATE
        );
        assertEquals(expectedBoxTypes, constraints.getAllowedBoxTypes());
        
        // Check default container types
        List<ContainerType> expectedContainerTypes = Arrays.asList(
            ContainerType.STANDARD_PALLET,
            ContainerType.OVERSIZE_PALLET,
            ContainerType.STANDARD_CRATE
        );
        assertEquals(expectedContainerTypes, constraints.getAllowedContainerTypes());
    }

    @Test
    void testBuilder_DefaultValues() {
        UserConstraints constraints = UserConstraints.newBuilder().build();

        assertFalse(constraints.isSunriseFlag());
        assertTrue(constraints.getAllowedBoxTypes().isEmpty());
        assertTrue(constraints.getAllowedContainerTypes().isEmpty());
    }

    @Test
    void testBuilder_WithSunriseFlag() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withSunriseFlag(true)
            .build();

        assertTrue(constraints.isSunriseFlag());
    }

    @Test
    void testBuilder_WithSunriseFlagFalse() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withSunriseFlag(false)
            .build();

        assertFalse(constraints.isSunriseFlag());
    }

    @Test
    void testBuilder_WithAllowedBoxTypes() {
        List<BoxType> allowedBoxTypes = Arrays.asList(BoxType.STANDARD, BoxType.LARGE);
        
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedBoxTypes(allowedBoxTypes)
            .build();

        assertEquals(allowedBoxTypes, constraints.getAllowedBoxTypes());
    }

    @Test
    void testBuilder_WithAllowedBoxTypesEmpty() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedBoxTypes(Collections.emptyList())
            .build();

        assertTrue(constraints.getAllowedBoxTypes().isEmpty());
    }

    @Test
    void testBuilder_WithAllowedBoxTypesNull() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedBoxTypes(null)
            .build();

        assertTrue(constraints.getAllowedBoxTypes().isEmpty());
    }

    @Test
    void testBuilder_WithAllowedContainerTypes() {
        List<ContainerType> allowedContainerTypes = Arrays.asList(
            ContainerType.STANDARD_PALLET, 
            ContainerType.OVERSIZE_PALLET
        );
        
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedContainerTypes(allowedContainerTypes)
            .build();

        assertEquals(allowedContainerTypes, constraints.getAllowedContainerTypes());
    }

    @Test
    void testBuilder_WithAllowedContainerTypesEmpty() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedContainerTypes(Collections.emptyList())
            .build();

        assertTrue(constraints.getAllowedContainerTypes().isEmpty());
    }

    @Test
    void testBuilder_WithAllowedContainerTypesNull() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedContainerTypes(null)
            .build();

        assertTrue(constraints.getAllowedContainerTypes().isEmpty());
    }

    @Test
    void testBuilder_Chaining() {
        List<BoxType> allowedBoxTypes = Arrays.asList(BoxType.STANDARD);
        List<ContainerType> allowedContainerTypes = Arrays.asList(ContainerType.STANDARD_PALLET);
        
        UserConstraints constraints = UserConstraints.newBuilder()
            .withSunriseFlag(true)
            .withAllowedBoxTypes(allowedBoxTypes)
            .withAllowedContainerTypes(allowedContainerTypes)
            .build();

        assertTrue(constraints.isSunriseFlag());
        assertEquals(allowedBoxTypes, constraints.getAllowedBoxTypes());
        assertEquals(allowedContainerTypes, constraints.getAllowedContainerTypes());
    }

    @Test
    void testBuilder_AllBoxTypes() {
        List<BoxType> allBoxTypes = Arrays.asList(
            BoxType.STANDARD, 
            BoxType.LARGE, 
            BoxType.UPS_SMALL, 
            BoxType.UPS_LARGE, 
            BoxType.CRATE
        );
        
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedBoxTypes(allBoxTypes)
            .build();

        assertEquals(allBoxTypes, constraints.getAllowedBoxTypes());
    }

    @Test
    void testBuilder_AllContainerTypes() {
        List<ContainerType> allContainerTypes = Arrays.asList(
            ContainerType.STANDARD_PALLET,
            ContainerType.GLASS_PALLET,
            ContainerType.OVERSIZE_PALLET,
            ContainerType.STANDARD_CRATE
        );
        
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedContainerTypes(allContainerTypes)
            .build();

        assertEquals(allContainerTypes, constraints.getAllowedContainerTypes());
    }

    @Test
    void testBuilder_SingleBoxType() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedBoxTypes(Arrays.asList(BoxType.CRATE))
            .build();

        assertEquals(1, constraints.getAllowedBoxTypes().size());
        assertEquals(BoxType.CRATE, constraints.getAllowedBoxTypes().get(0));
    }

    @Test
    void testBuilder_SingleContainerType() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withAllowedContainerTypes(Arrays.asList(ContainerType.STANDARD_CRATE))
            .build();

        assertEquals(1, constraints.getAllowedContainerTypes().size());
        assertEquals(ContainerType.STANDARD_CRATE, constraints.getAllowedContainerTypes().get(0));
    }

    @Test
    void testBuilder_RepeatedCalls() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withSunriseFlag(false)
            .withSunriseFlag(true)
            .withAllowedBoxTypes(Arrays.asList(BoxType.STANDARD))
            .withAllowedBoxTypes(Arrays.asList(BoxType.LARGE))
            .withAllowedContainerTypes(Arrays.asList(ContainerType.STANDARD_PALLET))
            .withAllowedContainerTypes(Arrays.asList(ContainerType.OVERSIZE_PALLET))
            .build();

        assertTrue(constraints.isSunriseFlag());
        assertEquals(Arrays.asList(BoxType.LARGE), constraints.getAllowedBoxTypes());
        assertEquals(Arrays.asList(ContainerType.OVERSIZE_PALLET), constraints.getAllowedContainerTypes());
    }

    @Test
    void testConsistency_MultipleGetters() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withSunriseFlag(true)
            .withAllowedBoxTypes(Arrays.asList(BoxType.STANDARD))
            .withAllowedContainerTypes(Arrays.asList(ContainerType.STANDARD_PALLET))
            .build();

        // Multiple calls should return consistent values
        assertTrue(constraints.isSunriseFlag());
        assertTrue(constraints.isSunriseFlag());
        
        assertEquals(1, constraints.getAllowedBoxTypes().size());
        assertEquals(1, constraints.getAllowedBoxTypes().size());
        
        assertEquals(1, constraints.getAllowedContainerTypes().size());
        assertEquals(1, constraints.getAllowedContainerTypes().size());
    }

    @Test
    void testToString_ReturnsNonNull() {
        UserConstraints constraints = new UserConstraints();
        String stringRepresentation = constraints.toString();
        
        assertNotNull(stringRepresentation);
        assertFalse(stringRepresentation.isEmpty());
    }

    @Test
    void testBuilderToString_ReturnsNonNull() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withSunriseFlag(true)
            .withAllowedBoxTypes(Arrays.asList(BoxType.STANDARD))
            .withAllowedContainerTypes(Arrays.asList(ContainerType.STANDARD_PALLET))
            .build();
        
        String stringRepresentation = constraints.toString();
        
        assertNotNull(stringRepresentation);
        assertFalse(stringRepresentation.isEmpty());
    }

    @Test
    void testEquality_DefaultConstraints() {
        UserConstraints constraints1 = new UserConstraints();
        UserConstraints constraints2 = new UserConstraints();

        // Should have same values
        assertEquals(constraints1.isSunriseFlag(), constraints2.isSunriseFlag());
        assertEquals(constraints1.getAllowedBoxTypes(), constraints2.getAllowedBoxTypes());
        assertEquals(constraints1.getAllowedContainerTypes(), constraints2.getAllowedContainerTypes());
    }

    @Test
    void testEquality_BuilderConstraints() {
        UserConstraints constraints1 = UserConstraints.newBuilder()
            .withSunriseFlag(true)
            .withAllowedBoxTypes(Arrays.asList(BoxType.STANDARD))
            .withAllowedContainerTypes(Arrays.asList(ContainerType.STANDARD_PALLET))
            .build();
            
        UserConstraints constraints2 = UserConstraints.newBuilder()
            .withSunriseFlag(true)
            .withAllowedBoxTypes(Arrays.asList(BoxType.STANDARD))
            .withAllowedContainerTypes(Arrays.asList(ContainerType.STANDARD_PALLET))
            .build();

        // Should have same values
        assertEquals(constraints1.isSunriseFlag(), constraints2.isSunriseFlag());
        assertEquals(constraints1.getAllowedBoxTypes(), constraints2.getAllowedBoxTypes());
        assertEquals(constraints1.getAllowedContainerTypes(), constraints2.getAllowedContainerTypes());
    }

    @Test
    void testComplexScenario() {
        UserConstraints constraints = UserConstraints.newBuilder()
            .withSunriseFlag(true)
            .withAllowedBoxTypes(Arrays.asList(BoxType.STANDARD, BoxType.LARGE))
            .withAllowedContainerTypes(Arrays.asList(
                ContainerType.STANDARD_PALLET, 
                ContainerType.OVERSIZE_PALLET
            ))
            .build();

        assertTrue(constraints.isSunriseFlag());
        assertEquals(2, constraints.getAllowedBoxTypes().size());
        assertTrue(constraints.getAllowedBoxTypes().contains(BoxType.STANDARD));
        assertTrue(constraints.getAllowedBoxTypes().contains(BoxType.LARGE));
        
        assertEquals(2, constraints.getAllowedContainerTypes().size());
        assertTrue(constraints.getAllowedContainerTypes().contains(ContainerType.STANDARD_PALLET));
        assertTrue(constraints.getAllowedContainerTypes().contains(ContainerType.OVERSIZE_PALLET));
    }

    @Test
    void testEmptyConstraints() {
        UserConstraints constraints = UserConstraints.newBuilder().build();

        assertFalse(constraints.isSunriseFlag());
        assertTrue(constraints.getAllowedBoxTypes().isEmpty());
        assertTrue(constraints.getAllowedContainerTypes().isEmpty());
    }

    @Test
    void testBuilder_NoCalls() {
        UserConstraints constraints = UserConstraints.newBuilder().build();

        assertFalse(constraints.isSunriseFlag());
        assertTrue(constraints.getAllowedBoxTypes().isEmpty());
        assertTrue(constraints.getAllowedContainerTypes().isEmpty());
    }
}
