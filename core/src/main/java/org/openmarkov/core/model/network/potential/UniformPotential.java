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
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Potential with discrete and/or continuous variables.
 *
 * @author Manuel Arias
 * @version 1.0
 */
@PotentialType(names = "Uniform") public class UniformPotential extends Potential implements Projectable {

    // Attributes
    /**
     * Value of a potential configuration when all the variables are discrete.
     */
    private double discreteValue = 0.0;
    
    // Constructors
    /**
     * @param variables {@code ArrayList} of {@code Variable}
     * @param role      {@code PotentialRole}
     */
    public UniformPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
        if (allVariablesAreDiscrete(variables)) {
            discreteValue = calculateDiscreteValue(variables);
        }
    }
    
    /**
     * @param role      {@code PotentialRole}
     * @param variables {@code Variable}
     */
    public UniformPotential(PotentialRole role, Variable... variables) {
        this(toList(variables), role);
    }
    
    /**
     * Copy constructor for UniformPotential
     *
     * @param potential Uniform potential
     */
    public UniformPotential(UniformPotential potential) {
        super(potential);
        if (allVariablesAreDiscrete(variables)) {
            discreteValue = calculateDiscreteValue(variables);
        }
    }
    
    // Methods
    /**
     * @param evidenceCase        {@code evidenceCase}
     * @param projectedPotentials List of projected potentials
     *
     * @return If this is a utility potential, it represents the case in
     * which all the utilities are zero; therefore, it suffices to return
     * an empty list. If this is a conditional probability P(Y|X1,...,Xn), it
     * returns a TablePotential that is uniform potential P(y).
     * If this is a joint probability, P(X1,...,Xn), it returns a
     * TablePotential that is equal to this potential.
     * In all cases, the argument evidenceCase is irrelevant.
     *
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials) throws NonProjectablePotentialException.PotentialCannotBeConvertedToATable {
        switch (this.role) {
            case LINK_RESTRICTION, UNSPECIFIED -> throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
            case CONDITIONAL_PROBABILITY, JOINT_PROBABILITY, POLICY -> {
                Variable conditionedVariable = variables.getFirst();
                boolean isNumeric = conditionedVariable.getVariableType() == VariableType.NUMERIC;
                if (isNumeric) {
                    throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
                }
                if (evidenceCase != null && evidenceCase.contains(conditionedVariable)) {
                    // returns a constant
                    TablePotential projectedPotential = new TablePotential(new ArrayList<>(), role);
                    projectedPotential.getValues()[0] = 1.0 / conditionedVariable.getNumStates();
                    return projectedPotential;
                }
                return createUniformTablePotential(evidenceCase, variables);
            }
        }
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    /** Not supported for this potential; always throws {@link NotSupportedOperationException}. */
    @Override
    public Potential project(EvidenceCase evidenceCase) {
        throw new NotSupportedOperationException();
    }
    
    private TablePotential createUniformTablePotential(EvidenceCase evidenceCase, List<Variable> vars) {
        List<Variable> potentialVariables = new ArrayList<>(vars);
        // the conditioned variable does not make part of the evidence
        if (evidenceCase != null) {
            potentialVariables.removeAll(evidenceCase.getVariables());
        }
        return new TablePotential(potentialVariables, getPotentialRole());
    }
    
    /**
     * @param variables {@code ArrayList} of {@code Variable}
     *
     * @return {@code true} if all the variables are FINITE_STATES.
     */
    private static boolean allVariablesAreDiscrete(List<Variable> variables) {
        for (Variable variable : variables) {
            if (variable.getVariableType() != VariableType.FINITE_STATES) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @param variables {@code ArrayList} of {@code Variable}
     *
     * @return 1 / multiplication of the number of states of conditioning
     * variables.
     */
    private static double calculateDiscreteValue(List<Variable> variables) {
        int statesSpace = 1;
        for (int i = 1; i < variables.size(); i++) {
            statesSpace *= variables.get(i).getNumStates();
        }
        return 1 / (double) statesSpace;
    }
    
    /**
     * @return discreteValue. {@code double}
     */
    public double getDiscreteValue() {
        return discreteValue;
    }
    
    /**
     * Used to apply discount rates in cost effectiveness analysis for utility
     * variables has no sense in chance nodes
     *
     * @param discreteValue Discrete value
     */
    public void setDiscreteValue(double discreteValue) {
        this.discreteValue = discreteValue;
    }
    
    @Override public Potential copy() {
        return new UniformPotential(this);
    }
    
    @Override public int sampleConditionedVariable(Random randomGenerator, Map<Variable, Integer> parentStateIndexes) {
        return randomGenerator.nextInt(variables.getFirst().getNumStates());
    }
    
    @Override public double getProbability(HashMap<Variable, Integer> sampledStateIndexes) {
        return 1.0 / variables.getFirst().getNumStates();
    }

    @Override public String toString() {
        return super.toString() + " = Uniform";
    }
    
    @Override public void scalePotential(double scale) {
    
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role
     *
     * @param node      {@code Node}
     * @param variables {@code ArrayList} of {@code Variable}
     * @param role      {@code PotentialRole}
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        //11/01/2023 FIXME Provisional; Potential for DESnets
        if ((node.getProbNet().getNetworkType() instanceof DESNetworkType)) return false;
        // TODO
        return true;
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        UniformPotential potential = (UniformPotential) super.deepCopy(copyNet);
        
        potential.setDiscreteValue(this.getDiscreteValue());
        return potential;
        
    }
    
    /** Uniform distribution is order-invariant; returns a copy. */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        return copy();
    }

    /** Uniform distribution is order-invariant; returns a copy. */
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        return copy();
    }
}
