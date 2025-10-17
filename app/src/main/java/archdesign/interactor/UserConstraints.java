package archdesign.interactor;


import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.ContainerType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A parameter object that encapsulates dynamic, user-defined constraints for a single packing run.
 * This immutable object carries filtering preferences from the entry point (Packer) to the
 * core algorithm (primarily the FeasibilityService) to influence the outcome.
 */
public class UserConstraints {

    private final boolean sunriseFlag;
    /**
     * A whitelist of box types. If this list is not empty, only the box types
     * specified here will be considered for packing. An empty list implies no restriction.
     */
    private final List<BoxType> allowedBoxTypes;

    /**
     * A whitelist of container types. If this list is not empty, only the container types
     * specified here will be considered for packing. An empty list implies no restriction.
     */
    private final List<ContainerType> allowedContainerTypes;


    public UserConstraints() {
        this.sunriseFlag = false;
        // Set default allowed box types: STANDARD, LARGE, CRATE
        this.allowedBoxTypes = Arrays.asList(
            BoxType.STANDARD, 
            BoxType.LARGE, 
            BoxType.CRATE
        );
        // Set default allowed container types: STANDARD_PALLET, OVERSIZE_PALLET, STANDARD_CRATE
        this.allowedContainerTypes = Arrays.asList(
            ContainerType.STANDARD_PALLET,
            ContainerType.OVERSIZE_PALLET,
            ContainerType.STANDARD_CRATE
        );
    }

    /**
     * Private constructor to be used by the Builder.
     */
    private UserConstraints(Builder builder)
    {
        this.sunriseFlag = builder.sunriseFlag;
        this.allowedBoxTypes = builder.allowedBoxTypes;
        this.allowedContainerTypes = builder.allowedContainerTypes;
    }

    /**
     * Creates a new Builder instance to construct a UserConstraints object.
     * @return A new Builder.
     */
    public static Builder newBuilder()
    {
        return new Builder();
    }

    // --- Getters ---

    public boolean isSunriseFlag() {
        return sunriseFlag;
    }

    public List<BoxType> getAllowedBoxTypes() {
        return allowedBoxTypes;
    }

    public List<ContainerType> getAllowedContainerTypes() {
        return allowedContainerTypes;
    }


    // --- Builder Class ---

    /**
     * A fluent Builder for creating UserConstraints objects.
     * This pattern makes creating objects with optional parameters readable and safe.
     */
    public static class Builder {
        private boolean sunriseFlag = false;
        private List<BoxType> allowedBoxTypes = Collections.emptyList();
        private List<ContainerType> allowedContainerTypes = Collections.emptyList();

        private Builder() {}

        /**
         * Sets the "Sunrise" flag.
         * @param sunriseFlag true to enable the flag, false otherwise.
         * @return this builder for chaining.
         */
        public Builder withSunriseFlag(boolean sunriseFlag) {
            this.sunriseFlag = sunriseFlag;
            return this;
        }

        /**
         * Provides a whitelist of allowed box types.
         * @param allowedBoxTypes A list of BoxTypes. If empty or null, all types are allowed.
         * @return this builder for chaining.
         */
        public Builder withAllowedBoxTypes(List<BoxType> allowedBoxTypes) {
            this.allowedBoxTypes = (allowedBoxTypes != null) ? allowedBoxTypes : Collections.emptyList();
            return this;
        }

        /**
         * Provides a whitelist of allowed container types.
         * @param allowedContainerTypes A list of ContainerTypes. If empty or null, all types are allowed.
         * @return this builder for chaining.
         */
        public Builder withAllowedContainerTypes(List<ContainerType> allowedContainerTypes) {
            this.allowedContainerTypes = (allowedContainerTypes != null) ? allowedContainerTypes : Collections.emptyList();
            return this;
        }

        /**
         * Builds the final, immutable UserConstraints object.
         * @return A new instance of UserConstraints.
         */
        public UserConstraints build() {
            return new UserConstraints(this);
        }
    }
}