package guimodule;

<<<<<<< HEAD
import java.awt.Window;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
=======
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd

/**
 * Secure Console Command Service for Look & Feel management. ----TODO: functionality is not yet settet, no Look & Feel switch!
 * 
 * Features:
 * - Whitelist-based command system
 * - Secure Look & Feel switching via console only
 * - Help system with available commands
 * - Input validation and sanitization
 * 
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0

>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class ConsoleCommandService {
    
    // Whitelist of allowed commands
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
        "help", "laf", "themes", "current", "reset", "status", "clear"
    );
    
    // Available Look & Feel themes
    private static final Map<String, String> THEME_MAP = Map.of(
        "system", "System Look & Feel",
        "nimbus", "Nimbus",
        "metal", "Metal",
        "motif", "Motif", 
        "windows", "Windows",
        "default", "Default Cross-Platform"
    );
    
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean consoleActive = false;
    
    /**
     * Start the secure console interface.
     */
    public static void startConsole() {
        if (consoleActive) {
            System.out.println("⚠️  Konsole ist bereits aktiv!");
            return;
        }
        
        consoleActive = true;
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔧 QUIZEE SECURE CONSOLE");
        System.out.println("=".repeat(60));
        System.out.println("Sichere Konsole für Look & Feel Management");
        System.out.println("Geben Sie 'help' für verfügbare Befehle ein.");
        System.out.println("Geben Sie 'exit' zum Beenden ein.");
        System.out.println("=".repeat(60) + "\n");
        
        while (consoleActive) {
            System.out.print("quizee> ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("exit") || input.equals("quit")) {
                consoleActive = false;
                System.out.println("👋 Konsole beendet.");
                break;
            }
            
            processCommand(input);
        }
    }
    
    
    /**
     * Process and validate console commands.
     */
    private static void processCommand(String input) {
        if (input.isEmpty()) {
            return;
        }
        
        String[] parts = input.split("\\s+");
        String command = parts[0];
        
        // Security check: Only allow whitelisted commands
        if (!ALLOWED_COMMANDS.contains(command)) {
            System.out.println("❌ Unbekannter Befehl: '" + command + "'");
            System.out.println("   Geben Sie 'help' für verfügbare Befehle ein.");
            return;
        }
        
        switch (command) {
            case "help":
                showHelp();
                break;
            case "laf":
                if (parts.length < 2) {
                    System.out.println("❌ Verwendung: laf <theme>");
                    System.out.println("   Geben Sie 'themes' für verfügbare Themes ein.");
                } else {
                    setLookAndFeel(parts[1]);
                }
                break;
            case "themes":
                showAvailableThemes();
                break;
            case "current":
                showCurrentTheme();
                break;
            case "reset":
                resetToDefault();
                break;
            case "status":
                showStatus();
                break;
            case "clear":
                clearConsole();
                break;
            default:
                System.out.println("❌ Befehl nicht implementiert: " + command);
        }
    }
    
    /**
     * Show help information.
     */
    private static void showHelp() {
        System.out.println("\n📖 VERFÜGBARE BEFEHLE:");
        System.out.println("─".repeat(40));
        System.out.println("help          - Diese Hilfe anzeigen");
        System.out.println("laf <theme>   - Look & Feel setzen");
        System.out.println("themes        - Verfügbare Themes anzeigen");
        System.out.println("current       - Aktuelles Theme anzeigen");
        System.out.println("reset         - Auf Standard zurücksetzen");
        System.out.println("status        - System-Status anzeigen");
        System.out.println("clear         - Konsole leeren");
        System.out.println("exit/quit     - Konsole beenden");
        System.out.println("─".repeat(40));
        System.out.println("\n💡 BEISPIELE:");
        System.out.println("   laf nimbus    - Nimbus Theme setzen");
        System.out.println("   laf system    - System Theme setzen");
        System.out.println("   laf default   - Standard Theme setzen");
        System.out.println();
    }
    
    /**
     * Show available themes.
     */
    private static void showAvailableThemes() {
        System.out.println("\n🎨 VERFÜGBARE THEMES:");
        System.out.println("─".repeat(40));
        THEME_MAP.forEach((key, value) -> 
            System.out.printf("%-10s - %s%n", key, value));
        System.out.println("─".repeat(40));
        System.out.println("Verwendung: laf <theme-name>");
        System.out.println();
    }
    
