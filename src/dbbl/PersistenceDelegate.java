package dbbl;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@code PersistenceDelegate} defines a functional interface for modular,
 * lambda-based persistence operations in the dbbl package.
 * <p>
 * All operations are expressed as functional interfaces, enabling:
 * <ul>
 *   <li>Lambda expressions for implementation</li>
 *   <li>Method references for delegation</li>
 *   <li>Functional composition and chaining</li>
 *   <li>Reactive and asynchronous execution patterns</li>
 * </ul>
 * <p>
 * This interface provides contracts for:
 * - Theme operations
 * - Question operations
 * - Leitner card persistence (extendable)
 * - Session management (extendable)
 * - Persistence lifecycle (save/load)
 * - Event notifications and error handling
 * 
 * @author D.
 * @version 1.0
 */
public interface PersistenceDelegate {

    // ------------------- THEME OPERATIONS -------------------

    /**
     * Lambda for saving a theme.
     * Accepts a {@link ThemeData} object containing title and description.
     * Returns {@code true} if saving was successful, {@code false} otherwise.
     */
    Function<ThemeData, Boolean> saveTheme();

    /**
     * Lambda for loading theme data by title.
     * Returns {@link ThemeData} or null if the theme does not exist.
     */
    Function<String, ThemeData> loadTheme();

    /**
     * Lambda for deleting a theme by title.
     * Returns {@code true} if deletion was successful, {@code false} otherwise.
     */
    Function<String, Boolean> deleteTheme();

    /**
     * Lambda to retrieve all theme titles.
     * Returns a {@link List} of theme names.
     */
    Supplier<List<String>> getAllThemes();

    // ------------------- QUESTION OPERATIONS -------------------

    /**
     * Lambda for saving a question.
     * Accepts {@link QuestionData} containing text, answers, and correctness flags.
     * Returns {@code true} if saving was successful.
     */
    Function<QuestionData, Boolean> saveQuestion();

    /**
     * Lambda to load all questions of a specific theme.
     * Accepts a theme title and returns a list of {@link QuestionData}.
     */
    Function<String, List<QuestionData>> loadQuestionsByTheme();

    /**
     * Lambda to delete a question by theme and index.
     * Accepts a {@link QuestionDeleteRequest} and returns success status.
     */
    Function<QuestionDeleteRequest, Boolean> deleteQuestion();

    // ------------------- PERSISTENCE LIFECYCLE -------------------

    /**
     * Lambda to persist all data to permanent storage.
     * Runnable executes full save operation.
     */
    Runnable persistAll();

    /**
     * Lambda to load all persisted data.
     * Runnable executes full load operation.
     */
    Runnable loadAll();

    /**
     * Lambda to create a backup of the persisted data.
     * Accepts backup name and returns {@code true} if backup succeeded.
     */
    Function<String, Boolean> createBackup();

    // ------------------- EVENT CALLBACKS -------------------

    /**
     * Event handler for data changes (themes/questions).
     * Accepts a {@link DataChangeEvent} describing the change.
     */
    Consumer<DataChangeEvent> onDataChanged();

    /**
     * Event handler for errors occurring during persistence operations.
     * Accepts a {@link PersistenceError} object with details.
     */
    Consumer<PersistenceError> onError();

    // ------------------- INTERNAL DATA TRANSFER OBJECTS -------------------

    /**
     * Data Transfer Object representing a theme.
     */
    class ThemeData {
        public final String title;        // Theme title
        public final String description;  // Theme description
        public final long timestamp;      // Creation timestamp

        public ThemeData(String title, String description) {
            this.title = title;
            this.description = description;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Data Transfer Object representing a question.
     */
    class QuestionData {
        public final String theme;            // Associated theme
        public final String title;            // Question title
        public final String questionText;     // Question text
        public final String explanation;      // Optional explanation
        public final List<String> answers;    // List of answer options
        public final List<Boolean> correctFlags; // Correctness flags
        public final long timestamp;          // Creation timestamp

        public QuestionData(String theme, String title, String questionText,
                           List<String> answers, List<Boolean> correctFlags) {
            this(theme, title, questionText, "", answers, correctFlags);
        }

        public QuestionData(String theme, String title, String questionText, String explanation,
                            List<String> answers, List<Boolean> correctFlags) {
            this.theme = theme;
            this.title = title;
            this.questionText = questionText;
            this.explanation = explanation;
            this.answers = answers;
            this.correctFlags = correctFlags;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Request object representing a question deletion operation.
     */
    class QuestionDeleteRequest {
        public final String theme;      // Theme name
        public final int questionIndex; // Index of question in the theme list

        public QuestionDeleteRequest(String theme, int questionIndex) {
            this.theme = theme;
            this.questionIndex = questionIndex;
        }
    }

    /**
     * Event object representing a change in data.
     */
    class DataChangeEvent {
        public final String type;       // Change type, e.g., "THEME_ADDED"
        public final String target;     // Target object, e.g., theme/question
        public final long timestamp;    // Timestamp of event

        public DataChangeEvent(String type, String target) {
            this.type = type;
            this.target = target;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * Error object representing an issue during persistence.
     */
    class PersistenceError {
        public final String operation;  // Operation name
        public final String message;    // Error message
        public final Throwable cause;   // Optional cause
        public final long timestamp;    // Timestamp

        public PersistenceError(String operation, String message, Throwable cause) {
            this.operation = operation;
            this.message = message;
            this.cause = cause;
            this.timestamp = System.currentTimeMillis();
        }
    }
}