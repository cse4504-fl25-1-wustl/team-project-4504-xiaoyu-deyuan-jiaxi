package archdesign.response;

/**
 * A pure, read-only data object for Art display information.
 * It contains all relevant raw data for maximum frontend flexibility.
 */
public record ArtViewModel(
    String id,
    double width,
    double height,
    String material,
    double weight
) {
}