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
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * In a network of events, turning into an event a numeric node without relation, such as a numeric decision whose
 * policy has been removed, ended in an unexpected error and left the node half changed, an event with one state, out
 * of the history (error 28 of the pending list). Now it becomes an event with its relation by default, and undoing it
 * gives the decision back.
 *
 * @author Manuel Arias
 */
class ANumericNodeWithoutRelationBecomesAnEventTest {

    @Test void itBecomesAnEventAndCanBeUndone() throws Exception {
        ProbNet probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        Node dose = probNet.addNode(new Variable("Dose"), NodeType.DECISION);
        dose.setPotentials(new ArrayList<>());
        probNet.getPNESupport().setWithUndo(true);

        new ChangeNodeTypeEdit(dose, NodeType.EVENT).executeEdit();

        assertEquals(NodeType.EVENT, dose.getNodeType());
        assertEquals(VariableType.EVENT, dose.getVariable().getVariableType());
        assertFalse(dose.getPotentials().isEmpty(), "The event has no relation");

        probNet.getPNESupport().undo();

        assertEquals(NodeType.DECISION, dose.getNodeType());
        assertEquals(VariableType.NUMERIC, dose.getVariable().getVariableType());
    }
}
