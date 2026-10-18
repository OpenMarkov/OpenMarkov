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
import org.openmarkov.gui.dialog.common.OkCancelDialog;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The tree of C asks for Age, a numeric variable, with one branch for each interval. «Split interval» and «Change
 * interval» put the limits that are written, and recognise a limit that a branch already has (error 291).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The editor and its dialogs need a screen")
class TheIntervalsOfATreeAreCutWhereTheyAreWrittenTest {

    private ProbNet net;
    private Node nodeC;
    private JFrame frame;

    @AfterEach void closeTheEditor() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    /** The tree of C on Age, whose domain goes from 0 to {@code maximum}, with a branch between each two limits. */
    private void aTreeOnAge(double maximum, boolean rightClosed, double... limits) {
        Variable age = new Variable("Age", true, 0, maximum, rightClosed, 0.01);
        Variable c = new Variable("C", "c0", "c1");
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.addNode(age, NodeType.CHANCE);
        nodeC = net.addNode(c, NodeType.CHANCE);
        net.addLink(age, c, true);
        List<Variable> variables = List.of(c, age);
        List<TreeADDBranch> branches = new ArrayList<>();
        for (int i = 0; i + 1 < limits.length; i++) {
            boolean last = i + 2 == limits.length;
            branches.add(new TreeADDBranch(new Threshold(limits[i], false), new Threshold(limits[i + 1], last && rightClosed),
                                           age, new TablePotential(List.of(c), PotentialRole.CONDITIONAL_PROBABILITY,
                                                                   new double[] { 0.5, 0.5 }), variables));
        }
        nodeC.setPotential(new TreeADDPotential(variables, age, PotentialRole.CONDITIONAL_PROBABILITY, branches));
    }

    /** @return the intervals of the tree of C, written as [a, b) */
    private List<String> intervals() {
        List<String> intervals = new ArrayList<>();
        for (TreeADDBranch branch : ((TreeADDPotential) nodeC.getPotentials().getFirst()).getBranches()) {
            intervals.add((branch.getLowerBound().belongsToLeft() ? "(" : "[") + branch.getLowerBound().getLimit() + ", "
                          + branch.getUpperBound().getLimit() + (branch.getUpperBound().belongsToLeft() ? "]" : ")"));
        }
        return intervals;
    }

    /**
     * Opens the editor of the tree of C, chooses {@code item} in the menu of the branch {@code index}, fills its dialog
     * with {@code fill}, presses OK and keeps the edited tree as the relation of C.
     *
     * @return the exception that the editor threw, if any
     */
    private Throwable chooseInTheMenuOfBranch(int index, Function<TreeADDEditorPanel, JMenuItem> item,
                                              Class<? extends OkCancelDialog> dialogClass,
                                              Consumer<OkCancelDialog> fill) throws Exception {
        AtomicReference<TreeADDEditorPanel> editor = new AtomicReference<>();
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> thrown.set(exception));
        try {
            SwingUtilities.invokeAndWait(() -> {
                editor.set(new TreeADDEditorPanel(new TreeADDCellRenderer(net), nodeC));
                frame = new JFrame();
                frame.add(editor.get());
                frame.setSize(600, 400);
                frame.setVisible(true);
                TreeADDPotential root = editor.get().getTreePotential();
                Rectangle bounds = editor.get().jTree.getPathBounds(new TreePath(new Object[] { root,
                        root.getBranches().get(index) }));
                editor.get().jTree.dispatchEvent(new MouseEvent(editor.get().jTree, MouseEvent.MOUSE_PRESSED,
                                                                System.currentTimeMillis(), 0, (int) bounds.getCenterX(),
                                                                (int) bounds.getCenterY(), 1, true, MouseEvent.BUTTON3));
            });
            SwingUtilities.invokeLater(() -> item.apply(editor.get()).doClick());
            OkCancelDialog dialog = null;
            for (long end = System.currentTimeMillis() + 10_000; dialog == null && System.currentTimeMillis() < end; ) {
                Thread.sleep(20);
                for (Window window : Window.getWindows()) {
                    if (window.isShowing() && dialogClass.isInstance(window)) {
                        dialog = (OkCancelDialog) window;
                    }
                }
            }
            OkCancelDialog shown = dialog;
            SwingUtilities.invokeAndWait(() -> fill.accept(shown));
            SwingUtilities.invokeLater(() -> shown.getOKButton().doClick());
            for (long end = System.currentTimeMillis() + 10_000; shown.isShowing() && System.currentTimeMillis() < end; ) {
                Thread.sleep(20);
            }
            SwingUtilities.invokeAndWait(() -> {
                editor.get().contextualMenu.setVisible(false);
                nodeC.setPotential(editor.get().getTreePotential());
            });
            return thrown.get();
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previous);
        }
    }

    private Throwable split(int index, String threshold, boolean belongsToLeft) throws Exception {
        return chooseInTheMenuOfBranch(index, editor -> editor.splitInterval, SplitIntervalDialog.class, dialog -> {
            SplitIntervalPanel panel = ((SplitIntervalDialog) dialog).getJPanelSplitInterval();
            panel.getLimit().setText(threshold);
            (belongsToLeft ? panel.belongsToLeft() : panel.belongsToRight()).setSelected(true);
        });
    }

    private Throwable change(int index, String min, String max) throws Exception {
        return chooseInTheMenuOfBranch(index, editor -> editor.changeInterval, ChangeIntervalDialog.class, dialog -> {
            ChangeIntervalPanel panel = ((ChangeIntervalDialog) dialog).getChangeIntervalPanel();
            panel.getMin().setText(min);
            panel.getMax().setText(max);
        });
    }

    @Test void splittingByAThresholdPutsThatVeryNumber() throws Exception {
        aTreeOnAge(Double.POSITIVE_INFINITY, false, 0, 0.1, Double.POSITIVE_INFINITY);

        split(1, "0.3", false);

        assertEquals(List.of("[0.0, 0.1)", "[0.1, 0.3)", "[0.3, Infinity)"), intervals());
    }

    @Test void splittingByTheLowerLimitOfTheBranchIsRefused() throws Exception {
        aTreeOnAge(Double.POSITIVE_INFINITY, false, 0, 0.1, Double.POSITIVE_INFINITY);

        split(1, "0.1", false);

        assertEquals(List.of("[0.0, 0.1)", "[0.1, Infinity)"), intervals());
    }

    @Test void changingTheUpperLimitKeepsTheLowerOne() throws Exception {
        aTreeOnAge(Double.POSITIVE_INFINITY, false, 0, 0.1, 1, Double.POSITIVE_INFINITY);

        change(1, "0.1", "2");

        assertEquals(List.of("[0.0, 0.1)", "[0.1, 2.0)", "[2.0, Infinity)"), intervals());
    }

    @Test void theUpperLimitOfTheDomainIsNotTakenForAChangeOfTheDomain() throws Exception {
        aTreeOnAge(0.3, true, 0, 0.1, 0.3);

        Throwable thrown = change(1, "0.2", "0.3");

        assertEquals(null, thrown);
        assertEquals(List.of("[0.0, 0.2)", "[0.2, 0.3]"), intervals());
    }
}
