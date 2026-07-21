/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * The class {@code PotentialOperations} contains method for performing
 * basic operations in bayesian networks such as matrix multiplication,
 * marginalization, etc.
 *
 * @author manuel
 * @author fjdiez
 * @version 1.0
 * @see TablePotential
 * @since OpenMarkov 1.0
 */
public class PotentialOperations {
    
    //	public static long lines; // Only for test
    
    // Constructor
    
    /**
     * Don't let anyone instantiate this class.
     */
    private PotentialOperations() {
    }
    
    /**
     * @param potential           Potential
     * @param variablesOfInterest List of the variables of interest
     * @return Marginalized potential
     */
    public static Potential marginalize(Potential potential, List<Variable> variablesOfInterest)
            {
        
        // Obtain parameters to invoke multiplyAndMarginalize
        // Union of the variables of the potential list
        List<Variable> variables = potential.getVariables();
        
        // parameters correct type verification before calling right method
        if (!(potential instanceof TablePotential)) {
            throw new NotSupportedOperationException("can only manage potentials of type TablePotential");
        }
        if (!hasFiniteStates(variables)) {
            throw new NotSupportedOperationException("can only manage variables of type FSVariable");
        }
        
        List<Variable> variablesToKeep = new ArrayList<>();
        List<Variable> variablesToEliminate = new ArrayList<>();
        
        for (Variable variable : variables) {
            if (variablesOfInterest.contains(variable)) {
                variablesToKeep.add(variable);
            } else {
                variablesToEliminate.add(variable);
            }
        }
        
        List<TablePotential> potentials = new ArrayList<>();
        potentials.add((TablePotential) potential);
        
        return DiscretePotentialOperations.multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }
    
    /**
     * @param potential            that will be marginalized
     * @param variablesToKeep      List of variables to keep
     * @param variablesToEliminate Listt of the variables to eliminate
     * @return Marginalized potential
     *                                     Condition: variablesToKeep + variablesToEliminate =
     *                                     potential.getVariables()
     *                                     Condition: variablesToKeep
     */
    public static Potential marginalize(Potential potential, List<Variable> variablesToKeep,
                                        List<Variable> variablesToEliminate) {
        
        // params correct type verification before calling right method
        if (!(potential instanceof TablePotential)) {
            throw new NotSupportedOperationException("can only manage potentials of type TablePotential");
        }
        if (!hasFiniteStates(variablesToKeep) || !hasFiniteStates(variablesToEliminate)) {
            throw new NotSupportedOperationException("can only manage variables of type FSVariable");
        }
        
        List<TablePotential> potentials = new ArrayList<>();
        potentials.add((TablePotential) potential);
        
        return DiscretePotentialOperations.multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }
    
    /**
     * @param potentials           List of table potentials
     * @param variablesToEliminate List of the variables to eliminate
     * @return Processed potential
     */
    public static Potential multiplyAndEliminate(List<TablePotential> potentials, List<Variable> variablesToEliminate)
            {
        if (!hasFiniteStates(variablesToEliminate)) {
            throw new NotSupportedOperationException("can only manage variables of type FSVariable");
        }
        
        // Obtain parameters to invoke multiplyAndMarginalize
        // Union of the variables of the potential list
        List<Variable> variablesToKeep = AuxiliaryOperations.getUnionVariables(potentials);
        variablesToKeep.removeAll(variablesToEliminate);
        
        return DiscretePotentialOperations.multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }
    
    /**
     * @param potentials          List of table potentials
     * @param variableToEliminate Variable to eliminate
     * @return Processed potential

     */
    public static Potential multiplyAndEliminate(List<TablePotential> potentials, Variable variableToEliminate)
            {
        return multiplyAndEliminate(potentials, Arrays.asList(variableToEliminate));
    }
    
    /**
     * @param potentials potentials array to multiply
     * @return The multiplied potentials

     */
    @SuppressWarnings("unchecked") public static Potential multiply(List<? extends Potential> potentials)
            {
        // correct type verification of parameters before calling method.
        if (!AuxiliaryOperations.checkObjectsCollectionType(potentials, TablePotential.class)) {
            throw new NotSupportedOperationException("can only manage variables of type TablePotential");
        }
        return DiscretePotentialOperations.multiply((List<TablePotential>) potentials);
    }
    
