package guimodule;

import dbbl.DbblDelegate;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Central facade delegate for the {@code guimodule} package.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Encapsulates GUI-related services and delegates calls to the database/business layer.</li>
 *   <li>Exposes functional endpoints for UI composition without leaking internal classes.</li>
 *   <li>Provides factory methods for modular sub-services like sorting, statistics, styling, and Leitner system.</li>
 *   <li>Supports Questions, Topics, Themes, Leitner Cards, and Sessions management.</li>
 * </ul>
 * <p>
 * Key principles:
 * <ul>
 *   <li>Functional delegation using Supplier, Function, BiConsumer.</li>
 *   <li>Single point of access for GUI modules to interact with the business layer.</li>
 *   <li>Backward-compatible access to {@link dbbl.BusinesslogicaDelegation}.</li>
 * </ul>
 * 
 * @author D.
 * @version 1.0
 */
public final class GuiModuleDelegate {

    /** Internal database/business delegate. Provides all CRUD operations and business logic. */
    private final DbblDelegate db;

    /**
     * Private constructor for controlled instantiation.
     * @param db Internal DB delegate
     */
    private GuiModuleDelegate(DbblDelegate db) {
        this.db = db;
    }

    /**
     * Factory method to create a default GuiModuleDelegate with default DB delegate.
     * @return new instance of GuiModuleDelegate
     */
    public static GuiModuleDelegate createDefault() {
        return new GuiModuleDelegate(DbblDelegate.createDefault());
    }

    // === Topics / Themes ===

    /**
     * Supplier for all available topics/themes.
     * @return list of all topic names
     */
    public Supplier<List<String>> allTopics() { 
        return db.uiAllTopics(); 
    }

    // === Questions ===

    /**
     * Function returning the question titles for a given topic.
     * @return mapping topic -> list of question titles
     */
    public Function<String, List<String>> questionTitles() { 
        return db.uiQuestionTitles(); 
    }

    /**
     * Function returning the number of questions for a given topic.
     * @return topic -> count of questions
     */
    public Function<String, Integer> questionCount() {
        return topic -> questionTitles().apply(topic).size();
    }

    /**
     * Provides a BiConsumer that handles question selection events.
     * @param handler callback to execute when a question is selected
     * @return BiConsumer accepting topic and question index
     */
    public BiConsumer<String, Integer> onQuestionSelected(QuestionSelectionHandler handler) {
        return (topic, index) -> handler.handle(topic, index);
    }

    // === Module factories: Sorting, Statistics, Styling, Leitner ===

    /** Creates a new modular sorting service. */
    public ModularSortingService newSortingService() { return new ModularSortingService(); }

    /** Creates a new modular statistics service for quizzes. */
    public ModularQuizStatistics newStatisticsService() { return new ModularQuizStatistics(); }

    /** Creates a new modular style service for GUI theming. */
    public ModularStyleService newStyleService() { return new ModularStyleService(); }

    /** Creates a new adaptive Leitner system for spaced repetition learning. */
    public AdaptiveLeitnerSystem newLeitnerSystem() { 
        return new AdaptiveLeitnerSystem(db.rawBusiness()); 
    }

    /**
     * Provides backward-compatible access to the business interface.
     * <p>
     * This is primarily used for legacy GUI panels that depend on direct
     * {@link dbbl.BusinesslogicaDelegation} access.
     * 
     * @return business layer delegate
     */
    public dbbl.BusinesslogicaDelegation business() { 
        return db.rawBusiness(); 
    }

    /**
     * Functional interface for question selection callbacks.
     * <p>
     * Used in {@link #onQuestionSelected(QuestionSelectionHandler)}.
     */
    @FunctionalInterface
    public interface QuestionSelectionHandler {
        /**
         * Handle a question selection.
         * @param topic The topic of the selected question
         * @param index The index of the selected question
         */
        void handle(String topic, int index);
    }
}