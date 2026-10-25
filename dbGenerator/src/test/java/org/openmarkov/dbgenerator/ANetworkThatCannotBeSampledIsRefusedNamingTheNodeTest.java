/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.dbgenerator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The generator refuses a network with a decision, with a chance node without a relation, or with a relation it
 * cannot sample, and the message names the node; a Bayesian network is still sampled.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANetworkThatCannotBeSampledIsRefusedNamingTheNodeTest {

    private final Variable decision = new Variable("D", "no", "yes");
    private final Variable chance = new Variable("X", "absent", "present");

    @Test void aDecisionIsRefused() {
        ProbNet probNet = new ProbNet();
        probNet.addNode(decision, NodeType.DECISION);
        probNet.addNode(chance, NodeType.CHANCE);
        probNet.addLink(decision, chance, true);
        probNet.addPotential(new TablePotential(List.of(chance, decision), PotentialRole.CONDITIONAL_PROBABILITY,
                                                new double[]{0.7, 0.3, 0.2, 0.8}));

        assertRefused(probNet, "D");
    }

    @Test void aChanceNodeWithoutARelationIsRefused() {
        ProbNet probNet = new ProbNet();
        probNet.addNode(chance, NodeType.CHANCE);

        assertRefused(probNet, "X");
    }

    @Test void aRelationThatCannotBeSampledIsRefused() {
        ProbNet probNet = new ProbNet();
        probNet.addNode(chance, NodeType.CHANCE);
        probNet.addPotential(new ExactDistrPotential(List.of(chance)));

        assertRefused(probNet, "X");
    }

    @Test void aBayesianNetworkIsSampled() throws Exception {
        ProbNet probNet = new ProbNet();
        probNet.addNode(chance, NodeType.CHANCE);
        probNet.addPotential(new TablePotential(List.of(chance), PotentialRole.CONDITIONAL_PROBABILITY,
                                                new double[]{0.4, 0.6}));

        assertEquals(10, DBGenerator.generate(probNet, 10, 12345L).getCases().length);
    }

    private static void assertRefused(ProbNet probNet, String nodeName) {
        var refused = assertThrows(NodeCannotBeSampled.class, () -> DBGenerator.generate(probNet, 10, 12345L));

        assertEquals("Cases cannot be generated: node " + nodeName
                     + " is not a chance node with a relation that can be sampled.", refused.getExceptionMessage());
    }
}
