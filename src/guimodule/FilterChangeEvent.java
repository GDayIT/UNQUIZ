package guimodule;

/**
 * Filter change event for Eclipse compatibility.
 * Separated from interface to avoid $ in class names.
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class FilterChangeEvent {
    public final FilterCriteria oldCriteria;
    public final FilterCriteria newCriteria;
    public final long timestamp;
    
    public FilterChangeEvent(FilterCriteria oldCriteria, FilterCriteria newCriteria) {
        this.oldCriteria = oldCriteria;
        this.newCriteria = newCriteria;
        this.timestamp = System.currentTimeMillis();
    }
}
