package archdesign.e2e;

import archdesign.Main;
import archdesign.e2e.testutils.ViewModelCsv;
import archdesign.response.ShipmentViewModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class GenerateGolden {

    private Path resourcePath(String name) throws URISyntaxException {
        URL res = this.getClass().getClassLoader().getResource("e2e/" + name);
        assertNotNull(res, "Test resource not found: " + name);
        return Path.of(res.toURI());
    }

    @Disabled("Dev helper to regenerate golden CSV; disabled in CI")
    @Test
    public void printSmallSampleCsv() throws Exception {
        Path p = resourcePath("sample_input_small.csv");
        ShipmentViewModel vm = Main.processFile(p.toString());
        String csv = ViewModelCsv.toCsv(vm);
        // Print to stdout so maintainers can capture and save as golden file
        System.out.println(csv);
        assertNotNull(csv);
    }
}
