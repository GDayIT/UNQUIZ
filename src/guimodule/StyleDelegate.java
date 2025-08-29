package guimodule;

import java.awt.*;
import java.util.function.*;
import javax.swing.*;

/**
 * Interface defining modular UI styling for the application using functional programming.
 * <p>
 * This interface enables complete modularity for styling:
 * <ul>
 *     <li>Button creation, styling, and state management</li>
 *     <li>Panel creation, theming, and layout configuration</li>
 *     <li>Color scheme management and dynamic color calculation</li>
 *     <li>Font creation and scaling</li>
 *     <li>Spacing and layout configuration</li>
 *     <li>Persistence of style configurations and theme switching</li>
 * </ul>
 * <p>
 * All methods return lambda expressions or functional interfaces to allow:
 * <ul>
 *     <li>Runtime modifications of styles</li>
 *     <li>Theme switching</li>
 *     <li>Serialization of style configurations</li>
 *     <li>Dynamic styling based on component or application state</li>
 * </ul>
 * <p>
 * This interface is tightly coupled with application modules for Questions, 
 * Themes, Leitner cards, and Sessions indirectly via UI styling.
 * 
 * @author D.Georgiou
 * @version 1.0
 */
public interface StyleDelegate {

    // ----------------------------
    // BUTTON STYLING
    // ----------------------------

    /** Creates a styled JButton with text */
    Function<String, JButton> createStyledButton();

    /** Applies a ButtonTheme to a JButton */
    BiFunction<JButton, ButtonTheme, JButton> applyButtonTheme();

    /** Returns ButtonStyle configuration based on ButtonState */
    Function<ButtonState, ButtonStyle> getButtonStyle();

    // ----------------------------
    // PANEL STYLING
    // ----------------------------

    /** Creates a styled JPanel based on PanelType */
    Function<PanelType, JPanel> createStyledPanel();

    /** Applies a PanelTheme to a JPanel */
    BiFunction<JPanel, PanelTheme, JPanel> applyPanelTheme();

    /** Creates a Border object based on BorderType */
    Function<BorderType, javax.swing.border.Border> createStyledBorder();

    // ----------------------------
    // COLOR SCHEME MANAGEMENT
    // ----------------------------

    /** Applies a ColorScheme and returns ColorConfiguration */
    Function<ColorScheme, ColorConfiguration> applyColorScheme();

    /** Calculates a color based on ComponentState */
    Function<ComponentState, Color> calculateColor();

    // ----------------------------
    // FONT MANAGEMENT
    // ----------------------------

    /** Creates a Font based on FontSpec */
    Function<FontSpec, Font> createStyledFont();

    /** Returns a Font scaler for dynamic resizing */
    Function<Float, UnaryOperator<Font>> createFontScaler();

    // ----------------------------
    // LAYOUT MANAGEMENT
    // ----------------------------

    /** Returns a LayoutManager based on LayoutType */
    Function<LayoutType, LayoutManager> createStyledLayout();

    /** Returns spacing configuration based on SpacingType */
    Function<SpacingType, SpacingConfiguration> getSpacing();

    // ----------------------------
    // PERSISTENCE
    // ----------------------------

    /** Saves StyleConfiguration for persistence */
    Consumer<StyleConfiguration> saveStyleConfiguration();

    /** Loads StyleConfiguration from persistent storage */
    Supplier<StyleConfiguration> loadStyleConfiguration();

    // ----------------------------
    // THEME MANAGEMENT
    // ----------------------------

    /** Applies an ApplicationTheme to the whole UI */
    Consumer<ApplicationTheme> applyApplicationTheme();

    /** Validates if a theme is correct and applicable */
    Predicate<ApplicationTheme> validateTheme();

    // ----------------------------
    // ENUMS AND DATA OBJECTS
    // ----------------------------

    enum ButtonState { NORMAL, HOVER, PRESSED, DISABLED, FOCUSED }
    enum PanelType { MAIN, FORM, LIST, DIALOG, TOOLBAR }
    enum BorderType { NONE, LINE, RAISED, LOWERED, COMPOUND, TITLED }
    enum ComponentState { ACTIVE, INACTIVE, SELECTED, ERROR, SUCCESS }
    enum LayoutType { BORDER, FLOW, GRID, BOX, CARD, GRIDBAG }
    enum SpacingType { COMPACT, NORMAL, COMFORTABLE, SPACIOUS }

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

    class ColorScheme {
        public final Color primary, secondary, accent, background, surface, error, success, warning;
        public ColorScheme(Color primary, Color secondary, Color accent, Color background,
                           Color surface, Color error, Color success, Color warning) {
            this.primary = primary; this.secondary = secondary; this.accent = accent;
            this.background = background; this.surface = surface; this.error = error;
            this.success = success; this.warning = warning;
        }
    }

    class FontSpec {
        public final String family;
        public final int style;
        public final float size;
        public final boolean antiAliasing;
        public FontSpec(String family, int style, float size, boolean antiAliasing) {
            this.family = family; this.style = style; this.size = size; this.antiAliasing = antiAliasing;
        }
    }

    class SpacingConfiguration {
        public final int small, medium, large, extraLarge;
        public SpacingConfiguration(int small, int medium, int large, int extraLarge) {
            this.small = small; this.medium = medium; this.large = large; this.extraLarge = extraLarge;
        }
    }

    class ButtonStyle {
        public final Color background, foreground;
        public final Font font;
        public final javax.swing.border.Border border;
        public ButtonStyle(Color bg, Color fg, Font font, javax.swing.border.Border border) {
            this.background = bg; this.foreground = fg; this.font = font; this.border = border;
        }
    }

    class ColorConfiguration {
        public final Color primary, secondary, background, text;
        public ColorConfiguration(Color primary, Color secondary, Color background, Color text) {
            this.primary = primary; this.secondary = secondary; this.background = background; this.text = text;
        }
    }

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
            this.defaultButtonTheme = buttonTheme; this.defaultPanelTheme = panelTheme;
            this.colorScheme = colorScheme; this.defaultFont = font; this.spacing = spacing;
            this.themeName = themeName; this.timestamp = System.currentTimeMillis();
        }
    }

    class ApplicationTheme {
        public final String name;
        public final StyleConfiguration styleConfig;
        public final boolean isDark;
        public final String version;
        public ApplicationTheme(String name, StyleConfiguration config, boolean isDark, String version) {
            this.name = name; this.styleConfig = config; this.isDark = isDark; this.version = version;
        }
    }
}