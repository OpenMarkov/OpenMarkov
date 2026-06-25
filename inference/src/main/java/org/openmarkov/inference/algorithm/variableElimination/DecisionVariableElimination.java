/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.potential.operation.MaxOutVariable;
import org.openmarkov.inference.algorithm.variableElimination.operation.CEAlgebra;

import java.util.List;

/**
 * Class used to maximize a set of potentials for a decision variable
 */
public class DecisionVariableElimination {

	TablePotential projectedProbability;
	Potential utility;
	TablePotential optimalPolicy;
    
    /**
     * Eliminates a decision variable by maximizing the total utility over the decision's states
     * and computing the optimal policy.
     *
     * @param variableToDelete       the decision variable to eliminate
     * @param probPotentials         probability potentials that depend on the variable
     * @param inputUtilityPotentials utility potentials that depend on the variable
     * @throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther if evidence is incompatible
     */
    public DecisionVariableElimination(Variable variableToDelete, List<TablePotential> probPotentials, List<Potential> inputUtilityPotentials) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
		// all the potentials have the same criterion
		Potential totalUtility;
		if (inputUtilityPotentials.size() == 1 && inputUtilityPotentials.get(0) instanceof GTablePotential) {
			totalUtility = inputUtilityPotentials.get(0);
		} else {
			@SuppressWarnings("unchecked")
			List<TablePotential> tablePotentials = (List<TablePotential>)(List<?>) inputUtilityPotentials;
			totalUtility = DiscretePotentialOperations.sum(tablePotentials);
		}
        TablePotential jointProbability;

		if (!probPotentials.isEmpty()) {
			jointProbability = DiscretePotentialOperations.multiply(probPotentials);
			projectedProbability = DiscretePotentialOperations.projectOutVariable(variableToDelete, jointProbability);
		} else {
            DiscretePotentialOperations.createUnityProbabilityPotential();
            projectedProbability = DiscretePotentialOperations.createUnityProbabilityPotential();
		}

		// maximize the utility potentials
		if (totalUtility instanceof GTablePotential gUtility) {
			utility = CEAlgebra.ceMaximize(gUtility, variableToDelete);
		} else if (totalUtility instanceof TablePotential tUtility) {
			MaxOutVariable max = new MaxOutVariable(variableToDelete,
					DiscretePotentialOperations.createUnityProbabilityPotential(), tUtility);
			utility = max.getUtility();
			optimalPolicy = max.getPolicy();
		} else {
			throw new IllegalStateException(
					"Unsupported utility potential type in decision VE: " + totalUtility.getClass().getSimpleName());
		}
	}

	/**
	 * Returns the probability potential after projecting out the decision variable.
	 *
	 * @return the projected probability potential
	 */
	public TablePotential getProjectedProbability() {
		return projectedProbability;
	}

	/**
	 * Returns the maximized utility potential after eliminating the decision variable.
	 *
	 * @return the maximized utility potential
	 */
	public Potential getUtility() {
		return utility;
	}

	/**
	 * Returns the optimal policy (deterministic or stochastic) for the eliminated decision variable.
	 *
	 * @return the optimal policy potential, or {@code null} for cost-effectiveness analysis
	 */
	public TablePotential getOptimalPolicy() {
		return optimalPolicy;
	}

}
