/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.heuristic.timeSliceElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.RemoveNodeEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the TimeSliceElimination heuristic.
 * Verifies that variables are eliminated from the last time slice
 * towards the first.
 *
 * @author Manuel Arias
 */
public class TimeSliceEliminationTest {

	/**
	 * Builds a simple temporal network: X[0] -> X[1] -> X[2]
	 * where each variable has an assigned timeSlice.
	 */
	private ProbNet buildTemporalChain() {
		ProbNet probNet = new ProbNet();

		Variable x0 = new Variable("X", "x0", "x1");
		x0.setBaseName("X");
		x0.setTimeSlice(0);

		Variable x1 = new Variable("X", "x0", "x1");
		x1.setBaseName("X");
		x1.setTimeSlice(1);

		Variable x2 = new Variable("X", "x0", "x1");
		x2.setBaseName("X");
		x2.setTimeSlice(2);

		Node node0 = probNet.addNode(x0, NodeType.CHANCE);
		Node node1 = probNet.addNode(x1, NodeType.CHANCE);
		Node node2 = probNet.addNode(x2, NodeType.CHANCE);

		probNet.makeLinksExplicit(false);
		probNet.addLink(node0, node1, true);
		probNet.addLink(node1, node2, true);

		// P(X[0])
		TablePotential pot0 = new TablePotential(
				List.of(x0), PotentialRole.CONDITIONAL_PROBABILITY);
		pot0.setValues(new double[]{0.5, 0.5});
		node0.setPotential(pot0);

		// P(X[1]|X[0])
		TablePotential pot1 = new TablePotential(
				Arrays.asList(x1, x0), PotentialRole.CONDITIONAL_PROBABILITY);
		pot1.setValues(new double[]{0.7, 0.3, 0.4, 0.6});
		node1.setPotential(pot1);

		// P(X[2]|X[1])
		TablePotential pot2 = new TablePotential(
				Arrays.asList(x2, x1), PotentialRole.CONDITIONAL_PROBABILITY);
		pot2.setValues(new double[]{0.8, 0.2, 0.3, 0.7});
		node2.setPotential(pot2);

		return probNet;
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testEliminatesLastSliceFirst() {
		ProbNet probNet = buildTemporalChain();
		List<List<Variable>> variablesToEliminate = new ArrayList<>();
		variablesToEliminate.add(new ArrayList<>(probNet.getVariables()));
		TimeSliceElimination heuristic = new TimeSliceElimination(probNet, variablesToEliminate);

		// The first variable to eliminate should be from the highest slice (slice 2)
		Variable first = heuristic.getVariableToDelete();
		assertNotNull(first);
		assertEquals(2, first.getTimeSlice(),
				"Should eliminate the variable from the highest time slice first");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testEliminatesAllVariables() {
		ProbNet probNet = buildTemporalChain();
		List<List<Variable>> variablesToEliminate = new ArrayList<>();
		variablesToEliminate.add(new ArrayList<>(probNet.getVariables()));
		TimeSliceElimination heuristic = new TimeSliceElimination(probNet, variablesToEliminate);

		List<Variable> order = new ArrayList<>();
		Variable variable = heuristic.getVariableToDelete();
		while (variable != null) {
			order.add(variable);
			RemoveNodeEdit edit = new RemoveNodeEdit(probNet, probNet.getNode(variable));
			heuristic.afterEditExecutes(edit);
			variable = heuristic.getVariableToDelete();
		}

		assertEquals(3, order.size(), "Should eliminate all 3 temporal variables");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testEliminationOrderIsDescendingBySlice() {
		ProbNet probNet = buildTemporalChain();
		List<List<Variable>> variablesToEliminate = new ArrayList<>();
		variablesToEliminate.add(new ArrayList<>(probNet.getVariables()));
		TimeSliceElimination heuristic = new TimeSliceElimination(probNet, variablesToEliminate);

		List<Integer> sliceOrder = new ArrayList<>();
		Variable variable = heuristic.getVariableToDelete();
		while (variable != null) {
			sliceOrder.add(variable.getTimeSlice());
			RemoveNodeEdit edit = new RemoveNodeEdit(probNet, probNet.getNode(variable));
			heuristic.afterEditExecutes(edit);
			variable = heuristic.getVariableToDelete();
		}

		// Each slice should be eliminated before the previous one
		for (int i = 0; i < sliceOrder.size() - 1; i++) {
			assertTrue(sliceOrder.get(i) >= sliceOrder.get(i + 1),
					"Slices should be eliminated in descending order: " + sliceOrder);
		}
	}
}
