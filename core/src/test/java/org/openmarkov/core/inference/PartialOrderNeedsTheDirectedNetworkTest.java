/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.inference;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MarkovNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The order in which variables may be eliminated can only be worked out from the directed network.
 * <p>
 * This is worth a test of its own because of where it bites. Elimination runs on the Markov network
 * built from the projected potentials, and it is tempting - it was about to be done - to compute the
 * order there too, so that a variable introduced by projection could be eliminated. But a Markov
 * network has no directed links: every one of them is a sibling. The partial order is built from the
 * sequence of decisions and from each decision's parents, and both of those are readings of the
 * arrows.
 * <p>
 * What happens without them is not that the order is lost, which would at least be visible. It is
 * REVERSED. Variables are eliminated from the last block to the first, so the directed network puts
 * the decision last and it goes first, before what is observed ahead of it; the undirected network of
 * the same two variables puts it first, so the observation is summed out before the decision is
 * maximized over. That is not the maximum expected utility of the diagram: it is the value of a policy
 * that ignores what it was supposed to have seen.
 * <p>
 * For a Bayesian network it costs nothing - there are no decisions and any order will do.
 *
 * @author Manuel Arias
 */
public class PartialOrderNeedsTheDirectedNetworkTest {

    /** An influence diagram: a chance node observed before a decision, and the decision. */
    private static ProbNet influenceDiagram() {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable observed = new Variable("Observed", 2);
        Variable decision = new Variable("Decision", 2);
        net.addNode(observed, NodeType.CHANCE);
        net.addNode(decision, NodeType.DECISION);
        net.addLink(observed, decision, true);
        return net;
    }

    @Test public void onTheDirectedNetworkTheDecisionIsOrderedAgainstWhatIsObservedBeforeIt() {
        List<List<Variable>> order = BasicOperations.calculatePartialOrder(influenceDiagram());

        assertTrue(order.size() >= 2,
                   "what is observed before the decision and the decision itself go in different blocks, but the "
                           + "order was " + order);
        List<Variable> lastBlock = order.get(order.size() - 1);
        assertEquals(1, lastBlock.size(), "the decision is alone in its block: " + order);
        assertEquals("Decision", lastBlock.getFirst().getName());
    }

    /**
     * And on an undirected network of the same two variables the order comes out reversed. This is the
     * check that says the order must not be computed where the elimination runs.
     */
    @Test public void onAnUndirectedNetworkTheOrderOfTheDecisionIsReversed() {
        ProbNet undirected = new ProbNet(MarkovNetworkType.getUniqueInstance());
        undirected.addConstraint(new OnlyUndirectedLinks());
        Variable observed = new Variable("Observed", 2);
        Variable decision = new Variable("Decision", 2);
        undirected.addNode(observed, NodeType.CHANCE);
        undirected.addNode(decision, NodeType.DECISION);
        undirected.addLink(observed, decision, false);

        List<List<Variable>> order = BasicOperations.calculatePartialOrder(undirected);

        // Eliminated from the last block to the first, so this says the decision goes LAST.
        assertEquals("Observed", order.get(order.size() - 1).getFirst().getName(),
                     "the undirected network eliminates the observation first: " + order);
        assertEquals("Decision", order.getFirst().getFirst().getName(),
                     "and the decision last, which is the reverse of the diagram's own order");

        List<List<Variable>> fromTheDiagram = BasicOperations.calculatePartialOrder(influenceDiagram());
        assertEquals("Decision", fromTheDiagram.get(fromTheDiagram.size() - 1).getFirst().getName(),
                     "whereas the diagram eliminates the decision first, as an influence diagram must");
    }
}
