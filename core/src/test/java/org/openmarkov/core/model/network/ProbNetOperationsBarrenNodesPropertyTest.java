/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Property-based tests for {@link ProbNetOperations#removeBarrenNodes}.
 *
 * <p>A barren node is one that:
 * <ul>
 *   <li>is not in {@code variablesOfInterest},</li>
 *   <li>is not in {@code variablesOfEvidence}, and</li>
 *   <li>has no children, or all its children are barren.</li>
 * </ul>
 *
 * <p>The algorithm is a fixed-point iteration that propagates barrenness up
 * from leaf nodes. Tests verify both correctness (the right nodes are removed)
 * and safety (interest/evidence nodes are never removed).
 */
class ProbNetOperationsBarrenNodesPropertyTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ProbNet freshBN() {
        return new ProbNet(BayesianNetworkType.getUniqueInstance());
    }

    private Variable chance(String name) {
        return new Variable(name, 2);
    }

    private Node add(ProbNet net, Variable v) {
        return net.addNode(v, NodeType.CHANCE);
    }

    private void link(ProbNet net, Variable parent, Variable child) {
        net.addLink(parent, child, true);
    }

    // -----------------------------------------------------------------------
    // Safety invariants
    // -----------------------------------------------------------------------

    /**
     * A node that is a variable of interest must never be removed,
     * even if it is a leaf (no children).
     */
    @Property
    void interestLeaf_isNeverRemoved(
            @ForAll @IntRange(min = 2, max = 5) int numStates) {
        Variable v = new Variable("Q", numStates);
        ProbNet net = freshBN();
        add(net, v);

        Collection<Variable> interest = Set.of(v);
        HashSet<Variable> evidence = new HashSet<>();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);

        assertThat(net.getNode(v))
                .as("interest variable must remain in the network")
                .isNotNull();
    }

    /**
     * A node that is a variable of evidence must never be removed,
     * even if it is a leaf.
     */
    @Property
    void evidenceLeaf_isNeverRemoved(
            @ForAll @IntRange(min = 2, max = 5) int numStates) {
        Variable v = new Variable("E", numStates);
        ProbNet net = freshBN();
        add(net, v);

        Collection<Variable> interest = Set.of();
        HashSet<Variable> evidence = new HashSet<>(Set.of(v));

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);

        assertThat(net.getNode(v))
                .as("evidence variable must remain in the network")
                .isNotNull();
    }

    /**
     * A parent of an interest node must not be removed even though
     * it has no other (non-barren) descendants.
     *
     * <p>Network: A → B, B is interest.  A is not interest/evidence.
     * A has a non-barren child (B), so A is not barren.
     */
    @Example
    void parentOfInterestNode_isNotRemoved() {
        Variable vA = chance("A");
        Variable vB = chance("B");
        ProbNet net = freshBN();
        add(net, vA);
        add(net, vB);
        link(net, vA, vB);

        Collection<Variable> interest = Set.of(vB);
        HashSet<Variable> evidence = new HashSet<>();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);

        assertThat(net.getNode(vA))
                .as("parent of an interest node must not be removed")
                .isNotNull();
        assertThat(net.getNode(vB))
                .as("interest node must not be removed")
                .isNotNull();
    }

    // -----------------------------------------------------------------------
    // Barren-node removal invariants
    // -----------------------------------------------------------------------

    /**
     * A leaf node that is neither interest nor evidence must be removed.
     */
    @Property
    void barrenLeaf_isRemoved(
            @ForAll @IntRange(min = 2, max = 5) int numStates) {
        Variable v = new Variable("B", numStates);
        ProbNet net = freshBN();
        add(net, v);

        Collection<Variable> interest = Set.of();
        HashSet<Variable> evidence = new HashSet<>();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);

        assertThat(net.getNode(v))
                .as("barren leaf must be removed from the network")
                .isNull();
    }

    /**
     * A chain A → B → C where C is barren must have all three nodes removed
     * when none of them is interest or evidence.
     *
     * <p>Tests the fixed-point propagation: C is removed first (leaf), then B
     * (its only child C is barren), then A.
     */
    @Example
    void barrenChain_isFullyRemoved() {
        Variable vA = chance("A");
        Variable vB = chance("B");
        Variable vC = chance("C");
        ProbNet net = freshBN();
        add(net, vA);
        add(net, vB);
        add(net, vC);
        link(net, vA, vB);
        link(net, vB, vC);

        Collection<Variable> interest = Set.of();
        HashSet<Variable> evidence = new HashSet<>();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);

        assertThat(net.getNode(vA)).as("A must be removed (barren chain root)").isNull();
        assertThat(net.getNode(vB)).as("B must be removed (barren chain middle)").isNull();
        assertThat(net.getNode(vC)).as("C must be removed (barren leaf)").isNull();
    }

    /**
     * A chain A → B → C where C is of interest:
     * none of the nodes should be removed.
     */
    @Example
    void chainWithInterestLeaf_noneRemoved() {
        Variable vA = chance("A");
        Variable vB = chance("B");
        Variable vC = chance("C");
        ProbNet net = freshBN();
        add(net, vA);
        add(net, vB);
        add(net, vC);
        link(net, vA, vB);
        link(net, vB, vC);

        Collection<Variable> interest = Set.of(vC);
        HashSet<Variable> evidence = new HashSet<>();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);

        assertThat(net.getNode(vA)).as("A must not be removed (ancestor of interest)").isNotNull();
        assertThat(net.getNode(vB)).as("B must not be removed (ancestor of interest)").isNotNull();
        assertThat(net.getNode(vC)).as("C must not be removed (interest)").isNotNull();
    }

    /**
     * A parent with two children — one barren, one of interest — must not be
     * removed.  Only the barren child should be removed.
     *
     * <p>Network: A → B (barren leaf), A → C (interest leaf).
     */
    @Example
    void parentWithMixedChildren_onlyBarrenChildRemoved() {
        Variable vA = chance("A");
        Variable vB = chance("B"); // barren
        Variable vC = chance("C"); // interest
        ProbNet net = freshBN();
        add(net, vA);
        add(net, vB);
        add(net, vC);
        link(net, vA, vB);
        link(net, vA, vC);

        Collection<Variable> interest = Set.of(vC);
        HashSet<Variable> evidence = new HashSet<>();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);

        assertThat(net.getNode(vA)).as("A must not be removed (has non-barren child C)").isNotNull();
        assertThat(net.getNode(vB)).as("B must be removed (barren leaf)").isNull();
        assertThat(net.getNode(vC)).as("C must not be removed (interest)").isNotNull();
    }

    /**
     * Diamond pattern: A → B, A → C, B → D, C → D, D is a barren leaf.
     * All four nodes must be removed when none is interest or evidence.
     *
     * <p>Tests that the algorithm correctly handles the case where a parent
     * (A) has two children (B, C) that both become barren in the same
     * fixed-point iteration.
     */
    @Example
    void diamondAllBarren_allFourRemoved() {
        Variable vA = chance("A");
        Variable vB = chance("B");
        Variable vC = chance("C");
        Variable vD = chance("D");
        ProbNet net = freshBN();
        add(net, vA);
        add(net, vB);
        add(net, vC);
        add(net, vD);
        link(net, vA, vB);
        link(net, vA, vC);
        link(net, vB, vD);
        link(net, vC, vD);

        Collection<Variable> interest = Set.of();
        HashSet<Variable> evidence = new HashSet<>();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);

        assertThat(net.getNode(vA)).as("A must be removed").isNull();
        assertThat(net.getNode(vB)).as("B must be removed").isNull();
        assertThat(net.getNode(vC)).as("C must be removed").isNull();
        assertThat(net.getNode(vD)).as("D must be removed (barren leaf)").isNull();
    }

    /**
     * Diamond pattern with D of interest:
     * A → B, A → C, B → D, C → D, D is of interest → none removed.
     */
    @Example
    void diamondWithInterestLeaf_noneRemoved() {
        Variable vA = chance("A");
        Variable vB = chance("B");
        Variable vC = chance("C");
        Variable vD = chance("D");
        ProbNet net = freshBN();
        add(net, vA);
        add(net, vB);
        add(net, vC);
        add(net, vD);
        link(net, vA, vB);
        link(net, vA, vC);
        link(net, vB, vD);
        link(net, vC, vD);

        Collection<Variable> interest = Set.of(vD);
        HashSet<Variable> evidence = new HashSet<>();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);

        assertThat(net.getNode(vA)).as("A must not be removed").isNotNull();
        assertThat(net.getNode(vB)).as("B must not be removed").isNotNull();
        assertThat(net.getNode(vC)).as("C must not be removed").isNotNull();
        assertThat(net.getNode(vD)).as("D must not be removed (interest)").isNotNull();
    }

    /**
     * Mixed graph: some sub-graphs are barren, others are not.
     *
     * <p>Network:
     * <pre>
     *   A → B (interest)       ← not barren
     *   C → D → E              ← fully barren chain
     *   F (interest, leaf)     ← not barren
     * </pre>
     * Expected: C, D, E removed; A, B, F kept.
     */
    @Example
    void mixedGraph_onlyBarrenSubtreeRemoved() {
        Variable vA = chance("A");
        Variable vB = chance("B"); // interest
        Variable vC = chance("C");
        Variable vD = chance("D");
        Variable vE = chance("E");
        Variable vF = chance("F"); // interest
        ProbNet net = freshBN();
        add(net, vA);
        add(net, vB);
        add(net, vC);
        add(net, vD);
        add(net, vE);
        add(net, vF);
        link(net, vA, vB);
        link(net, vC, vD);
        link(net, vD, vE);

        Collection<Variable> interest = Set.of(vB, vF);
        HashSet<Variable> evidence = new HashSet<>();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);

        assertThat(net.getNode(vA)).as("A (parent of interest B) must not be removed").isNotNull();
        assertThat(net.getNode(vB)).as("B (interest) must not be removed").isNotNull();
        assertThat(net.getNode(vC)).as("C (barren chain root) must be removed").isNull();
        assertThat(net.getNode(vD)).as("D (barren) must be removed").isNull();
        assertThat(net.getNode(vE)).as("E (barren leaf) must be removed").isNull();
        assertThat(net.getNode(vF)).as("F (interest leaf) must not be removed").isNotNull();
    }

    // -----------------------------------------------------------------------
    // Fixed-point / idempotence invariant
    // -----------------------------------------------------------------------

    /**
     * Calling {@code removeBarrenNodes} twice must produce the same result as
     * calling it once.  After the first call the network contains no barren
     * nodes, so the second call must leave it unchanged.
     */
    @Example
    void removeBarrenNodes_isIdempotent() {
        Variable vA = chance("A");
        Variable vB = chance("B"); // barren
        Variable vC = chance("C"); // interest
        ProbNet net = freshBN();
        add(net, vA);
        add(net, vB);
        add(net, vC);
        link(net, vA, vB);
        link(net, vA, vC);

        Collection<Variable> interest = Set.of(vC);
        HashSet<Variable> evidence = new HashSet<>();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);
        int nodesAfterFirst = net.getNodes().size();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);
        int nodesAfterSecond = net.getNodes().size();

        assertThat(nodesAfterSecond)
                .as("second call must not remove any additional nodes")
                .isEqualTo(nodesAfterFirst);
    }

    /**
     * After removing barren nodes, every remaining leaf node must be either
     * a variable of interest or a variable of evidence.
     *
     * <p>This is the completeness invariant: the algorithm must not leave any
     * barren node behind.
     */
    @Property
    void afterRemoval_noRemainingLeafIsBarren(
            @ForAll @IntRange(min = 2, max = 5) int numStates) {
        // Build: root → leaf1 (barren), root → leaf2 (interest)
        Variable vRoot  = new Variable("Root",  numStates);
        Variable vLeaf1 = new Variable("Leaf1", numStates); // barren
        Variable vLeaf2 = new Variable("Leaf2", numStates); // interest

        ProbNet net = freshBN();
        add(net, vRoot);
        add(net, vLeaf1);
        add(net, vLeaf2);
        link(net, vRoot, vLeaf1);
        link(net, vRoot, vLeaf2);

        Collection<Variable> interest  = Set.of(vLeaf2);
        HashSet<Variable>    evidence  = new HashSet<>();

        ProbNetOperations.removeBarrenNodes(net, interest, evidence);

        // After removal, check that every remaining leaf is in interest or evidence
        List<Node> remaining = net.getNodes();
        for (Node node : remaining) {
            if (node.getNumChildren() == 0) {
                Variable v = node.getVariable();
                assertThat(interest.contains(v) || evidence.contains(v))
                        .as("remaining leaf '%s' must be in interest or evidence", v.getName())
                        .isTrue();
            }
        }
    }
}
