/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.treeadd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The tree of D asks for Age, numeric from 0 to 100, and its branch from 50 to 100 asks for B. «Add subtree» with Age
 * on the branch «b0» asks for Age again, in the interval of the branch above, with its ends as they are. It passed to
 * the constructor of the interval whether the lower limit belongs to the left, the opposite of whether it is closed,
 * which the constructor kept the other way round; the constructor is now right (error 314).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The editor needs a screen")
class ASubtreeOnANumericVariableAskedAboveKeepsTheIntervalOfItsBranchTest {

    private ProbNet net;
    private Variable d;
    private Node nodeD;
    private JFrame frame;

    @AfterEach void closeTheEditor() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    private TablePotential table() {
        return new TablePotential(List.of(d), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.5, 0.5 });
    }

    /** The tree of D on Age, [0, 50) and then from 50 to 100, with 50 inside when {@code fiftyInside}. */
    private void aTreeOnAge(boolean fiftyInside) {
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
        TreeADDBranch b0 = new TreeADDBranch(List.of(b.getStates()[0]), b, table(), variables);
        TreeADDBranch b1 = new TreeADDBranch(List.of(b.getStates()[1]), b, table(), variables);
        TreeADDBranch lower = new TreeADDBranch(new Threshold(0, false), new Threshold(50, false), age, table(), variables);
        TreeADDBranch upper = new TreeADDBranch(new Threshold(50, !fiftyInside), new Threshold(100, true), age,
                                                new TreeADDPotential(variables, b, PotentialRole.CONDITIONAL_PROBABILITY,
                                                                     List.of(b0, b1)), variables);
        nodeD.setPotential(new TreeADDPotential(variables, age, PotentialRole.CONDITIONAL_PROBABILITY,
                                                List.of(lower, upper)));
    }

    /** Opens the editor of the tree of D, chooses «Add subtree» with Age on «b0» and keeps the tree. */
    private void addASubtreeOnAgeToB0() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TreeADDEditorPanel editor = new TreeADDEditorPanel(new TreeADDCellRenderer(net), nodeD);
            frame = new JFrame();
            frame.add(editor);
            frame.setSize(600, 400);
            frame.setVisible(true);
            TreeADDPotential root = editor.getTreePotential();
            TreeADDBranch upper = root.getBranches().get(1);
            TreeADDPotential treeOfB = (TreeADDPotential) upper.getPotential();
            Rectangle bounds = editor.jTree.getPathBounds(new TreePath(new Object[] { root, upper, treeOfB,
                    treeOfB.getBranches().getFirst() }));
            editor.jTree.dispatchEvent(new MouseEvent(editor.jTree, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                                                      0, (int) bounds.getCenterX(), (int) bounds.getCenterY(), 1, true,
                                                      MouseEvent.BUTTON3));
            for (Component item : editor.addSubtree.getMenuComponents()) {
                if (item instanceof JMenuItem menuItem && menuItem.getText().equals("Age")) {
                    menuItem.doClick();
                }
            }
            editor.contextualMenu.setVisible(false);
            nodeD.setPotential(editor.getTreePotential());
        });
    }

    /** @return the interval of the only branch of the subtree under «b0», written as [a, b) */
    private String intervalOfTheSubtree() {
        TreeADDPotential root = (TreeADDPotential) nodeD.getPotentials().getFirst();
        TreeADDPotential treeOfB = (TreeADDPotential) root.getBranches().get(1).getPotential();
        TreeADDPotential subtree = (TreeADDPotential) treeOfB.getBranches().getFirst().getPotential();
        assertEquals(1, subtree.getBranches().size());
        TreeADDBranch branch = subtree.getBranches().getFirst();
        return (branch.getLowerBound().belongsToLeft() ? "(" : "[") + branch.getLowerBound().getLimit() + ", "
               + branch.getUpperBound().getLimit() + (branch.getUpperBound().belongsToLeft() ? "]" : ")");
    }

    @Test void underABranchClosedBelowTheSubtreeIsClosedBelow() throws Exception {
        aTreeOnAge(true);

        addASubtreeOnAgeToB0();

        assertEquals("[50.0, 100.0]", intervalOfTheSubtree());
    }

    @Test void underABranchOpenBelowTheSubtreeIsOpenBelow() throws Exception {
        aTreeOnAge(false);

        addASubtreeOnAgeToB0();

        assertEquals("(50.0, 100.0]", intervalOfTheSubtree());
    }
}
