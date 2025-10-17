package response;
import java.util.List;

/**
 * A pure, read-only data object for Container display information.
 * It contains raw data for maximum frontend flexibility.
 */
public record ContainerViewModel(
    String id,
    String type,
    int length,
    int width,
    int currentHeight,
    double weight,
    List<BoxViewModel> boxes
) {
}