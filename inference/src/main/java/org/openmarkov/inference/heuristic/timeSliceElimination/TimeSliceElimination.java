/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.timeSliceElimination;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.AuxiliaryOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * This heuristic is meant for dynamic/temporal models and it eliminates variables starting
 * from the last slice and towards the first. It eliminates all variables of a slice before
 * proceeding to the next. Amongst the variables of each slice, it chooses the one that would
 * create the smallest potential.
 *
 * @author inigo
 */
public class TimeSliceElimination extends EliminationHeuristic {

	private int currentIndex;
	private final List<List<List<Variable>>> variablesToEliminateInSlices;

	public TimeSliceElimination(ProbNet probNet, List<List<Variable>> variablesToEliminate) {
		super(probNet, variablesToEliminate);

		int numSlices = getNumSlices(variablesToEliminate);

		this.variablesToEliminateInSlices = new ArrayList<List<List<Variable>>>();

		for (List<Variable> variableList : variablesToEliminate) {
			List<List<Variable>> variablesInSlices = new ArrayList<>(numSlices + 2);
			for (int i = 0; i < numSlices + 2; ++i)
				variablesInSlices.add(new ArrayList<Variable>());

			for (Variable variable : variableList) {
				int index = variable.isTemporal() ? variable.getTimeSlice() + 1 : 0;
				variablesInSlices.get(index).add(variable);
			}
			this.variablesToEliminateInSlices.add(variablesInSlices);
		}

		currentIndex = numSlices + 1;
	}
    
    private static int getNumSlices(List<List<Variable>> variables) {

		int numSlices = -1;
		for (List<Variable> variableList : variables) {
			for (Variable variable : variableList) {
				if (variable.isTemporal() && variable.getTimeSlice() > numSlices)
					numSlices = variable.getTimeSlice();
			}
		}
		return numSlices;
	}

	@Override public Variable getVariableToDelete() {
		Node bestNode = null;
		int bestScore = Integer.MAX_VALUE;
		if (currentIndex >= 0) {
			for (int i = variablesToEliminateInSlices.size() - 1; i >= 0 && bestNode == null; i--) {
				for (Variable variable : variablesToEliminateInSlices.get(i).get(currentIndex)) {
					Node node = probNet.getNode(variable);
					if (node != null) {
						List<Variable> variables = AuxiliaryOperations
								.getUnionVariables(probNet.getProbPotentials(variable));
						int tableSize = TablePotential.computeTableSize(variables);
						if (tableSize < bestScore) {
							bestScore = tableSize;
							bestNode = node;
						}
					}
				}
			}
		}
		return (bestNode != null) ? bestNode.getVariable() : null;
	}
    
    @Override public void afterEditExecutes(PNEdit edit) {
        super.afterEditExecutes(edit);
        Variable deletedVariable = getEventVariable(edit);
		if (deletedVariable != null) {
			List<List<Variable>> removableVariables = variablesToEliminateInSlices
					.get(variablesToEliminateInSlices.size() - 1);
			int index = deletedVariable.isTemporal() ? deletedVariable.getTimeSlice() + 1 : 0;
			removableVariables.get(index).remove(deletedVariable);
			while (currentIndex >= 0 && removableVariables.get(currentIndex).isEmpty()) {
				currentIndex--;
			}
			if (currentIndex < 0) {
				variablesToEliminateInSlices.remove(variablesToEliminateInSlices.size() - 1);
				if (!variablesToEliminateInSlices.isEmpty())
					currentIndex = variablesToEliminateInSlices.get(variablesToEliminateInSlices.size() - 1).size() - 1;
			}
		}

	}

}
