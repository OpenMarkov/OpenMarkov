/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.link;

import org.junit.jupiter.api.BeforeEach;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

public class LinkRestrictionPanelTest {

	private LinkRestrictionPanel panel = null;
	private Variable varA, varB;
	private State[] stateA, stateB;

	@BeforeEach public void setUp() {

		stateA = new State[] { new State("A1"), new State("A2"), new State("A3") };
		stateB = new State[] { new State("B1"), new State("B2") };
		varA = new Variable("A", stateA);
		varB = new Variable("B", stateB);
		List<Variable> variables = new ArrayList<Variable>();
		variables.add(varA);
		variables.add(varB);
		ProbNet net = new ProbNet();
		Node nodeA = new Node(net, varA, NodeType.CHANCE);
		Node nodeB = new Node(net, varB, NodeType.CHANCE);
		Link<Node> link = new Link<Node>(nodeA, nodeB, true);
		net.addLink(nodeA, nodeB, true);
		link.initializesRestrictionsPotential();
		link.setCompatibilityValue(stateA[1], stateB[0], 0);
		link.setCompatibilityValue(stateA[0], stateB[1], 0);
		panel = new LinkRestrictionPanel(link);

	}

	//	@Test
	//	public void testInit() {
	//
	//		 JFrame frame = new JFrame();
	//				 frame.add(panel);
	//				 frame.setSize(600, 400);
	//				 frame.show();
	//				 frame.show(false);
	//	}

}
