/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.menu;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;

import javax.swing.JMenuItem;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link TreeContextualMenu}, the menu shown on a node of the decision tree.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TreeContextualMenuTest {

    /** The menu offers exporting the tree to Amua, with its label taken from the bundle. */
    @Test
    void theMenuOffersExportingTheTreeToAmua() {
        TreeContextualMenu menu = new TreeContextualMenu(e -> { }, false);
        String command = ActionCommands.TREE_EXPORT_AMUA.getCommandName();

        JMenuItem item = Arrays.stream(menu.getComponents())
                               .filter(JMenuItem.class::isInstance)
                               .map(JMenuItem.class::cast)
                               .filter(candidate -> command.equals(candidate.getActionCommand()))
                               .findFirst()
                               .orElseThrow(() -> new AssertionError("no menu item with command " + command));

        assertEquals("Export to Amua", item.getText());
        assertSame(item, menu.getJComponentActionCommand(command));
    }
}