    /**
     * @param potentials          potentials array to multiply
     * @param variablesOfInterest Set of variables that must be kept (although
     *                            this set may contain some variables that are not in any potential)
     *                            {@code potentials}
     * @return The multiplied potentials

     */
    public static Potential multiplyAndMarginalize(List<TablePotential> potentials, List<Variable> variablesOfInterest)
            {
        // Obtain parameters to invoke multiplyAndMarginalize
        // Union of the variables of the potential list
        List<Variable> unionVariables = AuxiliaryOperations.getUnionVariables(potentials);
        
        // params correct type verification before calling right method
        if (!AuxiliaryOperations.checkObjectsCollectionType(potentials, TablePotential.class)) {
            throw new NotSupportedOperationException("can only manage variables of type TablePotential");
        }
        if (!hasFiniteStates(unionVariables)) {
            throw new NotSupportedOperationException("can only manage variables of type FSVariable");
        }
        
        // Classify unionVariables in two possibles arrays
        List<Variable> variablesToKeep = new ArrayList<>();
        List<Variable> variablesToEliminate = new ArrayList<>();
        for (Variable variable : unionVariables) {
            if (variablesOfInterest.contains(variable)) {
                variablesToKeep.add(variable);
            } else {
                variablesToEliminate.add(variable);
            }
        }
        
        return DiscretePotentialOperations.multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }
    
    /**
     * Multiplies several potentials and maximizes the result removing
     * variables that does not belong to {@code variablesOfInterest}
     *
     * @param potentials          potentials array to multiply
     * @param variablesOfInterest Set of variables that must be kept (although
     *                            this set may contain some variables that are not in any potential)
     *                            {@code potentials}
     * @return The multiplied potentials

     */
    public static Object[] multiplyAndMaximize(List<Potential> potentials, List<Variable> variablesOfInterest)
            {
        
        // Obtain parameters to invoke multiplyAndMarginalize
        // Union of the variables of the potential list
        List<Variable> unionVariables = AuxiliaryOperations.getUnionVariables(potentials);
        
        // params correct type verification before calling right method
        if (!AuxiliaryOperations.checkObjectsCollectionType(potentials, TablePotential.class)) {
            throw new NotSupportedOperationException("can only manage variables of type TablePotential");
        }
        
        if (!AuxiliaryOperations.checkVariablesCollectionType(unionVariables, VariableType.FINITE_STATES)) {
            throw new NotSupportedOperationException("can only manage variables of type FSVariable");
        }
        
        // Classify unionVariables in two possibles arrays
        List<Variable> variablesToKeep = new ArrayList<>();
        List<Variable> variablesToEliminate = new ArrayList<>();
        for (Variable variable : unionVariables) {
            if (variablesOfInterest.contains(variable)) {
                variablesToKeep.add(variable);
            } else {
                variablesToEliminate.add(variable);
            }
        }
        
        return DiscretePotentialOperations
                .multiplyAndMaximize(potentials, variablesToKeep, variablesToEliminate.get(0));
    }
    
    /**
     * @param potentials           array to multiply
     * @param variablesToKeep      The set of variables that will appear in the
     *                             resulting potential
     * @param variablesToEliminate The set of variables eliminated by
     *                             marginalization (in general, by summing out or maximizing)
     * @return result the multiplied potentials

     *                                     Condition: variablesToKeep and variablesToEliminate are a partition of
     *                                     the union of the variables of the potentials
     */
    public static Potential multiplyAndMarginalize(List<TablePotential> potentials, List<Variable> variablesToKeep,
                                                   List<Variable> variablesToEliminate) {
        // For test purposes only:
        /* Pruebas.numPotentialOperations++; */
        
        // params correct type verification before calling right method
        if (!AuxiliaryOperations.checkObjectsCollectionType(potentials, TablePotential.class)) {
            throw new NotSupportedOperationException("can only manage variables of type TablePotential");
        }
        if (!hasFiniteStates(variablesToKeep) || !hasFiniteStates(variablesToEliminate)) {
            throw new NotSupportedOperationException("can only manage variables of type FSVariable");
        }
        
        return DiscretePotentialOperations.multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }
    
    /**
     * Gets a uniform {@code Potential} object for the variable specified.
     *
     * @param probNet     the {@code probNet} object that contains the variable
     * @param variable    the {@code Variable} object.
     * @param auxNodeType the nodeType of the node that match the variable.
     * @return a new UniformPotential.
     */
    public static Potential getUniformPotential(ProbNet probNet, Variable variable, NodeType auxNodeType) {
        
        List<Variable> variables = new ArrayList<>();
        variables.add(variable);
        for (Node node : probNet.getParents(probNet.getNode(variable))) {
            variables.add(node.getVariable());
        }
        PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
        if (auxNodeType == NodeType.DECISION) {
            role = PotentialRole.POLICY;
        }
        return new UniformPotential(variables, role);
    }
    
    public static @NotNull ArrayList<Variable> variableAndParents(ProbNet probNet, Variable variable) {
        return new ArrayList<>(Stream.concat(Stream.of(variable),
                                             probNet.getParents(probNet.getNode(variable))
                                                    .stream()
                                                    .map(Node::getVariable)).toList());
    }
    
    private static boolean hasFiniteStates(List<Variable> variables) {
        boolean result = true;
        int i = 0;
        while (result && i < variables.size()) {
            result = variables.get(i).getVariableType() == VariableType.DISCRETIZED
                    || variables.get(i).getVariableType() == VariableType.FINITE_STATES;
            ++i;
        }
        return result;
    }
    
}
