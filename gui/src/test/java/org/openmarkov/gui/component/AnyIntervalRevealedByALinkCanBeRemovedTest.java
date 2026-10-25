/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.component;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.action.RevelationIntervalEdit;

import javax.swing.JButton;
import java.awt.Component;
import java.awt.Container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the revelation conditions of a link from a numeric variable, «Delete» removes the selected interval, also when
 * there are two or only one.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnyIntervalRevealedByALinkCanBeRemovedTest {

    static Link<Node> linkFromAge() {
        ProbNet network = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
        Node age = network.addNode(new Variable("Age", true, 0, 100, true, 1), NodeType.CHANCE);
        Node test = network.addNode(new Variable("Test", "negative", "positive"), NodeType.CHANCE);
        network.addLink(age, test, true);
        return network.getLink(age, test, true);
    }

    static RevelationArcDiscretizeTablePanel panelOf(Link<Node> link) {
        var panel = new RevelationArcDiscretizeTablePanel(new String[]{"Id", "Name", "(", "Low", ",", "Up", ")"}, link);
        panel.setPartitionedInterval();
        return panel;
    }

    static JButton button(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton button && name.equals(button.getName())) {
                return button;
            }
            if (component instanceof Container inner && button(inner, name) != null) {
                return button(inner, name);
            }
        }
        return null;
    }

    @ParameterizedTest @ValueSource(ints = {1, 2}) void theFirstIntervalCanBeRemoved(int intervals) throws Exception {
        Link<Node> link = linkFromAge();
        for (int interval = 0; interval < intervals; interval++) {
            new RevelationIntervalEdit(link, StateAction.ADD, interval, 0, false).executeEdit();
            new RevelationIntervalEdit(link, StateAction.MODIFY_VALUE_INTERVAL, interval, 10 * (interval + 1), false)
                    .executeEdit();
        }
        RevelationArcDiscretizeTablePanel panel = panelOf(link);
        panel.getValuesTable().onScrollableTable(scrollable -> scrollable.setRowSelectionInterval(0, 0));
        JButton delete = button(panel, "KeyTablePanel.removeValueButton");

        assertTrue(delete.isEnabled());
        delete.doClick();
        assertEquals(intervals - 1, link.getRevealingIntervals().size());
    }
}
