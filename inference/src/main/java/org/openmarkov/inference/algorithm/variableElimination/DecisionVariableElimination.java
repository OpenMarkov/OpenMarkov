/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
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

	/** Relative difference below which two probabilities are taken as equal. */
	private static final double TOLERANCE = 1E-9;

	TablePotential projectedProbability;
	Potential utility;
	TablePotential optimalPolicy;
    
    /**
     * Eliminates a decision variable by maximizing the total utility over the decision's states
     * and computing the optimal policy. The options that the evidence makes impossible are left out,
     * and the probability that remains is that of the options chosen.
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
        TablePotential jointProbability = probPotentials.isEmpty() ? null : DiscretePotentialOperations.multiply(probPotentials);
		boolean probabilityDependsOnDecision = jointProbability != null && dependsOn(jointProbability, variableToDelete);

		if (jointProbability == null) {
            projectedProbability = DiscretePotentialOperations.createUnityProbabilityPotential();
		} else if (!probabilityDependsOnDecision) {
			projectedProbability = DiscretePotentialOperations.projectOutVariable(variableToDelete, jointProbability);
		}

		// maximize the utility potentials
		if (totalUtility instanceof GTablePotential gUtility) {
			if (probabilityDependsOnDecision) {
				gUtility = CEAlgebra.discardImpossibleOptions(gUtility, jointProbability);
				projectedProbability = highestProbability(variableToDelete, jointProbability);
			}
			utility = CEAlgebra.ceMaximize(gUtility, variableToDelete);
		} else if (totalUtility instanceof TablePotential tUtility) {
			if (probabilityDependsOnDecision) {
				tUtility = DiscretePotentialOperations.sum(tUtility, minusInfinityWhereImpossible(jointProbability, tUtility));
			}
			MaxOutVariable max = new MaxOutVariable(variableToDelete,
					DiscretePotentialOperations.createUnityProbabilityPotential(), tUtility);
			utility = max.getUtility();
			optimalPolicy = max.getPolicy();
			if (probabilityDependsOnDecision) {
				double[] values = ((TablePotential) utility).getValues();
				for (int i = 0; i < values.length; i++) {
					if (values[i] == Double.NEGATIVE_INFINITY) {
						values[i] = 0.0;
					}
				}
				projectedProbability = DiscretePotentialOperations
						.multiplyAndMarginalize(List.of(optimalPolicy, jointProbability), variableToDelete);
			}
		} else {
			throw new IllegalStateException(
					"Unsupported utility potential type in decision VE: " + totalUtility.getClass().getSimpleName());
		}
	}

	/** Whether the probability changes with the option of the decision, as it does with a finding that depends on it. */
	private static boolean dependsOn(TablePotential probability, Variable decision) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
		if (!probability.getVariables().contains(decision)) {
			return false;
		}
		double[] first = given(probability, decision, 0).getValues();
		for (int state = 1; state < decision.getNumStates(); state++) {
			double[] other = given(probability, decision, state).getValues();
			for (int i = 0; i < first.length; i++) {
				if (Math.abs(first[i] - other[i]) > TOLERANCE * Math.max(Math.abs(first[i]), Math.abs(other[i]))) {
					return true;
				}
			}
		}
		return false;
	}

	/** The probability when the decision takes the option {@code state}. */
	private static TablePotential given(TablePotential probability, Variable decision, int state) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
		EvidenceCase option = new EvidenceCase();
		option.addFinding(new Finding(decision, state));
		return probability.tableProject(option, null);
	}

	/** For each configuration, the highest probability among the options of the decision. */
	private static TablePotential highestProbability(Variable decision, TablePotential probability) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
		TablePotential highest = given(probability, decision, 0);
		for (int state = 1; state < decision.getNumStates(); state++) {
			double[] other = given(probability, decision, state).getValues();
			for (int i = 0; i < other.length; i++) {
				highest.getValues()[i] = Math.max(highest.getValues()[i], other[i]);
			}
		}
		return highest;
	}

	/** A utility that is zero where the probability is not, and minus infinity where it is: added to another, it rules out the impossible options. */
	private static TablePotential minusInfinityWhereImpossible(TablePotential probability, TablePotential utility) {
		TablePotential mask = new TablePotential(probability.getVariables(), PotentialRole.UNSPECIFIED);
		for (int i = 0; i < mask.getValues().length; i++) {
			mask.getValues()[i] = probability.getValues()[i] == 0.0 ? Double.NEGATIVE_INFINITY : 0.0;
		}
		mask.setCriterion(utility.getCriterion());
		return mask;
	}

	/**
	 * Returns the probability potential without the decision variable.
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
