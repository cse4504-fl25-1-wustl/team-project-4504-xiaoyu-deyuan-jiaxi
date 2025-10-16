package response;

import java.util.List;

/**
 * The top-level, pure data object for a complete shipment plan.
 * This is the single object that any frontend will consume.
 */
public record ShipmentViewModel(
    double totalWeight,
    double totalCost,
    int totalContainers,
    int totalBoxes,
    List<ContainerViewModel> containers
) {
}