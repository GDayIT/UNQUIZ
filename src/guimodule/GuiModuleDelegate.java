package guimodule;

import dbbl.DbblDelegate;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Single facade delegate for the guimodule package.
 * Encapsulates GUI-related services and exposes functional endpoints
 * for UI composition without leaking internal classes.
 */
public final class GuiModuleDelegate {

    private final DbblDelegate db;

    private GuiModuleDelegate(DbblDelegate db) {
        this.db = db;
    }

    public static GuiModuleDelegate createDefault() {
        return new GuiModuleDelegate(DbblDelegate.createDefault());
    }

    // === Themes ===
    public Supplier<List<String>> allTopics() { return db.uiAllTopics(); }

    // === Questions ===
    public Function<String, List<String>> questionTitles() { return db.uiQuestionTitles(); }

    public Function<String, Integer> questionCount() {
        return topic -> questionTitles().apply(topic).size();
    }

    public BiConsumer<String, Integer> onQuestionSelected(QuestionSelectionHandler handler) {
        return (topic, index) -> handler.handle(topic, index);
    }

    // === Sorting/Statistics/Style: factories for modules ===
    public ModularSortingService newSortingService() { return new ModularSortingService(); }
    public ModularQuizStatistics newStatisticsService() { return new ModularQuizStatistics(); }
    public ModularStyleService newStyleService() { return new ModularStyleService(); }
    public AdaptiveLeitnerSystem newLeitnerSystem() { return new AdaptiveLeitnerSystem(db.rawBusiness()); }

    /**
     * Backward-compatible access to the business interface for existing panels.
     */
    public dbbl.BusinesslogicaDelegation business() { return db.rawBusiness(); }

    @FunctionalInterface
    public interface QuestionSelectionHandler {
        void handle(String topic, int index);
    }
}
