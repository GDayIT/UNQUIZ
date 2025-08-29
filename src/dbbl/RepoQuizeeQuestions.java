package dbbl;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing a single multiple-choice quiz question.
 * 
 * Responsibilities:
 * - Stores question metadata (title, text, theme, explanation)
 * - Maintains parallel lists of answers and correctness flags
 * - Provides creation timestamp for auditing and ordering
 * - Implements Serializable for persistence
 * 
 * Invariants:
 * - {@code antworten} and {@code korrekt} lists always have the same length
 *   (longer list trimmed to shorter list)
 * - Getters return defensive copies
 * - Null values normalized to empty strings or empty lists
 * 
 * Identity:
 * - Equality and hash code are based on {@code thema} and {@code titel}
 * - Allows safe usage in collections and maps
 * 
 * @author D.
 * @version 1.0
 */
public class RepoQuizeeQuestions implements Serializable {

    private static final long serialVersionUID = 1L;

    // ------------------- CORE FIELDS -------------------

    /** Topic or category of the question */
    private String thema;

    /** Question title */
    private String titel;

    /** Full question text */
    private String frageText;

    /** List of answer strings (multiple-choice options) */
    private List<String> antworten;

    /** Corresponding correctness flags for each answer */
    private List<Boolean> korrekt;

    /** Optional explanation for the answer */
    private String erklaerung;

    /** Timestamp for when the question was created */
    private LocalDateTime createdAt;

    // ------------------- CONSTRUCTORS -------------------

    /** Default constructor: initializes empty content and current timestamp */
    public RepoQuizeeQuestions() {
        this("", "", new String[0], new boolean[0], "");
    }

    /** Constructor with title, question text, answers, and correctness flags */
    public RepoQuizeeQuestions(String titel, String frageText, String[] antworten, boolean[] korrekt) {
        this(titel, frageText, antworten, korrekt, "");
    }

    /** Full constructor with explanation */
    public RepoQuizeeQuestions(String titel, String frageText, String[] antworten, boolean[] korrekt, String erklaerung) {
        this.titel = titel != null ? titel : "";
        this.frageText = frageText != null ? frageText : "";
        this.antworten = antworten != null ? new ArrayList<>(Arrays.asList(antworten)) : new ArrayList<>();
        this.korrekt = new ArrayList<>();
        if (korrekt != null) {
            for (boolean b : korrekt) this.korrekt.add(b);
        }
        this.erklaerung = erklaerung != null ? erklaerung : "";
        this.createdAt = LocalDateTime.now();
        this.thema = "";
        alignAnswerFlagSizes();
    }

    // ------------------- INTERNAL UTILITY -------------------

    /**
     * Ensures {@code antworten} and {@code korrekt} are the same length.
     * Trims longer list to match shorter list and initializes non-null empty lists.
     */
    private void alignAnswerFlagSizes() {
        if (antworten == null) antworten = new ArrayList<>();
        if (korrekt == null) korrekt = new ArrayList<>();
        int min = Math.min(antworten.size(), korrekt.size());
        if (antworten.size() != min) antworten = new ArrayList<>(antworten.subList(0, min));
        if (korrekt.size() != min) korrekt = new ArrayList<>(korrekt.subList(0, min));
    }

    // ------------------- GETTERS & SETTERS -------------------

    public String getTitel() { return titel; }
    public void setTitel(String titel) { this.titel = titel; }

    public String getFrageText() { return frageText; }
    public void setFrageText(String frageText) { this.frageText = frageText; }

    public List<String> getAntworten() { return new ArrayList<>(antworten); }
    public void setAntworten(List<String> antworten) {
        this.antworten = antworten != null ? new ArrayList<>(antworten) : new ArrayList<>();
        alignAnswerFlagSizes();
    }

    public List<Boolean> getKorrekt() { return new ArrayList<>(korrekt); }
    public void setKorrekt(List<Boolean> korrekt) {
        this.korrekt = korrekt != null ? new ArrayList<>(korrekt) : new ArrayList<>();
        alignAnswerFlagSizes();
    }

    public String getThema() { return thema; }
    public void setThema(String thema) { this.thema = thema; }

    public String getErklaerung() { return erklaerung; }
    public void setErklaerung(String erklaerung) { this.erklaerung = erklaerung != null ? erklaerung : ""; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // ------------------- OBJECT METHODS -------------------

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
        return Objects.equals(thema, that.thema) && Objects.equals(titel, that.titel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thema, titel);
    }
}