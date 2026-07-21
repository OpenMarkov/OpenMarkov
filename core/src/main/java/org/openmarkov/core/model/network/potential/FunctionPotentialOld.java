/// *
// * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
// * Unless required by applicable law or agreed to in writing,
// * this code is distributed on an "AS IS" basis,
// * WITHOUT WARRANTIES OF ANY KIND.
// */
package org.openmarkov.core.model.network.potential;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.expression.ReferencedExpression;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements a potential which is function of the values provided by the parents.
 * TODO Which parents???
 * Has they to be numeric or may we have "finites states" which have one value associated? For example male=1, female=0
 *
 * @author cmyago
 * @version 1.1 06/12/2019
 * @version 2 19/08/2022 - changed to mitigate nuisance variance and speed simulation creating only once the evaluator and the signature of sampling
 * 04/10/2023 FIXME Check license
 */
@PotentialType(names = "FunctionOld")
public class FunctionPotentialOld extends GLMPotential implements DESSimulablePotential {
    
    /**
     * The default function
     */
    
    public static final VariableExpression DEFAULT_FUNCTION = new VariableExpression(List.of(), "0");
    
    /**
     * The coefficient
     */
    protected static final double COEFFICIENT = 1;
    
    /**
     * Evaluates the function 19/08/2022 - changed to final field to speed the simulation
     */
    private final Evaluator evaluator = new Evaluator();
    
    /**
     * Creates a Function potential with the function by default
     *
     * @param variables - list with the node variable and their parents
     * @param role Potential role
     */
    public FunctionPotentialOld(List<Variable> variables, PotentialRole role) {
        super(variables, role, new VariableExpression[]{DEFAULT_FUNCTION}, new double[]{COEFFICIENT});
    }
    
    /**
     * Creates a Function potential with the function given by {@code function}
     *
     * @param variables - list with the node variable and their parents
     * @param role      - the role of the potential
     * @param function  - A string representing the function
     */
    public FunctionPotentialOld(List<Variable> variables, PotentialRole role, String function) {
        super(variables, role, new VariableExpression[]{new VariableExpression(variables, function)}, new double[]{COEFFICIENT});
    }
    
    /**
     * Creates a Function potential equal to {@code potential}
     *
     * @param potential - potential copied
     */
    public FunctionPotentialOld(FunctionPotentialOld potential) {
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
     * @return True if it is valid
     */
    public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
// 17/10/2020
//		return (
//				!variables.isEmpty() && variables.get(0).getVariableType() == VariableType.NUMERIC
//		);
        //FIXME check whether FunctionPotentialOld is necessary
        return false;
//		return (
//				!variables.isEmpty() && (variables.get(0).getVariableType() == VariableType.NUMERIC
//						|| variables.get(0).getVariableType() == VariableType.EVENT)
//		);
//
    }
    
    /**
     * Gets the unprocessed function of FunctionPotential
     *
     * @return the function contained in the FunctionPotential
     */
    public String getFunction() {
        return this.covariates[0].asStringExpression();
    }
    
    /**
     * Process and sets  {codefunction}
     *
     * @param function - The function (unprocessed) to be set
     */
    
    public void setFunction(String function) {
        setCovariates(new VariableExpression[]{new VariableExpression(this.variables, function)});
    }
    
    /**
     * Only throws NonProjectablePotentialException because this potential cannot be projected to a table
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    @Override public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                                          List<TablePotential> projectedPotentials) throws NonProjectablePotentialException {
        //15/01/2023 This method is called when removing a node with this potential;
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
        
    }
    
    @Override public Potential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    @Override
    protected TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, double[] coefficients, VariableExpression[] covariates, List<Variable> evidencelessVariables, Map<Variable, String> variableValues) throws NonProjectablePotentialException {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
    }
    
    /**
     * Only throws NonProjectablePotentialException because this potential cannot be projected to a table
     *
     * @throws NonProjectablePotentialException NonProjectablePotentialException
     */
    @Override protected TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions,
                                                          double[] coefficients, String[] covariates, List<Variable> evidencelessVariables,
                                                          Map<String, String> variableValues) throws NonProjectablePotentialException {
        throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
        
    }
    
    @Override public Potential copy() {
        return new FunctionPotentialOld(this);
    }
    
    /**
     * Multiplies function by {@code scale}
     *
     * @param scale - the scale factor
     */
    @Override public void scalePotential(double scale) {
// 24/10/2023 'Double(double)' is deprecated and marked for removal
//		String scaleString = new Double(scale).toString();
//
        String scaleString = Double.toString(scale);
        String function = scaleString.concat("*").concat(covariates[0].asStringExpression());
        covariates[0] = new VariableExpression(variables, function);
    }
    
    /**
     * Adds the variable to the new potential. The function does not change
     *
     * @param variable - the variable to be added
     * @return a FunctionPotential with the new variabla
     */
    @Override public Potential addVariable(Variable variable) {
        FunctionPotentialOld newPotential = null;
        //18/03/2023 -- for self-loop in DESnets; added check with conditioned variable; FIXME this can happen when it is not a DESnet?
        if (!(variables.subList(1, variables.size()).contains(variable))) {
            //
            List<Variable> newVariables = new ArrayList<>(variables);
            newVariables.add(variable);
            newPotential = new FunctionPotentialOld(newVariables, this.role);
            newPotential.setCovariates(this.covariates);
            newPotential.setCoefficients(new double[]{1});
        } else {
            newPotential = new FunctionPotentialOld(this);
        }
        return newPotential;
    }
