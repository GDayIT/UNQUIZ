package dbbl;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The {@code DbblDelegate} class is a final, package-level facade delegate for the {@code dbbl} package.
 * <p>
 * Its main purpose is to provide a unified, functional API for other packages (e.g., GUI or guimodule)
 * to interact with the business logic and persistence layers without exposing internal implementations.
 * This class composes {@link BusinesslogicaDelegation} and {@link PersistenceDelegate} in a single facade.
 * <p>
 * <b>Design Principles and Best Practices:</b>
 * <ul>
 *     <li>Immutability: All core references (persistence, business) are final and initialized once.</li>
 *     <li>Factory-based instantiation: {@link #createDefault()} provides a preconfigured default stack.</li>
 *     <li>Functional endpoints: Exposes Supplier, Function, BiFunction, and Runnable for lambda-friendly usage.</li>
 *     <li>Encapsulation: No internal types are leaked beyond the functional API, preserving modularity.</li>
 *     <li>Single Responsibility: Delegates business logic to {@link BusinesslogicaDelegation} and persistence to {@link PersistenceDelegate}.</li>
 * </ul>
 * 
 * <b>Core Responsibilities:</b>
 * <ul>
 *     <li>Themes management (CRUD via functional endpoints)</li>
 *     <li>Questions management (CRUD via functional endpoints)</li>
 *     <li>Leitner card management via persistence delegate</li>
 *     <li>Session registration/management via persistence delegate</li>
 *     <li>Functional shortcuts for GUI-oriented operations</li>
 * </ul>
 * 
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * DbblDelegate delegate = DbblDelegate.createDefault();
 * List<String> topics = delegate.uiAllTopics().get();
 * delegate.uiSaveQuestionSimple().save("Math", "Q1", "2+2=?", Arrays.asList("3","4"), Arrays.asList(false,true));
 * }</pre>
 * 
 * <p>Author: D. Georgiou
 * @version 1.0
 */
public final class DbblDelegate {

    // ------------------- CORE COMPOSED SERVICES -------------------

    /**
     * The persistence delegate handling all database operations.
     * Provides functional endpoints for Themes, Questions, Leitner cards, and Sessions.
     */
    private final PersistenceDelegate persistence;

    /**
     * The business logic delegate providing operations that the GUI or other clients consume.
     * Encapsulates domain-specific rules, computation, and aggregation of data.
     */
    private final BusinesslogicaDelegation business;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Private constructor to enforce immutability and controlled creation via factory methods.
     * 
     * @param persistence the persistence delegate
     * @param business the business logic delegate
     */
    private DbblDelegate(PersistenceDelegate persistence, BusinesslogicaDelegation business) {
        this.persistence = persistence;
        this.business = business;
    }

    // ------------------- FACTORY METHOD -------------------

    /**
     * Creates the default {@code DbblDelegate} instance with a preconfigured persistence
     * and business controller stack.
     * 
     * <p>This factory method ensures that clients get a fully initialized delegate
     * without needing to know about {@link ModularPersistenceService} or
     * {@link ModularBusinessController}.
     *
     * @return a new instance of {@code DbblDelegate} with default stack
     */
    public static DbblDelegate createDefault() {
        ModularPersistenceService p = new ModularPersistenceService();
        ModularBusinessController b = new ModularBusinessController(p);
        return new DbblDelegate(p, b);
    }

    // ------------------- THEMES API -------------------

    /**
     * Supplier for retrieving all theme titles.
     * Delegates the request to the persistence layer.
     *
     * @return a {@link Supplier} that provides a {@link List} of theme titles
     */
    public Supplier<List<String>> themesAll() {
        return persistence.getAllThemes();
    }

    /**
     * Function to save a theme (title + description) using persistence delegate.
     *
     * @return a {@link Function} that accepts {@link PersistenceDelegate.ThemeData} and returns success status
     */
    public Function<PersistenceDelegate.ThemeData, Boolean> themeSave() {
        return persistence.saveTheme();
    }

