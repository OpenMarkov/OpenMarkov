/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DANOperations#getNextDecisions} answers which decisions could be made
 * first; its size drives the whole expansion (one — branch on it; several —
 * branch on the order; none — nothing left to decide). A decision is discarded
 * when another one reveals everything it reveals: considering it first adds no
 * order worth exploring. What may never happen is discarding the whole tie:
 * with candidate decisions pending, an empty answer tells the caller the model
 * has nothing left to decide, and the expansion evaluates an asymmetric model
 * as if it were symmetric.
 *
 * @author Manuel Arias
 */
class GetNextDecisionsTest {

    private ProbNet probNet;
    private Node decision1;
    private Node decision2;
    private Node observed;

    /** Two parentless decisions; each can reveal the same chance variable. */
    @BeforeEach
    void setUp() {
        probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
        Variable d1 = new Variable("D1", "no", "yes");
        Variable d2 = new Variable("D2", "no", "yes");
        Variable c = new Variable("C", "absent", "present");
        decision1 = probNet.addNode(d1, NodeType.DECISION);
        decision2 = probNet.addNode(d2, NodeType.DECISION);
        observed = probNet.addNode(c, NodeType.CHANCE);
    }

    private void reveal(Node decision, Node revealed) {
        Link<Node> link = probNet.addLink(decision, revealed, true);
        for (org.openmarkov.core.model.network.State state : decision.getVariable().getStates()) {
            link.addRevealingState(state);
        }
    }

    /**
     * The case that used to come back empty: both decisions reveal the same
     * variable, so each dominated the other and both were discarded. A tie must
     * keep one — which of the two is a legitimate free choice, zero is not.
     */
    @Test
    void aTieBetweenTwoDecisionsKeepsOneNotZero() {
        reveal(decision1, observed);
        reveal(decision2, observed);

        List<Node> next = DANOperations.getNextDecisions(probNet);

        assertEquals(1, next.size(),
                "two decisions revealing the same set must leave one candidate, not " + next.size());
    }

    /** The pruning this method exists for: a strictly larger revealer wins. */
    @Test
    void aDecisionRevealingStrictlyMoreDiscardsTheLesserOne() {
        Variable extra = new Variable("C2", "absent", "present");
        Node alsoObserved = probNet.addNode(extra, NodeType.CHANCE);
        reveal(decision1, observed);
        reveal(decision1, alsoObserved);
        reveal(decision2, observed);

        List<Node> next = DANOperations.getNextDecisions(probNet);

        assertTrue(next.contains(decision1), "the decision revealing more must stay");
        assertFalse(next.contains(decision2), "the decision revealing a subset must be discarded");
    }

    /** Decisions that reveal nothing never dominated each other; that stands. */
    @Test
    void decisionsRevealingNothingAllStay() {
        List<Node> next = DANOperations.getNextDecisions(probNet);

        assertEquals(2, next.size());
    }

    /** The evidence-filtering entry point applies the same pruning. */
    @Test
    void theEvidenceVariantPrunesTheSameWay() {
        reveal(decision1, observed);
        reveal(decision2, observed);

        List<Node> next = DANOperations.getNextDecisions(probNet, new EvidenceCase());

        assertEquals(1, next.size());
    }
}
