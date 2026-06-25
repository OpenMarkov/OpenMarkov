/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination.operation;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility methods for ordering and filtering potentials during variable elimination,
 * particularly for handling strategy-tree interventions.
 *
 * @author Manuel Arias
 */
public class VariableEliminationOperations {

	/**
	 * Orders utility potentials according to the partial order induced by the remaining
	 * decisions. Potentials with interventions for earlier decisions come first.
	 *
	 * @param utilityPotentialsVariable the utility potentials to order
	 * @param decisionsUntilEnd         the remaining decision variables in elimination order
	 * @return a list of table potentials ordered by the partial order of decisions
	 */
	public static List<TablePotential> orderPotentialsByPartialOrder(List<TablePotential> utilityPotentialsVariable,
			List<Variable> decisionsUntilEnd) {
		List<TablePotential> orderedPotentials = new ArrayList<>();
        Set<TablePotential> setOfInputPotentials = new HashSet<>(utilityPotentialsVariable);
		Set<TablePotential> potentialsWithoutIntervention = new HashSet<>();
		//Remove from inputPotentials the potentials without Interventions
		for (TablePotential inputPotential : setOfInputPotentials) {
			if (!VariableEliminationOperations.containsInterventions(inputPotential)) {
				potentialsWithoutIntervention.add(inputPotential);
			}
		}

		if (decisionsUntilEnd != null) {
			for (Variable decision : decisionsUntilEnd) {
                Set<TablePotential> potentialsWithDecisionInIntervention = VariableEliminationOperations.
                        getPotentialsWithDecisionInIntervention(decision, setOfInputPotentials);
				setOfInputPotentials.removeAll(potentialsWithDecisionInIntervention);
				orderedPotentials.addAll(potentialsWithDecisionInIntervention);
			}
		}

		orderedPotentials.addAll(potentialsWithoutIntervention);
		setOfInputPotentials.removeAll(potentialsWithoutIntervention);
		orderedPotentials.addAll(setOfInputPotentials);

		return orderedPotentials;
	}

	/**
	 * Checks whether the given table potential contains strategy-tree interventions.
	 *
	 * @param tablePotential the potential to check
	 * @return {@code true} if the potential has one or more interventions
	 */
	public static boolean containsInterventions(TablePotential tablePotential) {
		return tablePotential.hasInterventions();
	}

	/**
	 * Returns the subset of potentials that have a strategy-tree intervention for the given decision.
	 *
	 * @param decision   the decision variable to look for in interventions
	 * @param potentials the collection of potentials to filter
	 * @return potentials whose interventions reference the given decision
	 */
	public static Set<TablePotential> getPotentialsWithDecisionInIntervention(Variable decision,
			Collection<TablePotential> potentials) {

		Set<TablePotential> potentialsWithDecisionInIntervention = new HashSet<TablePotential>();
		for (TablePotential potential : potentials) {
			if (VariableEliminationOperations.hasDecisionInIntervention(decision, potential)) {
				potentialsWithDecisionInIntervention.add(potential);
			}
		}
		return potentialsWithDecisionInIntervention;
	}

	/**
	 * Checks whether the given potential has a strategy-tree intervention for the specified decision.
	 *
	 * @param decision       the decision variable to check
	 * @param tablePotential the potential to inspect
	 * @return {@code true} if the potential has an intervention for the decision
	 */
	public static boolean hasDecisionInIntervention(Variable decision, TablePotential tablePotential) {
		return tablePotential.hasInterventionForDecision(decision);
	}

}
