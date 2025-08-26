package guimodule;

import dbbl.RepoQuizeeQuestions;
import java.util.List;
import java.util.function.Predicate;

/**
 * Filter criteria configuration for Eclipse compatibility.
 * Separated from interface to avoid $ in class names.
 */
public class FilterCriteria {
    public final String textFilter;
    public final DateRange dateRange;
    public final List<Predicate<RepoQuizeeQuestions>> customFilters;
    
    public FilterCriteria(String textFilter, DateRange dateRange, List<Predicate<RepoQuizeeQuestions>> customFilters) {
        this.textFilter = textFilter;
        this.dateRange = dateRange;
        this.customFilters = customFilters != null ? customFilters : List.of();
    }
}
