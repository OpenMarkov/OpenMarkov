/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests for renaming a state from the states table of the interface: a name another state already
 * has must leave the old name in the cell and warn, instead of killing the dialog or leaving two
 * states with the same name.
 *
 * @author Manuel Arias
 */
class DiscretizeTablePanelStateRenameTest {

    /** Column names as the node-properties dialog builds them; only the second column matters. */
    private static final String[] COLUMNS = {"Id", "Name", "(", "Low", ",", "High", ")"};

    private Variable variable;
    private WatchedPanel panel;

    /** The panel under test, with the warning dialog silenced and counted. */
    private static final class WatchedPanel extends DiscretizeTablePanel {
        private int warnings;

        private WatchedPanel(Node node) {
            super(COLUMNS, node);
        }

        @Override protected void warnStateRenameRejected() {
            warnings++;
        }
    }

    @BeforeEach
    void setUp() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        variable = new Variable("X", new State[]{new State("a"), new State("b")});
        Node node = net.addNode(variable, NodeType.CHANCE);
        panel = new WatchedPanel(node);
        // The table shows the states in reverse order: first row, last state.
        panel.setData(new Object[][]{{"b"}, {"a"}});
    }

    private String[] stateNames() {
        return Arrays.stream(variable.getStates()).map(State::getName).toArray(String[]::new);
    }

    @Test
    void renamingToAFreshNameGoesThrough() {
        panel.getValuesTable().getModel().setValueAt("c", 0, 1);
        assertArrayEquals(new String[]{"a", "c"}, stateNames());
        assertEquals(0, panel.warnings);
    }

    @Test
    void renamingToASiblingNameKeepsTheOldNameAndWarnsInsteadOfDying() {
        assertDoesNotThrow(() -> panel.getValuesTable().getModel().setValueAt("a", 0, 1));
        assertArrayEquals(new String[]{"a", "b"}, stateNames(),
                          "the variable must not end up with two states named the same");
        assertEquals("b", panel.getValuesTable().getModel().getValueAt(0, 1),
                     "the cell must show the name the state still has");
        assertEquals(1, panel.warnings, "the user must be told why their name was not kept");
    }

    @Test
    void leavingTheNameAsItWasNeitherWarnsNorEdits() {
        panel.getValuesTable().getModel().setValueAt("b", 0, 1);
        assertArrayEquals(new String[]{"a", "b"}, stateNames());
        assertEquals(0, panel.warnings);
    }
}
