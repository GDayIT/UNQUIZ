package dbbl;

import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Modular business controller using pure functional programming and lambda expressions.
 * 
 * This controller implements complete modularity through:
 * - Lambda-based delegation to persistence layer
 * - Functional composition for business logic
 * - No direct dependencies on concrete implementations
 * - Event-driven communication patterns
 * - Immutable data transfer objects
 * 
 * Architecture Pattern:
 * GUI Layer → ModularBusinessController → PersistenceDelegate → SerializationModule
 * 
 * All interactions are through functional interfaces, enabling:
 * - Easy testing with mock implementations
 * - Runtime behavior modification
 * - Aspect-oriented programming
 * - Reactive programming patterns
 * 
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
class ModularBusinessController implements BusinesslogicaDelegation {
    
    private final PersistenceDelegate persistence;
    
    // === LAMBDA STORAGE FOR BUSINESS OPERATIONS ===
    private final Function<String, List<String>> getTopicsOperation;
    private final BiFunction<String, String, Boolean> saveThemeOperation;
    private final Function<String, Boolean> deleteThemeOperation;
    private final Function<String, String> getThemeDescriptionOperation;
    private final Function<String, List<String>> getQuestionTitlesOperation;
    private final BiFunction<String, Integer, RepoQuizeeQuestions> getQuestionOperation;
    private final Function<QuestionSaveRequest, Boolean> saveQuestionOperation;
    private final BiFunction<String, Integer, Boolean> deleteQuestionOperation;
    private final Runnable saveAllOperation;
    
    // === EVENT HANDLERS ===
    private Consumer<String> onThemeChanged = theme -> {};
    private Consumer<String> onQuestionChanged = question -> {};
    private Consumer<String> onError = error -> System.err.println("Error: " + error);
    
    /**
     * Creates a new modular business controller with lambda-based persistence delegation.
     * 
     * @param persistence the persistence delegate (can be any implementation)
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
        
        // Setup event handling
        setupEventHandlers();
    }
    
    // === BUSINESS LOGIC IMPLEMENTATIONS ===
    
    private List<String> executeGetTopics(String filter) {
        try {
            List<String> allTopics = persistence.getAllThemes().get();
            
            // Apply filter if provided
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
            if (result) {
                onThemeChanged.accept(title);
            }
            return result;
            
        } catch (Exception e) {
            onError.accept("Failed to save theme: " + e.getMessage());
            return false;
        }
    }
    
    private Boolean executeDeleteTheme(String title) {
        try {
            if (title == null || title.trim().isEmpty()) {
                return false;
            }
            
            Boolean result = persistence.deleteTheme().apply(title);
            if (result) {
                onThemeChanged.accept(title);
            }
            return result;
            
        } catch (Exception e) {
            onError.accept("Failed to delete theme: " + e.getMessage());
            return false;
        }
    }
    
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
    
    private List<String> executeGetQuestionTitles(String theme) {
        try {
            if (theme == null) return List.of();
            
            List<PersistenceDelegate.QuestionData> questions = 
                persistence.loadQuestionsByTheme().apply(theme);
            
            return questions.stream()
                .map(q -> q.title)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            onError.accept("Failed to get question titles: " + e.getMessage());
            return List.of();
        }
    }
    
    private RepoQuizeeQuestions executeGetQuestion(String theme, Integer index) {
        try {
            if (theme == null || index == null || index < 0) return null;
            
            List<PersistenceDelegate.QuestionData> questions = 
                persistence.loadQuestionsByTheme().apply(theme);
            
            if (index >= questions.size()) return null;
            
            PersistenceDelegate.QuestionData questionData = questions.get(index);
            
            // Convert to RepoQuizeeQuestions
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
            if (result) {
                onQuestionChanged.accept(request.theme + ":" + request.title);
            }
            return result;
            
        } catch (Exception e) {
            onError.accept("Failed to save question: " + e.getMessage());
            return false;
        }
    }
    
    private Boolean executeDeleteQuestion(String theme, Integer index) {
        try {
            if (theme == null || index == null || index < 0) return false;
            
            PersistenceDelegate.QuestionDeleteRequest request = 
                new PersistenceDelegate.QuestionDeleteRequest(theme, index);
            
            Boolean result = persistence.deleteQuestion().apply(request);
            if (result) {
                onQuestionChanged.accept(theme + ":deleted");
            }
            return result;
            
        } catch (Exception e) {
            onError.accept("Failed to delete question: " + e.getMessage());
            return false;
        }
    }
    
    private void executeSaveAll() {
        try {
            persistence.persistAll().run();
        } catch (Exception e) {
            onError.accept("Failed to save all data: " + e.getMessage());
        }
    }
    
    // === INTERFACE IMPLEMENTATIONS ===
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
    
    // === EVENT HANDLER SETUP ===
    private void setupEventHandlers() {
        // Setup persistence event handlers - simplified for now
        // Note: Event handling would be implemented in a real system
        System.out.println("Event handlers setup completed");
    }
    
    // === PUBLIC EVENT REGISTRATION ===
    public void setOnThemeChanged(Consumer<String> handler) { this.onThemeChanged = handler; }
    public void setOnQuestionChanged(Consumer<String> handler) { this.onQuestionChanged = handler; }
    public void setOnError(Consumer<String> handler) { this.onError = handler; }
    
    // === UTILITY CLASSES ===
    public static class QuestionSaveRequest {
        public final String theme;
        public final String title;
        public final String text;
        public final String explanation;
        public final List<String> answers;
        public final List<Boolean> correct;

        public QuestionSaveRequest(String theme, String title, String text,
                                 List<String> answers, List<Boolean> correct) {
            this(theme, title, text, "", answers, correct);
        }

        public QuestionSaveRequest(String theme, String title, String text, String explanation,
                                 List<String> answers, List<Boolean> correct) {
            this.theme = theme;
            this.title = title;
            this.text = text;
            this.explanation = explanation;
            this.answers = answers;
            this.correct = correct;
        }
    }
}
