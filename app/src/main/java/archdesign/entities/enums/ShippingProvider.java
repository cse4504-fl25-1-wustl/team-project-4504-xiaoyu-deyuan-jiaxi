package entities.enums;

/**
 * Defines the supported shipping providers.
 * This enum serves as a type-safe identifier (a "tag" or "label"). Its sole purpose
 * is to allow the StrategyProvider to select the correct, corresponding cost
 * calculation strategy class at runtime. It contains no business logic itself.
 */
public enum ShippingProvider {
    FEDEX,
    UPS,
    
    /**
     * A placeholder provider used for development and testing when final
     * cost rules are not yet defined.
     */
    PLACEHOLDER;
}