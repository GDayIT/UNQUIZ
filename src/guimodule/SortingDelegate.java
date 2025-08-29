package guimodule;

import dbbl.RepoQuizeeQuestions;
import java.util.Comparator;
import java.util.List;
import java.util.function.*;

/**
 * Modular sorting and filtering delegation interface using lambda expressions.
 * <p>
 * This interface provides complete modularity for sorting and filtering operations
 * in a GUI quiz application with persistent configurations and event-driven updates.
 * All operations are designed as functional interfaces to support:
 * <ul>
 *     <li>Runtime sorting behavior modification through lambdas</li>
 *     <li>Functional composition for multi-criteria sorting</li>
 *     <li>Event-driven updates for UI components (lists, Leitner cards, statistics)</li>
 *     <li>Persistence of user preferences (sorting and filtering)</li>
 * </ul>
 * 
 * <p>Key categories:</p>
 * <ul>
 *     <li><b>Sorting lambdas:</b> alphabetical, date-based, custom comparator</li>
 *     <li><b>Filtering lambdas:</b> text-based, date-range based</li>
 *     <li><b>Combined operations:</b> sorting + filtering composition</li>
 *     <li><b>Persistence:</b> saving and loading sorting configurations</li>
 *     <li><b>Events:</b> notifications for sort and filter changes</li>
 * </ul>
 * 
 * <p>All related data classes are moved to separate files for Eclipse compatibility.</p>
 * 
 * @author D.Georgiou
 * @version 1.0
 * 
 */
public interface SortingDelegate {
    
    // === SORTING LAMBDAS ===
    
    /**
     * Lambda for alphabetical sorting of repository questions.
     * <p>
     * Returns a function that takes a list and returns a new sorted list according to the specified {@link SortDirection}.
     * 
     * @return Function that maps SortDirection to a sorting function for lists of RepoQuizeeQuestions
     */
    Function<SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> alphabeticalSort();
    
    /**
     * Lambda for date-based sorting of repository questions.
     * <p>
     * Returns a function that takes a list and returns a new sorted list by creation or modification date.
     * 
     * @return Function that maps SortDirection to a date-sorting function
     */
    Function<SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> dateSort();
    
    /**
     * Lambda for custom comparator sorting.
     * <p>
     * Allows runtime sorting using a provided Comparator and SortDirection.
     * 
     * @return BiFunction mapping (Comparator, SortDirection) to a sorting function
     */
    BiFunction<Comparator<RepoQuizeeQuestions>, SortDirection, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> customSort();
    
    // === FILTERING LAMBDAS ===
    
    /**
     * Lambda for text-based filtering.
     * <p>
     * Returns a predicate that evaluates whether a repository question matches the filter text.
     * 
     * @return Function mapping filter string to a Predicate for RepoQuizeeQuestions
     */
    Function<String, Predicate<RepoQuizeeQuestions>> textFilter();
    
    /**
     * Lambda for filtering by date range.
     * <p>
     * Returns a predicate that checks if a question falls within a specific date range.
     * 
     * @return Function mapping DateRange to a Predicate for RepoQuizeeQuestions
     */
    Function<DateRange, Predicate<RepoQuizeeQuestions>> dateRangeFilter();
    
    // === COMBINED OPERATIONS ===
    
    /**
     * Lambda for combined sorting and filtering.
     * <p>
     * Returns a function that applies both sorting and filtering to a list according to {@link SortCriteria} and {@link FilterCriteria}.
     * 
     * @return BiFunction mapping (SortCriteria, FilterCriteria) to a function operating on lists of RepoQuizeeQuestions
     */
    BiFunction<SortCriteria, FilterCriteria, Function<List<RepoQuizeeQuestions>, List<RepoQuizeeQuestions>>> sortAndFilter();
    
    // === PERSISTENCE LAMBDAS ===
    
    /**
     * Lambda for persisting sorting configuration.
     * <p>
     * Saves {@link SortingConfiguration} to storage or database.
     */
    Consumer<SortingConfiguration> saveSortingPreferences();
    
    /**
     * Lambda for loading saved sorting configuration.
     * <p>
     * Returns the last saved {@link SortingConfiguration} for restoring UI state.
     */
    Supplier<SortingConfiguration> loadSortingPreferences();
    
    // === EVENT LAMBDAS ===
    
    /**
     * Lambda for broadcasting sorting change events.
     * <p>
     * Consumers receive {@link SortingChangeEvent} whenever sorting criteria changes.
     */
    Consumer<SortingChangeEvent> onSortingChanged();
    
    /**
     * Lambda for broadcasting filter change events.
     * <p>
     * Consumers receive {@link FilterChangeEvent} whenever filter criteria changes.
     */
    Consumer<FilterChangeEvent> onFilterChanged();
    


    
    // === NOTE: All data classes moved to separate files for Eclipse compatibility ===
    // See: SortDirection.java, SortType.java, SortCriteria.java, FilterCriteria.java,
    //      DateRange.java, SortingConfiguration.java, SortingChangeEvent.java, FilterChangeEvent.java
}