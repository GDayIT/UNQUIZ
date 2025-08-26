package guimodule;

/**
 * Sort direction enumeration for Eclipse compatibility.
 * Separated from interface to avoid $ in class names.
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public enum SortDirection {
    ASCENDING("A→Z", "Earliest→Latest"),
    DESCENDING("Z→A", "Latest→Earliest");
    
    public final String alphabeticalLabel;
    public final String dateLabel;
    
    SortDirection(String alphabeticalLabel, String dateLabel) {
        this.alphabeticalLabel = alphabeticalLabel;
        this.dateLabel = dateLabel;
    }
    
    public SortDirection toggle() {
        return this == ASCENDING ? DESCENDING : ASCENDING;
    }
}
