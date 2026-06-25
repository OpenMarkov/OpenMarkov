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
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.operation.AugmentedProbTableInference;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.*;

/**
 * A potential for nodes with both finite-state and numeric parents. The finite-state
 * parents define a table structure, while numeric parents are referenced via symbolic
 * expressions in an {@link AugmentedProbTable}. During inference, the expressions are
 * evaluated with the evidence values of the numeric parents to produce a standard
 * {@link TablePotential}.
 */
@PotentialType(names = "AugmentedProbTable")
public class AugmentedProbTablePotential extends Potential {
    
    protected AugmentedProbTable augmentedProbTable;
    private List<Variable> finiteStatesVariables;
    private List<Variable> parameterVariables;
    
    /*Note should be discrete variables*/
    public AugmentedProbTablePotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        setFiniteStatesVariables(new ArrayList<>());
        setParameterVariables(new ArrayList<>());
        getFiniteStatesVariables().add(variables.getFirst());
        for (Variable variable : variables.subList(1, variables.size())) {
            if ((variable.getVariableType() == VariableType.FINITE_STATES) || (
                    variable.getVariableType() == VariableType.DISCRETIZED
            )) {
                finiteStatesVariables.add(variable);
            } else {
                parameterVariables.add(variable);
            }
        }
        
        setAugmentedProbTable(new AugmentedProbTable(getFiniteStatesVariables(), role));
    }
    
    public AugmentedProbTablePotential(AugmentedProbTablePotential AugmentedProbTablePotential) {
        this(AugmentedProbTablePotential.variables, AugmentedProbTablePotential.getPotentialRole());
        //UNCLEAR Should I copy Functions?
        this.augmentedProbTable = new AugmentedProbTable(AugmentedProbTablePotential.getAugmentedProbTable());
    }
    
    /**
     * Returns whether this potential type makes sense for the given node, variables and role.
     * It is valid only for non-DES networks whose conditioned variable is finite-states.
     *
     * @param node      the node the potential would be attached to
     * @param variables the variables of the potential
     * @param role      the potential role
     *
     * @return {@code true} if an {@code AugmentedProbTablePotential} is applicable
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        if (node.getProbNet().getNetworkType() instanceof DESNetworkType) {
            return false;
        }
        return (node.getVariable().getVariableType() == VariableType.FINITE_STATES);
        
    }
    
    public AugmentedProbTable getAugmentedProbTable() {
        return augmentedProbTable;
    }
    
    public void setAugmentedProbTable(AugmentedProbTable AugmentedProbTable) {
        this.augmentedProbTable = AugmentedProbTable;
    }
    
    /**
     * @return the finiteStatesVariables
     */
    public List<Variable> getFiniteStatesVariables() {
        return finiteStatesVariables;
    }
    
    /**
     * @param finiteStatesVariables the finiteStatesVariables to set
     */
    public void setFiniteStatesVariables(List<Variable> finiteStatesVariables) {
        this.finiteStatesVariables = finiteStatesVariables;
    }
    
    public List<Variable> getParameterVariables() {
        return parameterVariables;
    }
    
    public void setParameterVariables(List<Variable> parameterVariables) {
        this.parameterVariables = parameterVariables;
    }
    
    /**
     * Evaluates the symbolic cell expressions against the numeric parents' values in the
     * evidence and returns the resulting numeric {@link TablePotential} over the finite-state
     * variables.
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException.CannotEvaluate, NonProjectablePotentialException.CannotResolveVariable {
        Map<Variable, String> findingsMap = evidenceCase.getFindingsMap();
        VariableExpression[] expressions = this.augmentedProbTable.getFunctionValues();
        var resolvedTablePotential = new TablePotential(this.finiteStatesVariables, role);
        var variableOfPotential = variables.getFirst();
        int numStates = variableOfPotential.getNumStates();
        int divisions = resolvedTablePotential.getValues().length / numStates;
        for (int columnIndex = 0; columnIndex < divisions; columnIndex++){
            var unresolvedValues = Arrays.copyOfRange(expressions, columnIndex*numStates, (1+columnIndex)*numStates);
            var resolvedValues = AugmentedProbTableInference.resolveColumn(unresolvedValues, findingsMap, AugmentedProbTableInference.Operation.values());
            for(int rowIndex = 0; rowIndex < numStates; rowIndex++){
                resolvedTablePotential.getValues()[columnIndex*numStates + rowIndex] = resolvedValues[rowIndex];
            }
        }
        return resolvedTablePotential;
    }
    
    /**
     * Not supported: this potential can only be projected to a table via {@link #tableProject}.
     *
     * @throws NotSupportedOperationException always
     */
    @Override
    public Potential project(EvidenceCase evidenceCase) {
        throw new NotSupportedOperationException();
    }
    
    @Override public Potential copy() {
        return new AugmentedProbTablePotential(this);
    }
    
    @Override public boolean isUncertain() {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override public void scalePotential(double scale) {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * Returns a copy with the finite-state variables reordered as given; the numeric parameter
     * variables are kept and appended to the new order.
     */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        int size = newOrderOfVariables.size();
        //orderVariables has the order of the parents of the AugmentedProbTable, so parameterVariables should be added
        newOrderOfVariables.addAll(getParameterVariables());
        AugmentedProbTablePotential newPotential = new AugmentedProbTablePotential(newOrderOfVariables, getPotentialRole());
        AugmentedProbTable newDistributionTable = (AugmentedProbTable) getAugmentedProbTable().reorder(newOrderOfVariables.subList(0, size));
        newPotential.setAugmentedProbTable(newDistributionTable);
        return newPotential;
    }
    
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        AugmentedProbTablePotential copy = new AugmentedProbTablePotential(this);
        AugmentedProbTable reorderedTable = (AugmentedProbTable) augmentedProbTable.reorder(variable, newOrder);
        copy.setAugmentedProbTable(reorderedTable);
        return copy;
    }
    
}