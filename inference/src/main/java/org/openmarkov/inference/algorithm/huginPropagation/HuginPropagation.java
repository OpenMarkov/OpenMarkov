/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.huginPropagation;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.inference.annotation.InferenceAnnotation;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.PNConstraint;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.NetworkType;

import java.util.ArrayList;
import java.util.List;

/**
 * Create objects for Hugin algorithm inference. This class is nearly empty
 * because the algorithm is implemented in {@code HuginForest} construction
 * and in {@code openmarkov.inference.ClusterPropagation}.
 *
 * @author Manuel Arias
 * @author fjdiez
 * @version 1.0
 * @since OpenMarkov 1.0
 */
@InferenceAnnotation(name = "HuginPropagation") public class HuginPropagation extends ClusterPropagation {
	// Constructor

	/**
     * @param probNet {@code ProbNet}
	 * @throws NotEvaluableNetworkException NotEvaluableNetworkException
	 */
    public HuginPropagation(ProbNet probNet) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		super(probNet);
	}

	/// Is it deprecated?
	/*
	public static void checkEvaluability(ProbNet probNet) throws NotEvaluableNetworkException {
		if (!probNet.getNetworkType().equals(BayesianNetworkType.getUniqueInstance()))
			throw new NotEvaluableNetworkException("Hugin propagation can currently only evaluate Bayesian networks.");
	}
	*/

	/// Why doesn't HuginPropagation needs to implement setPostResolutionEvidence from Propagation

	/// Proposal of implementation
	@Override protected List<NetworkType> getPossibleNetworkTypes() {
		ArrayList<NetworkType> possibleNetworkTypes = new ArrayList<>();
		possibleNetworkTypes.add(BayesianNetworkType.getUniqueInstance());
		return possibleNetworkTypes;
	}


	@Override protected List<PNConstraint> getAdditionalConstraints() {
		return null;
	}

	// Methods from clusterPropagation
	@Override

	protected ClusterForest createForest(ProbNet markovNet,
			EliminationHeuristic heuristic, List<Node> queryNodes) // [Iago] maybe name queryNodes as variablesOfInterest
    {
		return new HuginForest(markovNet, heuristic, queryNodes);
	}

	@Override
	protected ClusterForest createForest(ProbNet markovNet, EliminationHeuristic heuristic) {
		return new HuginForest(markovNet, heuristic);
	}

	/**
	 * Creates a HuginForest for a MarkovNet.
	 *
     * @param markovNet {@code ProbNet} This Markov network will be
	 *                  destroyed.
     * @return A {@code HuginForest}
     */
    protected ClusterForest createForest(ProbNet markovNet) {
		return new HuginForest(markovNet, heuristic);
	}

	/// Hugin propagation only applies to Bayesian networks, so there is no decision strategy.
    public static StrategyTree getOptimalStrategy() {
		throw new UnsupportedOperationException(
				"getOptimalStrategy not applicable to Hugin propagation (Bayesian networks only)");
	}

	@Override
	public void setVariablesOfInterest(List<Variable> variablesOfInterest) {

	}
}
