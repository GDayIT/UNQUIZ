package guimodule;

import java.awt.*;
import java.io.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * ModularStyleService is a centralized, modular, and highly configurable styling service for
 * Swing-based GUI components. It provides complete UI styling, theme management, persistent
 * configuration, and functional composition for dynamic UI updates.
 * 
 * <p>This service supports the following features:</p>
 * <ul>
 *   <li>Lambda-based styling operations for buttons, panels, fonts, borders, and layouts.</li>
 *   <li>Persistence of style configurations using serialization for application sessions.</li>
 *   <li>Dynamic theme switching with validation and delegation.</li>
 *   <li>Functional composition to combine multiple styling operations.</li>
 *   <li>Integration with color schemes, spacing, and component states.</li>
 * </ul>
 * 
 * <p>Typical usage involves creating buttons or panels via lambda-based factory functions,
 * applying themes, and persisting or restoring style configurations.</p>
 * 
 * <p>This service integrates with application sessions, adaptive Leitner cards, questions,
 * and quiz themes indirectly via its styling configuration.</p>
 * 
 * <p>Author: D.Georgiou</p>
 * <p>Version: 1.0</p>
 */
public class ModularStyleService implements StyleDelegate, Serializable {

    /** Serialization ID for backward compatibility. */
    private static final long serialVersionUID = 1L;

    /** Default file used to persist style configuration across sessions. */
    private static final String STYLE_CONFIG_FILE = "style_config.dat";

    // === CORE STATE FIELDS ===

    /** Current style configuration applied to the application. */
    private StyleConfiguration currentConfig;

    /** Current theme applied to the application, including color schemes, fonts, and layouts. */
    private ApplicationTheme currentTheme;

    // === LAMBDA IMPLEMENTATION FIELDS ===
    
    /** Lambda to create a styled JButton with default configuration. */
    private Function<String, JButton> createStyledButtonImpl;

    /** Lambda to apply a ButtonTheme to a given JButton. */
    private BiFunction<JButton, ButtonTheme, JButton> applyButtonThemeImpl;

    /** Lambda to get a ButtonStyle depending on the button state (hover, pressed, disabled). */
    private Function<ButtonState, ButtonStyle> getButtonStyleImpl;

    /** Lambda to create a styled JPanel with default configuration. */
    private Function<PanelType, JPanel> createStyledPanelImpl;

    /** Lambda to apply a PanelTheme to a JPanel instance. */
    private BiFunction<JPanel, PanelTheme, JPanel> applyPanelThemeImpl;

    /** Lambda to create styled borders based on predefined types. */
    private Function<BorderType, Border> createStyledBorderImpl;

    /** Lambda to apply a color scheme to component styles. */
    private Function<ColorScheme, ColorConfiguration> applyColorSchemeImpl;

    /** Lambda to calculate dynamic component colors based on state. */
    private Function<ComponentState, Color> calculateColorImpl;

    /** Lambda to create a styled Font from a FontSpec object. */
    private Function<FontSpec, Font> createStyledFontImpl;

    /** Lambda to create a font scaler function for dynamic resizing. */
    private Function<Float, UnaryOperator<Font>> createFontScalerImpl;

    /** Lambda to create a styled layout manager for panels. */
    private Function<LayoutType, LayoutManager> createStyledLayoutImpl;

    /** Lambda to retrieve spacing configurations for layout padding/margins. */
    private Function<SpacingType, SpacingConfiguration> getSpacingImpl;

    /** Lambda to save a StyleConfiguration to persistent storage. */
    private Consumer<StyleConfiguration> saveStyleConfigurationImpl;

    /** Lambda to load a StyleConfiguration from persistent storage. */
    private Supplier<StyleConfiguration> loadStyleConfigurationImpl;

    /** Lambda to apply an ApplicationTheme including colors, fonts, and layouts. */
    private Consumer<ApplicationTheme> applyApplicationThemeImpl;

    /** Lambda to validate if a given ApplicationTheme is structurally correct. */
    private Predicate<ApplicationTheme> validateThemeImpl;

    // === CONSTRUCTORS ===

    /**
     * Default constructor initializes lambda implementations and loads existing configuration
     * or creates a default configuration.
     */
    public ModularStyleService() {
        initializeLambdas();
        loadOrCreateDefaultConfiguration();
    }

    // === LAMBDA INITIALIZATION ===

