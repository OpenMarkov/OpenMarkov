/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.window.edition.SelectedContent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PasteEdit}, verifying that copy-pasting nodes handles
 * external parent references correctly (issue #288).
 */
public class PasteEditTest {

    private ProbNet probNet;
    private Node nodeA;
    private Node nodeB;
    private Node nodeC;

    @BeforeEach
    void setUp() {
        probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        State[] states = {new State("yes"), new State("no")};
        nodeA = probNet.addNode(new Variable("A", states), NodeType.CHANCE);
        nodeB = probNet.addNode(new Variable("B", states), NodeType.CHANCE);
        nodeC = probNet.addNode(new Variable("C", states), NodeType.CHANCE);
        probNet.addLink(nodeA, nodeB, true);  // A -> B
        probNet.addLink(nodeA, nodeC, true);  // A -> C
        probNet.addLink(nodeB, nodeC, true);  // B -> C

        // Add potentials (addNode does not create them automatically)
        nodeA.setPotentials(List.of(new TablePotential(
                List.of(nodeA.getVariable()), PotentialRole.CONDITIONAL_PROBABILITY)));
        nodeB.setPotentials(List.of(new TablePotential(
                List.of(nodeB.getVariable(), nodeA.getVariable()), PotentialRole.CONDITIONAL_PROBABILITY)));
        nodeC.setPotentials(List.of(new TablePotential(
                List.of(nodeC.getVariable(), nodeA.getVariable(), nodeB.getVariable()), PotentialRole.CONDITIONAL_PROBABILITY)));
    }

    /**
     * Issue #288: copying a child node without its parents should NOT retain
     * external parent variable references in the pasted potential.
     * Previously, the potential kept references to A and B, causing phantom
     * links when the file was saved and reopened.
     */
    @Tag(TestSpeed.FAST)
    @Test
    void pastedNodeWithExternalParents_shouldNotRetainExternalVariables() throws DoEditException {
        // Copy only node C (which has parents A and B)
        SelectedContent clipboard = new SelectedContent(List.of(nodeC), List.of());
        
        PasteEdit pasteEdit = new PasteEdit(probNet, clipboard, null);
        pasteEdit.executeEdit();

        // Find the pasted node (C')
        Node pastedNode = probNet.getNode("C'");
        assertNotNull(pastedNode, "Pasted node C' should exist");

        // The pasted node should have no parents (A and B were not copied)
        assertTrue(pastedNode.getParents().isEmpty(),
                "Pasted node should have no parents since originals were not copied");

        // The pasted node's potential should only reference its own variable
        var potential = pastedNode.getPotentials().get(0);
        assertEquals(1, potential.getNumVariables(),
                "Potential should only contain the node's own variable, not external parents");
        assertEquals("C'", potential.getVariable(0).getName());
    }

    /**
     * When copying both a parent and its child, the pasted potential should
     * retain the internal parent reference (renamed) but NOT any external ones.
     */
    @Tag(TestSpeed.FAST)
    @Test
    void pastedNodesWithInternalLink_shouldRetainInternalParentVariable() throws DoEditException {
        // Copy B and C together (C has parents A and B; only B is copied)
        var linkBC = probNet.getLink(nodeB, nodeC, true);
        SelectedContent clipboard = new SelectedContent(List.of(nodeB, nodeC), List.of(linkBC));
        
        PasteEdit pasteEdit = new PasteEdit(probNet, clipboard, null);
        pasteEdit.executeEdit();

        Node pastedC = probNet.getNode("C'");
        Node pastedB = probNet.getNode("B'");
        assertNotNull(pastedC);
        assertNotNull(pastedB);

        // C' should have B' as parent (internal link was copied)
        assertTrue(pastedC.getParents().contains(pastedB),
                "Pasted C' should have B' as parent");

        // C' should NOT have A as parent (external, not copied)
        assertFalse(pastedC.getParents().contains(nodeA),
                "Pasted C' should NOT have original A as parent");

        // The potential of C' should reference C' and B', but NOT A
        var potential = pastedC.getPotentials().get(0);
        var varNames = potential.getVariables().stream().map(Variable::getName).toList();
        assertTrue(varNames.contains("C'"), "Potential should contain C'");
        assertFalse(varNames.contains("A"), "Potential should NOT contain external parent A");
    }

    /**
     * When all parents are copied together, the pasted potential should be
     * a full copy with all variable references updated.
     */
    @Tag(TestSpeed.FAST)
    @Test
    void pastedNodesWithAllParents_shouldRetainFullPotential() throws DoEditException {
        // Copy A, B, and C together with all links
        var linkAB = probNet.getLink(nodeA, nodeB, true);
        var linkAC = probNet.getLink(nodeA, nodeC, true);
        var linkBC = probNet.getLink(nodeB, nodeC, true);
        SelectedContent clipboard = new SelectedContent(
                List.of(nodeA, nodeB, nodeC), List.of(linkAB, linkAC, linkBC));
        
        PasteEdit pasteEdit = new PasteEdit(probNet, clipboard, null);
        pasteEdit.executeEdit();

        Node pastedC = probNet.getNode("C'");
        assertNotNull(pastedC);

        // C' should have both A' and B' as parents
        var parentNames = pastedC.getParents().stream().map(Node::getName).toList();
        assertTrue(parentNames.contains("A'"));
        assertTrue(parentNames.contains("B'"));

        // The potential should have 3 variables: C', A', B'
        var potential = pastedC.getPotentials().get(0);
        assertEquals(3, potential.getNumVariables(),
                "Potential should have the node's variable plus both pasted parents");
    }
}
