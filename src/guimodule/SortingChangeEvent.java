package guimodule;

/**
 * Sorting change event for Eclipse compatibility.
 * Separated from interface to avoid $ in class names.
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
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
