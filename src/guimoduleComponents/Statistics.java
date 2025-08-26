package guimoduleComponents;

import dbbl.BusinesslogicaDelegation;
import guimodule.ModularQuizStatistics;
import guimodule.ModularQuizStatistics.QuestionStatistics;
import guimodule.ModularQuizStatistics.ThemeStatistics;
import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * Statistics Panel with Karteikarten-System (Spaced Repetition).
 * 
 * Features:
 * - Overall quiz statistics
 * - Theme-based performance analysis
 * - Karteikarten system with color-coded levels (1=green, 6=red)
 * - Questions needing practice identification
 * - Lambda-based data processing
 * - Modular and delegated architecture
 * 
 * @author Quiz Application Team
 * @version 2.0
 */
public class Statistics extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;

    private final BusinesslogicaDelegation delegate;
    private ModularQuizStatistics statisticsService;
    
    // UI Components
    private final JLabel overallStatsLabel = new JLabel();
    private final JPanel karteikartenPanel = new JPanel();
    private final JPanel themeStatsPanel = new JPanel();
    private final JScrollPane karteikartenScrollPane;
    private final JScrollPane themeScrollPane;
    private final JButton refreshBtn = new JButton("Aktualisieren");
    private final JButton resetStatsBtn = new JButton("Statistiken zurücksetzen");
    private final JButton settingsBtn = new JButton("⚙️ Einstellungen");
    
    // Lambda-based operations
    private Supplier<Map<String, Object>> getOverallStats;
    private Supplier<Collection<ThemeStatistics>> getThemeStats;
    private Supplier<Collection<QuestionStatistics>> getQuestionStats;
    private Function<Integer, List<String>> getQuestionsByLevel;
    private Supplier<List<String>> getQuestionsNeedingPractice;
    private Runnable refreshDisplay;

    public Statistics(BusinesslogicaDelegation delegate) {
        this.delegate = delegate;
        
        // Initialize scroll panes
        karteikartenScrollPane = new JScrollPane(karteikartenPanel);
        themeScrollPane = new JScrollPane(themeStatsPanel);
        
        initializeLambdas();
        initUI();
    }
    
    /**
     * Set the statistics service (called from main application).
     */
    public void setStatisticsService(ModularQuizStatistics statisticsService) {
        this.statisticsService = statisticsService;
        initializeLambdas();
        refreshDisplay.run();
    }
    
    /**
     * Initialize lambda-based operations with strict validation.
     */
    private void initializeLambdas() {
        if (statisticsService == null) {
            // Default empty implementations
            getOverallStats = HashMap::new;
            getThemeStats = ArrayList::new;
            getQuestionStats = ArrayList::new;
            getQuestionsByLevel = level -> new ArrayList<>();
            getQuestionsNeedingPractice = ArrayList::new;
            refreshDisplay = this::displayNoDataMessage;
            return;
        }

        // Get overall statistics - ONLY for existing themes/questions
        getOverallStats = () -> {
            Map<String, Object> stats = statisticsService.getOverallStatistics();
            return filterStatisticsForExistingContent(stats);
        };

        // Get theme statistics - ONLY for existing themes
        getThemeStats = () -> {
            Collection<ThemeStatistics> allStats = statisticsService.getThemeStatistics();
            List<String> existingThemes = delegate.getAllTopics();
            return allStats.stream()
                .filter(stat -> existingThemes.contains(stat.themeName))
                .filter(stat -> stat.totalAttempts > 0) // Only themes that were actually played
                .collect(java.util.stream.Collectors.toList());
        };

        // Get question statistics - ONLY for existing questions
        getQuestionStats = () -> {
            Collection<QuestionStatistics> allStats = statisticsService.getQuestionStatistics();
            Set<String> existingQuestions = getAllExistingQuestions();
            return allStats.stream()
                .filter(stat -> existingQuestions.contains(stat.questionTitle))
                .filter(stat -> stat.totalAttempts > 0) // Only questions that were actually answered
                .collect(java.util.stream.Collectors.toList());
        };

        // Get questions by Karteikarten level - ONLY existing questions
        getQuestionsByLevel = level -> {
            Map<Integer, List<String>> questionsByLevel = statisticsService.getQuestionsByKarteikartenLevel();
            List<String> questionsAtLevel = questionsByLevel.getOrDefault(level, new ArrayList<>());
            Set<String> existingQuestions = getAllExistingQuestions();
            return questionsAtLevel.stream()
                .filter(existingQuestions::contains)
                .collect(java.util.stream.Collectors.toList());
        };

        // Get questions needing practice - ONLY existing questions
        getQuestionsNeedingPractice = () -> {
            List<String> practiceQuestions = statisticsService.getQuestionsNeedingPractice();
            Set<String> existingQuestions = getAllExistingQuestions();
            return practiceQuestions.stream()
                .filter(existingQuestions::contains)
                .collect(java.util.stream.Collectors.toList());
        };

        // Refresh display
        refreshDisplay = () -> {
            updateOverallStatistics();
            updateKarteikartenDisplay();
            updateThemeStatistics();
        };
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(900, 700)); // Vergrößert von 800x600 auf 900x700

        // === TOP PANEL: Controls ===
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(refreshBtn);
        topPanel.add(resetStatsBtn);
        topPanel.add(settingsBtn);
        add(topPanel, BorderLayout.NORTH);

        // === CENTER: Split between Karteikarten and Theme Stats ===
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.55); // Ausgewogeneres Verhältnis für bessere Sichtbarkeit
        splitPane.setDividerSize(8); // Etwas breiterer Divider für bessere Bedienbarkeit

        // Left: Overall Stats and Karteikarten System
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(new TitledBorder("Übersicht & Karteikarten-System"));

        // Overall Statistics
        overallStatsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        overallStatsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        overallStatsLabel.setVerticalAlignment(SwingConstants.TOP);
        leftPanel.add(overallStatsLabel, BorderLayout.NORTH);

        // Karteikarten System
        karteikartenPanel.setLayout(new BoxLayout(karteikartenPanel, BoxLayout.Y_AXIS));
        karteikartenScrollPane.setPreferredSize(new Dimension(450, 500)); // Vergrößert von 400x400 auf 450x500
        karteikartenScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        leftPanel.add(karteikartenScrollPane, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);

        // Right: Theme Statistics
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new TitledBorder("Themen-Statistiken"));

        themeStatsPanel.setLayout(new BoxLayout(themeStatsPanel, BoxLayout.Y_AXIS));
        themeScrollPane.setPreferredSize(new Dimension(350, 500)); // Vergrößert von 300x400 auf 350x500
        themeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        rightPanel.add(themeScrollPane, BorderLayout.CENTER);

        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);

        // === EVENT HANDLERS ===
        refreshBtn.addActionListener(e -> refreshDisplay.run());
        resetStatsBtn.addActionListener(e -> resetStatistics());
        settingsBtn.addActionListener(e -> showSettingsDialog());

        // Initial display
        refreshDisplay.run();
    }

    /**
     * Get all existing questions from all existing themes.
     */
    private Set<String> getAllExistingQuestions() {
        Set<String> existingQuestions = new HashSet<>();
        List<String> existingThemes = delegate.getAllTopics();

        for (String theme : existingThemes) {
            try {
                List<String> questionTitles = delegate.getQuestionTitles(theme);
                if (questionTitles != null) {
                    existingQuestions.addAll(questionTitles);
                }
            } catch (Exception e) {
                // Skip themes that cause errors
                System.err.println("Error getting questions for theme '" + theme + "': " + e.getMessage());
            }
        }

        return existingQuestions;
    }

    /**
     * Filter overall statistics to only include data for existing content.
     */
    private Map<String, Object> filterStatisticsForExistingContent(Map<String, Object> originalStats) {
        if (originalStats.isEmpty()) {
            return originalStats;
        }

        // Get existing content
        Set<String> existingQuestions = getAllExistingQuestions();
        List<String> existingThemes = delegate.getAllTopics();

        if (existingQuestions.isEmpty()) {
            // No existing questions, return empty stats
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalQuestions", 0);
            emptyStats.put("averageSuccessRate", 0.0);
            emptyStats.put("totalAttempts", 0L);
            emptyStats.put("totalCorrect", 0L);
            emptyStats.put("questionsNeedingPractice", 0);
            return emptyStats;
        }

        // Calculate filtered statistics
        Collection<QuestionStatistics> validQuestionStats = getQuestionStats.get();

        int totalQuestions = validQuestionStats.size();
        double avgSuccessRate = validQuestionStats.stream()
            .mapToDouble(QuestionStatistics::getSuccessRate)
            .average()
            .orElse(0.0);
        long totalAttempts = validQuestionStats.stream()
            .mapToLong(q -> q.totalAttempts)
            .sum();
        long totalCorrect = validQuestionStats.stream()
            .mapToLong(q -> q.correctAttempts)
            .sum();

        List<String> practiceQuestions = getQuestionsNeedingPractice.get();
        int questionsNeedingPractice = practiceQuestions.size();

        Map<String, Object> filteredStats = new HashMap<>();
        filteredStats.put("totalQuestions", totalQuestions);
        filteredStats.put("averageSuccessRate", avgSuccessRate);
        filteredStats.put("totalAttempts", totalAttempts);
        filteredStats.put("totalCorrect", totalCorrect);
        filteredStats.put("questionsNeedingPractice", questionsNeedingPractice);
        filteredStats.put("activeThemes", existingThemes.size());

        return filteredStats;
    }


    
    private void updateOverallStatistics() {
        Map<String, Object> stats = getOverallStats.get();

        if (stats.isEmpty() || (Integer) stats.getOrDefault("totalQuestions", 0) == 0) {
            overallStatsLabel.setText(
                "<html>" +
                "<div style='font-family: Arial; padding: 20px; text-align: center;'>" +
                "<h3 style='color: #2E86AB;'>📊 Gesamtübersicht</h3>" +
                "<p style='color: #666; margin: 20px 0;'>Noch keine Statistiken für existierende Themen vorhanden.</p>" +
                "<p style='color: #888; font-size: 12px;'>" +
                "1. Erstelle Themen im 'Quiz Topics' Tab<br>" +
                "2. Füge Fragen im 'Quiz Questions' Tab hinzu<br>" +
                "3. Spiele ein Quiz im 'Quiz Game' Tab<br>" +
                "4. Kehre hierher zurück für Statistiken!" +
                "</p>" +
                "</div>" +
                "</html>"
            );
            return;
        }

        // Extract filtered statistics
        int totalQuestions = (Integer) stats.get("totalQuestions");
        double avgSuccessRate = (Double) stats.get("averageSuccessRate");
        long totalAttempts = (Long) stats.get("totalAttempts");
        long totalCorrect = (Long) stats.get("totalCorrect");
        int questionsNeedingPractice = (Integer) stats.get("questionsNeedingPractice");
        int activeThemes = (Integer) stats.get("activeThemes");

        // Calculate additional statistics
        long totalWrong = totalAttempts - totalCorrect;

        // Calculate average time (placeholder - would need actual timing data)
        String avgTime = totalAttempts > 0 ? String.format("%.0fs", (totalAttempts * 30.0)) : "0s";

        String statsText = String.format(
            "<html>" +
            "<div style='font-family: Arial; padding: 10px;'>" +
            "<h3 style='color: #2E86AB; margin-bottom: 15px;'>📊 Gesamtübersicht (nur existierende Inhalte)</h3>" +
            "<table style='width: 100%%; border-spacing: 8px;'>" +
            "<tr><td><b>📚 Fragen insgesamt:</b></td><td style='text-align: right;'>%d</td></tr>" +
            "<tr><td><b>✅ Richtig beantwortet:</b></td><td style='text-align: right; color: green;'>%d</td></tr>" +
            "<tr><td><b>❌ Falsch beantwortet:</b></td><td style='text-align: right; color: red;'>%d</td></tr>" +
            "<tr><td><b>🎯 Erfolgsquote:</b></td><td style='text-align: right; color: %s;'><b>%.1f%%</b></td></tr>" +
            "<tr><td><b>🔄 Gesamte Versuche:</b></td><td style='text-align: right;'>%d</td></tr>" +
            "<tr><td><b>⏱️ Durchschnittliche Zeit:</b></td><td style='text-align: right;'>%s</td></tr>" +
            "<tr><td><b>📖 Verfügbare Themen:</b></td><td style='text-align: right;'>%d</td></tr>" +
            "<tr><td><b>🎯 Fragen zum Üben:</b></td><td style='text-align: right; color: orange;'>%d</td></tr>" +
            "</table>" +
            "<p style='font-size: 11px; color: #666; margin-top: 15px; font-style: italic;'>" +
            "* Nur Daten für Themen/Fragen, die über 'Quiz Topics' und 'Quiz Questions' erstellt wurden" +
            "</p>" +
            "</div>" +
            "</html>",
            totalQuestions, totalCorrect, totalWrong,
            (avgSuccessRate >= 70 ? "green" : avgSuccessRate >= 50 ? "orange" : "red"),
            avgSuccessRate, totalAttempts, avgTime, activeThemes, questionsNeedingPractice
        );

        overallStatsLabel.setText(statsText);
    }
    
    private void updateKarteikartenDisplay() {
        karteikartenPanel.removeAll();

        // Check if we have any question data (already filtered by initializeLambdas)
        boolean hasAnyData = false;
        for (int level = 1; level <= 6; level++) {
            List<String> questionsAtLevel = getQuestionsByLevel.apply(level);
            if (!questionsAtLevel.isEmpty()) {
                hasAnyData = true;
                break;
            }
        }

        List<String> practiceQuestions = getQuestionsNeedingPractice.get();
        if (!practiceQuestions.isEmpty()) {
            hasAnyData = true;
        }

        if (!hasAnyData) {
            JLabel noDataLabel = new JLabel(
                "<html>" +
                "<div style='font-family: Arial; padding: 20px; text-align: center;'>" +
                "<h3 style='color: #2E86AB;'>🎯 Karteikarten-System</h3>" +
                "<p style='color: #666; margin: 20px 0;'>Noch keine Fragen für existierende Themen beantwortet.</p>" +
                "<p style='color: #888; font-size: 12px;'>" +
                "Das Karteikarten-System kategorisiert Fragen nach Schwierigkeit:<br>" +
                "🟢 Level 1-2: Gut beherrscht<br>" +
                "🟡 Level 3: Solide Kenntnisse<br>" +
                "🔴 Level 4-6: Üben erforderlich<br><br>" +
                "Spiele ein Quiz, um das System zu aktivieren!" +
                "</p>" +
                "</div>" +
                "</html>"
            );
            noDataLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            karteikartenPanel.add(noDataLabel);
            karteikartenPanel.revalidate();
            karteikartenPanel.repaint();
            return;
        }

        // Add explanation only if we have data
        JLabel explanationLabel = new JLabel(
            "<html>" +
            "<div style='font-family: Arial; padding: 10px; background-color: #f5f5f5; border-radius: 5px;'>" +
            "<b>🎯 Karteikarten-System (nur existierende Fragen):</b><br>" +
            "<span style='color: #4CAF50;'>●</span> Grün (1-2): Gut beherrscht | " +
            "<span style='color: #FFEB3B;'>●</span> Gelb (3): Solide | " +
            "<span style='color: #FF5722;'>●</span> Orange-Rot (4-6): Üben erforderlich" +
            "</div>" +
            "</html>"
        );
        explanationLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5));
        karteikartenPanel.add(explanationLabel);

        // Group questions by Karteikarten level (already filtered)
        for (int level = 6; level >= 1; level--) {
            List<String> questionsAtLevel = getQuestionsByLevel.apply(level);

            if (!questionsAtLevel.isEmpty()) {
                JPanel levelPanel = createKarteikartenLevelPanel(level, questionsAtLevel);
                karteikartenPanel.add(levelPanel);
                karteikartenPanel.add(Box.createVerticalStrut(5));
            }
        }

        // Add questions needing practice section (already filtered)
        if (!practiceQuestions.isEmpty()) {
            karteikartenPanel.add(Box.createVerticalStrut(10));
            JPanel practicePanel = createPracticePanel(practiceQuestions);
            karteikartenPanel.add(practicePanel);
        }

        karteikartenPanel.revalidate();
        karteikartenPanel.repaint();
    }
    
    private JPanel createKarteikartenLevelPanel(int level, List<String> questions) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Get color and message for this level
        String color = getColorForLevel(level);
        String message = getMessageForLevel(level);
        
        // Header
        JLabel headerLabel = new JLabel(String.format("Level %d (%d Fragen) - %s", level, questions.size(), message));
        headerLabel.setOpaque(true);
        headerLabel.setBackground(Color.decode(color));
        headerLabel.setForeground(level >= 4 ? Color.WHITE : Color.BLACK);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(headerLabel, BorderLayout.NORTH);
        
        // Questions list (show first 5, then "...")
        JPanel questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        
        int maxShow = Math.min(5, questions.size());
        for (int i = 0; i < maxShow; i++) {
            JLabel questionLabel = new JLabel("• " + questions.get(i));
            questionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            questionsPanel.add(questionLabel);
        }
        
        if (questions.size() > 5) {
            JLabel moreLabel = new JLabel("... und " + (questions.size() - 5) + " weitere");
            moreLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
            moreLabel.setForeground(Color.GRAY);
            questionsPanel.add(moreLabel);
        }
        
        panel.add(questionsPanel, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        
        return panel;
    }
    
    private JPanel createPracticePanel(List<String> practiceQuestions) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("🎯 Empfohlene Übungsfragen"));
        
        JTextArea textArea = new JTextArea(8, 40); // Explizite Zeilen- und Spaltenanzahl
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textArea.setBackground(new Color(255, 245, 245)); // Light red background
        textArea.setLineWrap(true); // Automatischer Zeilenumbruch
        textArea.setWrapStyleWord(true); // Umbruch bei Wortgrenzen
        
        StringBuilder sb = new StringBuilder();
        sb.append("Diese Fragen solltest du noch üben:\n\n");
        for (String question : practiceQuestions) {
            sb.append("• ").append(question).append("\n");
        }
        
        textArea.setText(sb.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(0, 200)); // Vergrößert von 150 auf 200 für bessere Sichtbarkeit
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void updateThemeStatistics() {
        themeStatsPanel.removeAll();

        Collection<ThemeStatistics> allThemeStats = getThemeStats.get();

        // Filter statistics to only show themes that actually exist in the system
        List<String> existingThemes = delegate.getAllTopics();
        List<ThemeStatistics> validThemeStats = allThemeStats.stream()
            .filter(stats -> existingThemes.contains(stats.themeName))
            .filter(stats -> stats.totalAttempts > 0) // Only show themes that have been played
            .collect(java.util.stream.Collectors.toList());

        if (validThemeStats.isEmpty()) {
            JLabel noDataLabel = new JLabel(
                "<html>" +
                "<div style='font-family: Arial; padding: 20px; text-align: center;'>" +
                "<h3 style='color: #2E86AB;'>📚 Themen-Statistiken</h3>" +
                "<p style='color: #666; margin: 20px 0;'>Noch keine verfügbaren Themen gespielt.</p>" +
                "<p style='color: #888; font-size: 12px;'>" +
                "Verfügbare Themen: " + existingThemes.size() + "<br>" +
                "Gespielte Themen: 0<br><br>" +
                "Wähle ein Thema im 'Quiz Game' Tab aus, um Statistiken zu sammeln!" +
                "</p>" +
                "</div>" +
                "</html>"
            );
            noDataLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            themeStatsPanel.add(noDataLabel);
        } else {
            for (ThemeStatistics stats : validThemeStats) {
                JPanel themePanel = createThemeStatsPanel(stats);
                themeStatsPanel.add(themePanel);
                themeStatsPanel.add(Box.createVerticalStrut(5));
            }
        }

        themeStatsPanel.revalidate();
        themeStatsPanel.repaint();
    }
    
    private JPanel createThemeStatsPanel(ThemeStatistics stats) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(stats.themeName));
        
        String statsText = String.format(
            "<html>" +
            "Versuche: %d<br>" +
            "Richtig: %d<br>" +
            "Erfolgsrate: %.1f%%" +
            "</html>",
            stats.totalAttempts, stats.correctAttempts, stats.getSuccessRate()
        );
        
        JLabel statsLabel = new JLabel(statsText);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(statsLabel, BorderLayout.CENTER);
        
        // Color coding based on success rate
        Color bgColor;
        if (stats.getSuccessRate() >= 80) {
            bgColor = new Color(200, 255, 200); // Light green
        } else if (stats.getSuccessRate() >= 60) {
            bgColor = new Color(255, 255, 200); // Light yellow
        } else {
            bgColor = new Color(255, 220, 220); // Light red
        }
        panel.setBackground(bgColor);
        
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }
    
    private String getColorForLevel(int level) {
        switch (level) {
            case 1: return "#4CAF50"; // Green
            case 2: return "#8BC34A"; // Light Green
            case 3: return "#FFEB3B"; // Yellow
            case 4: return "#FF9800"; // Orange
            case 5: return "#FF5722"; // Deep Orange
            case 6: return "#F44336"; // Red
            default: return "#9E9E9E"; // Grey
        }
    }
    
    private String getMessageForLevel(int level) {
        switch (level) {
            case 1: return "Perfekt beherrscht! 🌟";
            case 2: return "Sehr gut! 👍";
            case 3: return "Solide Kenntnisse 👌";
            case 4: return "Noch etwas üben 📚";
            case 5: return "Mehr Übung erforderlich 💪";
            case 6: return "Hier solltest du noch dran arbeiten! 🎯";
            default: return "Noch nicht bewertet";
        }
    }
    
    private void displayNoDataMessage() {
        overallStatsLabel.setText("<html>Statistik-Service nicht verfügbar.<br>Bitte starte das Quiz-Spiel zuerst.</html>");
        karteikartenPanel.removeAll();
        themeStatsPanel.removeAll();
        
        JLabel noDataLabel = new JLabel("Keine Daten verfügbar.");
        karteikartenPanel.add(noDataLabel);
        
        karteikartenPanel.revalidate();
        karteikartenPanel.repaint();
        themeStatsPanel.revalidate();
        themeStatsPanel.repaint();
    }
    
    private void resetStatistics() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Möchtest du wirklich alle Statistiken zurücksetzen?\nDiese Aktion kann nicht rückgängig gemacht werden.",
            "Statistiken zurücksetzen",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION && statisticsService != null) {
            // Create new statistics service (effectively resetting)
            statisticsService = new ModularQuizStatistics();
            initializeLambdas();
            refreshDisplay.run();
            
            JOptionPane.showMessageDialog(
                this,
                "Alle Statistiken wurden zurückgesetzt.",
                "Zurückgesetzt",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * Shows the settings dialog for configuring statistics display and behavior.
     */
    private void showSettingsDialog() {
        JDialog settingsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                           "Statistik-Einstellungen", true);
        settingsDialog.setSize(500, 400);
        settingsDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // === KARTEIKARTEN EINSTELLUNGEN ===
        JPanel karteikartenSettings = new JPanel(new GridLayout(0, 2, 10, 10));
        karteikartenSettings.setBorder(BorderFactory.createTitledBorder("Karteikarten-System"));

        // Anzahl der Schwierigkeitsstufen
        karteikartenSettings.add(new JLabel("Schwierigkeitsstufen (1-10):"));
        JSpinner levelSpinner = new JSpinner(new SpinnerNumberModel(6, 1, 10, 1));
        karteikartenSettings.add(levelSpinner);

        // Wiederholungsintervall
        karteikartenSettings.add(new JLabel("Wiederholungsintervall (Tage):"));
        JSpinner intervalSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
        karteikartenSettings.add(intervalSpinner);

        // === ANZEIGE EINSTELLUNGEN ===
        JPanel displaySettings = new JPanel(new GridLayout(0, 1, 5, 5));
        displaySettings.setBorder(BorderFactory.createTitledBorder("Anzeige-Einstellungen"));

        JCheckBox showPercentagesBox = new JCheckBox("Prozentangaben anzeigen", true);
        JCheckBox showDatesBox = new JCheckBox("Erstellungsdaten anzeigen", true);
        JCheckBox showDifficultyBox = new JCheckBox("Schwierigkeitsgrad anzeigen", true);
        JCheckBox autoRefreshBox = new JCheckBox("Automatische Aktualisierung", false);

        displaySettings.add(showPercentagesBox);
        displaySettings.add(showDatesBox);
        displaySettings.add(showDifficultyBox);
        displaySettings.add(autoRefreshBox);

        // === EXPORT EINSTELLUNGEN ===
        JPanel exportSettings = new JPanel(new GridLayout(0, 2, 10, 10));
        exportSettings.setBorder(BorderFactory.createTitledBorder("Export-Einstellungen"));

        exportSettings.add(new JLabel("Export-Format:"));
        JComboBox<String> formatCombo = new JComboBox<>(new String[]{"CSV", "JSON", "XML", "TXT"});
        exportSettings.add(formatCombo);

        exportSettings.add(new JLabel("Zeitraum:"));
        JComboBox<String> timeRangeCombo = new JComboBox<>(new String[]{
            "Letzte 7 Tage", "Letzte 30 Tage", "Letzte 90 Tage", "Alle Daten"
        });
        exportSettings.add(timeRangeCombo);

        // === LAYOUT ===
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.add(karteikartenSettings);
        centerPanel.add(displaySettings);
        centerPanel.add(exportSettings);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // === BUTTONS ===
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveBtn = new JButton("Speichern");
        JButton cancelBtn = new JButton("Abbrechen");
        JButton exportBtn = new JButton("Daten exportieren");

        saveBtn.addActionListener(e -> {
            // Hier würden die Einstellungen gespeichert werden
            JOptionPane.showMessageDialog(settingsDialog,
                "Einstellungen gespeichert!\n" +
                "Schwierigkeitsstufen: " + levelSpinner.getValue() + "\n" +
                "Wiederholungsintervall: " + intervalSpinner.getValue() + " Tage\n" +
                "Export-Format: " + formatCombo.getSelectedItem(),
                "Einstellungen", JOptionPane.INFORMATION_MESSAGE);
            settingsDialog.dispose();
        });

        cancelBtn.addActionListener(e -> settingsDialog.dispose());

        exportBtn.addActionListener(e -> {
            String format = (String) formatCombo.getSelectedItem();
            String timeRange = (String) timeRangeCombo.getSelectedItem();
            JOptionPane.showMessageDialog(settingsDialog,
                "Export wird vorbereitet...\n" +
                "Format: " + format + "\n" +
                "Zeitraum: " + timeRange + "\n\n" +
                "Diese Funktion ist in der Demo-Version verfügbar.",
                "Export", JOptionPane.INFORMATION_MESSAGE);
        });

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(exportBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        settingsDialog.add(mainPanel);
        settingsDialog.setVisible(true);
    }
}
