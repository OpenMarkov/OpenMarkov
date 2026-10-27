/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Adding an event node, as pasting one does, to a network that does not admit them is refused naming the node. The
 * message named the node to be added, which did not exist yet, and said «>>> eventNode is null <<<» (error 178 of
 * the pending list).
 *
 * @author Manuel Arias
 */
class AnEventWhereNoneIsAdmittedIsNamedTest {

    @Test void theRefusalNamesTheEvent() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable onset = new Variable("Onset", "no", "yes");
        onset.setVariableType(VariableType.EVENT);

        ConstraintViolatedException refusal = assertThrows(ConstraintViolatedException.class,
                new AddNodeEdit(probNet, onset, NodeType.EVENT, null)::tryConstraintsWillBeMet);

        String message = refusal.toString();
        assertTrue(message.contains("Onset is an event node, which this network does not admit"), message);
        assertFalse(message.contains(">>>"), message);
    }
}
