package guimodule;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Represents a single adaptive Leitner flashcard for spaced repetition learning.
 * 
 * <p>This class implements an enhanced 6-box Leitner algorithm with adaptive difficulty:
 * <ul>
 *   <li>Tracks performance metrics such as consecutive correct/incorrect answers.</li>
 *   <li>Calculates next review date based on performance, difficulty, and randomness.</li>
 *   <li>Supports intelligent promotion and demotion across Leitner boxes.</li>
 *   <li>Tracks average response time and adjusts difficulty dynamically.</li>
 *   <li>Integrates with modular quiz system via questionId and theme.</li>
 * </ul>
 * 
 * <p>Serialization: This class implements {@link Serializable} to persist Leitner cards
 * between application sessions.
 * 
 * Author: D.Georgiou
 * Version: 1.0
 */
public class AdaptiveLeitnerCard implements Serializable {

    private static final long serialVersionUID = 1L;

    // ================== ENUMS ==================
    
    /**
     * Defines card difficulty levels.
     * Each level has a display name and a time factor used for interval calculation.
     */
    public enum Difficulty { 
        EASY("Einfach", 0.3), 
        MEDIUM("Mittel", 1.0), 
        HARD("Schwer", 2.0),
        VERY_HARD("Sehr schwer", 3.0);
        
        public final String displayName;
        public final double timeFactor;
        
        Difficulty(String displayName, double timeFactor) {
            this.displayName = displayName;
            this.timeFactor = timeFactor;
        }
    }

    // ================== FIELDS ==================
    
    // --- Identification ---
    private final String questionId;          // Unique ID for the question
    private final String theme;               // Theme/topic of the question
    private final String questionTitle;       // Title of the question

    // --- Leitner system state ---
    private int box;                          // Current Leitner box (1-6)
    private Difficulty difficulty;            // Current difficulty level

    // --- Performance tracking ---
    private int consecutiveCorrect;           // Consecutive correct answers
    private int consecutiveWrong;             // Consecutive incorrect answers
    private int totalAttempts;                // Total attempts
    private int totalCorrect;                 // Total correct answers
    private double averageResponseTime;       // Average response time in seconds

    // --- Timing ---
    private LocalDateTime lastReviewed;       // Last time this card was reviewed
    private LocalDate nextReviewDate;         // Date when the card is due next
    private final LocalDateTime createdAt;    // Creation timestamp

    // --- Constants for interval calculation ---
    private static final int[] BASE_INTERVALS = {1, 3, 7, 16, 35, 80};
    private static final double[] DIFFICULTY_FACTORS = {2.0, 1.0, 0.6, 0.3};

    // ================== CONSTRUCTORS ==================

