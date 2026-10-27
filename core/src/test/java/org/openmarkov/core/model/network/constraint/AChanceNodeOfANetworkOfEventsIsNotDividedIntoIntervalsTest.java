/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.VariableTypeEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.model.network.type.NetworkType;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A network of events does not admit chance nodes divided into intervals: the simulation takes the value of a chance
 * node as a state or as a number, and nothing says which number stands for an interval. Such a node used to stop the
 * simulation with an unexpected error that said it «must be chance» (error 106 of the pending list); now the type
 * cannot be chosen.
 *
 * @author Manuel Arias
 */
class AChanceNodeOfANetworkOfEventsIsNotDividedIntoIntervalsTest {

    private static Node ageIn(NetworkType networkType) {
        ProbNet probNet = new ProbNet(networkType);
        return probNet.addNode(new Variable("Age", "young", "old"), NodeType.CHANCE);
    }

    @Test void theChangeToIntervalsIsRefusedNamingTheNode() {
        VariableTypeEdit edit = new VariableTypeEdit(ageIn(DESNetworkType.getUniqueInstance()), VariableType.DISCRETIZED,
                                                     true);

        ConstraintViolatedException refusal = assertThrows(ConstraintViolatedException.class,
                                                           edit::tryConstraintsWillBeMet);
        assertInstanceOf(ConstraintViolatedException.DiscretizedChanceNode.class, refusal);
        assertTrue(refusal.toString().contains("Age cannot be divided into intervals: in a network of events"),
                   refusal.toString());
    }

    @Test void numericOrFiniteStatesAreAdmitted() {
        Node age = ageIn(DESNetworkType.getUniqueInstance());

        assertDoesNotThrow(new VariableTypeEdit(age, VariableType.NUMERIC, true)::tryConstraintsWillBeMet);
        assertDoesNotThrow(() -> age.getProbNet().checkConstraints());
    }

    @Test void otherNetworksStillAdmitIntervals() {
        Node age = ageIn(BayesianNetworkType.getUniqueInstance());

        assertDoesNotThrow(new VariableTypeEdit(age, VariableType.DISCRETIZED, true)::tryConstraintsWillBeMet);
    }

    @Test void theWholeNetworkIsCheckedToo() throws Exception {
        Node age = ageIn(DESNetworkType.getUniqueInstance());
        age.getVariable().setVariableType(VariableType.DISCRETIZED);

        assertInstanceOf(ConstraintViolatedException.DiscretizedChanceNode.class,
                         assertThrows(ConstraintViolatedException.class, age.getProbNet()::checkConstraints));
    }
}
