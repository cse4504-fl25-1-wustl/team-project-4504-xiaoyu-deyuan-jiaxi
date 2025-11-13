package archdesign.config.spec;

import archdesign.entities.enums.BoxType;
import archdesign.entities.enums.Material;

/**
 * Represents a single, immutable rule for packing an Art object into a Box.
 * This class is a pure data object (or DTO) that holds the conditions and
 * results of a rule. Instances are created using the fluent Builder pattern.
 */
public class BoxRuleSpecification {

    // --- Rule Conditions (The "IF" part) ---
    private final String description;
    private final Material material; // Rule applies to this material (null means any material)
    private final double minWidth;
    private final double maxWidth;
    private final double minHeight;
    private final double maxHeight;

    // --- Rule Results (The "THEN" part) ---
    private final BoxType allowedBoxType;
    private final int capacity; // How many pieces of this Art can fit in the allowed BoxType

    /**
     * Private constructor to enforce object creation via the Builder.
     * @param builder The builder instance containing all the rule's properties.
     */
    private BoxRuleSpecification(Builder builder) {
        this.description = builder.description;
        this.material = builder.material;
        this.minWidth = builder.minWidth;
        this.maxWidth = builder.maxWidth;
        this.minHeight = builder.minHeight;
        this.maxHeight = builder.maxHeight;
        this.allowedBoxType = builder.allowedBoxType;
        this.capacity = builder.capacity;
    }

    /**
     * Static factory method to start the building process.
     * A rule must have a description, an outcome (box type), and a capacity.
     * All other conditions are optional.
     */
    public static Builder newBuilder(String description, BoxType allowedBoxType, int capacity) {
        return new Builder(description, allowedBoxType, capacity);
    }

    // --- Public Getters ---
    // These are used by the FeasibilityService to check if an Art object matches the rule.

    public String getDescription() { return description; }
    public Material getMaterial() { return material; }
    public double getMinWidth() { return minWidth; }
    public double getMaxWidth() { return maxWidth; }
    public double getMinHeight() { return minHeight; }
    public double getMaxHeight() { return maxHeight; }
    public BoxType getAllowedBoxType() { return allowedBoxType; }
    public int getCapacity() { return capacity; }


    // --- Builder Class ---

    /**
     * A fluent Builder for creating BoxRuleSpecification objects in a readable way.
     */
    public static class Builder {
        // Required parameters
        private final String description;
        private final BoxType allowedBoxType;
        private final int capacity;

        // Optional parameters with default values
        private Material material = null; // Default to all materials
        private double minWidth = 0.0;
        private double maxWidth = Double.MAX_VALUE;
        private double minHeight = 0.0;
        private double maxHeight = Double.MAX_VALUE;

        /**
         * The builder's constructor takes the mandatory rule parameters.
         */
        public Builder(String description, BoxType allowedBoxType, int capacity) {
            this.description = description;
            this.allowedBoxType = allowedBoxType;
            this.capacity = capacity;
        }

        /**
         * Sets the material condition for this rule. If not called, the rule applies to any material.
         * @param material The specific material this rule applies to.
         * @return this builder for chaining.
         */
        public Builder forMaterial(Material material) {
            this.material = material;
            return this;
        }

        /**
         * Sets the minimum width condition (inclusive).
         * @param minWidth The minimum width an Art must have to match this rule.
         * @return this builder for chaining.
         */
        public Builder withMinWidth(double minWidth) {
            this.minWidth = minWidth;
            return this;
        }

        /**
         * Sets the maximum width condition (inclusive).
         * @param maxWidth The maximum width an Art can have to match this rule.
         * @return this builder for chaining.
         */
        public Builder withMaxWidth(double maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        /**
         * Sets the minimum height condition (inclusive).
         * @param minHeight The minimum height an Art must have to match this rule.
         * @return this builder for chaining.
         */
        public Builder withMinHeight(double minHeight) {
            this.minHeight = minHeight;
            return this;
        }

        /**
         * Sets the maximum height condition (inclusive).
         * @param maxHeight The maximum height an Art can have to match this rule.
         * @return this builder for chaining.
         */
        public Builder withMaxHeight(double maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        /**
         * Builds the final, immutable BoxRuleSpecification object.
         * @return A new instance of BoxRuleSpecification.
         */
        public BoxRuleSpecification build() {
            return new BoxRuleSpecification(this);
        }
    }
}