package guimodule;

import dbbl.RepoQuizeeQuestions;
import java.util.Comparator;

/**
 * SortCriteria defines sorting rules for quiz questions in lists or tables.
 * <p>
 * This class encapsulates the type of sorting, direction, and optional custom comparator.
 * It is separated from the interface to ensure Eclipse compatibility and avoid special 
 * characters in generated class names.
 * <p>
 * Use cases:
 * <ul>
 *     <li>Sorting quiz questions by title, creation date, or topic</li>
 *     <li>Configuring ascending/descending order for lists or tables</li>
 *     <li>Providing custom comparators for advanced sorting logic</li>
 * </ul>
 * <p>
 * This class is immutable: all fields are final and can only be set via constructor.
 * Integrates with:
 * <ul>
 *     <li>{@link RepoQuizeeQuestions} for repository-level sorting</li>
 *     <li>GUI lists and tables displaying quiz questions</li>
 *     <li>Leitner Cards and Quiz Sessions when sorting questions dynamically</li>
 * </ul>
 * 
 * @author D.
 * Georgiou
 * @version 1.0
 */
public class SortCriteria {

    /**
     * Defines the attribute by which the questions will be sorted.
     */
    public final SortType type;

    /**
     * Defines the sorting direction (ascending/descending).
     */
    public final SortDirection direction;

    /**
     * Optional custom comparator for advanced sorting logic.
     * If provided, it overrides default sorting by type.
     */
    public final Comparator<RepoQuizeeQuestions> customComparator;

    /**
     * Constructor without custom comparator.
     *
     * @param type the attribute to sort by
     * @param direction the direction (ascending or descending)
     */
    public SortCriteria(SortType type, SortDirection direction) {
        this(type, direction, null);
    }

    /**
     * Constructor with optional custom comparator.
     *
     * @param type the attribute to sort by
     * @param direction the direction (ascending or descending)
     * @param customComparator optional comparator for advanced sorting
     */
    public SortCriteria(SortType type, SortDirection direction, Comparator<RepoQuizeeQuestions> customComparator) {
        this.type = type;
        this.direction = direction;
        this.customComparator = customComparator;
    }
}