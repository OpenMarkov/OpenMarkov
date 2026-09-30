/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.node;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.graphic.VisualNode;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.AbstractButton;
import java.awt.Component;
import java.awt.Container;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * In edition mode, accepting the add finding dialog without touching it keeps the node's finding, and
 * undoing a change made in the dialog restores the finding the node had.
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class ChangingAFindingInTheAddFindingDialogCanBeUndoneTest {

    private static boolean ignoreStorage;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    private static AbstractButton button(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof AbstractButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container child) {
                AbstractButton button = button(child, text);
                if (button != null) {
                    return button;
                }
            }
        }
        return null;
    }

    @Tag(TestSpeed.MEDIUM)
    @Test void thePreviousFindingComesBack() throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("findings");
        Variable x = new Variable("X", "s0", "s1", "s2");
        net.addNode(x, NodeType.CHANCE);

        MainPanel mainPanel = MainGUI.INSTANCE.mainPanel;
        NetworkEditorPanel tab = mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
        try {
            VisualNode node = tab.getVisualNetwork().getVisualNodeOf(net.getNode(x));
            EvidenceCase evidence = tab.getEvidenceManager().getPreResolutionEvidence();
            tab.getEvidenceManager().setNewFinding(node, null, new Finding(x, x.getState("s2")), false);

            AddFindingDialog untouched = new AddFindingDialog(MainGUI.INSTANCE, net, x, node, evidence.getFinding(x), tab);
            untouched.getOKButton().doClick();
            assertEquals("s2", evidence.getFinding(x).getState());

            AddFindingDialog changed = new AddFindingDialog(MainGUI.INSTANCE, net, x, node, evidence.getFinding(x), tab);
            button(changed.getContentPane(), "s1").doClick();
            changed.getOKButton().doClick();
            assertEquals("s1", evidence.getFinding(x).getState());

            net.getPNESupport().undo();
            assertEquals("s2", evidence.getFinding(x).getState());
        } finally {
            mainPanel.forceClose(tab);
        }
    }
}
