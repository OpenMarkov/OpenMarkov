/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link DANOperations#selectVariableWithoutAncestorsInVariables} decides which
 * variable a decision tree branches on next, so it decides the shape of every
 * tree the DAN algorithms build: it must pick a candidate none of whose
 * ancestors is also a candidate, so that the tree branches on causes before
 * effects.
 *
 * @author Manuel Arias
 */
class SelectVariableWithoutAncestorsTest {

    private ProbNet probNet;
    private Variable cause;
    private Variable effect;
    private Variable grandEffect;

    /** Builds the chain cause → effect → grandEffect. */
    @BeforeEach
    void setUp() {
        probNet = new ProbNet();
        cause = new Variable("Cause", "no", "yes");
        effect = new Variable("Effect", "no", "yes");
        grandEffect = new Variable("GrandEffect", "no", "yes");

        Node causeNode = probNet.addNode(cause, NodeType.CHANCE);
        Node effectNode = probNet.addNode(effect, NodeType.CHANCE);
        Node grandEffectNode = probNet.addNode(grandEffect, NodeType.CHANCE);
        probNet.addLink(causeNode, effectNode, true);
        probNet.addLink(effectNode, grandEffectNode, true);
    }

    /**
     * The first candidate is not exempt from the ancestor check: listed after its
     * own cause, it must lose to it. This is the case the previous code got wrong
     * — it always returned the first candidate, whatever its ancestry.
     */
    @Test
    void skipsAFirstCandidateWhoseAncestorIsAlsoACandidate() {
        Variable selected = DANOperations
                .selectVariableWithoutAncestorsInVariables(List.of(effect, cause), probNet);

        assertSame(cause, selected);
    }

    /** Whatever the order of the candidates, the answer is the cause. */
    @Test
    void selectsTheRootOfAChainFromAnyCandidateOrder() {
        List<List<Variable>> orders = List.of(
                List.of(cause, effect, grandEffect),
                List.of(effect, grandEffect, cause),
                List.of(grandEffect, effect, cause));

        for (List<Variable> order : orders) {
            assertSame(cause, DANOperations.selectVariableWithoutAncestorsInVariables(order, probNet),
                    "candidate order " + order);
        }
    }

    /**
     * Candidates unrelated to each other: the first one is a fine answer — the
     * point of the method is only to never pick an effect over its cause.
     */
    @Test
    void keepsTheFirstCandidateWhenNoCandidateDescendsFromAnother() {
        Variable selected = DANOperations
                .selectVariableWithoutAncestorsInVariables(List.of(effect, grandEffect), probNet);

        // effect's ancestor (cause) is not a candidate, so effect stands.
        assertSame(effect, selected);
    }
}
