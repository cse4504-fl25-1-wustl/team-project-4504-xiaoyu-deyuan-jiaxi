package archdesign.response;

import archdesign.entities.Art;
import archdesign.entities.Box;
import archdesign.entities.Container;
import archdesign.interactor.PackingPlan; // Assuming PackingPlan is in interactor

import java.util.stream.Collectors;

/**
 * The Response layer that acts as a pure TRANSFORMER.
 * Its only responsibility is to convert the complex PackingPlan domain object
 * into a simple, easy-to-use ShipmentViewModel for any frontend to consume.
 */
public class Response {

    private final PackingPlan plan;

    public Response(PackingPlan plan) {
        this.plan = plan;
    }

    /**
     * Performs the conversion from the domain model to the view model.
     * This is the main public method for any frontend to get all display data.
     * @return A complete, simple, and read-only ShipmentViewModel.
     */
    public ShipmentViewModel generateViewModel() {
        if (plan == null) {
            return null; // Or an empty ViewModel
        }

        var containerVMs = plan.getContainers().stream()
                .map(this::convertContainerToViewModel)
                .collect(Collectors.toList());
        
        var unpackedArtVMs = plan.getUnpackedArts().stream()
                .map(this::convertArtToViewModel)
                .collect(Collectors.toList());
        
        int totalBoxes = containerVMs.stream()
                .mapToInt(cvm -> cvm.boxes().size())
                .sum();

        return new ShipmentViewModel(
            plan.getTotalWeight(),
            plan.getTotalCost(),
            plan.getContainers().size(),
            totalBoxes,
            containerVMs,
            unpackedArtVMs
        );
    }

    // --- Private Conversion Logic ---

    private ContainerViewModel convertContainerToViewModel(Container container) {
        var boxVMs = container.getBoxesInContainer().stream()
                .map(this::convertBoxToViewModel)
                .collect(Collectors.toList());

        return new ContainerViewModel(
            container.getId(),
            container.getContainerType().name(),
            container.getLength(),
            container.getWidth(),
            container.getCurrentHeight(),
            container.getTotalWeight(),
            boxVMs
        );
    }

    private BoxViewModel convertBoxToViewModel(Box box) {
        var artVMs = box.getArtsInBox().stream()
                .map(this::convertArtToViewModel)
                .collect(Collectors.toList());
        
        return new BoxViewModel(
            box.getId(),
            box.getBoxType().name(),
            box.getLength(),
            box.getWidth(),
            box.getCurrentHeight(),
            box.getTotalWeight(),
            artVMs
        );
    }

    /**
     * Converts an Art domain object to its corresponding ViewModel.
     * This now includes detailed information as requested.
     */
    private ArtViewModel convertArtToViewModel(Art art) {
        return new ArtViewModel(
            art.getId(),
            art.getWidth(),
            art.getHeight(),
            art.getMaterial().getDisplayName(), // Get the user-friendly name
            art.getWeight()
        );
    }
}