package archdesign;

import static org.junit.jupiter.api.Assertions.*;

import archdesign.output.ConsoleOutputFormatter;
import archdesign.response.ArtViewModel;
import archdesign.response.BoxViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.ShipmentViewModel;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

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
    @Test
    void displayViewModel_printsExpectedSummaryAndDetails() throws Exception {
        ArtViewModel art = new ArtViewModel("A1", 10, 20, "glass", 5.5);
        BoxViewModel box = new BoxViewModel("B1", "SMALL", 10, 10, 5, 6.5, List.of(art));
        ContainerViewModel container = new ContainerViewModel("C1", "STANDARD", 48, 40, 96, 12.0, List.of(box));

        ShipmentViewModel vm = new ShipmentViewModel(
            17.5, // totalWeight
            99.99, // totalCost
            1, // totalContainers
            1, // totalBoxes
            List.of(container),
            List.of() // no unpacked arts
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try {
            ConsoleOutputFormatter formatter = new ConsoleOutputFormatter();
            formatter.display(vm);
        } finally {
            System.setOut(originalOut);
        }

        String printed = out.toString();
        assertTrue(printed.contains("Total Estimated Cost: $99.99"), "should print total cost");
        assertTrue(printed.contains("-> Container: C1"), "should print container id");
        assertTrue(printed.contains("- Art: A1"), "should print art details");
    }
}
