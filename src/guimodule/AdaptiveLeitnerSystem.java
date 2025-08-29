package guimodule;

import dbbl.BusinesslogicaDelegation;
import dbbl.RepoQuizeeQuestions;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptive Leitner System Manager.
 *
 * Manages learning progress using an extended Leitner box system (Spaced Repetition)
 * and integrates with the modular quiz application. The system persistently stores
 * its state (cards, levels, due status, statistics) in a separate file, independent
 * from the actual question repository.
 *
 * Key aspects:
 * - Persistent state of the Leitner system (no test/sample data)
 * - Delegation to business logic to fetch questions/themes
 * - Provides due questions per topic or globally
 * - Statistics per level and topic
 *
 * Serialization:
 * - The delegate (BusinesslogicaDelegation) is not serialized (transient)
 * - When saving, the timestamp lastSystemUpdate is updated
 * - When loading, only internal data (cards, statistics) is restored; runtime dependencies remain unchanged
 * 
 * @author D.Georgiou
 * @version 1.0
 * 
 */
public class AdaptiveLeitnerSystem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final String LEITNER_DATA_FILE = "leitner_system.dat";
    
    // Card management
    private final Map<String, AdaptiveLeitnerCard> cards = new ConcurrentHashMap<>();
    private final transient BusinesslogicaDelegation delegate;
    
    // System statistics
    private int totalReviews = 0;
    private LocalDate lastSystemUpdate = LocalDate.now();
    
    /**
     * Serializable snapshot that contains only the minimal state required for
     * persistence. Prevents accidental serialization of runtime-only dependencies
     * and stabilizes the file format across versions.
     */
    private static class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<String, AdaptiveLeitnerCard> cards;
        int totalReviews;
        LocalDate lastSystemUpdate;
    }
    
    public AdaptiveLeitnerSystem(BusinesslogicaDelegation delegate) {
        this.delegate = delegate;
        loadSystem();
        // Note: No automatic initialization of new cards.
        // Cards are only created via real quiz results (processQuizResult).
    }

    /**
     * Merge policy for combining incoming Leitner cards with existing state.
     */
    public enum MergePolicy {
        /** Keep existing cards when conflict occurs. */
        PREFER_EXISTING,
        /** Replace existing cards with incoming. */
        PREFER_INCOMING,
        /** Choose the card with the higher Leitner level. */
        PREFER_HIGHER_LEVEL,
        /** Choose the card with the newer lastReviewed timestamp. */
        PREFER_NEWER
    }

    /**
     * Merges incoming cards with the current Leitner state according to the given policy.
     * Incoming map is identified by questionId (theme:title). Persisted afterwards.
     *
     * @param incoming map of cards to merge
     * @param policy merge policy
     */
    public void mergeCards(Map<String, AdaptiveLeitnerCard> incoming, MergePolicy policy) {
        if (incoming == null || incoming.isEmpty()) return;
        for (Map.Entry<String, AdaptiveLeitnerCard> e : incoming.entrySet()) {
            String id = e.getKey();
            AdaptiveLeitnerCard inc = e.getValue();
            AdaptiveLeitnerCard cur = this.cards.get(id);
            if (cur == null) {
                this.cards.put(id, inc);
                continue;
            }
            AdaptiveLeitnerCard chosen = cur;
            switch (policy) {
                case PREFER_EXISTING: {
                    chosen = cur; break;
                }
                case PREFER_INCOMING: {
                    chosen = inc; break;
                }
                case PREFER_HIGHER_LEVEL: {
                    int lCur = cur.getLevel();
                    int lInc = inc.getLevel();
                    if (lInc > lCur) chosen = inc; else if (lInc < lCur) chosen = cur; else {
                        // tie-breaker by lastReviewed
                        chosen = pickNewer(cur, inc);
                    }
                    break;
                }
                case PREFER_NEWER: {
                    chosen = pickNewer(cur, inc);
                    break;
                }
                default: {
                    chosen = cur; break;
                }
            }
            this.cards.put(id, chosen);
        }
        saveSystem();
    }

    private AdaptiveLeitnerCard pickNewer(AdaptiveLeitnerCard a, AdaptiveLeitnerCard b) {
        java.time.LocalDateTime ra = a.getLastReviewed();
        java.time.LocalDateTime rb = b.getLastReviewed();
        if (ra == null && rb == null) return a;
        if (ra == null) return b;
        if (rb == null) return a;
        return rb.isAfter(ra) ? b : a;
    }
    
    /**
     * Initializes new questions that do not yet have Leitner cards
     */
    private void initializeNewQuestions() {
        if (delegate == null) return;

        try {
            List<String> themes = delegate.getAllTopics();

            for (String theme : themes) {
                List<String> questionTitles = delegate.getQuestionTitles(theme);

                for (int i = 0; i < questionTitles.size(); i++) {
                    RepoQuizeeQuestions question = delegate.getQuestion(theme, i);
                    if (question != null) {
                        String questionId = generateQuestionId(question);

                        if (!cards.containsKey(questionId)) {
                            AdaptiveLeitnerCard card = new AdaptiveLeitnerCard(
                                questionId,
                                question.getThema(),
                                question.getTitel()
                            );
                            cards.put(questionId, card);
                        }
                    }
                }
            }

            saveSystem();
        } catch (Exception e) {
            System.err.println("Error initializing Leitner cards: " + e.getMessage());
        }
    }
    
    /**
     * Processes a quiz result and updates the corresponding card.
     * Creates a new card if necessary.
     *
     * Thread safety: This method is not synchronized. External synchronization
     * is recommended if called from multiple threads.
     *
     * Persistence: Saves the system state after processing the result.
     *
     * @param result QuizResult object (theme, question title, correctness, answer time)
     */
    public void processQuizResult(ModularQuizPlay.QuizResult result) {
        String questionId = generateQuestionId(result.theme, result.questionTitle);
        
        AdaptiveLeitnerCard card = cards.get(questionId);
        if (card == null) {
            // Create new card if missing
            card = new AdaptiveLeitnerCard(questionId, result.theme, result.questionTitle);
            cards.put(questionId, card);
        }
        
        // Process result
        card.processResult(result.isCorrect, result.getAnswerTimeSeconds());
        totalReviews++;
        
        saveSystem();
    }
    
    /**
     * Returns due questions for a specific topic.
     *
     * Retrieves all due questions for a given topic, sorted descending by priority
     * (most important first).
     *
     * @param theme The topic to filter questions
     * @return List of due questions for the topic (empty if none)
     */
    public List<RepoQuizeeQuestions> getDueQuestions(String theme) {
        if (delegate == null) return new ArrayList<>();

        try {
            List<String> questionTitles = delegate.getQuestionTitles(theme);
            List<RepoQuizeeQuestions> dueQuestions = new ArrayList<>();

            for (int i = 0; i < questionTitles.size(); i++) {
                RepoQuizeeQuestions question = delegate.getQuestion(theme, i);
                if (question != null) {
                    String questionId = generateQuestionId(question);
                    AdaptiveLeitnerCard card = cards.get(questionId);

                    if (card != null && card.isDue()) {
                        dueQuestions.add(question);
                    }
                }
            }

            // Sort by priority (most important first)
            dueQuestions.sort((q1, q2) -> {
                String id1 = generateQuestionId(q1);
                String id2 = generateQuestionId(q2);
                AdaptiveLeitnerCard card1 = cards.get(id1);
                AdaptiveLeitnerCard card2 = cards.get(id2);

                if (card1 == null || card2 == null) return 0;
                return Double.compare(card2.getPriority(), card1.getPriority());
            });

            return dueQuestions;

        } catch (Exception e) {
            System.err.println("Error getting due questions: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Returns all due questions (across all topics).
     *
     * Retrieves all due questions across all topics, sorted descending by priority
     * (most important first).
     *
     * @return List of all due questions (empty if none)
     */
    public List<RepoQuizeeQuestions> getAllDueQuestions() {
        if (delegate == null) return new ArrayList<>();

        try {
            List<RepoQuizeeQuestions> allDueQuestions = new ArrayList<>();
            List<String> themes = delegate.getAllTopics();

            for (String theme : themes) {
                List<RepoQuizeeQuestions> themeDueQuestions = getDueQuestions(theme);
                allDueQuestions.addAll(themeDueQuestions);
            }

            // Sort by priority (most important first)
            allDueQuestions.sort((q1, q2) -> {
                String id1 = generateQuestionId(q1);
                String id2 = generateQuestionId(q2);
                AdaptiveLeitnerCard card1 = cards.get(id1);
                AdaptiveLeitnerCard card2 = cards.get(id2);

                if (card1 == null || card2 == null) return 0;
                return Double.compare(card2.getPriority(), card1.getPriority());
            });

            return allDueQuestions;

        } catch (Exception e) {
            System.err.println("Error getting all due questions: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Returns statistics for a specific topic.
     *
     * Provides a distribution of cards by Leitner levels for a given topic.
     * If null or "All Topics" is passed, all cards are considered.
     *
     * @param theme Topic or null/"All Topics" for global view
     * @return Map of level -> list of cards
     */
    public Map<Integer, List<AdaptiveLeitnerCard>> getThemeStatistics(String theme) {
        Map<Integer, List<AdaptiveLeitnerCard>> levelStats = new HashMap<>();
        
        // Initialize all levels
        for (int level = 1; level <= 6; level++) {
            levelStats.put(level, new ArrayList<>());
        }
        
        // Filter cards by topic
        cards.values().stream()
            .filter(card -> theme == null || "All Topics".equals(theme) || theme.equals(card.getTheme()))
            .forEach(card -> levelStats.get(card.getLevel()).add(card));
        
        return levelStats;
    }
    
    /**
     * Returns overall statistics (all topics).
     *
     * @return Map of level -> list of cards
     */
    public Map<Integer, List<AdaptiveLeitnerCard>> getAllStatistics() {
        return getThemeStatistics(null);
    }
    
    /**
     * Returns count of due cards per level.
     *
     * Counts the number of due cards per level for the specified topic, or globally
     * if null/"All Topics" is passed.
     *
     * @param theme Topic or null/"All Topics"
     * @return Map of level -> due card count
     */
    public Map<Integer, Integer> getDueCountByLevel(String theme) {
        Map<Integer, Integer> dueCounts = new HashMap<>();
        
        for (int level = 1; level <= 6; level++) {
            dueCounts.put(level, 0);
        }
        
        cards.values().stream()
            .filter(card -> theme == null || "All Topics".equals(theme) || theme.equals(card.getTheme()))
            .filter(AdaptiveLeitnerCard::isDue)
            .forEach(card -> dueCounts.merge(card.getLevel(), 1, Integer::sum));
        
        return dueCounts;
    }
    
    /**
     * Returns the card for a specific question.
     *
     * @param theme Topic
     * @param questionTitle Question title
     * @return Card or null if none exists
     */
    public AdaptiveLeitnerCard getCard(String theme, String questionTitle) {
        String questionId = generateQuestionId(theme, questionTitle);
        return cards.get(questionId);
    }
    
    /**
     * Returns all cards for a topic.
     *
     * @param theme Topic or null/"All Topics"
     * @return Sorted list of cards (by question title)
     */
    public List<AdaptiveLeitnerCard> getCardsForTheme(String theme) {
        return cards.values().stream()
            .filter(card -> theme == null || "All Topics".equals(theme) || theme.equals(card.getTheme()))
            .sorted((a, b) -> a.getQuestionTitle().compareTo(b.getQuestionTitle()))
            .collect(Collectors.toList());
    }
    
    /**
     * Generates a unique ID for a question.
     *
     * @param question Question object
     * @return ID in the form "<theme>:<title>"
     */
    private String generateQuestionId(RepoQuizeeQuestions question) {
        return generateQuestionId(question.getThema(), question.getTitel());
    }
    
    /**
     * Generates a unique ID for a question based on theme and title.
     *
     * @param theme Topic
     * @param title Question title
     * @return ID in the form "<theme>:<title>"
     */
    private String generateQuestionId(String theme, String title) {
        return theme + ":" + title;
    }
    
    /**
     * Saves the Leitner system.
     *
     * Stores the current state in a file. Updates lastSystemUpdate before saving.
     */
    private void saveSystem() {
        this.lastSystemUpdate = LocalDate.now();
        File target = new File(LEITNER_DATA_FILE);
        File tmp = new File(LEITNER_DATA_FILE + ".tmp");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmp))) {
            Snapshot snapshot = new Snapshot();
            snapshot.cards = new HashMap<>(this.cards);
            snapshot.totalReviews = this.totalReviews;
            snapshot.lastSystemUpdate = this.lastSystemUpdate;
            oos.writeObject(snapshot);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Error saving Leitner system: " + e.getMessage());
            if (tmp.exists()) tmp.delete();
            return;
        }
        // Atomic replace
        if (target.exists() && !target.delete()) {
            System.err.println("Error saving Leitner system: unable to replace existing file");
            tmp.delete();
            return;
        }
        if (!tmp.renameTo(target)) {
            System.err.println("Error saving Leitner system: unable to finalize save");
            tmp.delete();
        }
    }
    
    /**
     * Loads the Leitner system.
     *
     * Loads the state from file if present. If an error occurs, starts with a fresh system.
     */
    private void loadSystem() {
        File file = new File(LEITNER_DATA_FILE);
        if (!file.exists()) return; // New system
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof Snapshot) {
                Snapshot snap = (Snapshot) obj;
                this.cards.clear();
                this.cards.putAll(snap.cards != null ? snap.cards : Collections.emptyMap());
                this.totalReviews = snap.totalReviews;
                this.lastSystemUpdate = snap.lastSystemUpdate != null ? snap.lastSystemUpdate : LocalDate.now();
            } else if (obj instanceof AdaptiveLeitnerSystem) {
                AdaptiveLeitnerSystem loaded = (AdaptiveLeitnerSystem) obj;
                // Legacy fallback: copy state fields only
                this.cards.clear();
                this.cards.putAll(loaded.cards);
                this.totalReviews = loaded.totalReviews;
                this.lastSystemUpdate = loaded.lastSystemUpdate;
            } else {
                // Unrecognized format: start fresh
                throw new IOException("Unrecognized Leitner data format: " + obj.getClass().getName());
            }
        } catch (Exception e) {
            System.err.println("Error loading Leitner system: " + e.getMessage());
            if (file.exists() && !file.delete()) {
                System.err.println("Unable to remove corrupt Leitner data file");
            }
        }
    }
    
    /**
     * Resets the entire system.
     *
     * Clears all cards and statistics, deletes the persistence file, and optionally
     * reinitializes cards for existing questions.
     */
    public void resetSystem() {
        cards.clear();
        totalReviews = 0;
        lastSystemUpdate = LocalDate.now();
        File file = new File(LEITNER_DATA_FILE);
        if (file.exists()) file.delete();
    }
    
    // ================ GETTERS ================

    public int getTotalCards() { return cards.size(); }
    public int getTotalReviews() { return totalReviews; }
    public LocalDate getLastSystemUpdate() { return lastSystemUpdate; }

    public int getDueCardsCount() {
        return (int) cards.values().stream().filter(AdaptiveLeitnerCard::isDue).count();
    }

    public int getDueCardsCount(String theme) {
        return (int) cards.values().stream()
            .filter(card -> theme == null || "All Topics".equals(theme) || theme.equals(card.getTheme()))
            .filter(AdaptiveLeitnerCard::isDue)
            .count();
    }
}