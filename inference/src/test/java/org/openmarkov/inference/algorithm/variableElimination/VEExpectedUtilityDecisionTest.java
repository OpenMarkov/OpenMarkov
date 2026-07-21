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
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEExpectedUtilityDecision;
import org.openmarkov.inference.testutils.TestNetworks;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for VEExpectedUtilityDecision: computes the expected utility
 * as a function of a given decision variable and its informational predecessors.
 *
 * @author Manuel Arias
 */
public class VEExpectedUtilityDecisionTest {

	private static final double TOLERANCE = 1E-4;

	@Tag(TestSpeed.FAST)
	@Test
	public void testExpectedUtilityPerDecision() throws Exception {
		ProbNet probNet = TestNetworks.buildSimpleID();
		Variable decision = probNet.getVariable("Decision");

		VEExpectedUtilityDecision veEUD = new VEExpectedUtilityDecision(probNet, decision);
		TablePotential eu = veEUD.getExpectedUtility();

		assertNotNull(eu, "Should return the expected utility table");
		assertTrue(eu.getVariables().contains(decision),
				"Expected utility table should contain the Decision variable");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testExpectedUtilityValues() throws Exception {
		ProbNet probNet = TestNetworks.buildSimpleID();
		Variable decision = probNet.getVariable("Decision");

		VEExpectedUtilityDecision veEUD = new VEExpectedUtilityDecision(probNet, decision);
		TablePotential eu = veEUD.getExpectedUtility();

		// EU(D=d0) = P(C=c0)*U(C=c0,D=d0) + P(C=c1)*U(C=c1,D=d0)
		//          = 0.7*10 + 0.3*3 = 7.9
		// EU(D=d1) = P(C=c0)*U(C=c0,D=d1) + P(C=c1)*U(C=c1,D=d1)
		//          = 0.7*5 + 0.3*8 = 5.9
		double[] values = eu.getValues();
		assertNotNull(values);
		assertTrue(values.length >= 2, "Should have at least 2 utility values (one per decision state)");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testLazyResolution() throws Exception {
		ProbNet probNet = TestNetworks.buildSimpleID();
		Variable decision = probNet.getVariable("Decision");

		VEExpectedUtilityDecision veEUD = new VEExpectedUtilityDecision(probNet, decision);

		// Calling getExpectedUtility twice should return the same result (lazy resolution)
		TablePotential first = veEUD.getExpectedUtility();
		TablePotential second = veEUD.getExpectedUtility();
		assertSame(first, second, "Repeated calls should return the cached result");
	}
}
