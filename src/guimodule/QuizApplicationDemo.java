package guimodule;

/**
 * Deprecated entry point. No demo application is provided anymore.
 * Use gui.Frame.main as the application entry point.
 */
public final class QuizApplicationDemo {
    private QuizApplicationDemo() {}

    /**
     * For backward compatibility only: forwards to gui.Frame.main.
     *
     * @param args CLI arguments
     */
    public static void main(String[] args) {
        gui.Frame.main(args);
    }
}
