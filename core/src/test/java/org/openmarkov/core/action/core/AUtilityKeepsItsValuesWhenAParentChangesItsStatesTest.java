/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * When a parent of a utility node gains or loses a state, the utility keeps the values of the configurations
 * that are still there, with 0 in those of the new state; when the states of the parent change otherwise, the
 * utility gets the relation of a new utility node. Never a uniform relation, which no utility can use.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AUtilityKeepsItsValuesWhenAParentChangesItsStatesTest {

    private ProbNet net;
    private Node nodeX;
    private Node nodeD;
    private Node nodeU;

    /** X → U, D → U, with U(D, X) = (1, 2, 3, 4), D changing fastest. */
    private void anInfluenceDiagram(boolean exact) {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable x = new Variable("X", "x0", "x1");
        Variable d = new Variable("D", "d0", "d1");
        Variable u = new Variable("U");
        nodeX = net.addNode(x, NodeType.CHANCE);
        nodeD = net.addNode(d, NodeType.DECISION);
        nodeU = net.addNode(u, NodeType.UTILITY);
        Criterion criterion = new Criterion("Cost", "euro");
        net.setDecisionCriteria(new ArrayList<>(List.of(criterion)));
        u.setDecisionCriterion(criterion);
        net.addLink(x, u, true);
        net.addLink(d, u, true);
        nodeX.setPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.5, 0.5 }));
        double[] values = { 1, 2, 3, 4 };
        nodeU.setPotential(exact ? new ExactDistrPotential(List.of(u, d, x), PotentialRole.UNSPECIFIED, values)
                                 : new TablePotential(List.of(u, d, x), PotentialRole.UNSPECIFIED, values));
    }

    private double[] valuesOfU() throws Exception {
        return nodeU.getPotential().getCPT().getValues();
    }

    @ParameterizedTest(name = "exact distribution: {0}")
    @ValueSource(booleans = { false, true })
    void aStateAddedToAParent(boolean exact) throws Exception {
        anInfluenceDiagram(exact);
        new NodeStateEdit(nodeX, StateAction.ADD, 1, "c").executeEdit();
        assertArrayEquals(new double[] { 1, 2, 0, 0, 3, 4 }, valuesOfU());
    }

    @ParameterizedTest(name = "exact distribution: {0}")
    @ValueSource(booleans = { false, true })
    void aStateRemovedFromAParent(boolean exact) throws Exception {
        anInfluenceDiagram(exact);
        new NodeStateEdit(nodeD, StateAction.REMOVE, 0, "").executeEdit();
        assertArrayEquals(new double[] { 1, 3 }, valuesOfU());
    }

    @Test void undoingGivesBackTheValues() throws Exception {
        anInfluenceDiagram(true);
        net.getPNESupport().setWithUndo(true);
        new NodeStateEdit(nodeX, StateAction.ADD, 1, "c").executeEdit();
        net.getPNESupport().undo();
        assertArrayEquals(new double[] { 1, 2, 3, 4 }, valuesOfU());
    }

    @Test void aStandardDomainOfAnotherSize() throws Exception {
        anInfluenceDiagram(true);
        new NodeReplaceStatesEdit(nodeX, new State[] { new State("a"), new State("b"), new State("c") }).executeEdit();
        assertFalse(nodeU.getPotential() instanceof UniformPotential);
        assertArrayEquals(new double[6], valuesOfU());
    }

    @Test void aChangeOfVariableTypeThroughNumeric() throws Exception {
        anInfluenceDiagram(true);
        new VariableTypeEdit(nodeX, VariableType.NUMERIC, true).executeEdit();
        new VariableTypeEdit(nodeX, VariableType.FINITE_STATES, true).executeEdit();
        assertFalse(nodeU.getPotential() instanceof UniformPotential);
        assertArrayEquals(new double[4], valuesOfU());
    }
}
