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
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements a function potential consisting of a GLMPotential
 * with only one covariate with coefficient=1
 *
 * @author carmenyago
 * @version 1.0 2016
 */
@PotentialType(names = "Function")
public class FunctionPotential extends GLMPotential implements DESSimulablePotential {
    
    /**
     * The default function
     */
    public static final VariableExpression DEFAULT_FUNCTION = new VariableExpression(Collections.emptyList(), "0");
    
    /**
     * The coefficient
     */
    protected static final double COEFFICIENT = 1;
    
    /**
     * Creates a Function potential with the function by default
     *
     * @param variables - list with the node variable and their parents
     * @param role      Potential role
     */
    public FunctionPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role, new VariableExpression[]{DEFAULT_FUNCTION}, new double[]{COEFFICIENT});
    }
    
    /**
     * Creates a Function potential with the function given by {@code function}
     *
     * @param variables - list with the node variable and their parents
     * @param role      - the role of the potential
     * @param function  - A string representing the function
     */
    public FunctionPotential(List<Variable> variables, PotentialRole role, VariableExpression function) {
        super(variables, role, new VariableExpression[]{function}, new double[]{COEFFICIENT});
    }
    
    /**
     * Creates a Function potential equal to {@code potential}
     *
     * @param potential - potential copied
     */
    public FunctionPotential(FunctionPotential potential) {
        super(potential);
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     * UNCLEAR--&#62; Should the parents be numeric
     *
     * @param node      . {@code Node}
     * @param variables . {@code ArrayList} of {@code Variable}.
     * @param role      . {@code PotentialRole}.
     *
     * @return True if it is valid
     */
    
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        return (
                !variables.isEmpty() && (variables.get(0).getVariableType() == VariableType.NUMERIC
                        || node.getNodeType() == NodeType.EVENT)
        );
    }
    
    @Override
    public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents)  {
        List<Variable> parentVariables = parents.getVariables();
        Map<Variable, String> variablesMap = new HashMap<>();
        double result =0;
        for (Variable parentVariable:parentVariables){
            variablesMap.put(parentVariable, ""+parents.getFinding(parentVariable).getNumericalValue());
        }
        try {
            result = Double.parseDouble(this.covariates[0].evaluateWith(variablesMap));
        } catch (NonProjectablePotentialException.CannotEvaluate |
                 NonProjectablePotentialException.CannotResolveVariable e) {
            throw new UnrecoverableException(e);
        }
        return  result;
    }
    
    /**
     * Gets the unprocessed function of FunctionPotential
     *
     * @return the function contained in the FunctionPotential
     */
    public VariableExpression getFunction() {
        return covariates[0];
    }
    
    /**
     * Process and sets  {codefunction}
     *
     * @param function - The function (unprocessed) to be set
     */
    
    public void setFunction(VariableExpression function) {
        setCovariates(new VariableExpression[]{function});
    }
    
    /**
     * Only throws NonProjectablePotentialException because this potential cannot be projected to a table
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    @Override
    public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> projectedPotentials) throws NonProjectablePotentialException.PotentialCannotBeConvertedToATable {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    @Override
    public Potential project(EvidenceCase evidenceCase) {
        throw new NotSupportedOperationException();
    }
    
    /**
     * Only throws NonProjectablePotentialException because this potential cannot be projected to a table
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    @Override protected TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                                    double[] coefficients, VariableExpression[] covariates, List<Variable> evidencelessVariables,
                                                    Map<Variable, String> variableValues) throws NonProjectablePotentialException.PotentialCannotBeConvertedToATable {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    @Override
    protected TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, double[] coefficients, String[] covariates, List<Variable> evidencelessVariables, Map<String, String> variableValues) throws NonProjectablePotentialException {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    @Override public Potential copy() {
        return new FunctionPotential(this);
    }
    
    /**
     * Multiplies function by {@code scale}
     *
     * @param scale - the scale factor
     */
    @Override public void scalePotential(double scale) {
        String scaleString = String.valueOf(scale);
        covariates[0] = new VariableExpression(this.variables, scaleString + "*" + this.covariates[0].asStringExpression());
    }
    
    /**
     * Adds the variable to the new potential. The function does not change
     *
     * @param variable - the variable to be added
     *
     * @return a FunctionPotential with the new variabla
     */
    @Override public Potential addVariable(Variable variable) {
        FunctionPotential newPotential;
        if (!variables.contains(variable)) {
            List<Variable> newVariables = new ArrayList<>(variables);
            newVariables.add(variable);
            newPotential = new FunctionPotential(newVariables, this.role);
            newPotential.setCovariates(covariates);
            newPotential.setCoefficients(new double[]{1});
        } else {
            newPotential = new FunctionPotential(this);
        }
        return newPotential;
    }
    
    /**
     * Removes a variable from FunctionPotential. If the function does not use the variable,
     * the function does not change, otherwise the function is set to its default value
     *
     * @param variable - the variable to be removed
     *
     * @return a FunctionPotential without the variable
     */
    @Override public Potential removeVariable(Variable variable) {
        if (variables.contains(variable)) {
            List<Variable> newVariables = new ArrayList<>(variables);
            newVariables.remove(variable);
            if (covariates[0].references().contains(variable)) {
                return new FunctionPotential(newVariables, this.role);
            }
        }
        return new FunctionPotential(this);
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        return super.deepCopy(copyNet);
    }
    
    @Override public String toString() {
        return covariates[0].toString();
    }
    
    /**
     * Always returns false because there is no uncertainty
     */
    
    @Override public boolean isUncertain() {
        return false;
    }
    
    
    /** Expression-based potential; variable-name-based, not index-based; returns a copy. */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        return copy();
    }
    
    /** Expression-based potential; variable-name-based, not index-based; returns a copy. */
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        return copy();
    }
    
}
