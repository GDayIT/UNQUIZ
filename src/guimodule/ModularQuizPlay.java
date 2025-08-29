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
 * ModularQuizPlay ist ein interaktives Quiz-Panel, das folgende Features bietet:
 * <ul>
 *   <li>Random Quiz-Modus (alle Themen gemischt)</li>
 *   <li>Themen-spezifischer Quiz-Modus</li>
 *   <li>Persistente Spielstatistiken</li>
 *   <li>Lambda-basierte Event-Handler</li>
 *   <li>Leitner-System für adaptive Wiederholung</li>
 * </ul>
 * 
 * <p>Die Klasse stellt ein voll funktionsfähiges GUI bereit, inklusive 
 * Anzeige von Fragen, Auswahlmöglichkeiten, Feedback und Fortschrittsanzeige.</p>
 * 
 * <p>Autor: D.Georgiou</p>
 * @version 1.0
 */
public class ModularQuizPlay extends JPanel implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- Core Services ---
    private final BusinesslogicaDelegation delegate;
    private final ModularQuizStatistics statisticsService;
    private final GuiModuleDelegate modules;

    // --- UI Components ---
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

    // --- Game State ---
    private List<RepoQuizeeQuestions> currentQuestions = new ArrayList<>();
    private int currentIndex = -1;
    private int correctAnswers = 0;
    private int totalQuestions = 0;
    private int answeredQuestions = 0;
    private String selectedAnswer = null;
    private boolean answerShown = false;
    private boolean questionAnswered = false;
    private String currentTheme = null;

    // --- Time Tracking ---
    private long questionStartTime = 0;
    private long totalQuizTime = 0;
    private long quizStartTime = 0;

    // --- Lambda-based operations ---
    private Function<String, List<RepoQuizeeQuestions>> getQuestionsForTheme;
    private Supplier<List<RepoQuizeeQuestions>> getAllQuestions;
    private Consumer<QuizResult> recordQuizResult;
    private Runnable onQuizCompleted;

    // --- Leitner System ---
    private AdaptiveLeitnerSystem leitnerSystem;

    // --- External statistics integration ---
    private ModularStatisticsPanel externalStatsPanel;

    // === INNER CLASSES ===

    /**
     * QuizResult speichert die Ergebnisse einer beantworteten Frage.
     */
    public static class QuizResult implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String theme;
        public final String questionTitle;
        public final String userAnswer;
        public final String correctAnswer;
        public final boolean isCorrect;
        public final long timestamp;
        public final long answerTimeMs;

        /**
         * Konstruktor für ein QuizResult.
         *
         * @param theme Thema der Frage
         * @param questionTitle Titel der Frage
         * @param userAnswer vom Benutzer ausgewählte Antwort
         * @param correctAnswer korrekte Antwort(en)
         * @param isCorrect ob die Antwort korrekt war
         * @param answerTimeMs Zeit in Millisekunden für die Beantwortung
         */
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
         * Gibt die Antwortzeit in Sekunden zurück.
         *
         * @return Zeit in Sekunden
         */
        public double getAnswerTimeSeconds() {
            return answerTimeMs / 1000.0;
        }
    }

    // === KONSTRUKTOREN ===

    /**
     * @deprecated Use ModularQuizPlay(GuiModuleDelegate) instead.
     */
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

    /**
     * Standardkonstruktor mit GuiModuleDelegate.
     * 
     * @param modules GUI- und Service-Delegation
     */
    public ModularQuizPlay(GuiModuleDelegate modules) {
        this.modules = modules;
        this.delegate = modules.business();
        this.statisticsService = modules.newStatisticsService();
        this.leitnerSystem = modules.newLeitnerSystem();
        initializeLambdas();
        initUI();
        loadThemes();
    }

    // === INITIALISIERUNG UND LAMBDA-SETUP ===

    /**
     * Initialisiert die Lambda-basierten Operationen für Fragen, Statistik und Quizabschluss.
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

        // Record quiz result
        recordQuizResult = result -> {
            statisticsService.recordAnswer(result);
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

    // === UI INITIALISIERUNG ===

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setPreferredSize(new Dimension(900, 700));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createTopPanel(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(8);
        splitPane.setLeftComponent(createQuestionPanel());
        splitPane.setRightComponent(createFeedbackPanel());
        add(splitPane, BorderLayout.CENTER);

        add(createControlPanel(), BorderLayout.SOUTH);

        // Event-Handler
        startQuizBtn.addActionListener(e -> startThemeQuiz());
        randomQuizBtn.addActionListener(e -> startRandomQuiz());
        leitnerQuizBtn.addActionListener(e -> startLeitnerQuiz());
        nextBtn.addActionListener(e -> showNextQuestion());
        showAnswerBtn.addActionListener(e -> showCorrectAnswer());
        stopQuizBtn.addActionListener(e -> stopQuiz());
        changeThemeBtn.addActionListener(e -> changeTheme());

        resetQuizState();
    }

    // === METHODEN FÜR UI PANEL CREATION ===
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
 // === UI PANEL CREATION ===

    /**
     * Creates the question panel displaying the current question and answer options.
     * @return JPanel the configured question panel
     */
    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Question"));

        questionLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        questionLabel.setOpaque(true);
        questionLabel.setBackground(new Color(248, 248, 248));
        questionLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.add(questionLabel, BorderLayout.NORTH);

        JPanel answersContainer = new JPanel(new BorderLayout());
        answersContainer.setBorder(BorderFactory.createTitledBorder("Possible Answers"));
        answersPanel.setLayout(new GridLayout(0, 1, 5, 8));
        answersPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JScrollPane answersScroll = new JScrollPane(answersPanel);
        answersScroll.setPreferredSize(new Dimension(0, 200));
        answersScroll.setBorder(BorderFactory.createLoweredBevelBorder());
        answersContainer.add(answersScroll, BorderLayout.CENTER);
        panel.add(answersContainer, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the feedback panel displaying explanations and progress information.
     * @return JPanel the configured feedback panel
     */
    private JPanel createFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Feedback & Information"));

        feedbackLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        feedbackLabel.setVerticalAlignment(SwingConstants.TOP);
        feedbackLabel.setHorizontalAlignment(SwingConstants.LEFT);
        feedbackLabel.setOpaque(true);
        feedbackLabel.setBackground(Color.WHITE);
        feedbackLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane feedbackScroll = new JScrollPane(feedbackLabel);
        feedbackScroll.setPreferredSize(new Dimension(0, 450));
        feedbackScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panel.add(feedbackScroll, BorderLayout.CENTER);

        JPanel progressPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        progressPanel.setBorder(BorderFactory.createTitledBorder("Progress"));
        progressPanel.add(new JLabel("Question: -/-"));
        progressPanel.add(new JLabel("Time: --:--"));
        progressPanel.add(new JLabel("Accuracy: --%"));
        panel.add(progressPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the control panel with navigation and action buttons.
     * @return JPanel the configured control panel
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        showAnswerBtn.setPreferredSize(new Dimension(120, 35));
        nextBtn.setPreferredSize(new Dimension(120, 35));
        stopQuizBtn.setPreferredSize(new Dimension(120, 35));
        changeThemeBtn.setPreferredSize(new Dimension(120, 35));

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

    /**
     * Applies a consistent style to the provided button.
     * @param button JButton to style
     * @param color Background color of the button
     */
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
    }

    // === QUIZ LOGIC ===

    /**
     * Loads all available topics into the theme selector combobox.
     */
    public void loadThemes() {
        themeSelector.removeAllItems();
        themeSelector.addItem("All Topics");
        List<String> themes = delegate.getAllTopics();
        for (String theme : themes) themeSelector.addItem(theme);
    }

    /**
     * Starts a quiz for the currently selected theme.
     */
    private void startThemeQuiz() {
        currentTheme = (String) themeSelector.getSelectedItem();
        currentQuestions = getQuestionsForTheme.apply(currentTheme);
        startQuiz();
    }

    /**
     * Starts a quiz with all questions in random order.
     */
    private void startRandomQuiz() {
        currentTheme = "Random";
        currentQuestions = getAllQuestions.get();
        Collections.shuffle(currentQuestions);
        startQuiz();
    }

    /**
     * Starts the Leitner mode quiz for due questions.
     */
    private void startLeitnerQuiz() {
        String selectedTheme = (String) themeSelector.getSelectedItem();
        if ("All Topics".equals(selectedTheme)) {
            currentQuestions = leitnerSystem.getAllDueQuestions();
            currentTheme = "Leitner Mode (All Topics)";
        } else {
            currentQuestions = leitnerSystem.getDueQuestions(selectedTheme);
            currentTheme = "Leitner Mode (" + selectedTheme + ")";
        }

        if (currentQuestions.isEmpty()) {
            questionLabel.setText("🎉 No due questions!");
            JOptionPane.showMessageDialog(this,
                "<html><h3>🎉 Congratulations!</h3><p>No repetitions due.</p></html>",
                "Leitner System", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
            "<html><h3>🧠 Leitner Mode started</h3><p>" + currentQuestions.size() +
            " questions due.</p></html>",
            "Leitner System", JOptionPane.INFORMATION_MESSAGE);
        startQuiz();
    }

    
    
    /**
     * Initializes the quiz state and prepares the first question.
     */
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
        quizStartTime = System.currentTimeMillis();
        totalQuizTime = 0;

        updateScore();
        showNextQuestion();
        startQuizBtn.setEnabled(false);
        randomQuizBtn.setEnabled(false);
        themeSelector.setEnabled(false);
    }

    
    
    
    
    
    /**
     * Displays the next question in the quiz.
     */
    private void showNextQuestion() {
        currentIndex++;
        if (currentIndex >= currentQuestions.size()) {
            onQuizCompleted.run();
            return;
        }

        RepoQuizeeQuestions question = currentQuestions.get(currentIndex);
        selectedAnswer = null;
        answerShown = false;
        questionAnswered = false;
        questionStartTime = System.currentTimeMillis();

        questionLabel.setText((currentIndex + 1) + ". " + question.getFrageText());
        feedbackLabel.setText("");
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

                javax.swing.Timer timer = new javax.swing.Timer(2000, evt -> {
                    if (currentIndex < currentQuestions.size() - 1) showNextQuestion();
                    else showQuizSummary();
                });
                timer.setRepeats(false);
                timer.start();

                for (Component comp : answersPanel.getComponents()) {
                    if (comp instanceof JRadioButton) ((JRadioButton) comp).setEnabled(false);
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

    
    /**
     * Checks the selected answer for correctness and updates feedback.
     */
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

        if (explanation != null && !explanation.trim().isEmpty()) {
            feedback.append("\n\n💡 Erklärung:\n").append(explanation);
        }

        feedbackLabel.setText("<html>" + feedback.toString().replace("\n", "<br>") + "</html>");
        long answerTime = System.currentTimeMillis() - questionStartTime;

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

    
    /**
     * Reveals the correct answer for the current question.
     */
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

    
    /**
     * Returns the correct answer(s) for a given question.
     * @param question The question to evaluate
     * @return String representation of correct answer(s)
     */
    private String getCorrectAnswer(RepoQuizeeQuestions question) {
        List<String> answers = question.getAntworten();
        List<Boolean> correct = question.getKorrekt();
        List<String> correctAnswers = new ArrayList<>();
        for (int i = 0; i < answers.size() && i < correct.size(); i++) {
            if (Boolean.TRUE.equals(correct.get(i))) correctAnswers.add(answers.get(i));
        }
        if (correctAnswers.isEmpty()) return !answers.isEmpty() ? answers.get(0) : "Keine Antwort";
        else if (correctAnswers.size() == 1) return correctAnswers.get(0);
        else return String.join(", ", correctAnswers);
    }

    /**
     * Determines if the selected answer is correct.
     * @param question The question being answered
     * @param selectedAnswer The answer selected by the user
     * @return true if correct, false otherwise
     */
    private boolean isAnswerCorrect(RepoQuizeeQuestions question, String selectedAnswer) {
        List<String> answers = question.getAntworten();
        List<Boolean> correct = question.getKorrekt();
        for (int i = 0; i < answers.size() && i < correct.size(); i++) {
            if (answers.get(i).equals(selectedAnswer) && Boolean.TRUE.equals(correct.get(i))) return true;
        }
        return false;
    }

    /**
     * Updates the displayed quiz score.
     */
    private void updateScore() {
        scoreLabel.setText("Score: " + correctAnswers + "/" + answeredQuestions);
    }

    
    /**
     * Shows a summary at the end of the quiz.
     */
    private void showQuizSummary() {
        questionLabel.setText("Quiz beendet!");
        answersPanel.removeAll();
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

    
    
    /**
     * Resets the quiz UI and internal state.
     */
    private void resetQuizState() {
        startQuizBtn.setEnabled(true);
        randomQuizBtn.setEnabled(true);
        themeSelector.setEnabled(true);
        nextBtn.setEnabled(false);
        showAnswerBtn.setEnabled(false);
        stopQuizBtn.setEnabled(false);
        changeThemeBtn.setEnabled(true);
    }

    
    
    /**
     * Stops the quiz and confirms with the user.
     */
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

    /**
     * Allows the user to change the quiz theme, resetting the current quiz.
     */
    private void changeTheme() {
        if (currentQuestions.isEmpty()) {
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
     * Returns the internal statistics service.
     * @return ModularQuizStatistics instance
     */
    public ModularQuizStatistics getStatisticsService() {
        return statisticsService;
    }

    /**
     * Sets an external statistics panel for integration.
     * @param externalStatsPanel Panel for statistics integration
     */
    public void setStatisticsService(ModularStatisticsPanel externalStatsPanel) {
        this.externalStatsPanel = externalStatsPanel;
    }
}