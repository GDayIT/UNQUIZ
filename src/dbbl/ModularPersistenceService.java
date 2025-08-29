package dbbl;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

/**
 * Fully modular persistence service using functional programming and lambda expressions.
 * 
 * This service implements complete modularity through:
 * - Lambda-based operations for all CRUD functions
 * - Functional composition for complex operations
 * - Event-driven architecture with callbacks
 * - Thread-safe operations using concurrent collections
 * - Serialization support for all data structures
 * 
 * Architecture:
 * - All operations are defined as lambdas/functions
 * - No direct method calls between modules
 * - Complete separation of concerns
 * - Reactive programming patterns
 * 
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
class ModularPersistenceService implements Serializable, PersistenceDelegate {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "quiz_questions.dat";
    
    // === CORE DATA STRUCTURES ===
    private final Map<String, List<RepoQuizeeQuestions>> questionsByTheme = new ConcurrentHashMap<>();
    private final Map<String, String> themeDescriptions = new ConcurrentHashMap<>();

    private final LocalDateTime createdAt;
    
    // === SERIALIZATION SNAPSHOT ===
    private static class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<String, List<RepoQuizeeQuestions>> questionsByTheme;
        Map<String, String> themeDescriptions;
        LocalDateTime createdAt;
    }
    
    // === LAMBDA STORAGE (RUNTIME ONLY) ===
    private transient Function<ThemeData, Boolean> saveThemeImpl;
    private transient Function<String, ThemeData> loadThemeImpl;
    private transient Function<String, Boolean> deleteThemeImpl;
    private transient Supplier<List<String>> getAllThemesImpl;
    private transient Function<QuestionData, Boolean> saveQuestionImpl;
    private transient Function<String, List<QuestionData>> loadQuestionsByThemeImpl;
    private transient Function<QuestionDeleteRequest, Boolean> deleteQuestionImpl;
    private transient Runnable persistAllImpl;
    private transient Runnable loadAllImpl;
    private transient Function<String, Boolean> createBackupImpl;
    private transient Consumer<DataChangeEvent> onDataChangedImpl;
    private transient Consumer<PersistenceError> onErrorImpl;
    
    /**
     * Creates a new modular persistence service with lambda-based configuration.
     */
    public ModularPersistenceService() {
        this.createdAt = LocalDateTime.now();
        initializeLambdas();
        loadAllImpl.run(); // Load existing data
    }
    
    /**
     * Initializes all lambda implementations using functional programming.
     */
    private void initializeLambdas() {
        
        // === THEME OPERATIONS ===
        saveThemeImpl = themeData -> {
            try {
                questionsByTheme.computeIfAbsent(themeData.title, k -> new ArrayList<>());
                themeDescriptions.put(themeData.title, themeData.description);
                
                persistAllImpl.run();
                notifyDataChange("THEME_SAVED", themeData.title);
                return true;
            } catch (Exception e) {
                handleError(e);
                return false;
            }
        };
        
        loadThemeImpl = title -> {
            String description = themeDescriptions.getOrDefault(title, "");
            return new ThemeData(title, description);
        };
        
        deleteThemeImpl = title -> {
            try {
                questionsByTheme.remove(title);
                themeDescriptions.remove(title);
                
                persistAllImpl.run();
                notifyDataChange("THEME_DELETED", title);
                return true;
            } catch (Exception e) {
                handleError(e);
                return false;
            }
        };
        
        getAllThemesImpl = () -> new ArrayList<>(questionsByTheme.keySet());
        
        // === QUESTION OPERATIONS ===
        saveQuestionImpl = questionData -> {
            try {
                List<RepoQuizeeQuestions> questions = questionsByTheme.computeIfAbsent(
                    questionData.theme, k -> new ArrayList<>());
                
                // Convert to internal format
                boolean[] correctArray = new boolean[questionData.correctFlags.size()];
                for (int i = 0; i < questionData.correctFlags.size(); i++) {
                    correctArray[i] = questionData.correctFlags.get(i);
                }

                RepoQuizeeQuestions question = new RepoQuizeeQuestions(
                    questionData.title,
                    questionData.questionText,
                    questionData.answers.toArray(new String[0]),
                    correctArray,
                    questionData.explanation
                );
                question.setThema(questionData.theme);
                
                // Update existing or add new
                boolean updated = false;
                for (int i = 0; i < questions.size(); i++) {
                    if (questions.get(i).getTitel().equals(questionData.title)) {
                        questions.set(i, question);
                        updated = true;
                        break;
                    }
                }
                
                if (!updated) {
                    questions.add(question);
                }
                
                persistAllImpl.run();
                notifyDataChange("QUESTION_SAVED", questionData.theme + ":" + questionData.title);
                return true;
            } catch (Exception e) {
                handleError(e);
                return false;
            }
        };
        
        loadQuestionsByThemeImpl = theme -> {
            List<RepoQuizeeQuestions> questions = questionsByTheme.getOrDefault(theme, new ArrayList<>());
            return questions.stream()
                .map(q -> new QuestionData(
                    q.getThema(),
                    q.getTitel(),
                    q.getFrageText(),
                    q.getErklaerung() != null ? q.getErklaerung() : "",
                    q.getAntworten(),
                    q.getKorrekt()
                ))
                .collect(Collectors.toList());
        };
        
        deleteQuestionImpl = request -> {
            try {
                List<RepoQuizeeQuestions> questions = questionsByTheme.get(request.theme);
                if (questions != null && request.questionIndex >= 0 && request.questionIndex < questions.size()) {
                    RepoQuizeeQuestions removed = questions.remove(request.questionIndex);
                    persistAllImpl.run();
                    notifyDataChange("QUESTION_DELETED", request.theme + ":" + removed.getTitel());
                    return true;
                }
                return false;
            } catch (Exception e) {
                handleError(e);
                return false;
            }
        };
        
        // === PERSISTENCE OPERATIONS ===
        persistAllImpl = () -> {
            File target = new File(DATA_FILE);
            File tmp = new File(DATA_FILE + ".tmp");
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tmp))) {
                Snapshot snap = new Snapshot();
                snap.questionsByTheme = new HashMap<>(questionsByTheme);
                snap.themeDescriptions = new HashMap<>(themeDescriptions);
                snap.createdAt = createdAt;
                out.writeObject(snap);
                out.flush();
                log("Data persisted successfully");
            } catch (IOException e) {
                handleError(e);
                if (tmp.exists()) tmp.delete();
                return;
            }
            if (target.exists() && !target.delete()) {
                log("Failed to replace existing data file");
                tmp.delete();
                return;
            }
            if (!tmp.renameTo(target)) {
                log("Failed to finalize data save");
                tmp.delete();
            }
        };
        
        loadAllImpl = () -> {
            File f = new File(DATA_FILE);
            if (!f.exists()) {
                log("No existing data found, starting fresh");
                initializeExampleData();
                return;
            }
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
                Object obj = in.readObject();
                if (obj instanceof Snapshot) {
                    Snapshot snap = (Snapshot) obj;
                    this.questionsByTheme.clear();
                    this.questionsByTheme.putAll(snap.questionsByTheme != null ? snap.questionsByTheme : new HashMap<>());
                    this.themeDescriptions.clear();
                    this.themeDescriptions.putAll(snap.themeDescriptions != null ? snap.themeDescriptions : new HashMap<>());
                    log("Data loaded successfully");
                } else if (obj instanceof ModularPersistenceService) {
                    ModularPersistenceService loaded = (ModularPersistenceService) obj;
                    // Legacy fallback
                    this.questionsByTheme.clear();
                    this.questionsByTheme.putAll(loaded.questionsByTheme);
                    this.themeDescriptions.clear();
                    this.themeDescriptions.putAll(loaded.themeDescriptions);
                    log("Data loaded from legacy format");
                } else {
                    log("Unknown data format - starting fresh");
                    initializeExampleData();
                }
            } catch (IOException | ClassNotFoundException e) {
                log("Failed to load data - starting fresh");
                if (f.exists() && !f.delete()) {
                    log("Failed to remove corrupt data file");
                }
                initializeExampleData();
            }
        };
        
        createBackupImpl = backupName -> {
            try {
                File original = new File(DATA_FILE);
                if (original.exists()) {
                    File backup = new File(backupName + "_" + System.currentTimeMillis() + ".bak");
                    try (FileInputStream fis = new FileInputStream(original);
                         FileOutputStream fos = new FileOutputStream(backup)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                    return true;
                }
                return false;
            } catch (IOException e) {
                handleError(e);
                return false;
            }
        };
        
        // === EVENT HANDLING ===
        onDataChangedImpl = event -> {
            log("Data changed: " + event.type + " - " + event.target);
            // Additional event handling can be added here
        };
        
        onErrorImpl = error -> {
            log("Error in " + error.operation + ": " + error.message);
            if (error.cause != null) {
                error.cause.printStackTrace();
            }
        };
    }
    
    // === INTERFACE IMPLEMENTATIONS ===
    @Override public Function<ThemeData, Boolean> saveTheme() { return saveThemeImpl; }
    @Override public Function<String, ThemeData> loadTheme() { return loadThemeImpl; }
    @Override public Function<String, Boolean> deleteTheme() { return deleteThemeImpl; }
    @Override public Supplier<List<String>> getAllThemes() { return getAllThemesImpl; }
    @Override public Function<QuestionData, Boolean> saveQuestion() { return saveQuestionImpl; }
    @Override public Function<String, List<QuestionData>> loadQuestionsByTheme() { return loadQuestionsByThemeImpl; }
    @Override public Function<QuestionDeleteRequest, Boolean> deleteQuestion() { return deleteQuestionImpl; }
    @Override public Runnable persistAll() { return persistAllImpl; }
    @Override public Runnable loadAll() { return loadAllImpl; }
    @Override public Function<String, Boolean> createBackup() { return createBackupImpl; }
    @Override public Consumer<DataChangeEvent> onDataChanged() { return onDataChangedImpl; }
    @Override public Consumer<PersistenceError> onError() { return onErrorImpl; }
    
    // === UTILITY METHODS ===
    private Void handleError(Throwable e) {
        onErrorImpl.accept(new PersistenceError("PERSISTENCE", e.getMessage(), e));
        return null;
    }
    
    private void log(String message) {
        System.out.println("[" + LocalDateTime.now() + "] " + message);
    }
    
    private void notifyDataChange(String type, String target) {
        onDataChangedImpl.accept(new DataChangeEvent(type, target));
    }
    
    private void initializeExampleData() {
        // Start with clean slate (no seeded data)
        log("Starting with empty database - no example data created");
    }
}
