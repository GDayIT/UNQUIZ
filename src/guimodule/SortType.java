package guimodule;

/**
 * Sort type enumeration for Eclipse compatibility.
 * Separated from interface to avoid $ in class names.
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
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
