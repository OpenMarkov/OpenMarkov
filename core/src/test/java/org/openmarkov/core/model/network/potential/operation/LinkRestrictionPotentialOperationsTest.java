/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class LinkRestrictionPotentialOperationsTest {
    private ProbNet net;
    private Variable varA, varB, varC;
    private State[] stateA, stateB, stateC;
    private Link<Node> link, link2;
    
    // private Node nodeA, nodeB, nodeC;
    
    private static ProbNet buildDAN_error_res_5_parents_pgmx() {
        ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
        // Variables
        Variable varA = new Variable("A", "absent", "present");
        Variable varB = new Variable("B", "absent", "present");
        Variable varC = new Variable("C", "absent", "present");
        Variable varE = new Variable("E", "absent", "mild", "moderate", "severe");
        Variable varF = new Variable("F", "absent", "present");
        Variable varG = new Variable("G", "absent", "present");
        
        // Nodes
        Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
        Node nodeB = probNet.addNode(varB, NodeType.CHANCE);
        Node nodeC = probNet.addNode(varC, NodeType.CHANCE);
        Node nodeE = probNet.addNode(varE, NodeType.CHANCE);
        Node nodeF = probNet.addNode(varF, NodeType.CHANCE);
        Node nodeG = probNet.addNode(varG, NodeType.CHANCE);
        
        // Links
        probNet.makeLinksExplicit(false);
        probNet.addLink(nodeA, nodeE, true);
        probNet.addLink(nodeB, nodeE, true);
        probNet.addLink(nodeC, nodeE, true);
        probNet.addLink(nodeF, nodeE, true);
        probNet.addLink(nodeG, nodeE, true);
        
        // Potentials
        UniformPotential potA = new UniformPotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeA.setPotential(potA);
        
        UniformPotential potB = new UniformPotential(Arrays.asList(varB), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeB.setPotential(potB);
        
        UniformPotential potC = new UniformPotential(Arrays.asList(varC), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeC.setPotential(potC);
        
        TablePotential potE = new TablePotential(Arrays.asList(varE, varA, varB, varC, varF, varG),
                                                 PotentialRole.CONDITIONAL_PROBABILITY);
        potE.setValues(new double[]{0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25, 0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25, 0, 0, 0, 0,
                0.25, 0.25, 0.25, 0.25, 0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25, 0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25, 0, 0, 0,
                0, 0.25, 0.25, 0.25, 0.25, 0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25, 0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25, 0, 0,
                0, 0, 0.25, 0.25, 0.25, 0.25, 0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25, 0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25, 0,
                0, 0, 0, 0.25, 0.25, 0.25, 0.25, 0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25, 0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25,
                0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25, 0, 0, 0, 0, 0.25, 0.25, 0.25, 0.25});
        nodeE.setPotential(potE);
        
        UniformPotential potF = new UniformPotential(Arrays.asList(varF), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeF.setPotential(potF);
        
        UniformPotential potG = new UniformPotential(Arrays.asList(varG), PotentialRole.CONDITIONAL_PROBABILITY);
        nodeG.setPotential(potG);
        
        // Link restrictions and revealing states
        Link link_nodeA_nodeE = probNet.getLink(nodeA, nodeE, true);
        link_nodeA_nodeE.initializesRestrictionsPotential();
        TablePotential restrictions_nodeA_nodeE = (TablePotential) link_nodeA_nodeE.getRestrictionsPotential();
        restrictions_nodeA_nodeE.setValues(new double[]{0, 1, 0, 1, 0, 1, 0, 1});
        
        // Always observed nodes
        
        return probNet;
    }
    
    @BeforeEach public void setUp() {
        
        stateA = new State[]{new State("A1"), new State("A2"), new State("A3")};
        stateB = new State[]{new State("B1"), new State("B2")};
        stateC = new State[]{new State("C1"), new State("C2")};
        varA = new Variable("A", stateA);
        varB = new Variable("B", stateB);
        varC = new Variable("C", stateC);
        ArrayList<Variable> variables = new ArrayList<>();
        
        variables.add(varB);
        variables.add(varA);
        variables.add(varC);
        net = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
        net.addNode(varA, NodeType.CHANCE);
        net.addNode(varB, NodeType.CHANCE);
        net.addNode(varC, NodeType.CHANCE);
        Node node = new Node(net, varA, NodeType.CHANCE);
        net.addNode(node);
        Node nodeB = new Node(net, varB, NodeType.CHANCE);
        net.addNode(nodeB);
        
        node = new Node(net, varC, NodeType.CHANCE);
        net.addNode(node);
        net.makeLinksExplicit(true);
        net.addLink(varA, varB, true);
        net.addLink(varC, varB, true);
        TablePotential potential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        nodeB.addPotential(potential);
        net.addPotential(potential);
        List<Link<Node>> links = net.getLinks();
        for (Link<Node> link : links) {
            if (link.getFrom().getVariable().equals(varA)) {
                this.link = link;
            }
            if (link.getFrom().getVariable().equals(varC)) {
                this.link2 = link;
            }
            
        }
        link.initializesRestrictionsPotential();
    }
    
    @Test public void testHasLinkRestriction() {
        Assertions.assertTrue(LinkRestrictionPotentialOperations.hasLinkRestriction(this.net.getNode("B")));
        Assertions.assertFalse(LinkRestrictionPotentialOperations.hasLinkRestriction(this.net.getNode("A")));
        
    }
    
    @Test public void testGetParentLinksWithRestriction() {
        Assertions.assertEquals(1,
                                LinkRestrictionPotentialOperations.getParentLinksWithRestriction(net.getNode("B"))
                                                                  .size());
        Assertions.assertEquals(0,
                                LinkRestrictionPotentialOperations.getParentLinksWithRestriction(net.getNode("A"))
                                                                  .size());
    }
    
    @Test public void testUpdatePotentialByAddLinkRestriction() {
        
        TablePotential probabilityPotential = (TablePotential) LinkRestrictionPotentialOperations
                .updatePotentialByAddLinkRestriction(net.getNode("B"), (TablePotential) link.getRestrictionsPotential(),
                                                     0, 0);
        int[] statesIndices = new int[]{0, 0, 0};
        Assertions.assertEquals(0, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        statesIndices = new int[]{0, 0, 1};
        Assertions.assertEquals(0, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        
        statesIndices = new int[]{1, 0, 0};
        Assertions.assertEquals(1, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        
        statesIndices = new int[]{0, 1, 0};
        Assertions.assertEquals(0.5, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        
        statesIndices = new int[]{0, 2, 0};
        Assertions.assertEquals(0.5, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        
        probabilityPotential = (TablePotential) LinkRestrictionPotentialOperations
                .updatePotentialByAddLinkRestriction(net.getNode("B"), (TablePotential) link.getRestrictionsPotential(),
                                                     1, 1);
        
        statesIndices = new int[]{1, 1, 0};
        Assertions.assertEquals(0, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        statesIndices = new int[]{1, 1, 1};
        Assertions.assertEquals(0, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        
    }
    
    @Test public void testUpdatePotentialByLinkRestrictions() {
        link.setCompatibilityValue(stateA[0], stateB[0], 0);
        link2.initializesRestrictionsPotential();
        link2.setCompatibilityValue(stateC[0], stateB[0], 0);
        
        TablePotential probabilityPotential = (TablePotential) LinkRestrictionPotentialOperations
                .updatePotentialByLinkRestrictions(net.getNode("B"));
        int[] statesIndices = new int[]{0, 0, 0};
        Assertions.assertEquals(0, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        
        statesIndices = new int[]{0, 0, 1};
        Assertions.assertEquals(0, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        
        statesIndices = new int[]{0, 1, 0};
        Assertions.assertEquals(0, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        
        statesIndices = new int[]{0, 2, 0};
        Assertions.assertEquals(0, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        
        statesIndices = new int[]{1, 0, 0};
        Assertions.assertEquals(1.0, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        
        statesIndices = new int[]{1, 0, 1};
        Assertions.assertEquals(1.0, probabilityPotential.getValue(probabilityPotential.getVariables(), statesIndices));
        
    }
    
    @Test public void testGetStateCombinationsWithLinkRestriction() {
        link.setCompatibilityValue(stateA[0], stateB[0], 0);
        link2.initializesRestrictionsPotential();
        link2.setCompatibilityValue(stateC[0], stateB[0], 0);
        List<int[]> states = LinkRestrictionPotentialOperations
                .getStateCombinationsWithLinkRestriction(net.getNode("B"));
        Assertions.assertEquals(5, states.size());
    }
    
    @Test public void testGetStateCombinationsWithLinkRestrictionBig() {
        ProbNet probNet = buildDAN_error_res_5_parents_pgmx();
        
        List<int[]> states = LinkRestrictionPotentialOperations
                .getStateCombinationsWithLinkRestriction(probNet.getNode("E"));
        Assertions.assertEquals(64, states.size());
    }
    
}
