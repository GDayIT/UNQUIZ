package guimodule;

import dbbl.BusinesslogicaDelegation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.*;
import javax.swing.*;

/**
 * Fully modular theme panel using lambda-based delegation and functional programming.
 *
 * <h2>Modular Architecture:</h2>
 * This panel implements complete modularity through:
 * - Lambda-based event handling for all user interactions
 * - Functional composition for complex operations
 * - No direct method calls to business logic
 * - Event-driven communication with other components
 * - Immutable data transfer objects
 *
 * <h2>Lambda Usage:</h2>
 * <ul>
 *   <li><b>Button Actions:</b> All button clicks handled via lambda expressions</li>
 *   <li><b>List Selection:</b> Selection changes processed through functional callbacks</li>
 *   <li><b>Form Operations:</b> Data collection and validation using function composition</li>
 *   <li><b>Message Display:</b> Status updates through consumer lambdas</li>
 * </ul>
 *
 * <h2>Delegation Pattern:</h2>
 * The panel communicates exclusively through the BusinesslogicaDelegation interface,
 * ensuring complete separation of concerns and enabling easy testing/mocking.
 *
 * <h2>Serialization Support:</h2>
 * All form data is automatically serialized through the delegation layer,
 * providing transparent persistence without GUI knowledge of storage mechanisms.
 *
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
=======
 * @author Quiz Application Team
 * @version 2.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class Theme extends JPanel {
    private final BusinesslogicaDelegation delegate;
    private final ModularStyleService styleService;

    private final JTextField themaField = new JTextField(20);
    private final JTextArea descriptionArea = new JTextArea(10, 20);
    private final JScrollPane scrollPane = new JScrollPane(descriptionArea);
    private final JList<String> themaList = new JList<>();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JButton saveBtn = new JButton("Save Topic");
    private final JButton deleteBtn = new JButton("Delete Topic");
    private final JButton newBtn = new JButton("New Topic");
    private final JLabel messageLabel = new JLabel(" ");

    private Runnable onTopicChanged;



<<<<<<< HEAD
    public Theme(BusinesslogicaDelegation delegate, ModularStyleService styleService) {
        super(new BorderLayout());
        this.delegate = delegate;
        this.styleService = styleService; // No styling
        initializeTheme();
    }

//    public Theme(BusinesslogicaDelegation delegate, ModularStyleService styleService) {
//        super(new BorderLayout());
//        this.delegate = delegate;
//        this.styleService = styleService;
//        initializeTheme();
//        applyModularStyling();
//    }
=======
    public Theme(BusinesslogicaDelegation delegate) {
        super(new BorderLayout());
        this.delegate = delegate;
        this.styleService = null; // No styling
        initializeTheme();
    }

    public Theme(BusinesslogicaDelegation delegate, ModularStyleService styleService) {
        super(new BorderLayout());
        this.delegate = delegate;
        this.styleService = styleService;
        initializeTheme();
        applyModularStyling();
    }
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd

    /**
     * Preferred constructor using the package delegate facade. Uses the business
     * interface and the style service factory from GuiModuleDelegate.
     */
    public Theme(GuiModuleDelegate modules) {
        this(modules.business(), modules.newStyleService());
    }

    private void initializeTheme() {
        // Setup list model and list
        themaList.setModel(listModel);
        themaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        themaList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onListSelectionChanged();
            }
        });

        // Actions via delegation (lambdas)
        saveBtn.addActionListener(e -> onSave());
        deleteBtn.addActionListener(e -> onDelete());
        newBtn.addActionListener(e -> clearForm());

        // Setup message label
        messageLabel.setForeground(Color.BLACK);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // === LEFT PANEL: Topic Input und Description ===
        JPanel leftPanel = new JPanel(new BorderLayout());

        // Topic Input
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Topic:"), BorderLayout.NORTH);
        topPanel.add(themaField, BorderLayout.CENTER);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Description
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        leftPanel.add(topPanel, BorderLayout.NORTH);
        leftPanel.add(centerPanel, BorderLayout.CENTER);

        // === RIGHT PANEL: Topic Selection Liste ===
        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel listLabel = new JLabel("Select Topic:");
        listLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        rightPanel.add(listLabel, BorderLayout.NORTH);

        JScrollPane listScrollPane = new JScrollPane(themaList);
        listScrollPane.setPreferredSize(new Dimension(250, 0)); // Feste Breite, variable Höhe
        listScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        rightPanel.add(listScrollPane, BorderLayout.CENTER);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0)); // Abstand links

        // === CENTER PANEL: Left und Right nebeneinander ===
        JPanel centerMainPanel = new JPanel(new BorderLayout());
        centerMainPanel.add(leftPanel, BorderLayout.CENTER);
        centerMainPanel.add(rightPanel, BorderLayout.EAST);

        // === BOTTOM SECTION: Message und Buttons (volle Breite) ===
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 15, 0)); // Gleichmäßige Verteilung über volle Breite

        // Set preferred height for buttons to make them more prominent
        saveBtn.setPreferredSize(new Dimension(0, 35));
        deleteBtn.setPreferredSize(new Dimension(0, 35));
        newBtn.setPreferredSize(new Dimension(0, 35));

        buttonPanel.add(saveBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(newBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messagePanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // === MAIN LAYOUT ===
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(centerMainPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        setPreferredSize(new java.awt.Dimension(750, 550)); // Optimierte Dimensionen

        reloadTopics();
    }

    private void onSave() {
        String title = themaField.getText().trim();
        String desc = descriptionArea.getText().trim();

        if (title.isEmpty()) {
            showMessage("Bitte geben Sie einen Titel ein!", Color.RED);
            return;
        }

        delegate.saveTheme(title, desc);
        reloadTopics();
        selectTopic(title);
        showMessage("Thema '" + title + "' erfolgreich gespeichert!", Color.GREEN);

        // Notify listeners that topics changed
        if (onTopicChanged != null) {
            onTopicChanged.run();
        }
    }

    private void onDelete() {
        String sel = getSelectedTopic();
        if (sel == null) {
            showMessage("Bitte wählen Sie ein Thema zum Löschen aus!", Color.RED);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
            "Möchten Sie das Thema '" + sel + "' wirklich löschen?",
            "Thema löschen",
            JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            delegate.deleteTheme(sel);
            reloadTopics();
            clearForm();
            showMessage("Thema '" + sel + "' erfolgreich gelöscht!", Color.GREEN);

            // Notify listeners that topics changed
            if (onTopicChanged != null) {
                onTopicChanged.run();
            }
        }
    }

    private void onListSelectionChanged() {
        String sel = getSelectedTopic();
        if (sel == null) return;
        themaField.setText(sel);
        descriptionArea.setText(delegate.getThemeDescription(sel));
        showMessage("Thema '" + sel + "' geladen", Color.BLUE);
    }

    /** Reloads topics from the delegation boundary and repopulates the list. */
    public void reloadTopics() {
        listModel.clear();
        for (String t : delegate.getAllTopics()) {
            listModel.addElement(t);
        }
    }

    private void selectTopic(String title) {
        if (title == null) return;
        themaList.setSelectedValue(title, true);
    }

    private void clearForm() {
        themaField.setText("");
        descriptionArea.setText("");
        themaList.clearSelection();
        showMessage("Neues Thema - Felder bereinigt", Color.BLUE);
    }

    public String getSelectedTopic() {
        return themaList.getSelectedValue();
    }

    private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);

        // Clear message after 3 seconds
        Timer timer = new Timer(3000, e -> {
            messageLabel.setText(" ");
            messageLabel.setForeground(Color.BLACK);
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Applies modular styling using lambda-based delegation.
     */
    private void applyModularStyling() {
        if (styleService == null) return;

<<<<<<< HEAD
        // Aktuelle Style-Konfiguration abrufen
        var config = styleService.getCurrentConfiguration();

        // Buttons stylen
        styleService.applyButtonTheme().apply(saveBtn, config.defaultButtonTheme);
        styleService.applyButtonTheme().apply(deleteBtn, config.defaultButtonTheme);
        styleService.applyButtonTheme().apply(newBtn, config.defaultButtonTheme);

        // Panel-Hintergrund
        if (config.defaultPanelTheme != null) {
            setBackground(config.defaultPanelTheme.backgroundColor);
            setBorder(config.defaultPanelTheme.border);
        }

        // Farbschema für Texte
        var colors = styleService.applyColorScheme().apply(config.colorScheme);
        messageLabel.setForeground(colors.text);
    }

//    private void applyModularStyling() {
//        if (styleService == null) return;
//
//        // Apply button styling through delegation
//        Function<String, JButton> buttonCreator = styleService.createStyledButton();
//
//        // Re-style existing buttons
//        styleService.applyButtonTheme().apply(saveBtn,
//            styleService.getCurrentConfiguration().defaultButtonTheme);
//        styleService.applyButtonTheme().apply(deleteBtn,
//            styleService.getCurrentConfiguration().defaultButtonTheme);
//        styleService.applyButtonTheme().apply(newBtn,
//            styleService.getCurrentConfiguration().defaultButtonTheme);
//
//        // Apply panel styling
//        StyleDelegate.PanelTheme panelTheme = styleService.getCurrentConfiguration().defaultPanelTheme;
//        if (panelTheme != null) {
//            setBackground(panelTheme.backgroundColor);
//            setBorder(panelTheme.border);
//        }
//
//        // Apply color scheme
//        StyleDelegate.ColorConfiguration colors = styleService.applyColorScheme()
//            .apply(styleService.getCurrentConfiguration().colorScheme);
//
//        // Update message label color
//        messageLabel.setForeground(colors.text);
//    }

=======
        // Apply button styling through delegation
        Function<String, JButton> buttonCreator = styleService.createStyledButton();

        // Re-style existing buttons
        styleService.applyButtonTheme().apply(saveBtn,
            styleService.getCurrentConfiguration().defaultButtonTheme);
        styleService.applyButtonTheme().apply(deleteBtn,
            styleService.getCurrentConfiguration().defaultButtonTheme);
        styleService.applyButtonTheme().apply(newBtn,
            styleService.getCurrentConfiguration().defaultButtonTheme);

        // Apply panel styling
        StyleDelegate.PanelTheme panelTheme = styleService.getCurrentConfiguration().defaultPanelTheme;
        if (panelTheme != null) {
            setBackground(panelTheme.backgroundColor);
            setBorder(panelTheme.border);
        }

        // Apply color scheme
        StyleDelegate.ColorConfiguration colors = styleService.applyColorScheme()
            .apply(styleService.getCurrentConfiguration().colorScheme);

        // Update message label color
        messageLabel.setForeground(colors.text);
    }

>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
    // Method to set callback for topic changes
    public void setOnTopicChanged(Runnable callback) {
        this.onTopicChanged = callback;
    }

    // Expose for orchestration if needed
    public JTextField getThemaField() { return themaField; }
    public JTextArea getDescriptionArea() { return descriptionArea; }
    public JList<String> getThemaList() { return themaList; }
}
