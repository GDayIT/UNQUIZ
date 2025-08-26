package dbbl;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Domain model for a single quiz question with multiple-choice answers.
 *
 * Responsibilities:
 * - Holds question metadata (title, text, theme, explanation)
 * - Manages aligned lists of answers and corresponding correctness flags
 * - Provides a creation timestamp for auditing/ordering scenarios
 * - Serializable for persistence in the modular storage layer
 *
 * Invariants and behavior:
 * - Answers (antworten) and correctness flags (korrekt) are always the same size
 *   by trimming the longer list to the shorter list when necessary.
 * - All collections exposed via getters are defensive copies.
 * - Null values are normalized to empty strings or empty lists where appropriate.
 *
 * Identity:
 * - Equality and hash code are defined by the pair (thema, titel). This allows
 *   safe usage in sets/maps and expresses the natural identity of a question
 *   within its theme.
<<<<<<< HEAD
 *   
 * @author D.Georgiou
 * @version 1.0
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class RepoQuizeeQuestions implements Serializable {
    private static final long serialVersionUID = 1L;

    // Core fields
    private String thema;                 // Topic/category of the question
    private String titel;                 // Title of the question
    private String frageText;             // Question text
    private List<String> antworten;       // Multiple-choice answers
    private List<Boolean> korrekt;        // Correctness flags for answers
    private String erklaerung;            // Explanation for the answer
    private LocalDateTime createdAt;      // Creation timestamp

    /**
     * Default constructor initializes empty content and current timestamp.
     */
    public RepoQuizeeQuestions() {
        this("", "", new String[0], new boolean[0], "");
    }

    /**
     * Constructor with basic question data.
     */
    public RepoQuizeeQuestions(String titel, String frageText, String[] antworten, boolean[] korrekt) {
        this(titel, frageText, antworten, korrekt, "");
    }

    /**
     * Constructor with full question data including explanation.
     */
    public RepoQuizeeQuestions(String titel, String frageText, String[] antworten, boolean[] korrekt, String erklaerung) {
        this.titel = titel != null ? titel : "";
        this.frageText = frageText != null ? frageText : "";
        this.antworten = antworten != null ? new ArrayList<>(Arrays.asList(antworten)) : new ArrayList<>();
        this.korrekt = new ArrayList<>();
        if (korrekt != null) {
            for (boolean b : korrekt) {
                this.korrekt.add(b);
            }
        }
        this.erklaerung = erklaerung != null ? erklaerung : "";
        this.createdAt = LocalDateTime.now();
        this.thema = "";
        alignAnswerFlagSizes();
    }

    /**
     * Ensures the answers and correctness flags lists are aligned to the same size.
     * The longer list will be trimmed to the size of the shorter one. Lists are
     * initialized to non-null empty lists when necessary.
     */
    private void alignAnswerFlagSizes() {
        if (this.antworten == null) this.antworten = new ArrayList<>();
        if (this.korrekt == null) this.korrekt = new ArrayList<>();
        int min = Math.min(this.antworten.size(), this.korrekt.size());
        if (this.antworten.size() != min) {
            this.antworten = new ArrayList<>(this.antworten.subList(0, min));
        }
        if (this.korrekt.size() != min) {
            this.korrekt = new ArrayList<>(this.korrekt.subList(0, min));
        }
    }

    // --- Getter & Setter methods ---
    
    /** @return the title */
    public String getTitel() { 
        return titel; 
    }
    
    /** @param titel the title to set */
    public void setTitel(String titel) { 
        this.titel = titel; 
    }

    /** @return the question text */
    public String getFrageText() { 
        return frageText; 
    }
    
    /** @param frageText the question text to set */
    public void setFrageText(String frageText) { 
        this.frageText = frageText; 
    }

    /** @return defensive copy of answers list */
    public List<String> getAntworten() { 
        return new ArrayList<>(antworten); 
    }
    
    /** @param antworten list of answer strings */
    public void setAntworten(List<String> antworten) {
        this.antworten = antworten != null ? new ArrayList<>(antworten) : new ArrayList<>();
        alignAnswerFlagSizes();
    }

    /** @return defensive copy of correctness flags */
    public List<Boolean> getKorrekt() { 
        return new ArrayList<>(korrekt); 
    }
    
    /** @param korrekt list of boolean flags */
    public void setKorrekt(List<Boolean> korrekt) {
        this.korrekt = korrekt != null ? new ArrayList<>(korrekt) : new ArrayList<>();
        alignAnswerFlagSizes();
    }

    /** @return topic/category */
    public String getThema() { 
        return thema; 
    }
    
    /** @param thema the topic to set */
    public void setThema(String thema) { 
        this.thema = thema; 
    }

    /** @return explanation text */
    public String getErklaerung() {
        return erklaerung;
    }

    /** @param erklaerung the explanation to set */
    public void setErklaerung(String erklaerung) {
        this.erklaerung = erklaerung != null ? erklaerung : "";
    }

    /** @return creation timestamp */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "RepoQuizeeQuestions{" +
                "title='" + titel + '\'' +
                ", topic='" + thema + '\'' +
                ", createdAt=" + createdAt +
                ", questionText='" + frageText + '\'' +
                ", answers=" + antworten +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepoQuizeeQuestions)) return false;
        RepoQuizeeQuestions that = (RepoQuizeeQuestions) o;
        return Objects.equals(thema, that.thema) &&
               Objects.equals(titel, that.titel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thema, titel);
    }
}
