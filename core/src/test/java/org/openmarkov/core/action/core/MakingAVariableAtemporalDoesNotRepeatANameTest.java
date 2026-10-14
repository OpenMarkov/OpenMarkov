/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.MIDType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Making «Therapy [0]» atemporal names it «Therapy». The check that the name is free used to ask about «Therapy
 * [-2147483648]», which never exists, so a network that already had «Therapy» was left with two variables of that name
 * (error 72).
 *
 * @author Manuel Arias
 */
class MakingAVariableAtemporalDoesNotRepeatANameTest {

    private ProbNet probNet;
    private Node therapy;

    @BeforeEach void aMarkovInfluenceDiagramWithTherapyInCycleZero() {
        probNet = new ProbNet(MIDType.getUniqueInstance());
        Variable variable = new Variable("Therapy", "no", "yes");
        variable.setTimeSlice(0);
        therapy = probNet.addNode(variable, NodeType.CHANCE);
    }

    @Test void itIsRefusedIfAnAtemporalVariableAlreadyHasTheName() {
        probNet.addNode(new Variable("Therapy", "low", "high"), NodeType.CHANCE);

        assertThrows(DoEditException.class, () -> new TimeSliceEdit(therapy, Variable.noTemporalTimeSlice).executeEdit());
        assertEquals(List.of("Therapy [0]", "Therapy"), probNet.getVariablesNames());
    }

    @Test void itIsAcceptedIfTheNameIsFreeAndUndoingGivesTheCycleBack() throws Exception {
        probNet.addNode(new Variable("Age", "young", "old"), NodeType.CHANCE);
        TimeSliceEdit edit = new TimeSliceEdit(therapy, Variable.noTemporalTimeSlice);

        edit.executeEdit();
        assertEquals(List.of("Therapy", "Age"), probNet.getVariablesNames());

        edit.undo();
        assertEquals(List.of("Therapy [0]", "Age"), probNet.getVariablesNames());
    }

    /** The other way round already asked about the right name. */
    @Test void givingACycleThatAnotherVariableHasIsRefused() {
        Variable otherTherapy = new Variable("Therapy", "low", "high");
        otherTherapy.setTimeSlice(1);
        probNet.addNode(otherTherapy, NodeType.CHANCE);

        assertThrows(DoEditException.class, () -> new TimeSliceEdit(therapy, 1).executeEdit());
        assertEquals(List.of("Therapy [0]", "Therapy [1]"), probNet.getVariablesNames());
    }
}
