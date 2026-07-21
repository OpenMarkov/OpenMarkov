/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.heuristic.minimalCliqueSize;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.RemoveNodeEdit;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the minimalCliqueSize heuristic (Kjaerulff 1993).
 *
 * @author Manuel Arias
 */
public class MinimalCliqueSizeTest {

	@Tag(TestSpeed.FAST)
	@Test
	public void testChainEliminatesExtremeFirst() {
		ProbNet probNet = TestNetworks.buildChain3();
		minimalCliqueSize heuristic = createHeuristic(probNet);

		// In A->B->C, A and C each form a clique of size 2 (node + 1 neighbor),
		// while B forms a clique of size 3. Should pick an extreme first.
		Variable first = heuristic.getVariableToDelete();
		assertNotNull(first);
		assertTrue(first.getName().equals("A") || first.getName().equals("C"),
				"In a chain, should eliminate an extreme node first (smallest clique)");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testEliminatesAllVariables() {
		ProbNet probNet = TestNetworks.buildDiamond();
		minimalCliqueSize heuristic = createHeuristic(probNet);

		List<String> order = eliminateAll(heuristic, probNet);
		assertEquals(4, order.size(), "Should eliminate all 4 diamond variables");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testAsiaNetworkComplete() {
		ProbNet probNet = TestNetworks.buildAsia();
		minimalCliqueSize heuristic = createHeuristic(probNet);

		List<String> order = eliminateAll(heuristic, probNet);
		assertEquals(8, order.size(), "Should eliminate all 8 Asia variables");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testPrefersSmallCliques() {
		ProbNet probNet = TestNetworks.buildAsia();
		minimalCliqueSize heuristic = createHeuristic(probNet);

		// VisitToAsia has only 1 neighbor (Tuberculosis), clique size = 2.
		// It should be among the first variables eliminated.
		Variable first = heuristic.getVariableToDelete();
		assertNotNull(first);
		Node firstNode = probNet.getNode(first);
		assertNotNull(firstNode, "Node should exist in the network for " + first.getName());
		int cliqueSize = firstNode.getNeighbors().size() + 1;
		assertTrue(cliqueSize <= 3,
				"First eliminated variable should have a small clique (got size " + cliqueSize + ")");
	}

	private minimalCliqueSize createHeuristic(ProbNet probNet) {
		List<List<Variable>> variablesToEliminate = new ArrayList<>();
		variablesToEliminate.add(new ArrayList<>(probNet.getVariables()));
		return new minimalCliqueSize(probNet, variablesToEliminate);
	}

	private List<String> eliminateAll(EliminationHeuristic heuristic, ProbNet probNet) {
		List<String> order = new ArrayList<>();
		Variable variable = heuristic.getVariableToDelete();
		while (variable != null) {
			order.add(variable.getName());
			RemoveNodeEdit edit = new RemoveNodeEdit(probNet, probNet.getNode(variable));
			heuristic.afterEditExecutes(edit);
			variable = heuristic.getVariableToDelete();
		}
		return order;
	}
}
