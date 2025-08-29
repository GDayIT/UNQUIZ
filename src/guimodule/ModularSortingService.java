package guimodule;

import dbbl.RepoQuizeeQuestions;
import java.io.*;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@code ModularSortingService} is a modular, lambda-driven implementation of a
 * {@link SortingDelegate} that provides a complete and extensible framework for
 * sorting and filtering {@link RepoQuizeeQuestions} objects.
 *
 * <p>This service integrates functional programming concepts, persistence, and event-driven
 * design patterns to allow flexible sorting/filtering of quiz questions across different
 * quiz modes (themes, adaptive Leitner sessions, or custom sessions).
 *
 * <h2>Core Features</h2>
 * <ul>
 *   <li>Lambda-based sorting and filtering implementations</li>
 *   <li>Alphabetical sorting, date sorting, and fully customizable comparators</li>
 *   <li>Combined sort-and-filter pipeline using Java Streams</li>
 *   <li>Persistent configuration with automatic save/load via serialization</li>
 *   <li>Event-driven update notifications for UI or session delegates</li>
 *   <li>Thread-safe state management (controlled via immutable configurations)</li>
 * </ul>
 *
 * <h2>Integration Context</h2>
 * <p>The service is designed for use within modular quiz systems. It can:
 * <ul>
 *   <li>Filter questions by theme (topic or subject)</li>
 *   <li>Order Leitner cards (for spaced repetition scheduling)</li>
 *   <li>Manage quiz sessions by providing sorted lists of questions</li>
 *   <li>Integrate with GUI modules for dynamic sorting/filtering options</li>
 * </ul>
 *
 * <h2>Persistence</h2>
 * Sorting configurations (criteria, filters, and preferences) are persisted to disk
 * using Java serialization. This ensures user preferences are restored across sessions.
 *
 * @author
 *   D. Georgiou
 * @version
 *   1.0
 */
public class ModularSortingService implements SortingDelegate, Serializable {

    /** Serial version for safe serialization across versions. */
    private static final long serialVersionUID = 1L;

    /** File where sorting configurations are stored persistently. */
    private static final String SORTING_CONFIG_FILE = "sorting_config.dat";

    /** The currently active sorting configuration. */
    private SortingConfiguration currentConfig;

    /** The currently selected sorting criteria (e.g., alphabetical, date, custom). */
    private SortCriteria currentSortCriteria;

    /** The currently applied filtering criteria (e.g., text-based, date ranges, custom). */
    private FilterCriteria currentFilterCriteria;

    // === LAMBDA IMPLEMENTATIONS ===

    /** 
     * Lambda for alphabetical sorting of quiz questions.
     * Ensures that titles starting with {@code "*"} are prioritized,
     * followed by alphabetical ordering, with creation date as a final tiebreaker.
     */
    private Function<SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> alphabeticalSortImpl;

    /** 
     * Lambda for date-based sorting of quiz questions.
     * Ensures that starred titles remain prioritized, then alphabetical, then creation date.
     */
    private Function<SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> dateSortImpl;

    /** 
     * Lambda for applying a custom comparator with optional ascending/descending direction.
     */
    private BiFunction<Comparator<RepoQuizeeQuestions>, SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> customSortImpl;

    /** 
     * Lambda for text-based filtering (applies to title, question text, and answers).
     */
    private Function<String, Predicate<RepoQuizeeQuestions>> textFilterImpl;

    /** 
     * Lambda for filtering questions within a date range.
     */
    private Function<DateRange, Predicate<RepoQuizeeQuestions>> dateRangeFilterImpl;

    /** 
     * Combined sorting and filtering lambda pipeline.
     * First applies filters, then applies the specified sorting criteria.
     */
    private BiFunction<SortCriteria, FilterCriteria, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> sortAndFilterImpl;

    /** 
     * Lambda to persist sorting configurations to disk.
     */
    private Consumer<SortingConfiguration> saveSortingPreferencesImpl;

    /** 
     * Lambda to load persisted sorting configurations from disk.
     */
    private Supplier<SortingConfiguration> loadSortingPreferencesImpl;

    /** 
     * Event handler for sorting changes (updates criteria and persists configuration).
     */
    private Consumer<SortingChangeEvent> onSortingChangedImpl;

    /** 
     * Event handler for filter changes (updates criteria and persists configuration).
     */
    private Consumer<FilterChangeEvent> onFilterChangedImpl;

