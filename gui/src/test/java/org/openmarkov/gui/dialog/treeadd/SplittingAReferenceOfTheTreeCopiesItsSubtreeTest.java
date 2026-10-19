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
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * The tree of D asks for Age, numeric from 0 to 100: [0, 50], with the label «outer», asks for B, whose branch «b0» has
 * the label «inner»; (50, 100] references «outer». «Split interval» on (50, 100] at 75 gives each half its own copy of
 * the subtree, so a single branch keeps the label «inner» (error 310).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The editor and its dialog need a screen")
class SplittingAReferenceOfTheTreeCopiesItsSubtreeTest {

    private ProbNet net;
    private Variable d;
    private Node nodeD;
    private JFrame frame;

    @BeforeEach void setUp() {
        d = new Variable("D", "d0", "d1");
        Variable age = new Variable("Age", true, 0, 100, true, 0.01);
        Variable b = new Variable("B", "b0", "b1");
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        nodeD = net.addNode(d, NodeType.CHANCE);
        for (Variable parent : List.of(age, b)) {
            net.addNode(parent, NodeType.CHANCE);
            net.addLink(parent, d, true);
        }
        List<Variable> variables = List.of(d, age, b);
        TreeADDBranch b0 = new TreeADDBranch(List.of(b.getStates()[0]), b, table(0.9, 0.1), variables);
        b0.setLabel("inner");
        TreeADDBranch b1 = new TreeADDBranch(List.of(b.getStates()[1]), b, table(0.2, 0.8), variables);
        TreeADDBranch lower = new TreeADDBranch(new Threshold(0, false), new Threshold(50, true), age,
                                                new TreeADDPotential(variables, b, PotentialRole.CONDITIONAL_PROBABILITY,
                                                                     List.of(b0, b1)), variables);
        lower.setLabel("outer");
        TreeADDBranch upper = new TreeADDBranch(new Threshold(50, true), new Threshold(100, true), age, "outer",
                                                variables);
        nodeD.setPotential(new TreeADDPotential(variables, age, PotentialRole.CONDITIONAL_PROBABILITY,
                                                List.of(lower, upper)));
    }

    @AfterEach void closeTheEditor() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    private TablePotential table(double... values) {
        return new TablePotential(List.of(d), PotentialRole.CONDITIONAL_PROBABILITY, values);
    }

    /** @return how many branches of the tree of D, at any depth, have {@code label} */
    private int branchesWithTheLabel(String label) {
        int count = 0;
        Deque<TreeADDPotential> subtrees = new ArrayDeque<>();
        subtrees.push((TreeADDPotential) nodeD.getPotentials().getFirst());
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

    /** Opens the editor of the tree of D, splits (50, 100] at 75, with 75 in the left half, and keeps the tree. */
    private void splitTheReferenceAt75() throws Exception {
        AtomicReference<TreeADDEditorPanel> editor = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            editor.set(new TreeADDEditorPanel(new TreeADDCellRenderer(net), nodeD));
            frame = new JFrame();
            frame.add(editor.get());
            frame.setSize(600, 400);
            frame.setVisible(true);
            TreeADDPotential root = editor.get().getTreePotential();
            Rectangle bounds = editor.get().jTree.getPathBounds(new TreePath(new Object[] { root,
                    root.getBranches().get(1) }));
            editor.get().jTree.dispatchEvent(new MouseEvent(editor.get().jTree, MouseEvent.MOUSE_PRESSED,
                                                            System.currentTimeMillis(), 0, (int) bounds.getCenterX(),
                                                            (int) bounds.getCenterY(), 1, true, MouseEvent.BUTTON3));
        });
        SwingUtilities.invokeLater(() -> editor.get().splitInterval.doClick());
        SplitIntervalDialog dialog = null;
        for (long end = System.currentTimeMillis() + 10_000; dialog == null && System.currentTimeMillis() < end; ) {
            Thread.sleep(20);
            for (Window window : Window.getWindows()) {
                if (window.isShowing() && window instanceof SplitIntervalDialog shown) {
                    dialog = shown;
                }
            }
        }
        SplitIntervalDialog shown = dialog;
        SwingUtilities.invokeAndWait(() -> {
            shown.getJPanelSplitInterval().getLimit().setText("75");
            shown.getJPanelSplitInterval().belongsToLeft().setSelected(true);
        });
        SwingUtilities.invokeLater(() -> shown.getOKButton().doClick());
        for (long end = System.currentTimeMillis() + 10_000; shown.isShowing() && System.currentTimeMillis() < end; ) {
            Thread.sleep(20);
        }
        SwingUtilities.invokeAndWait(() -> {
            editor.get().contextualMenu.setVisible(false);
            nodeD.setPotential(editor.get().getTreePotential());
        });
    }

    @Test void aSingleBranchKeepsTheInnerLabel() throws Exception {
        splitTheReferenceAt75();

        assertEquals(1, branchesWithTheLabel("inner"));
    }

    @Test void theLeftHalfDoesNotShareTheSubtreeOfTheLabeledBranch() throws Exception {
        splitTheReferenceAt75();

        List<TreeADDBranch> branches = ((TreeADDPotential) nodeD.getPotentials().getFirst()).getBranches();
        assertEquals(3, branches.size());
        assertNotSame(branches.get(0).getPotential(), branches.get(1).getPotential());
    }
}
