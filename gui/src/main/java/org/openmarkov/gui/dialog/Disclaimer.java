/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.configuration.UserPreferences;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.Component;
import java.awt.Dimension;

/**
 * The disclaimer of OpenMarkov, which frees its authors of any liability. The user accepts it the first time
 * OpenMarkov starts, and the About window shows it.
 *
 * @author Manuel Arias
 */
public final class Disclaimer {

    /** The width of the disclaimer in its own window, in pixels. */
    private static final int WIDTH_IN_ITS_WINDOW = 520;

    private Disclaimer() {
    }

    /**
     * Whether the user accepts the disclaimer. Until the user accepts it, it is asked in a window whose only button
     * is «Accept»; closing the window is refusing it. The acceptance is remembered in the preferences.
     *
     * @param owner the window over which the disclaimer is asked, or {@code null}
     */
    public static boolean isAcceptedBy(@Nullable Component owner) {
        if (UserPreferences.DISCLAIMER_ACCEPTED.get()) {
            return true;
        }
        StringDatabase strings = StringDatabase.getUniqueInstance();
        String accept = strings.getString("Disclaimer.Accept.Text");
        JLabel text = wrapped("<html>" + message() + "</html>", WIDTH_IN_ITS_WINDOW);
        int chosen = JOptionPane.showOptionDialog(owner, text, strings.getString("Disclaimer.Title.Text"),
                                                  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                                                  new Object[] { accept }, accept);
        if (chosen != 0) {
            return false;
        }
        UserPreferences.DISCLAIMER_ACCEPTED.set(true);
        return true;
    }

    /** The disclaimer opened by its title in bold, as a centred paragraph of the given width, in pixels. */
    public static JLabel labelWithTitle(int width) {
        String title = StringDatabase.getUniqueInstance().getString("Disclaimer.Title.Text");
        return wrapped("<html><div style='text-align: center'><b>" + title + ".</b> " + message() + "</div></html>",
                       width);
    }

    /** The text of the disclaimer. */
    public static String message() {
        return StringDatabase.getUniqueInstance().getString("Disclaimer.Message.Text");
    }

    /**
     * A label with the HTML, whose lines break at the given width, in pixels. A width written in the HTML would not
     * do: Swing takes each of its pixels as 1.3.
     */
    static JLabel wrapped(String html, int width) {
        JLabel label = new JLabel(html);
        View view = (View) label.getClientProperty(BasicHTML.propertyKey);
        view.setSize(width, 0);
        label.setPreferredSize(new Dimension(width, (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS))));
        return label;
    }
}
