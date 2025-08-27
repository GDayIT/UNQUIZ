package guimodule;

import java.awt.*;
import java.util.function.*;
import javax.swing.*;

/**
 * Modular styling delegation interface using lambda expressions and functional programming.
 * 
 * This interface provides complete modularity for all UI styling operations:
 * - Button styling with lambda-based configuration
 * - Panel styling with functional composition
 * - Color schemes with persistence support
 * - Font management with delegation patterns
 * - Layout configuration with serialization
 * 
 * All styling operations are defined as functional interfaces to support:
 * - Runtime style changes through lambdas
 * - Persistent style configurations
 * - Theme switching with delegation
 * - Dynamic styling based on application state
 * 
<<<<<<< HEAD
 * @author D.Georgiou
 * @version 1.0
>>>>>>> 51d430330dca283242d67944a6d45c96dfa445fd
 */
public interface StyleDelegate {
    
    // === BUTTON STYLING LAMBDAS ===
    
    /**
     * Lambda for creating styled buttons.
     * Function takes button text and returns configured JButton.
     */
    Function<String, JButton> createStyledButton();
    
    /**
     * Lambda for applying button themes.
     * BiFunction takes (button, theme) and returns styled button.
     */
    BiFunction<JButton, ButtonTheme, JButton> applyButtonTheme();
    
    /**
     * Lambda for button state styling.
     * Function takes button state and returns style configuration.
     */
    Function<ButtonState, ButtonStyle> getButtonStyle();
    
    // === PANEL STYLING LAMBDAS ===
    
    /**
     * Lambda for creating styled panels.
     * Function takes panel type and returns configured JPanel.
     */
    Function<PanelType, JPanel> createStyledPanel();
    
    /**
     * Lambda for applying panel themes.
     * BiFunction takes (panel, theme) and returns styled panel.
     */
    BiFunction<JPanel, PanelTheme, JPanel> applyPanelTheme();
    
    /**
     * Lambda for panel border styling.
     * Function takes border type and returns configured border.
     */
    Function<BorderType, javax.swing.border.Border> createStyledBorder();
    
    // === COLOR SCHEME LAMBDAS ===
    
    /**
     * Lambda for color scheme application.
     * Function takes color scheme and returns color configuration.
     */
    Function<ColorScheme, ColorConfiguration> applyColorScheme();
    
    /**
     * Lambda for dynamic color calculation.
     * Function takes component state and returns appropriate color.
     */
    Function<ComponentState, Color> calculateColor();
    
    // === FONT STYLING LAMBDAS ===
    
    /**
     * Lambda for font creation.
     * Function takes font specification and returns configured Font.
     */
    Function<FontSpec, Font> createStyledFont();
    
    /**
     * Lambda for font scaling.
     * Function takes scale factor and returns font transformer.
     */
    Function<Float, UnaryOperator<Font>> createFontScaler();
    
    // === LAYOUT STYLING LAMBDAS ===
    
    /**
     * Lambda for layout manager creation.
     * Function takes layout type and returns configured LayoutManager.
     */
    Function<LayoutType, LayoutManager> createStyledLayout();
    
    /**
     * Lambda for spacing configuration.
     * Function takes spacing type and returns spacing values.
     */
    Function<SpacingType, SpacingConfiguration> getSpacing();
    
    // === PERSISTENCE LAMBDAS ===
    
    /**
     * Lambda for style persistence.
     * Consumer saves style configuration to storage.
     */
    Consumer<StyleConfiguration> saveStyleConfiguration();
    
    /**
     * Lambda for style loading.
     * Supplier loads style configuration from storage.
     */
    Supplier<StyleConfiguration> loadStyleConfiguration();
    
    // === THEME SWITCHING LAMBDAS ===
    
    /**
     * Lambda for theme application.
     * Consumer applies complete theme to application.
     */
    Consumer<ApplicationTheme> applyApplicationTheme();
    
    /**
     * Lambda for theme validation.
     * Predicate checks if theme is valid and applicable.
     */
    Predicate<ApplicationTheme> validateTheme();
    
    // === DATA TRANSFER OBJECTS ===
    
    /**
     * Button theme configuration.
     */
    class ButtonTheme {
        public final Color backgroundColor;
        public final Color foregroundColor;
        public final Color borderColor;
        public final Font font;
        public final Dimension preferredSize;
        public final int borderRadius;
        
