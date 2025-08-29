package guimodule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import dbbl.BusinesslogicaDelegation;

/**
 * Fully modular theme panel using lambda-based delegation and functional programming.
 * 
 * <p>This panel allows users to manage topics (themes) for quizzes with full CRUD
 * operations: create new topics, update existing topics, delete topics, and display
 * topic details. All operations delegate data handling to {@link BusinesslogicaDelegation},
 * ensuring separation between GUI and business logic.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Topic creation, editing, and deletion (CRUD)</li>
 *   <li>Topic selection via JList with selection listener</li>
 *   <li>Sorting topics alphabetically or by date via {@link ModularSortingService}</li>
 *   <li>Dynamic messaging via {@link JLabel} for user feedback</li>
 *   <li>Optional modular styling through {@link ModularStyleService}</li>
 *   <li>Integration with other modules via {@link GuiModuleDelegate}</li>
 * </ul>
 * 
 * <p>All user interactions are handled with lambda expressions and functional callbacks
 * for modularity and testability.</p>
 * 
 * <p>Topics, themes, questions, Leitner cards, and sessions are all connected
 * through the delegate, allowing centralized data management.</p>
 * 
 * <p>Author: D.Georgiou</p>
 * <p>Version: 1.0</p>
 */
public class Theme extends JPanel {

    // === DELEGATION AND SERVICES ===
    
    /** Business logic delegate for all CRUD operations and topic data access */
    private final BusinesslogicaDelegation delegate;
    
    /** Optional modular styling service for applying themes and styles */
    private final ModularStyleService styleService;
    
    /** Sorting service for topic list */
    private final ModularSortingService sortingService;
    
    /** Optional package-level delegate for modules */
    private final GuiModuleDelegate modules;

    // === UI COMPONENTS ===
    
    /** Input field for topic title */
    private final JTextField themaField = new JTextField(20);
    
    /** Text area for topic description */
    private final JTextArea descriptionArea = new JTextArea(10, 20);
    
    /** Scroll pane wrapping description text area */
    private final JScrollPane scrollPane = new JScrollPane(descriptionArea);
    
    /** JList displaying existing topics */
    private final JList<String> themaList = new JList<>();
    
    /** Default list model for the JList */
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    
    /** Mapping from displayed row to original index in delegate's topic list */
    private final List<Integer> originalIndexMapping = new ArrayList<>();
    
    /** Button to save a topic */
    private final JButton saveBtn = new JButton("Save Topic");
    
    /** Button to delete a topic */
    private final JButton deleteBtn = new JButton("Delete Topic");
    
    /** Button to create a new topic */
    private final JButton newBtn = new JButton("New Topic");
    
    /** Label to display messages (status, errors, info) */
    private final JLabel messageLabel = new JLabel(" ");
    
    /** Formatter for displaying dates */
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm ");

    /** Callback for notifying listeners when a topic is changed */
    private Runnable onTopicChanged;

    // === CONSTRUCTORS ===
    
    /**
     * Primary constructor using delegate and optional style service.
     * Initializes the GUI, list, buttons, and sorting service.
     * 
     * @param delegate Business logic delegate for CRUD operations
     * @param styleService Optional modular style service
     */
    public Theme(BusinesslogicaDelegation delegate, ModularStyleService styleService) {
        super(new BorderLayout());
        this.delegate = delegate;
        this.styleService = styleService;
        this.sortingService = createSortingService();
		this.modules = null;
        
        initializeTheme();
    }

    private ModularSortingService createSortingService() {
        return (modules != null) ? modules.newSortingService() : new ModularSortingService();
    }