    /**
     * Initializes all lambda-based styling operations, including buttons, panels, fonts,
     * layouts, borders, colors, and persistence.
     */
    private void initializeLambdas() {
        
        // === BUTTON STYLING ===
        createStyledButtonImpl = text -> {
            JButton button = new JButton(text);
            if (currentConfig != null && currentConfig.defaultButtonTheme != null) {
                applyButtonThemeImpl.apply(button, currentConfig.defaultButtonTheme);
            }
            return button;
        };
        
        applyButtonThemeImpl = (button, theme) -> {
            button.setBackground(theme.backgroundColor);
            button.setForeground(theme.foregroundColor);
            button.setFont(theme.font);
            button.setPreferredSize(theme.preferredSize);
            
            // Create custom border with radius
            Border border = BorderFactory.createLineBorder(theme.borderColor, 1);
            if (theme.borderRadius > 0) {
                border = BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(2, 2, 2, 2),
                    border
                );
            }
            button.setBorder(border);
            
            button.setFocusPainted(false);
            button.setOpaque(true);
            return button;
        };
        
        getButtonStyleImpl = state -> {
            if (currentConfig == null) return getDefaultButtonStyle();
            
            ButtonTheme theme = currentConfig.defaultButtonTheme;
            Color bg = theme.backgroundColor;
            Color fg = theme.foregroundColor;
            
            // Modify colors based on state
            switch (state) {
                case HOVER:
                    bg = bg.brighter();
                    break;
                case PRESSED:
                    bg = bg.darker();
                    break;
                case DISABLED:
                    bg = Color.LIGHT_GRAY;
                    fg = Color.GRAY;
                    break;
                case FOCUSED:
                    bg = theme.backgroundColor;
                    break;
                default:
                    break;
            }
            
            return new ButtonStyle(bg, fg, theme.font, 
                BorderFactory.createLineBorder(theme.borderColor));
        };
        
        // === PANEL STYLING ===
        createStyledPanelImpl = type -> {
            JPanel panel = new JPanel();
            if (currentConfig != null && currentConfig.defaultPanelTheme != null) {
                applyPanelThemeImpl.apply(panel, currentConfig.defaultPanelTheme);
            }
            return panel;
        };
        
        applyPanelThemeImpl = (panel, theme) -> {
            panel.setBackground(theme.backgroundColor);
            panel.setBorder(theme.border);
            if (theme.layout != null) {
                panel.setLayout(theme.layout);
            }
            return panel;
        };
        
        createStyledBorderImpl = type -> {
            switch (type) {
                case LINE:
                    return BorderFactory.createLineBorder(Color.GRAY);
                case RAISED:
                    return BorderFactory.createRaisedBevelBorder();
                case LOWERED:
                    return BorderFactory.createLoweredBevelBorder();
                case TITLED:
                    return BorderFactory.createTitledBorder("Title");
                case COMPOUND:
                    return BorderFactory.createCompoundBorder(
                        BorderFactory.createRaisedBevelBorder(),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    );
                default:
                    return null;
            }
        };
        
        // === COLOR SCHEMES ===
        applyColorSchemeImpl = scheme -> new ColorConfiguration(
            scheme.primary, scheme.secondary, scheme.background, Color.BLACK
        );
        
        calculateColorImpl = state -> {
            if (currentConfig == null) return Color.LIGHT_GRAY;
            
            ColorScheme scheme = currentConfig.colorScheme;
            switch (state) {
                case ACTIVE:
                    return scheme.primary;
                case SELECTED:
                    return scheme.accent;
                case ERROR:
                    return scheme.error;
                case SUCCESS:
                    return scheme.success;
                default:
                    return scheme.background;
            }
        };
        
        // === FONT STYLING ===
        createStyledFontImpl = spec -> new Font(spec.family, spec.style, (int) spec.size);
        
        createFontScalerImpl = scaleFactor -> font -> 
            font.deriveFont(font.getSize() * scaleFactor);
        
        // === LAYOUT STYLING ===
        createStyledLayoutImpl = type -> {
            switch (type) {
                case BORDER:
                    return new BorderLayout();
                case FLOW:
                    return new FlowLayout();
                case GRID:
                    return new GridLayout();
                case BOX:
                    return new BoxLayout(new JPanel(), BoxLayout.Y_AXIS);
                case GRIDBAG:
                    return new GridBagLayout();
                default:
                    return new BorderLayout();
            }
        };
        
        getSpacingImpl = type -> {
            switch (type) {
                case COMPACT:
                    return new SpacingConfiguration(2, 4, 8, 12);
                case NORMAL:
                    return new SpacingConfiguration(5, 10, 15, 20);
                case COMFORTABLE:
                    return new SpacingConfiguration(8, 15, 25, 35);
                case SPACIOUS:
                    return new SpacingConfiguration(12, 20, 35, 50);
                default:
                    return new SpacingConfiguration(5, 10, 15, 20);
            }
        };
        
