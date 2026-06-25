/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.heuristic.weightedMinFill;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.RemoveNodeEdit;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the WeightedMinFill heuristic.
 *
 * @author Manuel Arias
 */
public class WeightedMinFillTest {

	@Tag(TestSpeed.FAST)
	@Test
	public void testEliminatesAllVariables() {
		ProbNet probNet = TestNetworks.buildDiamond();
		WeightedMinFill heuristic = createHeuristic(probNet);

		List<String> order = eliminateAll(heuristic, probNet);
		assertEquals(4, order.size(), "Should eliminate all 4 diamond variables");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testAsiaNetworkComplete() {
		ProbNet probNet = TestNetworks.buildAsia();
		WeightedMinFill heuristic = createHeuristic(probNet);

		List<String> order = eliminateAll(heuristic, probNet);
		assertEquals(8, order.size(), "Should eliminate all 8 Asia variables");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testChainEliminatesExtremeFirst() {
		ProbNet probNet = TestNetworks.buildChain3();
		WeightedMinFill heuristic = createHeuristic(probNet);

		// In A->B->C, eliminating A or C has cost 0 (no fill-in).
		Variable first = heuristic.getVariableToDelete();
		assertNotNull(first);
		assertTrue(first.getName().equals("A") || first.getName().equals("C"),
				"In a chain, should start with an extreme node (cost 0)");
	}

	private WeightedMinFill createHeuristic(ProbNet probNet) {
		List<List<Variable>> variablesToEliminate = new ArrayList<>();
		variablesToEliminate.add(new ArrayList<>(probNet.getVariables()));
		return new WeightedMinFill(probNet, variablesToEliminate);
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
