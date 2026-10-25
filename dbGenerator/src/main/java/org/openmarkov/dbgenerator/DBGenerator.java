/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.dbgenerator;

import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ColumnsThatDoNotAddUpToOne;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Generates synthetic case databases by forward sampling from a Bayesian network.
 * Nodes are sampled in topological order so that parent values are always available.
 */
public class DBGenerator  {

	/**
	 * Generates a database of sampled cases from the given network using forward sampling.
	 * The random generator is unseeded, so successive calls produce different databases.
	 *
	 * @param probNet       the Bayesian network to sample from
	 * @param numberOfCases the number of cases to generate
	 * @return a {@link CaseDatabase} containing the sampled cases
	 * @throws NodeCannotBeSampled if a node of the network cannot be sampled
	 */
	public static CaseDatabase generate(ProbNet probNet, int numberOfCases) throws NodeCannotBeSampled {
		return generate(probNet, numberOfCases, new Random());
	}

	/**
	 * Generates a database of sampled cases from the given network using forward sampling with a
	 * fixed seed. Two calls with the same seed, network and number of cases produce identical
	 * databases, which makes the generation reproducible.
	 *
	 * @param probNet       the Bayesian network to sample from
	 * @param numberOfCases the number of cases to generate
	 * @param seed          the seed for the random generator
	 * @return a {@link CaseDatabase} containing the sampled cases
	 * @throws NodeCannotBeSampled if a node of the network cannot be sampled
	 */
	public static CaseDatabase generate(ProbNet probNet, int numberOfCases, long seed) throws NodeCannotBeSampled {
		return generate(probNet, numberOfCases, new Random(seed));
	}

	/**
	 * @return the number of cases in {@code typed}, ignoring the blanks around it
	 *
	 * @throws InvalidArgumentException if it is not a whole number greater than 0
	 */
	public static int numberOfCases(String typed) {
		int numberOfCases;
		try {
			numberOfCases = Integer.parseInt(typed.strip());
		} catch (NumberFormatException notAWholeNumber) {
			numberOfCases = 0;
		}
		if (numberOfCases <= 0) {
			throw new InvalidArgumentException(typed, "number of cases", "it must be a whole number greater than 0");
		}
		return numberOfCases;
	}

	/**
	 * @throws NodeCannotBeSampled for the first node, in the order of sampling, that is not a chance node or has no
	 *                             relation
	 */
	public static void checkCanBeSampled(ProbNet probNet) throws NodeCannotBeSampled {
		for (Node node : ProbNetOperations.sortTopologically(probNet)) {
			if (node.getNodeType() != NodeType.CHANCE || node.getPotentials().isEmpty()) {
				throw new NodeCannotBeSampled(node.getName());
			}
		}
	}

	private static CaseDatabase generate(ProbNet probNet, int numberOfCases, Random randomGenerator)
			throws NodeCannotBeSampled {
		checkCanBeSampled(probNet);
		List<Node> nodes = probNet.getNodes();
		int[][] cases = new int[numberOfCases][nodes.size()];
		List<Node> sortedNodes = ProbNetOperations.sortTopologically(probNet);
		// Columns off by rounding are normalized in a copy, as inference does
		ProbNet normalized = probNet.copy();
		ColumnsThatDoNotAddUpToOne.normalizeWithinTolerance(normalized);
		List<Integer> sortedNodeIndexes = new ArrayList<>();
		for (Node node : sortedNodes) {
			sortedNodeIndexes.add(nodes.indexOf(node));
		}
		for (int i = 0; i < numberOfCases; ++i) {
			HashMap<Variable, Integer> sampledStateIndexes = new HashMap<Variable, Integer>();
			
			for (int j = 0; j < sortedNodeIndexes.size(); ++j) {
				Node node = sortedNodes.get(j);
				int sampledIndex;
				try {
					sampledIndex = normalized.getNode(node.getVariable()).getPotentials().get(0)
							.sampleConditionedVariable(randomGenerator, sampledStateIndexes);
				} catch (UnsupportedOperationException cannotBeSampled) {
					throw new NodeCannotBeSampled(node.getName());
				}
				sampledStateIndexes.put(node.getVariable(), sampledIndex);
				cases[i][sortedNodeIndexes.get(j)] = sampledIndex;
			}
		}
		return new CaseDatabase(probNet.getVariables(), cases);
	}
	
}
