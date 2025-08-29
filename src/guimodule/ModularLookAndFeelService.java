package guimodule;

import java.awt.*;
import java.util.function.*;
import javax.swing.*;

/**
 * ModularLookAndFeelService provides a centralized, modular approach for
 * managing Look & Feel (L&F) themes across the GUI module.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Provide multiple pre-defined themes: SYSTEM, NIMBUS, METAL, MOTIF, WINDOWS, DEFAULT.</li>
 *   <li>Allow runtime switching of themes using lambda-based operations.</li>
 *   <li>Apply theme changes across all active windows dynamically.</li>
 *   <li>Provide ready-to-use Swing GUI component for theme switching.</li>
 *   <li>Ensure safe startup by falling back to system/default theme if preferred theme fails.</li>
 * </ul>
 * <p>
 * Interaction Model:
 * <ul>
 *   <li>Static service: All methods and state are static for global access.</li>
 *   <li>GUI modules (panels, frames) can integrate the theme switcher panel via {@link #createThemeSwitcher()}.</li>
 *   <li>Internal operations leverage {@link UIManager} and {@link SwingUtilities} for theme management.</li>
 * </ul>
 * <p>
 * Best Practices and Design Notes:
 * <ul>
 *   <li>Encapsulation of theme metadata via {@link LookAndFeelTheme} enum.</li>
 *   <li>Safe application of themes using try-catch blocks.</li>
 *   <li>Functional-style lambdas for modularity and future extension.</li>
 *   <li>Clear separation between theme data, theme application logic, and GUI component generation.</li>
 * </ul>
 * <p>
 * Dependencies:
 * <ul>
 *   <li>{@link UIManager}: To set Look & Feel class.</li>
 *   <li>{@link SwingUtilities}: To update all windows after theme change.</li>
 *   <li>{@link Window}: Iterates all open windows to repaint UI tree.</li>
 *   <li>{@link JPanel}, {@link JComboBox}, {@link JButton}, {@link JLabel}: For theme switcher GUI.</li>
 * </ul>
 * <p>
 * Related Concepts:
 * <ul>
 *   <li>Can be integrated with {@link CreateQuizQuestionsPanel}, {@link GuiModuleDelegate}, {@link ModularStyleService} for dynamic theme changes.</li>
 *   <li>Supports user preferences and runtime customization of GUI appearance.</li>
 * </ul>
 * 
 * Author: D.Georgiou
 * Version: 1.0
 */
public class ModularLookAndFeelService {

    /**
     * Enumeration of available Look & Feel themes.
     * Each theme has a human-readable display name and a supplier for the L&F class name.
     */
    public enum LookAndFeelTheme {
        SYSTEM("System", () -> UIManager.getSystemLookAndFeelClassName()),
        NIMBUS("Nimbus", () -> findLookAndFeel("Nimbus")),
        METAL("Metal", () -> "javax.swing.plaf.metal.MetalLookAndFeel"),
        MOTIF("Motif", () -> "com.sun.java.swing.plaf.motif.MotifLookAndFeel"),
        WINDOWS("Windows", () -> "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"),
        DEFAULT("Default", () -> UIManager.getCrossPlatformLookAndFeelClassName());

        /** Human-readable theme name for UI display. */
        private final String displayName;

        /** Supplier providing the fully qualified class name for UIManager. */
        private final Supplier<String> classNameSupplier;

        LookAndFeelTheme(String displayName, Supplier<String> classNameSupplier) {
            this.displayName = displayName;
            this.classNameSupplier = classNameSupplier;
        }

        /** @return Display name of the theme. */
        public String getDisplayName() { return displayName; }

        /** @return Fully qualified class name of the Look & Feel. */
        public String getClassName() { return classNameSupplier.get(); }
    }

    /** Current active theme. Initialized to DEFAULT. */
    private static LookAndFeelTheme currentTheme = LookAndFeelTheme.DEFAULT;

    /** Lambda function to apply a given theme and update all open windows. */
    private static final Function<LookAndFeelTheme, Boolean> applyTheme = theme -> {
        try {
            String className = theme.getClassName();
            if (className != null && !className.isEmpty()) {
                UIManager.setLookAndFeel(className);

                // Apply changes to all open windows
                for (Window window : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(window);
                    window.repaint();
                }

                currentTheme = theme;
                System.out.println("Applied Look & Feel: " + theme.getDisplayName());
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to apply Look & Feel " + theme.getDisplayName() + ": " + e.getMessage());
        }
        return false;
    };

    /** Supplier lambda to get all available themes. */
    private static final Supplier<LookAndFeelTheme[]> getAvailableThemes = LookAndFeelTheme::values;

    /** Supplier lambda to get currently active theme. */
    private static final Supplier<LookAndFeelTheme> getCurrentTheme = () -> currentTheme;

    /** Runnable lambda to reset theme to DEFAULT. */
    private static final Runnable resetToDefault = () -> applyTheme.apply(LookAndFeelTheme.DEFAULT);

    /** Apply a specific Look & Feel theme. */
    public static boolean applyLookAndFeel(LookAndFeelTheme theme) {
        return applyTheme.apply(theme);
    }

    /** @return Array of all available Look & Feel themes. */
    public static LookAndFeelTheme[] getAvailableThemes() {
        return getAvailableThemes.get();
    }

    /** @return Currently applied theme. */
    public static LookAndFeelTheme getCurrentTheme() {
        return getCurrentTheme.get();
    }

    /** Reset the theme to the default Look & Feel. */
    public static void resetToDefault() {
        resetToDefault.run();
    }

    /**
     * Create a JPanel containing a combo box to select a theme, an Apply button, and a Reset button.
     * This allows runtime switching of Look & Feel within the GUI.
     *
     * @return JPanel ready to embed into any container.
     */
    public static JPanel createThemeSwitcher() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Look & Feel"));

        JComboBox<LookAndFeelTheme> themeCombo = new JComboBox<>(getAvailableThemes());
        themeCombo.setSelectedItem(getCurrentTheme());
        themeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LookAndFeelTheme) {
                    setText(((LookAndFeelTheme) value).getDisplayName());
                }
                return this;
            }
        });

        JButton applyBtn = new JButton("Apply");
        JButton resetBtn = new JButton("Reset");

        applyBtn.addActionListener(e -> {
            LookAndFeelTheme selected = (LookAndFeelTheme) themeCombo.getSelectedItem();
            if (selected != null) applyLookAndFeel(selected);
        });

        resetBtn.addActionListener(e -> {
            resetToDefault();
            themeCombo.setSelectedItem(getCurrentTheme());
        });

        panel.add(new JLabel("Theme:"));
        panel.add(themeCombo);
        panel.add(applyBtn);
        panel.add(resetBtn);

        return panel;
    }

    /**
     * Initialize default theme on application startup.
     * Attempts NIMBUS, then SYSTEM, then DEFAULT if all else fails.
     */
    public static void initializeDefault() {
        if (!applyLookAndFeel(LookAndFeelTheme.NIMBUS)) {
            if (!applyLookAndFeel(LookAndFeelTheme.SYSTEM)) {
                applyLookAndFeel(LookAndFeelTheme.DEFAULT);
            }
        }
    }

    /**
     * Helper function to find the fully-qualified class name of a theme by its name.
     *
     * @param name Name of the Look & Feel to find.
     * @return Class name string, or null if not found.
     */
    private static String findLookAndFeel(String name) {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (name.equals(info.getName())) return info.getClassName();
        }
        return null;
    }
}