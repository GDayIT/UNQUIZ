package guimodule;

/**
 * Represents a change in sorting criteria within the application.
 * <p>
 * This class is used to encapsulate the old and new {@link SortCriteria}
 * whenever a sorting event occurs, for example when a user changes
 * the sort order of quiz questions or Leitner cards.
 * <p>
 * It includes a timestamp to track when the change occurred, allowing
 * session management, history tracking, or UI animations based on sort events.
 * <p>
 * Design notes:
 * <ul>
 *     <li>Separated from interfaces for Eclipse compatibility to avoid $ in class names.</li>
 *     <li>Immutable fields ensure the event object is thread-safe and predictable.</li>
 *     <li>Can be used in event listeners for GUI components, quizzes, statistics, and sessions.</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>
 *     SortingChangeEvent event = new SortingChangeEvent(oldCriteria, newCriteria);
 *     listener.onSortingChanged(event);
 * </pre>
 * 
 * @author D.
 * Georgiou
 * @version 1.0
 */
public class SortingChangeEvent {

    /**
     * The previous sorting criteria before the change.
     */
    public final SortCriteria oldCriteria;

    /**
     * The new sorting criteria after the change.
     */
    public final SortCriteria newCriteria;

    /**
     * Timestamp (in milliseconds since epoch) when this sorting change occurred.
     * Useful for session tracking, audit, or UI state history.
     */
    public final long timestamp;

    /**
     * Constructs a new sorting change event.
     *
     * @param oldCriteria the previous sort criteria
     * @param newCriteria the new sort criteria
     */
    public SortingChangeEvent(SortCriteria oldCriteria, SortCriteria newCriteria) {
        this.oldCriteria = oldCriteria;
        this.newCriteria = newCriteria;
        this.timestamp = System.currentTimeMillis();
    }
}