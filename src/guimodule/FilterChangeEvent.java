package guimodule;

/**
 * Represents a filter change event in the system, tracking the previous
 * and new filtering criteria along with the timestamp of the change.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Encapsulates information about a change in filtering criteria.</li>
 *   <li>Tracks both the old and new {@link FilterCriteria} objects for comparison.</li>
 *   <li>Provides a timestamp for when the filter change occurred.</li>
 * </ul>
 * <p>
 * Design considerations:
 * <ul>
 *   <li>Immutable by design: all fields are final.</li>
 *   <li>Separation from interface avoids issues with special characters in class names ($).</li>
 *   <li>Intended for use in GUI modules, sorting/filtering services, and business logic events.</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 *   FilterChangeEvent event = new FilterChangeEvent(oldFilter, newFilter);
 *   System.out.println("Filter changed at: " + event.timestamp);
 * </pre>
 * 
 * @author D.
 * @version 1.0
 */
public class FilterChangeEvent {

    /**
     * The previous filter criteria before the change occurred.
     * This allows comparison and potential undo functionality.
     */
    public final FilterCriteria oldCriteria;

    /**
     * The new filter criteria that replaced the old criteria.
     * This represents the current state of filtering in the system.
     */
    public final FilterCriteria newCriteria;

    /**
     * The timestamp when the filter change event was created.
     * Represented in milliseconds since epoch (UTC).
     */
    public final long timestamp;

    /**
     * Constructs a new FilterChangeEvent with specified old and new filter criteria.
     * Automatically records the creation time as a timestamp.
     * 
     * @param oldCriteria the previous filter criteria
     * @param newCriteria the new filter criteria
     */
    public FilterChangeEvent(FilterCriteria oldCriteria, FilterCriteria newCriteria) {
        this.oldCriteria = oldCriteria;
        this.newCriteria = newCriteria;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Returns a string representation of the filter change event, including
     * old criteria, new criteria, and timestamp.
     * 
     * @return string in the format: "FilterChangeEvent[old=..., new=..., timestamp=...]"
     */
    @Override
    public String toString() {
        return "FilterChangeEvent[old=" + oldCriteria + ", new=" + newCriteria
                + ", timestamp=" + timestamp + "]";
    }
}