	public Theme(BusinesslogicaDelegation delegate) {
        super(new BorderLayout());
        this.delegate = delegate;
        this.styleService = null; // No styling
		this.sortingService = new ModularSortingService();
		this.modules = null;
        initializeTheme();
    }



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
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        JLabel listLabel = new JLabel("Select Topic:");
        listLabel.setAlignmentX(CENTER_ALIGNMENT);
        rightPanel.add(listLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0,5))); // Abstand

        JPanel sortingPanel = createSortingPanel();
        sortingPanel.setAlignmentX(CENTER_ALIGNMENT);
        rightPanel.add(sortingPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0,5))); // Abstand

        JScrollPane listScrollPane = new JScrollPane(themaList);
        listScrollPane.setPreferredSize(new Dimension(250, 400));
        rightPanel.add(listScrollPane);
        

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
    
    /**
     * Creates the sorting panel with alphabetical and date sorting buttons.
     */
    private JPanel createSortingPanel() {
        JPanel sortingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));

        // === ALPHABETICAL SORTING BUTTON ===
        JButton alphabeticalBtn = new JButton("🔤 Alphabet A→Z");
        alphabeticalBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        alphabeticalBtn.setPreferredSize(new Dimension(140, 25));
        alphabeticalBtn.setToolTipText("Alphabetisch sortieren");

        alphabeticalBtn.addActionListener(e -> {
            // Toggle alphabetical sort direction
            SortCriteria current = sortingService.getCurrentSortCriteria();
            SortDirection newDirection;

            if (current.type == SortType.ALPHABETICAL) {
                newDirection = current.direction.toggle();
            } else {
                newDirection = SortDirection.ASCENDING;
            }

            SortCriteria newCriteria = new SortCriteria(
                SortType.ALPHABETICAL, newDirection
            );

            sortingService.updateSortCriteria(newCriteria);
            updateAlphabeticalButtonText(alphabeticalBtn, newDirection);
            applySortingToList();
        });

        // === DATE SORTING BUTTON ===
        JButton dateBtn = new JButton("📅 Datum ↑");
        dateBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        dateBtn.setPreferredSize(new Dimension(140, 25));
        dateBtn.setToolTipText("Nach Datum sortieren");

        dateBtn.addActionListener(e -> {
            // Toggle date sort direction
            SortCriteria current = sortingService.getCurrentSortCriteria();
            SortDirection newDirection;

            if (current.type == SortType.DATE) {
                newDirection = current.direction.toggle();
            } else {
                newDirection = SortDirection.ASCENDING;
            }

            SortCriteria newCriteria = new SortCriteria(
                SortType.DATE, newDirection
            );

            sortingService.updateSortCriteria(newCriteria);
            updateDateButtonText(dateBtn, newDirection);
            applySortingToList();
        });

        sortingPanel.add(alphabeticalBtn);
        sortingPanel.add(dateBtn);

        // Initialize button texts
        SortCriteria currentCriteria = sortingService.getCurrentSortCriteria();
        if (currentCriteria.type == SortType.ALPHABETICAL) {
            updateAlphabeticalButtonText(alphabeticalBtn, currentCriteria.direction);
        }
        if (currentCriteria.type == SortType.DATE) {
            updateDateButtonText(dateBtn, currentCriteria.direction);
        }

        return sortingPanel;
    }
    
    
    /**
     * Updates alphabetical button text based on sort direction.
     */
    private void updateAlphabeticalButtonText(JButton button, SortDirection direction) {
        if (direction == SortDirection.ASCENDING) {
            button.setText("🔤 Alphabet A→Z");
            button.setToolTipText("Alphabetisch aufsteigend (klicken für absteigend)");
        } else {
            button.setText("🔤 Alphabet Z→A");
            button.setToolTipText("Alphabetisch absteigend (klicken für aufsteigend)");
        }
    }

    /**
     * Updates date button text based on sort direction.
     */
    private void updateDateButtonText(JButton button, SortDirection direction) {
        if (direction == SortDirection.ASCENDING) {
            button.setText("📅 Datum ↑");
            button.setToolTipText("Von früh nach spät (klicken für absteigend)");
        } else {
            button.setText("📅 Datum ↓");
            button.setToolTipText("Von spät nach früh (klicken für aufsteigend)");
        }
    }
    
    
    
    
    
    /**
     * Applies current sorting to the question list.
     */
    
    private void applySortingToList() {
        List<String> topics = new ArrayList<>(delegate.getAllTopics());

        // Alphabetische Sortierung
        SortCriteria current = sortingService.getCurrentSortCriteria();
        if (current.type == SortType.ALPHABETICAL) {
            topics.sort(String::compareToIgnoreCase);
            if (current.direction == SortDirection.DESCENDING) {
                Collections.reverse(topics);
            }
        }

        listModel.clear();
        for (String t : topics) {
            listModel.addElement(t);
        }
    }

    
   
    
    private void onSave() {
        String title = themaField.getText().trim();
        String desc = descriptionArea.getText().trim();

        if (title.isEmpty()) {
            showMessage("Bitte geben Sie einen Titel ein!", Color.RED);
            return;
        }

        // Wenn Description leer -> * voranstellen
        if (desc.isEmpty()) {
            desc = "*" + title;
            descriptionArea.setText(desc);
        }

        delegate.saveTheme(title, desc);
        reloadTopics();
        selectTopic(title);
        showMessage("Thema '" + title + "' erfolgreich gespeichert!", Color.GREEN);

        if (onTopicChanged != null) onTopicChanged.run();
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
        StyleDelegate.ColorConfiguration colors1 = styleService.applyColorScheme()
            .apply(styleService.getCurrentConfiguration().colorScheme);

        // Update message label color
        messageLabel.setForeground(colors1.text);
    }

    // Method to set callback for topic changes
    public void setOnTopicChanged(Runnable callback) {
        this.onTopicChanged = callback;
    }
    
    

    // Expose for orchestration if needed
    public JTextField getThemaField() { return themaField; }
    public JTextArea getDescriptionArea() { return descriptionArea; }
    public JList<String> getThemaList() { return themaList; }
}