package org.openmarkov.gui.configuration;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.openmarkov.core.exception.UnreachableException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Window;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public enum Theme {
    SYNC_OS,
    LIGHT,
    DARK,
    SYSTEM_LF;
    
    public String toUIString() {
        return switch (this) {
            case SYNC_OS -> "Sync with OS";
            case LIGHT -> "Light";
            case DARK -> "Dark (Beta)";
            case SYSTEM_LF -> "System (Might be unsupported)";
        };
    }
    
    public void setlookAndFeel() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (this == Theme.SYSTEM_LF) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            return;
        }
        UIManager.setLookAndFeel(switch (this) {
            case DARK -> new FlatDarculaLaf();
            case SYNC_OS -> OSisDark() ?
                    new FlatDarculaLaf() : new FlatLightLaf();
            case LIGHT -> new FlatLightLaf();
            case SYSTEM_LF -> null;
        });
    }
    
    public static boolean OSisDark() {
        return com.jthemedetecor.OsThemeDetector.getDetector().isDark();
    }
    
    public static void updateInterfaceToLook() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        UserPreferences.PREFERRED_THEME.get().setlookAndFeel();
        Arrays.stream(Window.getWindows()).forEach(SwingUtilities::updateComponentTreeUI);
    }
    
    static {
        AtomicBoolean lastWasDark = new AtomicBoolean(Theme.OSisDark());
        com.jthemedetecor.OsThemeDetector.getDetector().registerListener(newIsDark -> {
            if (UserPreferences.PREFERRED_THEME.get() != Theme.SYNC_OS) {
                return;
            }
            if (lastWasDark.get() == newIsDark) {
                return;
            }
            lastWasDark.set(newIsDark);
            SwingUtilities.invokeLater(() -> {
                try {
                    Theme.updateInterfaceToLook();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException e) {
                    throw new UnreachableException(e);
                }
            });
        });
    }
}
