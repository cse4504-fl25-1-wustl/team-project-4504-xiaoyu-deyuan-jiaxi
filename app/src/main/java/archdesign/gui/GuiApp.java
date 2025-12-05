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
 * Custom rounded border for modern UI
 */
class RoundedBorder extends javax.swing.border.AbstractBorder {
    private int radius;
    private Color color;
    private int thickness;
    
    public RoundedBorder(int radius, Color color, int thickness) {
        this.radius = radius;
        this.color = color;
        this.thickness = thickness;
    }
    
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }
    
    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(thickness + 5, thickness + 5, thickness + 5, thickness + 5);
    }
}

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
    
    // Visual panels for each tab
    private JPanel summaryVisualPanel;
    private JPanel detailedVisualPanel;
    private JPanel containersVisualPanel;
    private JPanel unpackedVisualPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GuiApp().createAndShowGui());
    }

    private void createAndShowGui() {
        lastSelectedDirectory = System.getProperty("user.home");
        
        frame = new JFrame("Art Packer - Shipping Estimate System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);
        
        // Simple white and black theme
        Color darkBg = new Color(245, 245, 245);        // White background
        Color cardBg = new Color(245, 245, 245);        // White
        Color primaryBlue = Color.BLACK;                // Black for borders
        Color accentGreen = Color.BLACK;                // Black
        Color textWhite = Color.BLACK;
        
        // Set UI defaults for black text on all components
        UIManager.put("TabbedPane.foreground", Color.BLACK);
        UIManager.put("TabbedPane.background", cardBg);
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Label.foreground", Color.BLACK);
        
        // Set dark theme
        frame.getContentPane().setBackground(darkBg);

        // Main container with modern padding
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(darkBg);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top panel: Input controls
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Center panel: Status and Reports
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(darkBg);
        
        // Status indicator with modern styling
        statusLabel = new JLabel("Status: Ready - Please select a file and submit");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(textWhite);
        statusLabel.setBackground(cardBg);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new javax.swing.border.CompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            new EmptyBorder(10, 15, 10, 15)
        ));
        centerPanel.add(statusLabel, BorderLayout.NORTH);

        // Report tabs with custom styling
        reportTabs = new JTabbedPane();
        reportTabs.setEnabled(false);
        reportTabs.setBackground(cardBg);
        reportTabs.setForeground(Color.BLACK);
        reportTabs.setBorder(null);
        
        // Summary Report
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        outputArea.setBackground(cardBg);
        outputArea.setForeground(Color.BLACK);
        outputArea.setCaretColor(primaryBlue);
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        
        JPanel summaryContentPanel = new JPanel(new BorderLayout(10, 10));
        summaryContentPanel.setBackground(cardBg);
        JPanel summaryVisualPanel = createVisualSummaryPanel();
        summaryContentPanel.add(summaryVisualPanel, BorderLayout.NORTH);
        summaryContentPanel.add(outputArea, BorderLayout.CENTER);
        
        JScrollPane summaryScroll = new JScrollPane(summaryContentPanel);
        summaryScroll.getViewport().setBackground(cardBg);
        summaryScroll.setBorder(null);
        reportTabs.addTab("Summary Report", summaryScroll);
        
        // Detailed Report
        JTextArea detailedArea = new JTextArea();
        detailedArea.setEditable(false);
        detailedArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        detailedArea.setBackground(cardBg);
        detailedArea.setForeground(Color.BLACK);
        detailedArea.setMargin(new Insets(10, 10, 10, 10));
        
        JPanel detailedContentPanel = new JPanel(new BorderLayout(10, 10));
        detailedContentPanel.setBackground(cardBg);
        JPanel detailedVisualPanel = createDetailedVisualPanel();
        detailedContentPanel.add(detailedVisualPanel, BorderLayout.NORTH);
        detailedContentPanel.add(detailedArea, BorderLayout.CENTER);
        
        JScrollPane detailedScroll = new JScrollPane(detailedContentPanel);
        detailedScroll.getViewport().setBackground(cardBg);
        detailedScroll.setBorder(null);
        reportTabs.addTab("Detailed Report", detailedScroll);
        
        // Containers Breakdown
        JTextArea containersArea = new JTextArea();
        containersArea.setEditable(false);
        containersArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        containersArea.setBackground(cardBg);
        containersArea.setForeground(Color.BLACK);
        containersArea.setMargin(new Insets(10, 10, 10, 10));
        
        JPanel containersContentPanel = new JPanel(new BorderLayout(10, 10));
        containersContentPanel.setBackground(cardBg);
        JPanel containersVisualPanel = createContainersVisualPanel();
        containersContentPanel.add(containersVisualPanel, BorderLayout.NORTH);
        containersContentPanel.add(containersArea, BorderLayout.CENTER);
        
        JScrollPane containersScroll = new JScrollPane(containersContentPanel);
        containersScroll.getViewport().setBackground(cardBg);
        containersScroll.setBorder(null);
        reportTabs.addTab("Containers Breakdown", containersScroll);
        
        // Unpacked Items
        JTextArea unpackedArea = new JTextArea();
        unpackedArea.setEditable(false);
        unpackedArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        unpackedArea.setBackground(cardBg);
        unpackedArea.setForeground(Color.BLACK);
        unpackedArea.setMargin(new Insets(10, 10, 10, 10));
        
        JPanel unpackedContentPanel = new JPanel(new BorderLayout(10, 10));
        unpackedContentPanel.setBackground(cardBg);
        JPanel unpackedVisualPanel = createUnpackedVisualPanel();
        unpackedContentPanel.add(unpackedVisualPanel, BorderLayout.NORTH);
        unpackedContentPanel.add(unpackedArea, BorderLayout.CENTER);
        
        JScrollPane unpackedScroll = new JScrollPane(unpackedContentPanel);
        unpackedScroll.getViewport().setBackground(cardBg);
        unpackedScroll.setBorder(null);
        reportTabs.addTab("Unpacked Items", unpackedScroll);

        centerPanel.add(reportTabs, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        frame.getContentPane().add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createInputPanel() {
        // Simple white and black theme
        Color darkBg = new Color(245, 245, 245);        // White background
        Color cardBg = new Color(245, 245, 245);        // White
        Color primaryBlue = Color.BLACK;                // Black for borders
        Color accentGreen = Color.BLACK;                // Black
        Color textWhite = Color.BLACK;
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(cardBg);
        panel.setBorder(new javax.swing.border.CompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // CSV File Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel csvLabel = new JLabel("CSV File:");
        csvLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        csvLabel.setForeground(textWhite);
        panel.add(csvLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fileLabel = new JLabel("<html><div style='text-align:center;'><b style='font-size:14px;'>Drag & Drop CSV Here</b><br/><small>or click Browse</small><br/><small style='color: #333;'>No file selected</small></div></html>");
        fileLabel.setBorder(new javax.swing.border.CompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            new EmptyBorder(15, 15, 15, 15)
        ));
        fileLabel.setBackground(new Color(245, 245, 245));
        fileLabel.setOpaque(true);
        fileLabel.setHorizontalAlignment(SwingConstants.CENTER);
        fileLabel.setVerticalAlignment(SwingConstants.CENTER);
        fileLabel.setPreferredSize(new Dimension(300, 80));
        fileLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fileLabel.setForeground(textWhite);
        
        // Enable drag and drop
        DropTarget dropTarget = new DropTarget(fileLabel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
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
                        
                        fileLabel.setText("<html><div style='text-align:center;'><font color='#64DC78'><b style='font-size:14px;'>[OK] File Loaded</b></font><br/>" + droppedFile.getName() + "</div></html>");
                        selectedFilePath = droppedFile.getAbsolutePath();
                        lastSelectedDirectory = droppedFile.getParent();
                        submitBtn.setEnabled(true);
                        updateStatus("Ready - Click 'Submit for Estimate' to process", accentGreen);
                        
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
        chooseBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chooseBtn.setBackground(primaryBlue);
        chooseBtn.setForeground(Color.WHITE);
        chooseBtn.setFocusPainted(false);
        chooseBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        chooseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chooseBtn.setToolTipText("Select a CSV file with artwork details (supports both old and new formats)");
        chooseBtn.addActionListener(e -> handleFileSelection());
        panel.add(chooseBtn, gbc);

        // Packing Mode Selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel modeLabel = new JLabel("Packing Mode:");
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        modeLabel.setForeground(textWhite);
        panel.add(modeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        packingModeCombo = new JComboBox<>(new String[]{
            "Default (Boxes and Crates)",
            "Box Only (No Crates)",
            "Crate Only (No Boxes)"
        });
        packingModeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        packingModeCombo.setBackground(cardBg);
        packingModeCombo.setForeground(textWhite);
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
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.setPreferredSize(new Dimension(220, 40));
        submitBtn.setBackground(accentGreen);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.setToolTipText("Process the selected CSV file and calculate packing estimates");
        submitBtn.addActionListener(e -> handleSubmit());
        panel.add(submitBtn, gbc);

        // Export Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        exportBtn = new JButton("Export Results (JSON/PDF)");
        exportBtn.setEnabled(false);
        exportBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        exportBtn.setPreferredSize(new Dimension(220, 35));
        exportBtn.setBackground(primaryBlue);
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setFocusPainted(false);
        exportBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBtn.setToolTipText("Save the packing results to JSON or PDF format");
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
            preview.append("-------------------------------------\n");
            
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null && lineCount < 3) {
                preview.append(line).append("\n");
                lineCount++;
            }
            
            if (lineCount > 0) {
                preview.append("-------------------------------------\n");
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

    private JPanel createVisualSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        panel.add(createMetricCard("Total Cost", "$0.00", "💰"));
        panel.add(createMetricCard("Total Weight", "0.00 lbs", "⚖️"));
        panel.add(createMetricCard("Containers", "0", "📦"));
        panel.add(createMetricCard("Boxes", "0", "📦"));
        
        return panel;
    }
    
    private JPanel createDetailedVisualPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        panel.add(createMetricCard("Total Pieces", "0", "🎨"));
        panel.add(createMetricCard("Packed", "0", "✓"));
        panel.add(createMetricCard("Unpacked", "0", "⚠️"));
        panel.add(createMetricCard("Success Rate", "0%", "📊"));
        
        return panel;
    }
    
    private JPanel createContainersVisualPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        panel.add(createMetricCard("Total Containers", "0", "📦"));
        panel.add(createMetricCard("Standard Pallets", "0", "🏗️"));
        panel.add(createMetricCard("Oversized Pallets", "0", "📐"));
        panel.add(createMetricCard("Crates", "0", "🗃️"));
        
        return panel;
    }
    
    private JPanel createUnpackedVisualPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        panel.add(createMetricCard("Unpacked Items", "0", "⚠️"));
        panel.add(createMetricCard("Packing Success", "100%", "✓"));
        panel.add(createMetricCard("Total Weight", "0 kg", "⚖️"));
        panel.add(createMetricCard("Status", "OK", "✓"));
        
        return panel;
    }
    
    private JPanel createMetricCard(String label, String value, String icon) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new javax.swing.border.CompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            new EmptyBorder(15, 10, 15, 10)
        ));
        
        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel);
        
        card.add(Box.createVerticalStrut(10));
        
        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setForeground(Color.BLACK);
        card.add(valueLabel);
        
        card.add(Box.createVerticalStrut(5));
        
        // Label
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelLabel.setForeground(new Color(100, 100, 100));
        card.add(labelLabel);
        
        return card;
    }
    
    private void updateMetricCard(JPanel containerPanel, int cardIndex, String newValue) {
        JPanel card = (JPanel) containerPanel.getComponent(cardIndex);
        // The value label is the second component (index 2: icon, strut, value, strut, label)
        JLabel valueLabel = (JLabel) card.getComponent(2);
        valueLabel.setText(newValue);
    }

    private void displayResults(ShipmentViewModel vm) {
        // Update Summary Visual Panel
        JScrollPane summaryScroll = (JScrollPane) reportTabs.getComponentAt(0);
        JPanel summaryContentPanel = (JPanel) summaryScroll.getViewport().getView();
        JPanel summaryVisual = (JPanel) summaryContentPanel.getComponent(0);
        updateMetricCard(summaryVisual, 0, String.format("$%.2f", vm.totalCost()));
        updateMetricCard(summaryVisual, 1, String.format("%.2f lbs", vm.totalWeight()));
        updateMetricCard(summaryVisual, 2, String.valueOf(vm.totalContainers()));
        updateMetricCard(summaryVisual, 3, String.valueOf(vm.totalBoxes()));
        
        // Summary Report - matches CLI "Shipment Plan Summary"
        StringBuilder summary = new StringBuilder();
        summary.append("========================================\n");
        summary.append("    SHIPMENT PLAN SUMMARY               \n");
        summary.append("========================================\n\n");
        summary.append(String.format("Total Estimated Cost: $%.2f\n", vm.totalCost()));
        summary.append(String.format("Total Weight: %.2f lbs\n", vm.totalWeight()));
        summary.append(String.format("Total Containers: %d\n", vm.totalContainers()));
        summary.append(String.format("Total Boxes: %d\n", vm.totalBoxes()));
        summary.append("----------------------------------------\n\n");
        
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
        summary.append("========================================\n");
        
        outputArea.setText(summary.toString());

        // Update Detailed Visual Panel
        JScrollPane detailedScroll = (JScrollPane) reportTabs.getComponentAt(1);
        JPanel detailedContentPanel = (JPanel) detailedScroll.getViewport().getView();
        JPanel detailedVisual = (JPanel) detailedContentPanel.getComponent(0);
        int totalPieces = allArts.size() + customPieceCount;
        double successRate = totalPieces > 0 ? (allArts.size() / (double) totalPieces) * 100 : 100;
        updateMetricCard(detailedVisual, 0, String.valueOf(totalPieces));
        updateMetricCard(detailedVisual, 1, String.valueOf(allArts.size()));
        updateMetricCard(detailedVisual, 2, String.valueOf(customPieceCount));
        updateMetricCard(detailedVisual, 3, String.format("%.1f%%", successRate));
        
        // Detailed Report
        JTextArea detailedArea = (JTextArea) detailedContentPanel.getComponent(1);
        detailedArea.setText(generateDetailedReport(vm));

        // Update Containers Visual Panel
        JScrollPane containersScroll = (JScrollPane) reportTabs.getComponentAt(2);
        JPanel containersContentPanel = (JPanel) containersScroll.getViewport().getView();
        JPanel containersVisual = (JPanel) containersContentPanel.getComponent(0);
        int standardPallets = 0, oversizedPallets = 0, crates = 0;
        for (ContainerViewModel container : vm.containers()) {
            String type = container.type();
            if ("STANDARD_PALLET".equals(type) || "GLASS_PALLET".equals(type)) {
                standardPallets++;
            } else if ("OVERSIZE_PALLET".equals(type)) {
                oversizedPallets++;
            } else if ("STANDARD_CRATE".equals(type)) {
                crates++;
            }
        }
        updateMetricCard(containersVisual, 0, String.valueOf(vm.totalContainers()));
        updateMetricCard(containersVisual, 1, String.valueOf(standardPallets));
        updateMetricCard(containersVisual, 2, String.valueOf(oversizedPallets));
        updateMetricCard(containersVisual, 3, String.valueOf(crates));
        
        // Containers Breakdown
        JTextArea containersArea = (JTextArea) containersContentPanel.getComponent(1);
        containersArea.setText(generateContainersReport(vm));

        // Update Unpacked Visual Panel
        JScrollPane unpackedScroll = (JScrollPane) reportTabs.getComponentAt(3);
        JPanel unpackedContentPanel = (JPanel) unpackedScroll.getViewport().getView();
        JPanel unpackedVisual = (JPanel) unpackedContentPanel.getComponent(0);
        int unpackedCount = vm.unpackedArts() != null ? vm.unpackedArts().size() : 0;
        double packingSuccess = totalPieces > 0 ? (allArts.size() / (double) totalPieces) * 100 : 100;
        double unpackedWeight = 0;
        if (vm.unpackedArts() != null) {
            for (ArtViewModel art : vm.unpackedArts()) {
                unpackedWeight += art.weight();
            }
        }
        updateMetricCard(unpackedVisual, 0, String.valueOf(unpackedCount));
        updateMetricCard(unpackedVisual, 1, String.format("%.1f%%", packingSuccess));
        updateMetricCard(unpackedVisual, 2, String.format("%.2f kg", unpackedWeight));
        updateMetricCard(unpackedVisual, 3, unpackedCount == 0 ? "OK" : "WARNING");
        
        // Unpacked Items
        JTextArea unpackedArea = (JTextArea) unpackedContentPanel.getComponent(1);
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

        // Work Order Summary - matches CLI output with detailed formatting
        report.append("=========================================\n");
        report.append("    WORK ORDER SUMMARY\n");
        report.append("=========================================\n\n");
        
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
        report.append("----------------------------------------\n");
        report.append("PACKAGING BREAKDOWN:\n");
        report.append(String.format("  Standard Boxes: %d\n", standardBoxCount));
        report.append(String.format("  Large Boxes: %d\n", largeBoxCount));
        report.append(String.format("  Unpacked Items: %d\n", customPieceCount));
        report.append("\n");
        
        report.append("CONTAINER BREAKDOWN:\n");
        report.append(String.format("  Standard Pallets: %d\n", standardPalletCount));
        report.append(String.format("  Oversized Pallets: %d\n", oversizedPalletCount));
        report.append(String.format("  Crates: %d\n", crateContainerCount));
        report.append("----------------------------------------\n\n");

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
            report.append(String.format("=========================================\n"));
            report.append(String.format("CONTAINER #%d\n", i + 1));
            report.append(String.format("=========================================\n"));
            report.append(String.format("ID: %s\n", container.id()));
            report.append(String.format("Type: %s\n", container.type()));
            report.append(String.format("Dimensions (L×W×H): %d × %d × %d cm\n", 
                container.length(), container.width(), container.currentHeight()));
            report.append(String.format("Total Weight: %.2f kg\n", container.weight()));
            report.append(String.format("Number of Boxes: %d\n\n", 
                container.boxes() != null ? container.boxes().size() : 0));

            if (container.boxes() != null && !container.boxes().isEmpty()) {
                report.append("Boxes in this container:\n");
                report.append("-------------------------------------------\n");
                
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
                            report.append(String.format("      * %s (%.1f × %.1f cm, %.2f kg, %s)\n",
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
            report.append("[OK] All items were successfully packed!\n");
            report.append("\nNo unpacked items to report.\n");
            return report.toString();
        }

        report.append(String.format("[WARN] WARNING: %d items could not be packed\n\n", vm.unpackedArts().size()));
        report.append("The following art pieces could not fit into any available\n");
        report.append("container configuration:\n\n");
        report.append("-------------------------------------------\n");

        for (int i = 0; i < vm.unpackedArts().size(); i++) {
            ArtViewModel art = vm.unpackedArts().get(i);
            report.append(String.format("\n%d. Art ID: %s\n", i + 1, art.id()));
            report.append(String.format("   Dimensions: %.1f × %.1f cm\n", art.width(), art.height()));
            report.append(String.format("   Weight: %.2f kg\n", art.weight()));
            report.append(String.format("   Material: %s\n", art.material()));
        }

        report.append("\n-------------------------------------------\n");
        report.append("\nRecommendations:\n");
        report.append("* Consider using larger containers\n");
        report.append("* Try different packing modes\n");
        report.append("* These items may require custom shipping\n");

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
            JOptionPane.PLAIN_MESSAGE,
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