    /**
     * Full constructor for database or deserialization purposes.
     */
    public AdaptiveLeitnerCard(
            String questionId,
            String theme,
            String questionTitle,
            int box,
            Difficulty difficulty,
            int consecutiveCorrect,
            int consecutiveWrong,
            int totalAttempts,
            int totalCorrect,
            double averageResponseTime,
            LocalDateTime lastReviewed,
            LocalDate nextReviewDate
    ) {
        this.questionId = questionId;
        this.theme = theme;
        this.questionTitle = questionTitle;
        this.box = box;
        this.difficulty = difficulty != null ? difficulty : Difficulty.MEDIUM;
        this.consecutiveCorrect = consecutiveCorrect;
        this.consecutiveWrong = consecutiveWrong;
        this.totalAttempts = totalAttempts;
        this.totalCorrect = totalCorrect;
        this.averageResponseTime = averageResponseTime;
        this.lastReviewed = lastReviewed;
        this.nextReviewDate = nextReviewDate != null ? nextReviewDate : LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Simplified constructor for new cards starting at box 1.
     */
    public AdaptiveLeitnerCard(String questionId, String theme, String questionTitle) {
        this(questionId, theme, questionTitle, 1, Difficulty.MEDIUM, 0, 0, 0, 0, 0.0, null, LocalDate.now());
    }

    // ================== METHODS ==================

    /**
     * Processes a learning result for the card.
     * Updates performance statistics, adjusts difficulty, and calculates next review date.
     * 
     * @param correct whether the answer was correct
     * @param responseTimeSeconds response time in seconds
     */
    public void processResult(boolean correct, double responseTimeSeconds) {
        totalAttempts++;
        lastReviewed = LocalDateTime.now();
        updateAverageResponseTime(responseTimeSeconds);

        if (correct) {
            totalCorrect++;
            consecutiveCorrect++;
            consecutiveWrong = 0;

            if (shouldPromoteToNextLevel(responseTimeSeconds) && box < 6) {
                box++;
                consecutiveCorrect = 0;
                adjustDifficultyAfterPromotion(responseTimeSeconds);
            }

        } else {
            consecutiveWrong++;
            consecutiveCorrect = 0;
            handleIncorrectAnswer();
        }

        updateNextReviewDate();
    }

    private boolean shouldPromoteToNextLevel(double responseTime) {
        int requiredCorrect = Math.max(2, Math.min(4, box));
        if (consecutiveCorrect < requiredCorrect) return false;

        if (box >= 4) {
            double expectedTime = difficulty.timeFactor * 10;
            double successRate = (double) totalCorrect / totalAttempts;
            if (responseTime > expectedTime * 2 || successRate < 0.7) return false;
        }

        return true;
    }

    private void adjustDifficultyAfterPromotion(double responseTime) {
        double expectedTime = difficulty.timeFactor * 10;
        if (responseTime < expectedTime * 0.5 && difficulty != Difficulty.EASY) {
            difficulty = Difficulty.values()[Math.max(0, difficulty.ordinal() - 1)];
        } else if (responseTime > expectedTime * 1.5 && difficulty != Difficulty.VERY_HARD) {
            difficulty = Difficulty.values()[Math.min(3, difficulty.ordinal() + 1)];
        }
    }

    private void handleIncorrectAnswer() {
        if (consecutiveWrong == 1) {
            box = Math.max(1, box > 3 ? box - 2 : box - 1);
            if (difficulty == Difficulty.EASY) difficulty = Difficulty.MEDIUM;
        } else if (consecutiveWrong >= 2) {
            box = 1;
            difficulty = Difficulty.HARD;
            consecutiveWrong = 0;
        }
    }

    private void updateAverageResponseTime(double responseTime) {
        if (totalAttempts == 1) averageResponseTime = responseTime;
        else averageResponseTime = 0.3 * responseTime + 0.7 * averageResponseTime;
    }

    private void updateNextReviewDate() {
        int baseInterval = BASE_INTERVALS[box - 1];
        double difficultyFactor = DIFFICULTY_FACTORS[difficulty.ordinal()];
        double performanceFactor = calculatePerformanceFactor();
        double noiseFactor = 0.85 + new Random().nextDouble() * 0.3;
        int intervalDays = Math.max(1, (int)Math.round(baseInterval * difficultyFactor * performanceFactor * noiseFactor));
        nextReviewDate = LocalDate.now().plusDays(intervalDays);
    }

    private double calculatePerformanceFactor() {
        if (totalAttempts < 3) return 1.0;
        double successRate = (double) totalCorrect / totalAttempts;
        if (successRate >= 0.9) return 1.3;
        else if (successRate >= 0.7) return 1.0;
        else if (successRate >= 0.5) return 0.8;
        else return 0.6;
    }

    /** Returns true if the card is due for review. */
    public boolean isDue() {
        return !LocalDate.now().isBefore(nextReviewDate);
    }

    /** Returns a priority score for sorting cards. */
    public double getPriority() {
        double basePriority = 7 - box;
        long daysOverdue = LocalDate.now().toEpochDay() - nextReviewDate.toEpochDay();
        if (daysOverdue > 0) basePriority += daysOverdue * 0.5;
        basePriority += difficulty.ordinal() * 0.3;
        return basePriority;
    }

    // ================== GETTERS ==================

    public String getQuestionId() { return questionId; }
    public String getTheme() { return theme; }
    public String getQuestionTitle() { return questionTitle; }
    public int getBox() { return box; }
    public int getLevel() { return box; }
    public Difficulty getDifficulty() { return difficulty; }
    public LocalDate getNextReviewDate() { return nextReviewDate; }
    public LocalDateTime getLastReviewed() { return lastReviewed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getConsecutiveCorrect() { return consecutiveCorrect; }
    public int getConsecutiveWrong() { return consecutiveWrong; }
    public int getTotalAttempts() { return totalAttempts; }
    public int getTotalCorrect() { return totalCorrect; }
    public double getAverageResponseTime() { return averageResponseTime; }
    public double getSuccessRate() { return totalAttempts > 0 ? (double) totalCorrect / totalAttempts : 0.0; }

    /** Returns a color code for UI status visualization. */
    public String getStatusColor() {
        switch (box) {
            case 1: return "#FF4444";
            case 2: return "#FF8800";
            case 3: return "#FFBB00";
            case 4: return "#DDDD00";
            case 5: return "#88DD00";
            case 6: return "#44AA44";
            default: return "#888888";
        }
    }

    @Override
    public String toString() {
        return String.format("LeitnerCard{id='%s', theme='%s', level=%d, difficulty=%s, success=%.1f%%, nextReview=%s}", 
            questionId, theme, box, difficulty.displayName, getSuccessRate() * 100, nextReviewDate);
    }
}