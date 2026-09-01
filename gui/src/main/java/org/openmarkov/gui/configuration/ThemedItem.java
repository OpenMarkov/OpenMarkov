package org.openmarkov.gui.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

public class ThemedItem<T> {
    
    private final @NotNull T defaultItem;
    private @Nullable EnumMap<Theme, Resolver<T>> themedItems;
    
    public ThemedItem(@NotNull T defaultItem) {
        this.defaultItem = defaultItem;
    }
    
    private void initializeThemedItems() {
        if (this.themedItems == null) {
            this.themedItems = new EnumMap<>(Theme.class);
        }
    }
    
    public ThemedItem<T> inDark(@NotNull T T) {
        return this.inTheme(Theme.DARK, T);
    }
    
    public ThemedItem<T> inDark(Supplier<T> T) {
        return this.inTheme(Theme.DARK, T);
    }
    
    public ThemedItem<T> inTheme(Theme theme, @NotNull T T) {
        this.initializeThemedItems();
        this.themedItems.put(theme, new Raw(T));
        return this;
    }
    
    public ThemedItem<T> inTheme(Theme theme, Supplier<T> T) {
        this.initializeThemedItems();
        this.themedItems.put(theme, new Lambda(T));
        return this;
    }
    
    public @NotNull T get() {
        if (this.themedItems == null) {
            return this.defaultItem;
        }
        Theme theme = UserPreferences.PREFERRED_THEME.get();
        Set<Theme> visitedThemes = EnumSet.noneOf(Theme.class);
        while (theme != null) {
            if (!visitedThemes.add(theme)) {
                break;
            }
            switch (this.themedItems.get(theme)) {
                case ThemedItem.Lambda<T> lambdaColor -> {
                    T resultingColor = lambdaColor.supplier.get();
                    if (resultingColor != null) {
                        return resultingColor;
                    }
                }
                case Raw(T rawColor) -> {
                    return rawColor;
                }
                case null -> {
                }
            }
            theme = ThemedItem.themeInderection(theme);
        }
        return this.defaultItem;
    }
    
    public @NotNull T getNoTheme() {
        return this.defaultItem;
    }
    
    private static Theme themeInderection(Theme originalTheme) {
        return switch (originalTheme) {
            case SYSTEM_LF, DARK -> Theme.LIGHT;
            case SYNC_OS -> Theme.OSisDark() ? Theme.DARK : Theme.LIGHT;
            case LIGHT -> Theme.SYSTEM_LF;
        };
    }
    
    private sealed interface Resolver<T> {
    }
    
    private record Raw<T>(@NotNull T T) implements Resolver<T> {
    }
    
    private record Lambda<T>(Supplier<T> supplier) implements Resolver<T> {
    }
    
}
