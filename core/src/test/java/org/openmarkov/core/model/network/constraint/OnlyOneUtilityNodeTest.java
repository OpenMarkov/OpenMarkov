/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OnlyOneUtilityNode is optional, and its check body was an empty {@code TODO} stub: it accepted
 * every network, including the ones it exists to reject. It stayed unnoticed because asking a
 * network type for its optional constraints failed outright, so nobody ever received this one.
 *
 * @author Manuel Arias
 */
class OnlyOneUtilityNodeTest {

	private ProbNet probNet;
	private OnlyOneUtilityNode onlyOneUtilityNode;

	@BeforeEach
	void setUp() {
		probNet = new ProbNet();
		onlyOneUtilityNode = new OnlyOneUtilityNode();
	}

	@Test
	void twoUtilityNodesAreRejected() {
		probNet.addNode(new Variable("cost"), NodeType.UTILITY);
		probNet.addNode(new Variable("effectiveness"), NodeType.UTILITY);

		assertFalse(onlyOneUtilityNode.isMetBy(probNet));
	}

	@Test
	void oneUtilityNodeIsAccepted() {
		probNet.addNode(new Variable("cost"), NodeType.UTILITY);

		assertTrue(onlyOneUtilityNode.isMetBy(probNet));
	}

	/**
	 * The constraint is an upper bound. Demanding at least one utility node is the job of
	 * {@link UtilityNodes} and {@link ProperUtilityPotentials}, so an empty network satisfies this one.
	 */
	@Test
	void aNetworkWithoutUtilityNodesIsAccepted() {
		probNet.addNode(new Variable("Disease", "absent", "present"), NodeType.CHANCE);

		assertTrue(onlyOneUtilityNode.isMetBy(probNet));
	}

	/**
	 * Only utility nodes count: a network full of chance and decision nodes and a single utility node
	 * is fine.
	 */
	@Test
	void nodesOfOtherKindsDoNotCount() {
		probNet.addNode(new Variable("Disease", "absent", "present"), NodeType.CHANCE);
		probNet.addNode(new Variable("Therapy", "no", "yes"), NodeType.DECISION);
		probNet.addNode(new Variable("cost"), NodeType.UTILITY);

		assertTrue(onlyOneUtilityNode.isMetBy(probNet));
	}

	/**
	 * The complaint has to name what is wrong: how many utility nodes there are and which ones.
	 */
	@Test
	void theComplaintNamesTheUtilityNodes() {
		probNet.addNode(new Variable("cost"), NodeType.UTILITY);
		probNet.addNode(new Variable("effectiveness"), NodeType.UTILITY);

		ConstraintChecker checker = new ConstraintChecker(probNet);
		onlyOneUtilityNode.checkProbNet(probNet, checker);
		ConstraintViolatedException complaint = assertThrows(ConstraintViolatedException.class, checker::buildAndThrow);

		String message = complaint.toString();
		assertTrue(message.contains("cost") && message.contains("effectiveness"),
				"The complaint does not name the utility nodes: " + message);
	}
}
