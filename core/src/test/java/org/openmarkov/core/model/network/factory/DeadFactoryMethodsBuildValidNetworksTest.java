/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.factory;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Two factory methods nobody calls today, and that therefore nobody noticed were broken. They are
 * kept, rather than deleted, so that they are ready the day somebody wants the networks they build.
 *
 * @author Manuel Arias
 */
class DeadFactoryMethodsBuildValidNetworksTest {

	/**
	 * The super-value potential started its list of variables with {@code Arrays.asList}, which is
	 * backed by an array and so of fixed size, and then added the parents to it: every call threw
	 * {@code UnsupportedOperationException}.
	 */
	@Test
	void theInfluenceDiagramWithASuperValueNodeCanBeBuilt() {
		ProbNet id = assertDoesNotThrow(IDFactory::buildIDSVDecideTestSymptom);

		assertNotNull(id);
		assertTrue(id.getNodes(NodeType.UTILITY).size() > 1,
				"The network is built around a super-value node adding up several utilities");
		assertEachNodeOwnsItsPotential(id);
	}

	/**
	 * The three-phases network added the <em>result</em> variable a second time as a utility node
	 * instead of the <em>cost</em> variable. Since a utility variable is numeric, that turned the test
	 * result — a variable with two states, negative and positive — into a numeric variable with a
	 * single empty state, while its conditional probability table still declared eight values. The
	 * network also came out with two nodes called "Test Result 0", which its own mandatory constraint
	 * reported: {@code getUnsatisfiedConstraints()} returned "Variables have distinct names".
	 */
	@Test
	void theThreePhasesOfTestsNetworkIsWellFormed() {
		ProbNet dan = DANFactory.buildThreePhasesOfTestsDAN();

		List<String> names = dan.getNodes().stream().map(Node::getName).sorted().toList();
		assertEquals(names.size(), names.stream().distinct().count(), "Two nodes share a name: " + names);
		assertEquals(List.of(), dan.getUnsatisfiedConstraints(), "The network does not satisfy its own constraints");
		assertEachNodeOwnsItsPotential(dan);
	}

	/** A test result is a binary variable, and adding it as a utility node used to make it numeric. */
	@Test
	void theTestResultsOfTheThreePhasesNetworkKeepTheirTwoStates() {
		ProbNet dan = DANFactory.buildThreePhasesOfTestsDAN();

		List<Node> testResults = dan.getNodes(NodeType.CHANCE)
									.stream()
									.filter(node -> node.getName().startsWith("Test Result"))
									.toList();

		assertEquals(5, testResults.size());
		for (Node testResult : testResults) {
			assertEquals(2, testResult.getVariable().getNumStates(),
					testResult.getName() + " lost its states: " + List.of(testResult.getVariable().getStates()));
		}
	}

	/** Every chance node needs a probability, and every utility node a utility. */
	@Test
	void everyChanceAndUtilityNodeOfTheThreePhasesNetworkHasAPotential() {
		ProbNet dan = DANFactory.buildThreePhasesOfTestsDAN();

		List<String> withoutPotential = dan.getNodes()
										   .stream()
										   .filter(node -> node.getNodeType() != NodeType.DECISION)
										   .filter(node -> node.getPotentials().isEmpty())
										   .map(Node::getName)
										   .sorted()
										   .collect(Collectors.toList());

		assertEquals(List.of(), withoutPotential, "These nodes have no potential at all");
	}

	/**
	 * The conditioned variable of a potential is the first one, by convention, and it has to be the
	 * variable of the node the potential hangs from. The cost potential was declared over the cost
	 * variable and hung from the node of the test result.
	 */
	private static void assertEachNodeOwnsItsPotential(ProbNet probNet) {
		for (Node node : probNet.getNodes()) {
			for (Potential potential : node.getPotentials()) {
				if (potential.getVariables().isEmpty()) {
					continue;
				}
				Variable conditioned = potential.getVariable(0);
				assertEquals(node.getVariable(), conditioned,
						"The potential of node " + node.getName() + " starts with " + conditioned.getName());
			}
		}
	}
}
