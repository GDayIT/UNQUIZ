package guimodule;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * QuizQuestion represents a single quiz question with multiple answer options.
 * <p>
 * This class is intended to be used as a **model object** for GUI forms, quizzes,
 * statistics calculations, and session management.
 * <p>
 * Features include:
 * <ul>
 *     <li>Title and question text</li>
 *     <li>Multiple possible answers</li>
 *     <li>Correctness flags for each answer</li>
 *     <li>Optional explanation text</li>
 *     <li>Thema (topic/category) association for quizzes and Leitner cards</li>
 *     <li>Creation timestamp to track when the question was added</li>
 * </ul>
 * <p>
 * The class supports **safe immutability-like behavior** by returning copies of lists
 * to prevent external modification.
 * <p>
 * Can be integrated with:
 * <ul>
 *     <li>{@link QuizFormData} for form data transfer</li>
 *     <li>{@link QuizDataMapper} for conversions to/from repository objects</li>
 *     <li>Themes, Questions, Leitner Cards, and Quiz Sessions for dynamic quizzes</li>
 * </ul>
 * 
 * @author D.
 * Georgiou
 * @version 1.0
 */
public class QuizQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The title of the quiz question.
     */
    private String titel;

    /**
     * The main text/content of the question.
     */
    private String frageText;

    /**
     * Optional explanation or hint for the question.
     */
    private String erklaerung;

    /**
     * List of possible answer strings.
     */
    private List<String> antworten = new ArrayList<>();

    /**
     * List of booleans indicating whether each answer is correct.
     */
    private List<Boolean> korrekt = new ArrayList<>();

    /**
     * Topic or theme associated with this question.
     * Used for filtering, statistics, and Leitner card management.
     */
    private String thema;

    /**
     * Timestamp when this question was created.
     */
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Default no-argument constructor.
     */
    public QuizQuestion() {}

    /**
     * Constructs a new QuizQuestion with specified values.
     * Null-safe: replaces null strings with empty strings and initializes lists safely.
     *
     * @param titel the title of the question
     * @param frageText the question text
     * @param antworten array of possible answers
     * @param korrekt array of correctness flags corresponding to answers
     */
    public QuizQuestion(String titel, String frageText, String[] antworten, boolean[] korrekt) {
        this.titel = titel != null ? titel : "";
        this.frageText = frageText != null ? frageText : "";
        if (antworten != null) this.antworten = new ArrayList<>(Arrays.asList(antworten));
        if (korrekt != null) {
            this.korrekt = new ArrayList<>(korrekt.length);
            for (boolean b : korrekt) this.korrekt.add(b);
        }
    }

    // --- Getters and setters with safe list copying ---

    public String getTitel() { return titel; }
    public void setTitel(String titel) { this.titel = titel; }

    public String getFrageText() { return frageText; }
    public void setFrageText(String frageText) { this.frageText = frageText; }

    public String getErklaerung() { return erklaerung; }
    public void setErklaerung(String erklaerung) { this.erklaerung = erklaerung; }

    public List<String> getAntworten() { return new ArrayList<>(antworten); }
    public void setAntworten(List<String> antworten) { this.antworten = new ArrayList<>(antworten); }

    public List<Boolean> getKorrekt() { return new ArrayList<>(korrekt); }
    public void setKorrekt(List<Boolean> korrekt) { this.korrekt = new ArrayList<>(korrekt); }

    public String getThema() { return thema; }
    public void setThema(String thema) { this.thema = thema; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}