/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.junit.jupiter.api.*;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.graphic.VisualNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;


/**
 * This class tests the action of undoing the movement of nodes.
 *
 * @author mpalacios
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MoveNodeEditTest {
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
	 * @throws Exception if an error occurrs.
	 */
    @BeforeEach public void setUp() throws DoEditException {

		probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		probNet.setName("Influence diagram");
		probNet.setComment("Influence diagram for testing");
		variableA = new Variable("A");
		variableB = new Variable("B");

		node1 = probNet.addNode(variableA, NodeType.CHANCE);

		node1.setCoordinateX(100.0);
		node1.setCoordinateY(150.0);
		node2 = probNet.addNode(variableB, NodeType.DECISION);
		node2.setCoordinateX(57.0);
		node2.setCoordinateY(49.0);
		
		VisualNode visualNodeA = new VisualNode(node1, null);
		VisualNode visualNodeB = new VisualNode(node2, null);
		visualNodeA.setTemporalPosition(new Point2D.Double(21, 160));
		visualNodeB.setTemporalPosition(new Point2D.Double(101, 99));

		ArrayList<VisualNode> movedNodes = new ArrayList<VisualNode>(2);
		movedNodes.add(visualNodeA);
		movedNodes.add(visualNodeB);

		probNet.getPNESupport().setWithUndo(true);
		MoveNodeEdit moveNodeEdit = new MoveNodeEdit(movedNodes);
        
        moveNodeEdit.executeEdit();
    }

	/**
	 * This method undoes and redoes several times.
	 *
     */
	@Tag(TestSpeed.SLOW)
	@Test public final void testUndoRedo() {
		var manager = probNet.getPNESupport().getCurrentEditHistory();
		assertEquals(node1.getCoordinateX(), 21.0, 0.1);
		assertEquals(node1.getCoordinateY(), 160.0, 0.1);
		assertEquals(node2.getCoordinateX(), 101.0, 0.1);
		assertEquals(node2.getCoordinateY(), 99.0, 0.1);
		probNet.getPNESupport().undo();
		assertEquals(node1.getCoordinateX(), 100.0, 0.1);
		assertEquals(node1.getCoordinateY(), 150.0, 0.1);
		assertEquals(node2.getCoordinateX(), 57.0, 0.1);
		assertEquals(node2.getCoordinateY(), 49.0, 0.1);
		probNet.getPNESupport().redo();
		assertEquals(node1.getCoordinateX(), 21.0, 0.1);
		assertEquals(node1.getCoordinateY(), 160.0, 0.1);
		assertEquals(node2.getCoordinateX(), 101.0, 0.1);
		assertEquals(node2.getCoordinateY(), 99.0, 0.1);
		probNet.getPNESupport().undo();
		assertEquals(node1.getCoordinateX(), 100.0, 0.1);
		assertEquals(node1.getCoordinateY(), 150.0, 0.1);
		assertEquals(node2.getCoordinateX(), 57.0, 0.1);
		assertEquals(node2.getCoordinateY(), 49.0, 0.1);
		probNet.getPNESupport().redo();
	}
}
