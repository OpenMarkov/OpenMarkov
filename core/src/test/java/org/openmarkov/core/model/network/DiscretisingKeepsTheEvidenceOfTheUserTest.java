/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Turning the numeric variables of a network into finite states ones walks every configuration of
 * the parents of each numeric node. Evidence on one of those parents must not get in the way.
 *
 * @author Manuel Arias
 */
class DiscretisingKeepsTheEvidenceOfTheUserTest {

	private ProbNet probNet;

	/**
	 * P → N, where P has two states and N is numeric, worth 3 under the first state of P and 7
	 * under the second.
	 */
	@BeforeEach
	void setUp() {
		probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		Variable parent = new Variable("P", "p0", "p1");
		probNet.addNode(parent, NodeType.CHANCE);

		Variable numeric = new Variable("N", "0");
		numeric.setVariableType(VariableType.NUMERIC);
		probNet.addNode(numeric, NodeType.CHANCE);
		probNet.addLink(parent, numeric, true);

		probNet.getNode(numeric).setPotential(new TablePotential(List.of(numeric, parent),
				PotentialRole.CONDITIONAL_PROBABILITY, new double[]{3.0, 7.0}));
	}

	@Test
	void aNumericNodeIsDiscretisedWithoutEvidence() throws Exception {
		ProbNet converted = ProbNetOperations.convertNumericalVariablesToFS(probNet, new EvidenceCase());

		assertEquals(2, converted.getVariable("N").getNumStates(),
				"The numeric node did not take one state per value of its parent");
	}

	/**
	 * The user has observed the second state of the parent, which is not the one the walk starts
	 * from.
	 */
	@Test
	void aNumericNodeIsDiscretisedWithEvidenceOnItsParent() throws Exception {
		EvidenceCase evidence = new EvidenceCase();
		evidence.addFinding(new Finding(probNet.getVariable("P"), 1));

		ProbNet converted = assertDoesNotThrow(
				() -> ProbNetOperations.convertNumericalVariablesToFS(probNet, evidence),
				"Discretising refused the evidence the user had set");

		assertEquals(2, converted.getVariable("N").getNumStates(),
				"Evidence on the parent changed the states the numeric node was given");
	}
}
