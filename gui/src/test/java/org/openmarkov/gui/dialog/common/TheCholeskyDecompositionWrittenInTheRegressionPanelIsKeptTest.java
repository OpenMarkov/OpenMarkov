/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.junit.jupiter.api.BeforeEach;
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
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A node whose regression gives the uncertainty of its two coefficients as a covariance matrix. In its panel the
 * user chooses «Cholesky decomposition» and writes one.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheCholeskyDecompositionWrittenInTheRegressionPanelIsKeptTest {

    private static final double[] WRITTEN = { 2.0, 0.5, 3.0 };

    private ProbNet probNet;
    private Node node;

    @BeforeEach void aNodeWithACovarianceMatrix() {
        probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.setName("regression");
        Variable parent = new Variable("X");
        Variable child = new Variable("Y");
        Node parentNode = probNet.addNode(parent, NodeType.CHANCE);
        parentNode.setPotential(new LinearCombinationPotential(List.of(parent), PotentialRole.CONDITIONAL_PROBABILITY));
        node = probNet.addNode(child, NodeType.CHANCE);
        probNet.addLink(parentNode, node, true);
        GLMPotential potential =
                new LinearCombinationPotential(List.of(child, parent), PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setCovarianceMatrix(new double[]{ 1.0, 0.0, 1.0 });
        node.setPotential(potential);
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

    private static JTable uncertaintyTable(GLMPotentialPanel panel) {
        return find((Container) find(panel, JComboBox.class).getParent(), JTable.class);
    }

    private void writeTheDecomposition() throws Exception {
        GLMPotentialPanel panel = new GLMPotentialPanel(node);
        find(panel, JComboBox.class).setSelectedItem("Cholesky decomposition");
        JTable table = uncertaintyTable(panel);
        table.setValueAt(WRITTEN[0], 1, 1);
        table.setValueAt(WRITTEN[1], 2, 1);
        table.setValueAt(WRITTEN[2], 2, 2);
        panel.saveChanges();
    }

    private GLMPotential potential(ProbNet network) {
        return (GLMPotential) network.getNode("Y").getPotentials().get(0);
    }

    @Test void theNodeKeepsOnlyTheDecomposition() throws Exception {
        writeTheDecomposition();

        assertNull(potential(probNet).getCovarianceMatrix());
        assertArrayEquals(WRITTEN, potential(probNet).getCholeskyDecomposition());
    }

    @Test void thePanelShowsItWhenItIsOpenedAgain() throws Exception {
        writeTheDecomposition();

        GLMPotentialPanel reopened = new GLMPotentialPanel(node);
        assertEquals("Cholesky decomposition", find(reopened, JComboBox.class).getSelectedItem());
        assertEquals(WRITTEN[1], uncertaintyTable(reopened).getValueAt(2, 1));
    }

    @Test void itIsWhatTheFileHas() throws Exception {
        writeTheDecomposition();
        Path written = Files.createTempDirectory("cholesky").resolve("regression.pgmx");
        new PGMXWriter_0_2().write(written.toString(), probNet, List.of());

        ProbNet reread = new PGMXReader().read(written.toUri().toURL()).probNet();
        assertArrayEquals(WRITTEN, potential(reread).getCholeskyDecomposition());
    }

    @Test void uncheckingUncertaintyLeavesTheNodeWithoutIt() throws Exception {
        GLMPotentialPanel panel = new GLMPotentialPanel(node);
        find(panel, JComboBox.class).setSelectedItem("Cholesky decomposition");
        find(panel, JCheckBox.class).setSelected(false);
        panel.saveChanges();

        assertNull(potential(probNet).getCovarianceMatrix());
        assertNull(potential(probNet).getCholeskyDecomposition());
    }
}
