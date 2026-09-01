/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.LinkOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Changing the states of a node clears the restrictions of its links. Undoing has to put them back.
 *
 * @author Manuel Arias
 */
class NodeStateEditLinkRestrictionTest {

	private ProbNet probNet;
	private Node node;
	private double[] restrictionBefore;

	/**
	 * A → B, with the combination (a0, b0) declared incompatible.
	 */
	@BeforeEach
	void setUp() {
		probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		probNet.addNode(new Variable("A", "a0", "a1"), NodeType.CHANCE);
		probNet.addNode(new Variable("B", "b0", "b1"), NodeType.CHANCE);
		probNet.addLink(probNet.getVariable("A"), probNet.getVariable("B"), true);

		node = probNet.getNode("A");
		LinkOperations.setCompatibilityValue(link(), probNet.getVariable("A").getState("a0"),
				probNet.getVariable("B").getState("b0"), 0);
		restrictionBefore = link().getRestrictionsPotential().getValues().clone();
	}

	@Test
	void undoingTheAdditionOfAStatePutsTheLinkRestrictionBack() throws Exception {
		NodeStateEdit edit = new NodeStateEdit(node, StateAction.ADD, 0, "a2");

		edit.executeEdit();
		edit.undo();

		assertTrue(link().hasRestrictions(), "Undoing left the link without its restriction");
		assertArrayEquals(restrictionBefore, link().getRestrictionsPotential().getValues(),
				"The link did not get its restriction back");
	}

	@Test
	void undoingTheRemovalOfAStatePutsTheLinkRestrictionBack() throws Exception {
		NodeStateEdit edit = new NodeStateEdit(node, StateAction.REMOVE, 0, "");

		edit.executeEdit();
		edit.undo();

		assertTrue(link().hasRestrictions(), "Undoing left the link without its restriction");
		assertArrayEquals(restrictionBefore, link().getRestrictionsPotential().getValues(),
				"The link did not get its restriction back");
	}

	/**
	 * Modifying the states really does clear the restriction: that is what undoing has to repair.
	 */
	@Test
	void addingAStateClearsTheLinkRestriction() throws Exception {
		new NodeStateEdit(node, StateAction.ADD, 0, "a2").executeEdit();

		assertTrue(!link().hasRestrictions(), "The restriction survived the change of states");
	}

	private Link<Node> link() {
		return probNet.getLink(probNet.getNode("A"), probNet.getNode("B"), true);
	}
}
