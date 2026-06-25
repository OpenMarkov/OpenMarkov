/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.treeADD;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.graph.LabelledLink;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.MIDType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TreeADDPotentialTest {
    
    private ProbNet probNet;
    
    private PNConstraint networkConstraint;
    
    private Variable variableA;
    
    private Variable variableB;
    
    private State absent;
    
    private State present;
    
    private PotentialRole role;
    
    private NodeType nodeType;
    
    private TablePotential potentialvaluesA;
    
    private TablePotential potentialBA;
    
    private TablePotential potentialBA0;
    
    private TablePotential potentialBA1;
    
    private TreeADDPotential treeADD;
    
    private List<Variable> listA;
    
    private List<Variable> listB;
    
    private List<Variable> listBA;
    
    private List<Variable> variablesBA;
    
    private List<Potential> leaves;
    
    private Variable startVariable;
    
    private Node startNode;
    
    private TreeADDBranch branchData0;
    
    private TreeADDBranch branchData1;
    
    private Node branchNode0;
    
    private Node branchNode1;
    
    private LabelledLink<Node> labelledlink0;
    
    private LabelledLink<Node> labelledlink1;
    
    @BeforeEach public void setUp() {
        
        // create variables
        variableA = new Variable("A", 2);
        variableB = new Variable("B", 2);
        
        // set variable states
        absent = new State("absent");
        present = new State("present");
        State[] states = {absent, present};
        
        variableA.setStates(states);
        variableB.setStates(states);
        
        // create table potential P(a)
        listA = new ArrayList<>(1);
        double[] tableA = {0.9, 0.1};
        listA.add(variableA);
        role = PotentialRole.CONDITIONAL_PROBABILITY;
        potentialvaluesA = new TablePotential(listA, role, tableA);
        
        // create subpotentials (leaves of the treeADD)
        listB = new ArrayList<>(1);
        double[] tableBA0 = {1.0, 0.0};
        double[] tableBA1 = {0.9, 0.1};
        listB.add(variableB);
        potentialBA0 = new TablePotential(listB, role, tableBA0);// leaf potential
        potentialBA1 = new TablePotential(listB, role, tableBA1);// leaf potential
        
        // create treeADD potential P(b|a)
        listBA = new ArrayList<>(2);
        listBA.add(variableB);
        listBA.add(variableA);
        
        //create branches
        startVariable = variableA;
        List<State> absentState = new ArrayList<>();
        List<State> presentState = new ArrayList<>();
        absentState.add(absent);
        presentState.add(present);
        branchData0 = new TreeADDBranch(absentState, startVariable, potentialBA0, listBA);
        branchData1 = new TreeADDBranch(presentState, startVariable, potentialBA1, listBA);
        
        // Append the new 'states' branch to the tree
        labelledlink0 = new LabelledLink<>(startNode, branchNode0, true, branchData0);
        labelledlink1 = new LabelledLink<>(startNode, branchNode1, true, branchData1);
        
        // create treeADD
        treeADD = new TreeADDPotential(listBA, startVariable, PotentialRole.CONDITIONAL_PROBABILITY);
        
        //MID Markov influence diagram
        probNet = new ProbNet(MIDType.getUniqueInstance());
        
        nodeType = NodeType.CHANCE;
        
        probNet.addNode(variableA, nodeType);
        probNet.addNode(variableB, nodeType);
        probNet.addLink(variableA, variableB, true);
        probNet.addPotential(potentialvaluesA);
        probNet.addPotential(treeADD);
        
        List<Potential> potentials = probNet.getPotentials();
        for (Potential potential : potentials) {
            if (potential instanceof TreeADDPotential) {
                this.treeADD = (TreeADDPotential) potential;
                variableB = potential.getVariable(0);
                variableA = potential.getVariable(1);
            }
        }
    }
	
	/*@Test
	public void testTableProject() 
	throws NonProjectablePotentialException, 
			WrongCriterionException, InvalidStateException, 
			IncompatibleEvidenceException {
								
		//InferenceOptions options = new InferenceOptions(probNet, variableB);
		TablePotential tablePotential = 
				treeADD.tableProject(null, null).get(0);
		ArrayList<Variable> variables = tablePotential.getVariables();
		assertEquals(2, variables.size());
		assertEquals(1.0, tablePotential.getValues()[0]);
		assertEquals(0.0, tablePotential.getValues()[1]);
		assertEquals(0.9, tablePotential.getValues()[2]);
		assertEquals(0.1, tablePotential.getValues()[3]);
		
		Finding bFinding = new Finding(variableB, 0);
		EvidenceCase evidence = new EvidenceCase();
		evidence.addFinding(bFinding);
		tablePotential = 
			treeADD.tableProject(evidence, null).get(0);
		variables = tablePotential.getVariables();
		assertEquals(1, variables.size());
		assertEquals(1.0, tablePotential.getValues()[0]);
		assertEquals(0.9, tablePotential.getValues()[1]);

		Finding aFinding = new Finding(variableA, 1);
		evidence = new EvidenceCase();
		evidence.addFinding(aFinding);
		tablePotential = 
			treeADD.tableProject(evidence, null).get(0);
		variables = tablePotential.getVariables();
		assertEquals(1, variables.size());
		assertEquals(0.9, tablePotential.getValues()[0]);
		assertEquals(0.1, tablePotential.getValues()[1]);
	}*/
    
    @Test public void testShift() {
        // TODO
    }

    @Test public void testGetInducedFindings() {
        // TODO
    }

    /**
     * Issue #215: reordering states of the top variable should reorder the
     * branches, not expand the tree to a flat table.
     */
    @Test
    public void testReorderTopVariablePreservesTreeStructure() {
        // Build a TreeADD with explicit TablePotential leaves
        State s0 = new State("absent");
        State s1 = new State("present");
        State[] statesAB = {s0, s1};

        Variable varA = new Variable("X", statesAB);
        Variable varB = new Variable("Y", statesAB);
        List<Variable> varsBA = List.of(varB, varA);

        // Leaf potentials for each branch of A
        TablePotential leafAbsent = new TablePotential(List.of(varB), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{1.0, 0.0});  // P(Y|X=absent)
        TablePotential leafPresent = new TablePotential(List.of(varB), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1});  // P(Y|X=present)

        TreeADDBranch branchAbsent = new TreeADDBranch(List.of(s0), varA, leafAbsent, varsBA);
        TreeADDBranch branchPresent = new TreeADDBranch(List.of(s1), varA, leafPresent, varsBA);

        TreeADDPotential tree = new TreeADDPotential(varsBA, varA, PotentialRole.CONDITIONAL_PROBABILITY,
                List.of(branchAbsent, branchPresent));

        // Reorder A's states to [present, absent]
        State[] newOrder = {s1, s0};
        Potential reordered = tree.reorder(varA, newOrder);

        // Result should still be a TreeADDPotential
        assertTrue(reordered instanceof TreeADDPotential,
                "Reordered potential should remain a TreeADDPotential");

        TreeADDPotential reorderedTree = (TreeADDPotential) reordered;
        assertEquals(2, reorderedTree.getBranches().size());

        // First branch should now be 'present', second should be 'absent'
        TreeADDBranch first = reorderedTree.getBranches().get(0);
        TreeADDBranch second = reorderedTree.getBranches().get(1);

        assertTrue(first.getStates().contains(s1), "First branch should be 'present'");
        assertTrue(second.getStates().contains(s0), "Second branch should be 'absent'");

        // Leaf potentials should follow their branches
        TablePotential firstLeaf = (TablePotential) first.getPotential();
        assertEquals(0.9, firstLeaf.getValues()[0], 1e-9, "present branch: P(Y=absent|X=present)=0.9");
        assertEquals(0.1, firstLeaf.getValues()[1], 1e-9, "present branch: P(Y=present|X=present)=0.1");

        TablePotential secondLeaf = (TablePotential) second.getPotential();
        assertEquals(1.0, secondLeaf.getValues()[0], 1e-9, "absent branch: P(Y=absent|X=absent)=1.0");
        assertEquals(0.0, secondLeaf.getValues()[1], 1e-9, "absent branch: P(Y=present|X=absent)=0.0");
    }

    /**
     * Reordering a variable that is NOT the top variable should propagate
     * into leaf potentials without changing the branch order.
     */
    @Test
    public void testReorderNonTopVariablePropagatesToLeaves() {
        State s0 = new State("absent");
        State s1 = new State("present");
        State[] statesAB = {s0, s1};

        Variable varA = new Variable("X", statesAB);
        Variable varB = new Variable("Y", statesAB);
        List<Variable> varsBA = List.of(varB, varA);

        TablePotential leafAbsent = new TablePotential(List.of(varB), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{1.0, 0.0});
        TablePotential leafPresent = new TablePotential(List.of(varB), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.9, 0.1});

        TreeADDBranch branchAbsent = new TreeADDBranch(List.of(s0), varA, leafAbsent, varsBA);
        TreeADDBranch branchPresent = new TreeADDBranch(List.of(s1), varA, leafPresent, varsBA);

        TreeADDPotential tree = new TreeADDPotential(varsBA, varA, PotentialRole.CONDITIONAL_PROBABILITY,
                List.of(branchAbsent, branchPresent));

        // Reorder Y's states to [present, absent]
        State[] newOrder = {s1, s0};
        Potential reordered = tree.reorder(varB, newOrder);

        assertTrue(reordered instanceof TreeADDPotential);
        TreeADDPotential reorderedTree = (TreeADDPotential) reordered;

        // Branch order unchanged (X is still top variable)
        assertEquals(2, reorderedTree.getBranches().size());
        TreeADDBranch first = reorderedTree.getBranches().get(0);
        assertTrue(first.getStates().contains(s0), "First branch should still be 'absent'");

        // Leaf values should be reordered for Y: [absent, present] → [present, absent]
        TablePotential firstLeaf = (TablePotential) first.getPotential();
        assertEquals(0.0, firstLeaf.getValues()[0], 1e-9, "After reorder: P(Y=present|X=absent)=0.0");
        assertEquals(1.0, firstLeaf.getValues()[1], 1e-9, "After reorder: P(Y=absent|X=absent)=1.0");
    }

}
