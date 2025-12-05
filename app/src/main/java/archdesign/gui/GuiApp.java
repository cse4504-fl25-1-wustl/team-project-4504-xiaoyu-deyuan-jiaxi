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
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
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
    private String selectedFilePath;  // Store the actual file path separately

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
        
        JTextArea analyticsArea = new JTextArea();
        analyticsArea.setEditable(false);
        analyticsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane analyticsScroll = new JScrollPane(analyticsArea);
        reportTabs.addTab("Analytics & Statistics", analyticsScroll);

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
        fileLabel = new JLabel("<html><div style='text-align:center;'><b>↓ Drag & Drop CSV Here ↓</b><br/><small>or click Browse</small><br/><small style='color: #888;'>No file selected</small></div></html>");
        fileLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 150, 255), 2),
            new EmptyBorder(10, 10, 10, 10)
        ));
        fileLabel.setBackground(new Color(230, 240, 255));
        fileLabel.setOpaque(true);
        fileLabel.setHorizontalAlignment(SwingConstants.CENTER);
        fileLabel.setVerticalAlignment(SwingConstants.CENTER);
        fileLabel.setPreferredSize(new Dimension(250, 70));
        fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fileLabel.setForeground(new Color(50, 100, 200));
        
        // Enable drag and drop
        new DropTarget(fileLabel, new java.awt.dnd.DropTargetAdapter() {
            @Override
            public void drop(java.awt.dnd.DropTargetDropEvent e) {
                try {
                    e.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                    java.util.List<?> droppedFiles = (java.util.List<?>) e.getTransferable()
                        .getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                    
                    if (!droppedFiles.isEmpty()) {
                        File droppedFile = (File) droppedFiles.get(0);
                        
                        if (!droppedFile.getName().toLowerCase().endsWith(".csv")) {
                            showError("Please drop a CSV file. Selected file: " + droppedFile.getName());
                            return;
                        }
                        
                        if (!droppedFile.exists()) {
                            showError("File does not exist: " + droppedFile.getName());
                            return;
                        }
                        
                        if (droppedFile.length() == 0) {
                            showError("The dropped file is empty. Please choose a file with data.");
                            return;
                        }
                        
                        if (droppedFile.length() > 10 * 1024 * 1024) {
                            showError("The file is too large (over 10MB). Please choose a smaller file.");
                            return;
                        }
                        
                        fileLabel.setText("<html><div style='text-align:center;'><font color='green'><b>✓ File Loaded</b></font><br/>" + droppedFile.getName() + "</div></html>");
                        selectedFilePath = droppedFile.getAbsolutePath();
                        lastSelectedDirectory = droppedFile.getParent();
                        submitBtn.setEnabled(true);
                        updateStatus("Ready - Click 'Submit for Estimate' to process", new Color(0, 100, 0));
                        
                        try {
                            showFilePreview(droppedFile);
                        } catch (Exception ex) {
                            // Silently ignore
                        }
                    }
                    e.dropComplete(true);
                } catch (Exception ex) {
                    e.dropComplete(false);
                }
            }
        });
        
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
        exportBtn = new JButton("Export Results");
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
            selectedFilePath = file.getAbsolutePath();
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
        String filePath = selectedFilePath != null ? selectedFilePath : fileLabel.getText();
        
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

        // Analytics & Statistics
        JTextArea analyticsArea = (JTextArea) ((JScrollPane) reportTabs.getComponentAt(4)).getViewport().getView();
        analyticsArea.setText(generateAnalyticsReport(vm));

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

        // Work Order Summary - matches CLI output with detailed formatting
        report.append("═════════════════════════════════════════\n");
        report.append("    WORK ORDER SUMMARY\n");
        report.append("═════════════════════════════════════════\n\n");
        
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
                        String dims = String.format("%.0f\" x %.0f\"", art.height(), art.width());
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
        
        // Display piece counts in the requested format
        report.append("Total Pieces: ").append(allArts.size() + customPieceCount).append("\n");
        report.append("Standard Size Pieces: ").append(standardPieces);
        
        // Calculate average standard size for context
        if (standardPieces > 0) {
            report.append(" (estimated at 43\" x 33\")");
        }
        report.append("\n");
        
        int totalOversized = oversizeMap.values().stream().mapToInt(info -> info.qty).sum();
        report.append("Oversized Pieces: ").append(totalOversized).append("\n");
        
        // Display oversized details with indentation
        for (java.util.Map.Entry<String, OversizeInfo> entry : oversizeMap.entrySet()) {
            OversizeInfo info = entry.getValue();
            report.append(String.format("   * %s (Qty: %d) = %.0f lbs\n", 
                entry.getKey(), info.qty, info.totalWeight));
        }
        report.append("\n");
        
        // Display weights with proper formatting
        report.append(String.format("Total Artwork Weight: %.0f lbs\n", totalArtworkWeight));
        report.append(String.format("Total Packaging Weight: %.0f lbs\n", totalPackagingWeight));
        report.append(String.format("Final Shipment Weight: %.0f lbs\n", finalShipmentWeight));
        report.append("\n");
        
        // Additional breakdown details
        report.append("─────────────────────────────────────\n");
        report.append("PACKAGING BREAKDOWN:\n");
        report.append(String.format("  Standard Boxes: %d\n", standardBoxCount));
        report.append(String.format("  Large Boxes: %d\n", largeBoxCount));
        report.append(String.format("  Unpacked Items: %d\n", customPieceCount));
        report.append("\n");
        
        report.append("CONTAINER BREAKDOWN:\n");
        report.append(String.format("  Standard Pallets: %d\n", standardPalletCount));
        report.append(String.format("  Oversized Pallets: %d\n", oversizedPalletCount));
        report.append(String.format("  Crates: %d\n", crateContainerCount));
        report.append("─────────────────────────────────────\n\n");

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

    private String createProgressBar(double percentage, int width) {
        int filled = (int) (percentage / 100.0 * width);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < width; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        return bar.toString();
    }

    private String generateAnalyticsReport(ShipmentViewModel vm) {
        StringBuilder report = new StringBuilder();
        report.append("╔════════════════════════════════════════════════════════╗\n");
        report.append("║      PACKING ANALYTICS & STATISTICS REPORT             ║\n");
        report.append("╚════════════════════════════════════════════════════════╝\n\n");

        // Collect data
        java.util.List<ArtViewModel> allArts = new java.util.ArrayList<>();
        java.util.Map<String, Integer> containerTypeCount = new java.util.LinkedHashMap<>();
        java.util.Map<String, Integer> boxTypeCount = new java.util.LinkedHashMap<>();
        java.util.Map<String, Integer> materialCount = new java.util.LinkedHashMap<>();
        int totalBoxes = 0;
        double totalContainerVolume = 0;
        double totalUsedVolume = 0;
        
        for (ContainerViewModel container : vm.containers()) {
            String type = container.type();
            containerTypeCount.put(type, containerTypeCount.getOrDefault(type, 0) + 1);
            
            int containerVolume = container.length() * container.width() * container.currentHeight();
            totalContainerVolume += containerVolume;
            
            if (container.boxes() != null) {
                totalBoxes += container.boxes().size();
                
                for (BoxViewModel box : container.boxes()) {
                    String boxType = box.type();
                    boxTypeCount.put(boxType, boxTypeCount.getOrDefault(boxType, 0) + 1);
                    
                    int boxVolume = box.length() * box.width() * box.currentHeight();
                    totalUsedVolume += boxVolume;
                    
                    if (box.arts() != null) {
                        for (ArtViewModel art : box.arts()) {
                            allArts.add(art);
                            String material = art.material();
                            materialCount.put(material, materialCount.getOrDefault(material, 0) + 1);
                        }
                    }
                }
            }
        }
        
        int unpackedCount = vm.unpackedArts() != null ? vm.unpackedArts().size() : 0;
        int totalArtworks = allArts.size() + unpackedCount;
        double overallUtilization = totalContainerVolume > 0 ? (totalUsedVolume / totalContainerVolume) * 100 : 0;
        double packingSuccessRate = totalArtworks > 0 ? (allArts.size() / (double) totalArtworks) * 100 : 0;
        double avgCostPerItem = totalArtworks > 0 ? vm.totalCost() / totalArtworks : 0;
        double avgWeightPerItem = totalArtworks > 0 ? vm.totalWeight() / totalArtworks : 0;

        // KEY PERFORMANCE INDICATORS
        String util_bar = createProgressBar(overallUtilization, 20);
        String success_bar = createProgressBar(packingSuccessRate, 20);
        report.append("╭─ KEY PERFORMANCE INDICATORS ─────────────────────────╮\n");
        report.append(String.format("│ Container Utilization: %5.1f%%  [%s]\n", overallUtilization, util_bar));
        report.append(String.format("│ Packing Success Rate:  %5.1f%%  [%s]\n", packingSuccessRate, success_bar));
        report.append(String.format("│ Cost per Item:         $%6.2f\n", avgCostPerItem));
        report.append("╰──────────────────────────────────────────────────────╯\n\n");

        // EFFICIENCY METRICS
        report.append("┌─ EFFICIENCY METRICS ───────────────────────────────────┐\n");
        report.append(String.format("│  Container Utilization: %.1f%%\n", overallUtilization));
        report.append(String.format("│  ├─ Total Container Volume: %,.0f cm³\n", totalContainerVolume));
        report.append(String.format("│  ├─ Used Volume:          %,.0f cm³\n", totalUsedVolume));
        report.append(String.format("│  └─ Unused Space:         %.1f%%\n", 100 - overallUtilization));
        report.append("│\n");
        report.append(String.format("│  Packing Success: %.1f%%\n", packingSuccessRate));
        report.append(String.format("│  ├─ Packed Items:    %d ✓\n", allArts.size()));
        report.append(String.format("│  ├─ Unpacked Items:  %d %s\n", unpackedCount, unpackedCount > 0 ? "✗" : "✓"));
        report.append(String.format("│  └─ Total Items:     %d\n", totalArtworks));
        report.append("│\n");
        report.append(String.format("│  Cost Analysis:\n"));
        report.append(String.format("│  ├─ Total Cost:      $%.2f\n", vm.totalCost()));
        report.append(String.format("│  ├─ Cost per Item:   $%.2f\n", avgCostPerItem));
        report.append(String.format("│  └─ Cost per Pound:  $%.2f\n", vm.totalWeight() > 0 ? vm.totalCost() / vm.totalWeight() : 0));
        report.append("└────────────────────────────────────────────────────────┘\n\n");

        // CONTAINER ANALYSIS
        report.append("┌─ CONTAINER ANALYSIS ───────────────────────────────────┐\n");
        report.append(String.format("│  Total Containers: %d\n", vm.totalContainers()));
        report.append("│\n");
        
        // Create visual pie chart for container types
        int maxTypeLength = containerTypeCount.keySet().stream()
            .mapToInt(String::length)
            .max()
            .orElse(15);
        
        for (java.util.Map.Entry<String, Integer> entry : containerTypeCount.entrySet()) {
            int count = entry.getValue();
            double percentage = vm.totalContainers() > 0 ? (count / (double) vm.totalContainers()) * 100 : 0;
            String bar = createProgressBar(percentage, 15);
            report.append(String.format("│  %-20s: %2d [%s] %5.1f%%\n", 
                entry.getKey(), count, bar, percentage));
        }
        report.append("└────────────────────────────────────────────────────────┘\n\n");

        // BOX ANALYSIS
        report.append("┌─ BOX ANALYSIS ─────────────────────────────────────────┐\n");
        report.append(String.format("│  Total Boxes: %d\n", totalBoxes));
        if (vm.totalContainers() > 0) {
            double avgBoxesPerContainer = totalBoxes / (double) vm.totalContainers();
            report.append(String.format("│  Avg per Container: %.2f boxes\n", avgBoxesPerContainer));
        }
        report.append("│\n");
        report.append("│  Box Type Distribution:\n");
        for (java.util.Map.Entry<String, Integer> entry : boxTypeCount.entrySet()) {
            int count = entry.getValue();
            double percentage = totalBoxes > 0 ? (count / (double) totalBoxes) * 100 : 0;
            String bar = createProgressBar(percentage, 15);
            report.append(String.format("│  %-15s: %2d [%s] %5.1f%%\n", 
                entry.getKey(), count, bar, percentage));
        }
        report.append("└────────────────────────────────────────────────────────┘\n\n");

        // ARTWORK ANALYSIS
        report.append("┌─ ARTWORK ANALYSIS ────────────────────────────────────┐\n");
        report.append(String.format("│  Total Items:     %d\n", totalArtworks));
        report.append(String.format("│  ├─ Packed:       %d ✓\n", allArts.size()));
        report.append(String.format("│  └─ Unpacked:     %d %s\n", unpackedCount, unpackedCount > 0 ? "✗" : "✓"));
        report.append("│\n");
        
        if (!allArts.isEmpty()) {
            double avgWeight = allArts.stream().mapToDouble(ArtViewModel::weight).average().orElse(0);
            double minWeight = allArts.stream().mapToDouble(ArtViewModel::weight).min().orElse(0);
            double maxWeight = allArts.stream().mapToDouble(ArtViewModel::weight).max().orElse(0);
            
            double avgWidth = allArts.stream().mapToDouble(ArtViewModel::width).average().orElse(0);
            double minWidth = allArts.stream().mapToDouble(ArtViewModel::width).min().orElse(0);
            double maxWidth = allArts.stream().mapToDouble(ArtViewModel::width).max().orElse(0);
            
            double avgHeight = allArts.stream().mapToDouble(ArtViewModel::height).average().orElse(0);
            double minHeight = allArts.stream().mapToDouble(ArtViewModel::height).min().orElse(0);
            double maxHeight = allArts.stream().mapToDouble(ArtViewModel::height).max().orElse(0);
            
            report.append("│  Weight Statistics (kg):\n");
            report.append(String.format("│  ├─ Average: %6.2f kg\n", avgWeight));
            report.append(String.format("│  ├─ Min:     %6.2f kg\n", minWeight));
            report.append(String.format("│  └─ Max:     %6.2f kg\n", maxWeight));
            
            report.append("│\n│  Dimensions (cm):\n");
            report.append(String.format("│  ├─ Width  → Avg: %6.1f  Min: %6.1f  Max: %6.1f\n", avgWidth, minWidth, maxWidth));
            report.append(String.format("│  └─ Height → Avg: %6.1f  Min: %6.1f  Max: %6.1f\n", avgHeight, minHeight, maxHeight));
        }
        report.append("│\n");
        report.append("│  Material Distribution:\n");
        for (java.util.Map.Entry<String, Integer> entry : materialCount.entrySet()) {
            int count = entry.getValue();
            double percentage = !allArts.isEmpty() ? (count / (double) allArts.size()) * 100 : 0;
            String bar = createProgressBar(percentage, 12);
            report.append(String.format("│  %-18s: %2d [%s] %5.1f%%\n", 
                entry.getKey(), count, bar, percentage));
        }
        report.append("└────────────────────────────────────────────────────────┘\n\n");

        // WEIGHT ANALYSIS
        report.append("┌─ WEIGHT ANALYSIS ──────────────────────────────────────┐\n");
        
        double totalArtworkWeight = allArts.stream().mapToDouble(ArtViewModel::weight).sum();
        if (vm.unpackedArts() != null) {
            totalArtworkWeight += vm.unpackedArts().stream().mapToDouble(ArtViewModel::weight).sum();
        }
        double packagingWeight = vm.totalWeight() - totalArtworkWeight;
        double packagingPercentage = vm.totalWeight() > 0 ? (packagingWeight / vm.totalWeight()) * 100 : 0;
        double artworkPercentage = 100 - packagingPercentage;
        
        String art_bar = createProgressBar(artworkPercentage, 15);
        String pkg_bar = createProgressBar(packagingPercentage, 15);
        
        report.append(String.format("│  Total Shipment Weight: %.2f kg\n", vm.totalWeight()));
        report.append(String.format("│  ├─ Artwork:   %.2f kg [%s] %5.1f%%\n", totalArtworkWeight, art_bar, artworkPercentage));
        report.append(String.format("│  └─ Packaging: %.2f kg [%s] %5.1f%%\n", packagingWeight, pkg_bar, packagingPercentage));
        report.append(String.format("│\n│  Avg per Item: %.2f kg\n", avgWeightPerItem));
        report.append("└────────────────────────────────────────────────────────┘\n\n");

        // RECOMMENDATIONS
        report.append("┌─ RECOMMENDATIONS & INSIGHTS ─────────────────────────┐\n");

        java.util.List<String> recommendations = new java.util.ArrayList<>();
        
        if (overallUtilization < 60) {
            recommendations.add("⚠  Low utilization (< 60%). Try smaller containers or batch shipments.");
        } else if (overallUtilization > 85) {
            recommendations.add("✓  Excellent utilization (> 85%). Very efficient packing!");
        } else {
            recommendations.add("✓  Good utilization (" + String.format("%.0f%%", overallUtilization) + "). Solid packing efficiency.");
        }
        
        if (unpackedCount > 0) {
            recommendations.add("⚠  " + unpackedCount + " item" + (unpackedCount > 1 ? "s" : "") + " unpacked. Try different modes or larger containers.");
        } else {
            recommendations.add("✓  100% packing success! All items packed efficiently.");
        }
        
        if (packagingPercentage > 30) {
            recommendations.add("ℹ  Packaging is " + String.format("%.1f%%", packagingPercentage) + " of total weight. Significant overhead.");
        } else if (packagingPercentage > 20) {
            recommendations.add("ℹ  Packaging is " + String.format("%.1f%%", packagingPercentage) + " of total weight. Moderate overhead.");
        }
        
        if (!containerTypeCount.isEmpty()) {
            String dominantType = containerTypeCount.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("Unknown");
            int dominantCount = containerTypeCount.get(dominantType);
            recommendations.add("ℹ  Primary container: " + dominantType + " (" + dominantCount + " used)");
        }

        for (int i = 0; i < recommendations.size(); i++) {
            String icon = recommendations.get(i).startsWith("✓") ? "  " : 
                          recommendations.get(i).startsWith("⚠") ? "  " : "  ";
            report.append(String.format("│%s%d. %s\n", icon, i + 1, recommendations.get(i).substring(2)));
        }

        report.append("└────────────────────────────────────────────────────────┘\n\n");
        return report.toString();
    }

    private void handleJsonExport() {
        if (currentViewModel == null) {
            showError("No results available to export. Please process a CSV file first.");
            return;
        }

        // Ask user which format to export
        String[] options = {"JSON", "PDF", "Both", "Cancel"};
        int choice = JOptionPane.showOptionDialog(frame,
            "Choose export format:",
            "Export Results",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == 3 || choice == JOptionPane.CLOSED_OPTION) {
            return; // Cancel
        }

        if (choice == 0 || choice == 2) {
            exportAsJson();
        }
        
        if (choice == 1 || choice == 2) {
            exportAsPdf();
        }
    }

    private void exportAsJson() {
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

    private void exportAsPdf() {
        JFileChooser chooser = new JFileChooser(lastSelectedDirectory);
        chooser.setDialogTitle("Save Packing Results as PDF");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        chooser.setSelectedFile(new File("packing_results.pdf"));

        int result = chooser.showSaveDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            lastSelectedDirectory = file.getParent();
            
            try {
                generatePdfReport(file.getAbsolutePath());
                
                JOptionPane.showMessageDialog(frame,
                    "Results successfully exported to:\n" + file.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                
                updateStatus("PDF export completed successfully", new Color(0, 150, 0));
            } catch (Exception ex) {
                showError("Failed to export PDF: " + ex.getMessage());
                updateStatus("PDF export failed", Color.RED);
            }
        }
    }

    private void generatePdfReport(String filePath) throws Exception {
        com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(filePath);
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);
        
        // Set font
        com.itextpdf.kernel.font.PdfFont font = 
            com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
        com.itextpdf.kernel.font.PdfFont boldFont = 
            com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        
        // Title
        com.itextpdf.layout.element.Paragraph title = new com.itextpdf.layout.element.Paragraph(
            "ART PACKER - SHIPPING ESTIMATE REPORT")
            .setFont(boldFont).setFontSize(18);
        document.add(title);
        document.add(new com.itextpdf.layout.element.Paragraph(""));
        
        // Summary Section
        addSectionHeader(document, boldFont, "SHIPMENT PLAN SUMMARY");
        java.util.List<ArtViewModel> allArts = new java.util.ArrayList<>();
        for (ContainerViewModel container : currentViewModel.containers()) {
            for (BoxViewModel box : container.boxes()) {
                allArts.addAll(box.arts());
            }
        }
        int unpackedCount = currentViewModel.unpackedArts() != null ? currentViewModel.unpackedArts().size() : 0;
        
        document.add(createLabelValue("Total Estimated Cost", "$" + String.format("%.2f", currentViewModel.totalCost())));
        document.add(createLabelValue("Total Weight", String.format("%.2f lbs", currentViewModel.totalWeight())));
        document.add(createLabelValue("Total Containers", String.valueOf(currentViewModel.totalContainers())));
        document.add(createLabelValue("Total Boxes", String.valueOf(currentViewModel.totalBoxes())));
        document.add(createLabelValue("Total Artwork Pieces", String.valueOf(allArts.size() + unpackedCount)));
        document.add(createLabelValue("Packed Items", String.valueOf(allArts.size())));
        document.add(createLabelValue("Unpacked Items", String.valueOf(unpackedCount)));
        document.add(new com.itextpdf.layout.element.Paragraph(""));
        
        // Container Analysis
        addSectionHeader(document, boldFont, "CONTAINER ANALYSIS");
        document.add(createLabelValue("Total Containers", String.valueOf(currentViewModel.totalContainers())));
        
        java.util.Map<String, Integer> containerTypes = new java.util.LinkedHashMap<>();
        for (ContainerViewModel container : currentViewModel.containers()) {
            String type = container.type();
            containerTypes.put(type, containerTypes.getOrDefault(type, 0) + 1);
        }
        for (java.util.Map.Entry<String, Integer> entry : containerTypes.entrySet()) {
            double percentage = currentViewModel.totalContainers() > 0 ? 
                (entry.getValue() / (double) currentViewModel.totalContainers()) * 100 : 0;
            document.add(createLabelValue("  " + entry.getKey(), 
                entry.getValue() + " (" + String.format("%.1f%%", percentage) + ")"));
        }
        document.add(new com.itextpdf.layout.element.Paragraph(""));
        
        // Efficiency Metrics
        addSectionHeader(document, boldFont, "EFFICIENCY METRICS");
        
        double totalVolume = 0;
        double usedVolume = 0;
        for (ContainerViewModel container : currentViewModel.containers()) {
            int containerVol = container.length() * container.width() * container.currentHeight();
            totalVolume += containerVol;
            if (container.boxes() != null) {
                for (BoxViewModel box : container.boxes()) {
                    int boxVol = box.length() * box.width() * box.currentHeight();
                    usedVolume += boxVol;
                }
            }
        }
        double utilization = totalVolume > 0 ? (usedVolume / totalVolume) * 100 : 0;
        double successRate = (allArts.size() + unpackedCount) > 0 ? 
            (allArts.size() / (double)(allArts.size() + unpackedCount)) * 100 : 0;
        
        document.add(createLabelValue("Container Utilization", String.format("%.1f%%", utilization)));
        document.add(createLabelValue("Packing Success Rate", String.format("%.1f%%", successRate)));
        document.add(createLabelValue("Cost per Item", "$" + String.format("%.2f", 
            (allArts.size() + unpackedCount) > 0 ? currentViewModel.totalCost() / (allArts.size() + unpackedCount) : 0)));
        document.add(new com.itextpdf.layout.element.Paragraph(""));
        
        // Cost Breakdown
        addSectionHeader(document, boldFont, "COST & WEIGHT BREAKDOWN");
        document.add(createLabelValue("Total Cost", "$" + String.format("%.2f", currentViewModel.totalCost())));
        document.add(createLabelValue("Total Weight", String.format("%.2f kg", currentViewModel.totalWeight())));
        
        double totalArtworkWeight = allArts.stream().mapToDouble(ArtViewModel::weight).sum();
        if (currentViewModel.unpackedArts() != null) {
            totalArtworkWeight += currentViewModel.unpackedArts().stream().mapToDouble(ArtViewModel::weight).sum();
        }
        double packagingWeight = currentViewModel.totalWeight() - totalArtworkWeight;
        double packagingPercentage = currentViewModel.totalWeight() > 0 ? 
            (packagingWeight / currentViewModel.totalWeight()) * 100 : 0;
        
        document.add(createLabelValue("Artwork Weight", String.format("%.2f kg (%.1f%%)", totalArtworkWeight, 100 - packagingPercentage)));
        document.add(createLabelValue("Packaging Weight", String.format("%.2f kg (%.1f%%)", packagingWeight, packagingPercentage)));
        document.add(new com.itextpdf.layout.element.Paragraph(""));
        
        // Footer
        document.add(new com.itextpdf.layout.element.Paragraph(""));
        document.add(new com.itextpdf.layout.element.Paragraph(
            "Report Generated: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()))
            .setFontSize(10));
        
        document.close();
    }

    private void addSectionHeader(com.itextpdf.layout.Document document, 
                                   com.itextpdf.kernel.font.PdfFont boldFont, String header) {
        com.itextpdf.layout.element.Paragraph section = new com.itextpdf.layout.element.Paragraph(header)
            .setFont(boldFont).setFontSize(12);
        document.add(section);
        document.add(new com.itextpdf.layout.element.LineSeparator(
            new com.itextpdf.kernel.pdf.canvas.draw.SolidLine()).setMarginBottom(5));
    }

    private com.itextpdf.layout.element.Paragraph createLabelValue(String label, String value) {
        return new com.itextpdf.layout.element.Paragraph(label + ": " + value).setMarginBottom(3);
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
