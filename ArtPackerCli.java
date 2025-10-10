import entities.Art;
import interactor.Packer;
import java.util.List;
import parser.CSVParser;
import responses.Response;

public class ArtPackerCli {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide the CSV file name as a command line argument.");
            return;
        }
        String fileName = args[0];

        // Parse CSV and create art objects
        CSVParser parser = new CSVParser();
        List<Art> arts = parser.parse(fileName);

        // Pack the items
        Packer packer = new Packer(arts);
        packer.pack();

        // Create response and display results
        Response response = new Response(packer);
        response.displayShipmentDetails();
    }
}