/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A node of a temporal model keeps the base name it shares with its twins in other cycles, and a
 * name that another variable of the same cycle has is still refused.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ATwinInAnotherCycleIsNotARepeatedNameTest {

    private ProbNet net;

    /** "A [0]", its twin "A [1]", and "B [0]". */
    @BeforeEach void aTemporalModel() {
        net = new ProbNet(MIDType.getUniqueInstance());
        net.addNode(new Variable("A [0]", "a0", "a1"), NodeType.CHANCE);
        net.addNode(new Variable("A [1]", "a0", "a1"), NodeType.CHANCE);
        net.addNode(new Variable("B [0]", "b0", "b1"), NodeType.CHANCE);
    }

    @Test void theBaseNameItHasIsAccepted() {
        assertDoesNotThrow(() -> new NodeBaseNameEdit(net.getNode("A [0]"), "A").tryConstraintsWillBeMet());
    }

    @Test void theNameOfAnotherVariableOfTheCycleIsRefused() {
        assertThrows(ConstraintViolatedException.class,
                     () -> new NodeBaseNameEdit(net.getNode("A [0]"), "B").tryConstraintsWillBeMet());
    }
}
