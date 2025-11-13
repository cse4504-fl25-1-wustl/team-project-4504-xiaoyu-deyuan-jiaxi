package archdesign.gui;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Lightweight Swing GUI so the project can compile without external GUI plugins.
 * This GUI re-uses the same Main.processFile(...) helper and displays a
 * small summary of the returned ShipmentViewModel.
 */
public class GuiApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GuiApp::createAndShowGui);
    }

    private static void createAndShowGui() {
        JFrame frame = new JFrame("Packer GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 450);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        JButton chooseBtn = new JButton("Choose CSV");
        JButton runBtn = new JButton("Run");
        runBtn.setEnabled(false);
        JLabel fileLabel = new JLabel("No file selected");

        JPanel controls = new JPanel();
        controls.add(chooseBtn);
        controls.add(runBtn);
        top.add(controls, BorderLayout.WEST);
        top.add(fileLabel, BorderLayout.CENTER);

        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(outputArea);

        chooseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select input CSV file");
            int res = chooser.showOpenDialog(frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                fileLabel.setText(f.getAbsolutePath());
                runBtn.setEnabled(true);
            }
        });

        runBtn.addActionListener(e -> {
            String path = fileLabel.getText();
            if (path == null || path.isBlank() || path.equals("No file selected")) {
                outputArea.setText("Please choose a CSV file first.\n");
                return;
            }

            outputArea.setText("Processing...\n");
            runBtn.setEnabled(false);

            // Run processing on background thread to keep UI responsive
            Thread t = new Thread(() -> {
                try {
                    ShipmentViewModel vm = Main.processFile(path);
                    if (vm == null) {
                        SwingUtilities.invokeLater(() -> outputArea.append("No packing plan returned (null).\n"));
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Total weight: ").append(vm.totalWeight()).append("\n");
                        sb.append("Total cost: ").append(vm.totalCost()).append("\n");
                        sb.append("Total containers: ").append(vm.totalContainers()).append("\n");
                        sb.append("Total boxes: ").append(vm.totalBoxes()).append("\n");
                        sb.append("Containers returned: ").append(vm.containers() != null ? vm.containers().size() : 0).append("\n");
                        sb.append("Unpacked arts: ").append(vm.unpackedArts() != null ? vm.unpackedArts().size() : 0).append("\n");
                        SwingUtilities.invokeLater(() -> outputArea.append(sb.toString()));
                    }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> outputArea.append("Failed: " + ex.getMessage() + "\n"));
                } finally {
                    SwingUtilities.invokeLater(() -> runBtn.setEnabled(true));
                }
            });
            t.start();
        });

        frame.getContentPane().add(top, BorderLayout.NORTH);
        frame.getContentPane().add(scroll, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
