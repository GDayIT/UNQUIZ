package guimodule;

/**
 * Sort direction enumeration for Eclipse compatibility.
 * Separated from interface to avoid $ in class names.
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
