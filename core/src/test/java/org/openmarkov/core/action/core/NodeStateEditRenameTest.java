/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the RENAME action of {@link NodeStateEdit}: doing it and undoing it must keep the
 * additional properties of the state with the state, and a name another state already has must be
 * refused.
 *
 * @author Manuel Arias
 */
class NodeStateEditRenameTest {

    private ProbNet net;
    private Variable variable;
    private Node node;

    @BeforeEach
    void setUp() {
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        variable = new Variable("X", new State[]{new State("s0"), new State("s1"), new State("s2")});
        node = net.addNode(variable, NodeType.CHANCE);
    }

    private String[] stateNames() {
        return Arrays.stream(variable.getStates()).map(State::getName).toArray(String[]::new);
    }

    @Test
    void renameThenUndoBringsTheAdditionalPropertiesBack() throws DoEditException {
        variable.setStateAdditionalProperty("s2", "color", "red");
        // stateIndex is inverted: 0 → the last state, "s2".
        NodeStateEdit rename = new NodeStateEdit(node, StateAction.RENAME, 0, "renamed");
        rename.executeEdit();
        assertEquals("red", variable.getStateAdditionalProperty("renamed", "color"));

        rename.undo();
        assertArrayEquals(new String[]{"s0", "s1", "s2"}, stateNames());
        assertEquals("red", variable.getStateAdditionalProperty("s2", "color"));
        assertNull(variable.getStateAdditionalProperty("renamed", "color"));
    }

    @Test
    void renamingToASiblingNameThroughTheEditIsRefused() {
        NodeStateEdit rename = new NodeStateEdit(node, StateAction.RENAME, 0, "s0");
        assertThrows(InvalidArgumentException.class, rename::executeEdit);
        assertArrayEquals(new String[]{"s0", "s1", "s2"}, stateNames());
    }
}
