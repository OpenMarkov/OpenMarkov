package org.openmarkov.gui.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.gui.componentBuilder.JMenuItemBuilder;
import org.openmarkov.gui.toolplugin.ToolPlugin;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.java.collectionsUtils.streamUtils.StreamUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class UILookAndFeelPlugin implements ToolPlugin {
    
    @Override public @NotNull ToolPluginGroup pluginGroup() {
        return ToolPluginGroup.USER_EXPERIENCE;
    }
    
    @Override public int priorityInGroup() {
        return 0;
    }
    
    @Override public JMenuItem toMenuItem() {
        return new JMenuItemBuilder("Change UI Look and Feel")
                .withItems(Arrays.stream(Theme.values()).map(UILookAndFeelPlugin::themeToButton))
                .build();
    }
    
    private static JMenuItem themeToButton(Theme theme) {
        boolean selected = LocalPreferences.PREFERRED_THEME.get() == theme;
        return new JMenuItemBuilder(theme.toUIString())
                .asRadio()
                .selected(selected)
                .onClick(() -> {
                    LocalPreferences.PREFERRED_THEME.set(theme);
                    UILookAndFeelPlugin.updateInterfaceToLook();
                    MainGUI.INSTANCE.mainPanel.getMainMenu().reInitialize();
                })
                .build();
    }
    
    public static void updateInterfaceToLook() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        LocalPreferences.PREFERRED_THEME.get().setlookAndFeel();
        Arrays.stream(Window.getWindows()).forEach(SwingUtilities::updateComponentTreeUI);
    }
    
    static {
        AtomicBoolean lastWasDark= new AtomicBoolean(Theme.OSisDark());
        com.jthemedetecor.OsThemeDetector.getDetector().registerListener(newIsDark -> {
            if(LocalPreferences.PREFERRED_THEME.get()!=Theme.SYNC_OS){
                return;
            }
            if (lastWasDark.get()==newIsDark) {
                return;
            }
            lastWasDark.set(newIsDark);
            SwingUtilities.invokeLater(()->{
                try {
                    UILookAndFeelPlugin.updateInterfaceToLook();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException e) {
                    throw new UnreachableException(e);
                }
            });
        });
    }
    
}
