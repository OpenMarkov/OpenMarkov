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
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * In a temporal model, the properties of a node whose twin is in another cycle are accepted with "OK"
 * without touching the name, and leaving the name field without changing it records no edit.
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialog needs a screen")
class ThePropertiesOfANodeWithATwinCanBeAcceptedTest {

    private static boolean ignoreStorage;

    /** Building the main window must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Tag(TestSpeed.MEDIUM)
    @Test void okClosesTheDialog() throws Exception {
        ProbNet net = new ProbNet(MIDType.getUniqueInstance());
        net.setName("twins");
        Node node = net.addNode(new Variable("A [0]", "a0", "a1"), NodeType.CHANCE);
        net.addNode(new Variable("A [1]", "a0", "a1"), NodeType.CHANCE);
        MainPanel mainPanel = MainGUI.INSTANCE.mainPanel;
        NetworkEditorPanel tab = mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
        net.getPNESupport().setWithUndo(true);
        NodePropertiesDialog[] dialog = new NodePropertiesDialog[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    dialog[0] = new NodePropertiesDialog(MainGUI.INSTANCE, tab, tab.getVisualNetwork().getVisualNodeOf(node), false, false);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            SwingUtilities.invokeLater(() -> dialog[0].setVisible(true));
            long end = System.currentTimeMillis() + 10_000;
            while (!dialog[0].isShowing() && System.currentTimeMillis() < end) {
                Thread.sleep(100);
            }
            SwingUtilities.invokeAndWait(() -> {
                NodeDefinitionPanel panel = theOnly(dialog[0], NodeDefinitionPanel.class);
                JTextField nameField = theOnly(panel, JTextField.class, "A");
                panel.focusLost(new FocusEvent(nameField, FocusEvent.FOCUS_LOST));
            });
            assertFalse(net.getPNESupport().getCanUndo(), "leaving the name field recorded an edit");

            SwingUtilities.invokeAndWait(() -> dialog[0].getOKButton().doClick());
            assertFalse(dialog[0].isShowing(), "the dialog is still open");
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                if (dialog[0] != null) {
                    dialog[0].dispose();
                }
                mainPanel.forceClose(tab);
            });
        }
    }

    private static <T> T theOnly(Container container, Class<T> type) {
        List<T> found = new ArrayList<>();
        collect(container, type, found);
        return found.getFirst();
    }

    private static JTextField theOnly(Container container, Class<JTextField> type, String text) {
        List<JTextField> found = new ArrayList<>();
        collect(container, type, found);
        return found.stream().filter(field -> field.getText().equals(text)).findFirst().orElseThrow();
    }

    private static <T> void collect(Container container, Class<T> type, List<T> found) {
        for (Component component : container.getComponents()) {
            if (type.isInstance(component)) {
                found.add(type.cast(component));
            }
            if (component instanceof Container inner) {
                collect(inner, type, found);
            }
        }
    }
}
