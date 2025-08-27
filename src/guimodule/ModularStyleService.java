package guimodule;

import java.awt.*;
import java.io.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Modular styling service implementing complete UI styling with persistence and delegation.
 * 
 * This service provides:
 * - Lambda-based styling operations for all UI components
 * - Persistent style configurations with serialization
 * - Theme switching with delegation patterns
 * - Dynamic styling based on application state
 * - Functional composition for complex styling operations
 * 
 * All styling operations are implemented as pure functions or controlled side-effects.
 * 
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public class ModularStyleService implements StyleDelegate, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String STYLE_CONFIG_FILE = "style_config.dat";
    
    private StyleConfiguration currentConfig;
    private ApplicationTheme currentTheme;
    
    // === LAMBDA IMPLEMENTATIONS ===
    private Function<String, JButton> createStyledButtonImpl;
    private BiFunction<JButton, ButtonTheme, JButton> applyButtonThemeImpl;
    private Function<ButtonState, ButtonStyle> getButtonStyleImpl;
    private Function<PanelType, JPanel> createStyledPanelImpl;
    private BiFunction<JPanel, PanelTheme, JPanel> applyPanelThemeImpl;
    private Function<BorderType, Border> createStyledBorderImpl;
    private Function<ColorScheme, ColorConfiguration> applyColorSchemeImpl;
    private Function<ComponentState, Color> calculateColorImpl;
    private Function<FontSpec, Font> createStyledFontImpl;
    private Function<Float, UnaryOperator<Font>> createFontScalerImpl;
    private Function<LayoutType, LayoutManager> createStyledLayoutImpl;
    private Function<SpacingType, SpacingConfiguration> getSpacingImpl;
    private Consumer<StyleConfiguration> saveStyleConfigurationImpl;
    private Supplier<StyleConfiguration> loadStyleConfigurationImpl;
    private Consumer<ApplicationTheme> applyApplicationThemeImpl;
    private Predicate<ApplicationTheme> validateThemeImpl;
    
    /**
     * Creates a new modular styling service with lambda-based implementations.
     */
    public ModularStyleService() {
        initializeLambdas();
        loadOrCreateDefaultConfiguration();
    }
    
    /**
     * Initializes all lambda implementations for styling operations.
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
