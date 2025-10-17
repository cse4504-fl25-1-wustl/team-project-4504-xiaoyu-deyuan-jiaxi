package archdesign.response;

import java.util.List;

/**
 * A pure, read-only data object for Box display information.
 * It contains raw data for maximum frontend flexibility.
 */
public record BoxViewModel(
    String id,
    String type,
    int length,
    int width,
    int currentHeight,
    double weight,
    List<ArtViewModel> arts
) {
}