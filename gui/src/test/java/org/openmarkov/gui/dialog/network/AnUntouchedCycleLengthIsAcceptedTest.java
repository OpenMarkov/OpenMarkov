/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.CycleLength.Unit;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Markov influence diagram whose cycle lasts 0 years, as a file written by hand may bring. The properties of the
 * network accept that length while nobody changes it, and a dialog to read closes with «OK»; it used to refuse the
 * length even untouched, also in the dialog to read, where it cannot be changed (error 254).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnUntouchedCycleLengthIsAcceptedTest {

    private ProbNet net;

    @BeforeEach void aCycleOfNoLength() {
        net = new ProbNet(MIDType.getUniqueInstance());
        net.setName("cycle of no length");
        Variable health = new Variable("Health", "good", "bad");
        health.setTimeSlice(0);
        net.addNode(health, NodeType.CHANCE);
        net.setCycleLength(new CycleLength(Unit.YEAR, 0));
    }

    private static JTextField lengthBox(Container panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JTextField box) {
                return box;
            }
            if (component instanceof Container inner && lengthBox(inner) != null) {
                return lengthBox(inner);
            }
        }
        return null;
    }

    @Test void anUntouchedLengthIsAcceptedAndKept() {
        NetworkTemporalOptionsPanel panel = new NetworkTemporalOptionsPanel(net);

        assertTrue(panel.applyCycleLength());
        assertEquals(0, net.getCycleLength().getValue());
    }

    @Test void aLengthWrittenByTheUserIsChecked() {
        NetworkTemporalOptionsPanel panel = new NetworkTemporalOptionsPanel(net);

        lengthBox(panel).setText("0");
        assertFalse(panel.applyCycleLength());

        lengthBox(panel).setText("2");
        assertTrue(panel.applyCycleLength());
        assertEquals(2, net.getCycleLength().getValue());
    }

    @Test
    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialog needs a screen")
    void aDialogToReadClosesWithOk() throws Exception {
        AtomicReference<Boolean> closes = new AtomicReference<>();
        // A refusal shows a modal message, so «OK» is not waited for
        SwingUtilities.invokeLater(() -> {
            NetworkPropertiesDialog dialog = new NetworkPropertiesDialog(null, net, true);
            closes.set(dialog.doOkClickBeforeHide());
            dialog.dispose();
        });
        for (long end = System.currentTimeMillis() + 10_000; closes.get() == null && System.currentTimeMillis() < end; ) {
            Thread.sleep(20);
            for (Window window : Window.getWindows()) {
                if (window.isShowing() && window instanceof JDialog message
                        && !(window instanceof NetworkPropertiesDialog)) {
                    SwingUtilities.invokeLater(message::dispose);
                }
            }
        }

        assertEquals(Boolean.TRUE, closes.get());
    }
}