    /**
     * Constructs a new {@code ModularSortingService} with default lambdas,
     * and loads the last persisted configuration (or creates defaults).
     */
    public ModularSortingService() {
        initializeLambdas();
        loadOrCreateDefaultConfiguration();
    }

    /**
     * Initializes all lambda implementations for sorting, filtering, persistence,
     * and event handling.
     */
    private void initializeLambdas() {
        // === Alphabetical sorting with priority ===
        alphabeticalSortImpl = direction -> questions -> {
            List<RepoQuizeeQuestions> sorted = new ArrayList<>(questions);

            Comparator<RepoQuizeeQuestions> comparator = Comparator
                .comparing((RepoQuizeeQuestions q) -> !q.getTitel().startsWith("*"))
                .thenComparing(q -> q.getTitel().replaceFirst("^\\*", ""), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(RepoQuizeeQuestions::getCreatedAt);

            if (direction == SortDirection.DESCENDING) {
                comparator = comparator.reversed();
            }

            sorted.sort(comparator);
            return sorted;
        };

        // === Date sorting with priority ===
        dateSortImpl = direction -> questions -> {
            List<RepoQuizeeQuestions> sorted = new ArrayList<>(questions);

            Comparator<RepoQuizeeQuestions> comparator = Comparator
                .comparing((RepoQuizeeQuestions q) -> !q.getTitel().startsWith("*"))
                .thenComparing(q -> q.getTitel().replaceFirst("^\\*", ""), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(RepoQuizeeQuestions::getCreatedAt);

            if (direction == SortDirection.DESCENDING) {
                comparator = comparator.reversed();
            }

            sorted.sort(comparator);
            return sorted;
        };

        // === Custom sorting ===
        customSortImpl = (comparator, direction) -> questions -> {
            List<RepoQuizeeQuestions> sorted = new ArrayList<>(questions);
            Comparator<RepoQuizeeQuestions> finalComparator = comparator;

            if (direction == SortDirection.DESCENDING) {
                finalComparator = comparator.reversed();
            }

            sorted.sort(finalComparator);
            return sorted;
        };

        // === Text filter ===
        textFilterImpl = filterText -> question -> {
            if (filterText == null || filterText.trim().isEmpty()) {
                return true;
            }
            String filter = filterText.toLowerCase();
            return question.getTitel().toLowerCase().contains(filter) ||
                   question.getFrageText().toLowerCase().contains(filter) ||
                   question.getAntworten().stream().anyMatch(a -> a.toLowerCase().contains(filter));
        };

        // === Date range filter ===
        dateRangeFilterImpl = dateRange -> question -> {
            if (dateRange == null) return true;
            long qDate = question.getCreatedAt().toEpochSecond(ZoneOffset.UTC);
            return qDate >= dateRange.startDate && qDate <= dateRange.endDate;
        };

        // === Combined sorting and filtering ===
        sortAndFilterImpl = (sortCriteria, filterCriteria) -> questions -> {
            Stream<RepoQuizeeQuestions> stream = questions.stream();

            if (filterCriteria != null && filterCriteria.textFilter != null) {
                stream = stream.filter(textFilterImpl.apply(filterCriteria.textFilter));
            }
            if (filterCriteria != null && filterCriteria.dateRange != null) {
                stream = stream.filter(dateRangeFilterImpl.apply(filterCriteria.dateRange));
            }
            if (filterCriteria != null && filterCriteria.customFilters != null) {
                for (Predicate<RepoQuizeeQuestions> customFilter : filterCriteria.customFilters) {
                    stream = stream.filter(customFilter);
                }
            }

            List<RepoQuizeeQuestions> filtered = stream.collect(Collectors.toList());

            if (sortCriteria != null) {
                switch (sortCriteria.type) {
                    case ALPHABETICAL:
                        return alphabeticalSortImpl.apply(sortCriteria.direction).apply(filtered);
                    case DATE:
                        return dateSortImpl.apply(sortCriteria.direction).apply(filtered);
                    case CUSTOM:
                        if (sortCriteria.customComparator != null) {
                            return customSortImpl.apply(sortCriteria.customComparator, sortCriteria.direction).apply(filtered);
                        }
                        break;
                }
            }
            return filtered;
        };

        // === Persistence lambdas ===
        saveSortingPreferencesImpl = config -> {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SORTING_CONFIG_FILE))) {
                out.writeObject(config);
            } catch (IOException e) {
                System.err.println("Failed to save sorting configuration: " + e.getMessage());
            }
        };

