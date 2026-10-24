/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.commonComponents;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.dialog.node.NodePropertiesDialog;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.MainPanel;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The selector of the criterion of a utility node shows the name of each criterion, not «Cost criterion named …».
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialogs need a screen")
class TheCriterionOfAUtilityShowsItsNameTest {

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
    @Test void theCriterionOfAUtility() throws Exception {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.setName("criteria");
        Criterion cost = new Criterion("Cost");
        net.setDecisionCriteria(new ArrayList<>(List.of(cost, new Criterion("Effectiveness"))));
        Variable utility = new Variable("U");
        utility.setDecisionCriterion(cost);
        Node node = net.addNode(utility, NodeType.UTILITY);
        MainPanel mainPanel = MainGUI.INSTANCE.mainPanel;
        NetworkEditorPanel tab = mainPanel.getMainPanelListenerAssistant().createNewFrame(net);
        NodePropertiesDialog[] dialog = new NodePropertiesDialog[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    dialog[0] = new NodePropertiesDialog(MainGUI.INSTANCE, tab, tab.getVisualNetwork().getVisualNodeOf(node), false, false);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                assertEquals(List.of("Cost"), firstItemsShown(dialog[0], Criterion.class));
            });
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                if (dialog[0] != null) {
                    dialog[0].dispose();
                }
                mainPanel.forceClose(tab);
            });
        }
    }

    /** The text each selector of {@code type} inside {@code container} shows for its first item. */
    private static List<String> firstItemsShown(Container container, Class<?> type) {
        List<String> shown = new ArrayList<>();
        for (JComboBox<?> comboBox : comboBoxes(container, new ArrayList<>())) {
            if (comboBox.getItemCount() > 0 && type.isInstance(comboBox.getItemAt(0))) {
                @SuppressWarnings("unchecked") var renderer = (javax.swing.ListCellRenderer<Object>) comboBox.getRenderer();
                Component cell = renderer.getListCellRendererComponent(new JList<>(), comboBox.getItemAt(0), 0, false, false);
                shown.add(((JLabel) cell).getText());
            }
        }
        return shown;
    }

    private static List<JComboBox<?>> comboBoxes(Container container, List<JComboBox<?>> found) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComboBox<?> comboBox) {
                found.add(comboBox);
            } else if (component instanceof Container inner) {
                comboBoxes(inner, found);
            }
        }
        return found;
    }
}
