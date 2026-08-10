/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.graphic.VisualNode;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Changing the finding of a node that already had one: the window takes the old finding away and
 * puts the new one before running the edit, so undo has to restore the old one and redo has to put
 * the new one back over it.
 *
 * @author Manuel Arias
 */
class RedoingAChangeOfFindingPutsTheNewFindingBackTest {

	private ProbNet probNet;
	private VisualNode visualNode;
	private EvidenceCase evidence;
	private Finding oldFinding;
	private Finding newFinding;

	@BeforeEach
	void setUp() throws Exception {
		probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		probNet.addNode(new Variable("A", 2), NodeType.CHANCE);
		visualNode = new VisualNetwork(probNet, null).getAllNodes().getFirst();

		Variable variable = probNet.getVariable("A");
		oldFinding = new Finding(variable, 0);
		newFinding = new Finding(variable, 1);
		evidence = new EvidenceCase();
		evidence.addFinding(oldFinding);
	}

	/** What the window does before running the edit: the old finding out, the new one in. */
	private AddFindingEdit changeOfFinding() {
		evidence.removeFinding(newFinding.getVariable());
		evidence.changeFinding(newFinding);
		return new AddFindingEdit(visualNode, evidence, oldFinding, newFinding);
	}

	@Test
	void redoingPutsTheNewFindingBack() throws Exception {
		AddFindingEdit edit = changeOfFinding();
		edit.executeEdit();
		edit.undo();

		assertDoesNotThrow(edit::redo, "Redoing ran against the finding that undo had restored");

		assertEquals(1, evidence.getFinding(probNet.getVariable("A")).getStateIndex(),
				"Redoing did not leave the new finding");
	}

	@Test
	void undoingRestoresTheFindingThatWasThere() throws Exception {
		AddFindingEdit edit = changeOfFinding();
		edit.executeEdit();

		edit.undo();

		assertEquals(0, evidence.getFinding(probNet.getVariable("A")).getStateIndex(),
				"Undoing did not restore the finding the node had");
	}
}
