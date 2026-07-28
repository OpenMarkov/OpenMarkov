/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.inference;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.factory.DANFactory;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In a decision analysis network the order of the decisions is given by the directed paths of the
 * graph: a decision that is an ancestor of all the others is the first one to be made, and decisions
 * with no directed path between them may be made in any order — cases 2 and 3 of the expansion
 * algorithm of Díez et al., <i>Decision analysis networks</i>, CISIAD-14-01, section III.
 *
 * <p>{@link PartialOrderDAN} keeps only the decisions, joins with an arrow every pair connected by a
 * path, and then removes the arrows that follow from the others — the transitive reduction. A
 * reduction has to preserve which decisions come before which; the one here did not, so the tests
 * below check both the shape of the order on three known networks and that property in general.
 *
 * <p>This class used to hold a single empty test: its body was commented out because the network it
 * needed did not build.
 *
 * @author manolo
 */
public class PartialOrderDANTest {

	private static List<String> orderOf(ProbNet dan) {
		return new PartialOrderDAN(dan).getOrder()
									   .getLinks()
									   .stream()
									   .map(link -> link.getFrom().getName() + " -> " + link.getTo().getName())
									   .sorted()
									   .toList();
	}

	/**
	 * The network is built with the links {T0, T1} → {T2, T3, T4} and T2 → {T3, T4}, so the three
	 * phases its name promises are {T0, T1} &lt; {T2} &lt; {T3, T4}.
	 *
	 * <p>The reduction used to leave only the two arrows of the first phase: it walked the links
	 * <em>incident</em> to each decision, incoming ones included, and read the end of each as if it
	 * were a successor. For an incoming link that end is the decision itself, so every outgoing link
	 * of a decision that had somebody before it was deleted.
	 */
	@Test
	public void theThreePhasesNetworkHasThreePhases() {
		assertEquals(List.of("Dec: Test 0 -> Dec: Test 2",
						"Dec: Test 1 -> Dec: Test 2",
						"Dec: Test 2 -> Dec: Test 3",
						"Dec: Test 2 -> Dec: Test 4"),
				orderOf(DANFactory.buildThreePhasesOfTestsDAN()));
	}

	/**
	 * Figure 1 of the paper, whose caption says there is no constraint on the order of the tests:
	 * both precede the therapy and neither precedes the other, which is the case that puts the
	 * meta-decision node OD in the equivalent decision tree.
	 */
	@Test
	public void theTwoTestsOfTheDiabetesNetworkAreUnordered() {
		assertEquals(List.of("Dec: Blood Test -> Therapy", "Dec: Urine test -> Therapy"),
				orderOf(DANFactory.buildDiabetesDAN()));
	}

	/** Figure 3 of the paper: whether to test comes before which reactor to build. */
	@Test
	public void theReactorNetworkDecidesTheTestBeforeTheBuilding() {
		assertEquals(List.of("Test decision -> Build decision"), orderOf(DANFactory.buildReactorDAN()));
	}

	/**
	 * The property that defines a transitive reduction, checked on the three networks: it may drop an
	 * arrow that follows from the others, but a decision that came before another must still come
	 * before it.
	 */
	@Test
	public void theReductionKeepsEveryPrecedence() {
		for (ProbNet dan : List.of(DANFactory.buildThreePhasesOfTestsDAN(),
				DANFactory.buildDiabetesDAN(),
				DANFactory.buildReactorDAN())) {
			ProbNet reducedOrder = new PartialOrderDAN(dan).getOrder();
			for (Node first : dan.getNodes(NodeType.DECISION)) {
				for (Node second : dan.getNodes(NodeType.DECISION)) {
					if (first == second) {
						continue;
					}
					boolean precedesInTheNetwork = dan.existsPath(first, second, true, Collections.emptyList());
					if (precedesInTheNetwork) {
						assertTrue(pathInOrder(reducedOrder, first, second),
								first.getName() + " comes before " + second.getName()
										+ " in the network, but not in the reduced order: " + orderOf(dan));
					}
				}
			}
		}
	}

	/** And it must not invent a precedence that the network does not have. */
	@Test
	public void theReductionInventsNoPrecedence() {
		ProbNet dan = DANFactory.buildDiabetesDAN();
		ProbNet reducedOrder = new PartialOrderDAN(dan).getOrder();

		Node bloodTest = dan.getNode("Dec: Blood Test");
		Node urineTest = dan.getNode("Dec: Urine test");

		assertFalse(pathInOrder(reducedOrder, bloodTest, urineTest));
		assertFalse(pathInOrder(reducedOrder, urineTest, bloodTest));
	}

	private static boolean pathInOrder(ProbNet order, Node from, Node to) {
		Node fromInOrder = order.getNode(from.getVariable());
		Node toInOrder = order.getNode(to.getVariable());
		return order.existsPath(fromInOrder, toInOrder, true, Collections.<Link<Node>>emptyList());
	}
}
