package guimodule;

import dbbl.RepoQuizeeQuestions;
import java.util.Comparator;

/**
 * Sort criteria configuration for Eclipse compatibility.
 * Separated from interface to avoid $ in class names.
 */
public class SortCriteria {
    public final SortType type;
    public final SortDirection direction;
    public final Comparator<RepoQuizeeQuestions> customComparator;
    
    public SortCriteria(SortType type, SortDirection direction) {
        this(type, direction, null);
    }
    
    public SortCriteria(SortType type, SortDirection direction, Comparator<RepoQuizeeQuestions> customComparator) {
        this.type = type;
        this.direction = direction;
        this.customComparator = customComparator;
    }
}