/*
Potential#removeVariable changes the potential to Uniform and org.openmarkov.core.action.RemoveLinkEdit.doEdit then checks
if the potential is projectable. If the potential is not, does not remove the link properly. I do not know the reason, so I do not change it.
As FunctionPotential is not projectable I leave the default behaviour
 */
//	/**
//	 * Removes a variable from FunctionPotential. If the function does not use the variable,
//	 * the function does not change, otherwise the function is set to its default value
//	 *
//	 * @param variable - the variable to be removed
//	 * @returns a FunctionPotential without the variable
//	 */
//	@Override public Potential removeVariable(Variable variable) {
//		if (variables.contains(variable)) {
//			List<Variable> newVariables = new ArrayList<>(variables);
//			newVariables.remove(variable);
//			int index = variables.indexOf(variable);
//			String variableToRemove = "#{v" + index + "}";
//			if (processedCovariates[0].contains(variableToRemove)) {
//				return new FunctionPotential(newVariables, this.role);
//			}
//		}
//		return new FunctionPotential(this);
//	}
    
    /**
     * Removes a variable from FunctionPotential. If the function does not use the variable,
     * the function does not change, otherwise the function is set to its default value
     *
     * @param variable - the variable to be removed
     * @return a FunctionPotential without the variable
     */
    @Override public Potential removeVariable(Variable variable) {
        if (variables.contains(variable)) {
            List<Variable> newVariables = new ArrayList<>(variables);
            newVariables.remove(variable);
            int index = variables.indexOf(variable);
            String variableToRemove = "#{v" + index + "}";
            if (covariates[0].asStringExpression().contains(variableToRemove)) {
                return new FunctionPotentialOld(newVariables, this.role);
            }
        }
        return new FunctionPotentialOld(this);
    }
    
    @Override public Potential deepCopy(ProbNet copyNet) {
        return super.deepCopy(copyNet);
    }
    
    @Override public String toString() {
        return unprocessCovariates(variables, Arrays.stream(covariates)
                                                    .map(ReferencedExpression::asStringExpression)
                                                    .toArray(String[]::new))[0];
    }
    
    /**
     * Always returns false because there is no uncertainty
     */
    
    @Override public boolean isUncertain() {
        return false;
    }

// 19/08/2022 - used double instead of Random and evaluator object only create once
    
    /**
     * @param values Values
     * @return The value obtained by evaluation the function for the assignment of variables given by 'values'
     * @throws EvaluationException EvaluationException
     */
    public String getValue(Map<Variable, String> values) throws EvaluationException, NonProjectablePotentialException.CannotEvaluate, NonProjectablePotentialException.CannotResolveVariable {
        return this.covariates[0].evaluateWith(values);
    }
    
    @Override
    public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {
        List<Variable> parentVariables = parents.getVariables();
        
        Map<Variable, String> variablesMap = new HashMap();
        
        for (Variable parentVariable : parentVariables) {
            int index = variables.indexOf(parentVariable);
            variablesMap.put(parentVariable, "" + parents.getFinding(parentVariable).getNumericalValue());
        }
        return Double.parseDouble(this.covariates[0].evaluateWith(variablesMap));
    }


//
    
    /**
     * Not implemented: returns {@code null} instead of throwing
     * {@code UnsupportedOperationException} as the base implementation does.
     */
    @Override
    public Potential reorder(List<Variable> newOrderOfVariables) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Not implemented: returns {@code null} instead of throwing
     * {@code UnsupportedOperationException} as the base implementation does.
     */
    @Override
    public Potential reorder(Variable variable, State[] newOrder) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
