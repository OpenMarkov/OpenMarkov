/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;


import org.junit.jupiter.api.Disabled;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.Tools;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * @author mluque
 * @author Manuel Arias
 * @author ibermejo
 */
@Disabled public abstract class InferenceTaskTest extends InferenceAlgorithmTest {
    
    /**
     * Maximum error allowed in tests. It could be modified by subclasses
     * if it is necessary (for example, approximate inference methods).
     */
    protected static final double maxError = 0.0001;
    
    /**
     * @param network
     * @param variableName
     * @return The variable in 'network' whose name is 'variableName'. It also checks whether the variable is not null.
     */
    public static Variable getVariableAndAssertNotNull(ProbNet network, String variableName) {
        
        Variable variable = network.getVariable(variableName);
        assertNotNull(variable);
        return variable;
    }
    
    /**
     * @param pot a table potential
     * Checks if 'pot' is a conditional probability potential correctly defined: the values in each column sum 1.0.
     */
    public static void checkIsAConditionalProbability(TablePotential pot) {
        
        double[] potValues = pot.getValues();
        int numStates = pot.getVariable(0).getNumStates();
        double[] auxValues = new double[numStates];
        int numColumns = potValues.length / numStates;
        int posInValues = 0;
        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numStates; j++) {
                auxValues[j] = potValues[posInValues];
                posInValues++;
            }
            assertEquals(1.0, Tools.sum(auxValues), maxError);
        }
    }
    
    /**
     * @param potA First potential in the comparison
     * @param potB Second potential in the comparison
     * @return true if potA and potB are equal (variables can be in different order)
     */
    @Override protected boolean areEqualPotentials(TablePotential potA, TablePotential potB) {
        
        List<Variable> varsA = potA.getVariables();
        List<Variable> varsB = potB.getVariables();
        
        boolean areEqual = varsA.size() == varsB.size();
        
        if (areEqual) {
            for (int i = 0; i < varsB.size() && areEqual; i++) {
                areEqual = varsA.contains(varsB.get(i));
            }
            int size = potA.getTableSize();
            
            for (int i = 0; i < size && areEqual; i++) {
                double valueA = potA.getValues()[i];
                double valueB = potB.getValue(varsA, potA.getConfiguration(i));
                areEqual = Math.abs(valueA - valueB) < maxError;
                
            }
            
        }
        return areEqual;
        
    }

    /**
     * @param aPosterioriProbs
     * @param variables
     * @param expectedProbs
     * The probabilities in expectedProbs are given as independent numbers for each variable:
     * It means that, if n is the number of states of a variable, then n-1 probabilities are given
     * for it in 'expectedProbs'. And the values are ordered according the order in 'variables'.
     * In the particular case when all variables are binary then 'variables' and 'expectedProbs' have
     * the same size.
     * */
    @Override
    protected void checkProbabilities(Map<Variable, TablePotential> aPosterioriProbs, ArrayList<Variable> variables,
                                      double[] expectedProbs) {
        
        int size = variables.size();
        
        int indexBaseProbs = 0;
        for (Variable auxVar : variables) {
            int numStates = auxVar.getNumStates();
            int numProbsAux = numStates - 1;
            double[] auxExpectedProbs = new double[numProbsAux];
            System.arraycopy(expectedProbs, indexBaseProbs + 0, auxExpectedProbs, 0, numProbsAux);
            checkProbabilityPotential(aPosterioriProbs, auxVar, auxExpectedProbs);
            indexBaseProbs = indexBaseProbs + numProbsAux;
        }
        
    }
    
    /**
     * @param probabilities
     * @param variableX
     * @param x
     * Checks if the probability potential of 'variableX' in 'probabilities' is equal
     * to x[0],..,x[n], where 'n' is the number of states of 'variableX'
     */
    @Override protected void checkProbabilityPotential(Map<Variable, TablePotential> probabilities, Variable variableX,
                                                       double... x) {
        TablePotential X = probabilities.get(variableX);
        checkProbabilities(X, x);
        
    }
    
    /**
     * @param pot
     * @param values
     * Checks if the values of 'pot' (except the last one) is equal to 'values' and if the sum of
     * the probabilities in 'pot' is 1.0.
     */
    @Override protected void checkProbabilities(TablePotential pot, double... values) {
        
        double[] potValues = pot.getValues();
        int potValuesLength = potValues.length;
        assertEquals(values.length + 1, potValuesLength);
        double sum = 0.0;
        for (int i = 0; i < potValuesLength - 1; i++) {
            double expected = values[i];
            double actual = potValues[i];
            assertEquals(expected, actual, maxError);
            sum = sum + expected;
        }
        assertEquals(1.0 - sum, potValues[potValuesLength - 1], maxError);
    }
    
    @Override protected boolean areEquals(double[] v1, double[] v2) {
        
        int v1length = v1.length;
        
        boolean areEquals = (v1length == v2.length);
        int i = 0;
        while (areEquals && i < v1length) {
            areEquals = Math.abs(v1[i] - v2[i]) < maxError;
            i = i + 1;
        }
        return areEquals;
        
    }
    
    protected boolean checkPolicy(TablePotential policy, Variable d, int numVar) {
        List<Variable> domainPolicy = policy.getVariables();
        domainPolicy.remove(d);
        return (numVar == domainPolicy.size());
    }
    
    protected TablePotential constructExpectedUtilitiesPolicyDDecisionTestProblem(Variable variableT,
                                                                                  Variable variableY, Variable variableD) {
        
        ArrayList<Variable> variables = new ArrayList<>();
        variables.add(variableT);
        variables.add(variableY);
        variables.add(variableD);
        
        TablePotential pot = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        double[] values = {81.04585153, 0.0, 87.93064729, 0.0, -2.0, 89.3, 49.3209607, 0.0, 97.51453104, 0.0, -2.0,
                95.1};
        pot.setValues(values);
        return pot;
    }
    
    protected TablePotential constructExpectedUtilitiesPolicyTDecisionTestProblem(Variable variableT) {
        
        ArrayList<Variable> variables = new ArrayList<>();
        variables.add(variableT);
        
        TablePotential pot = new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        double[] values = {96.006, 95.1};
        pot.setValues(values);
        return pot;
    }
}
