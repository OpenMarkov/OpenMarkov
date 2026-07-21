/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.DynamicBayesianNetwork;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;

/**
 * Test constraints applied to networks.
 * Supplies methods to create different types of <code>ProbNet</code>
 */
public class ConstraintsTests {
    
    /**
     * Creates a simple <code>probNet</code> for test purposes: A -> B -- C.
     *
     * @return <code>ProbNet</code>
     */
    public static ProbNet getTestProbNetMixed() {
        ProbNet probNet = new ProbNet();
        Variable va = new Variable("A", 2);
        Variable vb = new Variable("B", 2);
        Variable vc = new Variable("C", 2);
        probNet.addNode(va, NodeType.CHANCE);
        probNet.addNode(vb, NodeType.DECISION);
        probNet.addNode(vc, NodeType.CHANCE);
        probNet.addLink(va, vb, true);
        probNet.addLink(vb, vc, false);
        return probNet;
    }
    
    /**
     * Creates a simple <code>probNet</code> for test purposes: A -- B -- C.
     *
     * @return <code>ProbNet</code>
     */
    public static ProbNet getTestProbNetUndirected() {
        ProbNet probNet = new ProbNet();
        Variable va = new Variable("A", 2);
        Variable vb = new Variable("B", 2);
        Variable vc = new Variable("C", 2);
        probNet.addNode(va, NodeType.CHANCE);
        probNet.addNode(vb, NodeType.CHANCE);
        probNet.addNode(vc, NodeType.CHANCE);
        probNet.addLink(va, vb, false);
        probNet.addLink(vb, vc, false);
        
        return probNet;
    }
    
    /**
     * Creates a simple <code>probNet</code> for test purposes: A -> B -> C.
     *
     * @return <code>ProbNet</code>
     */
    public static ProbNet getTestProbNetDirected() {
        ProbNet probNet = new ProbNet();
        Variable va = new Variable("A", 2);
        Variable vb = new Variable("B", 2);
        Variable vc = new Variable("C", 2);
        probNet.addNode(va, NodeType.CHANCE);
        probNet.addNode(vb, NodeType.CHANCE);
        probNet.addNode(vc, NodeType.CHANCE);
        probNet.addLink(va, vb, true);
        probNet.addLink(vb, vc, true);
        return probNet;
    }
    
    /**
     * Creates a simple influence diagram (<code>probNet</code>) for test
     * purposes: A->D, U(A,D).
     *
     * @return <code>ProbNet</code>
     */
    public static ProbNet getInfuenceDiagram() {
        ProbNet influenceDiagram = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable vA = new Variable("A", 2);
        Variable vD = new Variable("D", 2);
        Variable vU = new Variable("U");
        ArrayList<Variable> variables = new ArrayList<>();
        variables.add(vU);
        variables.add(vA);
        variables.add(vD);
        influenceDiagram.addNode(vA, NodeType.CHANCE);
        influenceDiagram.addNode(vD, NodeType.DECISION);
        influenceDiagram.addNode(vU, NodeType.UTILITY);
        ExactDistrPotential utilityPotential = new ExactDistrPotential(variables,
                                                                       PotentialRole.CONDITIONAL_PROBABILITY);
        //			utilityPotential.setUtilityVariable(vU);
        influenceDiagram.addPotential(utilityPotential);
        influenceDiagram.addLink(vA, vD, true);
        influenceDiagram.makeLinksExplicit(true);
        return influenceDiagram;
    }
    
    /**
     * Creates a influence diagram (<code>probNet</code>) for test
     * purposes with not only utility Children: A->D, U->C(decision), U(A,D).
     *
     * @return <code>ProbNet</code>
     */
    public static ProbNet getNotOnlyUtilityChildrenInfluenceDiagram() {
        ProbNet influenceDiagram = getInfuenceDiagram();
        Variable vC = new Variable("C", 2);
        Variable vU = influenceDiagram.getVariable("U");
        influenceDiagram.addNode(vC, NodeType.DECISION);
        influenceDiagram.addLink(vU, vC, true);
        return influenceDiagram;
    }
    
    /**
     * Creates a influence diagram (<code>probNet</code>) for test
     * purposes with only Utility children: A->D, U->C(utility), U(A,D).
     *
     * @return <code>ProbNet</code>
     */
    public static ProbNet getOnlyUtilityChildrenInfluenceDiagram() {
        ProbNet influenceDiagram = getInfuenceDiagram();
        Variable vC = new Variable("C", 2);
        Variable vU = influenceDiagram.getVariable("U");
        influenceDiagram.addNode(vC, NodeType.UTILITY);
        influenceDiagram.addLink(vU, vC, true);
        return influenceDiagram;
    }
    
    /**
     * Creates a simple influence diagram (<code>probNet</code>) for test
     * purposes: A->D, U(A,D).
     *
     * @return <code>ProbNet</code>
     */
    public static ProbNet getNumericInfluenceDiagram() {
        ProbNet influenceDiagram = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable vA = new Variable("A");
        Variable vD = new Variable("D");
        Variable vU = new Variable("U");
        ArrayList<Variable> variables = new ArrayList<>();
        variables.add(vA);
        variables.add(vD);
        influenceDiagram.addNode(vA, NodeType.CHANCE);
        influenceDiagram.addNode(vD, NodeType.DECISION);
        influenceDiagram.addNode(vU, NodeType.UTILITY);
        TablePotential utilityPotential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        //			utilityPotential.setUtilityVariable(vU);
        influenceDiagram.addPotential(utilityPotential);
        influenceDiagram.addLink(vA, vD, true);
        influenceDiagram.makeLinksExplicit(true);
        return influenceDiagram;
        
    }
    
    /**
     * Creates a simple probabilistic net (<code>probNet</code>) for test
     * purposes with temporal variables: A->D, U(A,D).
     *
     * @return <code>ProbNet</code>
     */
    public static ProbNet getTemporalVarNet() {
        ProbNet influenceDiagram = new ProbNet(DynamicBayesianNetwork.getUniqueInstance());
        Variable vA = new Variable(" [12]", "YES", "NO");
        Variable vD = new Variable(" [14]", "YES", "NO");
        Variable vU = new Variable(" [15]", "YES", "NO");
        ArrayList<Variable> variables = new ArrayList<>();
        variables.add(vA);
        variables.add(vD);
        influenceDiagram.addNode(vA, NodeType.CHANCE);
        influenceDiagram.addNode(vD, NodeType.DECISION);
        influenceDiagram.addNode(vU, NodeType.UTILITY);
        TablePotential utilityPotential = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        //			utilityPotential.setUtilityVariable(vU);
        influenceDiagram.addPotential(utilityPotential);
        influenceDiagram.addLink(vA, vD, true);
        influenceDiagram.makeLinksExplicit(true);
        return influenceDiagram;
        
    }
    
}
