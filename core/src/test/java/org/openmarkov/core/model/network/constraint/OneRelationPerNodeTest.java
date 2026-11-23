/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MarkovNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A node of a network with directed links has one relation. A second one, which only a file can bring, breaks a
 * constraint of the network (error 400 of the pending list).
 *
 * @author Manuel Arias
 */
class OneRelationPerNodeTest {

    /** A network with the node X, which has as many tables as asked. */
    private static ProbNet withTablesInX(ProbNet net, int tables) {
        Variable x = new Variable("X", "no", "yes");
        Node node = net.addNode(x, NodeType.CHANCE);
        for (int i = 0; i < tables; i++) {
            node.addPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY));
        }
        return net;
    }

    @Test void aNodeWithOneRelationMeetsIt() {
        ProbNet net = withTablesInX(new ProbNet(BayesianNetworkType.getUniqueInstance()), 1);

        assertTrue(net.getConstraintViolations().isEmpty(), net.getConstraintViolations().toString());
    }

    @Test void aNodeWithTwoRelationsBreaksItAndTheMessageNamesTheNode() {
        ProbNet net = withTablesInX(new ProbNet(InfluenceDiagramType.getUniqueInstance()), 2);

        List<ConstraintViolatedException> violations = net.getConstraintViolations();

        assertEquals(1, violations.size(), violations.toString());
        ConstraintViolatedException.NodeHasMoreThanOneRelation violation =
                assertInstanceOf(ConstraintViolatedException.NodeHasMoreThanOneRelation.class, violations.getFirst());
        assertEquals("X", violation.node.getName());
        assertEquals(2, violation.numRelations);
        assertTrue(violation.toString().contains("X has 2 probability or utility relations instead of one."),
                   violation.toString());
    }

    @Test void aMarkovNetworkMayHaveSeveralPotentialsInANode() {
        ProbNet net = withTablesInX(new ProbNet(MarkovNetworkType.getUniqueInstance()), 2);

        assertFalse(net.hasConstraintOfClass(OneRelationPerNode.class));
        assertTrue(net.getConstraintViolations().isEmpty(), net.getConstraintViolations().toString());
    }
}
