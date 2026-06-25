/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Potential identical to another but moved to another temporal slice.
 *
 * @author Manuel Arias
 * @version 1.0
 */
@PotentialType(names = "CycleLengthShift")
public class CycleLengthShift extends Potential {
    
    private CycleLength cycleLength;
    
    // Constructor
    
    /**
     * @param variables   list of variables
     * @param cycleLength cycle length of the potential
     */
    public CycleLengthShift(List<Variable> variables, CycleLength cycleLength) {
        super(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        this.cycleLength = cycleLength;
    }
    
    // public CycleLengthShift(Potential potential) {
    // super(potential);
    // }
    
    public CycleLengthShift(CycleLengthShift potential) {
        super(potential);
        this.cycleLength = potential.cycleLength;
        
    }
    
    public CycleLength getCycleLength() {
        return cycleLength;
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role
     *
     * @param variables List of variables
     * @param role      PotentialRole
     *
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        if (node.getProbNet().getNetworkType() instanceof DESNetworkType) {
            return false;
        }
        // 10/01/2023 Issue #483. Signature changed. It used to be:
        // public static boolean validate(List<Variable> variables, PotentialRole role) {
        if (!(role == PotentialRole.CONDITIONAL_PROBABILITY && variables.size() == 2)) {
            return false;
        }
        // child = variables.get (0)
        // parent = variables.get (1)
        Variable child = variables.get(0);
        Variable parent = variables.get(1);
        if (!child.isTemporal() || !parent.isTemporal()) {
            return false;
        }
        return child.getBaseName().equals(parent.getBaseName())
                && child.getTimeSlice() == parent.getTimeSlice() + 1;
    }
    
    // Methods

    /**
     * Projects this shift onto a table potential. For a numeric conditioned
     * variable it returns a constant equal to the parent's numeric value plus
     * the cycle length; for a finite-states variable it returns the parent's
     * projected potential with its values shifted one state.
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials) throws NonProjectablePotentialException.MissingVariableInEvidence {
        Variable conditionedVariable = getConditionedVariable();
        Variable conditioningVariable = variables.get((conditionedVariable == variables.getFirst()) ? 1 : 0);
        TablePotential projectedPotential;
        if (conditionedVariable.getVariableType() == VariableType.NUMERIC) {
            for (Variable variable : variables) {
                if (!variable.equals(conditionedVariable) && !evidenceCase.contains(variable)) {
                    throw new NonProjectablePotentialException.MissingVariableInEvidence(variable, evidenceCase);
                }
            }
            projectedPotential = new TablePotential(new ArrayList<>(), role);
            projectedPotential.getValues()[0] = evidenceCase.getNumericalValue(conditioningVariable) + cycleLength
                    .getValue();
            return projectedPotential;
        }
        // Build projected potential based on parent's potential
        TablePotential projectedParentPotential = findPotentialByVariable(conditioningVariable,
                                                                          projectedPotentials);
        List<Variable> projectedVariables = projectedParentPotential.getVariables();
        // replace parent variable with child variable in the list of
        // variables of the projected potential
        projectedVariables.remove(conditioningVariable);
        projectedVariables.addFirst(conditionedVariable);
        projectedPotential = new TablePotential(projectedVariables, role);
        
        int numStates = conditionedVariable.getNumStates();
        int numStatesParent = conditioningVariable.getNumStates();
        int configurationIndex = 0;
        // Copy values from parent's projected potential, shifting values
        // one state
        for (int i = 0; i < projectedParentPotential.getValues().length; i += numStatesParent) {
            projectedPotential.getValues()[configurationIndex * numStates] = 0;
            for (int j = 0; j < numStatesParent; ++j) {
                projectedPotential.getValues()[configurationIndex * numStates + j + 1] = projectedParentPotential.getValues()[
                        i + j];
            }
            configurationIndex++;
        }
        return projectedPotential;
    }
    
    /** Not supported for this potential; always throws {@link NotSupportedOperationException}. */
    @Override
    public Potential project(EvidenceCase evidenceCase) {
        throw new NotSupportedOperationException();
    }

    @Override public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase) {
        Variable conditionedVariable = getConditionedVariable();
        Variable conditioningVariable = variables.get((conditionedVariable == variables.getFirst()) ? 1 : 0);
        List<Finding> inducedFindings = new ArrayList<>();
        if (evidenceCase.contains(conditioningVariable) && !evidenceCase.contains(conditionedVariable)) {
            double numericalValue = evidenceCase.getFinding(conditioningVariable).getNumericalValue() + cycleLength
                    .getValue();
            inducedFindings.add(new Finding(conditionedVariable, numericalValue));
        }
        return inducedFindings;
    }
    
    @Override public Potential copy() {
        List<Variable> copiedVariables = null;
        if (this.getVariables() != null && !this.getVariables().isEmpty()) {
            copiedVariables = new ArrayList<>(this.getVariables());
        }
        CycleLengthShift cycleLengthShift = new CycleLengthShift(copiedVariables, this.cycleLength);
        cycleLengthShift.comment = this.comment;
        return cycleLengthShift;
        
    }

    @Override public String toString() {
        return super.toString() + " = CycleLengthShift";
    }
    
    @Override public void scalePotential(double scale) {
        throw new NotSupportedOperationException();
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        CycleLengthShift potential = (CycleLengthShift) super.deepCopy(copyNet);
        potential.cycleLength = this.cycleLength.clone();
        return potential;
    }
    
    /** Structural/temporal potential; no state-indexed data; returns a copy. */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        return copy();
    }

    /** Structural/temporal potential; no state-indexed data; returns a copy. */
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        return copy();
    }
    
}
