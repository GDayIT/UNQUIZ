package guimodule;

/**
 * QuizFormData is a simple **Data Transfer Object (DTO)** for transferring
 * quiz question data from UI forms without directly binding to business-layer models
 * like {@link dbbl.RepoQuizeeQuestions}.
 * <p>
 * This class encapsulates the **question title, text, answer options, and correctness flags**.
 * It is immutable and designed for safe transfer of form data between GUI forms and
 * mapping utilities like {@link QuizDataMapper}.
 * <p>
 * Usage:
 * <ul>
 *     <li>Populate a form with existing data</li>
 *     <li>Transfer user input to the business layer for persistence</li>
 *     <li>Convert to {@link RepoQuizeeQuestions} via {@link QuizDataMapper}</li>
 * </ul>
 * <p>
 * Fields and arrays are final, ensuring immutability and thread safety.
 * Suitable for use in **Themes, Questions, Leitner Cards, and Quiz Sessions** for form editing.
 * 
 * @author D.
 * Georgiou
 * @version 1.0
 */
public class QuizFormData {

    /**
     * The title of the quiz question.
     */
    private final String titel;

    /**
     * The text/content of the quiz question.
     */
    private final String frage;

    /**
     * Array of possible answers for this question.
     */
    private final String[] antworten;

    /**
     * Array of correctness flags corresponding to each answer.
     * True indicates the answer is correct; false indicates incorrect.
     */
    private final boolean[] korrekt;

    /**
     * Constructs a new QuizFormData instance.
     *
     * @param titel the question title
     * @param frage the question text
     * @param antworten array of possible answers
     * @param korrekt array of correctness flags matching answers
     */
    public QuizFormData(String titel, String frage, String[] antworten, boolean[] korrekt) {
        this.titel = titel;
        this.frage = frage;
        this.antworten = antworten;
        this.korrekt = korrekt;
    }

    /**
     * Gets the title of the question.
     * 
     * @return question title
     */
    public String getTitel() { return titel; }

    /**
     * Gets the text/content of the question.
     * 
     * @return question text
     */
    public String getFrage() { return frage; }

    /**
     * Gets the array of possible answers.
     * 
     * @return array of answers
     */
    public String[] getAntworten() { return antworten; }

    /**
     * Gets the array of correctness flags corresponding to the answers.
     * 
     * @return array of boolean correctness values
     */
    public boolean[] getKorrekt() { return korrekt; }
}