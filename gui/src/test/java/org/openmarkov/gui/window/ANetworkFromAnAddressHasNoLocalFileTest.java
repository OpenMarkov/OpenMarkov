/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A network opened from an address took the path of the address as its local file, such as /redes/asia.pgmx, where
 * some ways of saving it wrote; and to hide it «Save» was turned off for every network after each edit, for the rest
 * of the session (error 171 of the pending list). Now it has no local file, as a new network, and «Save» follows the
 * changes of every network. A file: address goes through the same path as one of the internet.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class ANetworkFromAnAddressHasNoLocalFileTest {

    private static AbstractButton saveButton(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof AbstractButton button
                    && ActionCommands.SAVE_NETWORK.getCommandName().equals(button.getActionCommand())) {
                return button;
            }
            if (component instanceof Container inner && saveButton(inner) != null) {
                return saveButton(inner);
            }
        }
        return null;
    }

    @Test void itHasNoFileAndSaveFollowsTheOtherNetworks(@TempDir Path folder) throws Exception {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        ProbNet small = new ProbNet(BayesianNetworkType.getUniqueInstance());
        small.addNode(new Variable("A", "a0", "a1"), NodeType.CHANCE);
        Path file = folder.resolve("small.pgmx");
        new PGMXWriter_1_0().write(file.toString(), small, new ArrayList<>());
        MainPanel mainPanel = MainGUI.INSTANCE.mainPanel;
        NetworkEditorPanel[] tabs = new NetworkEditorPanel[2];
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    mainPanel.getMainPanelListenerAssistant().getFileHandler()
                             .openNetworkSource(new NetworkEditorPanel.NetworkSource.SourceURL(file.toUri().toURL()));
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                tabs[0] = mainPanel.getMainPanelListenerAssistant().getCurrentNetworkEditorPanel();
            });
            assertNull(tabs[0].getNetworkFile(), "The network from an address took a local file");
            assertEquals("small.pgmx", tabs[0].getProbNet().getName());

            ProbNet other = new ProbNet(BayesianNetworkType.getUniqueInstance());
            other.setName("other");
            SwingUtilities.invokeAndWait(() -> {
                tabs[1] = mainPanel.getMainPanelListenerAssistant().createNewFrame(other);
                try {
                    new AddNodeEdit(other, new Variable("B", "b0", "b1"), NodeType.CHANCE, null).executeEdit();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            AbstractButton save = saveButton(mainPanel.getStandardToolBar());
            assertNotNull(save);
            assertTrue(save.isEnabled(), "«Save» is off after a change of another network");
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                for (NetworkEditorPanel tab : tabs) {
                    if (tab != null) {
                        mainPanel.forceClose(tab);
                    }
                }
            });
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }
}
