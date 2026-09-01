/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Turning a numeric variable into another type replaces the potential of its node. Undoing has to
 * put the old one back.
 *
 * @author Manuel Arias
 */
class VariableTypeEditTest {

	private ProbNet probNet;
	private Node node;
	private Potential potentialBefore;

	@BeforeEach
	void setUp() throws Exception {
		probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		new AddNodeEdit(probNet, new Variable("A", 2), NodeType.CHANCE, null).executeEdit();
		node = probNet.getNode("A");

		new VariableTypeEdit(node, VariableType.NUMERIC, true).executeEdit();

		// A potential of its own, so that losing it is visible: the edit puts a uniform one in.
		// A numeric variable has a single state, so its table holds a single value.
		potentialBefore = new TablePotential(List.of(node.getVariable()),
				PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.42});
		node.setPotentials(new ArrayList<>(List.of(potentialBefore)));
	}

	@Test
	void undoingGivesTheNodeItsOwnPotentialBack() throws Exception {
		VariableTypeEdit edit = new VariableTypeEdit(node, VariableType.FINITE_STATES, true);

		edit.executeEdit();
		edit.undo();

		assertSame(potentialBefore, node.getPotentials().getFirst(),
				"Undoing did not give the node the potential it had");
	}

	/**
	 * The other caller of this edit asks it not to update the potential, and that path replaced it
	 * without writing the change down at all.
	 */
	@Test
	void undoingGivesThePotentialBackWhenTheEditWasAskedNotToUpdateIt() throws Exception {
		VariableTypeEdit edit = new VariableTypeEdit(node, VariableType.FINITE_STATES, false);

		edit.executeEdit();
		edit.undo();

		assertSame(potentialBefore, node.getPotentials().getFirst(),
				"Undoing did not give the node the potential it had");
	}
}
