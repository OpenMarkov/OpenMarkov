/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.heuristic.minimalFillIn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.RemoveNodeEdit;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MinimalFillIn heuristic.
 * Restored and extended from the original commented-out test.
 *
 * @author Manuel Arias
 */
public class MinimalFillInTest {

	private ProbNet probNet;
	private MinimalFillIn heuristic;

	/**
	 * Reconstruction of the original test network:
	 * 7 nodes with undirected links forming a complex graph.
	 */
	private ProbNet buildOriginalTestNet() {
		ProbNet net = new ProbNet();
		String[] names = {"A", "B", "C", "D", "E", "F", "G"};
		for (String name : names) {
			Variable v = new Variable(name, "positive", "negative");
			Node node = net.addNode(v, NodeType.CHANCE);
			TablePotential pot = new TablePotential(
					List.of(v), PotentialRole.CONDITIONAL_PROBABILITY);
			pot.setValues(new double[]{0.5, 0.5});
			node.setPotential(pot);
		}
		net.addLink(net.getVariable("A"), net.getVariable("B"), false);
		net.addLink(net.getVariable("B"), net.getVariable("C"), false);
		net.addLink(net.getVariable("C"), net.getVariable("D"), false);
		net.addLink(net.getVariable("D"), net.getVariable("E"), false);
		net.addLink(net.getVariable("E"), net.getVariable("F"), false);
		net.addLink(net.getVariable("E"), net.getVariable("G"), false);
		net.addLink(net.getVariable("C"), net.getVariable("G"), false);
		net.addLink(net.getVariable("E"), net.getVariable("C"), false);
		net.addLink(net.getVariable("G"), net.getVariable("B"), false);
		return net;
	}

	@BeforeEach
	public void setUp() {
		probNet = buildOriginalTestNet();
		List<List<Variable>> variablesToEliminate = new ArrayList<>();
		variablesToEliminate.add(new ArrayList<>(probNet.getVariables()));
		heuristic = new MinimalFillIn(probNet, variablesToEliminate);
	}

	/**
	 * Restoration of the original test: verifies the exact elimination order
	 * for the 7-node network with the MinimalFillIn heuristic.
	 */
	@Tag(TestSpeed.FAST)
	@Test
	public void testEliminationOrder() {
		List<String> order = eliminateAll(heuristic);

		assertEquals(7, order.size());
		assertEquals("A", order.get(0));
		assertEquals("B", order.get(1));
		assertEquals("D", order.get(2));
		assertEquals("C", order.get(3));
		assertEquals("F", order.get(4));
		assertEquals("E", order.get(5));
		assertEquals("G", order.get(6));
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testWithAsiaNetwork() {
		probNet = TestNetworks.buildAsia();
		List<List<Variable>> variablesToEliminate = new ArrayList<>();
		variablesToEliminate.add(new ArrayList<>(probNet.getVariables()));
		heuristic = new MinimalFillIn(probNet, variablesToEliminate);

		List<String> order = eliminateAll(heuristic);
		assertEquals(8, order.size(), "Should eliminate all 8 Asia variables");
	}

	@Tag(TestSpeed.FAST)
	@Test
	public void testChainProducesNoFillIn() {
		probNet = TestNetworks.buildChain3();
		List<List<Variable>> variablesToEliminate = new ArrayList<>();
		variablesToEliminate.add(new ArrayList<>(probNet.getVariables()));
		heuristic = new MinimalFillIn(probNet, variablesToEliminate);

		// In chain A->B->C, eliminating A or C produces no fill-in.
		Variable first = heuristic.getVariableToDelete();
		assertNotNull(first);
		assertTrue(first.getName().equals("A") || first.getName().equals("C"),
				"In a chain, should start with an extreme node (0 fill-in)");
	}

	private List<String> eliminateAll(EliminationHeuristic h) {
		List<String> order = new ArrayList<>();
		Variable variable = h.getVariableToDelete();
		while (variable != null) {
			order.add(variable.getName());
			RemoveNodeEdit edit = new RemoveNodeEdit(probNet, probNet.getNode(variable));
			h.afterEditExecutes(edit);
			variable = h.getVariableToDelete();
		}
		return order;
	}
}
