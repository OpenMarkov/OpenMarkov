/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.action.core.VariableTypeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class VariableTypeEditTest {

    private Node finiteStatesNode;
    private Node discretizedNode;
    private Node numericNode;
    private ProbNet probNet;

    private static ProbNet getProbNet4Test() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        // Variables
        Variable varA = new Variable("A", "absent", "mild", "moderate", "severe");
        Variable varB = new Variable("B", "yes", "its possible", "maybe not", "no");
        Variable varC = new Variable("C");

        // Nodes
        Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
        Node nodeB = probNet.addNode(varB, NodeType.CHANCE);
        Node nodeC = probNet.addNode(varC, NodeType.CHANCE);

        nodeB.getVariable().setPartitionedInterval(new PartitionedInterval(nodeB.getVariable().getDefaultInterval(4),
                Variable.getDefaultBelongs(4)));

        // Links
        probNet.makeLinksExplicit(false);
        probNet.addLink(nodeA, nodeB, true);
        probNet.addLink(nodeA, nodeC, true);

        // Potentials
        UniformPotential potA = new UniformPotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeA.setPotential(potA);

        UniformPotential potB = new UniformPotential(Arrays.asList(varB, varA), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeB.setPotential(potB);

        UniformPotential potC = new UniformPotential(Arrays.asList(varC, varA), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeC.setPotential(potC);

        // Link restrictions and revealing states
        // Always observed nodes

        return probNet;
    }

    @BeforeEach
    public void setUp() {
        probNet = getProbNet4Test();
        finiteStatesNode = probNet.getNode("A");
        discretizedNode = probNet.getNode("B");
        numericNode = probNet.getNode("C");
    }

    @Test
    public void testNumeric2Discretized() throws DoEditException {
        State[] defaultStates = numericNode.getProbNet().getDefaultStates();
        State[] states = numericNode.getVariable().getStates().clone();
        PartitionedInterval currentInterval = (PartitionedInterval) numericNode.getVariable().getPartitionedInterval()
                .clone();
        VariableTypeEdit edit = new VariableTypeEdit(numericNode, VariableType.DISCRETIZED, true);

        probNet.getPNESupport().setWithUndo(true);
        edit.executeEdit();

        // Check the states of the node (if there is one)
        if (states.length != 1) {
            int i = 0;
            for (State state : states) {
                assertSame(state, numericNode.getVariable().getStates()[i++]);
            }
        } else {
            // If numeric variable don't have any state, we check for default states
            int i = 0;
            for (State state : defaultStates) {
                assertEquals(state.toString(), numericNode.getVariable().getStates()[i++].toString());
            }
        }

        // Undo changes
        probNet.getPNESupport().undo();
        // Gest the undoed interval
        PartitionedInterval undoedInterval = (PartitionedInterval) numericNode.getVariable().getPartitionedInterval()
                .clone();

        // Check the number of intervals
        assertEquals(currentInterval.getNumSubintervals(), undoedInterval.getNumSubintervals());

        // Check the limits
        for (int i = 0; i < undoedInterval.getNumSubintervals(); i++) {
            assertEquals(undoedInterval.getLimit(i), currentInterval.getLimit(i));
        }
    }

    @Test
    public void testNumeric2FiniteStates() throws DoEditException {
        State[] defaultStates = numericNode.getProbNet().getDefaultStates();
        State[] states = numericNode.getVariable().getStates().clone();
        PartitionedInterval currentInterval = (PartitionedInterval) numericNode.getVariable().getPartitionedInterval()
                .clone();
        VariableTypeEdit edit = new VariableTypeEdit(numericNode, VariableType.FINITE_STATES, true);

        probNet.getPNESupport().setWithUndo(true);
        edit.executeEdit();

        // Check the states of the node (if there is one)
        if (states.length != 1) {
            int i = 0;
            for (State state : states) {
                assertSame(state, numericNode.getVariable().getStates()[i++]);
            }
        } else {
            // If numeric variable don't have any state, we check for default states
            int i = 0;
            for (State state : defaultStates) {
                assertEquals(state.toString(), numericNode.getVariable().getStates()[i++].toString());
            }
        }

        // Undo changes
        probNet.getPNESupport().undo();
        // Gest the undoed interval
        PartitionedInterval undoedInterval = (PartitionedInterval) numericNode.getVariable().getPartitionedInterval()
                .clone();

        // Check the number of intervals
        assertEquals(currentInterval.getNumSubintervals(), undoedInterval.getNumSubintervals());

        // Check the limits
        for (int i = 0; i < undoedInterval.getNumSubintervals(); i++) {
            assertEquals(undoedInterval.getLimit(i), currentInterval.getLimit(i));
        }
    }

    @Test
    public void testFiniteStates2Discretized() throws DoEditException {
        State[] defaultStates = finiteStatesNode.getProbNet().getDefaultStates();
        State[] states = finiteStatesNode.getVariable().getStates().clone();

        VariableTypeEdit edit = new VariableTypeEdit(finiteStatesNode, VariableType.DISCRETIZED, true);

        probNet.getPNESupport().setWithUndo(true);
        edit.executeEdit();

        // Check the states of the node (if there is one)
        if (states.length != 1) {
            int i = 0;
            for (State state : states) {
                assertSame(state, finiteStatesNode.getVariable().getStates()[i++]);
            }
        } else {
            // If numeric variable don't have any state, we check for default states
            int i = 0;
            for (State state : defaultStates) {
                assertSame(state, finiteStatesNode.getVariable().getStates()[i++]);
            }
        }
        // Check if the number of intervals is equals to the number of previous states
        assertEquals(states.length, finiteStatesNode.getVariable().getPartitionedInterval().getNumSubintervals());
        // Undo changes
        probNet.getPNESupport().undo();
    }

    @Test
    public void testFiniteStates2Numeric() throws DoEditException {
        VariableTypeEdit edit = new VariableTypeEdit(finiteStatesNode, VariableType.NUMERIC, true);

        probNet.getPNESupport().setWithUndo(true);
        edit.executeEdit();

        // Check the states of the node
        assertEquals(1, finiteStatesNode.getVariable().getNumStates());
        assertEquals("", finiteStatesNode.getVariable().getStates()[0].getName());

        // Check if the number of intervals is equals to one
        assertEquals(1, finiteStatesNode.getVariable().getPartitionedInterval().getNumSubintervals());
        // Undo changes
        probNet.getPNESupport().undo();
    }

    @Test
    public void testDiscretized2FiniteStates() throws DoEditException {
        State[] states = discretizedNode.getVariable().getStates().clone();
        PartitionedInterval currentInterval = (PartitionedInterval) discretizedNode.getVariable()
                .getPartitionedInterval().clone();
        VariableTypeEdit edit = new VariableTypeEdit(discretizedNode, VariableType.FINITE_STATES, true);

        probNet.getPNESupport().setWithUndo(true);
        edit.executeEdit();

        // Check that the number of states must be different than one
        assertTrue(states.length != 1);

        int j = 0;
        for (State state : states) {
            assertSame(state, discretizedNode.getVariable().getStates()[j++]);
        }

        // Undo changes
        probNet.getPNESupport().undo();
        // Gest the undoed interval
        PartitionedInterval undoedInterval = (PartitionedInterval) discretizedNode.getVariable()
                .getPartitionedInterval().clone();

        // Check the number of intervals
        assertEquals(currentInterval.getNumSubintervals(), undoedInterval.getNumSubintervals());

        // Check the limits
        for (int i = 0; i < undoedInterval.getNumSubintervals(); i++) {
            assertEquals(undoedInterval.getLimit(i), currentInterval.getLimit(i));
        }
    }

    @Test
    public void testDiscretized2Numeric() throws DoEditException {
        PartitionedInterval currentInterval = (PartitionedInterval) discretizedNode.getVariable()
                .getPartitionedInterval().clone();
        VariableTypeEdit edit = new VariableTypeEdit(discretizedNode, VariableType.NUMERIC, true);
        probNet.getPNESupport().setWithUndo(true);
        edit.executeEdit();

        // Check the states of the node
        assertEquals(1, discretizedNode.getVariable().getNumStates());
        assertEquals("", discretizedNode.getVariable().getStates()[0].getName());

        // Undo changes
        probNet.getPNESupport().undo();
        // Gest the undoed interval
        PartitionedInterval undoedInterval = (PartitionedInterval) discretizedNode.getVariable()
                .getPartitionedInterval().clone();

        // Check the number of intervals
        assertEquals(currentInterval.getNumSubintervals(), undoedInterval.getNumSubintervals());

        // Check the limits
        for (int i = 0; i < undoedInterval.getNumSubintervals(); i++) {
            assertEquals(undoedInterval.getLimit(i), currentInterval.getLimit(i));
        }
    }
}
