/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.LinkOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.window.edition.EditorPanelClipboardAssistant;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A pasted link keeps the restrictions and the revelation conditions of the copied one, when the network
 * admits them, and a pasted node keeps the marks "always observed" and "input".
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class PastingKeepsWhatTheLinksAndNodesHadTest {

    private ProbNet dan;

    /** A decision analysis network X → Y: X = x0 is incompatible with Y = y0, X = x1 reveals Y, X always observed and input. */
    @BeforeEach void aDecisionAnalysisNetwork() {
        dan = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
        dan.setName("source");
        Variable x = new Variable("X", "x0", "x1");
        Variable y = new Variable("Y", "y0", "y1");
        Node nodeX = dan.addNode(x, NodeType.CHANCE);
        Node nodeY = dan.addNode(y, NodeType.CHANCE);
        dan.addLink(x, y, true);
        nodeX.setPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7 }));
        nodeY.setPotential(new TablePotential(List.of(y, x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0, 1, 0.5, 0.5 }));
        Link<Node> link = dan.getLink(nodeX, nodeY, true);
        LinkOperations.setCompatibilityValue(link, x.getStates()[0], y.getStates()[0], 0);
        link.setRevealingStates(new ArrayList<>(List.of(x.getStates()[1])));
        nodeX.setAlwaysObserved(true);
        nodeX.setInput(true);
    }

    private void copyAllAndPasteInto(ProbNet target) throws Exception {
        VisualNetwork visualNetwork = new VisualNetwork(dan, null);
        EditorPanelClipboardAssistant clipboard = new EditorPanelClipboardAssistant();
        visualNetwork.setSelectedAllObjects(true);
        visualNetwork.exportToClipboard(false, clipboard);
        new PasteEdit(target, clipboard.paste(), null).executeEdit();
    }

    @Test void inTheSameNetwork() throws Exception {
        copyAllAndPasteInto(dan);

        Node pastedX = dan.getNode("X'");
        Link<Node> pastedLink = dan.getLink(pastedX, dan.getNode("Y'"), true);
        assertTrue(pastedLink.hasRestrictions());
        assertEquals(List.of(pastedX.getVariable(), dan.getVariable("Y'")), pastedLink.getRestrictionsPotential().getVariables());
        assertArrayEquals(new double[] { 0, 1, 1, 1 }, pastedLink.getRestrictionsPotential().getValues());
        assertEquals(List.of(pastedX.getVariable().getStates()[1]), pastedLink.getRevealingStates());
        assertTrue(pastedX.isAlwaysObserved());
        assertTrue(pastedX.isInput());
    }

    @Test void inANetworkThatAdmitsNoRestrictions() throws Exception {
        ProbNet bayesianNetwork = new ProbNet(BayesianNetworkType.getUniqueInstance());
        copyAllAndPasteInto(bayesianNetwork);

        Link<Node> pastedLink = bayesianNetwork.getLink(bayesianNetwork.getNode("X"), bayesianNetwork.getNode("Y"), true);
        assertFalse(pastedLink.hasRestrictions());
        assertTrue(pastedLink.getRevealingStates().isEmpty());
    }
}
