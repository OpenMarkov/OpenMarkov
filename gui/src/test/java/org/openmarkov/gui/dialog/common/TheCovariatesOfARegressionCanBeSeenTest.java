/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GLMPotential;
import org.openmarkov.core.model.network.potential.LinearCombinationPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JTable;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The panel of a regression shows which covariate each coefficient is of: the column of the covariates used to be
 * hidden, as if it were an internal key, and the panel showed a column of numbers that said nothing of what they
 * were, nor let rewrite a covariate with a double click (error 158 of the pending list).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheCovariatesOfARegressionCanBeSeenTest {

    private static <T extends Component> T find(Container container, Class<T> type) {
        for (Component component : container.getComponents()) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
            if (component instanceof Container inner) {
                T found = find(inner, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Test void theColumnOfTheCovariatesIsShownWithItsTitle() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable x = new Variable("X");
        Variable y = new Variable("Y");
        Node parentNode = probNet.addNode(x, NodeType.CHANCE);
        parentNode.setPotential(new LinearCombinationPotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY));
        Node node = probNet.addNode(y, NodeType.CHANCE);
        probNet.addLink(parentNode, node, true);
        GLMPotential potential = new LinearCombinationPotential(List.of(y, x), PotentialRole.CONDITIONAL_PROBABILITY);
        node.setPotential(potential);
        GLMPanel panel = find(new GLMPotentialPanel(node), GLMPanel.class);
        assertNotNull(panel);

        List<JTable> covariates = new ArrayList<>();
        panel.getValuesTable().onStickyTable(covariates::add);
        JTable table = covariates.getFirst();
        assertNotNull(table.getParent(), "The column of the covariates is out of the view");
        assertEquals("Covariate", table.getColumnName(0));
        List<String> shown = new ArrayList<>();
        for (int row = 0; row < table.getRowCount(); row++) {
            shown.add(String.valueOf(table.getValueAt(row, 0)));
        }
        List<String> expected = new ArrayList<>();
        for (var covariate : potential.getCovariates()) {
            expected.add(covariate.toString());
        }
        assertEquals(expected, shown);
    }
}
