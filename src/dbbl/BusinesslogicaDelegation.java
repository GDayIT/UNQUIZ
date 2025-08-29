/**
 * BusinesslogicaDelegation defines the delegation boundary between GUI and
 * business logic. GUI calls these methods (possibly via lambdas) instead of
 * directly wiring UI components to the controller.
 */
package dbbl;

import java.util.List;

/**
 * The {@code BusinesslogicaDelegation} interface defines the delegation
 * boundary between the Graphical User Interface (GUI) layer and the
 * business logic layer of the application.
 *
 * <p>This interface acts as an abstraction to decouple UI components
 * (e.g., buttons, menus, or dialog windows) from the underlying
 * business logic implementation. Instead of directly wiring UI
 * actions to a controller or persistence mechanism, the GUI interacts
 * only through the methods defined in this interface.
 *
 * <p>By following this design principle, the application gains:
 * <ul>
 *   <li><b>Flexibility:</b> The business logic can evolve independently
 *       of the user interface.</li>
 *   <li><b>Testability:</b> Business logic can be tested in isolation
 *       without UI dependencies.</li>
 *   <li><b>Maintainability:</b> Clear separation of concerns
 *       reduces coupling between layers.</li>
 *   <li><b>Extensibility:</b> New persistence mechanisms or
 *       logic changes can be introduced without altering the GUI code.</li>
 * </ul>
 *
 * <p><b>Connection to Other Components:</b></p>
 * <ul>
 *   <li>The GUI layer <i>depends on</i> this interface to trigger
 *       business operations.</li>
 *   <li>Concrete implementations of this interface
 *       <i>depend on</i> repositories, databases, or services
 *       (e.g., {@link RepoQuizeeQuestions}) to store and manage
 *       data.</li>
 *   <li>Each method maps a GUI-driven action to a business operation,
 *       ensuring user actions are consistently processed.</li>
 * </ul>
 *
 * <p><b>Domain Overview:</b></p>
 * <ul>
 *   <li><b>Theme Operations:</b> Manage topics/themes that
 *       group quiz questions (create, retrieve, delete).</li>
 *   <li><b>Question Operations:</b> Manage quiz questions
 *       under each theme (CRUD functionality with optional
 *       explanations and answers).</li>
 *   <li><b>Persistence Lifecycle:</b> Handle saving all data
 *       in one operation to ensure durability and consistency.</li>
 * </ul>
 *
 * @author  D. Georgiou
 * @version 1.0
 */
public interface BusinesslogicaDelegation {

    // ---------------------------------------------------------------------
    // THEME OPERATIONS
    // ---------------------------------------------------------------------

    /**
     * Retrieves all available topics (themes) within the system.
     *
     * <p>A topic acts as a container or category for related quiz questions.
     * The returned list contains the titles of all currently stored topics.
     *
     * @return a {@link java.util.List} of {@link String} objects representing
     *         all topic titles; never {@code null}, but may be empty
     */
    List<String> getAllTopics();

    /**
     * Persists a new theme (topic) with the given title and description.
     *
     * <p>If a theme with the same title already exists, the behavior is
     * implementation-specific (e.g., overwrite or reject).
     *
     * @param title       the unique title of the theme (must not be {@code null} or empty)
     * @param description a descriptive text explaining the theme
     *                    (may be {@code null} or empty if no description is provided)
     */
    void saveTheme(String title, String description);

    /**
     * Deletes the theme (topic) identified by the given title.
     *
     * <p>Deletion of a theme may also imply the removal of all associated
     * questions, depending on the implementation.
     *
     * @param title the title of the theme to delete (must not be {@code null} or empty)
     */
    void deleteTheme(String title);

    /**
     * Retrieves the description of a theme identified by its title.
     *
     * @param title the title of the theme (must not be {@code null} or empty)
     * @return the description of the theme, or {@code null} if the theme does not exist
     */
    String getThemeDescription(String title);

    // ---------------------------------------------------------------------
    // QUESTION OPERATIONS
    // ---------------------------------------------------------------------

    /**
     * Retrieves all question titles for a specific topic.
     *
     * <p>This is typically used to display a list of available questions
     * within a chosen theme.
     *
     * @param topic the title of the topic containing the questions
     *              (must not be {@code null} or empty)
     * @return a {@link java.util.List} of question titles; never {@code null},
     *         but may be empty if the topic has no questions
     */
    List<String> getQuestionTitles(String topic);

    /**
     * Retrieves a single question from a topic based on its index.
     *
     * <p>The index refers to the position of the question within
     * the topic's ordered question list.
     *
     * @param topic the title of the topic (must not be {@code null} or empty)
     * @param index the index of the question (0-based)
     * @return the {@link RepoQuizeeQuestions} object representing the question,
     *         or {@code null} if no such question exists
     */
    RepoQuizeeQuestions getQuestion(String topic, int index);

    /**
     * Persists a new question within a given topic.
     *
     * <p>Each question has a title, a body text, multiple possible answers,
     * and a set of boolean flags indicating which answers are correct.
     *
     * @param topic   the title of the topic where the question belongs
     * @param title   the title of the question
     * @param text    the main text (body) of the question
     * @param answers a list of possible answers (must not be {@code null})
     * @param correct a parallel list of booleans marking correct answers
     *                (must not be {@code null} and same size as {@code answers})
     */
    void saveQuestion(String topic, String title, String text,
                      List<String> answers, List<Boolean> correct);

    /**
     * Persists a new question within a given topic, with an additional explanation.
     *
     * <p>This overload extends
     * {@link #saveQuestion(String, String, String, java.util.List, java.util.List)}
     * by allowing an explanation text that clarifies the reasoning or
     * context behind the correct answer(s).
     *
     * @param topic       the title of the topic where the question belongs
     * @param title       the title of the question
     * @param text        the main text (body) of the question
     * @param explanation additional text explaining the correct answer(s),
     *                    may be {@code null}
     * @param answers     a list of possible answers
     * @param correct     a parallel list of booleans marking correct answers
     */
    void saveQuestion(String topic, String title, String text,
                      String explanation, List<String> answers,
                      List<Boolean> correct);

    /**
     * Deletes a question from a specific topic.
     *
     * <p>The question to be removed is identified by its index in the topic's
     * ordered list of questions.
     *
     * @param topic the title of the topic containing the question
     * @param index the index of the question to delete (0-based)
     */
    void deleteQuestion(String topic, int index);

    // ---------------------------------------------------------------------
    // PERSISTENCE LIFECYCLE
    // ---------------------------------------------------------------------

    /**
     * Saves all pending changes in the persistence layer.
     *
     * <p>This default method provides a no-op implementation, allowing
     * implementing classes to override it as needed. It can be used
     * to flush caches, write changes to disk, or synchronize state
     * with external storage.
     */
    default void saveAll() {}
}