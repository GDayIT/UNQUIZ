package dbbl.migration;

import dbbl.PersistenceDelegate;
import dbbl.RepoQuizeeQuestions;
import guimodule.AdaptiveLeitnerCard;
import guimodule.AdaptiveLeitnerSystem;
import guimodule.ModularQuizPlay;
import guimodule.ModularQuizStatistics;
import guimodule.achievements.AchievementsService;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

/**
 * Service responsible for merging external persisted data into the current
 * application state in a safe, duplicate-aware, modular manner.
 *
 * <p>Supports merging of:
 * <ul>
 *   <li>Questions and themes (quiz_questions.dat)</li>
 *   <li>Statistics (quiz_statistics.dat)</li>
 *   <li>Leitner flashcards (leitner_system.dat)</li>
 *   <li>Achievements (achievements.dat)</li>
 * </ul>
 *
 * <p>Strategies:
 * <ul>
 *   <li>Questions/Themes: deduplicated by (theme, title) using {@link PersistenceDelegate}</li>
 *   <li>Statistics: counters merged and results deduplicated by (theme|title|timestamp)</li>
 *   <li>Leitner: merge cards by question ID according to a policy</li>
 *   <li>Achievements: union with timestamp precedence for conflicts</li>
 * </ul>
 * 
 * Implements {@link Serializable} for possible persistence or transport.
 * 
 * Author: D.Georgiou
 * Version: 1.0
 */
public class DataMergeService implements Serializable {

    private static final long serialVersionUID = 1L;

    // ------------------- INNER CLASSES -------------------

    /**
     * Report summarizing the results of a merge operation.
     * Counts added, updated, and merged items across all categories.
     */
    public static class MergeReport implements Serializable {
        
		public int themesAdded;
        public int themesUpdated;
        public int questionsAdded;
        public int questionsUpdated;
        public int statsQuestionsMerged;
        public int statsThemesMerged;
        public int statsResultsAdded;
        public int leitnerCardsAdded;
        public int leitnerCardsUpdated;
        public int achievementsAdded;

        @Override
        public String toString() {
            return "MergeReport{" +
                "themesAdded=" + themesAdded +
                ", themesUpdated=" + themesUpdated +
                ", questionsAdded=" + questionsAdded +
                ", questionsUpdated=" + questionsUpdated +
                ", statsQuestionsMerged=" + statsQuestionsMerged +
                ", statsThemesMerged=" + statsThemesMerged +
                ", statsResultsAdded=" + statsResultsAdded +
                ", leitnerCardsAdded=" + leitnerCardsAdded +
                ", leitnerCardsUpdated=" + leitnerCardsUpdated +
                ", achievementsAdded=" + achievementsAdded +
                '}';
        }
    }

    // ------------------- FIELDS -------------------

    /** Reference to persistence delegate for saving/loading themes and questions */
    private final PersistenceDelegate persistence;

    /** Reference to the adaptive Leitner flashcard system */
    private final AdaptiveLeitnerSystem leitnerSystem;

    /** Reference to quiz statistics manager */
    private final ModularQuizStatistics statistics;

    /** Reference to achievements service */
    private final AchievementsService achievementsService;

    /** Policies to resolve Leitner card conflicts */
    public enum LeitnerMergePolicy { PREFER_EXISTING, PREFER_INCOMING, PREFER_HIGHER_LEVEL, PREFER_NEWER }

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the DataMergeService with references to required systems.
     * 
     * @param persistence The persistence delegate for themes/questions
     * @param leitnerSystem The adaptive Leitner system for flashcards
     * @param statistics The statistics manager for quizzes
     * @param achievementsService The achievements service for merging achievements
     */
    public DataMergeService(PersistenceDelegate persistence,
                            AdaptiveLeitnerSystem leitnerSystem,
                            ModularQuizStatistics statistics,
                            AchievementsService achievementsService) {
        this.persistence = persistence;
        this.leitnerSystem = leitnerSystem;
        this.statistics = statistics;
        this.achievementsService = achievementsService;
    }

    // ------------------- PUBLIC METHODS -------------------