        loadSortingPreferencesImpl = () -> {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SORTING_CONFIG_FILE))) {
                Object obj = in.readObject();
                if (obj instanceof SortingConfiguration) {
                    return (SortingConfiguration) obj;
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("No saved configuration found; using defaults.");
            }
            return createDefaultConfiguration();
        };

        // === Event lambdas ===
        onSortingChangedImpl = event -> {
            this.currentSortCriteria = event.newCriteria;
            updateConfiguration();
        };

        onFilterChangedImpl = event -> {
            this.currentFilterCriteria = event.newCriteria;
            updateConfiguration();
        };
    }

    // === INTERFACE IMPLEMENTATIONS ===
    @Override public Function<SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> alphabeticalSort() { return alphabeticalSortImpl; }
    @Override public Function<SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> dateSort() { return dateSortImpl; }
    @Override public BiFunction<Comparator<RepoQuizeeQuestions>, SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> customSort() { return customSortImpl; }
    @Override public Function<String, Predicate<RepoQuizeeQuestions>> textFilter() { return textFilterImpl; }
    @Override public Function<DateRange, Predicate<RepoQuizeeQuestions>> dateRangeFilter() { return dateRangeFilterImpl; }
    @Override public BiFunction<SortCriteria, FilterCriteria, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> sortAndFilter() { return sortAndFilterImpl; }
    @Override public Consumer<SortingConfiguration> saveSortingPreferences() { return saveSortingPreferencesImpl; }
    @Override public Supplier<SortingConfiguration> loadSortingPreferences() { return loadSortingPreferencesImpl; }
    @Override public Consumer<SortingChangeEvent> onSortingChanged() { return onSortingChangedImpl; }
    @Override public Consumer<FilterChangeEvent> onFilterChanged() { return onFilterChangedImpl; }

    // === UTILITY METHODS ===

    /**
     * Loads configuration from disk or creates a default configuration if none exists.
     */
    private void loadOrCreateDefaultConfiguration() {
        this.currentConfig = loadSortingPreferencesImpl.get();
        this.currentSortCriteria = currentConfig.defaultSortCriteria;
        this.currentFilterCriteria = currentConfig.defaultFilterCriteria;
    }

    /**
     * Creates a default configuration (alphabetical ascending, no filters).
     *
     * @return default {@link SortingConfiguration}
     */
    private SortingConfiguration createDefaultConfiguration() {
        SortCriteria defaultSort = new SortCriteria(SortType.ALPHABETICAL, SortDirection.ASCENDING);
        FilterCriteria defaultFilter = new FilterCriteria(null, null, null);
        return new SortingConfiguration(defaultSort, defaultFilter, true);
    }

    /**
     * Updates current configuration in memory and persists it.
     */
    private void updateConfiguration() {
        this.currentConfig = new SortingConfiguration(currentSortCriteria, currentFilterCriteria, true);
        saveSortingPreferencesImpl.accept(currentConfig);
    }

    // === PUBLIC CONVENIENCE METHODS ===

    /** @return current sorting configuration */
    public SortingConfiguration getCurrentConfiguration() { return currentConfig; }

    /** @return currently active sorting criteria */
    public SortCriteria getCurrentSortCriteria() { return currentSortCriteria; }

    /** @return currently active filter criteria */
    public FilterCriteria getCurrentFilterCriteria() { return currentFilterCriteria; }

    /**
     * Applies the current sorting and filtering configuration to a given list of questions.
     *
     * @param questions list of {@link RepoQuizeeQuestions}
     * @return sorted and filtered list
     */
    public List<RepoQuizeeQuestions> applySortingAndFiltering(List<RepoQuizeeQuestions> questions) {
        return sortAndFilterImpl.apply(currentSortCriteria, currentFilterCriteria).apply(questions);
    }

    /**
     * Updates sorting criteria, triggers event, and persists configuration.
     *
     * @param newCriteria new {@link SortCriteria} to apply
     */
    public void updateSortCriteria(SortCriteria newCriteria) {
        SortCriteria oldCriteria = this.currentSortCriteria;
        onSortingChangedImpl.accept(new SortingChangeEvent(oldCriteria, newCriteria));
    }

    /**
     * Toggles the sorting direction (ascending ↔ descending) while keeping the same sort type.
     */
    public void toggleSortDirection() {
        SortCriteria newCriteria = new SortCriteria(
            currentSortCriteria.type,
            currentSortCriteria.direction.toggle(),
            currentSortCriteria.customComparator
        );
        updateSortCriteria(newCriteria);
    }
}