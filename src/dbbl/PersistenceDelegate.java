package dbbl;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Functional delegation interface for persistence operations.
 * 
 * This interface defines lambda-based contracts for all persistence operations,
 * enabling complete modularity and functional programming patterns.
 * 
 * Each operation is defined as a functional interface to support:
 * - Lambda expressions for implementation
 * - Method references for delegation
 * - Functional composition and chaining
 * - Asynchronous execution patterns
 * 
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
=======
 * @author Quiz Application Team
 * @version 2.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public interface PersistenceDelegate {
    
    // === THEME OPERATIONS ===
    
    /**
     * Lambda for saving theme data.
     * Function takes (title, description) and returns success status.
     */
    Function<ThemeData, Boolean> saveTheme();
    
    /**
     * Lambda for loading theme data.
     * Function takes title and returns ThemeData or null.
     */
    Function<String, ThemeData> loadTheme();
    
    /**
     * Lambda for deleting theme.
     * Function takes title and returns success status.
     */
    Function<String, Boolean> deleteTheme();
    
    /**
     * Lambda for getting all theme titles.
     * Supplier returns list of all available themes.
     */
    Supplier<List<String>> getAllThemes();
    
    // === QUESTION OPERATIONS ===
    
    /**
     * Lambda for saving question data.
     * Function takes QuestionData and returns success status.
     */
    Function<QuestionData, Boolean> saveQuestion();
    
    /**
     * Lambda for loading questions by theme.
     * Function takes theme title and returns list of questions.
     */
    Function<String, List<QuestionData>> loadQuestionsByTheme();
    
    /**
     * Lambda for deleting question.
     * Function takes (theme, questionIndex) and returns success status.
     */
    Function<QuestionDeleteRequest, Boolean> deleteQuestion();
    
    // === PERSISTENCE LIFECYCLE ===
    
    /**
     * Lambda for complete data persistence.
     * Runnable executes full save operation.
     */
    Runnable persistAll();
    
    /**
     * Lambda for data loading.
     * Runnable executes full load operation.
     */
    Runnable loadAll();
    
    /**
     * Lambda for backup creation.
     * Function takes backup name and returns success status.
     */
    Function<String, Boolean> createBackup();
    
    // === EVENT CALLBACKS ===
    
    /**
     * Lambda for data change notifications.
     * Consumer receives change event data.
     */
    Consumer<DataChangeEvent> onDataChanged();
    
    /**
     * Lambda for error handling.
     * Consumer receives error information.
     */
    Consumer<PersistenceError> onError();
    
    // === UTILITY CLASSES ===
    
    /**
     * Data transfer object for theme information.
     */
    class ThemeData {
        public final String title;
        public final String description;
        public final long timestamp;
        
        public ThemeData(String title, String description) {
            this.title = title;
            this.description = description;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Data transfer object for question information.
     */
    class QuestionData {
        public final String theme;
        public final String title;
        public final String questionText;
        public final String explanation;
        public final List<String> answers;
        public final List<Boolean> correctFlags;
        public final long timestamp;

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
     * Request object for question deletion.
     */
    class QuestionDeleteRequest {
        public final String theme;
        public final int questionIndex;
        
        public QuestionDeleteRequest(String theme, int questionIndex) {
            this.theme = theme;
            this.questionIndex = questionIndex;
        }
    }
    
    /**
     * Event object for data changes.
     */
    class DataChangeEvent {
        public final String type; // "THEME_ADDED", "QUESTION_UPDATED", etc.
        public final String target; // affected theme/question
        public final long timestamp;
        
        public DataChangeEvent(String type, String target) {
            this.type = type;
            this.target = target;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Error information for persistence operations.
     */
    class PersistenceError {
        public final String operation;
        public final String message;
        public final Throwable cause;
        public final long timestamp;
        
        public PersistenceError(String operation, String message, Throwable cause) {
            this.operation = operation;
            this.message = message;
            this.cause = cause;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
