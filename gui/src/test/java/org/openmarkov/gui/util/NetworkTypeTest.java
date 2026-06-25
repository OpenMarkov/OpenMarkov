/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openmarkov.core.model.network.NodeType;

import java.util.Set;


/**
 * This class tests the class {@link NetworkType}.
 *
 * @author jmendoza
 */
public class NetworkTypeTest {
	/**
	 * This method tests the type of nodes that can be inserted into each type
	 * of network.
	 */
	@Test public final void testGetNodeTypes() {
		Set<NodeType> nodeTypes;
		nodeTypes = NetworkType.BAYESIAN_NET.getNodeTypes();
		assertEquals(nodeTypes.size(), 1);
		assertTrue(nodeTypes.contains(NodeType.CHANCE));
		nodeTypes = NetworkType.INFLUENCE_DIAGRAM.getNodeTypes();
		assertEquals(nodeTypes.size(), 3);
		assertTrue(nodeTypes.contains(NodeType.CHANCE));
		assertTrue(nodeTypes.contains(NodeType.DECISION));
		assertTrue(nodeTypes.contains(NodeType.UTILITY));
		nodeTypes = NetworkType.MARKOV_NET.getNodeTypes();
		assertEquals(nodeTypes.size(), 0);
		nodeTypes = NetworkType.CHAIN_GRAPH.getNodeTypes();
		assertEquals(nodeTypes.size(), 0);
	}
}