        // === PERSISTENCE ===
        saveStyleConfigurationImpl = config -> {
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(STYLE_CONFIG_FILE))) {
                out.writeObject(config);
                System.out.println("Style configuration saved successfully");
            } catch (IOException e) {
                System.err.println("Failed to save style configuration: " + e.getMessage());
            }
        };
        
        loadStyleConfigurationImpl = () -> {
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(STYLE_CONFIG_FILE))) {
                Object obj = in.readObject();
                if (obj instanceof StyleConfiguration) {
                    System.out.println("Style configuration loaded successfully");
                    return (StyleConfiguration) obj;
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("No existing style configuration found, using defaults");
            }
            return createDefaultConfiguration();
        };
        
        // === THEME APPLICATION ===
        applyApplicationThemeImpl = theme -> {
            this.currentTheme = theme;
            this.currentConfig = theme.styleConfig;
            saveStyleConfigurationImpl.accept(currentConfig);
            System.out.println("Applied theme: " + theme.name);
        };
        
        validateThemeImpl = theme -> 
            theme != null && theme.styleConfig != null && theme.name != null;
    }
    
    // === INTERFACE IMPLEMENTATIONS ===
    @Override public Function<String, JButton> createStyledButton() { return createStyledButtonImpl; }
    @Override public BiFunction<JButton, ButtonTheme, JButton> applyButtonTheme() { return applyButtonThemeImpl; }
    @Override public Function<ButtonState, ButtonStyle> getButtonStyle() { return getButtonStyleImpl; }
    @Override public Function<PanelType, JPanel> createStyledPanel() { return createStyledPanelImpl; }
    @Override public BiFunction<JPanel, PanelTheme, JPanel> applyPanelTheme() { return applyPanelThemeImpl; }
    @Override public Function<BorderType, Border> createStyledBorder() { return createStyledBorderImpl; }
    @Override public Function<ColorScheme, ColorConfiguration> applyColorScheme() { return applyColorSchemeImpl; }
    @Override public Function<ComponentState, Color> calculateColor() { return calculateColorImpl; }
    @Override public Function<FontSpec, Font> createStyledFont() { return createStyledFontImpl; }
    @Override public Function<Float, UnaryOperator<Font>> createFontScaler() { return createFontScalerImpl; }
    @Override public Function<LayoutType, LayoutManager> createStyledLayout() { return createStyledLayoutImpl; }
    @Override public Function<SpacingType, SpacingConfiguration> getSpacing() { return getSpacingImpl; }
    @Override public Consumer<StyleConfiguration> saveStyleConfiguration() { return saveStyleConfigurationImpl; }
    @Override public Supplier<StyleConfiguration> loadStyleConfiguration() { return loadStyleConfigurationImpl; }
    @Override public Consumer<ApplicationTheme> applyApplicationTheme() { return applyApplicationThemeImpl; }
    @Override public Predicate<ApplicationTheme> validateTheme() { return validateThemeImpl; }
    
    // === UTILITY METHODS ===
    private void loadOrCreateDefaultConfiguration() {
        this.currentConfig = loadStyleConfigurationImpl.get();
        this.currentTheme = new ApplicationTheme("Default", currentConfig, false, "1.0");
    }
    
    private StyleConfiguration createDefaultConfiguration() {
        ButtonTheme defaultButton = new ButtonTheme(
            new Color(70, 130, 180), Color.WHITE, Color.GRAY,
            new Font("Arial", Font.PLAIN, 12), new Dimension(120, 30), 5
        );
        
        PanelTheme defaultPanel = new PanelTheme(
            Color.WHITE, BorderFactory.createEmptyBorder(10, 10, 10, 10),
            new BorderLayout(), new Insets(5, 5, 5, 5)
        );
        
        ColorScheme defaultColors = new ColorScheme(
            new Color(70, 130, 180), new Color(100, 149, 237), new Color(255, 165, 0),
            Color.WHITE, new Color(248, 248, 255), Color.RED, Color.GREEN, Color.ORANGE
        );
        
        FontSpec defaultFont = new FontSpec("Arial", Font.PLAIN, 12, true);
        SpacingConfiguration defaultSpacing = new SpacingConfiguration(5, 10, 15, 20);
        
        return new StyleConfiguration(defaultButton, defaultPanel, defaultColors, 
                                    defaultFont, defaultSpacing, "Default");
    }
    
    private ButtonStyle getDefaultButtonStyle() {
        return new ButtonStyle(Color.LIGHT_GRAY, Color.BLACK, 
                             new Font("Arial", Font.PLAIN, 12),
                             BorderFactory.createLineBorder(Color.GRAY));
    }
    
    // === PUBLIC CONVENIENCE METHODS ===
    public StyleConfiguration getCurrentConfiguration() { return currentConfig; }
    public ApplicationTheme getCurrentTheme() { return currentTheme; }
}