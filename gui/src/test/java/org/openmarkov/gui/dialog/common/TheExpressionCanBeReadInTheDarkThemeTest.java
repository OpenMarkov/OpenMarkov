/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import com.formdev.flatlaf.FlatDarculaLaf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.Theme;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.configuration.UserPreferences;

import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * In the dark theme, the box in which an expression is written keeps black text on its green or red background, so
 * what is written can be read whether the expression is valid or not.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialog needs a screen")
class TheExpressionCanBeReadInTheDarkThemeTest {

    private boolean ignoreStorage;
    private Theme theme;
    private LookAndFeel lookAndFeel;

    @BeforeEach void useTheDarkTheme() throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        theme = UserPreferences.PREFERRED_THEME.get();
        lookAndFeel = UIManager.getLookAndFeel();
        UserPreferences.PREFERRED_THEME.set(Theme.DARK);
        UIManager.setLookAndFeel(new FlatDarculaLaf());
    }

    @AfterEach void restoreTheTheme() throws Exception {
        UserPreferences.PREFERRED_THEME.set(theme);
        UIManager.setLookAndFeel(lookAndFeel);
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Test void validAndInvalid() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ArithmeticExpressionDialog dialog = new ArithmeticExpressionDialog(null, List.of(new Variable("A")), "");
            try {
                JTextField box = textField(dialog.getContentPane());
                box.setText("1 + 1");
                assertEquals(new Color(0, 255, 0), box.getBackground());
                assertEquals(Color.BLACK, box.getForeground());
                box.setText("1 +");
                assertEquals(new Color(255, 0, 0), box.getBackground());
                assertEquals(Color.BLACK, box.getForeground());
            } finally {
                dialog.dispose();
            }
        });
    }

    private static JTextField textField(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTextField field) {
                return field;
            }
            if (component instanceof Container inner) {
                JTextField found = textField(inner);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
