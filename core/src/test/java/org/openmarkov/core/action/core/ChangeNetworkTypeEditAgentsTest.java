/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DECPOMDPType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.model.network.type.NetworkType;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Turning a network into a multiagent one gives it two agents. Undoing has to leave it as it was,
 * agents included.
 *
 * @author Manuel Arias
 */
class ChangeNetworkTypeEditAgentsTest {

	private ProbNet probNet;
	private NetworkType typeBefore;

	/**
	 * A network of temporal variables, which is what a multiagent one requires.
	 */
	@BeforeEach
	void setUp() {
		probNet = new ProbNet(MIDType.getUniqueInstance());
		Variable variable = new Variable("A", 2);
		variable.setTimeSlice(0);
		probNet.addNode(variable, NodeType.CHANCE);
		typeBefore = probNet.getNetworkType();
	}

	/**
	 * Becoming multiagent adds two agents of its own: that is what undoing has to take away again.
	 */
	@Test
	void becomingMultiagentGivesTheNetworkTwoAgents() throws Exception {
		new ChangeNetworkTypeEdit(probNet, DECPOMDPType.getUniqueInstance()).executeEdit();

		assertNotNull(probNet.getAgents());
		assertEquals(2, probNet.getAgents().size());
	}

	@Test
	void undoingLeavesTheNetworkWithTheTypeAndTheAgentsItHad() throws Exception {
		ChangeNetworkTypeEdit edit = new ChangeNetworkTypeEdit(probNet, DECPOMDPType.getUniqueInstance());
		edit.executeEdit();

		assertDoesNotThrow(edit::undo, "Undoing could not restore the type of the network");

		assertAll(() -> assertEquals(typeBefore, probNet.getNetworkType(), "The type was not restored"),
				() -> assertNull(probNet.getAgents(), "The network kept the agents that becoming multiagent added"));
	}
}
