/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.developmentStaticAnalysis.ToCheck;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.*;

/**
 * A deterministic potential that assigns probability 1 to a single state (for finite-states
 * variables) or a single numeric value (for numeric/discretized variables). Represents a
 * Dirac delta distribution.
 *
 * @author Manuel Arias
 */
@PotentialType(names = "Delta") public class DeltaPotential extends Potential implements Projectable, Scalable, DESSimulablePotential {
    
    // state and stateIndex are used for finite states variables
    private State state = null;
    private int stateIndex = -1;
    // numericValue is used for numeric variables
    private double numericValue = Double.NaN;
    
    public DeltaPotential(List<Variable> variables, PotentialRole role, double numericValue) {
        this(variables, role);
        initNumeric(getConditionedVariable(), numericValue);
    }
    
    public DeltaPotential(List<Variable> variables, PotentialRole role, State state) {
        this(variables, role);
        initFiniteStates(getConditionedVariable(), state);
    }
    
    public DeltaPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        // set default values
        Variable conditionedVariable = getConditionedVariable();
        if (conditionedVariable.getVariableType() != VariableType.FINITE_STATES) {
            PartitionedInterval variableDomain = conditionedVariable.getPartitionedInterval();
            double numericValue = variableDomain.isLeftClosed() ?
                    variableDomain.getMin() :
                    variableDomain.getMin() + conditionedVariable.getPrecision();
            initNumeric(conditionedVariable, numericValue);
        } else {
            initFiniteStates(conditionedVariable, conditionedVariable.getStates()[0]);
        }
    }
    
    public DeltaPotential(DeltaPotential potential) {
        super(potential);
        state = potential.state;
        stateIndex = potential.stateIndex;
        numericValue = potential.numericValue;
    }
    
    /**
     * Returns whether this type of Potential is suitable for the list of
     * variables and the potential role given.
     *
     * @param node      . {@code Node}
     * @param variables . {@code List} of {@code Variable}.
     * @param role      . {@code PotentialRole}.
     *
     * @return True if it is valid
     */
    @ToCheck(reasonKind = ToCheck.ReasonKind.PROBABLE_BUG,
            reasonDescription = "The boolean expression might be wrong, as there are no parentheses used.")
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        //I have tried to add DeltaPotential to the DESnet set of potentials; haphazardly OM returned Uniform and Sum potentials whereas in the GUI appears Delta potentials
        if ((node.getProbNet().getNetworkType() instanceof DESNetworkType)) {
            return true;
        }
        return (variables.size() <= 1) //This means it has no parents
                || (role == PotentialRole.POLICY)
                || ((role == PotentialRole.CONDITIONAL_PROBABILITY)
                && (node.getVariable().getVariableType() == VariableType.NUMERIC));
    }
    
    private void initFiniteStates(Variable conditionedVariable, State state) {
        this.state = state;
        stateIndex = conditionedVariable.getStateIndex(state);
    }
    
    private void initNumeric(Variable conditionedVariable, double numericValue) {
        this.numericValue = numericValue;
        if (conditionedVariable.getVariableType() == VariableType.DISCRETIZED || conditionedVariable.getVariableType() == VariableType.EVENT) {
            int index = conditionedVariable.getStateIndex(numericValue);
            this.stateIndex = Math.max(this.stateIndex, index);
        }
    }
    
    /**
     * Projects this delta onto a table potential. For a numeric variable it
     * returns a constant equal to the stored numeric value; for a finite-states
     * variable it returns a table assigning probability 1 to the delta state and
     * 0 to the rest (a constant 1 if that variable is already in the evidence).
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials) {
        // numeric variable
        if (state == null) {
            TablePotential projectedPotential = new TablePotential(new ArrayList<>(), PotentialRole.CONDITIONAL_PROBABILITY);
            projectedPotential.getValues()[0] = numericValue;
            return projectedPotential;
        }
        // finite states variable
        Variable conditionedVariable = getConditionedVariable();
        if (evidenceCase.contains(conditionedVariable)) {
            TablePotential projectedPotential = new TablePotential(new ArrayList<>(), PotentialRole.CONDITIONAL_PROBABILITY);
            projectedPotential.getValues()[0] = 1;
            return projectedPotential;
        }
        TablePotential projectedPotential = new TablePotential(Collections.singletonList(conditionedVariable), PotentialRole.CONDITIONAL_PROBABILITY);
        for (int i = 0; i < projectedPotential.getValues().length; ++i) {
            projectedPotential.getValues()[i] = (i == stateIndex) ? 1 : 0;
        }
        return projectedPotential;
    }
    
    /** Not supported for this potential; always throws {@link NotSupportedOperationException}. */
    @Override
    public Potential project(EvidenceCase evidenceCase) {
        throw new NotSupportedOperationException();
    }

    @Override public Potential copy() {
        return new DeltaPotential(this);
    }

    public State getState() {
        return state;
    }
    
    public double getNumericValue() {
        return numericValue;
    }
    
    public int getStateIndex() {
        return stateIndex;
    }
    
    public void setValue(State state) {
        this.state = state;
        stateIndex = getConditionedVariable().getStateIndex(state);
    }
    
    public void setValue(double numericValue) {
        this.numericValue = numericValue;
    }
    
    @Override public String toString() {
        return variables.getFirst().getName() + " = " + (state != null ? state.getName() : numericValue);
    }
    
    @Override public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase) {
        Finding inducedFinding;
        if (getConditionedVariable().getVariableType() == VariableType.FINITE_STATES) {
            inducedFinding = new Finding(getConditionedVariable(), state);
        } else {
            inducedFinding = new Finding(getConditionedVariable(), numericValue);
        }
        return List.of(inducedFinding);
    }
    
    @Override public void scalePotential(double scale) {
        this.numericValue *= scale;
    }

    /** Implements {@link Scalable#scale(double)}; delegates to {@link #scalePotential(double)}. */
    @Override public void scale(double factor) {
        scalePotential(factor);
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        DeltaPotential potential = (DeltaPotential) super.deepCopy(copyNet);
        
        potential.numericValue = this.numericValue;
        
        if (this.state != null) {
            potential.setValue(new State(this.state));
        }
        potential.stateIndex = this.stateIndex;
        
        return potential;
        
    }
    
    /**
     * Returns a copy of this potential with the variables in the new order.
     * The conditioned variable (first in the list) is always
     * {@code variables.getFirst()} regardless of variable ordering, so the
     * delta state/value is preserved unchanged.
     */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        DeltaPotential copy = new DeltaPotential(newOrderOfVariables, role);
        copy.state = this.state;
        copy.stateIndex = this.stateIndex;
        copy.numericValue = this.numericValue;
        copy.properties = this.properties;
        return copy;
    }

    /**
     * Returns a copy of this potential reflecting the new state order of
     * {@code variable}. If {@code variable} is the conditioned variable (a
     * finite-states variable), the stored {@code stateIndex} is remapped to
     * point to the same {@link State} object in the new order. If
     * {@code variable} is a parent, the delta value is independent of parent
     * states, so a plain copy is returned.
     */
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        Variable conditioned = getConditionedVariable();
        if (variable == conditioned && state != null) {
            // Find the new index of our state in the reordered array
            int newIndex = -1;
            for (int i = 0; i < newOrder.length; i++) {
                if (newOrder[i] == state) {
                    newIndex = i;
                    break;
                }
            }
            DeltaPotential copy = new DeltaPotential(variables, role);
            copy.state = this.state;
            copy.stateIndex = newIndex >= 0 ? newIndex : this.stateIndex;
            copy.numericValue = this.numericValue;
            copy.properties = this.properties;
            return copy;
        }
        return copy();
    }
    
    @Override
    public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) {
        return switch (getConditionedVariable().getVariableType()) {
            case FINITE_STATES-> stateIndex;
            case DISCRETIZED -> Double.NaN;
            case NUMERIC, EVENT-> numericValue;
        };
    }
    
}
