/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The last block of the partial order holds the variables nobody ever gets to observe. Its
 * order decides which candidate wins a tie in the elimination rule, so it must be the same
 * every time the program runs. Each run builds the network again, so its variables are new
 * objects with new identity hash codes.
 *
 * @author Manuel Arias
 */
class ThePartialOrderIsTheSameEveryRunTest {

    private static final int RUNS = 30;

    /** A decision that observes one variable, and six more that nobody ever observes. */
    private static ProbNet diagramWithUnobservedVariables() {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable observed = new Variable("Observed", 2);
        Variable decision = new Variable("Decision", 2);
        net.addNode(observed, NodeType.CHANCE);
        net.addNode(decision, NodeType.DECISION);
        net.addLink(observed, decision, true);
        for (int i = 0; i < 6; i++) {
            net.addNode(new Variable("Hidden" + i, 2), NodeType.CHANCE);
        }
        return net;
    }

    @Tag(TestSpeed.FAST)
    @Test void theBlockOfWhatIsNeverObservedKeepsItsOrder() {
        List<String> first = null;
        for (int run = 0; run < RUNS; run++) {
            List<List<Variable>> order =
                    BasicOperations.calculatePartialOrder(diagramWithUnobservedVariables());
            List<String> lastBlock = new ArrayList<>();
            for (Variable variable : order.getLast()) {
                lastBlock.add(variable.getName());
            }
            if (first == null) {
                first = lastBlock;
            } else {
                assertEquals(first, lastBlock, "run " + run);
            }
        }
    }
}
