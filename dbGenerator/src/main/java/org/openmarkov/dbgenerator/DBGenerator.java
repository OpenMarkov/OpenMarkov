/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.dbgenerator;

import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.Variable;

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
	 *
	 * @param probNet       the Bayesian network to sample from
	 * @param numberOfCases the number of cases to generate
	 * @return a {@link CaseDatabase} containing the sampled cases
	 */
	public static CaseDatabase generate(ProbNet probNet, int numberOfCases) {
		List<Node> nodes = probNet.getNodes();
		int[][] cases = new int[numberOfCases][nodes.size()];
		Random randomGenerator = new Random();
		List<Node> sortedNodes = ProbNetOperations.sortTopologically(probNet);
		List<Integer> sortedNodeIndexes = new ArrayList<>();
		for (Node node : sortedNodes) {
			sortedNodeIndexes.add(nodes.indexOf(node));
		}
		for (int i = 0; i < numberOfCases; ++i) {
			HashMap<Variable, Integer> sampledStateIndexes = new HashMap<Variable, Integer>();
			
			for (int j = 0; j < sortedNodeIndexes.size(); ++j) {
				Node node = sortedNodes.get(j);
				int sampledIndex = node.getPotentials().get(0).sampleConditionedVariable(randomGenerator, sampledStateIndexes);
				sampledStateIndexes.put(node.getVariable(), sampledIndex);
				cases[i][sortedNodeIndexes.get(j)] = sampledIndex;
			}
		}
		return new CaseDatabase(probNet.getVariables(), cases);
	}
	
}
