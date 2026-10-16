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
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.LinkOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Changing a variable between "finite states" and "discretized", which keeps its states, keeps the
 * restrictions and the revelation conditions of its links; a change through "numeric" removes the restrictions.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AChangeBetweenFiniteStatesAndDiscretizedKeepsTheLinksTest {

    private ProbNet net;
    private Node nodeX;
    private Variable x;
    private Link<Node> link;

    /** A decision analysis network X → Y; the link has a restriction and reveals Y when X is x1. */
    @BeforeEach void aDecisionAnalysisNetwork() {
        net = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
        x = new Variable("X", "x0", "x1");
        Variable y = new Variable("Y", "y0", "y1");
        nodeX = net.addNode(x, NodeType.CHANCE);
        Node nodeY = net.addNode(y, NodeType.CHANCE);
        net.addLink(x, y, true);
        nodeX.setPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7 }));
        nodeY.setPotential(new TablePotential(List.of(y, x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.5, 0.5, 1, 0 }));
        link = net.getLink(nodeX, nodeY, true);
        LinkOperations.setCompatibilityValue(link, x.getStates()[1], y.getStates()[1], 0);
        link.setRevealingStates(new ArrayList<>(List.of(x.getStates()[1])));
    }

    private void assertTheLinkKeepsWhatItHad() {
        assertTrue(link.hasRestrictions());
        assertEquals(List.of(x.getStates()[1]), link.getRevealingStates());
    }

    @Test void toDiscretizedAndBack() throws Exception {
        new VariableTypeEdit(nodeX, VariableType.DISCRETIZED, true).executeEdit();
        assertTheLinkKeepsWhatItHad();
        new VariableTypeEdit(nodeX, VariableType.FINITE_STATES, true).executeEdit();
        assertTheLinkKeepsWhatItHad();
    }

    @Test void toNumericRemovesTheRestrictionAndUndoingGivesItBack() throws Exception {
        net.getPNESupport().setWithUndo(true);
        new VariableTypeEdit(nodeX, VariableType.NUMERIC, true).executeEdit();
        assertFalse(link.hasRestrictions());

        net.getPNESupport().undo();
        assertTheLinkKeepsWhatItHad();
    }
}
