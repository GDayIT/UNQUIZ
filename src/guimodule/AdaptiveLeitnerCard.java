package guimodule;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Verbesserte Adaptive Hybrid-Leitner Algorithmus mit 6 Kästen.
 * 
 * Verbesserungen gegenüber ChatGPT Version:
 * - Bessere Schwierigkeitsanpassung
 * - Intelligentere Rückstufung
 * - Performance-Tracking
 * - Zeitbasierte Anpassungen
 * - Bessere Integration mit Quiz-System
 */
public class AdaptiveLeitnerCard implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private final String questionId;
    private final String theme;
    private final String questionTitle;

    // Leitner System State
    private int box; // 1 = Level 1 (rot), 6 = Level 6 (grün)
    private Difficulty difficulty;

    // Performance Tracking
    private int consecutiveCorrect;
    private int consecutiveWrong;
    private int totalAttempts;
    private int totalCorrect;
    private double averageResponseTime; // in Sekunden
    
    // Timing
    private LocalDateTime lastReviewed;
    private LocalDate nextReviewDate;
    private LocalDateTime createdAt;

    // Baseline Intervalle pro Level (in Tagen) - Verbessert
    private static final int[] BASE_INTERVALS = {1, 3, 7, 16, 35, 80};
    
    // Schwierigkeitsfaktoren für Intervalle
    private static final double[] DIFFICULTY_FACTORS = {2.0, 1.0, 0.6, 0.3}; // EASY, MEDIUM, HARD, VERY_HARD

    public AdaptiveLeitnerCard(String questionId, String theme, String questionTitle) {
        this.questionId = questionId;
        this.theme = theme;
        this.questionTitle = questionTitle;
        this.box = 1; // Start in Level 1 (rot) - realistischer
        this.difficulty = Difficulty.MEDIUM;
        this.consecutiveCorrect = 0;
        this.consecutiveWrong = 0;
        this.totalAttempts = 0;
        this.totalCorrect = 0;
        this.averageResponseTime = 0.0;
        this.createdAt = LocalDateTime.now();
        this.lastReviewed = null;
        this.nextReviewDate = LocalDate.now(); // Sofort verfügbar
    }

    /**
     * Verarbeitet ein Lernergebnis mit Antwortzeit.
     * VERBESSERT: Berücksichtigt Antwortzeit für Schwierigkeitsanpassung
     */
    public void processResult(boolean correct, double responseTimeSeconds) {
        totalAttempts++;
        lastReviewed = LocalDateTime.now();
        
        // Update average response time
        updateAverageResponseTime(responseTimeSeconds);
        
        if (correct) {
            totalCorrect++;
            consecutiveCorrect++;
            consecutiveWrong = 0;

            // VERBESSERT: Intelligentere Aufstiegskriterien
            boolean shouldPromote = shouldPromoteToNextLevel(responseTimeSeconds);
            
            if (shouldPromote && box < 6) {
                box++;
                consecutiveCorrect = 0; // Reset für nächste Stufe
                
                // Schwierigkeit anpassen basierend auf Performance
                adjustDifficultyAfterPromotion(responseTimeSeconds);
            }

        } else {
            consecutiveWrong++;
            consecutiveCorrect = 0;

            // VERBESSERT: Intelligentere Rückstufung
            handleIncorrectAnswer();
        }

        // Neues Intervall berechnen
        updateNextReviewDate();
    }
    
    /**
     * VERBESSERT: Intelligentere Aufstiegskriterien
     */
    private boolean shouldPromoteToNextLevel(double responseTime) {
        // Basis: 2-3 richtige Antworten je nach Level
        int requiredCorrect = Math.max(2, Math.min(4, box));
        
        if (consecutiveCorrect < requiredCorrect) {
            return false;
        }
        
        // Zusätzliche Kriterien für höhere Level
        if (box >= 4) {
            // Höhere Level: Antwortzeit muss angemessen sein
            double expectedTime = difficulty.timeFactor * 10; // Basis: 10 Sekunden
            if (responseTime > expectedTime * 2) {
                return false; // Zu langsam für Aufstieg
            }
            
            // Erfolgsrate muss hoch genug sein
            double successRate = (double) totalCorrect / totalAttempts;
            if (successRate < 0.7) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * VERBESSERT: Schwierigkeitsanpassung nach Aufstieg
     */
    private void adjustDifficultyAfterPromotion(double responseTime) {
        double expectedTime = difficulty.timeFactor * 10;
        
        if (responseTime < expectedTime * 0.5 && difficulty != Difficulty.EASY) {
            // Sehr schnell → einfacher machen
            difficulty = Difficulty.values()[Math.max(0, difficulty.ordinal() - 1)];
        } else if (responseTime > expectedTime * 1.5 && difficulty != Difficulty.VERY_HARD) {
            // Zu langsam → schwerer machen
            difficulty = Difficulty.values()[Math.min(3, difficulty.ordinal() + 1)];
        }
    }
    
    /**
     * VERBESSERT: Intelligentere Rückstufung
     */
    private void handleIncorrectAnswer() {
        if (consecutiveWrong == 1) {
            // Erste falsche Antwort: sanfte Rückstufung
            if (box > 3) {
                box = Math.max(1, box - 2); // Höhere Level: stärkere Rückstufung
            } else {
                box = Math.max(1, box - 1); // Niedrige Level: sanfte Rückstufung
            }
            
            // Schwierigkeit leicht erhöhen
            if (difficulty == Difficulty.EASY) {
                difficulty = Difficulty.MEDIUM;
            }
            
        } else if (consecutiveWrong >= 2) {
            // Mehrere falsche Antworten: zurück zu Level 1
            box = 1;
            difficulty = Difficulty.HARD;
            consecutiveWrong = 0; // Reset
        }
    }
    
    /**
     * VERBESSERT: Durchschnittliche Antwortzeit aktualisieren
     */
    private void updateAverageResponseTime(double responseTime) {
        if (totalAttempts == 1) {
            averageResponseTime = responseTime;
        } else {
            // Exponential moving average für bessere Anpassung
            double alpha = 0.3; // Gewichtung neuer Werte
            averageResponseTime = alpha * responseTime + (1 - alpha) * averageResponseTime;
        }
    }

    /**
     * VERBESSERT: Intelligentere Intervallberechnung
     */
    private void updateNextReviewDate() {
        int baseInterval = BASE_INTERVALS[box - 1];
        
        // Schwierigkeitsfaktor anwenden
        double difficultyFactor = DIFFICULTY_FACTORS[difficulty.ordinal()];
        
        // Performance-basierte Anpassung
        double performanceFactor = calculatePerformanceFactor();
        
        // Zufallsvariation (±15%) für "desirable difficulty"
        Random random = new Random();
        double noiseFactor = 0.85 + random.nextDouble() * 0.3;
        
        // Finale Berechnung
        double finalInterval = baseInterval * difficultyFactor * performanceFactor * noiseFactor;
        int intervalDays = Math.max(1, (int) Math.round(finalInterval));
        
        nextReviewDate = LocalDate.now().plusDays(intervalDays);
    }
    
    /**
     * VERBESSERT: Performance-basierte Anpassung
     */
    private double calculatePerformanceFactor() {
        if (totalAttempts < 3) {
            return 1.0; // Nicht genug Daten
        }
        
        double successRate = (double) totalCorrect / totalAttempts;
        
        // Erfolgsrate-basierte Anpassung
        if (successRate >= 0.9) {
            return 1.3; // Sehr gut → längere Intervalle
        } else if (successRate >= 0.7) {
            return 1.0; // Normal
        } else if (successRate >= 0.5) {
            return 0.8; // Schlecht → kürzere Intervalle
        } else {
            return 0.6; // Sehr schlecht → viel kürzere Intervalle
        }
    }

    /**
     * Prüft ob die Karte zur Wiederholung fällig ist
     */
    public boolean isDue() {
        return !LocalDate.now().isBefore(nextReviewDate);
    }
    
    /**
     * Berechnet Priorität für Sortierung (höher = wichtiger)
     */
    public double getPriority() {
        double basePriority = 7 - box; // Level 1 = 6, Level 6 = 1
        
        // Überfällige Karten haben höhere Priorität
        long daysOverdue = LocalDate.now().toEpochDay() - nextReviewDate.toEpochDay();
        if (daysOverdue > 0) {
            basePriority += daysOverdue * 0.5;
        }
        
        // Schwierige Karten haben höhere Priorität
        basePriority += difficulty.ordinal() * 0.3;
        
        return basePriority;
    }

    // ================ GETTERS ================
    
    public String getQuestionId() { return questionId; }
    public String getTheme() { return theme; }
    public String getQuestionTitle() { return questionTitle; }
    public int getBox() { return box; }
    public int getLevel() { return box; } // Alias für bessere Verständlichkeit
    public Difficulty getDifficulty() { return difficulty; }
    public LocalDate getNextReviewDate() { return nextReviewDate; }
    public LocalDateTime getLastReviewed() { return lastReviewed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public int getConsecutiveCorrect() { return consecutiveCorrect; }
    public int getConsecutiveWrong() { return consecutiveWrong; }
    public int getTotalAttempts() { return totalAttempts; }
    public int getTotalCorrect() { return totalCorrect; }
    public double getAverageResponseTime() { return averageResponseTime; }
    
    public double getSuccessRate() {
        return totalAttempts > 0 ? (double) totalCorrect / totalAttempts : 0.0;
    }
    
    public String getStatusColor() {
        switch (box) {
            case 1: return "#FF4444"; // Rot
            case 2: return "#FF8800"; // Orange
            case 3: return "#FFBB00"; // Gelb-Orange
            case 4: return "#DDDD00"; // Gelb
            case 5: return "#88DD00"; // Hellgrün
            case 6: return "#44AA44"; // Grün
            default: return "#888888"; // Grau
        }
    }

    @Override
    public String toString() {
        return String.format("LeitnerCard{id='%s', theme='%s', level=%d, difficulty=%s, success=%.1f%%, nextReview=%s}", 
            questionId, theme, box, difficulty.displayName, getSuccessRate() * 100, nextReviewDate);
    }
}
