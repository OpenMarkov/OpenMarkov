/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.constraint.*;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ProbNetTest {
    
    private ProbNet emptyProbNet;

    /** ProbNet for test: Two chance nodes A --> B, one decision D, B --> D;
     * one utility U, A --> U, D --> U. */
    private ProbNet simpleProbNet;

    private Variable A;
    
    private Variable B;
    
    private Variable D;

    private ArrayList<Variable> abVariables;

    private TablePotential pA;
    
    private TablePotential pBA;
    
    private ExactDistrPotential pU;
    
    private EvidenceCase simpleEvidence;

    // private Finding eB;

    /** Compares to probNets: number of nodes, variables, links and potentials.
     * @param probNet1 <code>ProbNet</code>
     * @param probNet2 <code>ProbNet</code> */
    public static void compareNetworks(ProbNet probNet1, ProbNet probNet2) {
        // Compare network type restrictions
        assertEquals(probNet1.getNetworkType(), probNet2.getNetworkType());
        // Compare constraints
        Set<Class<?>> constraintTypes1 = probNet1.getConstraints().stream()
                .map(Object::getClass)
                .collect(Collectors.toSet());

        for (PNConstraint constraint2 : probNet2.getConstraints()) {
            assertTrue(constraintTypes1.contains(constraint2.getClass()));
        }
        // Compare variables
        // Number of nodes of each node type
        for (NodeType nodeType : NodeType.values()) {
            assertEquals(probNet1.getNumNodes(nodeType), probNet2.getNumNodes(nodeType));
        }
        // Variables
        List<Variable> variables1 = probNet1.getVariables();
        for (Variable variable1 : variables1) {
            String variableName1 = variable1.getName();
            Node node2 = probNet2.getNode(variableName1);
            Node node1 = probNet1.getNode(variable1);
            // Checks that node1 and node2 has the same number of
            // children, siblings and parents.
            assertEquals(node1.getNumChildren(), node2.getNumChildren());
            assertEquals(node1.getNumParents(), node2.getNumParents());
            assertEquals(node1.getNumSiblings(), node2.getNumSiblings());
            // Checks the variable name
            Variable variable2 = node2.getVariable();
            assertTrue(variableName1.contentEquals(variable2.getName()));
            assertEquals(node1.isAlwaysObserved(), node2.isAlwaysObserved());
            // Check that the states are the same
            int numStates1 = variable1.getNumStates();
            assertEquals(numStates1, variable2.getNumStates());
            for (int i = 0; i < numStates1; i++) {
                String nameStateVariable1 = variable1.getStateName(i);
                assertTrue(nameStateVariable1.contentEquals(variable2.getState(nameStateVariable1).getName()));
            }
            
            // checks the links
            assertEquals(node1.getLinks().size(), node2.getLinks().size());
            for (Link<Node> link1 : node1.getLinks()) {
                Node linkNode1 = probNet2.getNode(link1.getFrom().getVariable().getName());
                Node linkNode2 = probNet2.getNode(link1.getTo().getVariable().getName());
                Link<Node> link2 = probNet2.getLink(linkNode1, linkNode2, link1.isDirected());
                assertEquals(link1.getFrom().getVariable().getName(), link2.getFrom().getVariable().getName());
                assertEquals(link1.getTo().getVariable().getName(), link2.getTo().getVariable().getName());
                assertEquals(link1.hasRestrictions(), link2.hasRestrictions());
                assertEquals(link1.hasRevealingConditions(), link2.hasRevealingConditions());
            }
            
            // Check that the potentials are the same
            List<Potential> potentials1 = node1.getPotentials();
            List<Potential> potentials2 = node2.getPotentials();
            int numPotentials1 = potentials1.size();
            assertEquals(numPotentials1, potentials2.size());
            // Until now (24-11-2011) a node has 0 or 1 potentials
            if (numPotentials1 == 1) {
                assertTrue(PotentialTest.equalPotentials(potentials1.getFirst(), potentials2.getFirst()));
            }
        }
    }
    
    @BeforeEach public void setUp() throws org.openmarkov.core.exception.IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        emptyProbNet = new ProbNet();
        
        // create simpleProbNet
        // create variables
        A = new Variable("A", 2);
        B = new Variable("B", 2);
        D = new Variable("D", 2);
        Variable u = new Variable("U");
        u.setDecisionCriterion(new Criterion());
        // create Arrays of variables used in potentials
        ArrayList<Variable> aVariables = new ArrayList<>(1);
        aVariables.add(A);
        abVariables = new ArrayList<>(2);
        abVariables.add(B);
        abVariables.add(A);
        ArrayList<Variable> adVariables = new ArrayList<>(2);
        adVariables.add(A);
        adVariables.add(D);
        // create potentials
        pA = new TablePotential(aVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        pA.getValues()[0] = 0.9;
        pA.getValues()[1] = 0.1;
        pBA = new TablePotential(abVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        pBA.getValues()[0] = 0.2;
        pBA.getValues()[1] = 0.8;
        pBA.getValues()[2] = 0.9;
        pBA.getValues()[3] = 0.1;
        pU = new ExactDistrPotential(Arrays.asList(u, A, D), PotentialRole.CONDITIONAL_PROBABILITY);
        //		pU.setUtilityVariable(U);
        double[] utilityValues = new double[4];
        utilityValues[0] = 1;
        utilityValues[1] = 2;
        utilityValues[2] = 3;
        utilityValues[3] = 4;
        pU.setValues(utilityValues);
        simpleProbNet = new ProbNet();
        simpleProbNet.addConstraint(new NoCycle());
        simpleProbNet.addConstraint(new OnlyDirectedLinks());
        // add potentials and variables
        simpleProbNet.addPotential(pA); // add variable and potential
        simpleProbNet.addNode(D, NodeType.DECISION);
        simpleProbNet.addNode(u, NodeType.UTILITY);
        simpleProbNet.addPotential(pU);
        simpleProbNet.addPotential(pBA);
        simpleProbNet.addLink(B, D, true);
        Finding eA = new Finding(A, 0);
        // eB = new Finding(B, 1);
        simpleEvidence = new EvidenceCase();
        simpleEvidence.addFinding(eA);
        
        //probNet peque
        //Variables
        String a = "A";
        String b = "B";
        String c = "C";
        
        //finite States variables
        Variable variableA = new Variable(a, 2);
        Variable variableB = new Variable(b, 2);
        Variable variableC = new Variable(c, 2);
        
        //additional properties
        String relevance = "Relevance";
        String value = "7.0";
        
        variableA.setAdditionalProperty(relevance, value);
        variableB.setAdditionalProperty(relevance, value);
        variableC.setAdditionalProperty(relevance, value);
        
        //Setting variable states
        State absent = new State("ausente");
        State present = new State("presente");
        State[] states = {absent, present};
        
        variableA.setStates(states);
        variableB.setStates(states);
        variableC.setStates(states);
        
        //Potentials
        //PotentialType type = PotentialType.TABLE;
        PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
        
        //Potential A
        double[] tableA = {0.2, 0.8};

        ArrayList<Variable> variablesA = new ArrayList<>();
        variablesA.add(variableA);

        TablePotential potentialvaluesA = new TablePotential(variablesA, role, tableA);
        
        //Potential BA
        double[] tableBA = {0.7, 0.3, 0.9, 0.1};

        ArrayList<Variable> variablesBA = new ArrayList<>();
        variablesBA.add(variableB);
        variablesBA.add(variableA);

        TablePotential potentialvaluesBA = new TablePotential(variablesBA, role, tableBA);
        
        //potencial CAB
        double[] tableCAB = {0.15, 0.29, 0.84, 0.98, 0.85, 0.71, 0.16, 0.02};

        ArrayList<Variable> variablesCAB = new ArrayList<>();
        variablesCAB.add(variableC);
        variablesCAB.add(variableA);
        variablesCAB.add(variableB);

        TablePotential potentialvaluesCAB = new TablePotential(variablesCAB, role, tableCAB);

        ProbNet peque = new ProbNet();
        
        NodeType nodeType = NodeType.CHANCE;
        
        peque.addNode(variableA, nodeType);
        peque.addNode(variableB, nodeType);
        peque.addNode(variableC, nodeType);
        
        //Links throws NodeNotFoundException
        peque.addLink(variableA, variableB, true);
        peque.addLink(variableA, variableC, true);
        peque.addLink(variableB, variableC, true);
        peque.addPotential(potentialvaluesA);
        peque.addPotential(potentialvaluesBA);
        peque.addPotential(potentialvaluesCAB);
        
        //ProbNet pruebaInferencia
        
        //Variables
        
        //finite States variables
        variableA = new Variable(a, 2);
        variableB = new Variable(b, 2);
        variableC = new Variable(c, 2);
        Variable variableD = new Variable("D", 2);
        Variable variableE = new Variable("E", 2);
        Variable variableF = new Variable("F", 2);
        Variable variableG = new Variable("G", 2);
        Variable variableH = new Variable("H", 2);
        Variable variableI = new Variable("I", 2);
        
        variableA.setAdditionalProperty(relevance, value);
        variableB.setAdditionalProperty(relevance, value);
        variableC.setAdditionalProperty(relevance, value);
        variableD.setAdditionalProperty(relevance, value);
        variableE.setAdditionalProperty(relevance, value);
        variableF.setAdditionalProperty(relevance, value);
        variableG.setAdditionalProperty(relevance, value);
        variableH.setAdditionalProperty(relevance, value);
        variableI.setAdditionalProperty(relevance, value);
        
        variableA.setStates(states);
        variableB.setStates(states);
        variableC.setStates(states);
        variableD.setStates(states);
        variableE.setStates(states);
        variableF.setStates(states);
        variableG.setStates(states);
        variableH.setStates(states);
        variableI.setStates(states);
        
        //Potentials
        //PotentialType type = PotentialType.TABLE;
        role = PotentialRole.CONDITIONAL_PROBABILITY;
        
        //Potential CA
        double[] tableCA = {0.81, 0.19, 0.98, 0.02};
        ArrayList<Variable> variablesCA = new ArrayList<>();
        variablesCA.add(variableC);
        variablesCA.add(variableA);

        TablePotential potentialvaluesCA = new TablePotential(variablesCA, role, tableCA);
        
        //Potential EBC
        double[] tableEBC = {0.02, 0.98, 0.68, 0.32, 0.24, 0.76, 0.79, 0.21};
        ArrayList<Variable> variablesEBC = new ArrayList<>();
        variablesEBC.add(variableE);
        variablesEBC.add(variableB);
        variablesEBC.add(variableC);

        TablePotential potentialvaluesEBC = new TablePotential(variablesEBC, role, tableEBC);
        
        //potentialFE
        double[] tableFE = {0.12, 0.88, 0.77, 0.23};
        ArrayList<Variable> variablesFE = new ArrayList<>();
        variablesFE.add(variableF);
        variablesFE.add(variableE);

        TablePotential potentialvaluesFE = new TablePotential(variablesFE, role, tableFE);
        
        //potentialGD
        double[] tableGD = {0.49, 0.51, 0.75, 0.25};
        ArrayList<Variable> variablesGD = new ArrayList<>();
        variablesGD.add(variableG);
        variablesGD.add(variableD);

        TablePotential potentialvaluesGD = new TablePotential(variablesGD, role, tableGD);
        
        //Potential I
        double[] tableI = {0.85, 0.15};

        ArrayList<Variable> variablesI = new ArrayList<>();
        variablesI.add(variableI);

        TablePotential potentialvaluesI = new TablePotential(variablesI, role, tableI);
        
        //Potential DBI
        double[] tableDBI = {0.22, 0.78, 0.86, 0.14, 0.57, 0.43, 0.9, 0.1};
        ArrayList<Variable> variablesDBI = new ArrayList<>();
        variablesDBI.add(variableD);
        variablesDBI.add(variableB);
        variablesDBI.add(variableI);

        TablePotential potentialvaluesDBI = new TablePotential(variablesDBI, role, tableDBI);
        
        //potentialBA
        double[] tableba = {0.77, 0.23, 0.26, 0.74};
        ArrayList<Variable> variablesba = new ArrayList<>();
        variablesba.add(variableB);
        variablesba.add(variableA);

        TablePotential potentialvaluesba = new TablePotential(variablesba, role, tableba);
        
        //potentialAH
        double[] tableAH = {0.09, 0.91, 0.83, 0.17};
        ArrayList<Variable> variablesAH = new ArrayList<>();
        variablesAH.add(variableA);
        variablesAH.add(variableH);

        TablePotential potentialvaluesAH = new TablePotential(variablesAH, role, tableAH);
        
        //Potential H
        double[] tableH = {0.68, 0.32};
        ArrayList<Variable> variablesH = new ArrayList<>();
        variablesH.add(variableH);

        TablePotential potentialvaluesH = new TablePotential(variablesH, role, tableH);

        ProbNet pruebaInferencia = new ProbNet();
        
        pruebaInferencia.addNode(variableA, nodeType);
        pruebaInferencia.addNode(variableB, nodeType);
        pruebaInferencia.addNode(variableC, nodeType);
        pruebaInferencia.addNode(variableD, nodeType);
        pruebaInferencia.addNode(variableE, nodeType);
        pruebaInferencia.addNode(variableF, nodeType);
        pruebaInferencia.addNode(variableG, nodeType);
        pruebaInferencia.addNode(variableH, nodeType);
        pruebaInferencia.addNode(variableI, nodeType);
        
        //Links throws NodeNotFoundException
        pruebaInferencia.addLink(variableA, variableB, true);
        pruebaInferencia.addLink(variableA, variableC, true);
        pruebaInferencia.addLink(variableB, variableD, true);
        
        pruebaInferencia.addLink(variableB, variableE, true);
        pruebaInferencia.addLink(variableC, variableE, true);
        pruebaInferencia.addLink(variableD, variableG, true);
        pruebaInferencia.addLink(variableE, variableF, true);
        pruebaInferencia.addLink(variableH, variableA, true);
        pruebaInferencia.addLink(variableI, variableD, true);
        
        pruebaInferencia.addPotential(potentialvaluesCA);
        pruebaInferencia.addPotential(potentialvaluesEBC);
        pruebaInferencia.addPotential(potentialvaluesFE);
        pruebaInferencia.addPotential(potentialvaluesGD);
        pruebaInferencia.addPotential(potentialvaluesI);
        pruebaInferencia.addPotential(potentialvaluesDBI);
        pruebaInferencia.addPotential(potentialvaluesba);
        pruebaInferencia.addPotential(potentialvaluesH);
        pruebaInferencia.addPotential(potentialvaluesAH);
        
    }
    
    @Test public void testProbNet() {
        // Test empty probabilistic network.
        // By default a ProbNet is a Bayesian Network
        int numBNConstraints = ConstraintManager.getUniqueInstance().
                                                buildConstraintList(BayesianNetworkType.getUniqueInstance()).
                                                size();
        assertEquals(numBNConstraints, emptyProbNet.getConstraints().size()); // No constraints
        for (NodeType nodeType : NodeType.values()) { // No nodes of every type
            assertEquals(0, emptyProbNet.getNumNodes(nodeType));
        }
    }
    
    @Test public void testAddConstraint() {
        // By default a ProbNet is a Bayesian Network
        int numBNConstraints = ConstraintManager.getUniqueInstance().
                                                buildConstraintList(BayesianNetworkType.getUniqueInstance()).
                                                size();
        emptyProbNet.addConstraint(new MaxNumParents(1));
        List<PNConstraint> constraints = emptyProbNet.getConstraints();
        assertEquals(numBNConstraints + 1, constraints.size());
    }
    
    @Test public void testRemoveConstraint() {
        PNConstraint constraint = new MaxNumParents(1);
        // By default a ProbNet is a Bayesian Network
        int numBNConstraints = ConstraintManager.getUniqueInstance().
                                                buildConstraintList(BayesianNetworkType.getUniqueInstance()).
                                                size();
        emptyProbNet.addConstraint(constraint);
        emptyProbNet.removeConstraint(constraint);
        List<PNConstraint> constraints = emptyProbNet.getConstraints();
        assertEquals(numBNConstraints, constraints.size());
    }
    
    @Test public void testGetNumPotentials() {
        assertEquals(3, simpleProbNet.getNumPotentials());
    }
    
    @Test public void testAddPotential() {
        ProbNet probNet = new ProbNet();
        // Add a potential and a variable
        probNet.addPotential(pA);
        Node nodeA = probNet.getNode(A);
        assertNotNull(nodeA);
        assertTrue(nodeA.getPotentials().contains(pA));
        // Add a potential with two variables. Both variables exists in probNet
        probNet.addNode(B, NodeType.CHANCE);
        probNet.addPotential(pBA);
        assertEquals(2, probNet.getNumPotentials());
        Node node = probNet.getNode(B);
        assertEquals(pBA, node.getPotentials().getFirst());
    }
    
    @Test public void testGetNodeString() {
        Node nodeA = simpleProbNet.getNode("A");
        assertNotNull(nodeA);
        Node nodeB = simpleProbNet.getNode("B");
        assertNotNull(nodeB);
        Node nodeD = simpleProbNet.getNode("D");
        assertNotNull(nodeD);
    }
    
    @Test public void testAddLink() {
        Node nodeA = simpleProbNet.getNode("A");
        Node nodeB = simpleProbNet.getNode("B");
        List<Node> AChildren = nodeA.getChildren();
        assertTrue(AChildren.contains(nodeB)); // test addLink
        List<Node> BParents = nodeB.getParents();
        assertTrue(BParents.contains(nodeA)); // test addLink
        assertEquals(1, BParents.size());// test that addLink adds only one link
    }
    
    @Test public void testGetNodes() {
        List<Node> nodes = simpleProbNet.getNodes();
        assertEquals(4, nodes.size());
    }
    
    @Test public void testGetVariables() {
        List<Variable> variables = simpleProbNet.getNonUtilityVariables();
        assertEquals(3, variables.size());
        assertTrue(variables.contains(A));
        assertTrue(variables.contains(B));
        assertTrue(variables.contains(D));
    }
    
    @Test public void testGetVariablesArrayListOfNode() {
        List<Node> nodes = simpleProbNet.getNodes();
        List<Variable> variables = ProbNet.getVariables(nodes);
        assertTrue(variables.contains(A));
        assertTrue(variables.contains(B));
        assertTrue(variables.contains(D));
    }
    
    @Test public void testGetNumNodes() {
        assertEquals(4, simpleProbNet.getNumNodes());
    }
    
    @Test public void testGetPotentials() {
        List<Potential> potentials = simpleProbNet.getPotentials();
        assertTrue(potentials.contains(pA));
        assertTrue(potentials.contains(pBA));
        assertTrue(potentials.contains(pU));
    }

    /* Just make sure that the class <code>ProbNet</code> returns the correct
     *  number of potentials with the correct variables. */
    @Test
    public void testGetProjectedPotentials()
            throws NonProjectablePotentialException {
        List<? extends Potential> projectedPotentials = simpleProbNet.tableProjectPotentials(simpleEvidence);
        assertEquals(3, projectedPotentials.size());
        boolean constantPotentialFound = false;
        boolean bPotentialFound = false;
        //		boolean utilityPotentialFound = false;
        for (Potential potential : projectedPotentials) {
            List<Variable> potentialVariables = potential.getVariables();
            constantPotentialFound = constantPotentialFound || potentialVariables.isEmpty();
            bPotentialFound = bPotentialFound || (
                    potentialVariables.contains(B) && potentialVariables.size() == 1
            );
            //			utilityPotentialFound = utilityPotentialFound ||
            //				(potential.getUtilityVariable() != null &&
            //						potential.getUtilityVariable().equals(U) &&
            //						potentialVariables.size() == 1
            //						&& potential.isUtility()
            //				&& potentialVariables.contains(D));
        }
        assertTrue(constantPotentialFound);
        assertTrue(bPotentialFound);
        //		assertTrue(utilityPotentialFound);
    }
    
    @Test public void testGetNodesArrayListOfVariable() {
        List<Variable> abVariables = new ArrayList<>();
        abVariables.addAll(this.abVariables);
        List<Node> chanceNodes = simpleProbNet.getNodes(abVariables);
        assertEquals(2, chanceNodes.size());
        Variable variable0 = chanceNodes.get(0).getVariable();
        Variable variable1 = chanceNodes.get(1).getVariable();
        if (variable0 == A) {
            assertEquals(variable1, B);
        } else {
            assertEquals(variable0, B);
            assertEquals(variable1, A);
        }
    }
    
    @Test public void testGetNumNodesNodeType() {
        assertEquals(2, simpleProbNet.getNumNodes(NodeType.CHANCE));
        assertEquals(1, simpleProbNet.getNumNodes(NodeType.DECISION));
        assertEquals(1, simpleProbNet.getNumNodes(NodeType.UTILITY));
    }
    
    @Test public void testGetNodesNodeType() {
        // test chance nodes
        List<Node> chanceNodes = simpleProbNet.getNodes(NodeType.CHANCE);
        assertEquals(2, chanceNodes.size());
        Variable variable0 = chanceNodes.get(0).getVariable();
        Variable variable1 = chanceNodes.get(1).getVariable();
        if (variable0 == A) {
            assertEquals(variable1, B);
        } else {
            assertEquals(variable0, B);
            assertEquals(variable1, A);
        }
        // test decision nodes
        List<Node> decisionNodes = simpleProbNet.getNodes(NodeType.DECISION);
        assertEquals(1, decisionNodes.size());
        assertEquals(D, decisionNodes.getFirst().getVariable());
        // test utility nodes
        List<Node> utilityNodes = simpleProbNet.getNodes(NodeType.UTILITY);
        assertEquals(1, utilityNodes.size());
        Node utilityNode = utilityNodes.getFirst();
        assertTrue(utilityNode.getPotentials().contains(pU));
    }
    
    @Test public void testGetPotentialsVariable() {
        List<Potential> potentials = simpleProbNet.getPotentials(A);
        assertEquals(3, potentials.size());
        assertTrue(potentials.contains(pA));
        assertTrue(potentials.contains(pBA));
        assertTrue(potentials.contains(pU));
    }
    
    @Test public void testGetPotentialsType() {
        // test chance potentials
        List<Potential> chancePotentials = simpleProbNet.getPotentialsByType(NodeType.CHANCE);
        assertEquals(2, chancePotentials.size());
        assertTrue(chancePotentials.contains(pA));
        assertTrue(chancePotentials.contains(pBA));
        
        // test utility potentials
        List<Potential> utilityPotentials = simpleProbNet.getPotentialsByType(NodeType.UTILITY);
        assertEquals(1, utilityPotentials.size());
        assertTrue(utilityPotentials.contains(pU));
    }
    
    @Test public void testGetUtilityPotentials() {
        // test 0 potentials
        List<Potential> BUtilityPotentials = simpleProbNet.getUtilityPotentials(B);
        assertEquals(0, BUtilityPotentials.size());
        
        // test 1 potential
        List<Potential> AUtilityPotentials = simpleProbNet.getUtilityPotentials(A);
        assertEquals(1, AUtilityPotentials.size());
        assertEquals(pU, AUtilityPotentials.getFirst());
        AUtilityPotentials = simpleProbNet.getUtilityPotentials(D);
        assertEquals(1, AUtilityPotentials.size());
        assertEquals(pU, AUtilityPotentials.getFirst());
    }
    
    @Test public void testGetPotentials2() {
        List<Potential> APotentials = simpleProbNet.getPotentials(A);
        assertEquals(3, APotentials.size());
        assertTrue(APotentials.contains(pA));
        assertTrue(APotentials.contains(pBA));
        assertTrue(APotentials.contains(pU));
        List<Potential> DPotentials = simpleProbNet.getPotentials(D);
        assertEquals(1, DPotentials.size());
        assertTrue(DPotentials.contains(pU));
        List<Potential> BPotentials = simpleProbNet.getPotentials(B);
        assertEquals(1, BPotentials.size());
        assertTrue(BPotentials.contains(pBA));
    }
    
    @Test public void testRemovePotential() {
        simpleProbNet.removePotential(pBA);
        assertEquals(0, simpleProbNet.getPotentials(B).size());
        assertEquals(2, simpleProbNet.getNumPotentials());
        simpleProbNet.removePotential(pA);
        assertEquals(1, simpleProbNet.getPotentials(A).size());
        assertEquals(1, simpleProbNet.getNumPotentials());
        simpleProbNet.removePotential(pU);
        assertEquals(0, simpleProbNet.getPotentials(D).size());
        assertEquals(0, simpleProbNet.getNumPotentials());
    }
    
    @Test public void testRemovePotentialsNode() {
        Node nodeA = simpleProbNet.getNode(A);
        simpleProbNet.removePotentials(nodeA);
        assertEquals(2, simpleProbNet.getNumPotentials());
        Node nodeB = simpleProbNet.getNode(B);
        simpleProbNet.removePotentials(nodeB);
        assertEquals(1, simpleProbNet.getNumPotentials());
    }
    
    @Test public void testGetNode() {
        Node nodeD = simpleProbNet.getNode("D", NodeType.DECISION);
        assertNotNull(nodeD);
        {
            Node nodeB = simpleProbNet.getNode("B", NodeType.DECISION);
            assertNull(nodeB);
        }
        
        Node nodeB = simpleProbNet.getNode("B", NodeType.CHANCE);
        assertNotNull(nodeB);
    }
    
    @Test public void testGetVariablesNodeType() {
        // test utility variables
        List<Variable> utilityVariables = simpleProbNet.getVariables(NodeType.UTILITY);
        assertEquals(1, utilityVariables.size());
        Node utilityNode = simpleProbNet.getNode(utilityVariables.get(0));
        
        // test chance variables
        List<Variable> chanceVariables = simpleProbNet.getVariables(NodeType.CHANCE);
        assertEquals(2, chanceVariables.size());
        assertTrue(chanceVariables.contains(A));
        assertTrue(chanceVariables.contains(B));
        
        // test decision variables
        List<Variable> decisionVariables = simpleProbNet.getVariables(NodeType.DECISION);
        assertEquals(1, decisionVariables.size());
        assertTrue(decisionVariables.contains(D));
    }
    
    @Test public void testRemoveNode() {
        Node nodeB = simpleProbNet.getNode(B);
        simpleProbNet.removeNode(nodeB);
        Node nodeA = simpleProbNet.getNode(A);
        assertEquals(1, nodeA.getNumChildren());
        Node nodeD = simpleProbNet.getNode(D);
        assertEquals(0, nodeD.getNumParents());
    }
    
    @Test public void testRemoveLink() {
        // No remove because link is directed
        simpleProbNet.removeLink(A, B, false); // It directed would be true
        Node nodeA = simpleProbNet.getNode(A);
        Node nodeB = simpleProbNet.getNode(B);
        assertTrue(nodeA.getChildren().contains(nodeB));
        // Remove a link
        simpleProbNet.removeLink(A, B, true);
        assertFalse(nodeA.getChildren().contains(nodeB));
    }
    
    @Test public void testCopy() {
        ProbNet copied = simpleProbNet.copy();
        List<Variable> copiedVariables = copied.getNonUtilityVariables();
        // test variables
        assertEquals(3, copiedVariables.size());
        assertTrue(copiedVariables.contains(A));
        assertTrue(copiedVariables.contains(B));
        assertTrue(copiedVariables.contains(D));
        List<Variable> utilityVariables = copied.getVariables(NodeType.UTILITY);
        assertEquals(1, utilityVariables.size());
        // test potentials
        assertEquals(3, copied.getNumPotentials());
        Node nodeA = copied.getNode(A);
        assertTrue(nodeA.getPotentials().contains(pA));
        Node nodeB = copied.getNode(B);
        assertTrue(nodeB.getPotentials().contains(pBA));
        Node nodeD = copied.getNode(D);
        Node nodeU = nodeD.getChildren().getFirst();
        assertTrue(nodeU.getPotentials().contains(pU));
        // test graph structure
        assertTrue(nodeA.getChildren().contains(nodeB));
        assertTrue(nodeA.getChildren().contains(nodeU));
        assertEquals(2, nodeA.getNeighbors().size());
        assertTrue(nodeB.getParents().contains(nodeA));
        assertTrue(nodeB.getChildren().contains(nodeD));
        assertEquals(2, nodeB.getNeighbors().size());
        assertTrue(nodeD.getParents().contains(nodeB));
        assertTrue(nodeD.getChildren().contains(nodeU));
        assertEquals(2, nodeD.getNeighbors().size());
        assertTrue(nodeU.getParents().contains(nodeA));
        assertTrue(nodeU.getParents().contains(nodeD));
        assertEquals(2, nodeU.getNeighbors().size());
    }
    
    // TODO Sobrecargar método equals.
    
    @Test public void testGetAdditionalConstraints() {
        ProbNet bnProbNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        PNConstraint maxNumParents = new MaxNumParents(1);
        bnProbNet.addConstraint(maxNumParents);
        List<PNConstraint> additionalConstraints = bnProbNet.getAdditionalConstraints();
        assertEquals(1, additionalConstraints.size());
        assertTrue(additionalConstraints.contains(maxNumParents));
    }
    
}
