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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.treeadd.Threshold;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.dialog.common.OkCancelDialog;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Window;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * «Split interval» and «Change interval», in the editor of a tree, warn and stay open when a value is not a number,
 * instead of closing and giving an unexpected error (error 161).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialogs need a screen")
class TheIntervalDialogsOfTheTreeOnlyAcceptNumbersTest {

    private OkCancelDialog dialog;

    @AfterEach void closeTheDialog() throws Exception {
        SwingUtilities.invokeAndWait(dialog::dispose);
    }

    /** Presses OK in the dialog: returns the message it shows, once closed, or null if the dialog closes. */
    private String pressOk() throws Exception {
        SwingUtilities.invokeLater(() -> dialog.getOKButton().doClick());
        long end = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < end) {
            AtomicReference<String> message = new AtomicReference<>();
            AtomicBoolean closed = new AtomicBoolean();
            SwingUtilities.invokeAndWait(() -> {
                closed.set(!dialog.isDisplayable());
                for (Window window : Window.getWindows()) {
                    if (window.isShowing() && window instanceof JDialog shown
                        && shown.getContentPane().getComponentCount() > 0
                        && shown.getContentPane().getComponent(0) instanceof JOptionPane pane) {
                        message.set(String.valueOf(pane.getMessage()));
                        shown.dispose();
                    }
                }
            });
            if (message.get() != null) {
                return message.get();
            }
            if (closed.get()) {
                return null;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("OK neither closed the dialog nor showed a message");
    }

    private SplitIntervalDialog splitIntervalDialogWith(String threshold) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            SplitIntervalDialog split = new SplitIntervalDialog(null);
            split.getJPanelSplitInterval().getLimit().setText(threshold);
            dialog = split;
        });
        return (SplitIntervalDialog) dialog;
    }

    /** The dialog of the branch from 0 to 2 of a numeric variable whose domain goes from 0 to {@code maximum}. */
    private ChangeIntervalDialog changeIntervalDialog(double maximum, double upperBound) throws Exception {
        Variable time = new Variable("Time", true, 0, maximum, false, 0.1);
        TreeADDBranch branch = new TreeADDBranch(new Threshold(0, false), new Threshold(upperBound, false), time, (Potential) null,
                                                 List.of(time));
        SwingUtilities.invokeAndWait(() -> dialog = new ChangeIntervalDialog(null, branch));
        return (ChangeIntervalDialog) dialog;
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "1,5", "NaN"})
    void splitIntervalWarnsAndStaysOpenWhenTheThresholdIsNotANumber(String threshold) throws Exception {
        splitIntervalDialogWith(threshold);

        assertEquals("The threshold value must be a number, such as 1.5.", pressOk());
        assertTrue(dialog.isDisplayable());
    }

    @Test void splitIntervalClosesWithANumber() throws Exception {
        splitIntervalDialogWith("1.5");

        assertNull(pressOk());
        assertEquals(OkCancelDialog.ChosenOption.Ok, dialog.getSelectedOption());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "1,5", "NaN"})
    void changeIntervalWarnsAndStaysOpenWhenALimitIsNotANumber(String limit) throws Exception {
        for (boolean lower : new boolean[]{true, false}) {
            ChangeIntervalDialog change = changeIntervalDialog(10, 2);
            SwingUtilities.invokeAndWait(() -> (lower ? change.getChangeIntervalPanel().getMin()
                                                      : change.getChangeIntervalPanel().getMax()).setText(limit));

            assertEquals("The limits of the interval must be numbers, such as 1.5.", pressOk());
            assertTrue(dialog.isDisplayable());
            SwingUtilities.invokeAndWait(dialog::dispose);
        }
    }

    @Test void changeIntervalClosesWithNumbers() throws Exception {
        ChangeIntervalDialog change = changeIntervalDialog(10, 2);
        SwingUtilities.invokeAndWait(() -> change.getChangeIntervalPanel().getMax().setText("1.5"));

        assertNull(pressOk());
        assertEquals(OkCancelDialog.ChosenOption.Ok, dialog.getSelectedOption());
    }

    @Test void changeIntervalClosesWhenALimitIsTheInfiniteEndOfTheDomain() throws Exception {
        ChangeIntervalDialog change = changeIntervalDialog(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertEquals("Infinity", change.getChangeIntervalPanel().getMax().getText());
        assertFalse(change.getChangeIntervalPanel().getMax().isEditable());

        assertNull(pressOk());
    }
}
