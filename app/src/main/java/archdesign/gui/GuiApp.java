package archdesign.gui;

import archdesign.Main;
import archdesign.output.JsonOutputWriter;
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
 * - JSON export functionality
 * - Status indicator (pending, processing, complete)
 * - Error reporting
 * - Multiple report views (Summary, Detailed, Containers, Unpacked)
 */
public class GuiApp {

    private JFrame frame;
    private JLabel fileLabel;
    private JButton chooseBtn;
    private JButton submitBtn;
    private JButton exportBtn;
    private JComboBox<String> packingModeCombo;
    private JLabel statusLabel;
    private JTextArea outputArea;
    private JTabbedPane reportTabs;
    private ShipmentViewModel currentViewModel;
    private String lastSelectedDirectory;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GuiApp().createAndShowGui());
    }

    private void createAndShowGui() {
        lastSelectedDirectory = System.getProperty("user.home");
        
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
        chooseBtn.setToolTipText("Select a CSV file with artwork details (supports both old and new formats)");
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
        packingModeCombo.setToolTipText(
            "Default: Use both boxes and crates for optimal packing\n" +
            "Box Only: Use only boxes, no crates\n" +
            "Crate Only: Use only crates, no boxes");
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
        submitBtn.setToolTipText("Process the selected CSV file and calculate packing estimates");
        submitBtn.addActionListener(e -> handleSubmit());
        panel.add(submitBtn, gbc);

        // Export Button (initially disabled)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        exportBtn = new JButton("Export Results to JSON");
        exportBtn.setEnabled(false);
        exportBtn.setFont(new Font("Arial", Font.BOLD, 12));
        exportBtn.setPreferredSize(new Dimension(200, 35));
        exportBtn.setToolTipText("Save the packing results to a JSON file for further analysis");
        exportBtn.addActionListener(e -> handleJsonExport());
        panel.add(exportBtn, gbc);

        return panel;
    }

    private void handleFileSelection() {
        JFileChooser chooser = new JFileChooser();
        if (lastSelectedDirectory != null) {
            chooser.setCurrentDirectory(new File(lastSelectedDirectory));
        }
        chooser.setDialogTitle("Select CSV file with art details");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        int result = chooser.showOpenDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            lastSelectedDirectory = file.getParent();
            
            // Validate file
            if (file.length() == 0) {
                showError("The selected file is empty. Please choose a file with data.");
                return;
            }
            
            if (file.length() > 10 * 1024 * 1024) { // 10MB limit
                showError("The selected file is too large (over 10MB). Please choose a smaller file.");
                return;
            }
            
            fileLabel.setText(file.getAbsolutePath());
            submitBtn.setEnabled(true);
            updateStatus("Ready - Click 'Submit for Estimate' to process", new Color(0, 100, 0));
            
            // Show preview of first few lines
            try {
                showFilePreview(file);
            } catch (Exception ex) {
                // Silently ignore preview errors
            }
        }
    }

    private void showFilePreview(File file) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            StringBuilder preview = new StringBuilder();
            preview.append("File: ").append(file.getName()).append(" (").append(file.length()).append(" bytes)\n");
            preview.append("─────────────────────────────────────\n");
            
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 3) {
                preview.append(line).append("\n");
                lineCount++;
            }
            
            if (lineCount > 0) {
                preview.append("─────────────────────────────────────\n");
                System.out.println(preview.toString()); // Show in console for debugging
            }
        } catch (Exception e) {
            // Silently fail for preview
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

        if (file.length() == 0) {
            showError("The selected file is empty. Please choose a file with data.");
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
                        showError("Processing completed but returned no packing plan.\n" +
                                 "Please verify that your CSV file contains valid artwork data with positive dimensions.");
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
                             "\n\nCommon issues:\n" +
                             "• Missing or incorrect columns in CSV\n" +
                             "• Invalid quantity, width, or height values\n" +
                             "• Empty fields in required columns\n\n" +
                             "Please check your CSV format and try again.");
                    resetControls();
                });
            }
        });
        processingThread.setName("CSV-Processing-Thread");
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
        // Summary Report - matches CLI "Shipment Plan Summary"
        StringBuilder summary = new StringBuilder();
        summary.append("========================================\n");
        summary.append("    SHIPMENT PLAN SUMMARY               \n");
        summary.append("========================================\n\n");
        summary.append(String.format("Total Estimated Cost: $%.2f\n", vm.totalCost()));
        summary.append(String.format("Total Weight: %.2f lbs\n", vm.totalWeight()));
        summary.append(String.format("Total Containers: %d\n", vm.totalContainers()));
        summary.append(String.format("Total Boxes: %d\n", vm.totalBoxes()));
        summary.append("────────────────────────────────────\n\n");
        
        // Add quick work order counts to summary
        java.util.List<ArtViewModel> allArts = new java.util.ArrayList<>();
        for (ContainerViewModel container : vm.containers()) {
            for (BoxViewModel box : container.boxes()) {
                allArts.addAll(box.arts());
            }
        }
        int customPieceCount = vm.unpackedArts() != null ? vm.unpackedArts().size() : 0;
        summary.append(String.format("Total Artwork Pieces: %d\n", allArts.size() + customPieceCount));
        summary.append(String.format("Packed Items: %d\n", allArts.size()));
        summary.append(String.format("Unpacked Items: %d\n", customPieceCount));
        summary.append("════════════════════════════════════\n");
        
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

        // Enable export button
        exportBtn.setEnabled(true);
    }

    private String generateDetailedReport(ShipmentViewModel vm) {
        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("       DETAILED PACKING REPORT          \n");
        report.append("========================================\n\n");

        report.append(String.format("Overall Statistics:\n"));
        report.append(String.format("  Total Weight: %.2f lbs\n", vm.totalWeight()));
        report.append(String.format("  Total Cost: $%.2f\n", vm.totalCost()));
        report.append(String.format("  Containers Used: %d\n", vm.totalContainers()));
        report.append(String.format("  Boxes Used: %d\n", vm.totalBoxes()));
        report.append("\n");

        // Work Order Summary - matches CLI output
        report.append("=== WORK ORDER SUMMARY ===\n\n");
        
        // Count piece types
        java.util.List<ArtViewModel> allArts = new java.util.ArrayList<>();
        java.util.Map<String, OversizeInfo> oversizeMap = new java.util.LinkedHashMap<>();
        int standardPieces = 0;
        double totalArtworkWeight = 0.0;
        
        for (ContainerViewModel container : vm.containers()) {
            for (BoxViewModel box : container.boxes()) {
                for (ArtViewModel art : box.arts()) {
                    allArts.add(art);
                    totalArtworkWeight += art.weight();
                    boolean isOversized = art.width() > 44 || art.height() > 44;
                    if (!isOversized) {
                        standardPieces++;
                    } else {
                        String dims = String.format("%.1f\" x %.1f\"", art.height(), art.width());
                        OversizeInfo info = oversizeMap.get(dims);
                        if (info == null) {
                            info = new OversizeInfo();
                            oversizeMap.put(dims, info);
                        }
                        info.qty++;
                        info.totalWeight += art.weight();
                    }
                }
            }
        }
        
        // Count box types
        int standardBoxCount = 0;
        int largeBoxCount = 0;
        for (ContainerViewModel container : vm.containers()) {
            for (BoxViewModel box : container.boxes()) {
                if ("STANDARD".equals(box.type())) {
                    standardBoxCount++;
                } else if ("LARGE".equals(box.type())) {
                    largeBoxCount++;
                }
            }
        }
        
        // Count container types
        int standardPalletCount = 0;
        int oversizedPalletCount = 0;
        int crateContainerCount = 0;
        for (ContainerViewModel container : vm.containers()) {
            String type = container.type();
            if ("STANDARD_PALLET".equals(type) || "GLASS_PALLET".equals(type)) {
                standardPalletCount++;
            } else if ("OVERSIZE_PALLET".equals(type)) {
                oversizedPalletCount++;
            } else if ("STANDARD_CRATE".equals(type)) {
                crateContainerCount++;
            }
        }
        
        int customPieceCount = vm.unpackedArts() != null ? vm.unpackedArts().size() : 0;
        for (ArtViewModel art : vm.unpackedArts()) {
            totalArtworkWeight += art.weight();
        }
        
        double finalShipmentWeight = vm.totalWeight();
        double totalPackagingWeight = finalShipmentWeight - totalArtworkWeight;
        
        // Display piece counts
        report.append("ARTWORK PIECES:\n");
        report.append(String.format("  Total Pieces: %d\n", allArts.size() + customPieceCount));
        report.append(String.format("  Standard Size Pieces: %d\n", standardPieces));
        report.append(String.format("  Oversized Pieces: %d\n", 
            oversizeMap.values().stream().mapToInt(info -> info.qty).sum()));
        
        for (java.util.Map.Entry<String, OversizeInfo> entry : oversizeMap.entrySet()) {
            OversizeInfo info = entry.getValue();
            report.append(String.format("    - %s (Qty: %d, Weight: %.0f lbs)\n", 
                entry.getKey(), info.qty, info.totalWeight));
        }
        report.append("\n");
        
        // Display packaging
        report.append("PACKAGING:\n");
        report.append(String.format("  Standard Box Count: %d\n", standardBoxCount));
        report.append(String.format("  Large Box Count: %d\n", largeBoxCount));
        report.append(String.format("  Custom Piece Count: %d\n", customPieceCount));
        report.append("\n");
        
        // Display containers
        report.append("CONTAINERS:\n");
        report.append(String.format("  Standard Pallet Count: %d\n", standardPalletCount));
        report.append(String.format("  Oversized Pallet Count: %d\n", oversizedPalletCount));
        report.append(String.format("  Crate Container Count: %d\n", crateContainerCount));
        report.append("\n");
        
        // Display weights
        report.append("WEIGHT BREAKDOWN:\n");
        report.append(String.format("  Total Artwork Weight: %.0f lbs\n", totalArtworkWeight));
        report.append(String.format("  Total Packaging Weight: %.0f lbs\n", totalPackagingWeight));
        report.append(String.format("  Final Shipment Weight: %.0f lbs\n", finalShipmentWeight));
        report.append("\n");

        if (vm.containers() != null && !vm.containers().isEmpty()) {
            report.append("\nContainer Details:\n");
            report.append("------------------\n");
            for (int i = 0; i < vm.containers().size(); i++) {
                ContainerViewModel container = vm.containers().get(i);
                report.append(String.format("\nContainer #%d [%s]:\n", i + 1, container.id()));
                report.append(String.format("  Type: %s\n", container.type()));
                report.append(String.format("  Dimensions: %d x %d x %d\n", 
                    container.length(), container.width(), container.currentHeight()));
                report.append(String.format("  Weight: %.2f lbs\n", container.weight()));
                report.append(String.format("  Number of Boxes: %d\n", 
                    container.boxes() != null ? container.boxes().size() : 0));
                
                if (container.boxes() != null) {
                    for (BoxViewModel box : container.boxes()) {
                        report.append(String.format("    Box [%s] - Type: %s, Arts: %d, Weight: %.2f lbs\n",
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
    
    // Helper class for tracking oversize group info
    private static class OversizeInfo {
        int qty = 0;
        double totalWeight = 0.0;
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

    private void handleJsonExport() {
        if (currentViewModel == null) {
            showError("No results available to export. Please process a CSV file first.");
            return;
        }

        JFileChooser chooser = new JFileChooser(lastSelectedDirectory);
        chooser.setDialogTitle("Save Packing Results as JSON");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json"));
        chooser.setSelectedFile(new File("packing_results.json"));

        int result = chooser.showSaveDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            lastSelectedDirectory = file.getParent();
            
            try {
                JsonOutputWriter jsonWriter = new JsonOutputWriter();
                jsonWriter.write(currentViewModel, file.getAbsolutePath());
                
                JOptionPane.showMessageDialog(frame,
                    "Results successfully exported to:\n" + file.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                
                updateStatus("Export completed successfully", new Color(0, 150, 0));
            } catch (Exception ex) {
                showError("Failed to export JSON: " + ex.getMessage());
                updateStatus("Export failed", Color.RED);
            }
        }
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