        public ButtonTheme(Color bg, Color fg, Color border, Font font, Dimension size, int radius) {
            this.backgroundColor = bg;
            this.foregroundColor = fg;
            this.borderColor = border;
            this.font = font;
            this.preferredSize = size;
            this.borderRadius = radius;
        }
    }
    
    /**
     * Panel theme configuration.
     */
    class PanelTheme {
        public final Color backgroundColor;
        public final javax.swing.border.Border border;
        public final LayoutManager layout;
        public final Insets padding;
        
        public PanelTheme(Color bg, javax.swing.border.Border border, LayoutManager layout, Insets padding) {
            this.backgroundColor = bg;
            this.border = border;
            this.layout = layout;
            this.padding = padding;
        }
    }
    
    /**
     * Color scheme configuration.
     */
    class ColorScheme {
        public final Color primary;
        public final Color secondary;
        public final Color accent;
        public final Color background;
        public final Color surface;
        public final Color error;
        public final Color success;
        public final Color warning;
        
        public ColorScheme(Color primary, Color secondary, Color accent, Color background, 
                          Color surface, Color error, Color success, Color warning) {
            this.primary = primary;
            this.secondary = secondary;
            this.accent = accent;
            this.background = background;
            this.surface = surface;
            this.error = error;
            this.success = success;
            this.warning = warning;
        }
    }
    
    /**
     * Font specification.
     */
    class FontSpec {
        public final String family;
        public final int style;
        public final float size;
        public final boolean antiAliasing;
        
        public FontSpec(String family, int style, float size, boolean antiAliasing) {
            this.family = family;
            this.style = style;
            this.size = size;
            this.antiAliasing = antiAliasing;
        }
    }
    
    /**
     * Spacing configuration.
     */
    class SpacingConfiguration {
        public final int small;
        public final int medium;
        public final int large;
        public final int extraLarge;
        
        public SpacingConfiguration(int small, int medium, int large, int extraLarge) {
            this.small = small;
            this.medium = medium;
            this.large = large;
            this.extraLarge = extraLarge;
        }
    }
    
    /**
     * Complete style configuration for persistence.
     */
    class StyleConfiguration {
        public final ButtonTheme defaultButtonTheme;
        public final PanelTheme defaultPanelTheme;
        public final ColorScheme colorScheme;
        public final FontSpec defaultFont;
        public final SpacingConfiguration spacing;
        public final String themeName;
        public final long timestamp;
        
        public StyleConfiguration(ButtonTheme buttonTheme, PanelTheme panelTheme, 
                                ColorScheme colorScheme, FontSpec font, 
                                SpacingConfiguration spacing, String themeName) {
            this.defaultButtonTheme = buttonTheme;
            this.defaultPanelTheme = panelTheme;
            this.colorScheme = colorScheme;
            this.defaultFont = font;
            this.spacing = spacing;
            this.themeName = themeName;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    // === ENUMS FOR TYPE SAFETY ===
    
    enum ButtonState { NORMAL, HOVER, PRESSED, DISABLED, FOCUSED }
    enum PanelType { MAIN, FORM, LIST, DIALOG, TOOLBAR }
    enum BorderType { NONE, LINE, RAISED, LOWERED, COMPOUND, TITLED }
    enum ComponentState { ACTIVE, INACTIVE, SELECTED, ERROR, SUCCESS }
    enum LayoutType { BORDER, FLOW, GRID, BOX, CARD, GRIDBAG }
    enum SpacingType { COMPACT, NORMAL, COMFORTABLE, SPACIOUS }
    
    /**
     * Additional styling classes for type safety.
     */
    class ButtonStyle {
        public final Color background;
        public final Color foreground;
        public final Font font;
        public final javax.swing.border.Border border;
        
        public ButtonStyle(Color bg, Color fg, Font font, javax.swing.border.Border border) {
            this.background = bg;
            this.foreground = fg;
            this.font = font;
            this.border = border;
        }
    }
    
    class ColorConfiguration {
        public final Color primary;
        public final Color secondary;
        public final Color background;
        public final Color text;
        
        public ColorConfiguration(Color primary, Color secondary, Color background, Color text) {
            this.primary = primary;
            this.secondary = secondary;
            this.background = background;
            this.text = text;
        }
    }
    
    class ApplicationTheme {
        public final String name;
        public final StyleConfiguration styleConfig;
        public final boolean isDark;
        public final String version;
        
        public ApplicationTheme(String name, StyleConfiguration config, boolean isDark, String version) {
            this.name = name;
            this.styleConfig = config;
            this.isDark = isDark;
            this.version = version;
        }
    }
}
