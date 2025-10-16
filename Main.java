import entities.Art;
import entities.Box;
import entities.Container;
import entities.enums.Material;
import entities.enums.ShippingProvider;
import interactor.*;


import java.util.ArrayList;
import java.util.List;

/**
 * A simple command-line application to run a test scenario for the Packer.
 * This class serves as the entry point for the application.
 */
public class Main {

    /**
     * The main entry point for the Java application.
     * @param args Command line arguments (not used in this test).
     */
    public static void main(String[] args) {
        System.out.println("--- Starting Packer Test ---");

        // 1. Create the input data: a list of 10 identical 30x30 Glass art pieces.
        List<Art> artsToPack = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            // Art constructor: (id, height, width, thickness, material)
            // The 'thickness' parameter is ignored in the Art constructor you provided.
            artsToPack.add(new Art("Art-" + i, 45, 45, 0, Material.GLASS));
        }
        System.out.println("Created " + artsToPack.size() + " pieces of 40x40 GLASS art to be packed.");

        // 2. Define the constraints and strategies for this packing run.
        // We'll use the default constraints.
        UserConstraints constraints = new UserConstraints();
        // We'll use the placeholder cost strategy for our prototype.
        ShippingProvider provider = ShippingProvider.PLACEHOLDER;

        // 3. Call the Packer's static pack method.
        System.out.println("\n--- Running Packer Algorithm... ---");
        PackingPlan finalPlan = Packer.pack(artsToPack, constraints, provider);
        System.out.println("--- Packer Algorithm Finished ---");

        // 4. Print the results from the returned PackingPlan.
        System.out.println("\n--- Packing Plan Results ---");
        printPackingPlan(finalPlan);
    }

    /**
     * A helper method to print the details of a PackingPlan in a human-readable format.
     * @param plan The PackingPlan to display.
     */
    private static void printPackingPlan(PackingPlan plan) {
        if (plan == null || plan.getContainers().isEmpty()) {
            System.out.println("No containers were used. The packing plan is empty.");
            return;
        }

        // Print Summary Metadata from the PackingPlan object.
        System.out.println("Total Estimated Cost: $" + String.format("%.2f", plan.getTotalCost()));
        System.out.println("Total Containers Used: " + plan.getContainers().size());

        int totalBoxes = 0;
        for (Container container : plan.getContainers()) {
            totalBoxes += container.getBoxesInContainer().size();
        }
        System.out.println("Total Boxes Used: " + totalBoxes);
        System.out.println("------------------------------------");

        // Print Hierarchical Details by iterating through the containers in the plan.
        for (Container container : plan.getContainers()) {
            System.out.println(
                "-> Container: " + container.getId() +
                " (Type: " + container.getContainerType() + ")" +
                " | Total Weight: " + String.format("%.2f", container.getTotalWeight()) + " lbs"
            );

            if (container.getBoxesInContainer().isEmpty()) {
                System.out.println("   (This container is empty)");
            } else {
                for (Box box : container.getBoxesInContainer()) {
                    System.out.println(
                        "   --> Box: " + box.getId() +
                        " (Type: " + box.getBoxType() + ")" +
                        " | Contains " + box.getArtsInBox().size() + " items"
                    );

                    for (Art art : box.getArtsInBox()) {
                        System.out.println("       - Art: " + art.getId());
                    }
                }
            }
            System.out.println(); // Add a blank line for readability between containers
        }
    }
}