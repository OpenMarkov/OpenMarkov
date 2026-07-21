/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.List;

/**
 * A {@link TablePotential} that carries an array of {@link StrategyTree}s —
 * one per table position — used during influence diagram solving to record the
 * optimal strategy at each configuration.
 * <p>
 * Separating this concern from {@link TablePotential} removes the nullable
 * {@code strategyTrees} field from every ordinary potential, eliminating the
 * null-checks scattered across the arithmetic operations and copy paths.
 *
 * @author Manuel Arias Calleja
 * @since OpenMarkov 0.3
 */
public class StrategicTablePotential extends TablePotential implements StrategyCarrier {

    /**
     * One strategy tree per table position.  Created and owned by
     * influence-diagram solving operations; {@code null} entries mean
     * "no strategy recorded at this configuration".
     */
    public volatile StrategyTree[] strategyTrees;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public StrategicTablePotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
    }

    public StrategicTablePotential(List<Variable> variables, PotentialRole role, double[] table) {
        super(variables, role, table);
    }

    /**
     * Copy constructor.  Performs a <em>shallow</em> copy of
     * {@code strategyTrees}; call {@link #deepCopy(ProbNet)} to obtain a
     * fully independent copy.
     *
     * @param source Source potential.
     */
    public StrategicTablePotential(StrategicTablePotential source) {
        super(source);
        this.strategyTrees = source.strategyTrees; // shallow; deepCopy does the real work
    }

    // -------------------------------------------------------------------------
    // StrategyCarrier implementation
    // -------------------------------------------------------------------------

    /** Implements {@link StrategyCarrier#getStrategyTrees()}. */
    @Override
    public StrategyTree[] getStrategyTrees() {
        return strategyTrees;
    }

    @Override
    public boolean hasInterventions() {
        return strategyTrees != null && strategyTrees.length > 0 && strategyTrees[0] != null;
    }

    @Override
    public boolean hasInterventionForDecision(Variable decision) {
        return hasInterventions() && strategyTrees[0].hasInterventionForDecision(decision);
    }

    @Override
    public Potential copy() {
        return new StrategicTablePotential(this);
    }

    /**
     * Returns a deep copy in which each {@link StrategyTree} is independently
     * cloned.
     */
    @Override
    public Potential deepCopy(ProbNet copyNet) {
        StrategicTablePotential copy = (StrategicTablePotential) super.deepCopy(copyNet);
        if (this.strategyTrees != null) {
            copy.strategyTrees = new StrategyTree[this.strategyTrees.length];
            for (int i = 0; i < this.strategyTrees.length; i++) {
                copy.strategyTrees[i] = (StrategyTree) this.strategyTrees[i].deepCopy(copyNet);
            }
        }
        return copy;
    }

    /**
     * Reorders both the {@code values} table and the {@code strategyTrees}
     * array to match {@code newOrderOfVariables}.
     */
    @Override
    public StrategicTablePotential reorder(List<Variable> newOrderOfVariables) {
        StrategicTablePotential newPotential = new StrategicTablePotential(newOrderOfVariables, getPotentialRole());
        int[] accOffsets = getAccumulatedOffsets(newOrderOfVariables);
        int[] potentialPositions = new int[getNumVariables()];
        int[] potentialDimensions = getDimensions();
        double[] valuesOrig = getValues();
        double[] valuesNew  = newPotential.getValues();
        StrategyTree[] origTrees = this.strategyTrees;
        if (origTrees != null) {
            newPotential.strategyTrees = new StrategyTree[origTrees.length];
        }

        int copyPos = 0;
        int numVariables = newOrderOfVariables.size();
        int incrementedVariable, i;
        for (i = 0; i < valuesOrig.length - 1; i++) {
            valuesNew[copyPos] = valuesOrig[i];
            if (origTrees != null) {
                newPotential.strategyTrees[copyPos] = origTrees[i];
            }

            for (incrementedVariable = 0; incrementedVariable < numVariables; incrementedVariable++) {
                potentialPositions[incrementedVariable]++;
                if (potentialPositions[incrementedVariable] == potentialDimensions[incrementedVariable]) {
                    potentialPositions[incrementedVariable] = 0;
                } else {
                    break;
                }
            }
            copyPos += accOffsets[incrementedVariable];
        }
        valuesNew[copyPos] = valuesOrig[i];
        if (origTrees != null) {
            newPotential.strategyTrees[copyPos] = origTrees[i];
        }

        if (isAdditive()) {
            newPotential.setCriterion(getCriterion());
        }
        newPotential.properties = properties;
        return newPotential;
    }

    /**
     * Reorders both {@code values} and {@code strategyTrees} when the states of
     * {@code variable} are renumbered to {@code newOrder}.
     * <p>
     * The inherited {@link TablePotential#reorder(Variable, State[])} only
     * rearranges {@code values}; overriding here keeps {@code strategyTrees} in
     * sync.
     */
    @Override
    public StrategicTablePotential reorder(Variable variable, State[] newOrder) {
        StrategicTablePotential copy = new StrategicTablePotential(this);
        double[] origValues = getValues();
        double[] newValues  = copy.getValues();
        int variableIndex = copy.getVariables().indexOf(variable);
        int offset = copy.getOffsets()[variableIndex];
        State[] oldOrder = variable.getStates();

        int[] displacements = new int[newOrder.length];
        for (int i = 0; i < newOrder.length; i++) {
            for (int j = 0; j < oldOrder.length; j++) {
                if (oldOrder[i] == newOrder[j]) {
                    displacements[i] = j - i;
                    break;
                }
            }
        }

        StrategyTree[] origTrees = this.strategyTrees;
        if (origTrees != null) {
            copy.strategyTrees = new StrategyTree[origTrees.length];
        }
        for (int i = 0; i < origValues.length; i++) {
            int indexOfState = (i / offset) % variable.getNumStates();
            int newIndex = i + (displacements[indexOfState] * offset);
            newValues[newIndex] = origValues[i];
            if (origTrees != null) {
                copy.strategyTrees[newIndex] = origTrees[i];
            }
        }

        if (isAdditive()) {
            copy.setCriterion(getCriterion());
        }
        copy.properties = properties;
        return copy;
    }
}
