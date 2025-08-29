package dbbl;

import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * {@code ModularBusinessController} is a modular, functional business controller
 * for the dbbl package, providing complete functional programming support.
 * <p>
 * This controller acts as the business logic layer between the GUI and the
 * persistence layer. All operations are defined via lambda expressions or
 * functional interfaces, ensuring:
 * <ul>
 *   <li>Immutability of references and data</li>
 *   <li>Easy testing with mock implementations</li>
 *   <li>Decoupling from concrete persistence implementations</li>
 *   <li>Reactive and event-driven programming</li>
 *   <li>Functional composition for business logic</li>
 * </ul>
 * <p>
 * Architecture Pattern:
 * <pre>
 * GUI Layer → ModularBusinessController → PersistenceDelegate → SerializationModule
 * </pre>
 * 
 * <p>Supports all CRUD operations for:
 * <ul>
 *   <li>Themes</li>
 *   <li>Questions</li>
 *   <li>Leitner cards via persistence delegate</li>
 * </ul>
 * <p>Also provides event handling hooks for GUI updates and error reporting.
 * 
 * <p>Author: D. Georgiou
 * @version 1.0
 */
class ModularBusinessController implements BusinesslogicaDelegation {

    // ------------------- CORE DEPENDENCIES -------------------

    /**
     * Persistence delegate responsible for database operations.
     * Can be any implementation of {@link PersistenceDelegate}.
     */
    private final PersistenceDelegate persistence;

    // ------------------- LAMBDA-BASED BUSINESS OPERATIONS -------------------

    private final Function<String, List<String>> getTopicsOperation;
    private final BiFunction<String, String, Boolean> saveThemeOperation;
    private final Function<String, Boolean> deleteThemeOperation;
    private final Function<String, String> getThemeDescriptionOperation;
    private final Function<String, List<String>> getQuestionTitlesOperation;
    private final BiFunction<String, Integer, RepoQuizeeQuestions> getQuestionOperation;
    private final Function<QuestionSaveRequest, Boolean> saveQuestionOperation;
    private final BiFunction<String, Integer, Boolean> deleteQuestionOperation;
    private final Runnable saveAllOperation;

    // ------------------- EVENT HANDLERS -------------------

    /**
     * Event handler triggered when a theme is created, updated or deleted.
     */
    private Consumer<String> onThemeChanged = theme -> {};

    /**
     * Event handler triggered when a question is created, updated or deleted.
     */
    private Consumer<String> onQuestionChanged = question -> {};

    /**
     * Event handler for errors or exceptions in business operations.
     */
    private Consumer<String> onError = error -> System.err.println("Error: " + error);

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Constructs a new modular business controller with lambda-based persistence delegation.
     * Initializes all business operations as lambda functions and sets up event handlers.
     *
     * @param persistence the persistence delegate to delegate all data operations
     */
    public ModularBusinessController(PersistenceDelegate persistence) {
        this.persistence = persistence;

        // Initialize all business operations as lambdas
        this.getTopicsOperation = this::executeGetTopics;
        this.saveThemeOperation = this::executeSaveTheme;
        this.deleteThemeOperation = this::executeDeleteTheme;
        this.getThemeDescriptionOperation = this::executeGetThemeDescription;
        this.getQuestionTitlesOperation = this::executeGetQuestionTitles;
        this.getQuestionOperation = this::executeGetQuestion;
        this.saveQuestionOperation = this::executeSaveQuestion;
        this.deleteQuestionOperation = this::executeDeleteQuestion;
        this.saveAllOperation = this::executeSaveAll;

        // Setup event handling hooks
        setupEventHandlers();
    }

    // ------------------- BUSINESS LOGIC IMPLEMENTATIONS -------------------

