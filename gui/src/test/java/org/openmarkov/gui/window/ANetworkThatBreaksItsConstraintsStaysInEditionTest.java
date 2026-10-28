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
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.menutoolbar.common.ActionCommands;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Asked to go to the inference mode, a network that breaks the constraints of its type stays in the edition mode, and
 * the message says which constraint it breaks (error 37 of the pending list). No algorithm accepts it, and the message
 * would have said only that there was no algorithm for the network.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class ANetworkThatBreaksItsConstraintsStaysInEditionTest {

    @Test void theInferenceModeSaysWhy() throws Exception {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        ProbNet net = ANetworkThatBreaksItsConstraintsOpensToBeCorrectedTest.withACycle(BayesianNetworkType.getUniqueInstance());
        MainPanel mainPanel = MainGUI.INSTANCE.mainPanel;
        NetworkEditorPanel[] tab = new NetworkEditorPanel[1];
        List<Throwable> causes = new ArrayList<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                tab[0] = mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
                try {
                    mainPanel.getMainPanelListenerAssistant().actionPerformed(new ActionEvent(
                            mainPanel, ActionEvent.ACTION_PERFORMED, ActionCommands.CHANGE_TO_INFERENCE_MODE.getCommandName()));
                } catch (RuntimeException refusal) {
                    for (Throwable cause = refusal; cause != null; cause = cause.getCause()) {
                        causes.add(cause);
                    }
                }
            });
            assertTrue(causes.stream().anyMatch(cause -> cause instanceof ConstraintViolatedException
                               && cause.toString().contains("The link A → B closes a cycle.")),
                       causes.toString());
            assertEquals(NetworkEditorPanel.WorkingMode.EDITION, tab[0].getWorkingMode());
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                if (tab[0] != null) {
                    mainPanel.forceClose(tab[0]);
                }
            });
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }
}
