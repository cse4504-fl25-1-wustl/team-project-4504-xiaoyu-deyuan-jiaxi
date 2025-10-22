package archdesign;

import static org.junit.jupiter.api.Assertions.*;

import archdesign.response.ShipmentViewModel;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void processFile_withSmallSample_returnsViewModel() throws Exception {
        String resource = "src/test/resources/e2e/sample_input_small.csv";
        ShipmentViewModel vm = Main.processFile(resource);
        assertNotNull(vm);
    assertTrue(vm.totalContainers() >= 0);
    assertTrue(vm.totalBoxes() >= 0);
    // At least one art should be present in the small sample
    assertTrue(vm.containers().stream().flatMap(c -> c.boxes().stream()).flatMap(b -> b.arts().stream()).count() > 0);
    }

    @Test
    void main_noArgs_printsUsage() {
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
        try {
            Main.main(new String[0]);
            String stdout = out.toString();
            String stderr = err.toString();
            // Expect usage text in either stdout or stderr
            assertTrue(!stdout.isBlank() || !stderr.isBlank());
        } finally {
            System.setOut(origOut);
            System.setErr(origErr);
        }
    }
}
