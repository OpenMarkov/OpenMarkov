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
import org.openmarkov.core.inference.tasks.ExpectedUtilityDecision;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.List;

/**
 * Task: resolution
 * <p>
 * Input: a symmetric network (usually containing decisions and utility nodes)
 * Optional input: pre-resolution evidence (E), imposed policies,
 * and observable variables (O) [for the evaluation of DANs]
 * <p>
 * Output: the global expected utility U(E,O), the probability P(E,O),
 * and the optimal policies (a table for each decision)
 *
 * @author mluque
 * @author fjdiez
 * @author Manuel Arias
 * @author jperez-martin
 * @author artasom
 */

public class VEExpectedUtilityDecision extends VariableElimination implements ExpectedUtilityDecision {

	private Variable decision;

	private TablePotential result;

	/**
	 * @param network  Probabilistic network to be resolved
	 * @param decision The variable for which the expected utility is required
	 */
	public VEExpectedUtilityDecision(ProbNet network, Variable decision) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		super(network);
		this.decision = decision;
	}

	private void resolve()
			throws NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ConstraintViolatedException {
		List<Variable> informationalPredecesors = ProbNetOperations.getInformationalPredecessors(probNet, decision);
		List<Variable> orderedVariables = new ArrayList<>(informationalPredecesors);
		orderedVariables.remove(decision);
		orderedVariables.add(0, decision);

		VEEvaluation veEvaluation = new VEEvaluation(probNet);
		veEvaluation.setConditioningVariables(informationalPredecesors);

		result = veEvaluation.getUtility().reorder(orderedVariables);
	}

	@Override public TablePotential getExpectedUtility()
			throws NonProjectablePotentialException, NotEvaluableNetworkException.NotApplicableNetwork, IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ConstraintViolatedException {
		if (result == null) {
			resolve();
		}

		return result;
	}
}