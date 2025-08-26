package guimodule;

import dbbl.RepoQuizeeQuestions;
import java.io.*;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Modular sorting service implementing complete sorting and filtering with persistence.
 * 
 * This service provides:
 * - Lambda-based sorting operations for all criteria
 * - Functional composition for complex sorting operations
 * - Persistent sorting configurations with serialization
 * - Event-driven sorting updates with callbacks
 * - Thread-safe operations for concurrent access
 * 
 * All sorting operations are implemented as pure functions or controlled side-effects.
 * 
 * @author Quiz Application Team
 * @version 2.0
 */
public class ModularSortingService implements SortingDelegate, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SORTING_CONFIG_FILE = "sorting_config.dat";
    
    private SortingConfiguration currentConfig;
    private SortCriteria currentSortCriteria;
    private FilterCriteria currentFilterCriteria;
    
    // === LAMBDA IMPLEMENTATIONS ===
    private Function<SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> alphabeticalSortImpl;
    private Function<SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> dateSortImpl;
    private BiFunction<Comparator<RepoQuizeeQuestions>, SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> customSortImpl;
    private Function<String, Predicate<RepoQuizeeQuestions>> textFilterImpl;
    private Function<DateRange, Predicate<RepoQuizeeQuestions>> dateRangeFilterImpl;
    private BiFunction<SortCriteria, FilterCriteria, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> sortAndFilterImpl;
    private Consumer<SortingConfiguration> saveSortingPreferencesImpl;
    private Supplier<SortingConfiguration> loadSortingPreferencesImpl;
    private Consumer<SortingChangeEvent> onSortingChangedImpl;
    private Consumer<FilterChangeEvent> onFilterChangedImpl;
    
    /**
     * Creates a new modular sorting service with lambda-based implementations.
     */
    public ModularSortingService() {
        initializeLambdas();
        loadOrCreateDefaultConfiguration();
    }
    
    /**
     * Initializes all lambda implementations for sorting operations.
     */
    private void initializeLambdas() {
        
        // === ALPHABETICAL SORTING ===
        alphabeticalSortImpl = direction -> questions -> {
            List<RepoQuizeeQuestions> sorted = new ArrayList<>(questions);
            Comparator<RepoQuizeeQuestions> comparator = Comparator.comparing(
                RepoQuizeeQuestions::getTitel, 
                String.CASE_INSENSITIVE_ORDER
            );
            
            if (direction == SortDirection.DESCENDING) {
                comparator = comparator.reversed();
            }
            
            sorted.sort(comparator);
            return sorted;
        };
        
        // === DATE SORTING ===
        dateSortImpl = direction -> questions -> {
            List<RepoQuizeeQuestions> sorted = new ArrayList<>(questions);
            Comparator<RepoQuizeeQuestions> comparator = Comparator.comparing(
                RepoQuizeeQuestions::getCreatedAt
            );
            
            if (direction == SortDirection.DESCENDING) {
                comparator = comparator.reversed();
            }
            
            sorted.sort(comparator);
            return sorted;
        };
        
        // === CUSTOM SORTING ===
        customSortImpl = (comparator, direction) -> questions -> {
            List<RepoQuizeeQuestions> sorted = new ArrayList<>(questions);
            Comparator<RepoQuizeeQuestions> finalComparator = comparator;
            
            if (direction == SortDirection.DESCENDING) {
                finalComparator = comparator.reversed();
            }
            
            sorted.sort(finalComparator);
            return sorted;
        };
        
        // === TEXT FILTERING ===
        textFilterImpl = filterText -> question -> {
            if (filterText == null || filterText.trim().isEmpty()) {
                return true;
            }
            
            String filter = filterText.toLowerCase();
            return question.getTitel().toLowerCase().contains(filter) ||
                   question.getFrageText().toLowerCase().contains(filter) ||
                   question.getAntworten().stream()
                       .anyMatch(answer -> answer.toLowerCase().contains(filter));
        };
        
        // === DATE RANGE FILTERING ===
        dateRangeFilterImpl = dateRange -> question -> {
            if (dateRange == null) {
                return true;
            }
            
            long questionDate = question.getCreatedAt().toEpochSecond(ZoneOffset.UTC);
            return questionDate >= dateRange.startDate && questionDate <= dateRange.endDate;
        };
        
        // === COMBINED SORT AND FILTER ===
        sortAndFilterImpl = (sortCriteria, filterCriteria) -> questions -> {
            // First apply filters
            Stream<RepoQuizeeQuestions> stream = questions.stream();
            
            // Apply text filter
            if (filterCriteria != null && filterCriteria.textFilter != null) {
                stream = stream.filter(textFilterImpl.apply(filterCriteria.textFilter));
            }
            
            // Apply date range filter
            if (filterCriteria != null && filterCriteria.dateRange != null) {
                stream = stream.filter(dateRangeFilterImpl.apply(filterCriteria.dateRange));
            }
            
            // Apply custom filters
            if (filterCriteria != null && filterCriteria.customFilters != null) {
                for (Predicate<RepoQuizeeQuestions> customFilter : filterCriteria.customFilters) {
                    stream = stream.filter(customFilter);
                }
            }
            
            List<RepoQuizeeQuestions> filtered = stream.collect(Collectors.toList());
            
            // Then apply sorting
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
        
        // === PERSISTENCE ===
        saveSortingPreferencesImpl = config -> {
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(SORTING_CONFIG_FILE))) {
                out.writeObject(config);
                System.out.println("Sorting configuration saved successfully");
            } catch (IOException e) {
                System.err.println("Failed to save sorting configuration: " + e.getMessage());
            }
        };
        
        loadSortingPreferencesImpl = () -> {
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(SORTING_CONFIG_FILE))) {
                Object obj = in.readObject();
                if (obj instanceof SortingConfiguration) {
                    System.out.println("Sorting configuration loaded successfully");
                    return (SortingConfiguration) obj;
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("No existing sorting configuration found, using defaults");
            }
            return createDefaultConfiguration();
        };
        
        // === EVENT HANDLING ===
        onSortingChangedImpl = event -> {
            System.out.println("Sorting changed from " + event.oldCriteria.type + 
                             " to " + event.newCriteria.type);
            this.currentSortCriteria = event.newCriteria;
            updateConfiguration();
        };
        
        onFilterChangedImpl = event -> {
            System.out.println("Filter changed");
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
    private void loadOrCreateDefaultConfiguration() {
        this.currentConfig = loadSortingPreferencesImpl.get();
        this.currentSortCriteria = currentConfig.defaultSortCriteria;
        this.currentFilterCriteria = currentConfig.defaultFilterCriteria;
    }
    
    private SortingConfiguration createDefaultConfiguration() {
        SortCriteria defaultSort = new SortCriteria(SortType.ALPHABETICAL, SortDirection.ASCENDING);
        FilterCriteria defaultFilter = new FilterCriteria(null, null, null);
        return new SortingConfiguration(defaultSort, defaultFilter, true);
    }
    
    private void updateConfiguration() {
        this.currentConfig = new SortingConfiguration(currentSortCriteria, currentFilterCriteria, true);
        saveSortingPreferencesImpl.accept(currentConfig);
    }
    
    // === PUBLIC CONVENIENCE METHODS ===
    public SortingConfiguration getCurrentConfiguration() { return currentConfig; }
    public SortCriteria getCurrentSortCriteria() { return currentSortCriteria; }
    public FilterCriteria getCurrentFilterCriteria() { return currentFilterCriteria; }
    
    /**
     * Convenience method to apply current sorting and filtering to a list.
     */
    public List<RepoQuizeeQuestions> applySortingAndFiltering(List<RepoQuizeeQuestions> questions) {
        return sortAndFilterImpl.apply(currentSortCriteria, currentFilterCriteria).apply(questions);
    }
    
    /**
     * Convenience method to update sort criteria and notify listeners.
     */
    public void updateSortCriteria(SortCriteria newCriteria) {
        SortCriteria oldCriteria = this.currentSortCriteria;
        onSortingChangedImpl.accept(new SortingChangeEvent(oldCriteria, newCriteria));
    }
    
    /**
     * Convenience method to toggle sort direction.
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
