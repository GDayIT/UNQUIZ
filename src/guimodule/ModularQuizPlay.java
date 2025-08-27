package guimodule;

import dbbl.BusinesslogicaDelegation;
import dbbl.RepoQuizeeQuestions;
import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.swing.*;

/**
 * Modular Quiz Play Panel with delegation, persistence and lambda-based operations.
 * 
 * Features:
 * - Random quiz mode (all themes mixed)
 * - Theme-specific quiz mode
 * - Persistent game statistics
 * - Lambda-based event handling
 * - Functional composition for quiz logic
 * 
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class ModularQuizPlay extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final BusinesslogicaDelegation delegate;
    private final ModularQuizStatistics statisticsService;
    private final GuiModuleDelegate modules;
    
    // UI Components
    private final JLabel questionLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel answersPanel = new JPanel(new GridLayout(0, 1, 5, 5));
    private final JButton nextBtn = new JButton("Nächste Frage");
    private final JButton showAnswerBtn = new JButton("Antwort zeigen");
    private final JButton stopQuizBtn = new JButton("Quiz beenden");
    private final JButton changeThemeBtn = new JButton("Thema wechseln");
    private final JLabel feedbackLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel scoreLabel = new JLabel("Score: 0/0", SwingConstants.LEFT);
    private final JComboBox<String> themeSelector = new JComboBox<>();
    private final JButton startQuizBtn = new JButton("Quiz starten");
    private final JButton randomQuizBtn = new JButton("Random Quiz");
    private final JButton leitnerQuizBtn = new JButton("🧠 Leitner-Modus");
    
    // Game State
    private List<RepoQuizeeQuestions> currentQuestions = new ArrayList<>();
    private int currentIndex = -1;
    private int correctAnswers = 0;
    private int totalQuestions = 0;
    private int answeredQuestions = 0;
    private String selectedAnswer = null;
    private boolean answerShown = false;
    private boolean questionAnswered = false;
    private String currentTheme = null;

    // Time tracking
    private long questionStartTime = 0;
    private long totalQuizTime = 0;
    private long quizStartTime = 0;
    
    // Lambda-based operations
    private Function<String, List<RepoQuizeeQuestions>> getQuestionsForTheme;
    private Supplier<List<RepoQuizeeQuestions>> getAllQuestions;
    private Consumer<QuizResult> recordQuizResult;
    private Runnable onQuizCompleted;

    // Adaptive Leitner System
    private AdaptiveLeitnerSystem leitnerSystem;
    
    /**
     * Quiz result data for statistics.
     */
    public static class QuizResult implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String theme;
        public final String questionTitle;
        public final String userAnswer;
        public final String correctAnswer;
        public final boolean isCorrect;
        public final long timestamp;
        public final long answerTimeMs; // Zeit in Millisekunden für diese Antwort

        public QuizResult(String theme, String questionTitle, String userAnswer,
                         String correctAnswer, boolean isCorrect, long answerTimeMs) {
            this.theme = theme;
            this.questionTitle = questionTitle;
            this.userAnswer = userAnswer;
            this.correctAnswer = correctAnswer;
            this.isCorrect = isCorrect;
            this.timestamp = System.currentTimeMillis();
            this.answerTimeMs = answerTimeMs;
        }

        /**
         * Get answer time in seconds.
         */
        public double getAnswerTimeSeconds() {
            return answerTimeMs / 1000.0;
        }
    }

    @Deprecated
    public ModularQuizPlay(BusinesslogicaDelegation delegate) {
        this.modules = GuiModuleDelegate.createDefault();
        this.delegate = delegate;
        this.statisticsService = modules.newStatisticsService();
        this.leitnerSystem = modules.newLeitnerSystem();
        initializeLambdas();
        initUI();
        loadThemes();
    }

    public ModularQuizPlay(GuiModuleDelegate modules) {
        this.modules = modules;
        this.delegate = modules.business();
        this.statisticsService = modules.newStatisticsService();
        this.leitnerSystem = modules.newLeitnerSystem();
        initializeLambdas();
        initUI();
        loadThemes();
    }
    
    /**
     * Initialize lambda-based operations.
     */
    private void initializeLambdas() {
        // Get questions for specific theme
        getQuestionsForTheme = theme -> {
            if (theme == null || theme.equals("Alle Themen")) {
                return getAllQuestions.get();
            }
            List<String> questionTitles = delegate.getQuestionTitles(theme);
            List<RepoQuizeeQuestions> questions = new ArrayList<>();
            for (int i = 0; i < questionTitles.size(); i++) {
                RepoQuizeeQuestions q = delegate.getQuestion(theme, i);
                if (q != null) questions.add(q);
            }
            return questions;
        };
        
        // Get all questions from all themes
        getAllQuestions = () -> {
            List<RepoQuizeeQuestions> allQuestions = new ArrayList<>();
            List<String> themes = delegate.getAllTopics();
            for (String theme : themes) {
                allQuestions.addAll(getQuestionsForTheme.apply(theme));
            }
            return allQuestions;
        };
        
        // Record quiz result for statistics
        recordQuizResult = result -> {
            // Record to internal statistics service
            statisticsService.recordAnswer(result);

            // Also record to external statistics panel if connected
            if (externalStatsPanel != null) {
                externalStatsPanel.recordAnswer(result);
            }

            System.out.println("Recorded: " + result.questionTitle + " -> " +
                             (result.isCorrect ? "CORRECT" : "WRONG"));
        };
        
        // Quiz completion callback
        onQuizCompleted = () -> {
            showQuizSummary();
            statisticsService.saveStatistics();
        };
    }
    
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setPreferredSize(new Dimension(900, 700));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // === TOP PANEL: Theme Selection (wie im Bild) ===
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // === CENTER: Split Layout (wie im Bild) ===
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(8);

        // Left: Question and Answers
        JPanel leftPanel = createQuestionPanel();
        splitPane.setLeftComponent(leftPanel);

        // Right: Feedback and Info
        JPanel rightPanel = createFeedbackPanel();
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        // === BOTTOM: Controls ===
        JPanel bottomPanel = createControlPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        // === EVENT HANDLERS ===
        startQuizBtn.addActionListener(e -> startThemeQuiz());
        randomQuizBtn.addActionListener(e -> startRandomQuiz());
        leitnerQuizBtn.addActionListener(e -> startLeitnerQuiz());
        nextBtn.addActionListener(e -> showNextQuestion());
        showAnswerBtn.addActionListener(e -> showCorrectAnswer());
        stopQuizBtn.addActionListener(e -> stopQuiz());
        changeThemeBtn.addActionListener(e -> changeTheme());

        // Initial state
        resetQuizState();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Quiz Konfiguration"));

        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftSection.add(new JLabel("Thema:"));
        themeSelector.setPreferredSize(new Dimension(200, 25));
        themeSelector.addItem("Alle Themen");
        leftSection.add(themeSelector);

        JPanel centerSection = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startQuizBtn.setPreferredSize(new Dimension(120, 30));
        randomQuizBtn.setPreferredSize(new Dimension(120, 30));
        leitnerQuizBtn.setPreferredSize(new Dimension(140, 30));
        centerSection.add(startQuizBtn);
        centerSection.add(randomQuizBtn);
        centerSection.add(leitnerQuizBtn);

        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        scoreLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        rightSection.add(scoreLabel);

        panel.add(leftSection, BorderLayout.WEST);
        panel.add(centerSection, BorderLayout.CENTER);
        panel.add(rightSection, BorderLayout.EAST);

        return panel;
    }

    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Frage"));

        // Question text area (wie im Bild)
        questionLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        questionLabel.setOpaque(true);
        questionLabel.setBackground(new Color(248, 248, 248));
        questionLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.add(questionLabel, BorderLayout.NORTH);

        // Answers section (wie im Bild)
        JPanel answersContainer = new JPanel(new BorderLayout());
        answersContainer.setBorder(BorderFactory.createTitledBorder("Mögliche Antworten"));

        answersPanel.setLayout(new GridLayout(0, 1, 5, 8));
        answersPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JScrollPane answersScroll = new JScrollPane(answersPanel);
        answersScroll.setPreferredSize(new Dimension(0, 200));
        answersScroll.setBorder(BorderFactory.createLoweredBevelBorder());
        answersContainer.add(answersScroll, BorderLayout.CENTER);

        panel.add(answersContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Feedback & Information"));

        // Feedback area (wie im Bild)
        feedbackLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        feedbackLabel.setVerticalAlignment(SwingConstants.TOP);
        feedbackLabel.setHorizontalAlignment(SwingConstants.LEFT);
        feedbackLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        feedbackLabel.setOpaque(true);
        feedbackLabel.setBackground(Color.WHITE);

        JScrollPane feedbackScroll = new JScrollPane(feedbackLabel);
        feedbackScroll.setPreferredSize(new Dimension(0, 450)); // Vergrößert von 300 auf 450
        feedbackScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        panel.add(feedbackScroll, BorderLayout.CENTER);

        // Progress info
        JPanel progressPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        progressPanel.setBorder(BorderFactory.createTitledBorder("Fortschritt"));

        JLabel currentQuestionLabel = new JLabel("Frage: -/-");
        JLabel timeLabel = new JLabel("Zeit: --:--");
        JLabel accuracyLabel = new JLabel("Genauigkeit: --%");

        progressPanel.add(currentQuestionLabel);
        progressPanel.add(timeLabel);
        progressPanel.add(accuracyLabel);

        panel.add(progressPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        showAnswerBtn.setPreferredSize(new Dimension(120, 35));
        nextBtn.setPreferredSize(new Dimension(120, 35));
        stopQuizBtn.setPreferredSize(new Dimension(120, 35));
        changeThemeBtn.setPreferredSize(new Dimension(120, 35));

        // Style buttons
        styleButton(showAnswerBtn, new Color(70, 130, 180));
        styleButton(nextBtn, new Color(60, 179, 113));
        styleButton(stopQuizBtn, new Color(220, 53, 69));
        styleButton(changeThemeBtn, new Color(255, 193, 7));

        buttonPanel.add(showAnswerBtn);
        buttonPanel.add(nextBtn);
        buttonPanel.add(stopQuizBtn);
        buttonPanel.add(changeThemeBtn);

        panel.add(buttonPanel, BorderLayout.CENTER);

        return panel;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
    }
    
    public void loadThemes() {
        themeSelector.removeAllItems();
        themeSelector.addItem("Alle Themen");
        List<String> themes = delegate.getAllTopics();
        for (String theme : themes) {
            themeSelector.addItem(theme);
        }
    }
    
    private void startThemeQuiz() {
        currentTheme = (String) themeSelector.getSelectedItem();
        currentQuestions = getQuestionsForTheme.apply(currentTheme);
        startQuiz();
    }
    
    private void startRandomQuiz() {
        currentTheme = "Random";
        currentQuestions = getAllQuestions.get();
        Collections.shuffle(currentQuestions);
        startQuiz();
    }

    /**
     * Start Leitner-based quiz with due questions prioritized.
     */
    private void startLeitnerQuiz() {
        String selectedTheme = (String) themeSelector.getSelectedItem();

        if ("Alle Themen".equals(selectedTheme)) {
            currentQuestions = leitnerSystem.getAllDueQuestions();
            currentTheme = "Leitner-Modus (Alle Themen)";
        } else {
            currentQuestions = leitnerSystem.getDueQuestions(selectedTheme);
            currentTheme = "Leitner-Modus (" + selectedTheme + ")";
        }

        if (currentQuestions.isEmpty()) {
            questionLabel.setText("🎉 Keine fälligen Fragen! Alle Karten sind auf dem neuesten Stand.");
            JOptionPane.showMessageDialog(this,
                "<html><h3>🎉 Gratulation!</h3>" +
                "<p>Alle Fragen in diesem Thema sind auf dem neuesten Stand.</p>" +
                "<p>Keine Wiederholungen fällig.</p>" +
                "<p><i>Versuchen Sie es später wieder oder wählen Sie ein anderes Thema.</i></p></html>",
                "Leitner-System", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show info about due questions
        int dueCount = currentQuestions.size();
        JOptionPane.showMessageDialog(this,
            "<html><h3>🧠 Leitner-Modus gestartet</h3>" +
            "<p><b>" + dueCount + "</b> Fragen sind zur Wiederholung fällig.</p>" +
            "<p>Die Fragen sind nach Priorität sortiert:</p>" +
            "<ul><li>• Schwierige Fragen (Level 1-2) zuerst</li>" +
            "<li>• Überfällige Fragen haben Vorrang</li>" +
            "<li>• Adaptive Schwierigkeitsanpassung</li></ul></html>",
            "Leitner-System", JOptionPane.INFORMATION_MESSAGE);

        startQuiz();
    }
    
    private void startQuiz() {
        if (currentQuestions.isEmpty()) {
            questionLabel.setText("Keine Fragen verfügbar!");
            return;
        }

        Collections.shuffle(currentQuestions);
        currentIndex = -1;
        correctAnswers = 0;
        answeredQuestions = 0;
        totalQuestions = currentQuestions.size();

        // Start time tracking
        quizStartTime = System.currentTimeMillis();
        totalQuizTime = 0;

        updateScore();
        showNextQuestion();

        startQuizBtn.setEnabled(false);
        randomQuizBtn.setEnabled(false);
        themeSelector.setEnabled(false);
    }
    
    private void showNextQuestion() {
        currentIndex++;

        if (currentIndex >= currentQuestions.size()) {
            onQuizCompleted.run();
            return;
        }

        // Reset question state
        selectedAnswer = null;
        questionAnswered = false;

        RepoQuizeeQuestions question = currentQuestions.get(currentIndex);
        selectedAnswer = null;
        answerShown = false;

        // Start timing for this question
        questionStartTime = System.currentTimeMillis();

        // Display question
        questionLabel.setText((currentIndex + 1) + ". " + question.getFrageText());
        feedbackLabel.setText("");
        
        // Create answer options
        answersPanel.removeAll();
        ButtonGroup group = new ButtonGroup();

        List<String> answers = question.getAntworten();
        Collections.shuffle(answers);
        
        for (String answer : answers) {
            JRadioButton btn = new JRadioButton(answer);
            group.add(btn);
            answersPanel.add(btn);

            btn.addActionListener(e -> {
                selectedAnswer = answer;
                checkAnswer(question);

                // Automatisches Fortfahren nach Antwort
                javax.swing.Timer timer = new javax.swing.Timer(2000, evt -> {
                    if (currentIndex < currentQuestions.size() - 1) {
                        showNextQuestion();
                    } else {
                        showQuizSummary();
                    }
                });
                timer.setRepeats(false);
                timer.start();

                // Disable all buttons after selection
                for (Component comp : answersPanel.getComponents()) {
                    if (comp instanceof JRadioButton) {
                        comp.setEnabled(false);
                    }
                }
            });
        }

        nextBtn.setEnabled(false);
        showAnswerBtn.setEnabled(true);
        stopQuizBtn.setEnabled(true);
        changeThemeBtn.setEnabled(false);
        
        revalidate();
        repaint();
    }
    
    private void checkAnswer(RepoQuizeeQuestions question) {
        if (selectedAnswer == null || questionAnswered) return;

        questionAnswered = true;
        answeredQuestions++;

        String allCorrectAnswers = getCorrectAnswer(question);
        boolean isCorrect = isAnswerCorrect(question, selectedAnswer);
        String explanation = question.getErklaerung();

        StringBuilder feedback = new StringBuilder();

        if (isCorrect) {
            correctAnswers++;
            feedback.append("✅ Richtig!");
            feedbackLabel.setForeground(Color.GREEN);
        } else {
            feedback.append("❌ Falsch! Richtige Antwort(en): ").append(allCorrectAnswers);
            feedbackLabel.setForeground(Color.RED);
        }

        // Add explanation if available
        if (explanation != null && !explanation.trim().isEmpty()) {
            feedback.append("\n\n💡 Erklärung:\n").append(explanation);
        }

        feedbackLabel.setText("<html>" + feedback.toString().replace("\n", "<br>") + "</html>");

        // Calculate answer time
        long answerTime = System.currentTimeMillis() - questionStartTime;

        // Record result for statistics (only if answered)
        recordQuizResult.accept(new QuizResult(
            currentTheme,
            question.getTitel(),
            selectedAnswer,
            allCorrectAnswers,
            isCorrect,
            answerTime
        ));

        updateScore();
        nextBtn.setEnabled(true);
        showAnswerBtn.setEnabled(false);
    }
    
    private void showCorrectAnswer() {
        if (currentIndex < 0 || currentIndex >= currentQuestions.size()) return;
        
        RepoQuizeeQuestions question = currentQuestions.get(currentIndex);
        String correctAnswer = getCorrectAnswer(question);
        
        feedbackLabel.setText("💡 Richtige Antwort: " + correctAnswer);
        feedbackLabel.setForeground(Color.BLUE);
        
        answerShown = true;
        nextBtn.setEnabled(true);
        showAnswerBtn.setEnabled(false);
    }
    
    private String getCorrectAnswer(RepoQuizeeQuestions question) {
        List<String> answers = question.getAntworten();
        List<Boolean> correct = question.getKorrekt();
        List<String> correctAnswers = new ArrayList<>();

        for (int i = 0; i < answers.size() && i < correct.size(); i++) {
            if (Boolean.TRUE.equals(correct.get(i))) {
                correctAnswers.add(answers.get(i));
            }
        }

        if (correctAnswers.isEmpty()) {
            return !answers.isEmpty() ? answers.get(0) : "Keine Antwort";
        } else if (correctAnswers.size() == 1) {
            return correctAnswers.get(0);
        } else {
            return String.join(", ", correctAnswers);
        }
    }

    private boolean isAnswerCorrect(RepoQuizeeQuestions question, String selectedAnswer) {
        List<String> answers = question.getAntworten();
        List<Boolean> correct = question.getKorrekt();

        for (int i = 0; i < answers.size() && i < correct.size(); i++) {
            if (answers.get(i).equals(selectedAnswer) && Boolean.TRUE.equals(correct.get(i))) {
                return true;
            }
        }
        return false;
    }
    
    private void updateScore() {
        scoreLabel.setText("Score: " + correctAnswers + "/" + answeredQuestions);
    }

    private void showQuizSummary() {
        questionLabel.setText("Quiz beendet!");
        answersPanel.removeAll();

        // Only consider answered questions for statistics
        double percentage = answeredQuestions > 0 ? (correctAnswers * 100.0 / answeredQuestions) : 0;
        String summary = String.format(
            "Ergebnis: %d/%d richtig (%.1f%%)\nThema: %s\nBeantwortete Fragen: %d von %d",
            correctAnswers, answeredQuestions, percentage, currentTheme, answeredQuestions, totalQuestions
        );

        feedbackLabel.setText("<html>" + summary.replace("\n", "<br>") + "</html>");
        feedbackLabel.setForeground(percentage >= 70 ? Color.GREEN : Color.ORANGE);

        resetQuizState();
        revalidate();
        repaint();
    }
    
    private void resetQuizState() {
        startQuizBtn.setEnabled(true);
        randomQuizBtn.setEnabled(true);
        themeSelector.setEnabled(true);
        nextBtn.setEnabled(false);
        showAnswerBtn.setEnabled(false);
        stopQuizBtn.setEnabled(false);
        changeThemeBtn.setEnabled(true);
    }

    private void stopQuiz() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Möchten Sie das Quiz wirklich beenden?\nIhr Fortschritt geht verloren.",
            "Quiz beenden",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            resetQuizState();
            questionLabel.setText("Quiz beendet. Wählen Sie ein neues Thema.");
            feedbackLabel.setText("Endpunktzahl: " + correctAnswers + "/" + totalQuestions);
            answersPanel.removeAll();
            answersPanel.revalidate();
            answersPanel.repaint();
        }
    }

    private void changeTheme() {
        if (currentQuestions.isEmpty()) {
            // No quiz running, just show message
            feedbackLabel.setText("Wählen Sie ein neues Thema und starten Sie das Quiz");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Möchten Sie das Thema wechseln?\nDas aktuelle Quiz wird beendet.",
            "Thema wechseln",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            resetQuizState();
            questionLabel.setText("Wählen Sie ein neues Thema und starten Sie das Quiz");
            feedbackLabel.setText("Quiz beendet. Wählen Sie ein neues Thema.");
            answersPanel.removeAll();
            answersPanel.revalidate();
            answersPanel.repaint();
        }
    }
    
    /**
     * Get statistics service for external access.
     */
    public ModularQuizStatistics getStatisticsService() {
        return statisticsService;
    }

    /**
     * Set external statistics service (for integration with ModularStatisticsPanel).
     */
    public void setStatisticsService(ModularStatisticsPanel externalStatsPanel) {
        this.externalStatsPanel = externalStatsPanel;
    }

    // External statistics panel for data transfer
    private ModularStatisticsPanel externalStatsPanel;
}
