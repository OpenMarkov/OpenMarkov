/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A network may hold potentials with no variables. The constraints that ask a potential for its
 * first variable have to skip them instead of failing.
 *
 * @author Manuel Arias
 */
class ConstraintsToleratePotentialsWithoutVariablesTest {

	/**
	 * A potential with no variables goes to the network's list of constant potentials, and from
	 * there into the potentials the constraint walks.
	 */
	@Test
	void utilityNodesSkipsAConstantPotential() {
		ProbNet probNet = new ProbNet();
		probNet.addNode(new Variable("cost"), NodeType.UTILITY);
		probNet.addPotential(constantPotential());

		assertDoesNotThrow(() -> new UtilityNodes().isMetBy(probNet));
	}

	/**
	 * The constant is not counted as the potential of any utility node, so a network whose only
	 * utility node has its potential still satisfies the constraint.
	 */
	@Test
	void aConstantPotentialIsNotCountedAsTheUtilityOfANode() {
		ProbNet probNet = new ProbNet();
		Variable disease = new Variable("Disease", "absent", "present");
		probNet.addNode(disease, NodeType.CHANCE);
		Node costNode = probNet.addNode(new Variable("cost"), NodeType.UTILITY);
		costNode.getVariable().setDecisionCriterion(new Criterion("cost"));
		probNet.addPotential(new TablePotential(List.of(costNode.getVariable(), disease),
				PotentialRole.UNSPECIFIED));

		assertTrue(new UtilityNodes().isMetBy(probNet), "The single utility node has its potential");

		probNet.addPotential(constantPotential());

		assertTrue(new UtilityNodes().isMetBy(probNet), "The constant potential changed the count");
	}

	/**
	 * The same shape in the constraint that looks for the potential of every chance node.
	 */
	@Test
	void chancePotentialsSkipAPotentialWithoutVariables() {
		ProbNet probNet = new ProbNet();
		Node node = probNet.addNode(new Variable("Disease", "absent", "present"), NodeType.CHANCE);
		List<Potential> potentials = new ArrayList<>();
		potentials.add(constantPotential());
		node.setPotentials(potentials);

		AllChanceVariablesHaveChancePotentials constraint = new AllChanceVariablesHaveChancePotentials();

		assertDoesNotThrow(() -> constraint.isMetBy(probNet));
		assertFalse(constraint.isMetBy(probNet),
				"A potential with no variables is not the potential of the chance node");
	}

	private static TablePotential constantPotential() {
		return new TablePotential(List.of(), PotentialRole.UNSPECIFIED, new double[]{7.0});
	}
}
