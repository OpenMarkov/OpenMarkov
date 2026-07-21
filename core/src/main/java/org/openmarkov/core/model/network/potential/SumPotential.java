/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Potential associated to supervalue node to indicate that the utility is a
 * sum of the utilities of its parents.
 *
 * @author mkpalacio
 * @version 1.0
 */
@PotentialType(names = "Sum") public class SumPotential extends Potential {
    
    // Constructor
    //	/**
    //	 *
    //	 * @param variables
    //	 * @param utilityVariable
    //	 */
    //	public SumPotential(Variable utilityVariable, List<Variable> variables) {
    //		super(utilityVariable, variables);
    //	}
    
    /**
     * @param variables List of variables
     * @param role      Potential role
     */
    public SumPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
    }
    
    /**
     * @param variables List of variables
     */
    public SumPotential(List<Variable> variables) {
        this(variables, PotentialRole.CONDITIONAL_PROBABILITY);
    }
    
    public SumPotential(SumPotential potential) {
        super(potential);
    }
    
    // Methods
    
    /**
     * Returns if an instance of a certain Potential type makes sense given
     * the variables and the potential role.
     *
     * @param node      {@code Node}
     * @param variables {@code ArrayList} of {@code Variable}.
     * @param role      {@code PotentialRole}.
     *
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        if (node.getProbNet().getNetworkType() instanceof DESNetworkType) {
            return false;
        }
        boolean suitable = (
                role == PotentialRole.CONDITIONAL_PROBABILITY || role == PotentialRole.POLICY
        ) && variables.getFirst().getVariableType() == VariableType.NUMERIC;
        
        return suitable || (role == PotentialRole.UNSPECIFIED && node.isSuperValueNode());
    }
    
    /**
     * Projects this potential as the sum of the already-projected table potentials
     * of its parent variables.
     *
     * @param evidenceCase        {@code EvidenceCase}
     * @param inferenceOptions    Inference options
     * @param projectedPotentials Projected potentials
     * @return The table potential resulting from summing the parents' potentials.
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials) {
        List<Variable> parentVariables = new ArrayList<>(variables);
        parentVariables.remove(getConditionedVariable());
        List<TablePotential> parentPotentials = new ArrayList<>();
        for (Variable parentVariable : parentVariables) {
            parentPotentials.add(findPotentialByVariable(parentVariable, projectedPotentials));
        }
        return DiscretePotentialOperations.sum(parentPotentials);
    }
    
    /**
     * Not supported by this potential.
     *
     * @throws NotSupportedOperationException always.
     */
    @Override
    public Potential project(EvidenceCase evidenceCase) {
        throw new NotSupportedOperationException();
    }
    
    @Override public Potential copy() {
        return new SumPotential(this);
    }
    
    public double getUtility(HashMap<Variable, Integer> sampledStateIndexes, HashMap<Variable, Double> utilities) {
        double sum = 0.0;
        for (Variable variable : getVariables()) {
            sum += utilities.get(variable);
        }
        return sum;
    }
    
    @Override public Potential addVariable(Variable variable) {
        variables.add(variable);
        return this;
    }
    
    /**
     * Removes variable to a potential implemented in each child class
     */
    @Override public Potential removeVariable(Variable variable) {
        variables.remove(variable);
        return this;
    }

    @Override public void scalePotential(double scale) {
    
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        return super.deepCopy(copyNet);
    }
    
    /** Structural potential with no state-indexed data; returns a copy. */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        return copy();
    }

    /** Structural potential with no state-indexed data; returns a copy. */
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        return copy();
    }
}


