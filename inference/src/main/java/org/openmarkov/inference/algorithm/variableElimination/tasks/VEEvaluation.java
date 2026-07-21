/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination.tasks;

import org.apache.logging.log4j.LogManager;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.inference.tasks.Evaluation;
import org.openmarkov.core.inference.tasks.OptimalPolicies;
import org.openmarkov.core.inference.tasks.TaskUtilities;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.StrategyCarrier;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.inference.algorithm.variableElimination.VariableEliminationCore;

import java.util.*;

/**
 * Task: Evaluation
 * <p>
 * Input: a symmetric network (usually containing decisions and utility nodes)
 * Optional input: pre-resolution evidence (E), imposed policies,
 * and observable variables (O) [for the evaluation of DANs]
 * </p>
 * Output: the global expected utility U(E,O), the probability P(E,O),
 * and the optimal policies (a table for each decision)
 *
 * @author mluque
 * @author fjdiez
 * @author Manuel Arias
 * @author jperez-martin
 * @author artasom
 */

public class VEEvaluation extends VariableElimination implements Evaluation, OptimalPolicies {

	private VariableEliminationCore variableEliminationCore = null;
	private Variable decisionVariable;
	
	public VEEvaluation(ProbNet network) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		super(network);
	}

	/**
	 * Preprocess the network and run the algorithm
	 *
	 * @throws IncompatibleEvidenceException Incompatible evidence exception
     */
	private void resolve()
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		LogManager.getLogger(getClass()).trace("Resolving VEEvaluation");

		generalPreprocessing();
		unicriterionPreprocess();
		exactAlgorithmsPreprocessing();

		ProbNet markovNetworkInference = TaskUtilities
				.projectTablesAndBuildMarkovDecisionNetwork(probNet, getPreResolutionEvidence());

		// Build list of variables to eliminate
		List<Variable> variablesToEliminate = markovNetworkInference.getChanceAndDecisionVariables();

		// If the user sets a decision variable, remove from variables to eliminate
		if (decisionVariable != null) {
			variablesToEliminate.remove(decisionVariable);
		}

		// Create heuristic instance
		EliminationHeuristic heuristic = heuristicFactory(probNet, new ArrayList<>(),
				getPreResolutionEvidence().getVariables(), getConditioningVariables(), variablesToEliminate);

		variableEliminationCore = new VariableEliminationCore(markovNetworkInference, heuristic, true);
	}

	@Override public TablePotential getProbability()
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		if (variableEliminationCore == null) {
			resolve();
		}
		return variableEliminationCore.getProbability();
	}

	@Override public TablePotential getUtility()
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		if (variableEliminationCore == null) {
			resolve();
		}
		return variableEliminationCore.getUtility();
	}

	@Override public StrategyTree getOptimalStrategyTree()
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		if (variableEliminationCore == null) {
			resolve();
		}
		TablePotential utility = variableEliminationCore.getUtility();
		return ((StrategyCarrier) utility).getStrategyTrees()[0];
	}

	@Override public HashMap<Variable, Potential> getOptimalPolicies()
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		if (variableEliminationCore == null) {
			resolve();
		}

		//noinspection unchecked
		HashMap<Variable, Potential> optimalPolicies = (HashMap<Variable, Potential>) variableEliminationCore.getOptimalPolicies();
		
		// Set uniform policy to those nodes without policy calculated by the inference algorithm (and without imposed policy)
		for (Variable dec: probNet.getVariables(NodeType.DECISION)) {
			if (!optimalPolicies.containsKey(dec) && !TaskUtilities.hasImposedPolicy(probNet, dec)) {
				Potential pot = new UniformPotential(Collections.singletonList(dec),PotentialRole.CONDITIONAL_PROBABILITY);
				optimalPolicies.put(dec, pot);
			}
		}
		
		return optimalPolicies;
	}

	@Override public Potential getOptimalPolicy(Variable decision)
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		getOptimalPolicies();
		return variableEliminationCore.getOptimalPolicy(decision);
	}

	public void setDecisionVariable(Variable decisionVariable) {
		this.decisionVariable = decisionVariable;
	}

}