package guimodule;

/**
 * Sort type enumeration for Eclipse compatibility.
 * Separated from interface to avoid $ in class names.
 */
public enum SortType {
    ALPHABETICAL("Alphabetical", "🔤"),
    DATE("Date", "📅"),
    CUSTOM("Custom", "⚙️");
    
    public final String label;
    public final String icon;
    
    SortType(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }
}
