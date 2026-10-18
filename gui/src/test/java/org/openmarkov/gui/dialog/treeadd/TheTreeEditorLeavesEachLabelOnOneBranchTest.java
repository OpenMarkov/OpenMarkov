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
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The tree of D asks for A: under «a0» it asks for B, and «b0», with the label «outer», asks for C, whose branch «c0»
 * has the label «inner»; «a1» references «outer» and «a2», «inner». «Remove label» on «b0» and «Remove reference» on
 * «a1», chosen in the editor, leave a single branch with the label «inner» (error 292).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheTreeEditorLeavesEachLabelOnOneBranchTest {

    private ProbNet net;
    private Variable d;
    private JFrame frame;

    @BeforeEach void setUp() {
        d = new Variable("D", "d0", "d1");
        Variable a = new Variable("A", "a0", "a1", "a2");
        Variable b = new Variable("B", "b0", "b1");
        Variable c = new Variable("C", "c0", "c1");
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        for (Variable variable : List.of(d, a, b, c)) {
            net.addNode(variable, NodeType.CHANCE);
        }
        for (Variable parent : List.of(a, b, c)) {
            net.addLink(parent, d, true);
        }
        List<Variable> variables = List.of(d, a, b, c);
        TreeADDBranch c0 = new TreeADDBranch(states(c, 0), c, table(0.9, 0.1), variables);
        c0.setLabel("inner");
        TreeADDBranch c1 = new TreeADDBranch(states(c, 1), c, table(0.2, 0.8), variables);
        TreeADDBranch b0 = new TreeADDBranch(states(b, 0), b, new TreeADDPotential(
                variables, c, PotentialRole.CONDITIONAL_PROBABILITY, List.of(c0, c1)), variables);
        b0.setLabel("outer");
        TreeADDBranch b1 = new TreeADDBranch(states(b, 1), b, table(0.3, 0.7), variables);
        TreeADDBranch a0 = new TreeADDBranch(states(a, 0), a, new TreeADDPotential(
                variables, b, PotentialRole.CONDITIONAL_PROBABILITY, List.of(b0, b1)), variables);
        TreeADDBranch a1 = new TreeADDBranch(states(a, 1), a, "outer", variables);
        TreeADDBranch a2 = new TreeADDBranch(states(a, 2), a, "inner", variables);
        net.getNode(d).setPotential(new TreeADDPotential(variables, a, PotentialRole.CONDITIONAL_PROBABILITY,
                                                         List.of(a0, a1, a2)));
    }

    @AfterEach void closeTheEditor() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    private TablePotential table(double... values) {
        return new TablePotential(List.of(d), PotentialRole.CONDITIONAL_PROBABILITY, values);
    }

    private static List<State> states(Variable variable, int index) {
        return List.of(variable.getStates()[index]);
    }

    /** @return how many branches of the tree of D, at any depth, have {@code label} */
    private int branchesWithTheLabel(String label) {
        int count = 0;
        Deque<TreeADDPotential> subtrees = new ArrayDeque<>();
        subtrees.push((TreeADDPotential) net.getNode(d).getPotentials().getFirst());
        while (!subtrees.isEmpty()) {
            for (TreeADDBranch branch : subtrees.pop().getBranches()) {
                if (label.equals(branch.getLabel())) {
                    count++;
                }
                Potential potential = branch.getPotential();
                if (!branch.isReference() && potential instanceof TreeADDPotential subtree) {
                    subtrees.push(subtree);
                }
            }
        }
        return count;
    }

    /** @return the path, in the editor, to «b0» */
    private static TreePath b0(TreeADDPotential root) {
        TreeADDBranch a0 = root.getBranches().getFirst();
        TreeADDPotential treeOfB = (TreeADDPotential) a0.getPotential();
        return new TreePath(new Object[] { root, a0, treeOfB, treeOfB.getBranches().getFirst() });
    }

    /** @return the path, in the editor, to «a1» */
    private static TreePath a1(TreeADDPotential root) {
        return new TreePath(new Object[] { root, root.getBranches().get(1) });
    }

    /**
     * Opens the editor of the tree of D, right-clicks the branch at the end of {@code path}, chooses {@code item} in
     * its menu and keeps the edited tree as the relation of D.
     */
    private void chooseInTheEditor(Function<TreeADDPotential, TreePath> path,
                                   Function<TreeADDEditorPanel, JMenuItem> item) throws Exception {
        Node nodeD = net.getNode(d);
        SwingUtilities.invokeAndWait(() -> {
            TreeADDEditorPanel editor = new TreeADDEditorPanel(new TreeADDCellRenderer(net), nodeD);
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
            nodeD.setPotential(editor.getTreePotential());
        });
    }

    @Test
    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The editor needs a screen")
    void removingTheOuterLabelLeavesOneBranchWithTheInnerLabel() throws Exception {
        chooseInTheEditor(TheTreeEditorLeavesEachLabelOnOneBranchTest::b0, editor -> editor.removeLabel);

        assertEquals(1, branchesWithTheLabel("inner"));
    }

    @Test
    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The editor needs a screen")
    void removingAReferenceLeavesOneBranchWithTheInnerLabel() throws Exception {
        chooseInTheEditor(TheTreeEditorLeavesEachLabelOnOneBranchTest::a1, editor -> editor.removeReference);

        assertEquals(1, branchesWithTheLabel("inner"));
    }
}