<<<<<<< HEAD
    
    
    
    /**
     * Aktualisiert alle offenen Swing-Fenster nach einem Look & Feel-Wechsel.
     */
    private static void refreshAllWindows() {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
                window.invalidate();
                window.validate();
                window.repaint();
            }
        });
    }
    
    
    
    
    
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
    /**
     * Set Look & Feel theme securely.
     */
    private static void setLookAndFeel(String themeName) {
        if (!THEME_MAP.containsKey(themeName)) {
            System.out.println("❌ Unbekanntes Theme: '" + themeName + "'");
            System.out.println("   Geben Sie 'themes' für verfügbare Themes ein.");
            return;
        }

        try {
            ModularLookAndFeelService.LookAndFeelTheme theme =
                    getThemeByName(themeName);

            if (theme != null) {
                boolean success = ModularLookAndFeelService.applyLookAndFeel(theme);
                if (success) {
<<<<<<< HEAD
                    refreshAllWindows(); // 👈 Aufruf statt Copy/Paste
=======
                    // 🔄 Wichtig: UI neu rendern, sonst NPE in Synth
                    SwingUtilities.invokeLater(() -> {
                        for (Window window : Window.getWindows()) {
                            SwingUtilities.updateComponentTreeUI(window);
                            window.invalidate();
                            window.validate();
                            window.repaint();
                        }
                    });

>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
                    System.out.println("✅ Look & Feel erfolgreich geändert zu: "
                            + THEME_MAP.get(themeName));
                } else {
                    System.out.println("❌ Fehler beim Setzen des Look & Feel");
                }
<<<<<<< HEAD
//                if (success) {
//                    // 🔄 Wichtig: UI neu rendern, sonst NPE in Synth
//                    SwingUtilities.invokeLater(() -> {
//                        for (Window window : Window.getWindows()) {
//                            SwingUtilities.updateComponentTreeUI(window);
//                            window.invalidate();
//                            window.validate();
//                            window.repaint();
//                        }
//                    });
//
//                    System.out.println("✅ Look & Feel erfolgreich geändert zu: "
//                            + THEME_MAP.get(themeName));
//                } else {
//                    System.out.println("❌ Fehler beim Setzen des Look & Feel");
//                }
=======
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
            } else {
                System.out.println("❌ Theme nicht verfügbar: " + themeName);
            }
        } catch (Exception e) {
            System.out.println("❌ Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }
//    private static void setLookAndFeel(String themeName) {
//        if (!THEME_MAP.containsKey(themeName)) {
//            System.out.println("❌ Unbekanntes Theme: '" + themeName + "'");
//            System.out.println("   Geben Sie 'themes' für verfügbare Themes ein.");
//            return;
//        }
//        
//        try {
//            ModularLookAndFeelService.LookAndFeelTheme theme = 
//                getThemeByName(themeName);
//            
//            if (theme != null) {
//                boolean success = ModularLookAndFeelService.applyLookAndFeel(theme);
//                if (success) {
//                    System.out.println("✅ Look & Feel erfolgreich geändert zu: " + 
//                        THEME_MAP.get(themeName));
//                } else {
//                    System.out.println("❌ Fehler beim Setzen des Look & Feel");
//                }
//            } else {
//                System.out.println("❌ Theme nicht verfügbar: " + themeName);
//            }
//        } catch (Exception e) {
//            System.out.println("❌ Fehler: " + e.getMessage());
//        }
//    }
    
    /**
     * Get theme enum by name.
     */
    private static ModularLookAndFeelService.LookAndFeelTheme getThemeByName(String name) {
        switch (name.toLowerCase()) {
            case "system": return ModularLookAndFeelService.LookAndFeelTheme.SYSTEM;
            case "nimbus": return ModularLookAndFeelService.LookAndFeelTheme.NIMBUS;
            case "metal": return ModularLookAndFeelService.LookAndFeelTheme.METAL;
            case "motif": return ModularLookAndFeelService.LookAndFeelTheme.MOTIF;
            case "windows": return ModularLookAndFeelService.LookAndFeelTheme.WINDOWS;
            case "default": return ModularLookAndFeelService.LookAndFeelTheme.DEFAULT;
            default: return null;
        }
    }
    
    /**
     * Show current theme.
     */
    private static void showCurrentTheme() {
        ModularLookAndFeelService.LookAndFeelTheme current = 
            ModularLookAndFeelService.getCurrentTheme();
        System.out.println("🎨 Aktuelles Theme: " + current.getDisplayName());
    }
    
    
    /**
     * Reset to default theme.
<<<<<<< HEAD
     *
     */
    private static void resetToDefault() {
        ModularLookAndFeelService.resetToDefault();
        refreshAllWindows(); // 👈 Einheitlicher Aufruf
        System.out.println("🔄 Look & Feel auf Standard zurückgesetzt");
    }
    
    
    
//    private static void resetToDefault() {
//        ModularLookAndFeelService.resetToDefault();
//
//        // 🔄 UI neu rendern nach Reset
//        SwingUtilities.invokeLater(() -> {
//            for (Window window : Window.getWindows()) {
//                SwingUtilities.updateComponentTreeUI(window);
//                window.invalidate();
//                window.validate();
//                window.repaint();
//            }
//        });
//
//        System.out.println("🔄 Look & Feel auf Standard zurückgesetzt");
//    }
    
    
    
=======
     */
    private static void resetToDefault() {
        ModularLookAndFeelService.resetToDefault();

        // 🔄 UI neu rendern nach Reset
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
                window.invalidate();
                window.validate();
                window.repaint();
            }
        });

        System.out.println("🔄 Look & Feel auf Standard zurückgesetzt");
    }
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
//    /**
//     * Reset to default theme.
//     */
//    private static void resetToDefault() {
//        ModularLookAndFeelService.resetToDefault();
//        System.out.println("🔄 Look & Feel auf Standard zurückgesetzt");
//    }
    
    /**
     * Show system status.
     */
    private static void showStatus() {
        System.out.println("\n📊 SYSTEM STATUS:");
        System.out.println("─".repeat(40));
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Aktuelles LAF: " + UIManager.getLookAndFeel().getName());
        System.out.println("Verfügbare LAFs: " + UIManager.getInstalledLookAndFeels().length);
        System.out.println("─".repeat(40));
        System.out.println();
    }
    
    /**
     * Clear console (simulate).
     */
    private static void clearConsole() {
        // Print multiple newlines to simulate clearing
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
        System.out.println("🧹 Konsole geleert");
    }
    
    /**
     * Check if console is active.
     */
    public static boolean isConsoleActive() {
        return consoleActive;
    }
    
    /**
     * Stop console programmatically.
     */
    public static void stopConsole() {
        consoleActive = false;
    }
}
