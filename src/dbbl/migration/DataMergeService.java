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
 * DataMergeService merges external persisted data from a directory into the
 * current application state in a modular, duplicate-safe way.
 *
 * Supported sources (from a given directory):
 * - quiz_questions.dat (questions/themes)
 * - quiz_statistics.dat (statistics)
 * - leitner_system.dat (Leitner cards)
 * - achievements.dat (user achievements)
 *
 * Strategy:
 * - Questions/Themes: use PersistenceDelegate lambdas (saveTheme/saveQuestion)
 *   to add or update; duplicates identified by (theme, title)
 * - Statistics: merge counters and results; deduplicate results by (theme|title|timestamp)
 * - Leitner: merge cards by questionId with selectable merge policy
 * - Achievements: union of unlocked achievements; newer timestamp wins if tracked
 */
public class DataMergeService implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private final PersistenceDelegate persistence;
    private final AdaptiveLeitnerSystem leitnerSystem;
    private final ModularQuizStatistics statistics;
    private final AchievementsService achievementsService;

    public enum LeitnerMergePolicy { PREFER_EXISTING, PREFER_INCOMING, PREFER_HIGHER_LEVEL, PREFER_NEWER }

    public DataMergeService(PersistenceDelegate persistence,
                            AdaptiveLeitnerSystem leitnerSystem,
                            ModularQuizStatistics statistics,
                            AchievementsService achievementsService) {
        this.persistence = persistence;
        this.leitnerSystem = leitnerSystem;
        this.statistics = statistics;
        this.achievementsService = achievementsService;
    }

    public MergeReport mergeFromDirectory(String dir, LeitnerMergePolicy policy) {
        MergeReport report = new MergeReport();
        File base = new File(dir);
        if (!base.exists() || !base.isDirectory()) {
            throw new IllegalArgumentException("Directory not found: " + dir);
        }

        // 1) Merge Questions/Themes
        File qf = new File(base, "quiz_questions.dat");
        if (qf.exists()) {
            mergeQuestions(qf, report);
        }

        // 2) Merge Statistics
        File sf = new File(base, "quiz_statistics.dat");
        if (sf.exists() && statistics != null) {
            mergeStatistics(sf, report);
        }

        // 3) Merge Leitner
        File lf = new File(base, "leitner_system.dat");
        if (lf.exists() && leitnerSystem != null) {
            mergeLeitner(lf, report, policy);
        }

        // 4) Merge Achievements
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

    private void mergeQuestions(File file, MergeReport report) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            Map<String, List<RepoQuizeeQuestions>> questionsByTheme = null;
            Map<String, String> themeDescriptions = null;

            // Try reflection on Snapshot (ModularPersistenceService$Snapshot)
            if (obj != null && obj.getClass().getName().endsWith("ModularPersistenceService$Snapshot")) {
                java.lang.reflect.Field qf = obj.getClass().getDeclaredField("questionsByTheme");
                qf.setAccessible(true);
                Object qm = qf.get(obj);
                if (qm instanceof Map) {
                    questionsByTheme = (Map<String, List<RepoQuizeeQuestions>>) qm;
                }
                java.lang.reflect.Field tf = obj.getClass().getDeclaredField("themeDescriptions");
                tf.setAccessible(true);
                Object tm = tf.get(obj);
                if (tm instanceof Map) {
                    themeDescriptions = (Map<String, String>) tm;
                }
            } else if (obj instanceof Map) {
                // Unrecognized map format, skip
            } else if (obj != null && obj.getClass().getName().contains("ModularPersistenceService")) {
                // Legacy service object
                java.lang.reflect.Field qf2 = obj.getClass().getDeclaredField("questionsByTheme");
                qf2.setAccessible(true);
                Object qm = qf2.get(obj);
                if (qm instanceof Map) {
                    questionsByTheme = (Map<String, List<RepoQuizeeQuestions>>) qm;
                }
                java.lang.reflect.Field tf2 = obj.getClass().getDeclaredField("themeDescriptions");
                tf2.setAccessible(true);
                Object tm = tf2.get(obj);
                if (tm instanceof Map) {
                    themeDescriptions = (Map<String, String>) tm;
                }
            }

            if (themeDescriptions != null) {
                for (Map.Entry<String, String> e : themeDescriptions.entrySet()) {
                    String title = e.getKey();
                    String desc = e.getValue();
                    PersistenceDelegate.ThemeData td = new PersistenceDelegate.ThemeData(title, desc);
                    boolean ok = persistence.saveTheme().apply(td);
                    if (ok) report.themesAdded++; else report.themesUpdated++; // heuristic
                }
            }

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
                        if (ok) report.questionsAdded++; else report.questionsUpdated++; // heuristic
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void mergeStatistics(File file, MergeReport report) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            Map<String, ModularQuizStatistics.QuestionStatistics> qStats = new HashMap<>();
            Map<String, ModularQuizStatistics.ThemeStatistics> tStats = new HashMap<>();
            List<ModularQuizPlay.QuizResult> results = new ArrayList<>();

            if (obj instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) obj;
                Map<String, ?> qs = (Map<String, ?>) data.get("questionStats");
                Map<String, ?> ts = (Map<String, ?>) data.get("themeStats");
                List<?> rs = (List<?>) data.get("allResults");
                if (qs != null) convertQuestionStatsMap(qs, qStats);
                if (ts != null) convertThemeStatsMap(ts, tStats);
                if (rs != null) convertResults(rs, results);
            } else if (obj != null && obj.getClass().getName().endsWith("ModularStatisticsPanel$StatisticsData")) {
                // Reflection-based conversion
                java.lang.reflect.Field qf = obj.getClass().getDeclaredField("questionStats");
                qf.setAccessible(true);
                convertQuestionStatsMap((Map<String, ?>) qf.get(obj), qStats);
                java.lang.reflect.Field tf = obj.getClass().getDeclaredField("themeStats");
                tf.setAccessible(true);
                convertThemeStatsMap((Map<String, ?>) tf.get(obj), tStats);
                java.lang.reflect.Field af = obj.getClass().getDeclaredField("allResults");
                af.setAccessible(true);
                convertResults((List<?>) af.get(obj), results);
            } else if (obj != null && obj.getClass().getName().equals("guimodule.ModularStatisticsPanel")) {
                java.lang.reflect.Field qf = obj.getClass().getDeclaredField("questionStats");
                qf.setAccessible(true);
                convertQuestionStatsMap((Map<String, ?>) qf.get(obj), qStats);
                java.lang.reflect.Field tf = obj.getClass().getDeclaredField("themeStats");
                tf.setAccessible(true);
                convertThemeStatsMap((Map<String, ?>) tf.get(obj), tStats);
                java.lang.reflect.Field af = obj.getClass().getDeclaredField("allResults");
                af.setAccessible(true);
                convertResults((List<?>) af.get(obj), results);
            }

            int beforeQ = qStats.size();
            int beforeT = tStats.size();
            int beforeR = results.size();

            statistics.mergeData(qStats, tStats, results, true);

            report.statsQuestionsMerged += beforeQ;
            report.statsThemesMerged += beforeT;
            report.statsResultsAdded += beforeR; // merged results are deduped internally
        } catch (Exception ignored) {}
    }

    private void mergeLeitner(File file, MergeReport report, LeitnerMergePolicy p) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            Map<String, AdaptiveLeitnerCard> cards = null;
            if (obj != null && obj.getClass().getName().endsWith("AdaptiveLeitnerSystem$Snapshot")) {
                java.lang.reflect.Field cf = obj.getClass().getDeclaredField("cards");
                cf.setAccessible(true);
                cards = (Map<String, AdaptiveLeitnerCard>) cf.get(obj);
            } else if (obj != null && obj.getClass().getName().equals("guimodule.AdaptiveLeitnerSystem")) {
                java.lang.reflect.Field cf = obj.getClass().getDeclaredField("cards");
                cf.setAccessible(true);
                cards = (Map<String, AdaptiveLeitnerCard>) cf.get(obj);
            }
            if (cards != null && !cards.isEmpty()) {
                int size = cards.size();
                AdaptiveLeitnerSystem.MergePolicy mp = AdaptiveLeitnerSystem.MergePolicy.PREFER_HIGHER_LEVEL;
                switch (p) {
                    case PREFER_EXISTING: mp = AdaptiveLeitnerSystem.MergePolicy.PREFER_EXISTING; break;
                    case PREFER_INCOMING: mp = AdaptiveLeitnerSystem.MergePolicy.PREFER_INCOMING; break;
                    case PREFER_NEWER: mp = AdaptiveLeitnerSystem.MergePolicy.PREFER_NEWER; break;
                    case PREFER_HIGHER_LEVEL: default: mp = AdaptiveLeitnerSystem.MergePolicy.PREFER_HIGHER_LEVEL; break;
                }
                leitnerSystem.mergeCards(cards, mp);
                report.leitnerCardsAdded += size; // approximate
            }
        } catch (Exception ignored) {}
    }

    private void convertQuestionStatsMap(Map<String, ?> src,
                                         Map<String, ModularQuizStatistics.QuestionStatistics> dst) throws Exception {
        for (Map.Entry<String, ?> e : src.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();
            String title = (String) getField(val, "questionTitle", key);
            Integer totalAttempts = (Integer) getField(val, "totalAttempts", 0);
            Integer correctAttempts = (Integer) getField(val, "correctAttempts", 0);
            Integer consecutiveCorrect = (Integer) getField(val, "consecutiveCorrect", 0);
            Integer consecutiveWrong = (Integer) getField(val, "consecutiveWrong", 0);
            Long lastAttempt = (Long) getField(val, "lastAttempt", 0L);
            Integer level = (Integer) getField(val, "karteikartenLevel", 1);
            ModularQuizStatistics.QuestionStatistics q = new ModularQuizStatistics.QuestionStatistics(title);
            q.totalAttempts = totalAttempts;
            q.correctAttempts = correctAttempts;
            q.consecutiveCorrect = consecutiveCorrect;
            q.consecutiveWrong = consecutiveWrong;
            q.lastAttempt = lastAttempt;
            q.karteikartenLevel = level;
            dst.put(title != null ? title : key, q);
        }
    }

    private void convertThemeStatsMap(Map<String, ?> src,
                                      Map<String, ModularQuizStatistics.ThemeStatistics> dst) throws Exception {
        for (Object val : src.values()) {
            String name = (String) getField(val, "themeName", "");
            Integer totalQuestions = (Integer) getField(val, "totalQuestions", 0);
            Integer totalAttempts = (Integer) getField(val, "totalAttempts", 0);
            Integer correctAttempts = (Integer) getField(val, "correctAttempts", 0);
            Long lastPlayed = (Long) getField(val, "lastPlayed", 0L);
            ModularQuizStatistics.ThemeStatistics t = new ModularQuizStatistics.ThemeStatistics(name);
            t.totalQuestions = totalQuestions;
            t.totalAttempts = totalAttempts;
            t.correctAttempts = correctAttempts;
            t.lastPlayed = lastPlayed;
            dst.put(name, t);
        }
    }

    private void convertResults(List<?> src, List<ModularQuizPlay.QuizResult> dst) {
        for (Object o : src) {
            try {
                String theme = (String) getField(o, "theme", "");
                String qTitle = (String) getField(o, "questionTitle", "");
                String userAnswer = (String) getField(o, "userAnswer", "");
                String correctAnswer = (String) getField(o, "correctAnswer", "");
                Boolean isCorrect = (Boolean) getField(o, "isCorrect", Boolean.FALSE);
                Long answerTimeMs = (Long) getField(o, "answerTimeMs", 0L);
                // ModularQuizPlay.QuizResult has timestamp in constructor (now). Preserve original if available
                Long ts = (Long) getField(o, "timestamp", System.currentTimeMillis());
                ModularQuizPlay.QuizResult r = new ModularQuizPlay.QuizResult(theme, qTitle, userAnswer, correctAnswer, isCorrect, answerTimeMs);
                // Overwrite timestamp via reflection to preserve ordering
                java.lang.reflect.Field tf = r.getClass().getDeclaredField("timestamp");
                tf.setAccessible(true);
                tf.set(r, ts);
                dst.add(r);
            } catch (Exception ignored) {}
        }
    }

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
