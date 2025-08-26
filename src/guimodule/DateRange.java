package guimodule;

/**
 * Date range for filtering - Eclipse compatible.
 * Separated from interface to avoid $ in class names.
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class DateRange {
    public final long startDate;
    public final long endDate;
    
    public DateRange(long startDate, long endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