    /**
     * Merges data from a given directory into the current application state.
     * 
     * @param dir The directory containing data files
     * @param policy Leitner merge policy for resolving conflicts
     * @return A {@link MergeReport} summarizing the merge outcome
     * @throws IllegalArgumentException if the directory does not exist
     */
    public MergeReport mergeFromDirectory(String dir, LeitnerMergePolicy policy) {
        MergeReport report = new MergeReport();
        File base = new File(dir);
        if (!base.exists() || !base.isDirectory()) {
            throw new IllegalArgumentException("Directory not found: " + dir);
        }

        // Merge questions/themes
        File qf = new File(base, "quiz_questions.dat");
        if (qf.exists()) mergeQuestions(qf, report);

        // Merge statistics
        File sf = new File(base, "quiz_statistics.dat");
        if (sf.exists() && statistics != null) mergeStatistics(sf, report);

        // Merge Leitner cards
        File lf = new File(base, "leitner_system.dat");
        if (lf.exists() && leitnerSystem != null) mergeLeitner(lf, report, policy);

        // Merge achievements
        File af = new File(base, "achievements.dat");
        if (af.exists() && achievementsService != null) {
            try {
                AchievementsService.Snapshot snap = AchievementsService.loadSnapshot(af);
                int before = achievementsService.getUnlocked().size();
                achievementsService.mergeSnapshot(snap);
                int after = achievementsService.getUnlocked().size();
                report.achievementsAdded += Math.max(0, after - before);
                achievementsService.save();
            } catch (Exception ignored) {}
        }

        return report;
    }

    // ------------------- PRIVATE MERGE METHODS -------------------

    /**
     * Merges questions and themes from a file.
     */
    private void mergeQuestions(File file, MergeReport report) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            Map<String, List<RepoQuizeeQuestions>> questionsByTheme = null;
            Map<String, String> themeDescriptions = null;

            // Reflection-based extraction of ModularPersistenceService Snapshot
            if (obj != null && obj.getClass().getName().endsWith("ModularPersistenceService$Snapshot")) {
                java.lang.reflect.Field qf = obj.getClass().getDeclaredField("questionsByTheme");
                qf.setAccessible(true);
                Object qm = qf.get(obj);
                if (qm instanceof Map) questionsByTheme = (Map<String, List<RepoQuizeeQuestions>>) qm;

                java.lang.reflect.Field tf = obj.getClass().getDeclaredField("themeDescriptions");
                tf.setAccessible(true);
                Object tm = tf.get(obj);
                if (tm instanceof Map) themeDescriptions = (Map<String, String>) tm;
            }

            // Merge themes
            if (themeDescriptions != null) {
                for (Map.Entry<String, String> e : themeDescriptions.entrySet()) {
                    PersistenceDelegate.ThemeData td = new PersistenceDelegate.ThemeData(e.getKey(), e.getValue());
                    boolean ok = persistence.saveTheme().apply(td);
                    if (ok) report.themesAdded++; else report.themesUpdated++;
                }
            }

