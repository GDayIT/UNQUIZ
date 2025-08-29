package guimodule;

/**
 * Enumeration representing the direction of sorting for lists or tables.
 * <p>
 * This enum provides human-readable labels for both alphabetical and date sorting.
 * It is separated from the interface to ensure Eclipse compatibility and avoid
 * special characters ($) in generated class names.
 * <p>
 * Typical usage:
 * <ul>
 *     <li>Sorting quiz questions alphabetically by title</li>
 *     <li>Sorting quiz questions chronologically by creation date</li>
 *     <li>Toggle between ascending and descending order</li>
 * </ul>
 * <p>
 * Integration:
 * <ul>
 *     <li>Used in {@link SortCriteria} to define sorting rules</li>
 *     <li>Applicable to GUI components displaying question lists or sessions</li>
 *     <li>Supports Leitner cards and quiz statistics sorted by date or title</li>
 * </ul>
 * 
 * @author D.
 * Georgiou
 * @version 1.0
 */
public enum SortDirection {

    /**
     * Ascending order: alphabetical (A→Z) or chronological (Earliest→Latest).
     */
    ASCENDING("A→Z", "Earliest→Latest"),

    /**
     * Descending order: alphabetical (Z→A) or chronological (Latest→Earliest).
     */
    DESCENDING("Z→A", "Latest→Earliest");

    /**
     * Label to describe alphabetical ascending/descending order for UI display.
     */
    public final String alphabeticalLabel;

    /**
     * Label to describe chronological ascending/descending order for UI display.
     */
    public final String dateLabel;

    /**
     * Constructor to initialize enum labels for alphabetical and date sorting.
     * 
     * @param alphabeticalLabel human-readable label for alphabetical sorting
     * @param dateLabel human-readable label for date sorting
     */
    SortDirection(String alphabeticalLabel, String dateLabel) {
        this.alphabeticalLabel = alphabeticalLabel;
        this.dateLabel = dateLabel;
    }

    /**
     * Returns the opposite sort direction.
     * <p>
     * Useful for toggle buttons or UI elements where users switch
     * between ascending and descending order.
     * 
     * @return the opposite {@link SortDirection}
     */
    public SortDirection toggle() {
        return this == ASCENDING ? DESCENDING : ASCENDING;
    }
}