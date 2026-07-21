/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.heuristic.canoAndMoral;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.RemoveNodeEdit;
import org.openmarkov.core.inference.BasicOperations;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;


public class CanoAndMoralTest {

	private CanoMoralElimination canoAndMoral = null;
	private ProbNet probNet = null;

	private static ProbNet buildDI_test_CanoAndMoral_1_java() {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		// Variables
		Variable varA = new Variable("A", "ausente", "presente");
		Variable varB = new Variable("B", "ausente", "presente");
		Variable varD = new Variable("D", "no", "sí");
		Variable varU = new Variable("U");
		Variable varC = new Variable("C", "ausente", "presente");
		Variable varD1 = new Variable("D1", "no", "sí");
		Variable varU1 = new Variable("U1");

		// Nodes
		Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
		Node nodeB = probNet.addNode(varB, NodeType.CHANCE);
		Node nodeD = probNet.addNode(varD, NodeType.DECISION);
		Node nodeU = probNet.addNode(varU, NodeType.UTILITY);
		Node nodeC = probNet.addNode(varC, NodeType.CHANCE);
		Node nodeD1 = probNet.addNode(varD1, NodeType.DECISION);
		Node nodeU1 = probNet.addNode(varU1, NodeType.UTILITY);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeA, nodeB, true);
		probNet.addLink(nodeA, nodeU, true);
		probNet.addLink(nodeB, nodeD, true);
		probNet.addLink(nodeD, nodeC, true);
		probNet.addLink(nodeD, nodeU, true);
		probNet.addLink(nodeC, nodeD1, true);
		probNet.addLink(nodeD1, nodeU1, true);

		// Potentials
		UniformPotential potA = new UniformPotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeA.setPotential(potA);

		UniformPotential potB = new UniformPotential(Arrays.asList(varB, varA), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeB.setPotential(potB);

		UniformPotential potU = new UniformPotential(Arrays.asList(varU, varD, varA), PotentialRole.UNSPECIFIED);
		nodeU.setPotential(potU);

		UniformPotential potC = new UniformPotential(Arrays.asList(varC, varD), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeC.setPotential(potC);

		UniformPotential potU1 = new UniformPotential(Arrays.asList(varU1, varD1), PotentialRole.UNSPECIFIED);
		nodeU1.setPotential(potU1);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	@BeforeEach public void setUp() {
		probNet = buildDI_test_CanoAndMoral_1_java();
		List<List<Variable>> setsOfVariablesToEliminate = BasicOperations.calculatePartialOrder(probNet);
		canoAndMoral = new CanoMoralElimination(probNet, setsOfVariablesToEliminate);
	}
	
	@Tag(TestSpeed.SLOW)
	@Test public void testCanoAndMoral() {
		Variable variable = removeNextVariable(canoAndMoral);
		assertNotNull(variable);
		assertTrue(variable.getName().contentEquals("A"));
		variable = removeNextVariable(canoAndMoral);
		assertNotNull(variable);
		assertTrue(variable.getName().contentEquals("D1"));
		variable = removeNextVariable(canoAndMoral);
		assertNotNull(variable);
		assertTrue(variable.getName().contentEquals("C"));
		variable = removeNextVariable(canoAndMoral);
		assertNotNull(variable);
		assertTrue(variable.getName().contentEquals("D"));
		variable = removeNextVariable(canoAndMoral);
		assertNotNull(variable);
		assertTrue(variable.getName().contentEquals("B"));
	}

	/**
	 * @param heuristic
	 * @return variable removed
	 */
	private Variable removeNextVariable(EliminationHeuristic heuristic) {
		Variable variable = heuristic.getVariableToDelete();
		RemoveNodeEdit removeEdit = new RemoveNodeEdit(probNet, probNet.getNode(variable));
        heuristic.afterEditExecutes(removeEdit);
		return variable;
	}
	
}
