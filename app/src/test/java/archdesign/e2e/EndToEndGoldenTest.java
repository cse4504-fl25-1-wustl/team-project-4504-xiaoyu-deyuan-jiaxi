package archdesign.e2e;

import archdesign.Main;
import archdesign.e2e.testutils.ViewModelCsv;
import archdesign.response.ShipmentViewModel;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class EndToEndGoldenTest {

    private Path resourcePath(String name) throws URISyntaxException {
        URL res = this.getClass().getClassLoader().getResource("e2e/" + name);
        assertNotNull(res, "Test resource not found: " + name);
        return Path.of(res.toURI());
    }

    @Test
    public void golden_smallSample_matchesExpectedJson() throws Exception {
        Path input = resourcePath("sample_input_small.csv");
        ShipmentViewModel vm = Main.processFile(input.toString());
    String actual = ViewModelCsv.toCsv(vm).trim();

    URL goldenUrl = this.getClass().getClassLoader().getResource("e2e/golden/sample_input_small_golden.csv");
        assertNotNull(goldenUrl, "Golden file missing");
    String expected = Files.readString(Path.of(goldenUrl.toURI()));

    // Normalize newlines to LF and trim to avoid platform-dependent CRLF vs LF mismatches
    String normExpected = expected.replace("\r\n", "\n").replace("\r", "\n").trim();
    String normActual = actual.replace("\r\n", "\n").replace("\r", "\n").trim();

    assertEquals(normExpected, normActual);
    }
}
