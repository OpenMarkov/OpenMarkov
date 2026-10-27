/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.full;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.configuration.UserPreferences;
import org.openmarkov.gui.window.MainGUI;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Tools menu offered every user three tools meant for the developers of OpenMarkov, one of which, «Edits history»,
 * kept a processor busy and could hang the program (error 172 of the pending list). As the team decided, they are
 * offered only when «Show developer tools» is ticked in the settings.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheDeveloperToolsAreOfferedOnlyWhenAskedForTest {

    private static final List<String> DEVELOPER_TOOLS = List.of("Edits history (Developers' tool)",
                                                                "Break-point (Developers' tool)",
                                                                "Hover GUI logger (Developers' tool)");

    private static boolean ignoreStorage;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    /** The entries of the Tools menu, as it is filled when the user opens it. */
    private static List<String> toolsMenu() {
        JMenuBar menuBar = MainGUI.INSTANCE.mainPanel.getMainMenu();
        List<String> texts = new ArrayList<>();
        for (int index = 0; index < menuBar.getMenuCount(); index++) {
            JMenu menu = menuBar.getMenu(index);
            if ("Tools".equals(menu.getText())) {
                menu.setSelected(true);
                menu.setSelected(false);
                for (Component component : menu.getMenuComponents()) {
                    if (component instanceof JMenuItem item) {
                        texts.add(item.getText());
                    }
                }
            }
        }
        return texts;
    }

    @Test void onlyWhenTheSettingsAskForThem() {
        boolean shown = UserPreferences.SHOW_DEVELOPER_TOOLS.get();
        try {
            UserPreferences.SHOW_DEVELOPER_TOOLS.set(false);
            List<String> withoutThem = toolsMenu();
            assertTrue(withoutThem.contains("Sort probnet"), withoutThem.toString());
            assertTrue(DEVELOPER_TOOLS.stream().noneMatch(withoutThem::contains), withoutThem.toString());

            UserPreferences.SHOW_DEVELOPER_TOOLS.set(true);
            List<String> withThem = toolsMenu();
            assertTrue(withThem.containsAll(DEVELOPER_TOOLS), withThem.toString());
        } finally {
            UserPreferences.SHOW_DEVELOPER_TOOLS.set(shown);
        }
    }
}
