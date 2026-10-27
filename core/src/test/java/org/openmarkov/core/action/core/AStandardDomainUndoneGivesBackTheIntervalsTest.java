/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Choosing a standard domain for a variable divided into intervals gives it default ones. Cancelling the window of
 * the node, or undoing, gave back the names of the states and the tables, but not the intervals: «normal», from 0 to
 * 37.5, and «fever», from 37.5 to 42, came back from minus infinity to 0 and from 0 to infinity, so that a temperature
 * of 36 fell in «fever» (error 24 of the pending list).
 *
 * @author Manuel Arias
 */
class AStandardDomainUndoneGivesBackTheIntervalsTest {

    private static final double[] LIMITS = { 0, 37.5, 42 };

    private ProbNet probNet;
    private Node temperature;

    @BeforeEach void aTemperatureDividedIntoIntervals() {
        probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable variable = new Variable("Temperature", new State[]{ new State("normal"), new State("fever") },
                                         new PartitionedInterval(LIMITS, new boolean[]{ false, true, true }), 0.1);
        temperature = probNet.addNode(variable, NodeType.CHANCE);
        probNet.getPNESupport().setWithUndo(true);
    }

    private void assertTheIntervalsAreBack() {
        assertEquals(2, temperature.getVariable().getNumStates());
        assertArrayEquals(LIMITS, temperature.getVariable().getPartitionedInterval().getLimits());
        assertEquals(1, temperature.getVariable().getStateIndex(38.0));
        assertEquals(0, temperature.getVariable().getStateIndex(36.0));
    }

    @Test void cancellingTheWindowGivesThemBack() throws Exception {
        probNet.getPNESupport().openNewSubEditHistory();
        new NodeReplaceStatesEdit(temperature, new State[]{ new State("low"), new State("medium"), new State("high") })
                .executeEdit();

        probNet.getPNESupport().cancelLastSubEditHistory();

        assertTheIntervalsAreBack();
    }

    @Test void undoGivesThemBackWithTheSameNumberOfStatesToo() throws Exception {
        new NodeReplaceStatesEdit(temperature, new State[]{ new State("no"), new State("yes") }).executeEdit();

        probNet.getPNESupport().undo();

        assertTheIntervalsAreBack();
    }
}
