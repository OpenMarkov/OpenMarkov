/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.treeadd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The tree of C asks for A: under «a0» it asks for B, and the branch «b0» carries the label «shared»; the branch «a1»
 * references it. Removing that label, or the subtree that holds it, in the editor of the tree gives «a1» its own copy
 * of the table, so the network still opens and gives the same probabilities. A file that already has a reference
 * without its label is refused with a message on opening (error 17).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class RemovingALabelInUseKeepsTheTreeThatUsesItTest {

    @TempDir Path folder;

    private ProbNet net;
    private Variable a;
    private Variable b;
    private Variable c;
    private JFrame frame;

    @BeforeEach void setUp() {
        a = new Variable("A", "a0", "a1");
        b = new Variable("B", "b0", "b1");
        c = new Variable("C", "c0", "c1");
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        for (Variable variable : List.of(a, b, c)) {
            net.addNode(variable, NodeType.CHANCE);
        }
        net.addLink(a, c, true);
        net.addLink(b, c, true);
        net.getNode(a).setPotential(table(a, 0.5, 0.5));
        net.getNode(b).setPotential(table(b, 0.5, 0.5));
        List<Variable> variables = List.of(c, a, b);
        TreeADDBranch b0 = new TreeADDBranch(states(b, 0), b, table(c, 0.9, 0.1), variables);
        b0.setLabel("shared");
        TreeADDBranch b1 = new TreeADDBranch(states(b, 1), b, table(c, 0.2, 0.8), variables);
        TreeADDPotential treeOfB = new TreeADDPotential(variables, b, PotentialRole.CONDITIONAL_PROBABILITY,
                                                        List.of(b0, b1));
        TreeADDBranch a0 = new TreeADDBranch(states(a, 0), a, treeOfB, variables);
        TreeADDBranch a1 = new TreeADDBranch(states(a, 1), a, "shared", variables);
        net.getNode(c).setPotential(new TreeADDPotential(variables, a, PotentialRole.CONDITIONAL_PROBABILITY,
                                                         List.of(a0, a1)));
    }

    @AfterEach void closeTheEditor() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    private static TablePotential table(Variable variable, double... values) {
        return new TablePotential(List.of(variable), PotentialRole.CONDITIONAL_PROBABILITY, values);
    }

    private static List<State> states(Variable variable, int index) {
        return List.of(variable.getStates()[index]);
    }

    private static double[] probabilitiesOfC(ProbNet network) throws Exception {
        Variable c = network.getVariable("C");
        VEPropagation propagation = new VEPropagation(network);
        propagation.setVariablesOfInterest(List.of(c));
        return propagation.getPosteriorValues().get(c).getValues();
    }

    private ProbNet savedAndOpened() throws Exception {
        Path file = folder.resolve("saved.pgmx");
        new PGMXWriter_1_0().write(file.toString(), net, List.of());
        return new PGMXReader().read(file.toUri().toURL()).probNet();
    }

    /** @return the path, in the editor, to «a0» */
    private static TreePath a0(TreeADDPotential root) {
        return new TreePath(new Object[] { root, root.getBranches().getFirst() });
    }

    /** @return the path, in the editor, to «b0», the branch with the label */
    private static TreePath b0(TreeADDPotential root) {
        TreeADDPotential treeOfB = (TreeADDPotential) root.getBranches().getFirst().getPotential();
        return a0(root).pathByAddingChild(treeOfB).pathByAddingChild(treeOfB.getBranches().getFirst());
    }

    /**
     * Opens the editor of the tree of C, right-clicks the branch at the end of {@code path}, chooses {@code item} in
     * its menu and keeps the edited tree as the relation of C.
     */
    private void chooseInTheEditor(Function<TreeADDPotential, TreePath> path,
                                   Function<TreeADDEditorPanel, JMenuItem> item) throws Exception {
        Node nodeC = net.getNode(c);
        SwingUtilities.invokeAndWait(() -> {
            TreeADDEditorPanel editor = new TreeADDEditorPanel(new TreeADDCellRenderer(net), nodeC);
            frame = new JFrame();
            frame.add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            Rectangle bounds = editor.jTree.getPathBounds(path.apply(editor.getTreePotential()));
            editor.jTree.dispatchEvent(new MouseEvent(editor.jTree, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                                                      0, (int) bounds.getCenterX(), (int) bounds.getCenterY(), 1, true,
                                                      MouseEvent.BUTTON3));
            item.apply(editor).doClick();
            editor.contextualMenu.setVisible(false);
            nodeC.setPotential(editor.getTreePotential());
        });
    }

    @Test
    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The editor needs a screen")
    void removingTheLabelGivesTheReferenceItsOwnTable() throws Exception {
        double[] before = probabilitiesOfC(net);

        chooseInTheEditor(RemovingALabelInUseKeepsTheTreeThatUsesItTest::b0, editor -> editor.removeLabel);

        TreeADDBranch a1 = ((TreeADDPotential) net.getNode(c).getPotentials().getFirst()).getBranches().get(1);
        assertFalse(a1.isReference());
        assertArrayEquals(before, probabilitiesOfC(savedAndOpened()), 1E-12);
        assertArrayEquals(new double[] { 0.725, 0.275 }, before, 1E-12);
    }

    @Test
    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The editor needs a screen")
    void removingTheSubtreeWithTheLabelGivesTheReferenceItsOwnTable() throws Exception {
        chooseInTheEditor(RemovingALabelInUseKeepsTheTreeThatUsesItTest::a0, editor -> editor.removeSubtree);

        TreeADDBranch a1 = ((TreeADDPotential) net.getNode(c).getPotentials().getFirst()).getBranches().get(1);
        assertFalse(a1.isReference());
        // «a0» is now uniform, and «a1» keeps 0.9 and 0.1
        assertArrayEquals(new double[] { (0.5 + 0.9) / 2, (0.5 + 0.1) / 2 }, probabilitiesOfC(savedAndOpened()), 1E-12);
    }

    /** A file saved with a reference whose label had been removed. */
    @Test
    void aFileWithAReferenceWithoutItsLabelIsRefused() throws Exception {
        TreeADDPotential tree = (TreeADDPotential) net.getNode(c).getPotentials().getFirst();
        ((TreeADDPotential) tree.getBranches().get(0).getPotential()).getBranches().get(0).setLabel(null);

        var refused = assertThrows(PGMXParserException.ReferenceWithoutLabel.class, this::savedAndOpened);

        assertEquals("The relation of C cannot be read: a branch of its tree uses the label shared, which no branch "
                     + "of that tree has.", refused.getExceptionMessage());
    }
}
