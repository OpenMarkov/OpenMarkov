package org.openmarkov.gui.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.function.Supplier;

public class GUIColor {
    
    private final Color defaultColor;
    private @Nullable EnumMap<Theme, ColorResolver> themedColors;
    
    public GUIColor(@NotNull Color defaultColor) {
        this.defaultColor = defaultColor;
    }
    
    private void initializeThemedColors() {
        if (this.themedColors == null) {
            this.themedColors = new EnumMap<>(Theme.class);
        }
    }
    
    public GUIColor inDark(@NotNull Color color) {
        return this.inTheme(Theme.DARK, color);
    }
    
    public GUIColor inDark(Supplier<Color> color) {
        return this.inTheme(Theme.DARK, color);
    }
    
    public GUIColor inTheme(Theme theme, @NotNull Color color) {
        this.initializeThemedColors();
        this.themedColors.put(theme, new RawColor(color));
        return this;
    }
    
    public GUIColor inTheme(Theme theme, Supplier<Color> color) {
        this.initializeThemedColors();
        this.themedColors.put(theme, new LambdaColor(color));
        return this;
    }
    
    public GUIColor negativizeInDark() {
        return this.negativizeColorInTheme(Theme.DARK);
    }
    
    public GUIColor negativizeColorInTheme(Theme theme) {
        this.initializeThemedColors();
        return this.inTheme(theme, new Color(
                255 - this.defaultColor.getRed(),
                255 - this.getColor().getGreen(),
                255 - this.defaultColor.getBlue(),
                this.defaultColor.getAlpha()));
    }
    
    public @NotNull Color getUnthemedColor() {
        return this.defaultColor;
    }
    
    public @NotNull Color getColor() {
        if (this.themedColors == null) {
            return this.defaultColor;
        }
        Theme theme = LocalPreferences.PREFERRED_THEME.get();
        HashSet<Theme> visitedThemes = new HashSet<>();
        while (theme != null) {
            if (!visitedThemes.add(theme)) {
                break;
            }
            switch (this.themedColors.get(theme)) {
                case LambdaColor lambdaColor -> {
                    Color resultingColor = lambdaColor.colorSupplier.get();
                    if (resultingColor != null) {
                        return resultingColor;
                    }
                }
                case RawColor(Color rawColor) -> {
                    return rawColor;
                }
                case null -> {
                }
            }
            theme = GUIColor.themeInderection(theme);
        }
        return this.defaultColor;
    }
    
    private static Theme themeInderection(Theme originalTheme) {
        return switch (originalTheme) {
            case SYSTEM_LF, DARK -> Theme.LIGHT;
            case SYNC_OS -> Theme.OSisDark()?Theme.DARK:Theme.LIGHT;
            case LIGHT -> Theme.SYSTEM_LF;
        };
    }
    
    private sealed interface ColorResolver {
    }
    
    record RawColor(@NotNull Color color) implements ColorResolver {
    }
    
    record LambdaColor(Supplier<Color> colorSupplier) implements ColorResolver {
    }
    
}
