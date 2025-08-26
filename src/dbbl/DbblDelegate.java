package dbbl;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Single, package-level facade delegate for the dbbl package.
 * 
 * Purpose:
 * - Provide one unified delegate to be used by other packages (gui, guimodule)
 * - Compose business (BusinesslogicaDelegation) and persistence (PersistenceDelegate) in a single API
 * - Expose only functional endpoints (Supplier/Function/BiFunction), enabling lambda-based usage and testing
 * 
 * Best Practices:
 * - Immutability of references, explicit factory methods to create default stack
 * - No direct type leakage of internal implementations; other packages only need this facade
 */
public final class DbblDelegate {

    // Core composed services
    private final PersistenceDelegate persistence;
    private final BusinesslogicaDelegation business;

    private DbblDelegate(PersistenceDelegate persistence, BusinesslogicaDelegation business) {
        this.persistence = persistence;
        this.business = business;
    }

    /**
     * Creates the default delegate with the modular persistence and controller.
     */
    public static DbblDelegate createDefault() {
        ModularPersistenceService p = new ModularPersistenceService();
        ModularBusinessController b = new ModularBusinessController(p);
        return new DbblDelegate(p, b);
    }

    // === THEMES API ===

    /** Supplier of all theme titles. */
    public Supplier<List<String>> themesAll() {
        return persistence.getAllThemes();
    }

    /** Function to save theme (title+description) via persistence. */
    public Function<PersistenceDelegate.ThemeData, Boolean> themeSave() {
        return persistence.saveTheme();
    }

    /** Function to load a theme (by title) from persistence. */
    public Function<String, PersistenceDelegate.ThemeData> themeLoad() {
        return persistence.loadTheme();
    }

    /** Function to delete a theme (by title). */
    public Function<String, Boolean> themeDelete() {
        return persistence.deleteTheme();
    }

    // === QUESTIONS API ===

    /** Function to load questions of a theme (title). */
    public Function<String, List<PersistenceDelegate.QuestionData>> questionsByTheme() {
        return persistence.loadQuestionsByTheme();
    }

    /** Function to save a question. */
    public Function<PersistenceDelegate.QuestionData, Boolean> questionSave() {
        return persistence.saveQuestion();
    }

    /** Function to delete a question by (theme, index). */
    public Function<PersistenceDelegate.QuestionDeleteRequest, Boolean> questionDelete() {
        return persistence.deleteQuestion();
    }

    // === BUSINESS SHORTCUTS (GUI-ORIENTED) ===

    /** Supplier for all topics (delegates to business). */
    public Supplier<List<String>> uiAllTopics() {
        return () -> business.getAllTopics();
    }

    /** Function to get a question title list for a given topic. */
    public Function<String, List<String>> uiQuestionTitles() {
        return t -> business.getQuestionTitles(t);
    }

    /** BiFunction to get a specific question object by (topic, index). */
    public BiFunction<String, Integer, RepoQuizeeQuestions> uiGetQuestion() {
        return (t, i) -> business.getQuestion(t, i);
    }

    /** Shortcut to save a simple question (without explanation). */
    public QuestionSaveSimple uiSaveQuestionSimple() {
        return (topic, title, text, answers, correct) -> {
            business.saveQuestion(topic, title, text, answers, correct);
            return true;
        };
    }

    /** Persist all data now. */
    public Runnable persistAll() { return persistence.persistAll(); }

    // === Functional types ===
    @FunctionalInterface
    public interface QuestionSaveSimple {
        boolean save(String topic, String title, String text, List<String> answers, List<Boolean> correct);
    }

    // === Expose raw delegates if needed internally ===
    public PersistenceDelegate rawPersistence() { return persistence; }
    public BusinesslogicaDelegation rawBusiness() { return business; }
}
