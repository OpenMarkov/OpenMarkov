/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Changing the variable type of a decision, or of a parent of a decision, through "numeric" never
 * imposes a policy on the decision, and removes the one it had, whose cells no longer fit.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AChangeOfVariableTypeImposesNoPolicyTest {

    private ProbNet net;
    private Node nodeX;
    private Node nodeD;

    /** X → D, with D left without an imposed policy. */
    @BeforeEach void anInfluenceDiagram() {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable x = new Variable("X", "x0", "x1");
        Variable d = new Variable("D", "d0", "d1");
        nodeX = net.addNode(x, NodeType.CHANCE);
        nodeD = net.addNode(d, NodeType.DECISION);
        net.addLink(x, d, true);
        nodeX.setPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.5, 0.5 }));
    }

    private void imposeThePolicyDEqualsX() {
        nodeD.setPotential(new TablePotential(List.of(nodeD.getVariable(), nodeX.getVariable()), PotentialRole.POLICY,
                                              new double[] { 1, 0, 0, 1 }));
    }

    @Test void aDecisionWithoutPolicyStaysWithoutOne() throws Exception {
        new VariableTypeEdit(nodeD, VariableType.NUMERIC, true).executeEdit();
        assertFalse(nodeD.hasPolicy());
        new VariableTypeEdit(nodeD, VariableType.FINITE_STATES, true).executeEdit();
        assertFalse(nodeD.hasPolicy());
    }

    @Test void aDecisionLosesItsImposedPolicy() throws Exception {
        imposeThePolicyDEqualsX();
        new VariableTypeEdit(nodeD, VariableType.NUMERIC, true).executeEdit();
        assertFalse(nodeD.hasPolicy());
    }

    @Test void aDecisionLosesItsImposedPolicyWhenItsParentChanges() throws Exception {
        imposeThePolicyDEqualsX();
        new VariableTypeEdit(nodeX, VariableType.NUMERIC, true).executeEdit();
        assertFalse(nodeD.hasPolicy());
    }

    @Test void undoingGivesBackThePolicy() throws Exception {
        imposeThePolicyDEqualsX();
        net.getPNESupport().setWithUndo(true);
        new VariableTypeEdit(nodeD, VariableType.NUMERIC, true).executeEdit();
        net.getPNESupport().undo();
        assertArrayEquals(new double[] { 1, 0, 0, 1 }, ((TablePotential) nodeD.getPotentials().getFirst()).getValues());
    }
}
