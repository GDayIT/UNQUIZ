package gui;

import java.awt.HeadlessException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import guimodule.AppConfigService;
import guimodule.ConsoleCommandService;
import guimodule.ModularStyleService;
import guimodule.PnlForming;
import guimodule.StyleDelegate;

/**
 * Main application window (JFrame) for ProtoQUIZEE modular quiz system.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Initialize and display the main Swing frame.</li>
 *   <li>Set up modular service layers via GuiDelegate.</li>
 *   <li>Apply color schemes and styling via ModularStyleService.</li>
 *   <li>Attach main panel (PnlForming) for modular quiz interaction.</li>
 *   <li>Start a background console listener for runtime commands.</li>
 *   <li>Optionally perform data merge on startup using DataMergeService.</li>
 * </ul>
 *
 * <p>Interactions:
 * <ul>
 *   <li>{@link GuiDelegate} provides access to modular service instances.</li>
 *   <li>{@link ModularStyleService} manages styling and color schemes.</li>
 *   <li>{@link StyleDelegate} applies colors to UI components.</li>
 *   <li>{@link ConsoleCommandService} handles console-based commands.</li>
 *   <li>{@link dbbl.migration.DataMergeService} merges external persisted data.</li>
 *   <li>{@link PnlForming} serves as the main interactive panel for quizzes.</li>
 * </ul>
 *
 * <p>Threading:
 * <ul>
 *   <li>Console listener runs on a separate background thread to avoid blocking the UI.</li>
 *   <li>All Swing components are created and manipulated on the Event Dispatch Thread (EDT).</li>
 * </ul>
 * 
 * <p>Author: D.Georgiou
 * Version: 1.0
 */
public class Frame extends JFrame {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs the main application frame.
     * <p>
     * Sets default size, location, and close operation. Initializes
     * modular services, applies styling, sets the main panel, and starts
     * the console listener.
     *
     * @throws HeadlessException if the environment does not support a display
     */
    public Frame() throws HeadlessException {
        super("ProtoQUIZEE - Fully Modular Edition");

        // JFrame configuration
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 650);
        setLocationRelativeTo(null);

        // === Modular services via delegate pattern ===
        GuiDelegate gd = GuiDelegate.createDefault();
        var modules = gd.modules();
        ModularStyleService styleService = modules.newStyleService();

        // === Apply global application styling ===
        applyApplicationStyling(styleService);

        // === Attach main quiz panel ===
        add(new PnlForming(modules));

        setVisible(true);

        // === Start background console listener ===
        startConsoleListener();
    }

    /**
     * Applies modular styling to the main frame using the provided style service.
     *
     * @param styleService The modular style service used to obtain and apply color schemes
     */
    private void applyApplicationStyling(ModularStyleService styleService) {
        StyleDelegate.ColorConfiguration colors = styleService.applyColorScheme()
                .apply(styleService.getCurrentConfiguration().colorScheme);

        // Set background color for the frame
        setBackground(colors.background);
    }

    /**
     * Starts a background thread that listens for console commands.
     * <p>
     * Recognized commands:
     * <ul>
     *   <li>"console": starts the safe console service</li>
     *   <li>"exit": terminates the application gracefully</li>
     * </ul>
     */
    private void startConsoleListener() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n💡 Type 'console' to start the safe console...");

            while (true) {
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("console")) {
                    ConsoleCommandService.startConsole();
                } else if (input.equals("exit")) {
                    System.out.println("👋 Application exiting...");
                    System.exit(0);
                }
            }
        }).start();
    }

    /**
     * Application entry point.
     *
     * <p>Responsibilities:
     * <ul>
     *   <li>Sets system Look & Feel</li>
     *   <li>Reads CLI arguments and configuration</li>
     *   <li>Performs optional data merge on startup</li>
     *   <li>Starts Swing UI on the Event Dispatch Thread</li>
     * </ul>
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Apply system Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Could not apply Look and Feel: " + e.getMessage());
        }

        // === Load configuration from CLI or properties ===
        AppConfigService config = new AppConfigService(args);
        boolean mergeOnStartup = config.getBoolean("merge.onStartup", false);
        String mergeDir = config.getString("merge.dir", null);

        // === Optional data merge on startup ===
        if (mergeOnStartup && mergeDir != null) {
            try {
                var db = dbbl.DbblDelegate.createDefault();
                var achievements = new guimodule.achievements.AchievementsService();
                achievements.load();
                var tmpLeitner = new guimodule.AdaptiveLeitnerSystem(db.rawBusiness());
                var tmpStats = new guimodule.ModularQuizStatistics();

                var merger = new dbbl.migration.DataMergeService(db.rawPersistence(), tmpLeitner, tmpStats, achievements);
                var report = merger.mergeFromDirectory(
                        mergeDir,
                        dbbl.migration.DataMergeService.LeitnerMergePolicy.PREFER_HIGHER_LEVEL
                );
                db.rawPersistence().persistAll().run();
                achievements.save();
                System.out.println("Data merge completed: " + report);
            } catch (Exception ex) {
                System.err.println("Data merge failed: " + ex.getMessage());
            }
        }

        // === Start Swing UI on EDT ===
        SwingUtilities.invokeLater(Frame::new);
    }
}