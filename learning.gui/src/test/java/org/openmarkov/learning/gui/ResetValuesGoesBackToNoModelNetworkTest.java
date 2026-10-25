/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.learning.gui;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * With the open network chosen as the model network of the learning window, «Reset values» goes back to «Do not use
 * any model network», with what hangs from it disabled and unticked.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The learning window needs a screen")
class ResetValuesGoesBackToNoModelNetworkTest {

    private static boolean ignoreStorage;

    @BeforeAll static void doNotTouchThePreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreThePreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Test void resetChoosesNoModelNetwork() throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("model");
        net.addNode(new Variable("A", "yes", "no"), NodeType.CHANCE);
        SwingUtilities.invokeAndWait(() -> {
            NetworkEditorPanel tab = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
            JFrame owner = new JFrame();
            try {
                LearningDialog dialog = new LearningDialog(owner);
                ModelNetTabPanel modelNetTab = modelNetTabOf(dialog);
                modelNetTab.fromOpenMarkovRadioButton.doClick();
                assertTrue(modelNetTab.useNodePositionsCheckBox.isEnabled(), "with the open network chosen");

                button(dialog.getContentPane(), "Reset values").doClick();

                assertAll(() -> assertTrue(modelNetTab.noModelNetRadioButton.isSelected(), "no model network"),
                          () -> assertFalse(modelNetTab.fromOpenMarkovRadioButton.isSelected(), "the open network"),
                          () -> assertFalse(modelNetTab.useNodePositionsCheckBox.isEnabled(), "nodes enabled"),
                          () -> assertFalse(modelNetTab.useNodePositionsCheckBox.isSelected(), "nodes ticked"),
                          () -> assertFalse(modelNetTab.startFromModelNetCheckBox.isEnabled(), "start enabled"));
                dialog.dispose();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                owner.dispose();
                MainGUI.INSTANCE.mainPanel.forceClose(tab);
            }
        });
    }

    private static ModelNetTabPanel modelNetTabOf(LearningDialog dialog) throws Exception {
        Field field = LearningDialog.class.getDeclaredField("modelNetTabPanel");
        field.setAccessible(true);
        return (ModelNetTabPanel) field.get(dialog);
    }

    private static JButton button(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container inner && button(inner, text) != null) {
                return button(inner, text);
            }
        }
        return null;
    }
}
