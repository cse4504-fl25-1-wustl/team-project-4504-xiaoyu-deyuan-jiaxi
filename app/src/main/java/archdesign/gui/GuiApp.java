package archdesign.gui;

import archdesign.Main;
import archdesign.response.ShipmentViewModel;
import archdesign.response.ContainerViewModel;
import archdesign.response.BoxViewModel;
import archdesign.response.ArtViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;

/**
 * Enhanced Swing GUI for the Packer application.
 * Features include:
 * - CSV file upload
 * - Packing mode selection (box-only, crate-only, or both)
 * - Submit button for processing
 * - Status indicator (pending, processing, complete)
 * - Error reporting
 * - Multiple report views (Summary, Detailed, Containers, Unpacked)
 */
public class GuiApp {

    private JFrame frame;
    private JLabel fileLabel;
    private JButton chooseBtn;
    private JButton submitBtn;
    private JComboBox<String> packingModeCombo;
    private JLabel statusLabel;
    private JTextArea outputArea;
    private JTabbedPane reportTabs;
    private ShipmentViewModel currentViewModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GuiApp().createAndShowGui());
    }

    private void createAndShowGui() {
        frame = new JFrame("Art Packer - Shipping Estimate System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 650);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top panel: Input controls
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Center panel: Status and Reports
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Status indicator
        statusLabel = new JLabel("Status: Ready - Please select a file and submit");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(new Color(0, 100, 0));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(5, 10, 5, 10)
        ));
        centerPanel.add(statusLabel, BorderLayout.NORTH);

        // Report tabs
        reportTabs = new JTabbedPane();
        reportTabs.setEnabled(false);
        
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane summaryScroll = new JScrollPane(outputArea);
        reportTabs.addTab("Summary Report", summaryScroll);
        
        JTextArea detailedArea = new JTextArea();
        detailedArea.setEditable(false);
        detailedArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane detailedScroll = new JScrollPane(detailedArea);
        reportTabs.addTab("Detailed Report", detailedScroll);
        
        JTextArea containersArea = new JTextArea();
        containersArea.setEditable(false);
        containersArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane containersScroll = new JScrollPane(containersArea);
        reportTabs.addTab("Containers Breakdown", containersScroll);
        
        JTextArea unpackedArea = new JTextArea();
        unpackedArea.setEditable(false);
        unpackedArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane unpackedScroll = new JScrollPane(unpackedArea);
        reportTabs.addTab("Unpacked Items", unpackedScroll);

        centerPanel.add(reportTabs, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        frame.getContentPane().add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            "Input Configuration",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // CSV File Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel csvLabel = new JLabel("CSV File:");
        panel.add(csvLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fileLabel = new JLabel("No file selected");
        fileLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            new EmptyBorder(3, 5, 3, 5)
        ));
        fileLabel.setBackground(Color.WHITE);
        fileLabel.setOpaque(true);
        panel.add(fileLabel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        chooseBtn = new JButton("Browse...");
        chooseBtn.addActionListener(e -> handleFileSelection());
        panel.add(chooseBtn, gbc);

        // Packing Mode Selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel modeLabel = new JLabel("Packing Mode:");
        panel.add(modeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        packingModeCombo = new JComboBox<>(new String[]{
            "Default (Boxes and Crates)",
            "Box Only (No Crates)",
            "Crate Only (No Boxes)"
        });
        packingModeCombo.setToolTipText("Select whether crates can be used for packing");
        panel.add(packingModeCombo, gbc);

        // Submit Button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        submitBtn = new JButton("Submit for Estimate");
        submitBtn.setEnabled(false);
        submitBtn.setFont(new Font("Arial", Font.BOLD, 14));
        submitBtn.setPreferredSize(new Dimension(200, 35));
        submitBtn.addActionListener(e -> handleSubmit());
        panel.add(submitBtn, gbc);

        return panel;
    }

    private void handleFileSelection() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select CSV file with art details");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        int result = chooser.showOpenDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            fileLabel.setText(file.getAbsolutePath());
            submitBtn.setEnabled(true);
            updateStatus("Ready - Click 'Submit for Estimate' to process", new Color(0, 100, 0));
        }
    }

    private void handleSubmit() {
        String filePath = fileLabel.getText();
        
        if (filePath == null || filePath.isBlank() || filePath.equals("No file selected")) {
            showError("Please select a CSV file before submitting.");
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            showError("The selected file does not exist: " + filePath);
            return;
        }

        // Determine packing mode
        String packingMode = getPackingMode();

        // Disable controls during processing
        submitBtn.setEnabled(false);
        chooseBtn.setEnabled(false);
        packingModeCombo.setEnabled(false);
        reportTabs.setEnabled(false);
        
        updateStatus("Processing... Please wait", new Color(200, 100, 0));
        outputArea.setText("Starting packing process...\n");

        // Run processing on background thread
        Thread processingThread = new Thread(() -> {
            try {
                ShipmentViewModel vm = Main.processFile(filePath, packingMode);
                
                if (vm == null) {
                    SwingUtilities.invokeLater(() -> {
                        showError("Processing completed but returned no packing plan.");
                        resetControls();
                    });
                } else {
                    currentViewModel = vm;
                    SwingUtilities.invokeLater(() -> {
                        displayResults(vm);
                        updateStatus("Complete - Estimates are ready", new Color(0, 150, 0));
                        reportTabs.setEnabled(true);
                        resetControls();
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    showError("Processing failed: " + ex.getMessage() + 
                             "\n\nPlease check that your CSV file is properly formatted.");
                    resetControls();
                });
            }
        });
        processingThread.start();
    }

    private String getPackingMode() {
        int selectedIndex = packingModeCombo.getSelectedIndex();
        switch (selectedIndex) {
            case 0:
                return "default";
            case 1:
                return "box-only";
            case 2:
                return "crate-only";
            default:
                return "default";
        }
    }

    private void displayResults(ShipmentViewModel vm) {
        // Summary Report
        StringBuilder summary = new StringBuilder();
        summary.append("========================================\n");
        summary.append("         PACKING ESTIMATE SUMMARY       \n");
        summary.append("========================================\n\n");
        summary.append(String.format("Total Weight:      %.2f kg\n", vm.totalWeight()));
        summary.append(String.format("Total Cost:        $%.2f\n", vm.totalCost()));
        summary.append(String.format("Total Containers:  %d\n", vm.totalContainers()));
        summary.append(String.format("Total Boxes:       %d\n", vm.totalBoxes()));
        summary.append(String.format("Packed Containers: %d\n", vm.containers() != null ? vm.containers().size() : 0));
        summary.append(String.format("Unpacked Items:    %d\n", vm.unpackedArts() != null ? vm.unpackedArts().size() : 0));
        summary.append("\n========================================\n");
        
        outputArea.setText(summary.toString());

        // Detailed Report
        JTextArea detailedArea = (JTextArea) ((JScrollPane) reportTabs.getComponentAt(1)).getViewport().getView();
        detailedArea.setText(generateDetailedReport(vm));

        // Containers Breakdown
        JTextArea containersArea = (JTextArea) ((JScrollPane) reportTabs.getComponentAt(2)).getViewport().getView();
        containersArea.setText(generateContainersReport(vm));

        // Unpacked Items
        JTextArea unpackedArea = (JTextArea) ((JScrollPane) reportTabs.getComponentAt(3)).getViewport().getView();
        unpackedArea.setText(generateUnpackedReport(vm));
    }

    private String generateDetailedReport(ShipmentViewModel vm) {
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("       DETAILED PACKING REPORT          \n");
        report.append("========================================\n\n");

        report.append(String.format("Overall Statistics:\n"));
        report.append(String.format("  Total Weight: %.2f kg\n", vm.totalWeight()));
        report.append(String.format("  Total Cost: $%.2f\n", vm.totalCost()));
        report.append(String.format("  Containers Used: %d\n", vm.totalContainers()));
        report.append(String.format("  Boxes Used: %d\n", vm.totalBoxes()));
        report.append("\n");

        if (vm.containers() != null && !vm.containers().isEmpty()) {
            report.append("Container Details:\n");
            report.append("------------------\n");
            for (int i = 0; i < vm.containers().size(); i++) {
                ContainerViewModel container = vm.containers().get(i);
                report.append(String.format("\nContainer #%d [%s]:\n", i + 1, container.id()));
                report.append(String.format("  Type: %s\n", container.type()));
                report.append(String.format("  Dimensions: %d x %d x %d\n", 
                    container.length(), container.width(), container.currentHeight()));
                report.append(String.format("  Weight: %.2f kg\n", container.weight()));
                report.append(String.format("  Number of Boxes: %d\n", 
                    container.boxes() != null ? container.boxes().size() : 0));
                
                if (container.boxes() != null) {
                    for (BoxViewModel box : container.boxes()) {
                        report.append(String.format("    Box [%s] - Type: %s, Arts: %d, Weight: %.2f kg\n",
                            box.id(), box.type(), 
                            box.arts() != null ? box.arts().size() : 0,
                            box.weight()));
                    }
                }
            }
        }

        if (vm.unpackedArts() != null && !vm.unpackedArts().isEmpty()) {
            report.append(String.format("\n\nUnpacked Items: %d\n", vm.unpackedArts().size()));
            report.append("WARNING: Some items could not be packed!\n");
        }

        return report.toString();
    }

    private String generateContainersReport(ShipmentViewModel vm) {
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("       CONTAINERS BREAKDOWN             \n");
        report.append("========================================\n\n");

        if (vm.containers() == null || vm.containers().isEmpty()) {
            report.append("No containers were used in this packing plan.\n");
            return report.toString();
        }

        for (int i = 0; i < vm.containers().size(); i++) {
            ContainerViewModel container = vm.containers().get(i);
            report.append(String.format("═══════════════════════════════════════\n"));
            report.append(String.format("CONTAINER #%d\n", i + 1));
            report.append(String.format("═══════════════════════════════════════\n"));
            report.append(String.format("ID: %s\n", container.id()));
            report.append(String.format("Type: %s\n", container.type()));
            report.append(String.format("Dimensions (L×W×H): %d × %d × %d cm\n", 
                container.length(), container.width(), container.currentHeight()));
            report.append(String.format("Total Weight: %.2f kg\n", container.weight()));
            report.append(String.format("Number of Boxes: %d\n\n", 
                container.boxes() != null ? container.boxes().size() : 0));

            if (container.boxes() != null && !container.boxes().isEmpty()) {
                report.append("Boxes in this container:\n");
                report.append("───────────────────────────────────────\n");
                
                for (int j = 0; j < container.boxes().size(); j++) {
                    BoxViewModel box = container.boxes().get(j);
                    report.append(String.format("\n  Box #%d [%s]\n", j + 1, box.id()));
                    report.append(String.format("    Type: %s\n", box.type()));
                    report.append(String.format("    Dimensions: %d × %d × %d cm\n", 
                        box.length(), box.width(), box.currentHeight()));
                    report.append(String.format("    Weight: %.2f kg\n", box.weight()));
                    report.append(String.format("    Art Items: %d\n", 
                        box.arts() != null ? box.arts().size() : 0));
                    
                    if (box.arts() != null && !box.arts().isEmpty()) {
                        report.append("    Art pieces:\n");
                        for (ArtViewModel art : box.arts()) {
                            report.append(String.format("      • %s (%.1f × %.1f cm, %.2f kg, %s)\n",
                                art.id(), art.width(), art.height(), art.weight(), art.material()));
                        }
                    }
                }
            }
            report.append("\n");
        }

        return report.toString();
    }

    private String generateUnpackedReport(ShipmentViewModel vm) {
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("         UNPACKED ITEMS REPORT          \n");
        report.append("========================================\n\n");

        if (vm.unpackedArts() == null || vm.unpackedArts().isEmpty()) {
            report.append("✓ All items were successfully packed!\n");
            report.append("\nNo unpacked items to report.\n");
            return report.toString();
        }

        report.append(String.format("⚠ WARNING: %d items could not be packed\n\n", vm.unpackedArts().size()));
        report.append("The following art pieces could not fit into any available\n");
        report.append("container configuration:\n\n");
        report.append("───────────────────────────────────────\n");

        for (int i = 0; i < vm.unpackedArts().size(); i++) {
            ArtViewModel art = vm.unpackedArts().get(i);
            report.append(String.format("\n%d. Art ID: %s\n", i + 1, art.id()));
            report.append(String.format("   Dimensions: %.1f × %.1f cm\n", art.width(), art.height()));
            report.append(String.format("   Weight: %.2f kg\n", art.weight()));
            report.append(String.format("   Material: %s\n", art.material()));
        }

        report.append("\n───────────────────────────────────────\n");
        report.append("\nRecommendations:\n");
        report.append("• Consider using larger containers\n");
        report.append("• Try different packing modes\n");
        report.append("• These items may require custom shipping\n");

        return report.toString();
    }

    private void showError(String message) {
        updateStatus("Error occurred", Color.RED);
        JOptionPane.showMessageDialog(frame, 
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText("Status: " + message);
        statusLabel.setForeground(color);
    }

    private void resetControls() {
        submitBtn.setEnabled(true);
        chooseBtn.setEnabled(true);
        packingModeCombo.setEnabled(true);
    }
}
