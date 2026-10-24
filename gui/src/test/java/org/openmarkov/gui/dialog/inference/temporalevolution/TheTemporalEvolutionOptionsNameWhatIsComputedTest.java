/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.inference.temporalevolution;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.SwingUtilities;
import java.awt.Window;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The window of options of the temporal evolution is titled with the variable whose evolution is computed, or with
 * the network when no node is selected, after a space.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheTemporalEvolutionOptionsNameWhatIsComputedTest {

    private static boolean ignoreStorage;

    private static NetworkEditorPanel tab;

    @BeforeAll static void openTheNetwork(@TempDir Path folder) throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        Path file = folder.resolve("MID-decision-in-a-cycle.pgmx");
        try (InputStream network = TheTemporalEvolutionOptionsNameWhatIsComputedTest.class.getResourceAsStream(
                "/MID-decision-in-a-cycle.pgmx")) {
            Files.copy(network, file);
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                MainGUI.INSTANCE.mainPanel.openNetwork(file.toString());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            tab = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().getCurrentNetworkEditorPanel();
        });
    }

    @AfterAll static void closeTheNetwork() throws Exception {
        SwingUtilities.invokeAndWait(() -> tab.close());
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Test void withANodeTheTitleNamesItsVariable() throws Exception {
        ProbNet net = tab.getProbNet();
        assertEquals("Temporal Evolution of: Health",
                     titleOf(() -> new TemporalEvolutionDialog(MainGUI.INSTANCE, net.getNode("Health [0]"), new EvidenceCase())));
    }

    @Test void withoutANodeTheTitleNamesTheNetwork() throws Exception {
        ProbNet net = tab.getProbNet();
        assertEquals("Temporal Evolution of: " + net.getName(),
                     titleOf(() -> new TemporalEvolutionDialog(MainGUI.INSTANCE, net, new EvidenceCase())));
    }

    /** Opens the window, which is modal, waits for it to show, and closes it. */
    private static String titleOf(Runnable opening) throws Exception {
        SwingUtilities.invokeLater(opening);
        TemporalEvolutionDialog dialog = null;
        for (long end = System.currentTimeMillis() + 20_000; dialog == null && System.currentTimeMillis() < end; ) {
            Thread.sleep(50);
            for (Window window : Window.getWindows()) {
                if (window instanceof TemporalEvolutionDialog shown && shown.isShowing()) {
                    dialog = shown;
                }
            }
        }
        assertNotNull(dialog, "the window of options did not show");
        String title = dialog.getTitle();
        SwingUtilities.invokeAndWait(dialog::dispose);
        return title;
    }
}
