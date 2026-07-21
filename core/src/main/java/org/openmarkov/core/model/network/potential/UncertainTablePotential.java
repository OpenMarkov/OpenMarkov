/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link TablePotential} that carries uncertain values for sensitivity analysis.
 * <p>
 * Plain {@link TablePotential} instances never hold uncertain values; only instances of
 * this subclass do.
 *
 * @author Manuel Arias
 */
public class UncertainTablePotential extends TablePotential implements UncertaintyCarrier {

    /**
     * Uncertain values for sensitivity analysis.  {@code null} means no uncertainty.
     * Public and volatile for efficiency reasons.
     */
    public volatile UncertainValue[] uncertainValues;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    public UncertainTablePotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
    }

    public UncertainTablePotential(List<Variable> variables, PotentialRole role, double[] table) {
        super(variables, role, table);
    }

    /**
     * Copy constructor.
     * Required by {@link Potential#deepCopy} (reflection-based).
     * Performs a <em>shallow</em> copy of {@code uncertainValues};
     * {@link #deepCopy} performs the full clone.
     */
    public UncertainTablePotential(UncertainTablePotential source) {
        super(source);
        this.uncertainValues = source.uncertainValues;   // deepCopy will replace this with a deep clone
    }

    // -----------------------------------------------------------------------
    // Overrides — uncertain-value storage
    // -----------------------------------------------------------------------

    @Override
    public UncertainValue[] getUncertainValues() {
        return uncertainValues;
    }

    @Override
    public void setUncertainValues(UncertainValue[] uv) {
        this.uncertainValues = uv;
    }

    // -----------------------------------------------------------------------
    // Overrides — copy / deepCopy must return UncertainTablePotential
    // -----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>The returned copy is an {@code UncertainTablePotential} that shares (shallow-copies)
     * the {@code uncertainValues} array of this potential.
     */
    @Override
    public Potential copy() {
        return new UncertainTablePotential(this);
    }

    /**
     * {@inheritDoc}
     * <p>In addition to the inherited state, this also deep-clones the
     * {@code uncertainValues} array.
     */
    @Override
    public Potential deepCopy(ProbNet copyNet) {
        UncertainTablePotential p = (UncertainTablePotential) super.deepCopy(copyNet);
        if (this.uncertainValues != null) {
            p.uncertainValues = new UncertainValue[this.uncertainValues.length];
            for (int i = 0; i < this.uncertainValues.length; i++) {
                if (this.uncertainValues[i] != null) {
                    p.uncertainValues[i] = this.uncertainValues[i].copy();
                }
            }
        }
        return p;
    }

    // -----------------------------------------------------------------------
    // Overrides — reorder must return UncertainTablePotential and copy uncertainValues
    // -----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>In addition to reordering the values, this also propagates the
     * {@code uncertainValues} into the reordered potential.
     */
    @Override
    public UncertainTablePotential reorder(List<Variable> newOrderOfVariables) {
        UncertainTablePotential newPotential = new UncertainTablePotential(newOrderOfVariables, getPotentialRole());
        int[] accOffsets = getAccumulatedOffsets(newOrderOfVariables);
        int[] potentialPositions = new int[getNumVariables()];
        int[] potentialDimensions = getDimensions();
        double[] valuesOrig = getValues();
        double[] valuesNew  = newPotential.getValues();
        UncertainValue[] origUncertain = this.uncertainValues;
        UncertainValue[] newUncertain  = null;
        if (origUncertain != null) {
            newPotential.uncertainValues = new UncertainValue[origUncertain.length];
            newUncertain = newPotential.uncertainValues;
        }
        int copyTablePosition = 0;
        int numVariables = newOrderOfVariables.size();
        int incrementedVariable, i;
        for (i = 0; i < valuesOrig.length - 1; i++) {
            valuesNew[copyTablePosition] = valuesOrig[i];
            if (newUncertain != null) {
                newUncertain[copyTablePosition] = origUncertain[i];
            }
            for (incrementedVariable = 0; incrementedVariable < numVariables; incrementedVariable++) {
                potentialPositions[incrementedVariable]++;
                if (potentialPositions[incrementedVariable] == potentialDimensions[incrementedVariable]) {
                    potentialPositions[incrementedVariable] = 0;
                } else {
                    break;
                }
            }
            copyTablePosition += accOffsets[incrementedVariable];
        }
        valuesNew[copyTablePosition] = valuesOrig[i];
        if (newUncertain != null) {
            newUncertain[copyTablePosition] = origUncertain[i];
        }
        if (isAdditive()) newPotential.setCriterion(getCriterion());
        newPotential.properties = properties;
        return newPotential;
    }

    /**
     * {@inheritDoc}
     * <p>In addition to reordering the states, this also propagates the
     * {@code uncertainValues} into the reordered potential.
     */
    @Override
    public TablePotential reorder(Variable variable, State[] newOrder) {
        UncertainTablePotential copyPotential = new UncertainTablePotential(this);
        double[] tableOrig = getValues();
        double[] tableCopy = copyPotential.getValues();
        int[] displacements = new int[newOrder.length];
        List<Variable> vars = copyPotential.getVariables();
        int variableIndex = vars.indexOf(variable);
        int offset = copyPotential.getOffsets()[variableIndex];
        State[] oldOrder = variable.getStates();
        for (int i = 0; i < newOrder.length; ++i) {
            displacements[i] = -1;
            int j = 0;
            boolean found = false;
            while (!found) {
                if (oldOrder[i] == newOrder[j]) {
                    displacements[i] = j - i;
                    found = true;
                }
                ++j;
            }
        }
        UncertainValue[] srcUncertain = this.uncertainValues;
        UncertainValue[] copyUncertain = null;
        if (srcUncertain != null) {
            copyPotential.uncertainValues = new UncertainValue[srcUncertain.length];
            copyUncertain = copyPotential.uncertainValues;
        }
        for (int i = 0; i < tableOrig.length; i++) {
            int indexOfState = (i / offset) % variable.getNumStates();
            int newIndex = i + (displacements[indexOfState % variable.getNumStates()] * offset);
            tableCopy[newIndex] = tableOrig[i];
            if (copyUncertain != null) {
                copyUncertain[newIndex] = srcUncertain[i];
            }
        }
        if (isAdditive()) copyPotential.setCriterion(getCriterion());
        copyPotential.properties = properties;
        return copyPotential;
    }

    // -----------------------------------------------------------------------
    // Override — tableProject must return UncertainTablePotential and copy uncertainValues
    // -----------------------------------------------------------------------

    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase,
                                                InferenceOptions inferenceOptions,
                                                List<TablePotential> projectedPotentials)
            throws NonProjectablePotentialException {
        List<Variable> unobservedVariables = new ArrayList<>(variables);
        if (evidenceCase != null) {
            unobservedVariables.removeAll(evidenceCase.getVariables());
        }
        int numUnobservedVariables = unobservedVariables.size();
        int numVariables = (variables != null) ? variables.size() : 0;
        if (numVariables == numUnobservedVariables) {
            return this;   // no projection needed
        }
        UncertainTablePotential projectedPotential = new UncertainTablePotential(unobservedVariables, role);
        int length = projectedPotential.getValues().length;
        if (uncertainValues != null) {
            projectedPotential.uncertainValues = new UncertainValue[length];
        }
        // Compute first position (offset from evidence)
        int firstPosition = 0;
        for (int i = 0; i < variables.size(); i++) {
            Variable variable = variables.get(i);
            if ((evidenceCase != null) && evidenceCase.contains(variable)) {
                int state = evidenceCase.getState(variable);
                firstPosition += state * offsets[i];
            }
        }
        if (numUnobservedVariables == 0) {
            // Constant potential after projection
            projectedPotential.getValues()[0] = getValues()[firstPosition];
            if (projectedPotential.uncertainValues != null) {
                projectedPotential.uncertainValues[0] = uncertainValues[firstPosition];
            }
        } else {
            int[] accumulatedOffsets = projectedPotential.getAccumulatedOffsets(variables);
            int numVariablesProjected = projectedPotential.getNumVariables();
            int[] projectedCoordinate = new int[numVariablesProjected];
            int[] projectedDimensions = new int[numVariablesProjected];
            for (int i = 0; i < numVariablesProjected; i++) {
                projectedDimensions[i] = unobservedVariables.get(i).getNumStates();
            }
            for (int projectedPosition = 0; projectedPosition < length - 1; projectedPosition++) {
                projectedPotential.getValues()[projectedPosition] = getValues()[firstPosition];
                if (projectedPotential.uncertainValues != null) {
                    projectedPotential.uncertainValues[projectedPosition] = uncertainValues[firstPosition];
                }
                int increasedVariable = 0;
                for (int j = 0; j < projectedCoordinate.length; j++) {
                    projectedCoordinate[j]++;
                    if (projectedCoordinate[j] < projectedDimensions[j]) {
                        increasedVariable = j;
                        break;
                    }
                    projectedCoordinate[j] = 0;
                }
                firstPosition += accumulatedOffsets[increasedVariable];
            }
            projectedPotential.getValues()[length - 1] = getValues()[firstPosition];
            if (projectedPotential.uncertainValues != null) {
                projectedPotential.uncertainValues[length - 1] = uncertainValues[firstPosition];
            }
        }
        nullifyIfAllNull(projectedPotential);
        return projectedPotential;
    }

    private static void nullifyIfAllNull(UncertainTablePotential p) {
        if (p.uncertainValues == null) return;
        for (UncertainValue uv : p.uncertainValues) {
            if (uv != null) return;
        }
        p.uncertainValues = null;
    }
}
