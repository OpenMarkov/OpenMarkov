/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.Variable;

import java.util.List;
import java.util.Map;

/**
 * Carries the values of a table to another one whose variables differ in a parent, as a relation does when a parent
 * is added or removed, so that it keeps its numbers (error 99 of the pending list).
 *
 * @author Manuel Arias
 */
final class ValuesOfTheSameStates {

    private ValuesOfTheSameStates() {
    }

    /**
     * Gives each configuration of {@code to} the value of the same states in {@code from}. A variable that
     * {@code from} does not have is left out, so each of its states repeats the values; one that {@code to} does not
     * have is taken in its first state. A variable that each table builds for itself, such as the one that stands for
     * the event parents, is matched through {@code sameAs}, from that of {@code to} to that of {@code from}, and its
     * states by their names: a state that {@code from} does not have, such as a new event, takes the values of the
     * first one.
     */
    static void copy(TablePotential from, TablePotential to, Map<Variable, Variable> sameAs) {
        List<Variable> fromVariables = from.getVariables();
        List<Variable> toVariables = to.getVariables();
        // For each variable of from, its position in to, or -1, and the state it takes for each state there
        int[] positionInTo = new int[fromVariables.size()];
        int[][] fromState = new int[fromVariables.size()][];
        for (int k = 0; k < fromVariables.size(); k++) {
            Variable fromVariable = fromVariables.get(k);
            positionInTo[k] = -1;
            for (int j = 0; j < toVariables.size(); j++) {
                Variable toVariable = toVariables.get(j);
                if (toVariable == fromVariable || sameAs.get(toVariable) == fromVariable) {
                    positionInTo[k] = j;
                    fromState[k] = new int[toVariable.getNumStates()];
                    for (int state = 0; state < fromState[k].length; state++) {
                        fromState[k][state] = toVariable == fromVariable
                                ? state : Math.max(0, fromVariable.getStateIndex(toVariable.getStateName(state)));
                    }
                }
            }
        }
        int[] fromOffsets = from.getOffsets();
        int[] toOffsets = to.getOffsets();
        int[] toDimensions = to.getDimensions();
        double[] fromValues = from.getValues();
        double[] values = new double[to.getValues().length];
        for (int configuration = 0; configuration < values.length; configuration++) {
            int fromConfiguration = 0;
            for (int k = 0; k < fromVariables.size(); k++) {
                if (positionInTo[k] >= 0) {
                    int toState = configuration / toOffsets[positionInTo[k]] % toDimensions[positionInTo[k]];
                    fromConfiguration += fromState[k][toState] * fromOffsets[k];
                }
            }
            values[configuration] = fromValues[fromConfiguration];
        }
        to.setValues(values);
    }
}
