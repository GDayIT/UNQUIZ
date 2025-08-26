package guimodule;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Modular Quiz Statistics Service with persistence and lambda-based operations.
 * 
 * Features:
 * - Persistent answer tracking
 * - Karteikarten-System (Spaced Repetition)
 * - Theme-based statistics
 * - Lambda-based data processing
 * - Functional composition for complex queries
 * 
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
=======
 * @author Quiz Application Team
 * @version 2.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class ModularQuizStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String STATISTICS_FILE = "quiz_statistics.dat";

    // Desktop integration
    private QuizApplicationManager appManager;
    
    // Statistics storage
    private final Map<String, QuestionStatistics> questionStats = new ConcurrentHashMap<>();
    private final Map<String, ThemeStatistics> themeStats = new ConcurrentHashMap<>();
    private final List<ModularQuizPlay.QuizResult> allResults = new ArrayList<>();
    
    // Lambda-based operations
    private Function<String, QuestionStatistics> getQuestionStats;
    private Function<String, ThemeStatistics> getThemeStats;
    private Function<String, List<ModularQuizPlay.QuizResult>> getResultsForQuestion;
    private Function<String, Double> calculateSuccessRate;
    private Function<String, Integer> getKarteikartenLevel;
    private Predicate<String> needsPractice;
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
        
        public String getKarteikartenColor() {
            switch (karteikartenLevel) {
                case 1: return "#4CAF50"; // Green
                case 2: return "#8BC34A"; // Light Green
                case 3: return "#FFEB3B"; // Yellow
                case 4: return "#FF9800"; // Orange
                case 5: return "#FF5722"; // Deep Orange
                case 6: return "#F44336"; // Red
                default: return "#9E9E9E"; // Grey
            }
        }
        
        public String getKarteikartenMessage() {
            switch (karteikartenLevel) {
                case 1: return "Perfekt beherrscht! 🌟";
                case 2: return "Sehr gut! 👍";
                case 3: return "Solide Kenntnisse 👌";
                case 4: return "Noch etwas üben 📚";
                case 5: return "Mehr Übung erforderlich 💪";
                case 6: return "Hier solltest du noch dran arbeiten! 🎯";
                default: return "Noch nicht bewertet";
            }
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
    
    public ModularQuizStatistics() {
        this.appManager = new QuizApplicationManager();
        initializeLambdas();
        loadStatistics();
    }
    
    /**
     * Initialize lambda-based operations.
     */
    private void initializeLambdas() {
        // Get or create question statistics
        getQuestionStats = questionTitle -> 
            questionStats.computeIfAbsent(questionTitle, QuestionStatistics::new);
        
        // Get or create theme statistics
        getThemeStats = themeName -> 
            themeStats.computeIfAbsent(themeName, ThemeStatistics::new);
        
        // Get all results for a specific question
        getResultsForQuestion = questionTitle -> 
            allResults.stream()
                .filter(result -> result.questionTitle.equals(questionTitle))
                .collect(Collectors.toList());
        
        // Calculate success rate for question
        calculateSuccessRate = questionTitle -> {
            QuestionStatistics stats = getQuestionStats.apply(questionTitle);
            return stats.getSuccessRate();
        };
        
        // Get Karteikarten level (1-6)
        getKarteikartenLevel = questionTitle -> {
            QuestionStatistics stats = getQuestionStats.apply(questionTitle);
            return stats.karteikartenLevel;
        };
        
        // Check if question needs practice (level 4-6)
        needsPractice = questionTitle -> getKarteikartenLevel.apply(questionTitle) >= 4;
        
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
                if (qStats.consecutiveCorrect >= 3 && qStats.karteikartenLevel > 1) {
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
        };
    }
    
    /**
     * Record a quiz answer.
     */
    public void recordAnswer(ModularQuizPlay.QuizResult result) {
        recordAnswerImpl.accept(result);
    }
    
    /**
     * Get all questions that need practice (Karteikarten level 4-6).
     */
    public List<String> getQuestionsNeedingPractice() {
        return questionStats.keySet().stream()
            .filter(needsPractice)
            .sorted((q1, q2) -> Integer.compare(
                getKarteikartenLevel.apply(q2), 
                getKarteikartenLevel.apply(q1)
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Get questions by Karteikarten level.
     */
    public Map<Integer, List<String>> getQuestionsByKarteikartenLevel() {
        return questionStats.entrySet().stream()
            .collect(Collectors.groupingBy(
                entry -> entry.getValue().karteikartenLevel,
                Collectors.mapping(
                    Map.Entry::getKey,
                    Collectors.toList()
                )
            ));
    }
    
    /**
     * Get overall statistics.
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        int totalQuestions = questionStats.size();
        double avgSuccessRate = questionStats.values().stream()
            .mapToDouble(QuestionStatistics::getSuccessRate)
            .average()
            .orElse(0.0);
        
        long totalAttempts = questionStats.values().stream()
            .mapToLong(q -> q.totalAttempts)
            .sum();
        
        long totalCorrect = questionStats.values().stream()
            .mapToLong(q -> q.correctAttempts)
            .sum();
        
        stats.put("totalQuestions", totalQuestions);
        stats.put("averageSuccessRate", avgSuccessRate);
        stats.put("totalAttempts", totalAttempts);
        stats.put("totalCorrect", totalCorrect);
        stats.put("questionsNeedingPractice", getQuestionsNeedingPractice().size());
        
        return stats;
    }
    
    /**
     * Get theme statistics.
     */
    public Collection<ThemeStatistics> getThemeStatistics() {
        return themeStats.values();
    }
    
    /**
     * Get question statistics.
     */
    public Collection<QuestionStatistics> getQuestionStatistics() {
        return questionStats.values();
    }
    
    /**
     * Save statistics to file.
     */
    public void saveStatistics() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(STATISTICS_FILE))) {
            Map<String, Object> data = new HashMap<>();
            data.put("questionStats", questionStats);
            data.put("themeStats", themeStats);
            data.put("allResults", allResults);
            out.writeObject(data);
            System.out.println("Statistics saved successfully");
        } catch (IOException e) {
            System.err.println("Failed to save statistics: " + e.getMessage());
        }
    }
    
    /**
     * Load statistics from file.
     * Supports multiple formats for backward compatibility:
     * - Map-based snapshot (current default)
     * - ModularStatisticsPanel$StatisticsData (panel snapshot)
     * - ModularStatisticsPanel (legacy serialized panel)
     */
    @SuppressWarnings("unchecked")
    private void loadStatistics() {
        File f = new File(STATISTICS_FILE);
        if (!f.exists()) {
            System.out.println("No existing statistics found, starting fresh");
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(STATISTICS_FILE))) {
            Object obj = in.readObject();
            if (obj instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) obj;
                Map<String, QuestionStatistics> loadedQuestionStats =
                    (Map<String, QuestionStatistics>) data.get("questionStats");
                Map<String, ThemeStatistics> loadedThemeStats =
                    (Map<String, ThemeStatistics>) data.get("themeStats");
                List<ModularQuizPlay.QuizResult> loadedResults =
                    (List<ModularQuizPlay.QuizResult>) data.get("allResults");

                if (loadedQuestionStats != null) questionStats.putAll(loadedQuestionStats);
                if (loadedThemeStats != null) themeStats.putAll(loadedThemeStats);
                if (loadedResults != null) allResults.addAll(loadedResults);
            } else if (obj != null && obj.getClass().getName().endsWith("ModularStatisticsPanel$StatisticsData")) {
                loadFromPanelStatisticsData(obj);
            } else if (obj != null && obj.getClass().getName().equals("guimodule.ModularStatisticsPanel")) {
                // Legacy: load fields from serialized panel instance
                loadFromLegacyPanel(obj);
            } else {
                System.out.println("Unknown statistics data format, starting fresh");
            }
            System.out.println("Statistics loaded successfully");
        } catch (Exception e) {
            System.out.println("No existing statistics found, starting fresh");
        }
    }

    /**
     * Convert ModularStatisticsPanel$StatisticsData into local structures via reflection.
     */
    @SuppressWarnings("unchecked")
    private void loadFromPanelStatisticsData(Object dataObj) throws Exception {
        // Extract maps/lists via reflection
        java.lang.reflect.Field qf = dataObj.getClass().getDeclaredField("questionStats");
        qf.setAccessible(true);
        Map<String, ?> qsMap = (Map<String, ?>) qf.get(dataObj);

        java.lang.reflect.Field tf = dataObj.getClass().getDeclaredField("themeStats");
        tf.setAccessible(true);
        Map<String, ?> tsMap = (Map<String, ?>) tf.get(dataObj);

        java.lang.reflect.Field af = dataObj.getClass().getDeclaredField("allResults");
        af.setAccessible(true);
        List<ModularQuizPlay.QuizResult> results = (List<ModularQuizPlay.QuizResult>) af.get(dataObj);

        if (qsMap != null) {
            for (Map.Entry<String, ?> e : qsMap.entrySet()) {
                String key = e.getKey();
                Object src = e.getValue();
                QuestionStatistics dst = convertPanelQuestionStats(src);
                // Fallback: if title not set in src, use key
                if (dst != null) {
                    if (dst.questionTitle == null || dst.questionTitle.isEmpty()) {
                        try {
                            java.lang.reflect.Field t = dst.getClass().getDeclaredField("questionTitle");
                            t.setAccessible(true);
                            t.set(dst, key);
                        } catch (Exception ignore) {}
                    }
                    questionStats.put(dst.questionTitle, dst);
                }
            }
        }

        if (tsMap != null) {
            for (Object src : tsMap.values()) {
                ThemeStatistics dst = convertPanelThemeStats(src);
                if (dst != null) {
                    themeStats.put(dst.themeName, dst);
                }
            }
        }

        if (results != null) {
            allResults.addAll(results);
        }
    }

    /**
     * Convert legacy serialized panel into local structures via reflection.
     */
    @SuppressWarnings("unchecked")
    private void loadFromLegacyPanel(Object panelObj) throws Exception {
        java.lang.reflect.Field qf = panelObj.getClass().getDeclaredField("questionStats");
        qf.setAccessible(true);
        Map<String, ?> qsMap = (Map<String, ?>) qf.get(panelObj);

        java.lang.reflect.Field tf = panelObj.getClass().getDeclaredField("themeStats");
        tf.setAccessible(true);
        Map<String, ?> tsMap = (Map<String, ?>) tf.get(panelObj);

        java.lang.reflect.Field af = panelObj.getClass().getDeclaredField("allResults");
        af.setAccessible(true);
        List<ModularQuizPlay.QuizResult> results = (List<ModularQuizPlay.QuizResult>) af.get(panelObj);

        if (qsMap != null) {
            for (Map.Entry<String, ?> e : qsMap.entrySet()) {
                String key = e.getKey();
                Object src = e.getValue();
                QuestionStatistics dst = convertPanelQuestionStats(src);
                if (dst != null) {
                    if (dst.questionTitle == null || dst.questionTitle.isEmpty()) {
                        try {
                            java.lang.reflect.Field t = dst.getClass().getDeclaredField("questionTitle");
                            t.setAccessible(true);
                            t.set(dst, key);
                        } catch (Exception ignore) {}
                    }
                    questionStats.put(dst.questionTitle, dst);
                }
            }
        }
        if (tsMap != null) {
            for (Object src : tsMap.values()) {
                ThemeStatistics dst = convertPanelThemeStats(src);
                if (dst != null) {
                    themeStats.put(dst.themeName, dst);
                }
            }
        }
        if (results != null) {
            allResults.addAll(results);
        }
    }

    /**
     * Convert a ModularStatisticsPanel.QuestionStatistics via reflection.
     */
    private QuestionStatistics convertPanelQuestionStats(Object src) {
        if (src == null) return null;
        try {
            String title = (String) getFieldValue(src, "questionTitle");
            Integer totalAttempts = (Integer) getFieldValue(src, "totalAttempts");
            Integer correctAttempts = (Integer) getFieldValue(src, "correctAttempts");
            Integer consecutiveCorrect = (Integer) getFieldValue(src, "consecutiveCorrect");
            Integer consecutiveWrong = (Integer) getFieldValue(src, "consecutiveWrong");
            Long lastAttempt = (Long) getFieldValue(src, "lastAttempt");
            Integer level = (Integer) getFieldValue(src, "karteikartenLevel");

            QuestionStatistics dst = new QuestionStatistics(title);
            dst.totalAttempts = totalAttempts != null ? totalAttempts : 0;
            dst.correctAttempts = correctAttempts != null ? correctAttempts : 0;
            dst.consecutiveCorrect = consecutiveCorrect != null ? consecutiveCorrect : 0;
            dst.consecutiveWrong = consecutiveWrong != null ? consecutiveWrong : 0;
            dst.lastAttempt = lastAttempt != null ? lastAttempt : 0L;
            dst.karteikartenLevel = level != null ? level : 1;
            return dst;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert a ModularStatisticsPanel.ThemeStatistics via reflection.
     */
    private ThemeStatistics convertPanelThemeStats(Object src) {
        if (src == null) return null;
        try {
            String name = (String) getFieldValue(src, "themeName");
            Integer totalQuestions = (Integer) getFieldValue(src, "totalQuestions");
            Integer totalAttempts = (Integer) getFieldValue(src, "totalAttempts");
            Integer correctAttempts = (Integer) getFieldValue(src, "correctAttempts");
            Long lastPlayed = (Long) getFieldValue(src, "lastPlayed");

            ThemeStatistics dst = new ThemeStatistics(name);
            dst.totalQuestions = totalQuestions != null ? totalQuestions : 0;
            dst.totalAttempts = totalAttempts != null ? totalAttempts : 0;
            dst.correctAttempts = correctAttempts != null ? correctAttempts : 0;
            dst.lastPlayed = lastPlayed != null ? lastPlayed : 0L;
            return dst;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper to retrieve field via reflection.
     */
    private Object getFieldValue(Object obj, String name) throws Exception {
        java.lang.reflect.Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    /**
     * Merge incoming statistics into this service. Optionally deduplicate results.
     */
    public void mergeData(Map<String, QuestionStatistics> incomingQ,
                          Map<String, ThemeStatistics> incomingT,
                          List<ModularQuizPlay.QuizResult> incomingR,
                          boolean dedupeResults) {
        if (incomingQ != null) {
            for (Map.Entry<String, QuestionStatistics> e : incomingQ.entrySet()) {
                String title = e.getKey();
                QuestionStatistics inc = e.getValue();
                QuestionStatistics cur = questionStats.computeIfAbsent(title, QuestionStatistics::new);
                cur.totalAttempts += inc.totalAttempts;
                cur.correctAttempts += inc.correctAttempts;
                cur.consecutiveCorrect = Math.max(cur.consecutiveCorrect, inc.consecutiveCorrect);
                cur.consecutiveWrong = Math.max(cur.consecutiveWrong, inc.consecutiveWrong);
                cur.lastAttempt = Math.max(cur.lastAttempt, inc.lastAttempt);
                cur.karteikartenLevel = Math.max(cur.karteikartenLevel, inc.karteikartenLevel);
            }
        }
        if (incomingT != null) {
            for (Map.Entry<String, ThemeStatistics> e : incomingT.entrySet()) {
                String name = e.getKey();
                ThemeStatistics inc = e.getValue();
                ThemeStatistics cur = themeStats.computeIfAbsent(name, ThemeStatistics::new);
                cur.totalQuestions = Math.max(cur.totalQuestions, inc.totalQuestions);
                cur.totalAttempts += inc.totalAttempts;
                cur.correctAttempts += inc.correctAttempts;
                cur.lastPlayed = Math.max(cur.lastPlayed, inc.lastPlayed);
            }
        }
        if (incomingR != null && !incomingR.isEmpty()) {
            if (dedupeResults) {
                // Deduplicate by (theme|title|timestamp)
                Set<String> seen = new HashSet<>();
                for (ModularQuizPlay.QuizResult r : allResults) {
                    seen.add(r.theme + "|" + r.questionTitle + "|" + r.timestamp);
                }
                for (ModularQuizPlay.QuizResult r : incomingR) {
                    String key = r.theme + "|" + r.questionTitle + "|" + r.timestamp;
                    if (seen.add(key)) allResults.add(r);
                }
            } else {
                allResults.addAll(incomingR);
            }
        }
        saveStatistics();
    }
}
