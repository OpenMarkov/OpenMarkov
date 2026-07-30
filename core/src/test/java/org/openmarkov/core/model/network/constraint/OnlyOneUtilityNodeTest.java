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
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A network carrying this constraint accepts at most one utility node, both when the whole network
 * is checked and when an edit would add a second one.
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

	/**
	 * Checking the whole network is not enough to stop the user: an edit is refused beforehand by
	 * {@code checkConstraintsWillBeMet}, which is what the editor asks.
	 */
	@Test
	void addingASecondUtilityNodeIsRefused() {
		ProbNet probNet = influenceDiagramWith(onlyOneUtilityNode);
		probNet.addNode(new Variable("cost"), NodeType.UTILITY);

		AddNodeEdit edit = new AddNodeEdit(probNet, new Variable("effectiveness"), NodeType.UTILITY, null);

		assertFalse(edit.constraintsWillBeMet(), "The editor would have let a second utility node in");
	}

	@Test
	void addingTheFirstUtilityNodeIsAllowed() {
		ProbNet probNet = influenceDiagramWith(onlyOneUtilityNode);
		probNet.addNode(new Variable("Disease", "absent", "present"), NodeType.CHANCE);

		AddNodeEdit edit = new AddNodeEdit(probNet, new Variable("cost"), NodeType.UTILITY, null);

		assertTrue(edit.constraintsWillBeMet());
	}

	/**
	 * The refusal has to name the utility node the network already has.
	 */
	@Test
	void theRefusedEditNamesTheUtilityNodeAlreadyThere() {
		ProbNet probNet = influenceDiagramWith(onlyOneUtilityNode);
		probNet.addNode(new Variable("cost"), NodeType.UTILITY);

		AddNodeEdit edit = new AddNodeEdit(probNet, new Variable("effectiveness"), NodeType.UTILITY, null);
		ConstraintViolatedException complaint = assertThrows(ConstraintViolatedException.class,
				edit::tryConstraintsWillBeMet);

		assertTrue(complaint.toString().contains("cost"),
				"The refusal does not name the utility node already there: " + complaint);
	}

	/**
	 * A network type that admits utility nodes, so that what refuses the edit is this constraint and
	 * not the refusal a Bayesian network makes to any node that is not a chance node.
	 */
	private static ProbNet influenceDiagramWith(OnlyOneUtilityNode constraint) {
		ProbNet influenceDiagram = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		influenceDiagram.addConstraint(constraint);
		return influenceDiagram;
	}
}