            // Merge questions
            if (questionsByTheme != null) {
                for (Map.Entry<String, List<RepoQuizeeQuestions>> e : questionsByTheme.entrySet()) {
                    String theme = e.getKey();
                    List<RepoQuizeeQuestions> list = e.getValue();
                    if (list == null) continue;
                    for (RepoQuizeeQuestions q : list) {
                        PersistenceDelegate.QuestionData qd = new PersistenceDelegate.QuestionData(
                            theme,
                            q.getTitel(),
                            q.getFrageText(),
                            q.getErklaerung() != null ? q.getErklaerung() : "",
                            q.getAntworten(),
                            q.getKorrekt()
                        );
                        boolean ok = persistence.saveQuestion().apply(qd);
                        if (ok) report.questionsAdded++; else report.questionsUpdated++;
                    }
                }
            }

        } catch (Exception ignored) {}
    }

    /**
     * Merges statistics from a serialized file into the current statistics object.
     */
    private void mergeStatistics(File file, MergeReport report) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            Map<String, ModularQuizStatistics.QuestionStatistics> qStats = new HashMap<>();
            Map<String, ModularQuizStatistics.ThemeStatistics> tStats = new HashMap<>();
            List<ModularQuizPlay.QuizResult> results = new ArrayList<>();

            if (obj instanceof Map) {
                Map<String, ?> data = (Map<String, ?>) obj;
                convertQuestionStatsMap((Map<String, ?>) data.get("questionStats"), qStats);
                convertThemeStatsMap((Map<String, ?>) data.get("themeStats"), tStats);
                convertResults((List<?>) data.get("allResults"), results);
            }

            statistics.mergeData(qStats, tStats, results, true);
            report.statsQuestionsMerged += qStats.size();
            report.statsThemesMerged += tStats.size();
            report.statsResultsAdded += results.size();

        } catch (Exception ignored) {}
    }

    /**
     * Merges Leitner cards from a file using the selected policy.
     */
    private void mergeLeitner(File file, MergeReport report, LeitnerMergePolicy p) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            Map<String, AdaptiveLeitnerCard> cards = null;

            if (obj != null && obj.getClass().getName().endsWith("AdaptiveLeitnerSystem$Snapshot")) {
                java.lang.reflect.Field cf = obj.getClass().getDeclaredField("cards");
                cf.setAccessible(true);
                cards = (Map<String, AdaptiveLeitnerCard>) cf.get(obj);
            }

            if (cards != null && !cards.isEmpty()) {
                AdaptiveLeitnerSystem.MergePolicy mp = AdaptiveLeitnerSystem.MergePolicy.PREFER_HIGHER_LEVEL;
                switch (p) {
                    case PREFER_EXISTING: mp = AdaptiveLeitnerSystem.MergePolicy.PREFER_EXISTING; break;
                    case PREFER_INCOMING: mp = AdaptiveLeitnerSystem.MergePolicy.PREFER_INCOMING; break;
                    case PREFER_NEWER: mp = AdaptiveLeitnerSystem.MergePolicy.PREFER_NEWER; break;
                    case PREFER_HIGHER_LEVEL: default: break;
                }
                leitnerSystem.mergeCards(cards, mp);
                report.leitnerCardsAdded += cards.size();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Converts arbitrary maps to QuestionStatistics.
     */
    private void convertQuestionStatsMap(Map<String, ?> src,
                                         Map<String, ModularQuizStatistics.QuestionStatistics> dst) throws Exception {
        if (src == null) return;
        for (Map.Entry<String, ?> e : src.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();
            ModularQuizStatistics.QuestionStatistics q = new ModularQuizStatistics.QuestionStatistics((String)getField(val,"questionTitle", key));
            q.totalAttempts = (Integer) getField(val,"totalAttempts",0);
            q.correctAttempts = (Integer) getField(val,"correctAttempts",0);
            q.consecutiveCorrect = (Integer) getField(val,"consecutiveCorrect",0);
            q.consecutiveWrong = (Integer) getField(val,"consecutiveWrong",0);
            q.lastAttempt = (Long) getField(val,"lastAttempt",0L);
            q.karteikartenLevel = (Integer) getField(val,"karteikartenLevel",1);
            dst.put(key,q);
        }
    }

    /**
     * Converts arbitrary maps to ThemeStatistics.
     */
    private void convertThemeStatsMap(Map<String, ?> src,
                                      Map<String, ModularQuizStatistics.ThemeStatistics> dst) throws Exception {
        if (src == null) return;
        for (Object val : src.values()) {
            ModularQuizStatistics.ThemeStatistics t = new ModularQuizStatistics.ThemeStatistics((String)getField(val,"themeName",""));
            t.totalQuestions = (Integer)getField(val,"totalQuestions",0);
            t.totalAttempts = (Integer)getField(val,"totalAttempts",0);
            t.correctAttempts = (Integer)getField(val,"correctAttempts",0);
            t.lastPlayed = (Long)getField(val,"lastPlayed",0L);
            dst.put(t.name,t);//evtl.problematisch mögliches TODO da felder evtl. nicht eindeutig und damit inkonsistent/disharmonie etablieren könnten(also gerne beachten)!
        }
    }

    /**
     * Converts arbitrary lists to QuizResult objects.
     */
    private void convertResults(List<?> src, List<ModularQuizPlay.QuizResult> dst) {
        if (src == null) return;
        for (Object o : src) {
            try {
                ModularQuizPlay.QuizResult r = new ModularQuizPlay.QuizResult(
                        (String)getField(o,"theme",""),
                        (String)getField(o,"questionTitle",""),
                        (String)getField(o,"userAnswer",""),
                        (String)getField(o,"correctAnswer",""),
                        (Boolean)getField(o,"isCorrect",Boolean.FALSE),
                        (Long)getField(o,"answerTimeMs",0L)
                );
                dst.add(r);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Reflection-based safe field getter with default value.
     */
    private Object getField(Object obj, String name, Object def) throws Exception {
        if (obj == null) return def;
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            Object v = f.get(obj);
            return v != null ? v : def;
        } catch (NoSuchFieldException ex) {
            return def;
        }
    }
}