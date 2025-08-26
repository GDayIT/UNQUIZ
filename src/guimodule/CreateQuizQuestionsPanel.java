package guimodule;

import dbbl.BusinesslogicaDelegation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Question form panel (title, question text, answers, correct flags) using pure delegation.
 *
 * Responsibilities:
 * - Captures form input for a quiz question (title, text, answers, correctness flags)
 * - Exposes clearForm() and setFormData(QuizQuestion) for orchestration from outside
 * - Delegates save/delete operations via BusinesslogicaDelegation using callbacks/lambdas
 *
 * Interaction model:
 * - The panel depends only on the BusinesslogicaDelegation boundary and on
 *   small functional callbacks provided by its parent/orchestrator.
 * - No direct references to controllers or other UI components exist.
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0 
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class CreateQuizQuestionsPanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final BusinesslogicaDelegation delegate;
    private final Supplier<String> currentTopicSupplier;
    private final Runnable onSaved;
    private final Runnable onDeleted;
    private final IntSupplier selectedIndexSupplier;

    private final JTextField titelField;
    private final JTextArea frageArea;
    private final JTextArea erklaerungArea;
    private final JTextField[] antwortFields = new JTextField[4];
    private final JCheckBox[] correctBoxes = new JCheckBox[4];
    private final JLabel messageLabel = new JLabel(" ");

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

        // Setup message label
        messageLabel.setForeground(Color.BLUE);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 30)); // Increased bottom margin

        // Title
        titelField = new JTextField(20);
        titelField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        leftPanel.add(new JLabel("Title"));
        leftPanel.add(titelField);
        leftPanel.add(Box.createVerticalStrut(10));

        // Question
        frageArea = new JTextArea(5, 20);
        frageArea.setLineWrap(true);
        frageArea.setWrapStyleWord(true);
        JScrollPane frageScroll = new JScrollPane(frageArea);
        frageScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        leftPanel.add(new JLabel("Question"));
        leftPanel.add(frageScroll);
        leftPanel.add(Box.createVerticalStrut(10));

        // Explanation
        erklaerungArea = new JTextArea(3, 20);
        erklaerungArea.setLineWrap(true);
        erklaerungArea.setWrapStyleWord(true);
        JScrollPane erklaerungScroll = new JScrollPane(erklaerungArea);
        erklaerungScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        leftPanel.add(new JLabel("Erklärung (optional)"));
        leftPanel.add(erklaerungScroll);
        leftPanel.add(Box.createVerticalStrut(20)); // Increased spacing

        // Answers section with headers
        JPanel antwortPanel = new JPanel(new GridBagLayout());

        // Add column headers
        GridBagConstraints headerConstraints = new GridBagConstraints();
        headerConstraints.insets = new Insets(5, 0, 10, 5);
        headerConstraints.gridy = 0;

        headerConstraints.gridx = 0;
        headerConstraints.anchor = GridBagConstraints.WEST;
        antwortPanel.add(new JLabel(""), headerConstraints); // Empty for answer number column

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

        // Add answer fields
        for (int i = 0; i < 4; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(3, 0, 3, 5);
            c.gridy = i + 1; // Start from row 1 (after headers)
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
        leftPanel.add(Box.createVerticalStrut(20)); // Increased spacing

        // Buttons with improved layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton saveBtn = new JButton("Save Question");
        JButton deleteBtn = new JButton("Delete Question");

        saveBtn.addActionListener(e -> {
            String topic = currentTopicSupplier != null ? currentTopicSupplier.get() : null;
            if (topic == null || topic.isBlank()) {
                showMessage("Bitte wählen Sie zuerst ein Thema aus!", Color.RED);
                return;
            }

            var data = collectFormData();
            if (data.title.trim().isEmpty()) {
                showMessage("Bitte geben Sie einen Titel ein!", Color.RED);
                return;
            }
            if (data.text.trim().isEmpty()) {
                showMessage("Bitte geben Sie eine Frage ein!", Color.RED);
                return;
            }

            delegate.saveQuestion(topic, data.title, data.text, data.explanation, data.answers, data.correct);
            showMessage("Frage '" + data.title + "' erfolgreich gespeichert!", Color.GREEN);
            onSaved.run();
        });

        deleteBtn.addActionListener(e -> {
            String topic = currentTopicSupplier != null ? currentTopicSupplier.get() : null;
            int idx = selectedIndexSupplier.getAsInt();
            if (topic == null || idx < 0) {
                showMessage("Bitte wählen Sie eine Frage zum Löschen aus!", Color.RED);
                return;
            }
            delegate.deleteQuestion(topic, idx);
            showMessage("Frage erfolgreich gelöscht!", Color.GREEN);
            onDeleted.run();
        });

        buttonPanel.add(saveBtn);
        buttonPanel.add(deleteBtn);

        // Bottom panel with message and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0)); // 30px bottom margin

        leftPanel.add(bottomPanel);

        add(leftPanel, BorderLayout.CENTER);
    }

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

    /** Clears all input fields (title, question, explanation, answers, flags). */
    public void clearForm() {
        titelField.setText("");
        frageArea.setText("");
        erklaerungArea.setText("");
        for (int i = 0; i < antwortFields.length; i++) {
            antwortFields[i].setText("");
            correctBoxes[i].setSelected(false);
        }
        showMessage("Formular bereinigt - bereit für neue Frage", Color.BLUE);
    }

    /** Fills the form with the provided question data. */
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
        showMessage("Frage '" + q.getTitel() + "' geladen", Color.BLUE);
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

    // Helper DTO
    private static class FormData {
        final String title; final String text; final String explanation; final List<String> answers; final List<Boolean> correct;
        FormData(String title, String text, String explanation, List<String> answers, List<Boolean> correct) {
            this.title = title; this.text = text; this.explanation = explanation; this.answers = answers; this.correct = correct;
        }
    }
}
