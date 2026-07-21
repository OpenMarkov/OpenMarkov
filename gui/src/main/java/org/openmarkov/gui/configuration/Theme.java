package org.openmarkov.gui.configuration;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

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
}
