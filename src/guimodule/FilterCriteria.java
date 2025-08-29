package guimodule;

import dbbl.RepoQuizeeQuestions;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents filter criteria for quiz questions, including text-based filtering,
 * date range filtering, and custom predicate-based filtering.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Encapsulates all filtering parameters applied to questions or other entities.</li>
 *   <li>Supports text search via {@link #textFilter}.</li>
 *   <li>Supports date range filtering via {@link #dateRange}.</li>
 *   <li>Supports arbitrary custom filtering rules via {@link #customFilters}.</li>
 * </ul>
 * <p>
 * Design considerations:
 * <ul>
 *   <li>Immutable by design: all fields are {@code final}.</li>
 *   <li>Compatible with Eclipse and avoids $ in class names.</li>
 *   <li>Designed for use in GUI modules for Themes, Questions, Leitner Cards, and Sessions filtering.</li>
 * </ul>
 * 
 * Example usage:
 * <pre>
 * DateRange range = new DateRange(startMillis, endMillis);
 * List<Predicate&lt;RepoQuizeeQuestions&gt;&gt; custom = List.of(q -&gt; q.isActive());
 * FilterCriteria criteria = new FilterCriteria("quiz", range, custom);
 * </pre>
 * 
 * @author D.
 * @version 1.0
 */
public class FilterCriteria {

    /**
     * A string to filter question titles, explanations, or other text fields.
     * <p>
     * This filter is applied as a simple substring match or can be used in
     * combination with custom predicates.
     */
    public final String textFilter;

    /**
     * Optional date range used to filter questions by creation or modification date.
     * <p>
     * If null, no date filtering is applied.
     */
    public final DateRange dateRange;

    /**
     * A list of arbitrary predicates that can be applied to {@link RepoQuizeeQuestions}.
     * <p>
     * Allows for complex filtering scenarios beyond simple text or date range filters.
     * Immutable and initialized as an empty list if null is provided.
     */
    public final List<Predicate<RepoQuizeeQuestions>> customFilters;

    /**
     * Constructs a new FilterCriteria object with the given parameters.
     * 
     * @param textFilter    the text filter to apply to questions
     * @param dateRange     the optional date range filter
     * @param customFilters a list of custom predicates to apply to questions; may be null
     */
    public FilterCriteria(String textFilter, DateRange dateRange, List<Predicate<RepoQuizeeQuestions>> customFilters) {
        this.textFilter = textFilter;
        this.dateRange = dateRange;
        this.customFilters = customFilters != null ? customFilters : List.of();
    }

    /**
     * Returns a string representation of the filter criteria, useful for debugging.
     * Includes text filter, date range, and number of custom filters.
     * 
     * @return a string summary of the filter criteria
     */
    @Override
    public String toString() {
        return "FilterCriteria[textFilter=" + textFilter +
               ", dateRange=" + dateRange +
               ", customFiltersCount=" + customFilters.size() + "]";
    }
}