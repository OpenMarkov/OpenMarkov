/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for VEEvaluation: Influence Diagram evaluation
 * (expected utility and optimal policies).
 *
 * @author Manuel Arias
 */
public class VEEvaluationTest {

	private static final double TOLERANCE = 1E-4;

	@Tag(TestSpeed.FAST)
	@Test
	public void testSimpleIDUtility() throws Exception {
		ProbNet probNet = TestNetworks.buildSimpleID();
		VEEvaluation evaluation = new VEEvaluation(probNet);

		TablePotential utility = evaluation.getUtility();
		assertNotNull(utility, "Should return the expected utility");

		// Expected utility = P(C=0)*max(U(C=0,D=0), U(C=0,D=1))
		//                  + P(C=1)*max(U(C=1,D=0), U(C=1,D=1))
		//                  = 0.7*max(10,5) + 0.3*max(3,8)
		//                  = 0.7*10 + 0.3*8 = 7.0 + 2.4 = 9.4
		double expectedUtility = 9.4;
		assertEquals(expectedUtility, utility.getValues()[0], TOLERANCE,
				"Expected utility of the simple ID");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testSimpleIDOptimalPolicies() throws Exception {
		ProbNet probNet = TestNetworks.buildSimpleID();
		VEEvaluation evaluation = new VEEvaluation(probNet);

		HashMap<Variable, Potential> policies = evaluation.getOptimalPolicies();
		assertNotNull(policies);
		assertFalse(policies.isEmpty(), "There should be at least one optimal policy");

		// There should be a policy for the Decision variable
		Variable decision = probNet.getVariable("Decision");
		assertTrue(policies.containsKey(decision),
				"A policy should exist for the Decision variable");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testSimpleIDOptimalPolicyValues() throws Exception {
		ProbNet probNet = TestNetworks.buildSimpleID();
		VEEvaluation evaluation = new VEEvaluation(probNet);

		Variable decision = probNet.getVariable("Decision");
		Potential policy = evaluation.getOptimalPolicy(decision);
		assertNotNull(policy, "Should return the optimal policy for Decision");

		// The policy should be a TablePotential with variables [Decision, Chance]
		assertInstanceOf(TablePotential.class, policy);
		TablePotential policyTable = (TablePotential) policy;

		// Optimal: D=d0 when C=c0 (U=10 > U=5), D=d1 when C=c1 (U=8 > U=3)
		// Policy is P(D|C): [P(D=d0|C=c0), P(D=d1|C=c0), P(D=d0|C=c1), P(D=d1|C=c1)]
		//                 = [1, 0, 0, 1]
		double[] values = policyTable.getValues();
		assertTrue(values.length >= 4, "Policy table should have at least 4 entries");
		assertEquals(1.0, values[0], TOLERANCE, "P(D=d0|C=c0) should be 1 (optimal)");
		assertEquals(0.0, values[1], TOLERANCE, "P(D=d1|C=c0) should be 0");
		assertEquals(0.0, values[2], TOLERANCE, "P(D=d0|C=c1) should be 0");
		assertEquals(1.0, values[3], TOLERANCE, "P(D=d1|C=c1) should be 1 (optimal)");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testSimpleIDProbability() throws Exception {
		ProbNet probNet = TestNetworks.buildSimpleID();
		VEEvaluation evaluation = new VEEvaluation(probNet);

		TablePotential probability = evaluation.getProbability();
		assertNotNull(probability, "Should return the probability");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testSimpleIDStrategyTree() throws Exception {
		ProbNet probNet = TestNetworks.buildSimpleID();
		VEEvaluation evaluation = new VEEvaluation(probNet);

		assertNotNull(evaluation.getOptimalStrategyTree(),
				"Should return a strategy tree for the simple ID");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testUtilityIsConsistentWithPolicies() throws Exception {
		ProbNet probNet = TestNetworks.buildSimpleID();
		VEEvaluation evaluation = new VEEvaluation(probNet);

		TablePotential utility = evaluation.getUtility();
		HashMap<Variable, Potential> policies = evaluation.getOptimalPolicies();

		// Both should come from the same resolved core
		assertNotNull(utility);
		assertNotNull(policies);
		assertFalse(policies.isEmpty());
		assertTrue(utility.getValues()[0] > 0,
				"Expected utility should be positive for this network");
	}
}
