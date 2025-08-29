package guimodule;

import dbbl.BusinesslogicaDelegation;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.swing.*;

/**
 * Panel for creating and editing quiz questions with full form input.
 * 
 * Responsibilities:
 * - Captures user input for quiz question: title, question text, explanation, answers, correctness flags.
 * - Exposes clearForm() and setFormData() methods for external orchestration.
 * - Delegates save and delete operations to BusinesslogicaDelegation.
 * - Displays inline messages for success, errors, and form state feedback.
 * 
 * Design considerations:
 * - Pure delegation: all persistence operations are handled externally.
 * - Functional callbacks: Runnables and Suppliers provide decoupled orchestration.
 * - Swing UI components are organized with layouts for flexible resizing and alignment.
 * 
 * Dependencies:
 * - BusinesslogicaDelegation: for save/delete operations.
 * - QuizQuestion: data object representing a quiz question.
 * - Standard Swing components (JTextField, JTextArea, JCheckBox, JLabel, JButton, JPanel).
 * 
 * @author D.
 * @version 1.0
 */
public class CreateQuizQuestionsPanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Delegate for handling persistence operations */
    private final BusinesslogicaDelegation delegate;

    /** Provides the currently selected topic */
    private final Supplier<String> currentTopicSupplier;

    /** Callback invoked after successful save */
    private final Runnable onSaved;

    /** Callback invoked after successful deletion */
    private final Runnable onDeleted;

    /** Provides currently selected question index */
    private final IntSupplier selectedIndexSupplier;

    /** Text field for question title */
    private final JTextField titelField;

    /** Text area for question body */
    private final JTextArea frageArea;

    /** Text area for optional explanation */
    private final JTextArea erklaerungArea;

    /** Text fields for up to 4 answer options */
    private final JTextField[] antwortFields = new JTextField[4];

    /** Checkboxes to mark correct answers */
    private final JCheckBox[] correctBoxes = new JCheckBox[4];

    /** Label to display messages (success, error, info) */
    private final JLabel messageLabel = new JLabel(" ");

    /**
     * Constructor with full functional delegation.
     * Initializes UI, sets up input fields, buttons, and message label.
     * 
     * @param delegate handles save/delete operations
     * @param currentTopicSupplier provides currently selected topic
     * @param onSaved callback invoked after save
     * @param onDeleted callback invoked after delete
     * @param selectedIndexSupplier provides selected question index
     */
    public CreateQuizQuestionsPanel(
        BusinesslogicaDelegation delegate,
        Supplier<String> currentTopicSupplier,
        Runnable onSaved,
        Runnable onDeleted,
        IntSupplier selectedIndexSupplier
    ) {
        this.delegate = delegate;
        this.currentTopicSupplier = currentTopicSupplier;
        this.onSaved = onSaved != null ? onSaved : () -> {};
        this.onDeleted = onDeleted != null ? onDeleted : () -> {};
        this.selectedIndexSupplier = selectedIndexSupplier != null ? selectedIndexSupplier : () -> -1;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Create/Edit Quiz Question"));

        // Message label configuration
        messageLabel.setForeground(Color.BLUE);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 30));

        // Title field setup
        titelField = new JTextField(20);
        titelField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        leftPanel.add(new JLabel("Title"));
        leftPanel.add(titelField);
        leftPanel.add(Box.createVerticalStrut(10));

        // Question text area setup
        frageArea = new JTextArea(5, 20);
        frageArea.setLineWrap(true);
        frageArea.setWrapStyleWord(true);
        JScrollPane frageScroll = new JScrollPane(frageArea);
        frageScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        leftPanel.add(new JLabel("Question"));
        leftPanel.add(frageScroll);
        leftPanel.add(Box.createVerticalStrut(10));

        // Optional explanation setup
        erklaerungArea = new JTextArea(3, 20);
        erklaerungArea.setLineWrap(true);
        erklaerungArea.setWrapStyleWord(true);
        JScrollPane erklaerungScroll = new JScrollPane(erklaerungArea);
        erklaerungScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        leftPanel.add(new JLabel("Erklärung (optional)"));
        leftPanel.add(erklaerungScroll);
        leftPanel.add(Box.createVerticalStrut(20));

        // Answer fields with headers
        JPanel antwortPanel = new JPanel(new GridBagLayout());

        GridBagConstraints headerConstraints = new GridBagConstraints();
        headerConstraints.insets = new Insets(5, 0, 10, 5);
        headerConstraints.gridy = 0;

        headerConstraints.gridx = 0;
        headerConstraints.anchor = GridBagConstraints.WEST;
        antwortPanel.add(new JLabel(""), headerConstraints);

        headerConstraints.gridx = 1;
        headerConstraints.anchor = GridBagConstraints.CENTER;
        JLabel answersLabel = new JLabel("Answers");
        answersLabel.setFont(answersLabel.getFont().deriveFont(Font.BOLD));
        antwortPanel.add(answersLabel, headerConstraints);

        headerConstraints.gridx = 2;
        headerConstraints.anchor = GridBagConstraints.CENTER;
        JLabel correctLabel = new JLabel("Richtig");
        correctLabel.setFont(correctLabel.getFont().deriveFont(Font.BOLD));
        antwortPanel.add(correctLabel, headerConstraints);

        // Answer input rows
        for (int i = 0; i < 4; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(3, 0, 3, 5);
            c.gridy = i + 1; // offset for header
            c.gridx = 0;
            c.anchor = GridBagConstraints.WEST;
            antwortPanel.add(new JLabel("Answer " + (i + 1)), c);

            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            antwortFields[i] = new JTextField();
            antwortFields[i].setPreferredSize(new Dimension(250, 25));
            antwortPanel.add(antwortFields[i], c);

            c.gridx = 2;
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.anchor = GridBagConstraints.CENTER;
            correctBoxes[i] = new JCheckBox();
            antwortPanel.add(correctBoxes[i], c);
        }
        leftPanel.add(antwortPanel);
        leftPanel.add(Box.createVerticalStrut(20));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton saveBtn = new JButton("Save Question");
        JButton deleteBtn = new JButton("Delete Question");

        saveBtn.addActionListener(e -> saveQuestion());
        deleteBtn.addActionListener(e -> deleteQuestion());

        buttonPanel.add(saveBtn);
        buttonPanel.add(deleteBtn);

        // Bottom panel with message and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));

        leftPanel.add(bottomPanel);
        add(leftPanel, BorderLayout.CENTER);
    }

    /**
     * Collects form data into a structured object.
     * @return FormData object containing title, text, explanation, answers, and correctness flags
     */
    private FormData collectFormData() {
        String titel = titelField.getText();
        String frage = frageArea.getText();
        String erklaerung = erklaerungArea.getText();
        List<String> answers = new ArrayList<>(antwortFields.length);
        List<Boolean> correct = new ArrayList<>(correctBoxes.length);
        for (int i = 0; i < antwortFields.length; i++) {
            answers.add(antwortFields[i].getText());
            correct.add(correctBoxes[i].isSelected());
        }
        return new FormData(titel, frage, erklaerung, answers, correct);
    }

    /** Clears all form fields, resets checkboxes, and shows info message */
    public void clearForm() {
        titelField.setText("");
        frageArea.setText("");
        erklaerungArea.setText("");
        for (int i = 0; i < antwortFields.length; i++) {
            antwortFields[i].setText("");
            correctBoxes[i].setSelected(false);
        }
        showMessage("Form cleared - ready for new question", Color.BLUE);
    }

    /** Populates form fields with provided QuizQuestion data */
    public void setFormData(QuizQuestion q) {
        if (q == null) { clearForm(); return; }
        titelField.setText(q.getTitel());
        frageArea.setText(q.getFrageText());
        erklaerungArea.setText(q.getErklaerung() != null ? q.getErklaerung() : "");
        var antworten = q.getAntworten();
        var korrekt = q.getKorrekt();
        for (int i = 0; i < antwortFields.length; i++) {
            antwortFields[i].setText(i < antworten.size() ? antworten.get(i) : "");
            correctBoxes[i].setSelected(i < korrekt.size() && Boolean.TRUE.equals(korrekt.get(i)));
        }
        showMessage("Question '" + q.getTitel() + "' loaded", Color.BLUE);
    }

    /** Displays a message in the panel, auto-clears after 3 seconds */
    private void showMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
        Timer timer = new Timer(3000, e -> {
            messageLabel.setText(" ");
            messageLabel.setForeground(Color.BLACK);
        });
        timer.setRepeats(false);
        timer.start();
    }

    /** Handles saving question via delegate with validation */
    private void saveQuestion() {
        String topic = currentTopicSupplier != null ? currentTopicSupplier.get() : null;
        if (topic == null || topic.isBlank()) {
            showMessage("Please select a topic first!", Color.RED);
            return;
        }

        var data = collectFormData();
        if (data.title.trim().isEmpty()) {
            showMessage("Please enter a title!", Color.RED);
            return;
        }
        if (data.text.trim().isEmpty()) {
            showMessage("Please enter a question!", Color.RED);
            return;
        }

        delegate.saveQuestion(topic, data.title, data.text, data.explanation, data.answers, data.correct);
        showMessage("Question '" + data.title + "' saved successfully!", Color.GREEN);
        onSaved.run();
    }

    /** Handles deleting question via delegate */
    private void deleteQuestion() {
        String topic = currentTopicSupplier != null ? currentTopicSupplier.get() : null;
        int idx = selectedIndexSupplier.getAsInt();
        if (topic == null || idx < 0) {
            showMessage("Please select a question to delete!", Color.RED);
            return;
        }
        delegate.deleteQuestion(topic, idx);
        showMessage("Question deleted successfully!", Color.GREEN);
        onDeleted.run();
    }

    /** Internal DTO for capturing form data */
    private static class FormData {
        final String title;
        final String text;
        final String explanation;
        final List<String> answers;
        final List<Boolean> correct;
        FormData(String title, String text, String explanation, List<String> answers, List<Boolean> correct) {
            this.title = title;
            this.text = text;
            this.explanation = explanation;
            this.answers = answers;
            this.correct = correct;
        }
    }
}