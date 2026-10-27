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
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the panel of a regression, the cell of each coefficient lies within the part of the table that is shown. The
 * table used to sit inside a second scroll pane narrower than itself, and the numbers, aligned to the right, were out
 * of sight.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheCoefficientsOfARegressionCanBeSeenTest {

    private static GLMPanel regressionWithTwoCoefficients() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable parent = new Variable("X");
        Variable child = new Variable("Y");
        Node parentNode = probNet.addNode(parent, NodeType.CHANCE);
        parentNode.setPotential(new LinearCombinationPotential(List.of(parent), PotentialRole.CONDITIONAL_PROBABILITY));
        Node node = probNet.addNode(child, NodeType.CHANCE);
        probNet.addLink(parentNode, node, true);
        GLMPotential potential =
                new LinearCombinationPotential(List.of(child, parent), PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setCoefficients(new double[]{ 1.5, 2.5 });
        node.setPotential(potential);

        GLMPotentialPanel panel = new GLMPotentialPanel(node);
        panel.setSize(panel.getPreferredSize());
        layOut(panel);
        return find(panel, GLMPanel.class);
    }

    /** Lays out the whole panel as a window would, also without a screen. */
    private static void layOut(Container container) {
        container.doLayout();
        for (Component component : container.getComponents()) {
            if (component instanceof Container inner) {
                layOut(inner);
            }
        }
    }

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

    @Test void eachCoefficientIsWithinWhatTheTableShows() {
        List<JTable> tables = new ArrayList<>();
        regressionWithTwoCoefficients().getValuesTable().onScrollableTable(tables::add);
        JTable coefficients = tables.getFirst();
        assertEquals(2, coefficients.getRowCount());

        Rectangle shown = coefficients.getVisibleRect();
        for (int row = 0; row < coefficients.getRowCount(); row++) {
            Rectangle cell = coefficients.getCellRect(row, 0, true);
            assertTrue(shown.contains(cell), "row " + row + ": the cell " + cell + " is not within " + shown);
        }
    }
}
