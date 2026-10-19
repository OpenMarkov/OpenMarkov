/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.network;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.dialog.common.KeyTablePanel;
import org.openmarkov.gui.dialog.common.PrefixedKeyTablePanel;

import javax.swing.JButton;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the lists of criteria, agents and states, «Delete» needs a selected row and a list that may lose one: a network
 * keeps one criterion, may have no agents, and a variable keeps two states. «Delete» used to be enabled by the number
 * of rows, so after a deletion it stayed enabled without a selected row, and pressing it failed (error 206).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class DeleteNeedsASelectedRowTest {

    private static JButton delete(Container panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JButton button && "KeyTablePanel.removeValueButton".equals(button.getName())) {
                return button;
            }
            if (component instanceof Container inner && delete(inner) != null) {
                return delete(inner);
            }
        }
        return null;
    }

    private static void select(KeyTablePanel panel, int row) {
        if (row == -1) {
            panel.getValuesTable().onScrollableTable(table -> table.clearSelection());
        } else {
            panel.getValuesTable().setRowSelectionInterval(row, row);
        }
    }

    private static ProbNet anInfluenceDiagram() {
        return new ProbNet(InfluenceDiagramType.getUniqueInstance());
    }

    @Test void theListOfCriteria() {
        ProbNet net = anInfluenceDiagram();
        net.setDecisionCriteria(new ArrayList<>(List.of(new Criterion("cost"), new Criterion("effectiveness"),
                                                        new Criterion("QALY"))));
        DecisionCriteriaTablePanel panel = new DecisionCriteriaTablePanel(new String[] { "Id", "Name", "Unit" }, net, null);
        panel.setDataFromCriteria(net.getDecisionCriteria());
        JButton delete = delete(panel);

        assertFalse(delete.isEnabled(), "nothing selected");
        select(panel, 1);
        assertTrue(delete.isEnabled());
        select(panel, -1);
        assertFalse(delete.isEnabled(), "the selection removed");

        delete.doClick();
        assertEquals(3, net.getDecisionCriteria().size(), "pressed without a selected row");

        select(panel, 0);
        delete.doClick();
        assertEquals(List.of("effectiveness", "QALY"), net.getDecisionCriteria().stream().map(Criterion::getCriterionName).toList());
        assertEquals(0, panel.getValuesTable().getSelectedRow(), "the neighbour is selected");

        delete.doClick();
        assertEquals(1, net.getDecisionCriteria().size());
        assertFalse(delete.isEnabled(), "the last criterion stays");
    }

    @Test void theListOfAgents() {
        ProbNet net = anInfluenceDiagram();
        net.setAgents(new ArrayList<>(List.of(new StringWithProperties("Doctor"), new StringWithProperties("Patient"))));
        NetworkAgentsTablePanel panel = new NetworkAgentsTablePanel(new String[] { "Key", "Agents" }, net);
        panel.setDataFromAdvancedProperties(net.getAgents());
        JButton delete = delete(panel);

        assertFalse(delete.isEnabled(), "nothing selected");
        select(panel, 1);
        assertTrue(delete.isEnabled(), "one of two agents can be deleted");

        delete.doClick();
        assertEquals(List.of("Doctor"), net.getAgents().stream().map(StringWithProperties::getString).toList());
        assertEquals(0, panel.getValuesTable().getSelectedRow(), "the neighbour is selected");

        delete.doClick();
        assertTrue(net.getAgents().isEmpty(), "the last agent can be deleted too");
    }

    @Test void theListOfStates() {
        Node node = anInfluenceDiagram().addNode(new Variable("X", "x0", "x1"), NodeType.CHANCE);
        PrefixedKeyTablePanel panel = new PrefixedKeyTablePanel(new String[] { "Key", "State" },
                                                                new Object[][] { { "x0" }, { "x1" } }, "s", true, node);
        JButton delete = delete(panel);

        select(panel, 0);
        assertFalse(delete.isEnabled(), "a variable keeps two states");
    }
}
