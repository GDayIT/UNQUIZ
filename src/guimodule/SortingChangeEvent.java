package guimodule;

/**
 * Sorting change event for Eclipse compatibility.
 * Separated from interface to avoid $ in class names.
 */
public class SortingChangeEvent {
    public final SortCriteria oldCriteria;
    public final SortCriteria newCriteria;
    public final long timestamp;
    
    public SortingChangeEvent(SortCriteria oldCriteria, SortCriteria newCriteria) {
        this.oldCriteria = oldCriteria;
        this.newCriteria = newCriteria;
        this.timestamp = System.currentTimeMillis();
    }
}
