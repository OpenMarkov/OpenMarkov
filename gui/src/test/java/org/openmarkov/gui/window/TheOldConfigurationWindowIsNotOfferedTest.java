/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * «OpenMarkov configuration», at the end of the Tools menu, showed and edited a store of preferences the program no
 * longer reads, and its only working button cleared the real ones without asking (error 175 of the pending list). The
 * team decided to remove it: «Settings...», in the Edit menu, works on the real preferences.
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheOldConfigurationWindowIsNotOfferedTest {

    private static boolean ignoreStorage;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    private static List<String> commands(JMenu menu, List<String> found) {
        for (Component component : menu.getMenuComponents()) {
            if (component instanceof JMenu submenu) {
                commands(submenu, found);
            } else if (component instanceof JMenuItem item) {
                found.add(item.getActionCommand());
            }
        }
        return found;
    }

    @Tag(TestSpeed.MEDIUM)
    @Test void onlyTheSettingsAreOffered() {
        var menuBar = MainGUI.INSTANCE.mainPanel.getMainMenu();
        List<String> commands = new ArrayList<>();
        for (int index = 0; index < menuBar.getMenuCount(); index++) {
            commands(menuBar.getMenu(index), commands);
        }

        assertFalse(commands.contains("Tools.Configuration"), commands.toString());
        assertTrue(commands.contains("OpenSettings"), commands.toString());
    }
}
