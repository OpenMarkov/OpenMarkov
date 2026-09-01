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
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Absorbing a node rewrites the potential of its child. Undoing has to put the old one back.
 *
 * @author Manuel Arias
 */
class AbsorbNodeEditTest {

	private ProbNet probNet;
	private Node childNode;
	private double[] valuesBefore;

	/**
	 * A → C, where C also has a second parent B, so that absorbing A leaves C with a potential.
	 */
	@BeforeEach
	void setUp() throws Exception {
		probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		new AddNodeEdit(probNet, new Variable("A", 2), NodeType.CHANCE, null).executeEdit();
		new AddNodeEdit(probNet, new Variable("B", 3), NodeType.CHANCE, null).executeEdit();
		new AddNodeEdit(probNet, new Variable("C", 2), NodeType.CHANCE, null).executeEdit();
		probNet.addLink(probNet.getVariable("A"), probNet.getVariable("C"), true);
		probNet.addLink(probNet.getVariable("B"), probNet.getVariable("C"), true);

		childNode = probNet.getNode("C");
		valuesBefore = ((TablePotential) childNode.getPotentials().getFirst()).getValues().clone();
	}

	@Test
	void undoingAnAbsorptionPutsTheChildPotentialBack() throws Exception {
		AbsorbNodeEdit edit = new AbsorbNodeEdit(probNet, probNet.getVariable("A"));

		edit.executeEdit();
		edit.undo();

		List<Potential> potentialsAfter = probNet.getNode("C").getPotentials();
		assertFalse(potentialsAfter.isEmpty(), "Undoing left the child without any potential");
		assertArrayEquals(valuesBefore, ((TablePotential) potentialsAfter.getFirst()).getValues(),
				"The child did not get its old potential back");
	}

	@Test
	void theAbsorptionItselfChangesTheChildPotential() throws Exception {
		AbsorbNodeEdit edit = new AbsorbNodeEdit(probNet, probNet.getVariable("A"));

		edit.executeEdit();

		List<Variable> variablesAfter = probNet.getNode("C").getPotentials().getFirst().getVariables();
		assertFalse(variablesAfter.contains(probNet.getVariable("A")),
				"The absorbed variable is still in the potential of the child");
	}

	/**
	 * The links the absorption creates towards the child are removed when it is undone.
	 */
	@Test
	void undoingAnAbsorptionRemovesTheLinksItCreated() throws Exception {
		Node grandparent = addNodeWithLinkTo("G", "A");
		int linksBefore = probNet.getLinks().size();

		AbsorbNodeEdit edit = new AbsorbNodeEdit(probNet, probNet.getVariable("A"));
		edit.executeEdit();
		edit.undo();

		assertEquals(linksBefore, probNet.getLinks().size(),
				"Undoing left behind the links the absorption created");
		assertFalse(probNet.getNode("C").getParents().contains(grandparent),
				"The grandparent stayed a parent of the child after undoing");
	}

	private Node addNodeWithLinkTo(String name, String childName) throws Exception {
		new AddNodeEdit(probNet, new Variable(name, 2), NodeType.CHANCE, null).executeEdit();
		Node node = probNet.getNode(name);
		probNet.addLink(node.getVariable(), probNet.getVariable(childName), true);
		List<Potential> potentials = new ArrayList<>();
		Node child = probNet.getNode(childName);
		List<Variable> variables = new ArrayList<>();
		variables.add(child.getVariable());
		for (Node parent : child.getParents()) {
			variables.add(parent.getVariable());
		}
		potentials.add(new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY));
		child.setPotentials(potentials);
		return node;
	}
}
