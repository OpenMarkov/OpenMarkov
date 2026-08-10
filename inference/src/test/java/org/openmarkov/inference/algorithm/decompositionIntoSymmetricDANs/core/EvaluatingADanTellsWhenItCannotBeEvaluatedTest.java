/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.NoMixedParents;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Variable elimination refuses a network whose utility node mixes utility parents with chance or
 * decision ones. The evaluation of a decision analysis network must say so instead of answering
 * with zeros.
 *
 * @author Manuel Arias
 */
class EvaluatingADanTellsWhenItCannotBeEvaluatedTest {

	/** U2 is a utility node with a utility parent, U1, and a chance parent, C. */
	private static ProbNet danWithMixedParents() {
		ProbNet dan = healthyDan();
		Variable chance = new Variable("C", "absent", "present");
		Variable superValue = new Variable("U2");
		dan.addNode(chance, NodeType.CHANCE);
		dan.addNode(superValue, NodeType.UTILITY);
		dan.addLink(dan.getVariable("U1"), superValue, true);
		dan.addLink(chance, superValue, true);
		return dan;
	}

	/** D → U1, which variable elimination accepts. */
	private static ProbNet healthyDan() {
		ProbNet dan = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
		Variable decision = new Variable("D", "no", "yes");
		Variable utility = new Variable("U1");
		dan.addNode(decision, NodeType.DECISION);
		dan.addNode(utility, NodeType.UTILITY);
		dan.addLink(decision, utility, true);
		return dan;
	}

	@Test
	void aNetworkTheAlgorithmRefusesIsReportedInsteadOfWeighingZero() {
		NotEvaluableNetworkException.UnsatisfiedConstraints thrown = assertThrows(
				NotEvaluableNetworkException.UnsatisfiedConstraints.class,
				() -> new DANDecompositionIntoSymmetricDANsEvaluation(danWithMixedParents()),
				"The evaluation answered with numbers for a network the algorithm cannot handle");

		assertTrue(thrown.unsatisfiedConstraints.stream().anyMatch(NoMixedParents.class::isInstance),
				"The report does not name the constraint the network fails");
	}

	@Test
	void aNetworkTheAlgorithmAcceptsIsStillEvaluated() {
		assertDoesNotThrow(() -> new DANDecompositionIntoSymmetricDANsEvaluation(healthyDan()).getUtility(),
				"A network the algorithm accepts was refused");
	}
}