    /**
     * Retrieves all topics optionally filtered by a string.
     *
     * @param filter optional filter string; null or empty returns all topics
     * @return list of topic names
     */
    private List<String> executeGetTopics(String filter) {
        try {
            List<String> allTopics = persistence.getAllThemes().get();
            if (filter != null && !filter.trim().isEmpty()) {
                String filterLower = filter.toLowerCase();
                return allTopics.stream()
                    .filter(topic -> topic.toLowerCase().contains(filterLower))
                    .collect(Collectors.toList());
            }
            return allTopics;
        } catch (Exception e) {
            onError.accept("Failed to get topics: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Saves a theme using persistence delegate and triggers theme-changed event.
     *
     * @param title theme title
     * @param description theme description
     * @return success status
     */
    private Boolean executeSaveTheme(String title, String description) {
        try {
            if (title == null || title.trim().isEmpty()) {
                onError.accept("Theme title cannot be empty");
                return false;
            }
            PersistenceDelegate.ThemeData themeData = new PersistenceDelegate.ThemeData(
                title.trim(),
                description != null ? description.trim() : ""
            );
            Boolean result = persistence.saveTheme().apply(themeData);
            if (result) onThemeChanged.accept(title);
            return result;
        } catch (Exception e) {
            onError.accept("Failed to save theme: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a theme by title and triggers theme-changed event.
     *
     * @param title theme title
     * @return success status
     */
    private Boolean executeDeleteTheme(String title) {
        try {
            if (title == null || title.trim().isEmpty()) return false;
            Boolean result = persistence.deleteTheme().apply(title);
            if (result) onThemeChanged.accept(title);
            return result;
        } catch (Exception e) {
            onError.accept("Failed to delete theme: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the description of a theme.
     *
     * @param title theme title
     * @return theme description or empty string if not found
     */
    private String executeGetThemeDescription(String title) {
        try {
            if (title == null) return "";
            PersistenceDelegate.ThemeData themeData = persistence.loadTheme().apply(title);
            return themeData != null ? themeData.description : "";
        } catch (Exception e) {
            onError.accept("Failed to get theme description: " + e.getMessage());
            return "";
        }
    }

    /**
     * Retrieves all question titles of a given theme.
     *
     * @param theme theme name
     * @return list of question titles
     */
    private List<String> executeGetQuestionTitles(String theme) {
        try {
            if (theme == null) return List.of();
            List<PersistenceDelegate.QuestionData> questions = persistence.loadQuestionsByTheme().apply(theme);
            return questions.stream().map(q -> q.title).collect(Collectors.toList());
        } catch (Exception e) {
            onError.accept("Failed to get question titles: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Retrieves a specific question by theme and index and maps it to {@link RepoQuizeeQuestions}.
     *
     * @param theme theme name
     * @param index question index
     * @return question object or null if not found
     */
    private RepoQuizeeQuestions executeGetQuestion(String theme, Integer index) {
        try {
            if (theme == null || index == null || index < 0) return null;
            List<PersistenceDelegate.QuestionData> questions = persistence.loadQuestionsByTheme().apply(theme);
            if (index >= questions.size()) return null;
            PersistenceDelegate.QuestionData questionData = questions.get(index);
            boolean[] correctArray = new boolean[questionData.correctFlags.size()];
            for (int i = 0; i < questionData.correctFlags.size(); i++) {
                correctArray[i] = questionData.correctFlags.get(i);
            }
            RepoQuizeeQuestions question = new RepoQuizeeQuestions(
                questionData.title,
                questionData.questionText,
                questionData.answers.toArray(new String[0]),
                correctArray
            );
            question.setThema(questionData.theme);
            return question;
        } catch (Exception e) {
            onError.accept("Failed to get question: " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves a question via persistence delegate and triggers question-changed event.
     *
     * @param request encapsulates all question data
     * @return success status
     */
    private Boolean executeSaveQuestion(QuestionSaveRequest request) {
        try {
            if (request.theme == null || request.title == null || request.text == null) {
                onError.accept("Question data incomplete");
                return false;
            }
            PersistenceDelegate.QuestionData questionData = new PersistenceDelegate.QuestionData(
                request.theme,
                request.title,
                request.text,
                request.explanation,
                request.answers,
                request.correct
            );
            Boolean result = persistence.saveQuestion().apply(questionData);
            if (result) onQuestionChanged.accept(request.theme + ":" + request.title);
            return result;
        } catch (Exception e) {
            onError.accept("Failed to save question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a question by theme and index and triggers question-changed event.
     *
     * @param theme theme name
     * @param index question index
     * @return success status
     */
    private Boolean executeDeleteQuestion(String theme, Integer index) {
        try {
            if (theme == null || index == null || index < 0) return false;
            PersistenceDelegate.QuestionDeleteRequest request = new PersistenceDelegate.QuestionDeleteRequest(theme, index);
            Boolean result = persistence.deleteQuestion().apply(request);
            if (result) onQuestionChanged.accept(theme + ":deleted");
            return result;
        } catch (Exception e) {
            onError.accept("Failed to delete question: " + e.getMessage());
            return false;
        }
    }

    /**
     * Persists all data via persistence delegate.
     */
    private void executeSaveAll() {
        try { persistence.persistAll().run(); }
        catch (Exception e) { onError.accept("Failed to save all data: " + e.getMessage()); }
    }

    // ------------------- INTERFACE IMPLEMENTATIONS -------------------

    @Override public List<String> getAllTopics() { return getTopicsOperation.apply(null); }
    @Override public void saveTheme(String title, String description) { saveThemeOperation.apply(title, description); }
    @Override public void deleteTheme(String title) { deleteThemeOperation.apply(title); }
    @Override public String getThemeDescription(String title) { return getThemeDescriptionOperation.apply(title); }
    @Override public List<String> getQuestionTitles(String topic) { return getQuestionTitlesOperation.apply(topic); }
    @Override public RepoQuizeeQuestions getQuestion(String topic, int index) { return getQuestionOperation.apply(topic, index); }
    @Override public void deleteQuestion(String topic, int index) { deleteQuestionOperation.apply(topic, index); }
    @Override public void saveAll() { saveAllOperation.run(); }

    @Override
    public void saveQuestion(String topic, String title, String text, List<String> answers, List<Boolean> correct) {
        saveQuestion(topic, title, text, "", answers, correct);
    }

    @Override
    public void saveQuestion(String topic, String title, String text, String explanation, List<String> answers, List<Boolean> correct) {
        QuestionSaveRequest request = new QuestionSaveRequest(topic, title, text, explanation, answers, correct);
        saveQuestionOperation.apply(request);
    }

    // ------------------- EVENT HANDLER REGISTRATION -------------------

    private void setupEventHandlers() {
        // In a full implementation, events from persistence could be registered here
        System.out.println("Event handlers setup completed");
    }

    public void setOnThemeChanged(Consumer<String> handler) { this.onThemeChanged = handler; }
    public void setOnQuestionChanged(Consumer<String> handler) { this.onQuestionChanged = handler; }
    public void setOnError(Consumer<String> handler) { this.onError = handler; }

    // ------------------- UTILITY CLASSES -------------------

    /**
     * Data container for saving questions through business layer.
     * Immutable by design.
     */
    public static class QuestionSaveRequest {
        public final String theme;
        public final String title;
        public final String text;
        public final String explanation;
        public final List<String> answers;
        public final List<Boolean> correct;

        public QuestionSaveRequest(String theme, String title, String text, List<String> answers, List<Boolean> correct) {
            this(theme, title, text, "", answers, correct);
        }

        public QuestionSaveRequest(String theme, String title, String text, String explanation, List<String> answers, List<Boolean> correct) {
            this.theme = theme;
            this.title = title;
            this.text = text;
            this.explanation = explanation;
            this.answers = answers;
            this.correct = correct;
        }
    }
}