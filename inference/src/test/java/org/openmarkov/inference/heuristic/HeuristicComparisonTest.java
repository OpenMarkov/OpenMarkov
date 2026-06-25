/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.heuristic;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.RemoveNodeEdit;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.heuristic.minimalCliqueSize.minimalCliqueSize;
import org.openmarkov.inference.heuristic.minimalFillIn.MinimalFillIn;
import org.openmarkov.inference.heuristic.rollout.RolloutElimination;
import org.openmarkov.inference.heuristic.simpleElimination.SimpleElimination;
import org.openmarkov.inference.heuristic.weightedMinFill.WeightedMinFill;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Compares all available elimination heuristics on the same networks,
 * verifying that each produces a valid elimination order (all variables
 * eliminated exactly once, no duplicates, no missing variables).
 *
 * @author Manuel Arias
 */
public class HeuristicComparisonTest {

	private record HeuristicEntry(String name,
			BiFunction<ProbNet, List<List<Variable>>, EliminationHeuristic> factory) {}

	private static final List<HeuristicEntry> HEURISTICS = List.of(
			new HeuristicEntry("SimpleElimination", SimpleElimination::new),
			new HeuristicEntry("MinimalFillIn", MinimalFillIn::new),
			new HeuristicEntry("WeightedMinFill", WeightedMinFill::new),
			new HeuristicEntry("minimalCliqueSize", minimalCliqueSize::new),
			new HeuristicEntry("RolloutElimination", RolloutElimination::new)
	);

	@Tag(TestSpeed.FAST)
	@Test
	public void testAllHeuristicsProduceValidOrderOnAsia() {
		for (HeuristicEntry entry : HEURISTICS) {
			ProbNet probNet = TestNetworks.buildAsia();
			assertValidOrder(entry, probNet, 8);
		}
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testAllHeuristicsProduceValidOrderOnDiamond() {
		for (HeuristicEntry entry : HEURISTICS) {
			ProbNet probNet = TestNetworks.buildDiamond();
			assertValidOrder(entry, probNet, 4);
		}
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testAllHeuristicsProduceValidOrderOnChain() {
		for (HeuristicEntry entry : HEURISTICS) {
			ProbNet probNet = TestNetworks.buildChain3();
			assertValidOrder(entry, probNet, 3);
		}
	}

	private void assertValidOrder(HeuristicEntry entry, ProbNet probNet, int expectedSize) {
		Set<String> originalNames = new HashSet<>();
		for (Variable v : probNet.getVariables()) {
			originalNames.add(v.getName());
		}

		List<List<Variable>> variablesToEliminate = new ArrayList<>();
		variablesToEliminate.add(new ArrayList<>(probNet.getVariables()));
		EliminationHeuristic heuristic = entry.factory.apply(probNet, variablesToEliminate);

		List<String> order = new ArrayList<>();
		Variable variable = heuristic.getVariableToDelete();
		while (variable != null) {
			order.add(variable.getName());
			RemoveNodeEdit edit = new RemoveNodeEdit(probNet, probNet.getNode(variable));
			heuristic.afterEditExecutes(edit);
			variable = heuristic.getVariableToDelete();
		}

		assertEquals(expectedSize, order.size(),
				entry.name + " should eliminate all " + expectedSize + " variables");

		Set<String> eliminatedNames = new HashSet<>(order);
		assertEquals(expectedSize, eliminatedNames.size(),
				entry.name + " should not eliminate any variable twice");
		assertEquals(originalNames, eliminatedNames,
				entry.name + " should eliminate exactly the original variables");
	}
}
