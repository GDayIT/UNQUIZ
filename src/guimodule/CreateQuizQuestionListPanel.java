package guimodule;

import dbbl.BusinesslogicaDelegation;
import dbbl.RepoQuizeeQuestions;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

/**
 * Topic selector and question titles list using pure delegation and callbacks.
 */
public class CreateQuizQuestionListPanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final BusinesslogicaDelegation delegate;
    private final ModularSortingService sortingService;
    private final GuiModuleDelegate modules;

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> fragenListe = new JList<>(listModel);
    // Mapping from displayed row to original index in delegate's question list
    private final List<Integer> originalIndexMapping = new ArrayList<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final JComboBox<String> themaDropdown = new JComboBox<>();

    private Runnable onNewQuestion;
    private BiConsumer<String, Integer> onQuestionSelected;

    @Deprecated
    public CreateQuizQuestionListPanel(BusinesslogicaDelegation delegate) {
        this.delegate = delegate;
        this.modules = null;
        this.sortingService = createSortingService();
        setLayout(new BorderLayout());

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel themaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton anzeigenBtn = new JButton("Show Topic");
        anzeigenBtn.addActionListener(e -> refreshListForSelectedTopic());
        themaPanel.add(themaDropdown);
        themaPanel.add(anzeigenBtn);
        rightPanel.add(themaPanel);

        rightPanel.add(new JLabel("Questions for Topic"));

        // === SORTING BUTTONS ===
        JPanel sortingPanel = createSortingPanel();
        rightPanel.add(sortingPanel);

        JScrollPane listScroll = new JScrollPane(fragenListe);
        listScroll.setPreferredSize(new Dimension(350, 300));
        rightPanel.add(listScroll);

        // Button panel with consistent spacing to match CreateQuizQuestionsPanel
        JButton neueFrageBtn = new JButton("New Question");
        neueFrageBtn.addActionListener(e -> { if (onNewQuestion != null) onNewQuestion.run(); });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(neueFrageBtn);

        // Bottom panel with consistent margin to align with other panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0)); // 30px bottom margin to match

        rightPanel.add(bottomPanel);

        fragenListe.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewIdx = fragenListe.getSelectedIndex();
                if (viewIdx >= 0 && onQuestionSelected != null) {
                    // Map view index back to original index of the question in the delegate's list
                    int originalIdx = (viewIdx < originalIndexMapping.size()) ? originalIndexMapping.get(viewIdx) : viewIdx;
                    onQuestionSelected.accept(getSelectedTopic(), originalIdx);
                }
            }
        });

        add(rightPanel, BorderLayout.CENTER);

        reloadTopics();
    }

    public CreateQuizQuestionListPanel(GuiModuleDelegate modules) {
        this.modules = modules;
        this.delegate = modules.business();
        this.sortingService = createSortingService();
        setLayout(new BorderLayout());

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel themaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton anzeigenBtn = new JButton("Show Topic");
        anzeigenBtn.addActionListener(e -> refreshListForSelectedTopic());
        themaPanel.add(themaDropdown);
        themaPanel.add(anzeigenBtn);
        rightPanel.add(themaPanel);

        rightPanel.add(new JLabel("Questions for Topic"));

        // === SORTING BUTTONS ===
        JPanel sortingPanel = createSortingPanel();
        rightPanel.add(sortingPanel);

        JScrollPane listScroll = new JScrollPane(fragenListe);
        listScroll.setPreferredSize(new Dimension(350, 300));
        rightPanel.add(listScroll);

        // Button panel with consistent spacing to match CreateQuizQuestionsPanel
        JButton neueFrageBtn = new JButton("New Question");
        neueFrageBtn.addActionListener(e -> { if (onNewQuestion != null) onNewQuestion.run(); });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(neueFrageBtn);

        // Bottom panel with consistent margin to align with other panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0)); // 30px bottom margin to match

        rightPanel.add(bottomPanel);

        fragenListe.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewIdx = fragenListe.getSelectedIndex();
                if (viewIdx >= 0 && onQuestionSelected != null) {
                    // Map view index back to original index of the question in the delegate's list
                    int originalIdx = (viewIdx < originalIndexMapping.size()) ? originalIndexMapping.get(viewIdx) : viewIdx;
                    onQuestionSelected.accept(getSelectedTopic(), originalIdx);
                }
            }
        });

        add(rightPanel, BorderLayout.CENTER);

        reloadTopics();
    }

    private ModularSortingService createSortingService() {
        return (modules != null) ? modules.newSortingService() : new ModularSortingService();
    }

    /**
     * Reload topics from the delegate into the dropdown.
     * Preserves current selection if possible.
     */
    public void reloadTopics() {
        String currentSelection = getSelectedTopic();
        themaDropdown.removeAllItems();
        for (String t : delegate.getAllTopics()) {
            themaDropdown.addItem(t);
        }
        // Try to restore previous selection if it still exists
        if (currentSelection != null) {
            themaDropdown.setSelectedItem(currentSelection);
        }
    }

    /**
     * Refresh the question titles list for the currently selected topic with sorting applied.
     */
    public void refreshListForSelectedTopic() {
        applySortingToList(); // Use the new sorting-aware method
    }

    public void refreshList(List<String> titles) {
        listModel.clear();
        if (titles != null) titles.forEach(listModel::addElement);
    }

    public void setOnNewQuestion(Runnable r) { this.onNewQuestion = r; }
    public void setOnQuestionSelected(BiConsumer<String, Integer> c) { this.onQuestionSelected = c; }

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
        String selectedTopic = getSelectedTopic();
        if (selectedTopic != null && !selectedTopic.isEmpty()) {
            // Get original titles for mapping to indices
            List<String> originalTitles = delegate.getQuestionTitles(selectedTopic);

            // Build unsorted question objects alongside their original indices via id mapping
            Map<String, Integer> idToIndex = new HashMap<>();
            for (int i = 0; i < originalTitles.size(); i++) {
                idToIndex.put(selectedTopic + ":" + originalTitles.get(i), i);
            }

            List<RepoQuizeeQuestions> questions = new ArrayList<>();
            for (int i = 0; i < originalTitles.size(); i++) {
                try {
                    RepoQuizeeQuestions q = delegate.getQuestion(selectedTopic, i);
                    if (q != null) {
                        questions.add(q);
                    }
                } catch (Exception ex) {
                    // ignore broken entries
                }
            }

            // Apply sorting and filtering
            List<RepoQuizeeQuestions> sortedQuestions = sortingService.applySortingAndFiltering(questions);

            // Update list model and index mapping; display title + date
            listModel.clear();
            originalIndexMapping.clear();
            for (RepoQuizeeQuestions q : sortedQuestions) {
                String id = (q.getThema() != null ? q.getThema() : selectedTopic) + ":" + q.getTitel();
                Integer origIdx = idToIndex.getOrDefault(id, -1);
                originalIndexMapping.add(origIdx >= 0 ? origIdx : 0);
                String dateStr = q.getCreatedAt() != null ? q.getCreatedAt().format(dateFormatter) : "";
                listModel.addElement(q.getTitel() + "  (" + dateStr + ")");
            }
        }
    }

    public String getSelectedTopic() {
        Object sel = themaDropdown.getSelectedItem();
        return sel != null ? sel.toString() : null;
    }

    public JList<String> getFragenListe() { return fragenListe; }
    public DefaultListModel<String> getListModel() { return listModel; }
    public JComboBox<String> getThemaDropdown() { return themaDropdown; }
}
