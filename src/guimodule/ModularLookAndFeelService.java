package guimodule;

import java.awt.*;
import java.util.function.*;
import javax.swing.*;

/**
 * Modular Look & Feel Service with easy switching capabilities.
 * 
 * Features:
 * - Quick switch between different Look & Feel themes
 * - Default system theme support
 * - Lambda-based theme application
 * - Runtime theme switching
 * 
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
=======
 * @author Quiz Application Team
 * @version 2.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class ModularLookAndFeelService {
    
    public enum LookAndFeelTheme {
        SYSTEM("System", () -> UIManager.getSystemLookAndFeelClassName()),
        NIMBUS("Nimbus", () -> findLookAndFeel("Nimbus")),
        METAL("Metal", () -> "javax.swing.plaf.metal.MetalLookAndFeel"),
        MOTIF("Motif", () -> "com.sun.java.swing.plaf.motif.MotifLookAndFeel"),
        WINDOWS("Windows", () -> "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"),
        DEFAULT("Default", () -> UIManager.getCrossPlatformLookAndFeelClassName());
        
        private final String displayName;
        private final Supplier<String> classNameSupplier;
        
        LookAndFeelTheme(String displayName, Supplier<String> classNameSupplier) {
            this.displayName = displayName;
            this.classNameSupplier = classNameSupplier;
        }
        
        public String getDisplayName() { return displayName; }
        public String getClassName() { return classNameSupplier.get(); }
    }
    
    private static LookAndFeelTheme currentTheme = LookAndFeelTheme.DEFAULT;
    
    // Lambda-based operations
    private static final Function<LookAndFeelTheme, Boolean> applyTheme = theme -> {
        try {
            String className = theme.getClassName();
            if (className != null && !className.isEmpty()) {
                UIManager.setLookAndFeel(className);
                
                // Update all existing windows
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
    
    private static final Supplier<LookAndFeelTheme[]> getAvailableThemes = () -> {
        return LookAndFeelTheme.values();
    };
    
    private static final Supplier<LookAndFeelTheme> getCurrentTheme = () -> currentTheme;
    
    private static final Runnable resetToDefault = () -> applyTheme.apply(LookAndFeelTheme.DEFAULT);
    
    /**
     * Apply a specific Look & Feel theme.
     */
    public static boolean applyLookAndFeel(LookAndFeelTheme theme) {
        return applyTheme.apply(theme);
    }
    
    /**
     * Get all available themes.
     */
    public static LookAndFeelTheme[] getAvailableThemes() {
        return getAvailableThemes.get();
    }
    
    /**
     * Get current theme.
     */
    public static LookAndFeelTheme getCurrentTheme() {
        return getCurrentTheme.get();
    }
    
    /**
     * Quick reset to default theme.
     */
    public static void resetToDefault() {
        resetToDefault.run();
    }
    
    /**
     * Create a theme switcher component.
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
            if (selected != null) {
                applyLookAndFeel(selected);
            }
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
     * Initialize with default theme (safe startup).
     */
    public static void initializeDefault() {
        // Try to apply a nice theme, but fall back to default if it fails
        if (!applyLookAndFeel(LookAndFeelTheme.NIMBUS)) {
            if (!applyLookAndFeel(LookAndFeelTheme.SYSTEM)) {
                applyLookAndFeel(LookAndFeelTheme.DEFAULT);
            }
        }
    }
    
    /**
     * Helper method to find Look & Feel by name.
     */
    private static String findLookAndFeel(String name) {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (name.equals(info.getName())) {
                return info.getClassName();
            }
        }
        return null;
    }
}
