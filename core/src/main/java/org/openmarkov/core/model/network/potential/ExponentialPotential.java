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
 * A GLM potential that applies the exponential link function: the conditioned
 * variable's value is {@code exp(linear_combination)}. Used for modeling rates,
 * counts, and other positive-valued quantities.
 */
@PotentialType(names = "Exponential") public class ExponentialPotential extends GLMPotential {
    
    public ExponentialPotential(List<Variable> variables, PotentialRole role) {
        super(variables, role);
    }
    
    //	public ExponentialPotential(Variable utilityVariable, List<Variable> variables) {
    //		this(variables, PotentialRole.UTILITY);
    //		this.utilityVariable = utilityVariable;
    //	}
    
    public ExponentialPotential(List<Variable> variables, PotentialRole role, VariableExpression[] covariates,
                                double[] coefficients) {
        super(variables, role, covariates, coefficients);
    }
    
    public ExponentialPotential(ExponentialPotential potential) {
        super(potential);
    }
    
    /**
     * Returns if an instance of a certain Potential type makes sense given the
     * variables and the potential role.
     *
     * @param node      . {@code Node}
     * @param variables . {@code List} of {@code Variable}.
     * @param role      . {@code PotentialRole}.
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
        if (node.getProbNet().getNetworkType() instanceof DESNetworkType) {
            return false;
        }
        return role == PotentialRole.UNSPECIFIED || (!variables.isEmpty() && variables.getFirst().getVariableType()
                == VariableType.NUMERIC
        );
    }
    
    /**
     * Projecting to a generic {@link Potential} is not supported; use the
     * {@code tableProject} methods instead.
     *
     * @throws NotSupportedOperationException always
     */
    @Override
    public Potential project(EvidenceCase evidenceCase) {
        throw new NotSupportedOperationException();
    }
    
    @Override protected TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                                    double[] coefficients, VariableExpression[] covariates, List<Variable> evidencelessVariables,
                                                    Map<Variable, String> variableValues) throws NonProjectablePotentialException.CannotEvaluate, NonProjectablePotentialException.CannotResolveVariable {
        // Fill arrays numericValues and evidencelessVariables
        int constantIndex = getConstantIndex(covariates);
        
        List<Variable> projectedPotentialVariables = new ArrayList<>(evidencelessVariables);
        projectedPotentialVariables.addFirst(variables.getFirst());
        TablePotential projectedPotential = new TablePotential(projectedPotentialVariables, role);
        Variable conditionedVariable = getConditionedVariable();
        int numStates = conditionedVariable.getNumStates();
        int parentFirstIndex = (conditionedVariable == projectedPotentialVariables.getFirst()) ? 1 : 0;
        int[] offsets = projectedPotential.getOffsets();
        int[] dimensions = projectedPotential.getDimensions();
        for (int i = 0; i < projectedPotential.getValues().length; i += numStates) {
            // Set the values of variables without evidence
            for (int j = parentFirstIndex; j < projectedPotentialVariables.size(); ++j) {
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
                double covariateValue;
                if (j != constantIndex) {
                    covariateValue = Double.parseDouble(covariates[j].evaluateWith(variableValues));
                        regression += covariateValue * coefficients[j];
                }
            }
            projectedPotential.getValues()[i] = Math.exp(regression);
        }
        return projectedPotential;
    }
    
    @Override
    protected TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, double[] coefficients, String[] covariates, List<Variable> evidencelessVariables, Map<String, String> variableValues) throws NonProjectablePotentialException {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    @Override public Potential copy() {
        return new ExponentialPotential(this);
    }
    
    @Override public String toString() {
        return super.toString() + " = Exponential";
    }
    
    @Override public void scalePotential(double scale) {
        /*
         * Add ln(scale) to the first coefficient (constant covariate) is the same as
         * multiply all the exponential potential by the scale
         */
        coefficients[0] += Math.log(scale);
        
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        return super.deepCopy(copyNet);
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
