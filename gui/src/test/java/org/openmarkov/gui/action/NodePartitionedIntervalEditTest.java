/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.action.core.VariableTypeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;


/**
 * This class tests the action of undoing the changes in the node's name of nodes.
 *
 * @author mpalacios
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NodePartitionedIntervalEditTest {

	/**
	 * Network for testing.
	 */
	private ProbNet probNet = null;

	/**
	 * A whose position will be undone and redone.
	 */
	private Node node1 = null;

	/**
	 * A whose position will be undone and redone.
	 */
	private Node node2 = null;

	private Variable variableA;
	private Variable variableB;

	/**
	 * This method creates a network and various nodes and various links.
	 *
	 * @throws Exception if an error occurs.
	 */
    @BeforeEach public void setUp() throws DoEditException {

		probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		probNet.setName("Influence diagram");
		probNet.setComment("Influence diagram for testing");
		State[] states = { new State("absent"), new State("present") };
		variableA = new Variable("A", states);
		variableB = new Variable("B");

		node1 = probNet.addNode(variableA, NodeType.CHANCE);

		node1.setCoordinateX(100.0);
		node1.setCoordinateY(150.0);
		node2 = probNet.addNode(variableB, NodeType.DECISION);
		node2.setCoordinateX(57.0);
		node2.setCoordinateY(49.0);

		probNet.getPNESupport().setWithUndo(true);

		VariableTypeEdit variableTypeEdit = new VariableTypeEdit(node1, VariableType.DISCRETIZED, true);
        
        variableTypeEdit.executeEdit();
        
        NodePartitionedIntervalEdit nodePartitionedIntervalEdit = new NodePartitionedIntervalEdit(node1,
				StateAction.MODIFY_DELIMITER_INTERVAL, 0, true);
        
        nodePartitionedIntervalEdit.executeEdit();
    }

	/**
	 * This method undoes and redoes several times the node's name.
	 *
     */
	@Tag(TestSpeed.SLOW)
	@Test public final void testUndoRedo() {

		assertEquals(node1.getVariable().getPartitionedInterval().
				getBelongsToLeftSide(0), false);
		probNet.getPNESupport().undo();

		assertEquals(node1.getVariable().getPartitionedInterval().
				getBelongsToLeftSide(0), true);
		probNet.getPNESupport().redo();

		assertEquals(node1.getVariable().getPartitionedInterval().
				getBelongsToLeftSide(0), false);
		probNet.getPNESupport().undo();

		assertEquals(node1.getVariable().getPartitionedInterval().
				getBelongsToLeftSide(0), true);
		probNet.getPNESupport().redo();

	}

}
