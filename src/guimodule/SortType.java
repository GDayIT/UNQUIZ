package guimodule;

/**
 * Enumeration representing the type of sorting applied to quiz questions.
 * <p>
 * This enum is designed for Eclipse compatibility and is separated from any interfaces
 * to avoid $-sign issues in class names.
 * <p>
 * The supported sort types include:
 * <ul>
 *     <li><b>ALPHABETICAL:</b> Sorts questions in alphabetical order by title.</li>
 *     <li><b>DATE:</b> Sorts questions by creation or modification date.</li>
 *     <li><b>CUSTOM:</b> Allows user-defined custom sorting (can be implemented with a Comparator).</li>
 * </ul>
 * Each type includes a human-readable label and an optional icon for GUI representation.
 * <p>
 * This enum integrates with {@link SortingDelegate}, {@link SortCriteria}, and other
 * sorting-related classes to provide modular, lambda-driven sorting functionality.
 * 
 * @author D.Georgiou
 * @version 1.0
 */
public enum SortType {
    
    /** Alphabetical sorting (title-based, A→Z) */
    ALPHABETICAL("Alphabetical", "🔤"),
    
    /** Date-based sorting (earliest to latest or vice versa) */
    DATE("Date", "📅"),
    
    /** Custom sorting using user-defined Comparator */
    CUSTOM("Custom", "⚙️"); // can be implemented if needed
    
    /** Human-readable label for the sort type */
    public final String label;
    
    /** Icon representation for GUI display (optional) */
    public final String icon;
    
    /**
     * Constructs a new SortType with a label and icon.
     * 
     * @param label human-readable label
     * @param icon  icon representation (can be used in GUI)
     */
    SortType(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }
}