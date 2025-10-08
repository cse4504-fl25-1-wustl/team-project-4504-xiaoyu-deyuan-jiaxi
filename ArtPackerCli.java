// No package declaration since it's in the root source folder

import entities.Art;
import interactor.Packer;
import java.util.ArrayList;
import java.util.List;
import parser.CSVParser;
import requests.Request;
import responses.ArtInfo;
import responses.BoxInfo;
import responses.ContainerInfo;
import responses.Response;

public class ArtPackerCli { // <-- Changed from HelloWorld to ArtPackerCli
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide the CSV file name as a command line argument.");
            return;
        }
        String fileName = args[0];

        CSVParser parser = new CSVParser();
        List<Art> arts = parser.parse(fileName);

        Request packRequest = new Request(arts, true);

        Packer packer = new Packer(packRequest.getArtObjects());
        packer.pack();

        // Create the response object
        List<ArtInfo> itemInfos = new ArrayList<>();
        for (Art art : packer.getArtsToPack()) {
            itemInfos.add(new ArtInfo(art.getId(), art.getWeight()));
        }

        List<BoxInfo> boxInfos = new ArrayList<>();
        packer.getBoxesUsed().forEach(box -> {
            List<String> artIds = new ArrayList<>();
            box.getArtsInBox().forEach(art -> artIds.add(art.getId()));
            boxInfos.add(new BoxInfo(box.getId(), artIds, box.getWeight()));
        });

        List<ContainerInfo> containerInfos = new ArrayList<>();
        packer.getContainersUsed().forEach(container -> {
            List<String> boxIds = new ArrayList<>();
            container.getBoxInContainer().forEach(box -> boxIds.add(box.getId()));
            String type = container.isCrate() ? "Crate" : "Pallet";
            containerInfos.add(new ContainerInfo(container.getId(), boxIds, container.getTotalWeight(), 0, type));
        });

        float totalWeight = 0;
        for(ContainerInfo container : containerInfos) {
            totalWeight += container.getWeight();
        }

        Response response = new Response(itemInfos, boxInfos, containerInfos, totalWeight);

        // Dump the response to the terminal
        System.out.println("--- Shipment Details ---");
        System.out.println("Total Weight: " + response.getTotalWeight());
        System.out.println("\n--- Items ---");
        response.getArts().forEach(item -> {
            System.out.println("ID: " + item.getId() + ", Weight: " + item.getWeight());
        });
        System.out.println("\n--- Boxes ---");
        response.getBoxes().forEach(box -> {
            System.out.println("ID: " + box.getId() + ", Weight: " + box.getWeight());
            System.out.println("  Items: " + String.join(", ", box.getItemIds()));
        });
        System.out.println("\n--- Containers ---");
        response.getContainers().forEach(container -> {
            System.out.println("ID: " + container.getId() + ", Type: " + container.getType() + ", Weight: " + container.getWeight() + ", Height: " + container.getHeight());
            System.out.println("  Boxes: " + String.join(", ", container.getBoxIds()));
        });
    }
}