    /**
     * Function to load a theme by title.
     *
     * @return a {@link Function} that maps theme title to {@link PersistenceDelegate.ThemeData}
     */
    public Function<String, PersistenceDelegate.ThemeData> themeLoad() {
        return persistence.loadTheme();
    }

    /**
     * Function to delete a theme by title.
     *
     * @return a {@link Function} that accepts a theme title and returns deletion success status
     */
    public Function<String, Boolean> themeDelete() {
        return persistence.deleteTheme();
    }

    // ------------------- QUESTIONS API -------------------

    /**
     * Function to load all questions of a given theme.
     *
     * @return a {@link Function} that maps theme title to a {@link List} of {@link PersistenceDelegate.QuestionData}
     */
    public Function<String, List<PersistenceDelegate.QuestionData>> questionsByTheme() {
        return persistence.loadQuestionsByTheme();
    }

    /**
     * Function to save a question (with all details) in persistence.
     *
     * @return a {@link Function} that accepts {@link PersistenceDelegate.QuestionData} and returns success status
     */
    public Function<PersistenceDelegate.QuestionData, Boolean> questionSave() {
        return persistence.saveQuestion();
    }

    /**
     * Function to delete a question using theme + index as request.
     *
     * @return a {@link Function} that accepts {@link PersistenceDelegate.QuestionDeleteRequest} and returns deletion success status
     */
    public Function<PersistenceDelegate.QuestionDeleteRequest, Boolean> questionDelete() {
        return persistence.deleteQuestion();
    }

    // ------------------- BUSINESS SHORTCUTS (GUI-ORIENTED) -------------------

    /**
     * Supplier for all topics (delegates to business logic).
     *
     * @return a {@link Supplier} providing a {@link List} of topic names
     */
    public Supplier<List<String>> uiAllTopics() {
        return () -> business.getAllTopics();
    }

    /**
     * Function to get all question titles for a given topic.
     *
     * @return a {@link Function} mapping topic name to {@link List} of question titles
     */
    public Function<String, List<String>> uiQuestionTitles() {
        return t -> business.getQuestionTitles(t);
    }

    /**
     * BiFunction to get a specific question object by (topic, index).
     *
     * @return a {@link BiFunction} mapping (topic, index) to {@link RepoQuizeeQuestions}
     */
    public BiFunction<String, Integer, RepoQuizeeQuestions> uiGetQuestion() {
        return (t, i) -> business.getQuestion(t, i);
    }

    /**
     * Shortcut to save a simple question (without explanation).
     *
     * @return a {@link QuestionSaveSimple} functional interface for GUI-friendly usage
     */
    public QuestionSaveSimple uiSaveQuestionSimple() {
        return (topic, title, text, answers, correct) -> {
            business.saveQuestion(topic, title, text, answers, correct);
            return true;
        };
    }

    /**
     * Runnable to persist all data immediately via persistence delegate.
     *
     * @return a {@link Runnable} that executes persistence flush
     */
    public Runnable persistAll() {
        return persistence.persistAll();
    }

    // ------------------- FUNCTIONAL TYPES -------------------

    /**
     * Functional interface to save a simple question (without explanation).
     */
    @FunctionalInterface
    public interface QuestionSaveSimple {
        boolean save(String topic, String title, String text, List<String> answers, List<Boolean> correct);
    }

    // ------------------- RAW DELEGATE ACCESS -------------------

    /**
     * Returns the raw persistence delegate instance (for internal or advanced use).
     *
     * @return the {@link PersistenceDelegate} instance
     */
    public PersistenceDelegate rawPersistence() {
        return persistence;
    }

    /**
     * Returns the raw business delegate instance (for internal or advanced use).
     *
     * @return the {@link BusinesslogicaDelegation} instance
     */
    public BusinesslogicaDelegation rawBusiness() {
        return business;
    }
}