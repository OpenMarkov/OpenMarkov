/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.List;
import java.util.Random;

/**
 * A potential indicating that the distribution is identical to the previous temporal
 * slice. Used in dynamic Bayesian networks where a variable at time t has the same
 * CPT as at time t-1. Cannot be directly sampled or projected; the original potential
 * must be retrieved via {@link #getOriginalPotential(ProbNet)}.
 *
 * @author Manuel Arias
 * @version 1.0
 */
@PotentialType(names = "Same as previous") public class SameAsPrevious extends Potential {
    // Constructors
    
    /**
     * @param variables List of variables
     */
    public SameAsPrevious(List<Variable> variables) {
        super(variables, PotentialRole.CONDITIONAL_PROBABILITY);
    }
    
    /**
     * Copy constructor
     *
     * @param potential Potential
     */
    public SameAsPrevious(SameAsPrevious potential) {
        super(potential);
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role
     *
     * @param node      Node
     * @param variables List of variables
     * @param role      Potential role
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        return node.getVariable().isTemporal() && node.getVariable().getTimeSlice() > 0;
    }
    
    // Methods
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials) throws NonProjectablePotentialException.PotentialCannotBeConvertedToATable {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    /** Not supported for this potential; always throws {@link NotSupportedOperationException}. */
    @Override
    public Potential project(EvidenceCase evidenceCase) {
        throw new NotSupportedOperationException();
    }

    /**
     * @return the relation of the same variable in the latest earlier cycle that has one other than «Same as
     * previous», or null if there is none
     */
    public @Nullable Potential getOriginalPotential(ProbNet probNet) {
        return getOriginalPotential(probNet, getConditionedVariable());
    }
    
    @Override public Potential sample(Random randomGenerator) {
        throw new NotSupportedOperationException("SameAsPrevious potentials cannot be sampled.");
    }
    
    private static @Nullable Potential getOriginalPotential(ProbNet probNet, Variable variable) {
        if (!variable.isTemporal()) {
            return null;
        }
        // A cycle without the variable, or whose node has no relation, is skipped
        for (int timeSlice = variable.getTimeSlice() - 1; timeSlice >= 0; timeSlice--) {
            @Nullable Variable previousVariable = probNet.getVariable(variable.getBaseName(), timeSlice);
            @Nullable Node previousNode = previousVariable == null ? null : probNet.getNode(previousVariable);
            if (previousNode != null && !previousNode.getPotentials().isEmpty()
                && !(previousNode.getPotentials().getFirst() instanceof SameAsPrevious)) {
                return previousNode.getPotentials().getFirst();
            }
        }
        return null;
    }
    
    @Override public Potential copy() {
        return new SameAsPrevious(this);
    }
    
    @Override public String toString() {
        return super.toString() + " = SameAsPrevious";
    }
    
    @Override public void replaceNumericVariable(Variable convertedParentVariable) {
        super.replaceNumericVariable(convertedParentVariable);
    }
    
    @Override public boolean isUncertain() {
        throw new NotSupportedOperationException("There is no way to know whether SameAsPrevious potentials are uncertain");
    }
    
    @Override public void scalePotential(double scale) {
    
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        return super.deepCopy(copyNet);
    }
    
    /** No state-indexed data; returns a copy. */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        return copy();
    }

    /** No state-indexed data; returns a copy. */
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        return copy();
    }
    
}
