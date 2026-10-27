/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.action;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.NetworkType;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A network of events does not admit chance nodes divided into intervals (error 106 of the pending list). A decision
 * divided into intervals that becomes a chance node there takes finite states, as when its type is not admitted at
 * all; elsewhere it keeps its intervals.
 *
 * @author Manuel Arias
 */
class ADecisionThatBecomesAChanceNodeOfANetworkOfEventsLosesItsIntervalsTest {

    private static Node doseIn(NetworkType networkType) {
        Variable dose = new Variable("Dose", new State[]{ new State("low"), new State("high") },
                                     new PartitionedInterval(new double[]{ 0, 10, 20 }, new boolean[]{ false, false, false }),
                                     1);
        return new ProbNet(networkType).addNode(dose, NodeType.DECISION);
    }

    @Test void inANetworkOfEventsItTakesFiniteStates() throws Exception {
        Node dose = doseIn(DESNetworkType.getUniqueInstance());

        new ChangeNodeTypeEdit(dose, NodeType.CHANCE).executeEdit();

        assertEquals(VariableType.FINITE_STATES, dose.getVariable().getVariableType());
    }

    @Test void elsewhereItKeepsItsIntervals() throws Exception {
        Node dose = doseIn(InfluenceDiagramType.getUniqueInstance());

        new ChangeNodeTypeEdit(dose, NodeType.CHANCE).executeEdit();

        assertEquals(VariableType.DISCRETIZED, dose.getVariable().getVariableType());
    }
}
