package org.openmarkov.gui.configuration;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.function.Supplier;

public class GUIColor extends ThemedItem<Color> {
    
    public GUIColor(@NotNull Color defaultItem) {
        super(defaultItem);
    }
    
    public GUIColor negativizeInDark() {
        return this.negativizeColorInTheme(Theme.DARK);
    }
    
    public GUIColor negativizeColorInTheme(Theme theme) {
        var defaultColor = this.getNoTheme();
        
        return this.inTheme(theme, new Color(
                255 - defaultColor.getRed(),
                255 - defaultColor.getGreen(),
                255 - defaultColor.getBlue(),
                defaultColor.getAlpha()));
    }
    
    
    public GUIColor inDark(@NotNull Color color) {
        super.inTheme(Theme.DARK, color);
        return this;
    }
    
    public GUIColor inDark(Supplier<Color> color) {
        super.inTheme(Theme.DARK, color);
        return this;
    }
    
    public GUIColor inTheme(Theme theme, @NotNull Color color) {
        super.inTheme(theme, color);
        return this;
    }
    
    public GUIColor inTheme(Theme theme, Supplier<Color> color) {
        super.inTheme(theme, color);
        return this;
    }
    
    public Color getColor() {
        return super.get();
    }
    
}
