package guimodule;

/**
 * QuizApplicationDemo serves as a **deprecated entry point** for the quiz application.
 * <p>
 * It exists purely for backward compatibility and no longer provides a demo application.
 * All users and future code should instead use {@link gui.Frame#main(String[])} as the
 * primary entry point to launch the application.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Forward CLI arguments to the current main application entry point</li>
 *     <li>Preserve legacy compatibility for older scripts or shortcuts</li>
 * </ul>
 * <p>
 * This class is marked as `final` to prevent extension, emphasizing that it is
 * only a static forwarding utility.
 * <p>
 * No instances are ever created; the constructor is private to enforce static usage.
 * <p>
 * Integration with application modules:
 * <ul>
 *     <li>The method indirectly initializes Themes, Questions, Quiz Sessions, and
 *         Leitner Cards through {@link gui.Frame#main(String[])}</li>
 *     <li>All UI components, statistics, and quiz logic are delegated to the main frame</li>
 * </ul>
 * 
 * @author D.
 * Georgiou
 * @version 1.0
 * @see gui.Frame
 */
public final class QuizApplicationDemo {

    /**
     * Private constructor to prevent instantiation.
     * Ensures that the class is used solely as a static entry-point forwarder.
     */
    private QuizApplicationDemo() {}

    /**
     * Main method for backward compatibility.
     * <p>
     * This method simply forwards all command-line arguments to
     * {@link gui.Frame#main(String[])}. It does not initialize any UI or modules
     * itself, and serves only as a legacy bridge.
     * <p>
     * Indirectly, invoking this method will:
     * <ul>
     *     <li>Initialize all GUI modules</li>
     *     <li>Load themes and statistics panels</li>
     *     <li>Setup quiz sessions, questions, and Leitner card systems</li>
     *     <li>Wire UI panels, forms, lists, and game logic</li>
     * </ul>
     *
     * @param args CLI arguments that will be forwarded to the real main method
     */
    public static void main(String[] args) {
        gui.Frame.main(args);
    }
}