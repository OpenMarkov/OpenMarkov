/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.CycleLength.Unit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.MIDType;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The cycle length written in the temporal options of the network properties reaches the network (error 73).
 *
 * @author Manuel Arias
 */
class NetworkTemporalOptionsPanelTest {

    private static Component named(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
        }
        throw new AssertionError("No component named " + name);
    }

    private static boolean write(NetworkTemporalOptionsPanel panel, String value, Unit unit) {
        ((JTextField) named(panel, "cycleLengthValue")).setText(value);
        ((JComboBox<?>) named(panel, "cycleLengthUnit")).setSelectedIndex(unit.ordinal());
        return panel.applyCycleLength();
    }

    private static ProbNet oneYear() {
        ProbNet net = new ProbNet(MIDType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        net.setCycleLength(new CycleLength(Unit.YEAR, 1));
        return net;
    }

    @Test
    void theLengthWrittenReachesTheNetworkAndCanBeUndone() {
        ProbNet net = oneYear();

        assertTrue(write(new NetworkTemporalOptionsPanel(net), "6", Unit.MONTH));
        assertEquals(Unit.MONTH, net.getCycleLength().getUnit());
        assertEquals(6, net.getCycleLength().getValue());

        net.getPNESupport().undo();
        assertEquals(Unit.YEAR, net.getCycleLength().getUnit());
        assertEquals(1, net.getCycleLength().getValue());
    }

    @Test
    void aLengthThatIsNotAPositiveNumberIsRefused() {
        ProbNet net = oneYear();
        NetworkTemporalOptionsPanel panel = new NetworkTemporalOptionsPanel(net);

        for (String value : new String[]{"", "six", "0", "-2"}) {
            assertFalse(write(panel, value, Unit.MONTH), value);
            assertEquals(Unit.YEAR, net.getCycleLength().getUnit());
        }
    }

    @Test
    void openingTheTabDoesNotGiveTheNetworkALength() {
        ProbNet net = new ProbNet(MIDType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        net.setCycleLength(null);

        new NetworkTemporalOptionsPanel(net);

        assertNull(net.getCycleLength());
    }
}
