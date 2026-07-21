/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.inference.algorithm.variableElimination.operation.CEAlgebra;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to sum out a chance variable given a set of potentials
 */
public class ChanceVariableElimination {

	final TablePotential marginalProbability;
	final List<Potential> utilityPotentials;

	/**
	 * Eliminates a chance variable by marginalizing (summing out) probability potentials and
	 * computing expected utility for each criterion.
	 *
	 * @param variableToDelete       the chance variable to sum out
	 * @param probPotentials         probability potentials that depend on the variable
	 * @param inputUtilityPotentials utility potentials that depend on the variable
	 */
	public ChanceVariableElimination(Variable variableToDelete, List<TablePotential> probPotentials,
                                     List<Potential> inputUtilityPotentials) {

		boolean hasGTablePotential = inputUtilityPotentials.stream().anyMatch(p -> p instanceof GTablePotential);
		List<Potential> utilityPotentialsByCriterion;
		if (hasGTablePotential) {
			utilityPotentialsByCriterion = new ArrayList<>(inputUtilityPotentials);
		} else {
			@SuppressWarnings("unchecked")
			List<TablePotential> tablePotentials = (List<TablePotential>)(List<?>) inputUtilityPotentials;
			utilityPotentialsByCriterion = new ArrayList<>(DiscretePotentialOperations.sumByCriterion(tablePotentials));
		}

		utilityPotentials = new ArrayList<>();

		if (inputUtilityPotentials.isEmpty()) {
			// add the marginal probability to the network
			marginalProbability = DiscretePotentialOperations.multiplyAndMarginalize(probPotentials, variableToDelete);
		} else {
			TablePotential jointProbability = DiscretePotentialOperations.multiply(probPotentials);
			marginalProbability = DiscretePotentialOperations.marginalize(jointProbability, variableToDelete);
			TablePotential conditionalProbability = DiscretePotentialOperations
					.divide(jointProbability, marginalProbability);
			//Set to 0 every NaN cell in conditionalProbability
			double[] conditionalProbabilityValues = conditionalProbability.getValues();
			for (int i = 0; i < conditionalProbabilityValues.length; i++) {
				if (Double.isNaN(conditionalProbabilityValues[i])) {
					conditionalProbabilityValues[i] = 0.0;
				}
			}

			List<Variable> orderedVariables = new ArrayList<>();
			orderedVariables.add(variableToDelete);
			for (Variable variable : conditionalProbability.getVariables()) {
				if (variable != variableToDelete) {
					orderedVariables.add(variable);
				}
			}
            if (!conditionalProbability.getVariables().isEmpty()) {
				conditionalProbability = conditionalProbability.reorder(orderedVariables);
			}

			// eliminate the chance variable as in the unicriterion case
			for (Potential utilityPotential : utilityPotentialsByCriterion) {
				if (utilityPotential instanceof GTablePotential gUtility) {
					utilityPotentials.add(CEAlgebra
							.multiplyAndMarginalize(conditionalProbability, gUtility, variableToDelete));
				} else if (utilityPotential instanceof TablePotential tUtility) {
					utilityPotentials.add(DiscretePotentialOperations
							.multiplyAndMarginalize(conditionalProbability, tUtility, variableToDelete));
				} else {
					throw new IllegalStateException(
							"Unsupported utility potential type in VE: " + utilityPotential.getClass().getSimpleName());
				}
			}
		}
	}

	/**
	 * Checks whether any value in the given potential falls outside the valid probability
	 * range [0, 1].
	 *
	 * @param pot the table potential to check (may be {@code null})
	 * @return {@code true} if any value is outside [0, 1], {@code false} otherwise
	 */
	public static boolean hasIncorrectProbability(TablePotential pot) {
		boolean isCorrect = true;
		if (pot != null) {
			double[] values = pot.getValues();
			for (int i = 0; i < values.length && isCorrect; i++) {
				double value = values[i];
				isCorrect = value >= 0.0 && value <= 1.0;
			}
		}
		return !isCorrect;
	}

	/**
	 * @return the marginal probability
	 */
	public TablePotential getMarginalProbability() {
		return marginalProbability;
	}

	/**
	 * @return a utility potential for each criterion
	 */
	public List<Potential> getUtilityPotentials() {
		return utilityPotentials;
	}

}
