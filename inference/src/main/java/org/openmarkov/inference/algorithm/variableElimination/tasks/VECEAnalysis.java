/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination.tasks;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.inference.tasks.CEAnalysis;
import org.openmarkov.core.inference.tasks.TaskUtilities;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.inference.algorithm.variableElimination.VariableEliminationCore;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jperez-martin
 */
public class VECEAnalysis extends VariableElimination implements CEAnalysis {

	private boolean solved = false;

	private Variable decision;

	private GTablePotential utility;

	private TablePotential probability;

	/**
	 * @param network a symmetric network having at least two criteria (and usually decisions and utility nodes)
	 */
	public VECEAnalysis(ProbNet network) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		super(network);
	}
    
    private void resolve() throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		generalPreprocessing();
		bicriteriaPreprocess();
		exactAlgorithmsPreprocessing();

		ProbNet markovNetworkInference = TaskUtilities
				.projectTablesAndBuildMarkovDecisionNetwork(probNet, getPreResolutionEvidence());

		// Build list of variables to eliminate
		List<Variable> variablesToEliminate = probNet.getChanceAndDecisionVariables();
		// And remove the received decision from them
		if (decision != null) {
			variablesToEliminate.remove(decision);
		}

		// Create heuristic instance
		EliminationHeuristic heuristic = heuristicFactory(markovNetworkInference, new ArrayList<>(),
				getPreResolutionEvidence().getVariables(), getConditioningVariables(), variablesToEliminate);

		VariableEliminationCore variableEliminationCore = new VariableEliminationCore(markovNetworkInference, heuristic,
				false);

		utility = variableEliminationCore.getUtility();
		probability = variableEliminationCore.getProbability();

		// The following lines are a kludge for the exceptional case in which utility does not depend on the decision
		if (decision != null && utility.elementTable.size() != decision.getNumStates()) {
			int diff = decision.getNumStates() - utility.elementTable.size();
			for (int i = 0; i < diff; i++) {
			   utility.elementTable.add(utility.elementTable.get(0));
			}
		}

		solved = true;
	}
    
    @Override
    public GTablePotential getUtility() throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		if (!solved) {
			resolve();
		}
		return utility;
	}

	@Override public TablePotential getProbability()
            throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		if (!solved) {
			resolve();
		}
		return probability;
	}

	@Override public void setDecisionVariable(Variable decisionVariable) {
		this.decision = decisionVariable;
	}
    
    @Override
    public CEP getCEP() throws NonProjectablePotentialException, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
		if (!solved) {
			resolve();
		}
		return (CEP) getUtility().elementTable.get(0);
	}
}
