/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.window.MainGUI;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Each row of the help of shortcuts that names a key with Ctrl gives the key of its menu entry. The help promised
 * «Ctrl + W» for «Save and reopen», which closes the network, and «Ctrl + A/Q» for the previous and next tab, of which
 * «Ctrl + Q» closes OpenMarkov (error 177 of the pending list).
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheShortcutHelpGivesTheKeysOfTheMenusTest {

    private static JMenuItem item(JMenuBar menuBar, String command) {
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            for (Component component : menu.getMenuComponents()) {
                if (component instanceof JMenuItem item && command.equals(item.getActionCommand())) {
                    return item;
                }
            }
        }
        return null;
    }

    /** The key as the help writes it, such as «Ctrl + Alt + S». */
    private static String written(KeyStroke key) {
        StringBuilder text = new StringBuilder();
        if ((key.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0) {
            text.append("Ctrl + ");
        }
        if ((key.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0) {
            text.append("Shift + ");
        }
        if ((key.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0) {
            text.append("Alt + ");
        }
        return text + KeyEvent.getKeyText(key.getKeyCode());
    }

    @Test void eachRowGivesTheKeyOfItsMenuEntry() {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            JMenuBar menuBar = MainGUI.INSTANCE.mainPanel.getMainMenu();
            Map<String, ActionCommands> rows = new LinkedHashMap<>();
            rows.put("Specific.SaveReopen", ActionCommands.SAVE_OPEN_NETWORK);
            rows.put("Specific.CloseNetwork", ActionCommands.CLOSE_TAB);
            rows.put("Specific.Exit", ActionCommands.EXIT_APPLICATION);
            rows.put("Common.New", ActionCommands.NEW_NETWORK);
            rows.put("Common.Open", ActionCommands.OPEN_NETWORK);
            rows.put("Common.Save", ActionCommands.SAVE_NETWORK);
            rows.put("Common.SelectAll", ActionCommands.SELECT_ALL);
            rows.put("Common.Cut", ActionCommands.CLIPBOARD_CUT);
            rows.put("Common.Copy", ActionCommands.CLIPBOARD_COPY);
            rows.put("Common.Paste", ActionCommands.CLIPBOARD_PASTE);
            rows.put("Common.Undo", ActionCommands.UNDO);
            rows.put("Common.Redo", ActionCommands.REDO);
            StringDatabase texts = StringDatabase.getUniqueInstance();
            for (var row : rows.entrySet()) {
                JMenuItem item = item(menuBar, row.getValue().getCommandName());
                assertNotNull(item, row.getKey());
                assertEquals(written(item.getAccelerator()), texts.getString("ShortcutsBox." + row.getKey() + ".Shortcut"),
                             row.getKey());
            }
            assertEquals("Ctrl + Shift + ←/→", texts.getString("ShortcutsBox.Specific.PreviousNext.Shortcut"));
        } finally {
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }
}
