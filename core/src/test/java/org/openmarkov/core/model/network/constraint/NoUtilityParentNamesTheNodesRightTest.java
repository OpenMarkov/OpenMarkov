/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A utility node cannot be the parent of a node that is not one. The refusal has to say which node
 * is which.
 *
 * @author Manuel Arias
 */
class NoUtilityParentNamesTheNodesRightTest {

	private ProbNet probNet;

	@BeforeEach
	void setUp() {
		probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		probNet.addNode(new Variable("cost"), NodeType.UTILITY);
		probNet.addNode(new Variable("Disease", "absent", "present"), NodeType.CHANCE);
	}

	/**
	 * The message tells the child from the utility parent: "Node {child} cannot be a child of
	 * utility parent: {utilityNode}".
	 */
	@Test
	void theRefusedLinkNamesTheChildAndTheUtilityParentInThatOrder() {
		AddLinkEdit edit = new AddLinkEdit(probNet, probNet.getVariable("cost"),
				probNet.getVariable("Disease"), true);

		ConstraintViolatedException complaint = assertThrows(ConstraintViolatedException.class,
				edit::tryConstraintsWillBeMet);

		assertNamesTheChildThenTheUtilityParent(complaint);
	}

	/**
	 * Checking the whole network words it the same way.
	 */
	@Test
	void theWholeNetworkComplaintNamesThemInTheSameOrder() {
		probNet.addLink(probNet.getVariable("cost"), probNet.getVariable("Disease"), true);

		ConstraintViolatedException complaint = assertThrows(ConstraintViolatedException.class,
				probNet::checkConstraints);

		assertNamesTheChildThenTheUtilityParent(complaint);
	}

	/**
	 * The message names the child first and the utility parent after the colon. Each node prints
	 * its whole description, so what is checked is which name falls on each side.
	 */
	private static void assertNamesTheChildThenTheUtilityParent(ConstraintViolatedException complaint) {
		String message = complaint.toString();
		int separator = message.indexOf("cannot be a child of utility parent");
		assertTrue(separator > 0, "The message does not have the expected shape: " + message);

		String beforeSeparator = message.substring(0, separator);
		String afterSeparator = message.substring(separator);
		assertTrue(beforeSeparator.contains("Disease") && !beforeSeparator.contains("cost"),
				"The child of the link is not the one named as the child: " + message);
		assertTrue(afterSeparator.contains("cost") && !afterSeparator.contains("Disease"),
				"The utility node is not the one named as the utility parent: " + message);
	}
}
