package config.spec;

import entities.enums.BoxType;
import entities.enums.ContainerType;

/**
 * Represents a single, immutable rule for packing a Box into a Container.
 * Instances are created using the fluent Builder pattern.
 */
public class ContainerRuleSpecification {

    // --- Rule Conditions ---
    private final String description;
    private final ContainerType containerType;

    // --- Rule Results ---
    private final BoxType allowedBoxType;
    private final int capacity; // How many boxes of the allowed type fit in this container

    private ContainerRuleSpecification(Builder builder) {
        this.description = builder.description;
        this.containerType = builder.containerType;
        this.allowedBoxType = builder.allowedBoxType;
        this.capacity = builder.capacity;
    }

    public static Builder newBuilder(String description) {
        return new Builder(description);
    }

    // --- Public Getters ---
    public String getDescription() { return description; }
    public ContainerType getContainerType() { return containerType; }
    public BoxType getAllowedBoxType() { return allowedBoxType; }
    public int getCapacity() { return capacity; }

    // --- Builder Class ---
    public static class Builder {
        private final String description;
        private ContainerType containerType;
        private BoxType allowedBoxType;
        private int capacity;

        public Builder(String description) {
            this.description = description;
        }

        public Builder forContainerType(ContainerType containerType) {
            this.containerType = containerType;
            return this;
        }

        public Builder withAllowedBoxType(BoxType boxType) {
            this.allowedBoxType = boxType;
            return this;
        }

        public Builder withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public ContainerRuleSpecification build() {
            return new ContainerRuleSpecification(this);
        }
    }
}