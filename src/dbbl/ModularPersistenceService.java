package dbbl;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

/**
 * {@code ModularPersistenceService} is a fully modular, functional persistence
 * service for dbbl, implementing all CRUD operations for themes, questions,
 * Leitner cards, and session management.
 * <p>
 * This class uses lambda expressions and functional programming paradigms
 * to decouple persistence logic from the business layer, providing:
 * <ul>
 *   <li>Thread-safe operations using {@link ConcurrentHashMap}</li>
 *   <li>Event-driven notifications for data changes and errors</li>
 *   <li>Serialization support for all data structures</li>
 *   <li>Backup creation and snapshot management</li>
 * </ul>
 * 
 * Architecture:
 * <pre>
 * ModularBusinessController → ModularPersistenceService → Serialized data storage
 * </pre>
 * <p>
 * All operations are exposed via functional interfaces such as {@link Function},
 * {@link Supplier}, {@link Consumer}, and {@link Runnable}, enabling easy
 * testing, lambda-based usage, and reactive behavior.
 * 
 * @author D.
 * @version 1.0
 */
class ModularPersistenceService implements Serializable, PersistenceDelegate {
    
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "quiz_questions.dat";

    // ------------------- CORE DATA STRUCTURES -------------------

    /**
     * Stores all questions grouped by theme.
     * Thread-safe concurrent map for multi-threaded access.
     */
    private final Map<String, List<RepoQuizeeQuestions>> questionsByTheme = new ConcurrentHashMap<>();

    /**
     * Stores theme descriptions for each theme.
     */
    private final Map<String, String> themeDescriptions = new ConcurrentHashMap<>();

    /**
     * Timestamp indicating when the service instance was created.
     */
    private final LocalDateTime createdAt;

    // ------------------- SERIALIZATION SNAPSHOT -------------------

    /**
     * Snapshot of the data for persistence purposes.
     * Serialized to disk and used for loading and backup.
     */
    private static class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<String, List<RepoQuizeeQuestions>> questionsByTheme;
        Map<String, String> themeDescriptions;
        @SuppressWarnings("unused")
		LocalDateTime createdAt;
    }

    // ------------------- LAMBDA STORAGE (TRANSIENT) -------------------

    /**
     * Functional implementations for runtime CRUD and utility operations.
     */
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

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Creates a new modular persistence service instance.
     * Initializes all lambda-based operations and loads existing data.
     */
    public ModularPersistenceService() {
        this.createdAt = LocalDateTime.now();
        initializeLambdas();
        loadAllImpl.run(); // Load persisted data
    }

    // ------------------- LAMBDA INITIALIZATION -------------------

    /**
     * Initializes all lambda implementations for CRUD, backup, and event handling.
     * This decouples the functional behavior from the core class logic.
     */
    @SuppressWarnings("unused")
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

        loadThemeImpl = title -> new ThemeData(title, themeDescriptions.getOrDefault(title, ""));

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

                boolean updated = false;
                for (int i = 0; i < questions.size(); i++) {
                    if (questions.get(i).getTitel().equals(questionData.title)) {
                        questions.set(i, question);
                        updated = true;
                        break;
                    }
                }
                if (!updated) questions.add(question);

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
            } catch (IOException e) {
                handleError(e);
                if (tmp.exists()) tmp.delete();
                return;
            }
            if (target.exists() && !target.delete()) tmp.delete();
            if (!tmp.renameTo(target)) tmp.delete();
        };

        loadAllImpl = () -> {
            File f = new File(DATA_FILE);
            if (!f.exists()) {
                initializeExampleData();
                return;
            }
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
                Object obj = in.readObject();
                if (obj instanceof Snapshot) {
                    Snapshot snap = (Snapshot) obj;
                    questionsByTheme.clear();
                    questionsByTheme.putAll(snap.questionsByTheme != null ? snap.questionsByTheme : new HashMap<>());
                    themeDescriptions.clear();
                    themeDescriptions.putAll(snap.themeDescriptions != null ? snap.themeDescriptions : new HashMap<>());
                } else initializeExampleData();
            } catch (IOException | ClassNotFoundException e) {
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
        onDataChangedImpl = event -> {};
        onErrorImpl = error -> { if (error.cause != null) error.cause.printStackTrace(); };
    }

    // ------------------- INTERFACE IMPLEMENTATIONS -------------------
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

    // ------------------- UTILITY METHODS -------------------

    private Void handleError(Throwable e) {
        onErrorImpl.accept(new PersistenceError("PERSISTENCE", e.getMessage(), e));
        return null;
    }

    private void notifyDataChange(String type, String target) {
        onDataChangedImpl.accept(new DataChangeEvent(type, target));
    }

    private void initializeExampleData() {
        // No initial seed; empty database
    }
}
