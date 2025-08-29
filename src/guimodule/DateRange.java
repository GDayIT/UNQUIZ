package guimodule;

/**
 * Represents a date range with a start and end timestamp.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Encapsulates a period of time defined by start and end dates (in milliseconds since epoch).</li>
 *   <li>Used for filtering quiz questions, Leitner sessions, or other entities by date.</li>
 *   <li>Provides immutable fields to ensure safe usage in multithreaded or UI environments.</li>
 * </ul>
 * <p>
 * Design considerations:
 * <ul>
 *   <li>Separated from interfaces to avoid issues with special characters in class names ($).</li>
 *   <li>Immutable by design (fields are final) for thread safety.</li>
 *   <li>Simple, lightweight DTO-like structure without methods beyond the constructor.</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 *   DateRange filterRange = new DateRange(System.currentTimeMillis() - 7*24*60*60*1000, System.currentTimeMillis());
 * </pre>
 * This creates a range for the last 7 days.
 * 
 * @author D.
 * @version 1.0
 */
public class DateRange {

    /**
     * The start timestamp of the range (inclusive).
     * Represented as milliseconds since epoch (UTC).
     */
    public final long startDate;

    /**
     * The end timestamp of the range (inclusive).
     * Represented as milliseconds since epoch (UTC).
     */
    public final long endDate;

    /**
     * Constructs a new DateRange with the specified start and end timestamps.
     * <p>
     * Both startDate and endDate are immutable and cannot be modified after construction.
     * There is no internal validation; the caller must ensure startDate <= endDate.
     * 
     * @param startDate the start timestamp (milliseconds since epoch)
     * @param endDate the end timestamp (milliseconds since epoch)
     */
    public DateRange(long startDate, long endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Checks whether a given timestamp falls within this date range (inclusive).
     * 
     * @param timestamp the timestamp to check
     * @return true if timestamp >= startDate && timestamp <= endDate, false otherwise
     */
    public boolean contains(long timestamp) {
        return timestamp >= startDate && timestamp <= endDate;
    }

    /**
     * Returns a string representation of the date range in milliseconds.
     * 
     * @return string in the format "DateRange[start=..., end=...]"
     */
    @Override
    public String toString() {
        return "DateRange[start=" + startDate + ", end=" + endDate + "]";
    }
}