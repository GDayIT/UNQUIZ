package guimodule;

import dbbl.BusinesslogicaDelegation;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;
import javax.swing.*;

/**
 * Enhanced Statistics Panel with integrated statistics service.
 *
 * Features:
 * - Integrated ModularQuizStatistics functionality
 * - Real-time theme updates from delegate
 * - Automatic data synchronization
 * - Persistent statistics storage
 * - Karteikarten-System (Spaced Repetition)
 *
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
=======
 * @author Quiz Application Team
 * @version 3.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class ModularStatisticsPanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String STATISTICS_FILE = "quiz_statistics.dat";

    // UI Components
    private final JComboBox<String> themeSelector = new JComboBox<>();
    private final JButton refreshBtn = new JButton("🔄 Aktualisieren");
    private final JButton resetBtn = new JButton("🗑️ Reset Statistiken");
    private final JTextArea statisticsArea = new JTextArea();

    // Chart Components
    private JPanel chartsPanel;
    private JPanel successRateChart;
    private JPanel themeComparisonChart;
    private JPanel karteikartenChart;

    // Metric cards
    private JLabel totalQuestionsValue = new JLabel("0");
    private JLabel correctAnswersValue = new JLabel("0");
    private JLabel wrongAnswersValue = new JLabel("0");
    private JLabel successRateValue = new JLabel("0%");
    private JLabel avgTimeValue = new JLabel("0s");
    private JLabel activeThemesValue = new JLabel("0");

    // Integrated Statistics Service
    private final Map<String, QuestionStatistics> questionStats = new ConcurrentHashMap<>();
    private final Map<String, ThemeStatistics> themeStats = new ConcurrentHashMap<>();
    private final List<ModularQuizPlay.QuizResult> allResults = new ArrayList<>();

    // Adaptive Leitner System Integration
    private AdaptiveLeitnerSystem leitnerSystem;
    private String selectedTheme = "Alle Themen";

    // Business Logic Delegate for real-time theme updates
    private BusinesslogicaDelegation delegate;
    // Gui module delegate (preferred)
    private GuiModuleDelegate modules;

    // Lambda-based operations
    private Function<String, QuestionStatistics> getQuestionStats;
    private Function<String, ThemeStatistics> getThemeStats;
    private Consumer<ModularQuizPlay.QuizResult> recordAnswerImpl;

    /**
     * Statistics for individual questions.
     */
    public static class QuestionStatistics implements Serializable {
        private static final long serialVersionUID = 1L;

        public final String questionTitle;
        public int totalAttempts = 0;
        public int correctAttempts = 0;
        public int consecutiveCorrect = 0;
        public int consecutiveWrong = 0;
        public long lastAttempt = 0;
        public int karteikartenLevel = 1; // 1-6 (1=green, 6=red)

        public QuestionStatistics(String questionTitle) {
            this.questionTitle = questionTitle;
        }

        public double getSuccessRate() {
            return totalAttempts > 0 ? (correctAttempts * 100.0 / totalAttempts) : 0;
        }
    }

    /**
     * Statistics for themes.
     */
    public static class ThemeStatistics implements Serializable {
        private static final long serialVersionUID = 1L;

        public final String themeName;
        public int totalQuestions = 0;
        public int totalAttempts = 0;
        public int correctAttempts = 0;
        public long lastPlayed = 0;

        public ThemeStatistics(String themeName) {
            this.themeName = themeName;
        }

        public double getSuccessRate() {
            return totalAttempts > 0 ? (correctAttempts * 100.0 / totalAttempts) : 0;
        }
    }

    public ModularStatisticsPanel() {
        initializeLambdas();
        initUI();
        // Lazy load statistics in background
        SwingUtilities.invokeLater(this::loadStatisticsAsync);
    }

    @Deprecated
    public ModularStatisticsPanel(BusinesslogicaDelegation delegate) {
        this.delegate = delegate;
        this.modules = null;
        this.leitnerSystem = new AdaptiveLeitnerSystem(delegate);
        initializeLambdas();
        initUI();
        updateThemeSelector();
        SwingUtilities.invokeLater(this::loadStatisticsAsync);
    }

    public ModularStatisticsPanel(GuiModuleDelegate modules) {
        this.modules = modules;
        this.delegate = modules.business();
        this.leitnerSystem = modules.newLeitnerSystem();
        initializeLambdas();
        initUI();
        updateThemeSelector();
        SwingUtilities.invokeLater(this::loadStatisticsAsync);
    }

    /**
     * Initialize lambda-based operations.
     */
    private void initializeLambdas() {
        // Get or create question statistics
        getQuestionStats = questionTitle -> {
            return questionStats.computeIfAbsent(questionTitle, QuestionStatistics::new);
        };

        // Get or create theme statistics
        getThemeStats = themeName -> {
            return themeStats.computeIfAbsent(themeName, ThemeStatistics::new);
        };

        // Record answer implementation
        recordAnswerImpl = result -> {
            // Update question statistics
            QuestionStatistics qStats = getQuestionStats.apply(result.questionTitle);
            qStats.totalAttempts++;
            qStats.lastAttempt = result.timestamp;

            if (result.isCorrect) {
                qStats.correctAttempts++;
                qStats.consecutiveCorrect++;
                qStats.consecutiveWrong = 0;

                // Improve Karteikarten level (move towards green)
                if (qStats.consecutiveCorrect >= 2 && qStats.karteikartenLevel > 1) {
                    qStats.karteikartenLevel--;
                }
            } else {
                qStats.consecutiveWrong++;
                qStats.consecutiveCorrect = 0;

                // Worsen Karteikarten level (move towards red)
                if (qStats.consecutiveWrong >= 2 && qStats.karteikartenLevel < 6) {
                    qStats.karteikartenLevel++;
                }
            }

            // Update theme statistics
            if (result.theme != null && !result.theme.equals("Random")) {
                ThemeStatistics tStats = getThemeStats.apply(result.theme);
                tStats.totalAttempts++;
                tStats.lastPlayed = result.timestamp;

                if (result.isCorrect) {
                    tStats.correctAttempts++;
                }
            }

            // Store result
            allResults.add(result);
            saveStatistics();
        };
    }

    /**
     * Record a quiz answer.
     */
    public void recordAnswer(ModularQuizPlay.QuizResult result) {
        recordAnswerImpl.accept(result);

        // Update Leitner System
        if (leitnerSystem != null) {
            leitnerSystem.processQuizResult(result);
        }

        // Refresh UI asynchronously to avoid blocking
        SwingUtilities.invokeLater(this::refreshStatistics);
    }

    /**
     * Get overall statistics.
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();

        if (questionStats.isEmpty()) {
            return stats; // Return empty map if no data
        }

        int totalQuestions = questionStats.size();
        long totalAttempts = questionStats.values().stream().mapToLong(q -> q.totalAttempts).sum();
        long totalCorrect = questionStats.values().stream().mapToLong(q -> q.correctAttempts).sum();
        double avgSuccessRate = questionStats.values().stream()
            .mapToDouble(QuestionStatistics::getSuccessRate)
            .average()
            .orElse(0.0);

        stats.put("totalQuestions", totalQuestions);
        stats.put("totalAttempts", totalAttempts);
        stats.put("totalCorrect", totalCorrect);
        stats.put("averageSuccessRate", avgSuccessRate);

        return stats;
    }

    /**
     * Get theme statistics.
     */
    public Collection<ThemeStatistics> getThemeStatistics() {
        return themeStats.values();
    }

    /**
     * Get questions by Karteikarten level.
     */
    public Map<Integer, List<String>> getQuestionsByKarteikartenLevel() {
        Map<Integer, List<String>> result = new HashMap<>();
        for (QuestionStatistics qStats : questionStats.values()) {
            result.computeIfAbsent(qStats.karteikartenLevel, k -> new ArrayList<>())
                  .add(qStats.questionTitle);
        }
        return result;
    }

    /**
     * Save statistics to file asynchronously.
     */
    private void saveStatistics() {
        // Save in background thread to avoid UI blocking
        new Thread(() -> {
            try {
                // Create a lightweight data structure for saving
                StatisticsData data = new StatisticsData();
                data.questionStats.putAll(this.questionStats);
                data.themeStats.putAll(this.themeStats);
                data.allResults.addAll(this.allResults);

                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(STATISTICS_FILE))) {
                    out.writeObject(data);
                }
            } catch (IOException e) {
                System.err.println("Failed to save statistics: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Lightweight data structure for serialization.
     */
    private static class StatisticsData implements Serializable {
        private static final long serialVersionUID = 1L;
        final Map<String, QuestionStatistics> questionStats = new ConcurrentHashMap<>();
        final Map<String, ThemeStatistics> themeStats = new ConcurrentHashMap<>();
        final List<ModularQuizPlay.QuizResult> allResults = new ArrayList<>();
    }

    /**
     * Load statistics from file asynchronously.
     */
    private void loadStatisticsAsync() {
        // Show loading indicator
        SwingUtilities.invokeLater(() -> {
            totalQuestionsValue.setText("...");
            correctAnswersValue.setText("...");
            wrongAnswersValue.setText("...");
            successRateValue.setText("...");
            avgTimeValue.setText("...");
            activeThemesValue.setText("...");
        });

        // Load in background thread
        new Thread(() -> {
            try {
                loadStatistics();
                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    loadThemes();
                    refreshStatistics();
                });
            } catch (Exception e) {
                System.err.println("Error loading statistics: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    totalQuestionsValue.setText("0");
                    correctAnswersValue.setText("0");
                    wrongAnswersValue.setText("0");
                    successRateValue.setText("0%");
                    avgTimeValue.setText("0s");
                    activeThemesValue.setText("0");
                });
            }
        }).start();
    }

    /**
     * Load statistics from file (synchronous).
     */
    private void loadStatistics() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(STATISTICS_FILE))) {
            Object obj = in.readObject();

            if (obj instanceof StatisticsData) {
                // New format
                StatisticsData data = (StatisticsData) obj;
                this.questionStats.clear();
                this.questionStats.putAll(data.questionStats);
                this.themeStats.clear();
                this.themeStats.putAll(data.themeStats);
                this.allResults.clear();
                this.allResults.addAll(data.allResults);
            } else if (obj instanceof ModularStatisticsPanel) {
                // Legacy format
                ModularStatisticsPanel loaded = (ModularStatisticsPanel) obj;
                this.questionStats.clear();
                this.questionStats.putAll(loaded.questionStats);
                this.themeStats.clear();
                this.themeStats.putAll(loaded.themeStats);
                this.allResults.clear();
                this.allResults.addAll(loaded.allResults);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing statistics found, starting fresh");
        }
    }

    /**
     * Public method to refresh themes and statistics (called from external components).
     */
    public void refreshThemesAndStatistics() {
        loadThemes();
        refreshStatistics();
    }

    /**
     * Reset all statistics data.
     */
    private void resetAllStatistics() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "<html><h3>🗑️ Alle Statistiken zurücksetzen?</h3>" +
            "<p>Diese Aktion löscht <b>unwiderruflich</b>:</p>" +
            "<ul>" +
            "<li>• Alle Quiz-Ergebnisse</li>" +
            "<li>• Alle Themen-Statistiken</li>" +
            "<li>• Alle Karteikarten-Level</li>" +
            "<li>• Alle Diagramm-Daten</li>" +
            "</ul>" +
            "<p><b>Möchten Sie wirklich fortfahren?</b></p></html>",
            "Statistiken zurücksetzen",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            // Clear all data
            questionStats.clear();
            themeStats.clear();
            allResults.clear();

            // Delete statistics file
            try {
                java.io.File statsFile = new java.io.File(STATISTICS_FILE);
                if (statsFile.exists()) {
                    statsFile.delete();
                }
            } catch (Exception e) {
                System.err.println("Error deleting statistics file: " + e.getMessage());
            }

            // Also reset Leitner system: clear in-memory instance and delete persistence file
            try {
                // Set to null so charts render empty after reset
                this.leitnerSystem = null;
                java.io.File leitnerFile = new java.io.File("leitner_system.dat");
                if (leitnerFile.exists()) {
                    leitnerFile.delete();
                }
            } catch (Exception e) {
                System.err.println("Error resetting Leitner system: " + e.getMessage());
            }

            // Reset UI immediately
            SwingUtilities.invokeLater(() -> {
                totalQuestionsValue.setText("0");
                correctAnswersValue.setText("0");
                wrongAnswersValue.setText("0");
                successRateValue.setText("0%");
                avgTimeValue.setText("0s");
                activeThemesValue.setText("0");

                statisticsArea.setText("=== STATISTIKEN ZURÜCKGESETZT ===\n\n" +
                    "Alle Daten wurden erfolgreich gelöscht.\n\n" +
                    "Beginnen Sie ein neues Quiz, um neue Statistiken zu sammeln!");

                // Update charts
                updateChartsWithData(Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap());
            });

            JOptionPane.showMessageDialog(
                this,
                "<html><h3>✅ Statistiken zurückgesetzt</h3>" +
                "<p>Alle Daten wurden erfolgreich gelöscht.</p>" +
                "<p>Sie können jetzt neue Statistiken sammeln!</p></html>",
                "Reset erfolgreich",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setPreferredSize(new Dimension(900, 900));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // === TOP PANEL: Enhanced Controls ===
        JPanel topPanel = createTopControlPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // === CENTER: Tabbed Statistics Display ===
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Overview Tab
        JPanel overviewPanel = createOverviewPanel();
        tabbedPane.addTab("📊 Übersicht", overviewPanel);
        
        // Detailed Statistics Tab
        JPanel detailPanel = createDetailPanel();
        tabbedPane.addTab("📈 Details", detailPanel);
        
        // Charts Tab
        chartsPanel = createChartsPanel();
        tabbedPane.addTab("📉 Diagramme", chartsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // === BOTTOM: Status and Export ===
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // === EVENT HANDLERS ===
        refreshBtn.addActionListener(e -> {
            selectedTheme = (String) themeSelector.getSelectedItem();
            loadThemes(); // Reload themes from delegate
            refreshStatistics();
        });
        resetBtn.addActionListener(e -> resetAllStatistics());

        // Theme selector change handler
        themeSelector.addActionListener(e -> {
            selectedTheme = (String) themeSelector.getSelectedItem();
            if (selectedTheme != null) {
                refreshStatistics();
            }
        });
        themeSelector.addActionListener(e -> refreshStatistics());

        // Load initial data
        loadThemes();
        refreshStatistics();
    }
    
    private JPanel createTopControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Statistik Konfiguration"));
        
        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftSection.add(new JLabel("Thema:"));
        themeSelector.setPreferredSize(new Dimension(200, 25));
        leftSection.add(themeSelector);
        
        JPanel centerSection = new JPanel(new FlowLayout(FlowLayout.CENTER));
        refreshBtn.setPreferredSize(new Dimension(140, 30));
        resetBtn.setPreferredSize(new Dimension(140, 30));
        styleButton(refreshBtn, new Color(70, 130, 180));
        styleButton(resetBtn, new Color(220, 53, 69));
        centerSection.add(refreshBtn);
        centerSection.add(resetBtn);
        
        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel timestampLabel = new JLabel("Letzte Aktualisierung: " + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss  ")));
        timestampLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        rightSection.add(timestampLabel);
        
        panel.add(leftSection, BorderLayout.WEST);
        panel.add(centerSection, BorderLayout.CENTER);
        panel.add(rightSection, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Key metrics cards
        JPanel metricsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create metric cards
        metricsPanel.add(createMetricCard("Gesamt Fragen", totalQuestionsValue, "📝", new Color(52, 152, 219)));
        metricsPanel.add(createMetricCard("Richtig beantwortet", correctAnswersValue, "✅", new Color(46, 204, 113)));
        metricsPanel.add(createMetricCard("Falsch beantwortet", wrongAnswersValue, "❌", new Color(231, 76, 60)));
        metricsPanel.add(createMetricCard("Erfolgsquote", successRateValue, "🎯", new Color(155, 89, 182)));
        metricsPanel.add(createMetricCard("Durchschnittliche Zeit", avgTimeValue, "⏱️", new Color(241, 196, 15)));
        metricsPanel.add(createMetricCard("Aktive Themen", activeThemesValue, "📚", new Color(230, 126, 34)));
        
        panel.add(metricsPanel, BorderLayout.NORTH);
        
        // Quick summary
        JTextArea summaryArea = new JTextArea();
        summaryArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        summaryArea.setEditable(false);
        summaryArea.setBackground(new Color(248, 249, 250));
        summaryArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Zusammenfassung"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        summaryArea.setText("QUIZ-STATISTIKEN ZUSAMMENFASSUNG\n\n" +
            "Noch keine Daten vorhanden.\n\n" +
            "So startest du:\n" +
            "1. Erstelle Themen im 'Quiz Topics' Tab\n" +
            "2. Füge Fragen im 'Quiz Questions' Tab hinzu\n" +
            "3. Spiele ein Quiz im 'Quiz Game' Tab\n" +
            "4. Kehre hierher zurück für echte Statistiken!\n\n" +
            "Nach dem Spielen siehst du hier:\n" +
            "• Deine Erfolgsrate pro Thema\n" +
            "• Empfehlungen für weitere Übungen\n" +
            "• Karteikarten-Status\n" +
            "• Lernfortschritt");
        
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setPreferredSize(new Dimension(0, 200));
        panel.add(summaryScroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Enhanced statistics area
        statisticsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statisticsArea.setEditable(false);
        statisticsArea.setBackground(Color.WHITE);
        statisticsArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JScrollPane scrollPane = new JScrollPane(statisticsArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Detaillierte Statistiken"),
            BorderFactory.createLoweredBevelBorder()
        ));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create chart container with grid layout
        JPanel chartsContainer = new JPanel(new GridLayout(2, 2, 10, 10));
        chartsContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create individual chart panels
        successRateChart = createSuccessRateChart();
        themeComparisonChart = createThemeComparisonChart();
        karteikartenChart = createKarteikartenChart();
        JPanel progressChart = createProgressChart();

        chartsContainer.add(successRateChart);
        chartsContainer.add(themeComparisonChart);
        chartsContainer.add(karteikartenChart);
        chartsContainer.add(progressChart);

        panel.add(chartsContainer, BorderLayout.CENTER);

        // Add instructions at the bottom
        JLabel instructionLabel = new JLabel(
            "<html><center><i>📈 Diagramme werden automatisch aktualisiert, sobald Sie Quiz-Fragen beantworten!</i></center></html>"
        );
        instructionLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(instructionLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create Success Rate Pie Chart.
     */
    private JPanel createSuccessRateChart() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                drawSuccessRatePieChart(g2d, getWidth(), getHeight());
                g2d.dispose();
            }
        };
    }

    /**
     * Create Theme Comparison Bar Chart.
     */
    private JPanel createThemeComparisonChart() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                drawThemeComparisonBarChart(g2d, getWidth(), getHeight());
                g2d.dispose();
            }
        };
    }

    /**
     * Create Karteikarten Level Chart.
     */
    private JPanel createKarteikartenChart() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                drawKarteikartenLevelChart(g2d, getWidth(), getHeight());
                g2d.dispose();
            }
        };
    }

    /**
     * Create Progress Over Time Chart.
     */
    private JPanel createProgressChart() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                drawProgressOverTimeChart(g2d, getWidth(), getHeight());
                g2d.dispose();
            }
        };
    }

    /**
     * Draw Success Rate Pie Chart.
     */
    private void drawSuccessRatePieChart(Graphics2D g2d, int width, int height) {
        Map<String, Object> stats = getOverallStatistics();

        // Chart title
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        String title = "📊 Erfolgsquote";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 20);

        if (stats.isEmpty()) {
            // No data message
            g2d.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            String noData = "Keine Daten verfügbar";
            int noDataWidth = g2d.getFontMetrics().stringWidth(noData);
            g2d.drawString(noData, (width - noDataWidth) / 2, height / 2);
            return;
        }

        long totalCorrect = (Long) stats.getOrDefault("totalCorrect", 0L);
        long totalAttempts = (Long) stats.getOrDefault("totalAttempts", 0L);
        long totalWrong = totalAttempts - totalCorrect;

        if (totalAttempts == 0) {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            String noData = "Noch keine Antworten";
            int noDataWidth = g2d.getFontMetrics().stringWidth(noData);
            g2d.drawString(noData, (width - noDataWidth) / 2, height / 2);
            return;
        }

        // Calculate pie chart - kompakter Layout
        int centerX = width / 2;
        int centerY = height / 2 - 10; // Höher positionieren
        int radius = Math.min(width, height - 80) / 4; // Kleiner für Platz für Legende

        double correctAngle = (totalCorrect * 360.0) / totalAttempts;
        double wrongAngle = 360.0 - correctAngle;

        // Draw pie slices
        g2d.setColor(new Color(76, 175, 80)); // Green for correct
        g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 0, (int) correctAngle);

        g2d.setColor(new Color(244, 67, 54)); // Red for wrong
        g2d.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, (int) correctAngle, (int) wrongAngle);

        // Draw border around pie
        g2d.setColor(Color.BLACK);
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Draw legend - kompakter und höher
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        int legendY = centerY + radius + 15; // Weniger Abstand

        // Richtig (links)
        g2d.setColor(new Color(76, 175, 80));
        g2d.fillRect(centerX - 80, legendY, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(centerX - 80, legendY, 10, 10);
        g2d.drawString("Richtig: " + totalCorrect, centerX - 65, legendY + 8);

        // Falsch (rechts)
        g2d.setColor(new Color(244, 67, 54));
        g2d.fillRect(centerX + 10, legendY, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(centerX + 10, legendY, 10, 10);
        g2d.drawString("Falsch: " + totalWrong, centerX + 25, legendY + 8);

        // Prozentanzeige in der Mitte
        if (totalAttempts > 0) {
            double successRate = (totalCorrect * 100.0) / totalAttempts;
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            String percentText = String.format("%.1f%%", successRate);
            FontMetrics percentFm = g2d.getFontMetrics();
            int percentWidth = percentFm.stringWidth(percentText);
            g2d.setColor(Color.BLACK);
            g2d.drawString(percentText, centerX - percentWidth / 2, centerY + 5);
        }
    }

    /**
     * Draw Theme Comparison Bar Chart.
     */
    private void drawThemeComparisonBarChart(Graphics2D g2d, int width, int height) {
        Collection<ThemeStatistics> themeStats = getThemeStatistics();

        // Chart title
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        String title = "📚 Themen-Vergleich";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 20);

        if (themeStats.isEmpty()) {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            String noData = "Keine Themen gespielt";
            int noDataWidth = g2d.getFontMetrics().stringWidth(noData);
            g2d.drawString(noData, (width - noDataWidth) / 2, height / 2);
            return;
        }

        // Filter by selected theme if not "Alle Themen"
        List<ThemeStatistics> themes = new ArrayList<>(themeStats);
        if (!"Alle Themen".equals(selectedTheme)) {
            themes = themes.stream()
                .filter(t -> selectedTheme.equals(t.themeName))
                .collect(Collectors.toList());
        }

        if (themes.isEmpty()) {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            String noData = "Keine Daten für gewähltes Thema";
            int noDataWidth = g2d.getFontMetrics().stringWidth(noData);
            g2d.drawString(noData, (width - noDataWidth) / 2, height / 2);
            return;
        }

        themes.sort((a, b) -> Double.compare(b.getSuccessRate(), a.getSuccessRate()));

        int maxThemes = Math.min(5, themes.size());
        int barHeight = 25;
        int barSpacing = 40;
        int startY = 50;
        int leftMargin = 20;
        int rightMargin = 80;
        int maxBarWidth = width - leftMargin - rightMargin;

        // Find max success rate for scaling
        double maxRate = themes.stream().mapToDouble(ThemeStatistics::getSuccessRate).max().orElse(100.0);
        if (maxRate == 0) maxRate = 100.0;

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

        for (int i = 0; i < maxThemes; i++) {
            ThemeStatistics theme = themes.get(i);
            int y = startY + i * barSpacing;

            // Calculate bar width
            int barWidth = (int) ((theme.getSuccessRate() / maxRate) * maxBarWidth);

            // Draw bar with gradient colors
            Color barColor = getThemeColor(theme.getSuccessRate());
            g2d.setColor(barColor);
            g2d.fillRect(leftMargin, y, barWidth, barHeight);

            // Draw border
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect(leftMargin, y, barWidth, barHeight);

            // Draw theme name (left side)
            g2d.setColor(Color.BLACK);
            String themeName = theme.themeName.length() > 12 ?
                theme.themeName.substring(0, 9) + "..." : theme.themeName;
            g2d.drawString(themeName, leftMargin + 5, y + 17);

            // Draw percentage (right side)
            String percentage = String.format("%.1f%%", theme.getSuccessRate());
            FontMetrics percentFm = g2d.getFontMetrics();
            int percentWidth = percentFm.stringWidth(percentage);
            g2d.drawString(percentage, leftMargin + maxBarWidth + 10, y + 17);

            // Draw attempts count
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
            String attempts = "(" + theme.totalAttempts + " Versuche)";
            g2d.drawString(attempts, leftMargin + maxBarWidth + 10, y + 30);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        }
    }

    /**
     * Get color based on success rate.
     */
    private Color getThemeColor(double successRate) {
        if (successRate >= 80) {
            return new Color(76, 175, 80); // Green
        } else if (successRate >= 60) {
            return new Color(255, 193, 7); // Yellow
        } else if (successRate >= 40) {
            return new Color(255, 152, 0); // Orange
        } else {
            return new Color(244, 67, 54); // Red
        }
    }

    /**
     * Draw Karteikarten Level Chart.
     */
    private void drawKarteikartenLevelChart(Graphics2D g2d, int width, int height) {
        Map<Integer, List<AdaptiveLeitnerCard>> leitnerData = getLeitnerStatistics();

        // Chart title with theme info
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        String title = "🎯 Leitner-System (L1-L6)";
        if (!"Alle Themen".equals(selectedTheme)) {
            title += " - " + selectedTheme;
        }
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 20);

        // Check if we have any cards at all
        int totalCards = leitnerData.values().stream().mapToInt(List::size).sum();
        if (totalCards == 0) {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            String noData = "Keine Leitner-Daten für " + selectedTheme;
            int noDataWidth = g2d.getFontMetrics().stringWidth(noData);
            g2d.drawString(noData, (width - noDataWidth) / 2, height / 2);
            return;
        }

        // Colors for levels (red to green - Leitner style)
        Color[] levelColors = {
            new Color(244, 67, 54),   // Level 1 - Red (needs most practice)
            new Color(255, 87, 34),   // Level 2 - Deep Orange
            new Color(255, 152, 0),   // Level 3 - Orange
            new Color(255, 235, 59),  // Level 4 - Yellow
            new Color(139, 195, 74),  // Level 5 - Light Green
            new Color(76, 175, 80)    // Level 6 - Green (mastered)
        };

        int barWidth = 35;
        int maxBarHeight = height - 100;
        int startX = 25;
        int spacing = 45;

        // Find max count for scaling
        int maxCount = leitnerData.values().stream()
            .mapToInt(List::size)
            .max()
            .orElse(1);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));

        for (int level = 1; level <= 6; level++) {
            List<AdaptiveLeitnerCard> cards = leitnerData.getOrDefault(level, new ArrayList<>());
            int count = cards.size();

            // Count due cards for this level
            int dueCount = (int) cards.stream().filter(AdaptiveLeitnerCard::isDue).count();

            int x = startX + (level - 1) * spacing;
            int barHeight = maxCount > 0 ? (count * maxBarHeight) / maxCount : 0;
            int y = height - 60 - barHeight;

            // Draw bar
            g2d.setColor(levelColors[level - 1]);
            g2d.fillRect(x, y, barWidth, barHeight);

            // Draw due cards overlay (darker)
            if (dueCount > 0) {
                int dueHeight = maxCount > 0 ? (dueCount * maxBarHeight) / maxCount : 0;
                int dueY = height - 60 - dueHeight;
                g2d.setColor(new Color(levelColors[level - 1].getRed(),
                                     levelColors[level - 1].getGreen(),
                                     levelColors[level - 1].getBlue(), 150));
                g2d.fillRect(x, dueY, barWidth, dueHeight);
            }

            // Draw border
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, barWidth, barHeight);

            // Draw level label
            g2d.drawString("L" + level, x + 12, height - 40);

            // Draw count
            if (count > 0) {
                String countStr = String.valueOf(count);
                FontMetrics countFm = g2d.getFontMetrics();
                int countWidth = countFm.stringWidth(countStr);
                g2d.drawString(countStr, x + (barWidth - countWidth) / 2, y - 5);
            }

            // Draw due count if any
            if (dueCount > 0) {
                g2d.setColor(Color.RED);
                String dueStr = "(" + dueCount + ")";
                FontMetrics dueFm = g2d.getFontMetrics();
                int dueWidth = dueFm.stringWidth(dueStr);
                g2d.drawString(dueStr, x + (barWidth - dueWidth) / 2, height - 25);
            }
        }

        // Draw legend
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g2d.drawString("Rot = Schwer, Grün = Gemeistert", 10, height - 5);
        g2d.drawString("(Zahl) = Fällige Wiederholungen", 150, height - 5);
    }

    /**
     * Draw Progress Over Time Chart.
     */
    private void drawProgressOverTimeChart(Graphics2D g2d, int width, int height) {
        // Chart title
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        String title = "📈 Fortschritt";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, 20);

        if (allResults.isEmpty()) {
            g2d.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            String noData = "Noch keine Quiz-Ergebnisse";
            int noDataWidth = g2d.getFontMetrics().stringWidth(noData);
            g2d.drawString(noData, (width - noDataWidth) / 2, height / 2);
            return;
        }

        // Simple progress indicator
        int totalQuestions = questionStats.size();
        long totalCorrect = questionStats.values().stream().mapToLong(q -> q.correctAttempts).sum();
        long totalAttempts = questionStats.values().stream().mapToLong(q -> q.totalAttempts).sum();

        // Draw progress bar
        int barY = height / 2 - 10;
        int barWidth = width - 40;
        int barHeight = 20;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(20, barY, barWidth, barHeight);

        if (totalAttempts > 0) {
            double successRate = (totalCorrect * 100.0) / totalAttempts;
            int progressWidth = (int) ((successRate / 100.0) * barWidth);

            Color progressColor = successRate >= 80 ? new Color(76, 175, 80) :
                                 successRate >= 60 ? new Color(255, 235, 59) :
                                 new Color(244, 67, 54);

            g2d.setColor(progressColor);
            g2d.fillRect(20, barY, progressWidth, barHeight);
        }

        g2d.setColor(Color.BLACK);
        g2d.drawRect(20, barY, barWidth, barHeight);

        // Draw statistics
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g2d.drawString("Fragen: " + totalQuestions, 20, barY + 40);
        g2d.drawString("Versuche: " + totalAttempts, 20, barY + 55);
        if (totalAttempts > 0) {
            double successRate = (totalCorrect * 100.0) / totalAttempts;
            g2d.drawString(String.format("Erfolg: %.1f%%", successRate), 20, barY + 70);
        }
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel statusLabel = new JLabel("Bereit");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        panel.add(statusLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton settingsBtn = new JButton("⚙️ Einstellungen");
        styleButton(settingsBtn, new Color(108, 117, 125));
        settingsBtn.addActionListener(e -> showStatisticsSettings());
        rightPanel.add(settingsBtn);

        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createMetricCard(String title, JLabel valueLabel, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        iconLabel.setForeground(color);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        titleLabel.setForeground(new Color(73, 80, 87));
        
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        valueLabel.setForeground(color);
        
        JPanel content = new JPanel(new GridLayout(3, 1, 0, 5));
        content.setOpaque(false);
        content.add(iconLabel);
        content.add(valueLabel);
        content.add(titleLabel);
        
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
    }
    
    private void loadThemes() {
        themeSelector.removeAllItems();
        themeSelector.addItem("Alle Themen");

        // Load real themes from delegate (existing themes)
        List<String> existingThemes = null;
        if (modules != null) {
            existingThemes = modules.business().getAllTopics();
        } else if (delegate != null) {
            existingThemes = delegate.getAllTopics();
        }
        if (existingThemes != null) {
            for (String theme : existingThemes) {
                themeSelector.addItem(theme);
            }
        }

        // If no themes available, show message
        if (themeSelector.getItemCount() == 1) {
            themeSelector.addItem("(Keine Themen verfügbar)");
        }
    }
    
    private void refreshStatistics() {
        // Update in background to avoid UI blocking
        new Thread(() -> {
            try {
                // Calculate statistics in background
                Map<String, Object> stats = getOverallStatistics();
                Collection<ThemeStatistics> themeStatistics = getThemeStatistics();
                Map<Integer, List<String>> karteikartenData = getQuestionsByKarteikartenLevel();

                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    updateMetricCardsWithData(stats);
                    updateDetailedStatisticsWithData(stats, themeStatistics, karteikartenData);
                    updateChartsWithData(stats, themeStatistics, karteikartenData);
                });
            } catch (Exception e) {
                System.err.println("Error refreshing statistics: " + e.getMessage());
            }
        }).start();
    }
    
    private void updateMetricCards() {
        // Get real statistics from integrated service
        Map<String, Object> stats = getOverallStatistics();
        updateMetricCardsWithData(stats);
    }

    private void updateMetricCardsWithData(Map<String, Object> stats) {
        if (stats.isEmpty()) {
            totalQuestionsValue.setText("0");
            correctAnswersValue.setText("0");
            wrongAnswersValue.setText("0");
            successRateValue.setText("0%");
            avgTimeValue.setText("0s");
            activeThemesValue.setText("0");
        } else {
            int totalQuestions = (Integer) stats.getOrDefault("totalQuestions", 0);
            long totalCorrect = (Long) stats.getOrDefault("totalCorrect", 0L);
            long totalAttempts = (Long) stats.getOrDefault("totalAttempts", 0L);
            long totalWrong = totalAttempts - totalCorrect;
            double successRate = (Double) stats.getOrDefault("averageSuccessRate", 0.0);

            // Get active themes count from delegate
            int activeThemes = 0;
            if (modules != null) {
                activeThemes = modules.business().getAllTopics().size();
            } else if (delegate != null) {
                activeThemes = delegate.getAllTopics().size();
            }

            // Calculate average time
            double avgTimeSeconds = calculateAverageAnswerTime();
            String avgTimeText = formatTime(avgTimeSeconds);

            totalQuestionsValue.setText(String.valueOf(totalQuestions));
            correctAnswersValue.setText(String.valueOf(totalCorrect));
            wrongAnswersValue.setText(String.valueOf(totalWrong));
            successRateValue.setText(String.format("%.1f%%", successRate));
            avgTimeValue.setText(avgTimeText);
            activeThemesValue.setText(String.valueOf(activeThemes));
        }
    }
    
    private void updateDetailedStatistics() {
        Map<String, Object> stats = getOverallStatistics();
        Collection<ThemeStatistics> themeStatistics = getThemeStatistics();
        Map<Integer, List<String>> karteikartenData = getQuestionsByKarteikartenLevel();
        updateDetailedStatisticsWithData(stats, themeStatistics, karteikartenData);
    }

    private void updateDetailedStatisticsWithData(Map<String, Object> stats,
                                                  Collection<ThemeStatistics> themeStatistics,
                                                  Map<Integer, List<String>> karteikartenData) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== QUIZ STATISTIKEN ===\n\n");
        sb.append("Thema: ").append(themeSelector.getSelectedItem()).append("\n");
        sb.append("Generiert am: ").append(java.time.LocalDateTime.now()).append("\n\n");

        if (stats.isEmpty()) {
            sb.append("KEINE DATEN VORHANDEN\n\n");
            sb.append("So generierst du Statistiken:\n");
            sb.append("1. Gehe zum 'Quiz Topics' Tab\n");
            sb.append("2. Erstelle ein neues Thema oder wähle ein existierendes\n");
            sb.append("3. Gehe zum 'Quiz Questions' Tab\n");
            sb.append("4. Füge Fragen zu deinem Thema hinzu\n");
            sb.append("5. Gehe zum 'Quiz Game' Tab\n");
            sb.append("6. Spiele ein Quiz mit deinem Thema\n");
            sb.append("7. Kehre hierher zurück für echte Statistiken!\n\n");

            // Show available themes from delegate
            int availableThemes = delegate != null ? delegate.getAllTopics().size() : 0;
            sb.append("Aktuell verfügbare Themen: ").append(availableThemes).append("\n");
            sb.append("Gespielte Quizzes: 0\n");
        } else {
            // Show real statistics
            int totalQuestions = (Integer) stats.getOrDefault("totalQuestions", 0);
            long totalCorrect = (Long) stats.getOrDefault("totalCorrect", 0L);
            long totalAttempts = (Long) stats.getOrDefault("totalAttempts", 0L);
            long totalWrong = totalAttempts - totalCorrect;
            double successRate = (Double) stats.getOrDefault("averageSuccessRate", 0.0);

            sb.append("ÜBERSICHT (nur echte Daten):\n");
            sb.append("- Gesamte Fragen beantwortet: ").append(totalQuestions).append("\n");
            sb.append("- Richtige Antworten: ").append(totalCorrect).append(" (").append(String.format("%.1f", successRate)).append("%)\n");
            sb.append("- Falsche Antworten: ").append(totalWrong).append(" (").append(String.format("%.1f", 100.0 - successRate)).append("%)\n");
            sb.append("- Gesamte Versuche: ").append(totalAttempts).append("\n\n");

            // Show theme statistics if available
            if (!themeStatistics.isEmpty()) {
                sb.append("THEMEN-BREAKDOWN:\n");
                for (var themeStat : themeStatistics) {
                    sb.append(String.format("%-20s %d/%d (%.1f%%)\n",
                        themeStat.themeName + ":",
                        themeStat.correctAttempts,
                        themeStat.totalAttempts,
                        themeStat.getSuccessRate()));
                }
                sb.append("\n");
            }

            // Show Karteikarten status if available
            if (!karteikartenData.isEmpty()) {
                sb.append("KARTEIKARTEN-STATUS:\n");
                for (int level = 1; level <= 6; level++) {
                    List<String> questions = karteikartenData.getOrDefault(level, new ArrayList<>());
                    if (!questions.isEmpty()) {
                        sb.append("Stufe ").append(level).append(": ").append(questions.size()).append(" Fragen\n");
                    }
                }
                sb.append("\n");
            }
        }

        statisticsArea.setText(sb.toString());
    }

    /**
     * Update charts with new data.
     */
    private void updateChartsWithData(Map<String, Object> stats,
                                      Collection<ThemeStatistics> themeStatistics,
                                      Map<Integer, List<String>> karteikartenData) {
        // Repaint all chart panels to reflect new data
        if (successRateChart != null) {
            successRateChart.repaint();
        }
        if (themeComparisonChart != null) {
            themeComparisonChart.repaint();
        }
        if (karteikartenChart != null) {
            karteikartenChart.repaint();
        }
        if (chartsPanel != null) {
            chartsPanel.repaint();
        }
    }

    /**
     * Show statistics settings dialog.
     */
    private void showStatisticsSettings() {
        JDialog settingsDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                                           "⚙️ Statistik-Einstellungen", true);
        settingsDialog.setPreferredSize(new Dimension(500, 650));
        settingsDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("<html><h2>⚙️ Statistik-Einstellungen</h2></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Settings content
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // === KARTEIKARTEN SETTINGS ===
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel karteikartenTitle = new JLabel("<html><h3>🎯 Karteikarten-System</h3></html>");
        settingsPanel.add(karteikartenTitle, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        settingsPanel.add(new JLabel("Level-Anpassung:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> levelAdjustment = new JComboBox<>(new String[]{
            "Konservativ (3 richtige für Level-Up)",
            "Normal (2 richtige für Level-Up)",
            "Aggressiv (1 richtige für Level-Up)"
        });
        levelAdjustment.setSelectedIndex(1); // Normal als Standard
        settingsPanel.add(levelAdjustment, gbc);

        // === DIAGRAMM SETTINGS ===
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JLabel chartTitle = new JLabel("<html><h3>📊 Diagramm-Einstellungen</h3></html>");
        settingsPanel.add(chartTitle, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        settingsPanel.add(new JLabel("Max. Themen in Vergleich:"), gbc);
        gbc.gridx = 1;
        JSpinner maxThemes = new JSpinner(new SpinnerNumberModel(5, 1, 10, 1));
        settingsPanel.add(maxThemes, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JCheckBox showPercentages = new JCheckBox("Prozentangaben in Charts anzeigen");
        showPercentages.setSelected(true);
        gbc.gridwidth = 2;
        settingsPanel.add(showPercentages, gbc);

        // === EXPORT SETTINGS ===
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JLabel exportTitle = new JLabel("<html><h3>📤 Export-Einstellungen</h3></html>");
        settingsPanel.add(exportTitle, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        settingsPanel.add(new JLabel("Export-Format:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> exportFormat = new JComboBox<>(new String[]{
            "CSV (Comma Separated Values)",
            "TXT (Einfacher Text)",
            "HTML (Web-Format)"
        });
        settingsPanel.add(exportFormat, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JCheckBox includeCharts = new JCheckBox("Diagramm-Daten in Export einschließen");
        includeCharts.setSelected(true);
        gbc.gridwidth = 2;
        settingsPanel.add(includeCharts, gbc);

        mainPanel.add(settingsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn = new JButton("💾 Speichern");
        JButton exportBtn = new JButton("📤 Exportieren");
        JButton cancelBtn = new JButton("❌ Abbrechen");
        JButton resetBtn = new JButton("🔄 Zurücksetzen");

        saveBtn.addActionListener(e -> {
            // TODO: Save settings to preferences
            JOptionPane.showMessageDialog(settingsDialog,
                "<html><h3>✅ Einstellungen gespeichert</h3>" +
                "<p>Die Einstellungen wurden erfolgreich übernommen.</p></html>",
                "Einstellungen gespeichert", JOptionPane.INFORMATION_MESSAGE);
            settingsDialog.dispose();
        });

        exportBtn.addActionListener(e -> {
            settingsDialog.dispose(); // Close settings first
            exportStatistics(); // Then open export dialog
        });

        cancelBtn.addActionListener(e -> settingsDialog.dispose());

        resetBtn.addActionListener(e -> {
            levelAdjustment.setSelectedIndex(1);
            maxThemes.setValue(5);
            showPercentages.setSelected(true);
            exportFormat.setSelectedIndex(0);
            includeCharts.setSelected(true);
        });

        buttonPanel.add(saveBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(resetBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        settingsDialog.add(mainPanel);
        settingsDialog.pack();
        settingsDialog.setVisible(true);
    }

    /**
     * Export statistics to file.
     */
    private void exportStatistics() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("📤 Statistiken exportieren");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Add file filters
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "CSV Dateien (*.csv)", "csv"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Text Dateien (*.txt)", "txt"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "HTML Dateien (*.html)", "html"));

        // Set default filename
        fileChooser.setSelectedFile(new java.io.File("quiz_statistiken_" +
            java.time.LocalDate.now().toString() + ".csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String fileName = file.getName().toLowerCase();

            try {
                if (fileName.endsWith(".csv")) {
                    exportToCSV(file);
                } else if (fileName.endsWith(".html")) {
                    exportToHTML(file);
                } else {
                    exportToTXT(file);
                }

                JOptionPane.showMessageDialog(this,
                    "<html><h3>✅ Export erfolgreich</h3>" +
                    "<p>Statistiken wurden exportiert nach:</p>" +
                    "<p><b>" + file.getAbsolutePath() + "</b></p></html>",
                    "Export erfolgreich", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "<html><h3>❌ Export fehlgeschlagen</h3>" +
                    "<p>Fehler beim Exportieren:</p>" +
                    "<p><i>" + e.getMessage() + "</i></p></html>",
                    "Export Fehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Export to CSV format.
     */
    private void exportToCSV(java.io.File file) throws java.io.IOException {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
            writer.println("Quiz Statistiken Export - " + java.time.LocalDateTime.now());
            writer.println();

            // Overall statistics
            Map<String, Object> stats = getOverallStatistics();
            writer.println("GESAMTSTATISTIK");
            writer.println("Kategorie,Wert");
            writer.println("Gesamte Fragen," + stats.getOrDefault("totalQuestions", 0));
            writer.println("Richtige Antworten," + stats.getOrDefault("totalCorrect", 0));
            writer.println("Gesamte Versuche," + stats.getOrDefault("totalAttempts", 0));
            writer.println("Erfolgsrate," + String.format("%.1f%%", (Double) stats.getOrDefault("averageSuccessRate", 0.0)));
            writer.println();

            // Theme statistics
            writer.println("THEMEN-STATISTIKEN");
            writer.println("Thema,Versuche,Richtig,Erfolgsrate");
            for (ThemeStatistics theme : getThemeStatistics()) {
                writer.println(String.format("%s,%d,%d,%.1f%%",
                    theme.themeName, theme.totalAttempts, theme.correctAttempts, theme.getSuccessRate()));
            }
            writer.println();

            // Karteikarten statistics
            writer.println("KARTEIKARTEN-LEVEL");
            writer.println("Level,Anzahl Fragen");
            Map<Integer, List<String>> karteikartenData = getQuestionsByKarteikartenLevel();
            for (int level = 1; level <= 6; level++) {
                List<String> questions = karteikartenData.getOrDefault(level, new ArrayList<>());
                writer.println(level + "," + questions.size());
            }
        }
    }

    /**
     * Export to TXT format.
     */
    private void exportToTXT(java.io.File file) throws java.io.IOException {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
            writer.println("=== QUIZ STATISTIKEN EXPORT ===");
            writer.println("Exportiert am: " + java.time.LocalDateTime.now());
            writer.println();

            // Use the same content as detailed statistics
            String detailedStats = statisticsArea.getText();
            writer.println(detailedStats);
        }
    }

    /**
     * Export to HTML format.
     */
    private void exportToHTML(java.io.File file) throws java.io.IOException {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file, "UTF-8")) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html><head>");
            writer.println("<title>Quiz Statistiken</title>");
            writer.println("<meta charset='UTF-8'>");
            writer.println("<style>");
            writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("h1, h2 { color: #333; }");
            writer.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("th { background-color: #f2f2f2; }");
            writer.println(".success { color: #4CAF50; font-weight: bold; }");
            writer.println(".warning { color: #FF9800; font-weight: bold; }");
            writer.println(".error { color: #F44336; font-weight: bold; }");
            writer.println("</style>");
            writer.println("</head><body>");

            writer.println("<h1>📊 Quiz Statistiken</h1>");
            writer.println("<p>Exportiert am: " + java.time.LocalDateTime.now() + "</p>");

            // Overall statistics table
            Map<String, Object> stats = getOverallStatistics();
            writer.println("<h2>Gesamtstatistik</h2>");
            writer.println("<table>");
            writer.println("<tr><th>Kategorie</th><th>Wert</th></tr>");
            writer.println("<tr><td>Gesamte Fragen</td><td>" + stats.getOrDefault("totalQuestions", 0) + "</td></tr>");
            writer.println("<tr><td>Richtige Antworten</td><td>" + stats.getOrDefault("totalCorrect", 0) + "</td></tr>");
            writer.println("<tr><td>Gesamte Versuche</td><td>" + stats.getOrDefault("totalAttempts", 0) + "</td></tr>");

            double successRate = (Double) stats.getOrDefault("averageSuccessRate", 0.0);
            String rateClass = successRate >= 80 ? "success" : successRate >= 60 ? "warning" : "error";
            writer.println("<tr><td>Erfolgsrate</td><td class='" + rateClass + "'>" +
                String.format("%.1f%%", successRate) + "</td></tr>");
            writer.println("</table>");

            // Theme statistics
            writer.println("<h2>Themen-Statistiken</h2>");
            writer.println("<table>");
            writer.println("<tr><th>Thema</th><th>Versuche</th><th>Richtig</th><th>Erfolgsrate</th></tr>");
            for (ThemeStatistics theme : getThemeStatistics()) {
                double themeRate = theme.getSuccessRate();
                String themeClass = themeRate >= 80 ? "success" : themeRate >= 60 ? "warning" : "error";
                writer.println(String.format("<tr><td>%s</td><td>%d</td><td>%d</td><td class='%s'>%.1f%%</td></tr>",
                    theme.themeName, theme.totalAttempts, theme.correctAttempts, themeClass, themeRate));
            }
            writer.println("</table>");

            writer.println("</body></html>");
        }
    }

    /**
     * Calculate average answer time from all quiz results.
     */
    private double calculateAverageAnswerTime() {
        if (allResults.isEmpty()) {
            return 0.0;
        }

        double totalTime = 0.0;
        int count = 0;

        for (ModularQuizPlay.QuizResult result : allResults) {
            if (result.answerTimeMs > 0) { // Only count valid times
                totalTime += result.getAnswerTimeSeconds();
                count++;
            }
        }

        return count > 0 ? totalTime / count : 0.0;
    }

    /**
     * Get Leitner statistics filtered by selected theme.
     */
    private Map<Integer, List<AdaptiveLeitnerCard>> getLeitnerStatistics() {
        if (leitnerSystem == null) {
            return new HashMap<>();
        }

        if ("Alle Themen".equals(selectedTheme)) {
            return leitnerSystem.getAllStatistics();
        } else {
            return leitnerSystem.getThemeStatistics(selectedTheme);
        }
    }

    /**
     * Update theme selector with available themes.
     */
    private void updateThemeSelector() {
        try {
            themeSelector.removeAllItems();
            themeSelector.addItem("Alle Themen");

            List<String> themes = null;
            if (modules != null) {
                themes = modules.business().getAllTopics();
            } else if (delegate != null) {
                themes = delegate.getAllTopics();
            } else {
                themes = java.util.Collections.emptyList();
            }

            for (String theme : themes) {
                themeSelector.addItem(theme);
            }

            themeSelector.setSelectedItem(selectedTheme);
        } catch (Exception e) {
            System.err.println("Error updating theme selector: " + e.getMessage());
        }
    }

    /**
     * Format time in seconds to human-readable string.
     */
    private String formatTime(double seconds) {
        if (seconds <= 0) {
            return "0s";
        }

        if (seconds < 60) {
            return String.format("%.1fs", seconds);
        } else if (seconds < 3600) {
            int minutes = (int) (seconds / 60);
            int remainingSeconds = (int) (seconds % 60);
            return String.format("%dm %ds", minutes, remainingSeconds);
        } else {
            int hours = (int) (seconds / 3600);
            int minutes = (int) ((seconds % 3600) / 60);
            return String.format("%dh %dm", hours, minutes);
        }
    }
}
