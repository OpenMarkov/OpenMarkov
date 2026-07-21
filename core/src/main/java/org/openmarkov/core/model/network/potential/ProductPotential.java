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
import java.util.List;

/**
 * Potential associated to supervalue node to indicate that the utility is a
 * product of the utilities of its parents.
 *
 * @author Manuel Arias
 * @author mkpalacio
 * @version 1.0
 */
@PotentialType(names = "Product") public class ProductPotential extends Potential {
    
    // Constructor
    
    /**
     * @param variables variables
     * @param role      potential role
     */
    public ProductPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
    }
    
    public ProductPotential(ProductPotential potential) {
        super(potential);
    }
    
    /**
     * @param variables variables
     */
    public ProductPotential(List<Variable> variables) {
        this(variables, PotentialRole.CONDITIONAL_PROBABILITY);
    }
    
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
    
    // Methods
    
    /**
     * Project the potential and return a list of projected table potentials
     *
     * @param evidenceCase        {@code EvidenceCase}
     * @param inferenceOptions    Inference options
     * @param projectedPotentials Projected potentials
     *
     * @return a list of projected table potentials
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials) {
        List<Variable> parentVariables = new ArrayList<>(variables);
        parentVariables.remove(getConditionedVariable());
        List<TablePotential> parentPotentials = new ArrayList<>();
        for (Variable parentVariable : parentVariables) {
            parentPotentials.add(findPotentialByVariable(parentVariable, projectedPotentials));
        }
        return DiscretePotentialOperations.multiply(parentPotentials);
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
        return new ProductPotential(this);
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

