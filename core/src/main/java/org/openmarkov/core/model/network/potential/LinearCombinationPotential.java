/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.expression.VariableExpression;
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
import java.util.List;
import java.util.Map;

/**
 * A GLM potential using the identity link function: the conditioned variable's
 * value equals a linear combination of covariates and their coefficients.
 * Also known as "linear regression" potential. Used for numeric or discretized
 * conditioned variables.
 *
 * @author Manuel Arias
 */
@PotentialType(names = {"Linear combination", "Linear regression"})
public class LinearCombinationPotential extends GLMPotential implements Scalable {
    
    public LinearCombinationPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role, getDefaultCovariates(variables, role), new double[variables.size()]);
    }
    
    public LinearCombinationPotential(List<Variable> variables, PotentialRole role, VariableExpression[] covariates,
                                      double[] coefficients) {
        super(variables, role, covariates, coefficients);
    }
    
    public LinearCombinationPotential(LinearCombinationPotential potential) {
        super(potential);
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     *
     * @param node      . {@code Node}
     * @param variables . {@code ArrayList} of {@code Variable}.
     * @param role      . {@code PotentialRole}.
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        if (node.getProbNet().getNetworkType() instanceof DESNetworkType) {
            return false;
        }
        return role == PotentialRole.UNSPECIFIED || (
                !variables.isEmpty() && variables.getFirst().getVariableType() == VariableType.NUMERIC
        );
    }
    
    /**
     * Not supported by this potential; use the {@code tableProject} family instead.
     *
     * @throws NotSupportedOperationException always.
     */
    @Override
    public Potential project(EvidenceCase evidenceCase) {
        throw new NotSupportedOperationException();
    }
    
    /**
     * Projects this potential by evaluating, for every configuration of the
     * evidenceless variables, the linear combination of covariates and coefficients
     * (the identity-link regression value) and storing it in the resulting table.
     */
    @Override
    protected TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                                    double[] coefficients, VariableExpression[] covariates, List<Variable> evidencelessVariables,
                                                    Map<Variable, String> variableValues) throws NonProjectablePotentialException.CannotEvaluate, NonProjectablePotentialException.CannotResolveVariable {
        Variable conditionedVariable = getConditionedVariable();
        int numStates = conditionedVariable.getNumStates();
        // Fill arrays numericValues and evidencelessVariables
        
        int constantIndex = getConstantIndex(covariates);
        
        List<Variable> projectedPotentialVariables = new ArrayList<>(evidencelessVariables);
        projectedPotentialVariables.addFirst(variables.getFirst());
        TablePotential projectedPotential = new TablePotential(projectedPotentialVariables, role);
        
        int[] offsets = projectedPotential.getOffsets();
        int[] dimensions = projectedPotential.getDimensions();
        int firstParentIndex = 1;
        for (int i = 0; i < projectedPotential.getValues().length; i += numStates) {
            // Set the values of variables without evidence
            for (int j = firstParentIndex; j < projectedPotentialVariables.size(); ++j) {
                Variable variable = projectedPotentialVariables.get(j);
                int index = (i / offsets[j]) % dimensions[j];
                double value = index;
                try {
                    value = Double.parseDouble(variable.getStates()[index].getName());
                } catch (NumberFormatException e) {
                    // ignore
                }
                variableValues.put(variable, String.valueOf(value));
            }
            double regression = coefficients[constantIndex];
            for (int j = 0; j < coefficients.length; ++j) {
                
                if (j != constantIndex) {
                    double covariateValue = Double.parseDouble(covariates[j].evaluateWith(variableValues));
                        regression += covariateValue * coefficients[j];
                }
            }
            if (getConditionedVariable().getVariableType() == VariableType.NUMERIC) {
                projectedPotential.getValues()[i] = regression;
            } else {
                int stateIndex = getConditionedVariable().getStateIndex(regression);
                for (int j = 0; j < numStates; ++j) {
                    projectedPotential.getValues()[i + j] = (j == stateIndex) ? 1 : 0;
                }
            }
        }
        return projectedPotential;
    }
    
    @Override
    protected TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, double[] coefficients, String[] covariates, List<Variable> evidencelessVariables, Map<String, String> variableValues) throws NonProjectablePotentialException {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    
    @Override
    public Potential copy() {
        return new LinearCombinationPotential(this);
    }
    
    @Override
    public void scalePotential(double scale) {
        // Multiply all the coefficients by the scale
        for (int i = 0; i < coefficients.length; i++) {
            coefficients[i] *= scale;
        }
    }

    /** Implements {@link Scalable#scale(double)}; delegates to {@link #scalePotential(double)}. */
    @Override
    public void scale(double factor) {
        scalePotential(factor);
    }
    
    @Override
    public Potential addVariable(Variable variable) {
        LinearCombinationPotential newPotential;
        if (!variables.contains(variable)) {
            List<Variable> newVariables = new ArrayList<>(variables);
            newVariables.add(variable);
            newPotential = new LinearCombinationPotential(newVariables, this.role);
            VariableExpression[] newCovariates = new VariableExpression[covariates.length + 1];
            System.arraycopy(covariates, 0, newCovariates, 0, covariates.length);
            newCovariates[covariates.length] = new VariableExpression(newVariables, "{" + variable.getName() + "}");
            newPotential.setCovariates(newCovariates);
            
            double[] newCoefficients = new double[coefficients.length + 1];
            System.arraycopy(coefficients, 0, newCoefficients, 0, coefficients.length);
            newCoefficients[coefficients.length] = 0.0;
            newPotential.setCoefficients(newCoefficients);
        } else {
            newPotential = new LinearCombinationPotential(this);
        }
        return newPotential;
        
    }
    
    @Override
    public Potential removeVariable(Variable variable) {
        LinearCombinationPotential newPotential;
        if (variables.contains(variable)) {
            List<Variable> newVariables = new ArrayList<>(variables);
            newVariables.remove(variable);
            newPotential = new LinearCombinationPotential(newVariables, this.role);
            //List<String> newCovariates = new ArrayList<>();
            List<Double> newCoefficients = new ArrayList<>();
            
            double[] newCoefficientsArray = new double[newCoefficients.size()];
            for (int i = 0; i < newCoefficients.size(); ++i) {
                newCoefficientsArray[i] = newCoefficients.get(i);
            }
            
            newPotential.setCoefficients(newCoefficientsArray);
        } else {
            newPotential = new LinearCombinationPotential(this);
        }
        return newPotential;
    }
    
    @Override
    public Potential deepCopy(ProbNet copyNet) {
        return super.deepCopy(copyNet);
    }
    
    @Override
    public String toString() {
        String out = super.toString() + " = ";
        VariableExpression[] covariates = this.covariates;
        boolean first = true;
        for (int i = 0; i < covariates.length; ++i) {
            if (this.coefficients[i] != 0.0) {
                if (!first)
                    out += " + ";
                first = false;
                if (this.coefficients[i] != 1.0)
                    out += this.coefficients[i] + "*";
                out += covariates[i];
            }
        }
        return out;
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
