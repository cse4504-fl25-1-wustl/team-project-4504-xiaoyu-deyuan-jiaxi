package archdesign.e2e.testutils;

import archdesign.response.ArtViewModel;
import archdesign.response.BoxViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.ShipmentViewModel;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Test helper to convert ShipmentViewModel into a canonical CSV representation.
 * The CSV format chosen is simple and deterministic: one row per art with containerId,boxId,artId,width,height,material,weight
 */
public final class ViewModelCsv {
    private ViewModelCsv() {}

    public static String toCsv(ShipmentViewModel vm) {
        String header = "containerId,boxId,artId,width,height,material,weight";

        return header + "\n" + vm.containers().stream()
                // sort containers by id to be deterministic
                .sorted(Comparator.comparing(ContainerViewModel::id))
                .flatMap(c -> c.boxes().stream()
                        .sorted(Comparator.comparing(BoxViewModel::id))
                        .flatMap(b -> b.arts().stream()
                                .sorted(Comparator.comparing(ArtViewModel::id))
                                .map(a -> csvRow(c.id(), b.id(), a))
                        )
                )
                .collect(Collectors.joining("\n"));
    }

    private static String csvRow(String containerId, String boxId, ArtViewModel a) {
        // Escape commas minimally by wrapping fields in quotes if they contain commas or quotes
        String artId = escape(a.id());
        String material = escape(a.material());
        return String.join(",",
                escape(containerId),
                escape(boxId),
                artId,
                String.valueOf(a.width()),
                String.valueOf(a.height()),
                material,
                String.valueOf(a.weight())
        );
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            // replace quotes with double quotes and wrap in quotes
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
