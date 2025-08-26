//package gui;
//
//import guimodule.ModularStyleService;
//import guimodule.PnlForming;
//import guimodule.StyleDelegate;
//import java.awt.HeadlessException;
//import java.io.File;
//import javax.swing.JFrame;
//import javax.swing.UIManager;
//
///**
// * Main application frame using fully modular, delegated architecture with styling.
// */
//public class Frame extends JFrame {
//    private static final long serialVersionUID = 1L;
//
//    public Frame() throws HeadlessException {
//        super("ProtoQUIZEE - Fully Modular Edition");
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
//        setSize(850, 650);
//        setLocationRelativeTo(null);
//
//        // Create modular service layers via delegate
//        GuiDelegate gd = GuiDelegate.createDefault();
//        var modules = gd.modules();
//        ModularStyleService styleService = modules.newStyleService();
//
//        // Apply application theme
//        applyApplicationStyling(styleService);
//
//        // Wire GUI with modular services
//        add(new PnlForming(modules));
//
//        setVisible(true);
//    }
//
//    /**
//     * Applies modular styling to the main frame using lambda-based styling.
//     */
//    private void applyApplicationStyling(ModularStyleService styleService) {
//        // Apply frame styling through delegation
//        StyleDelegate.ColorConfiguration colors = styleService.applyColorScheme()
//            .apply(styleService.getCurrentConfiguration().colorScheme);
//
//        setBackground(colors.background);
//    }
//
//    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            System.out.println("Could not apply Look and Feel: " + e.getMessage());
//        }
//
//        // Optional merge on startup (configurable via CLI or application.properties)
//        guimodule.AppConfigService config = new guimodule.AppConfigService(args);
//        boolean mergeOnStartup = config.getBoolean("merge.onStartup", false);
//        String mergeDir = config.getString("merge.dir", null);
//
//        if (mergeOnStartup && mergeDir != null) {
//            try {
//                // Build services using dbbl delegate for merging only
//                var db = dbbl.DbblDelegate.createDefault();
//                var achievements = new guimodule.achievements.AchievementsService();
//                achievements.load();
//                var tmpLeitner = new guimodule.AdaptiveLeitnerSystem(db.rawBusiness());
//                var tmpStats = new guimodule.ModularQuizStatistics();
//
//                var merger = new dbbl.migration.DataMergeService(db.rawPersistence(), tmpLeitner, tmpStats, achievements);
//                var report = merger.mergeFromDirectory(
//                    mergeDir,
//                    dbbl.migration.DataMergeService.LeitnerMergePolicy.PREFER_HIGHER_LEVEL
//                );
//                db.rawPersistence().persistAll().run();
//                achievements.save();
//                System.out.println("Data merge completed: " + report);
//            } catch (Exception ex) {
//                System.err.println("Data merge failed: " + ex.getMessage());
//            }
//        }
//
//        // Always create Swing UI on the Event Dispatch Thread
//        javax.swing.SwingUtilities.invokeLater(Frame::new);
//    }
//}



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
 * Main application frame using fully modular, delegated architecture with styling
 * + Console command listener for Look & Feel.
<<<<<<< HEAD
 * 
 * @author D.Georgiou
 * @version 1.0 
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class Frame extends JFrame {
    private static final long serialVersionUID = 1L;

    public Frame() throws HeadlessException {
        super("ProtoQUIZEE - Fully Modular Edition");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 650);
        setLocationRelativeTo(null);

        // === Modular service layers via delegate ===
        GuiDelegate gd = GuiDelegate.createDefault();
        var modules = gd.modules();
        ModularStyleService styleService = modules.newStyleService();

        // === Apply styling ===
        applyApplicationStyling(styleService);

        // === Main panel ===
        add(new PnlForming(modules));

        setVisible(true);

        // === Start console listener in background ===
        startConsoleListener();
    }

    /**
     * Applies modular styling to the main frame using lambda-based styling.
     */
    private void applyApplicationStyling(ModularStyleService styleService) {
        StyleDelegate.ColorConfiguration colors = styleService.applyColorScheme()
                .apply(styleService.getCurrentConfiguration().colorScheme);

        setBackground(colors.background);
    }

    /**
     * Console listener for commands (console / exit).
     */
    private void startConsoleListener() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n💡 Geben Sie 'console' ein, um die sichere Konsole zu starten...");

            while (true) {
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("console")) {
                    ConsoleCommandService.startConsole();
                } else if (input.equals("exit")) {
                    System.out.println("👋 Anwendung wird beendet...");
                    System.exit(0);
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Could not apply Look and Feel: " + e.getMessage());
        }

        // === Optional merge on startup (configurable via CLI or properties) ===
        AppConfigService config = new AppConfigService(args);
        boolean mergeOnStartup = config.getBoolean("merge.onStartup", false);
        String mergeDir = config.getString("merge.dir", null);

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

        // === Always create Swing UI on EDT ===
        javax.swing.SwingUtilities.invokeLater(Frame::new);
    }
}
