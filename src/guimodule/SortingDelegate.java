package guimodule;

import dbbl.RepoQuizeeQuestions;
import java.util.Comparator;
import java.util.List;
import java.util.function.*;

/**
 * Modular sorting delegation interface using lambda expressions.
 * 
 * This interface provides complete modularity for sorting and filtering operations:
 * - Lambda-based sorting functions for different criteria
 * - Functional composition for complex sorting operations
 * - Event-driven sorting with callbacks
 * - Persistent sorting preferences with serialization
 * 
 * All sorting operations are defined as functional interfaces to support:
 * - Runtime sorting behavior modification through lambdas
 * - Functional composition for multi-criteria sorting
 * - Event-driven sorting updates
 * - Persistent sorting configurations
 * 
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
=======
 * @author Quiz Application Team
 * @version 2.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public interface SortingDelegate {
    
    // === SORTING LAMBDAS ===
    
    /**
     * Lambda for alphabetical sorting.
     * Function takes sort direction and returns sorting function.
     */
    Function<SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> alphabeticalSort();
    
    /**
     * Lambda for date-based sorting.
     * Function takes sort direction and returns sorting function.
     */
    Function<SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> dateSort();
    
    /**
     * Lambda for custom sorting.
     * Function takes (comparator, direction) and returns sorting function.
     */
    BiFunction<Comparator<RepoQuizeeQuestions>, SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> customSort();
    
    // === FILTERING LAMBDAS ===
    
    /**
     * Lambda for text-based filtering.
     * Function takes filter text and returns filtering function.
     */
    Function<String, Predicate<RepoQuizeeQuestions>> textFilter();
    
    /**
     * Lambda for date range filtering.
     * Function takes date range and returns filtering function.
     */
    Function<DateRange, Predicate<RepoQuizeeQuestions>> dateRangeFilter();
    
    // === COMBINED OPERATIONS ===
    
    /**
     * Lambda for sort and filter combination.
     * Function takes (sortCriteria, filterCriteria) and returns combined operation.
     */
    BiFunction<SortCriteria, FilterCriteria, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> sortAndFilter();
    
    // === PERSISTENCE LAMBDAS ===
    
    /**
     * Lambda for saving sorting preferences.
     * Consumer saves sorting configuration to storage.
     */
    Consumer<SortingConfiguration> saveSortingPreferences();
    
    /**
     * Lambda for loading sorting preferences.
     * Supplier loads sorting configuration from storage.
     */
    Supplier<SortingConfiguration> loadSortingPreferences();
    
    // === EVENT LAMBDAS ===
    
    /**
     * Lambda for sorting change notifications.
     * Consumer receives sorting change events.
     */
    Consumer<SortingChangeEvent> onSortingChanged();
    
    /**
     * Lambda for filter change notifications.
     * Consumer receives filter change events.
     */
    Consumer<FilterChangeEvent> onFilterChanged();
    
    // === NOTE: All data classes moved to separate files for Eclipse compatibility ===
    // See: SortDirection.java, SortType.java, SortCriteria.java, FilterCriteria.java,
    //      DateRange.java, SortingConfiguration.java, SortingChangeEvent.java, FilterChangeEvent.java
}
