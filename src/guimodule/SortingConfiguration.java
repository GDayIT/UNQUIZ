package guimodule;

import java.io.Serializable;

/**
 * Encapsulates the complete sorting and filtering configuration for persistence.
 * <p>
 * This class provides a serializable representation of:
 * <ul>
 *     <li>Default sorting criteria ({@link SortCriteria})</li>
 *     <li>Default filter criteria ({@link FilterCriteria})</li>
 *     <li>Whether the application should remember the last sort</li>
 *     <li>Timestamp of when the configuration was created</li>
 * </ul>
 * <p>
 * Design notes:
 * <ul>
 *     <li>Separated from interface for Eclipse compatibility to avoid $ in class names.</li>
 *     <li>Immutable public final fields for thread safety and consistent state.</li>
 *     <li>Intended for GUI lists, Leitner cards, quiz sessions, and statistics persistence.</li>
 * </ul>
 * 
 * Usage example:
 * <pre>
 *     SortingConfiguration config = new SortingConfiguration(defaultSort, defaultFilter, true);
 *     persistence.save(config);
 * </pre>
 * 
 * Fields can be used to restore previous UI states, remember last sort, and replay sessions.
 * 
 * @author D.
 * Georgiou
 * @version 1.0
 */
public class SortingConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default sorting criteria for lists or tables.
     */
    public final SortCriteria defaultSortCriteria;

    /**
     * Default filtering criteria applied to lists, quizzes, or Leitner cards.
     */
    public final FilterCriteria defaultFilterCriteria;

    /**
     * Flag indicating if the last sort and filter state should be remembered.
     * Useful for session restoration or returning users.
     */
    public final boolean rememberLastSort;

    /**
     * Timestamp (in milliseconds since epoch) when this configuration was created.
     * Useful for versioning or auditing.
     */
    public final long timestamp;

    /**
     * Constructs a new sorting configuration.
     *
     * @param sortCriteria the default sorting criteria
     * @param filterCriteria the default filtering criteria
     * @param rememberLastSort whether to remember the last sort/filter state
     */
    public SortingConfiguration(SortCriteria sortCriteria, FilterCriteria filterCriteria, boolean rememberLastSort) {
        this.defaultSortCriteria = sortCriteria;
        this.defaultFilterCriteria = filterCriteria;
        this.rememberLastSort = rememberLastSort;
        this.timestamp = System.currentTimeMillis();
    }
}