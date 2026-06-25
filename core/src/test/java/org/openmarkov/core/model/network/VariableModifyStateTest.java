/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for {@link Variable#modifyState(Node, StateAction, int, String)}.
 *
 * <p><strong>Index convention</strong>: for actions other than {@code ADD},
 * {@code stateIndex} is an <em>inverted</em> position:
 * {@code selectedStateIndex = numStates - (stateIndex + 1)}.
 * That means {@code stateIndex=0} refers to the <em>last</em> state in the
 * array, and {@code stateIndex = numStates-1} refers to the <em>first</em>.
 *
 * <p>For {@code ADD}, {@code stateIndex} is a direct insertion position.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class VariableModifyStateTest {

    /** A simple BN with one chance node (the subject under test). */
    private ProbNet net;
    /** The variable attached to the subject node. */
    private Variable variable;
    /** The node whose variable we are modifying. */
    private Node node;

    /**
     * Creates a Bayesian network with a single chance node having three states:
     * "s0", "s1", "s2" (indices 0, 1, 2).
     */
    @BeforeEach
    public void setUp() {
        net      = new ProbNet(BayesianNetworkType.getUniqueInstance());
        variable = new Variable("X",
                new State[]{new State("s0"), new State("s1"), new State("s2")});
        node     = net.addNode(variable, NodeType.CHANCE);
        // UP/DOWN need at least one potential on the node.
        TablePotential prior = new TablePotential(List.of(variable), PotentialRole.CONDITIONAL_PROBABILITY);
        node.setPotentials(List.of(prior));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String[] stateNames() {
        return Arrays.stream(variable.getStates())
                     .map(State::getName)
                     .toArray(String[]::new);
    }

    // -----------------------------------------------------------------------
    // ADD
    // -----------------------------------------------------------------------

    @Test
    public void addAtBeginningIncreasesStateCount() {
        variable.modifyState(node, StateAction.ADD, 0, "new");
        assertEquals(4, variable.getNumStates());
    }

    @Test
    public void addAtBeginningInsertsStateAtPositionZero() {
        variable.modifyState(node, StateAction.ADD, 0, "new");
        assertEquals("new", variable.getStates()[0].getName());
    }

    @Test
    public void addAtBeginningPreservesExistingStatesAfterInsertion() {
        variable.modifyState(node, StateAction.ADD, 0, "new");
        assertArrayEquals(new String[]{"new", "s0", "s1", "s2"}, stateNames());
    }

    @Test
    public void addAtEndAppendsState() {
        // stateIndex == numStates → insert at the end
        variable.modifyState(node, StateAction.ADD, 3, "last");
        assertArrayEquals(new String[]{"s0", "s1", "s2", "last"}, stateNames());
    }

    @Test
    public void addInMiddleInsertsAtCorrectPosition() {
        variable.modifyState(node, StateAction.ADD, 1, "mid");
        assertArrayEquals(new String[]{"s0", "mid", "s1", "s2"}, stateNames());
    }

    @Test
    public void addResetsNodePotentialToUniform() {
        // After ADD the node's potential must have been reset (not null).
        variable.modifyState(node, StateAction.ADD, 0, "extra");
        assertFalse(node.getPotentials().isEmpty(),
                "Node must have a potential after ADD");
    }

    // -----------------------------------------------------------------------
    // REMOVE
    // -----------------------------------------------------------------------

    @Test
    public void removeDecreasesStateCount() {
        // stateIndex=0 → removes selectedStateIndex = 3-1 = 2 (last state "s2")
        variable.modifyState(node, StateAction.REMOVE, 0, "");
        assertEquals(2, variable.getNumStates());
    }

    @Test
    public void removeLastStateByInvertedIndex() {
        // stateIndex=0 → selectedStateIndex=2 → "s2" is removed
        variable.modifyState(node, StateAction.REMOVE, 0, "");
        assertArrayEquals(new String[]{"s0", "s1"}, stateNames());
    }

    @Test
    public void removeFirstStateByInvertedIndex() {
        // stateIndex=2 → selectedStateIndex=0 → "s0" is removed
        variable.modifyState(node, StateAction.REMOVE, 2, "");
        assertArrayEquals(new String[]{"s1", "s2"}, stateNames());
    }

    @Test
    public void removeMiddleStateByInvertedIndex() {
        // stateIndex=1 → selectedStateIndex=1 → "s1" is removed
        variable.modifyState(node, StateAction.REMOVE, 1, "");
        assertArrayEquals(new String[]{"s0", "s2"}, stateNames());
    }

    @Test
    public void removeResetsNodePotential() {
        variable.modifyState(node, StateAction.REMOVE, 0, "");
        assertFalse(node.getPotentials().isEmpty(),
                "Node must have a potential after REMOVE");
    }

    // -----------------------------------------------------------------------
    // RENAME
    // -----------------------------------------------------------------------

    @Test
    public void renameDoesNotChangeStateCount() {
        variable.modifyState(node, StateAction.RENAME, 0, "renamed");
        assertEquals(3, variable.getNumStates());
    }

    @Test
    public void renameLastStateByInvertedIndex() {
        // stateIndex=0 → selectedStateIndex = 3-1 = 2 → "s2" is renamed.
        // RENAME: newState = selectedState; newState.setName(newName) mutates it in place.
        variable.modifyState(node, StateAction.RENAME, 0, "renamed");
        assertArrayEquals(new String[]{"s0", "s1", "renamed"}, stateNames());
    }

    @Test
    public void renameDoesNotAlterOtherStates() {
        variable.modifyState(node, StateAction.RENAME, 1, "any");
        assertEquals("s0", variable.getStates()[0].getName());
        assertEquals("s2", variable.getStates()[2].getName());
    }

    // -----------------------------------------------------------------------
    // UP / DOWN
    // -----------------------------------------------------------------------

    @Test
    public void downSwapsSelectedStateWithPredecessor() {
        // stateIndex=0 → selectedStateIndex=2 ("s2"). DOWN swaps with index 1 ("s1").
        variable.modifyState(node, StateAction.DOWN, 0, "");
        assertArrayEquals(new String[]{"s0", "s2", "s1"}, stateNames());
    }

    @Test
    public void downOnFirstSelectedIndexIsNoOp() {
        // stateIndex=2 → selectedStateIndex=0. DOWN at 0: condition (selectedStateIndex > 0) is false → no swap.
        String[] before = stateNames().clone();
        variable.modifyState(node, StateAction.DOWN, 2, "");
        assertArrayEquals(before, stateNames());
    }

    @Test
    public void upSwapsSelectedStateWithSuccessor() {
        // stateIndex=2 → selectedStateIndex=0 ("s0"). UP swaps with index 1 ("s1").
        variable.modifyState(node, StateAction.UP, 2, "");
        assertArrayEquals(new String[]{"s1", "s0", "s2"}, stateNames());
    }

    @Test
    public void upOnLastSelectedIndexIsNoOp() {
        // stateIndex=0 → selectedStateIndex=2 (last). UP at last index is a no-op.
        String[] before = stateNames().clone();
        variable.modifyState(node, StateAction.UP, 0, "");
        assertArrayEquals(before, stateNames());
    }

    // -----------------------------------------------------------------------
    // Sequential operations
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Impact on child nodes
    // -----------------------------------------------------------------------

    @Test
    public void addStateAlsoResetsChildNodePotential() {
        // Build A → B. Give B an initial CPT with pBgA.getValues()[0]=0.9.
        Variable b = new Variable("B", new State[]{new State("b0"), new State("b1")});
        Node nodeB = net.addNode(b, NodeType.CHANCE);
        net.addLink(node, nodeB, true);
        TablePotential pBgA = new TablePotential(List.of(b, variable), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1, 0.8, 0.2});
        nodeB.setPotentials(List.of(pBgA));

        // Modify parent variable by adding a state.
        variable.modifyState(node, StateAction.ADD, 3, "s3");

        // setUniformPotential replaces B's potential (with a UniformPotential) — different object.
        assertNotSame(pBgA, nodeB.getPotentials().get(0),
                "Child potential must be replaced after parent state is added");
        Potential newChildPotential = nodeB.getPotentials().get(0);
        assertTrue(newChildPotential.getVariables().contains(b));
        assertTrue(newChildPotential.getVariables().contains(variable));
    }

    @Test
    public void removeStateAlsoResetsChildNodePotential() {
        Variable b = new Variable("B", new State[]{new State("b0"), new State("b1")});
        Node nodeB = net.addNode(b, NodeType.CHANCE);
        net.addLink(node, nodeB, true);
        TablePotential pBgA = new TablePotential(List.of(b, variable), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1, 0.8, 0.2, 0.7, 0.3});
        nodeB.setPotentials(List.of(pBgA));

        // Remove last state of parent (stateIndex=0 → selectedStateIndex=2).
        variable.modifyState(node, StateAction.REMOVE, 0, "");

        // setUniformPotential replaces B's potential (with a UniformPotential) — different object.
        assertNotSame(pBgA, nodeB.getPotentials().get(0),
                "Child potential must be replaced after parent state is removed");
        Potential newChildPotential = nodeB.getPotentials().get(0);
        assertTrue(newChildPotential.getVariables().contains(b));
        assertTrue(newChildPotential.getVariables().contains(variable));
    }

    // -----------------------------------------------------------------------
    // DISCRETIZED variable — PartitionedInterval is updated
    // -----------------------------------------------------------------------

    /**
     * Creates a DISCRETIZED variable with 2 states and interval limits [0, 1, +∞).
     * Precision = 1.0 so that getNewPartitionedInterval inserts a new boundary.
     */
    private Variable buildDiscretizedVariable(String name) {
        State[] states = new State[]{new State("low"), new State("high")};
        // limits: [0.0, 1.0, +Inf], belongsToLeftSide: [false, false, false]
        double[] limits = {0.0, 1.0, Double.POSITIVE_INFINITY};
        boolean[] belongs = {false, false, false};
        PartitionedInterval pi = new PartitionedInterval(limits, belongs);
        return new Variable(name, states, pi, 1.0);
    }

    @Test
    public void addStateToDiscretizedVariableIncreasesIntervalLimitCount() {
        Variable disc = buildDiscretizedVariable("D");
        Node nodeDisc = net.addNode(disc, NodeType.CHANCE);
        TablePotential prior = new TablePotential(List.of(disc), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeDisc.setPotentials(List.of(prior));

        int limitsBefore = disc.getPartitionedInterval().getLimits().length;
        disc.modifyState(nodeDisc, StateAction.ADD, 2, "mid"); // insert at end-of-finite

        assertEquals(limitsBefore + 1, disc.getPartitionedInterval().getLimits().length,
                "ADD must insert a new boundary in the PartitionedInterval");
    }

    @Test
    public void removeStateFromDiscretizedVariableDecreasesIntervalLimitCount() {
        Variable disc = buildDiscretizedVariable("D");
        Node nodeDisc = net.addNode(disc, NodeType.CHANCE);
        TablePotential prior = new TablePotential(List.of(disc), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeDisc.setPotentials(List.of(prior));

        int limitsBefore = disc.getPartitionedInterval().getLimits().length;
        // stateIndex=0 → selectedStateIndex = 2-1 = 1 → remove last state "high"
        disc.modifyState(nodeDisc, StateAction.REMOVE, 0, "");

        assertEquals(limitsBefore - 1, disc.getPartitionedInterval().getLimits().length,
                "REMOVE must delete a boundary from the PartitionedInterval");
    }

    @Test
    public void removeStateFromDiscretizedVariableRemovesCorrectBoundary() {
        Variable disc = buildDiscretizedVariable("D");
        Node nodeDisc = net.addNode(disc, NodeType.CHANCE);
        TablePotential prior = new TablePotential(List.of(disc), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeDisc.setPotentials(List.of(prior));

        // stateIndex=1 → selectedStateIndex = 2-2 = 0 → remove "low" (first state)
        // pos = selectedStateIndex = 0; removes limits[0] = 0.0
        disc.modifyState(nodeDisc, StateAction.REMOVE, 1, "");

        double[] newLimits = disc.getPartitionedInterval().getLimits();
        assertEquals(2, newLimits.length);
        assertEquals(1.0, newLimits[0], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, newLimits[1]);
    }

    // -----------------------------------------------------------------------
    // Sequential operations
    // -----------------------------------------------------------------------

    @Test
    public void addThenRemoveRestoresOriginalStates() {
        // Add at end
        variable.modifyState(node, StateAction.ADD, 3, "tmp");
        assertEquals(4, variable.getNumStates());

        // Remove the newly added state (stateIndex=0 → selectedStateIndex=3, the last one "tmp")
        variable.modifyState(node, StateAction.REMOVE, 0, "");
        assertArrayEquals(new String[]{"s0", "s1", "s2"}, stateNames());
    }
}
