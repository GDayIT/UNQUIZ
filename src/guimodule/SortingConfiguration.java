package guimodule;

import java.io.Serializable;

/**
 * Complete sorting configuration for persistence - Eclipse compatible.
 * Separated from interface to avoid $ in class names.
 */
public class SortingConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public final SortCriteria defaultSortCriteria;
    public final FilterCriteria defaultFilterCriteria;
    public final boolean rememberLastSort;
    public final long timestamp;
    
    public SortingConfiguration(SortCriteria sortCriteria, FilterCriteria filterCriteria, boolean rememberLastSort) {
        this.defaultSortCriteria = sortCriteria;
        this.defaultFilterCriteria = filterCriteria;
        this.rememberLastSort = rememberLastSort;
        this.timestamp = System.currentTimeMillis();
    }
}
