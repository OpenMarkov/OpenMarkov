/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The constraint of the networks that admit a single agent: the network must carry no agent list,
 * and no variable may name an agent.
 *
 * @author Manuel Arias
 */
class OnlyOneAgentTest {

	private ProbNet probNet;
	private OnlyOneAgent onlyOneAgent;

	@BeforeEach
	void setUp() {
		probNet = new ProbNet();
		onlyOneAgent = new OnlyOneAgent();
	}

	@Test
	void aNetworkWithoutAgentsIsAccepted() {
		assertTrue(onlyOneAgent.isMetBy(probNet));
	}

	/**
	 * Removing every agent of a network leaves an empty list, not a null one. A network with zero
	 * agents has nothing to complain about.
	 */
	@Test
	void anEmptyAgentListIsAccepted() {
		probNet.setAgents(new ArrayList<>());

		assertTrue(onlyOneAgent.isMetBy(probNet), "A network with no agents was reported as having them");
	}

	@Test
	void aNetworkWithAgentsIsRejected() {
		probNet.setAgents(new ArrayList<>(List.of(new StringWithProperties("Agent 1"))));

		assertFalse(onlyOneAgent.isMetBy(probNet));
	}

	@Test
	void aVariableThatNamesAnAgentIsRejected() {
		Variable therapy = new Variable("Therapy", "no", "yes");
		therapy.setAgent(new StringWithProperties("Agent 1"));
		probNet.addNode(therapy, NodeType.DECISION);

		assertFalse(onlyOneAgent.isMetBy(probNet));
	}
}
