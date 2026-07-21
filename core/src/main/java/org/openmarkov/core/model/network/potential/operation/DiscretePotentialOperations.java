/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.AbstractIndexedPotential;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.Collection;
import java.util.List;

/**
 * This class defines a set of common operations over discrete potentials (
 * {@code TablePotential}s) and discrete variables ({@code Variable} s). The
 * method are invoked from {@code PotentialOperations} after checking that the
 * parameters are discrete.
 *
 * @author Manuel Arias
 */
public final class DiscretePotentialOperations {
    
    /**
     * Round error used to compare two numbers. If they differ in less than
     * {@code maxRoundErrorAllowed} they will be considered equals.
     */
    public static final double maxRoundErrorAllowed = 1.0E-8;
    
    /**
     * @param tablePotentials {@code ArrayList} of extends {@code Potential}.
     *
     * @return A {@code TablePotential} as result.
     */
    public static TablePotential multiply(List<TablePotential> tablePotentials) {
        return TablePotentialArithmetic.multiply(tablePotentials);
    }
    
    /**
     * @param potentials {@code ArrayList} of extends {@code Potential}.
     *
     * @return A {@code TablePotential} as result.
     */
    public static TablePotential multiply(TablePotential... potentials) {
        return TablePotentialArithmetic.multiply(potentials);
    }
    
    /**
     * @param tablePotentials {@code ArrayList} of extends {@code Potential}.
     * @param reorder         Sorts or not the potentials prior to multiplication.
     *                        {@code boolean}.
     *
     * @return A {@code TablePotential} as result. //TODO
     */
    public static TablePotential multiply(List<TablePotential> tablePotentials, boolean reorder) {
        return TablePotentialArithmetic.multiply(tablePotentials, reorder);
    }
    
    /**
     * @param tablePotentials {@code List} of {@code TablePotential}s.
     *
     * @return {@code TablePotential}
     */
    public static TablePotential sum(List<TablePotential> tablePotentials) {
        return TablePotentialArithmetic.sum(tablePotentials);
    }
    
    public static TablePotential sum(TablePotential... tablePotentials) {
        return TablePotentialArithmetic.sum(tablePotentials);
    }

    /**
     * @param utilityPotentials {@code List} of {@code TablePotential}s.
     *
     * @return A TablePotential for each criterion
     */
    public static List<TablePotential> sumByCriterion(List<TablePotential> utilityPotentials) {
        return TablePotentialArithmetic.sumByCriterion(utilityPotentials);
    }

    /**
     * @param potentials Collection of potentials
     *
     * @return The potential role
     */
    public static PotentialRole getRole(Collection<? extends Potential> potentials) {
        return TablePotentialArithmetic.getRole(potentials);
    }
    
    /**
     * @param tablePotentials      array to multiply
     * @param variablesToKeep      The set of variables that will appear in the
     *                             resulting potential
     * @param variablesToEliminate The set of variables eliminated by
     *                             marginalization (in general, by summing out or
     *                             maximizing)
     *
     * @return A {@code TablePotential} result of multiply and marginalize.
     * Condition: variablesToKeep and variablesToEliminate are a partition
     * of the union of the variables of the potential
     */
    public static TablePotential multiplyAndMarginalize(Collection<TablePotential> tablePotentials,
                                                        List<Variable> variablesToKeep, List<Variable> variablesToEliminate) {
        return TablePotentialElimination.multiplyAndMarginalize(tablePotentials, variablesToKeep, variablesToEliminate);
    }
    
    /**
     * @param probPotential       probability potential
     * @param utilityPotential    utility potential
     * @param variableToEliminate The set of variables eliminated by marginalization
     *                            (in general, by summing out or maximizing)
     *
     * @return A {@code TablePotential} result of multiply and marginalize.
     * Condition: variablesToKeep and variablesToEliminate are a partition
     * of the union of the variables of the potential
     */
    public static TablePotential multiplyAndMarginalize(TablePotential probPotential, TablePotential utilityPotential,
                                                        Variable variableToEliminate) {
        return TablePotentialElimination.multiplyAndMarginalize(probPotential, utilityPotential, variableToEliminate);
    }
    
    /**
     * @param potentials          potentials array to multiply
     * @param variablesOfInterest Set of variables that must be kept (although this
     *                            set may contain some variables that are not in any
     *                            potential) {@code potentials}
     *
     * @return The multiplied potentials
     */
    public static TablePotential multiplyAndMarginalize(List<TablePotential> potentials,
                                                        List<Variable> variablesOfInterest) {
        return TablePotentialElimination.multiplyAndMarginalize(potentials, variablesOfInterest);
    }
    
    /**
     * @param potentials          {@code ArrayList} of {@code Potential}s to
     *                            multiply.
     * @param variableToEliminate {@code Variable}.
     *
     * @return result {@code Potential} multiplied without
     * {@code variableToEliminate}
     */
    public static TablePotential multiplyAndMarginalize(List<TablePotential> potentials, Variable variableToEliminate) {
        return TablePotentialElimination.multiplyAndMarginalize(potentials, variableToEliminate);
    }
    
    /**
     * @param potential           {@code Potential} to marginalize
     * @param variableToEliminate {@code Variable}
     *
     * @return Marginalized potential
     */
    public static TablePotential marginalize(TablePotential potential, Variable variableToEliminate) {
        return TablePotentialElimination.marginalize(potential, variableToEliminate);
    }
    
    /**
     * @param potential           potential
     * @param variablesOfInterest list of variables of interest
     *
     * @return Marginalized potential
     */
    public static TablePotential marginalize(TablePotential potential, List<Variable> variablesOfInterest) {
        return TablePotentialElimination.marginalize(potential, variablesOfInterest);
    }
    
    /**
     * @param potential            that will be marginalized
     * @param variablesToKeep      variables to keep
     * @param variablesToEliminate variable to eliminate Condition: variablesToKeep
     *                             + variablesToEliminate = potential.getVariables()
     *                             Condition: variablesToKeep
     *
     * @return Marginalized potential
     */
    public static Potential marginalize(TablePotential potential, List<Variable> variablesToKeep,
                                        List<Variable> variablesToEliminate) {
        return TablePotentialElimination.marginalize(potential, variablesToKeep, variablesToEliminate);
    }
    
    /**
     * @param potentials An array of ordered {@code TablePotential}s
     *
     * @return constantFactor: The product of the constant potentials (the first
     * <i>k</i> because the array is ordered by size)
     *
     * @see AuxiliaryOperations#getNonConstantPotentials(Collection)
     */
    public static double getConstantFactor(List<TablePotential> potentials) {
        return AuxiliaryOperations.getConstantFactor(potentials);
    }
    
    /**
     * Compute the accumulated offsets of a {@code Potential}s array with the order
     * imposed by {@code potentialResult}
     *
     * @param potentials      {@code ArrayList} of {@code Potential}s.
     * @param potentialResult {@code TablePotential}.
     *
     * @return An array of arrays of integers ({@code int[][]}).
     */
    public static int[][] getAccumulatedOffsets(List<TablePotential> potentials, TablePotential potentialResult) {
        return AuxiliaryOperations.getAccumulatedOffsets(potentials, potentialResult);
    }
    
    /**
     * Compute the accumulated offsets of a {@code Potential}s array with the order
     * imposed by {@code variables}
     *
     * @param potentials {@code ArrayList} of {@code Potential}s.
     * @param variables  list of variables
     *
     * @return An array of arrays of integers ({@code int[][]}).
     */
    public static int[][] getAccumulatedOffsets(List<TablePotential> potentials, List<Variable> variables) {
        return AuxiliaryOperations.getAccumulatedOffsets(potentials, variables);
    }
    
    /**
     * @param potentials           list of TablePotentials
     * @param variablesToEliminate variables to eliminate
     *
     * @return resultant potential
     */
    public static Potential multiplyAndEliminate(List<TablePotential> potentials, List<Variable> variablesToEliminate) {
        return TablePotentialElimination.multiplyAndEliminate(potentials, variablesToEliminate);
    }
    
    // TODO Eliminar este método si no es usado por otros
    // Respuesta: Sí es usado por otros
    
    /**
     * @param potential a {@code TablePotential}
     *
     * @return The {@code potential} normalized
     */
    public static TablePotential normalize(TablePotential potential) throws CannotNormalizePotentialException {
        return TablePotentialTransform.normalize(potential);
    }
    
    /**
     * Divides two {@code TablePotential}s using the accumulated offsets algorithm.
     *
     * @param numerator   {@code Potential}.
     * @param denominator {@code Potential}.
     *
     * @return The quotient: A {@code TablePotential} with the union of the
     * variables of numerator and denominator. Condition: numerator and
     * denominator have the same domain (variables)
     */
    public static TablePotential divide(Potential numerator, Potential denominator) {
        return TablePotentialArithmetic.divide(numerator, denominator);
    }

    /**
     * @param numerator   {@code Potential}
     * @param denominator {@code Potential}
     *
     * @return The quotient
     */
    public static Potential dividePotentials(Potential numerator, Potential denominator) {
        return TablePotentialArithmetic.dividePotentials(numerator, denominator);
    }
    
    /**
     * @param tablePotentials      {@code ArrayList} of {@code TablePotential}s.
     * @param fSVariablesToKeep    {@code ArrayList} of {@code Variable}s.
     * @param fSVariableToMaximize {@code Variable}.
     *
     * @return Two potentials: 1) a {@code Potential} resulting of multiplication
     * and maximization of {@code variableToMaximize} and 2) a
     * {@code GTablePotential} of {@code Choice} (same variables as
     * preceding) with the value choosed for {@code variableToMaximize} in
     * each configuration.
     */
    public static Object[] multiplyAndMaximize(List<? extends Potential> tablePotentials,
                                               List<Variable> fSVariablesToKeep, Variable fSVariableToMaximize) {
        return TablePotentialMaximization.multiplyAndMaximize(tablePotentials, fSVariablesToKeep, fSVariableToMaximize);
    }
    
    /**
     * @param arrayListPotentials List of table potentials
     *
     * @return true if there is utility potential in a list of potentials
     */
    public static boolean isThereAUtilityPotential(List<TablePotential> arrayListPotentials) {
        return TablePotentialMaximization.isThereAUtilityPotential(arrayListPotentials);
    }
    
    /**
     * @param tablePotentials    {@code ArrayList} of {@code TablePotential}s.
     * @param variablesToKeep    {@code ArrayList} of {@code Variable}s.
     * @param variableToMaximize {@code Variable}.
     *
     * @return Two potentials: 1) a {@code Potential} resulting of multiplication
     * and maximization of {@code variableToMaximize} and 2) a
     * {@code TablePotential} with the mass probability 1.0 uniformly
     * distributed among the maximizing states of {@code variableToMaximize}
     * in each configuration; this is typically a policy of a decision.
     */
    public static TablePotential[] multiplyAndMaximizeUniformly(List<TablePotential> tablePotentials,
                                                                List<Variable> variablesToKeep, Variable variableToMaximize) {
        return TablePotentialMaximization.multiplyAndMaximizeUniformly(tablePotentials, variablesToKeep, variableToMaximize);
    }

    public static Object[] multiplyAndMaximize(List<? extends Potential> potentialsVariable,
                                               Variable variableToMaximize) {
        return TablePotentialMaximization.multiplyAndMaximize(potentialsVariable, variableToMaximize);
    }

    public static TablePotential[] multiplyAndMaximizeUniformly(List<TablePotential> potentialsVariable,
                                                                Variable variableToMaximize) {
        return TablePotentialMaximization.multiplyAndMaximizeUniformly(potentialsVariable, variableToMaximize);
    }

    public static Object[] maximize(Potential potential, Variable variableToMaximize) {
        return TablePotentialMaximization.maximize(potential, variableToMaximize);
    }

    public static TablePotential maximize(Collection<TablePotential> potentials) {
        return TablePotentialMaximization.maximize(potentials);
    }
    
    /**
     * @param dimension         dimension
     * @param coordinate        coordinate
     * @param increasedVariable increased variable
     *
     * @return index
     */
    static int findNextConfigurationAndIndexIncreasedVariable(int[] dimension, int[] coordinate,
                                                              int increasedVariable) {
        return AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(dimension, coordinate, increasedVariable);
    }
    
    /**
     * @param outputUtilityPotential Output utility potential
     *
     * @return True if there are relevant utilities
     */
    static boolean thereAreRelevantUtilities(TablePotential outputUtilityPotential) {
        boolean thereAreRelevantUtilities = false;
        for (int i = 0; i < outputUtilityPotential.getValues().length; i++) {
            if (!almostEqual(outputUtilityPotential.getValues()[i], 0.0)) {
                thereAreRelevantUtilities = true;
                break;
            }
        }
        return thereAreRelevantUtilities;
    }
    
    /**
     * This method is used to remove a decision variable from a probability
     * potential that in fact does not depend on the decision variable
     *
     * @param variable       {@code Variable}
     * @param inputPotential {@code TablePotential}
     *
     * @return A {@code TablePotential}
     */
    public static TablePotential projectOutVariable(Variable variable, TablePotential inputPotential) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        return TablePotentialTransform.projectOutVariable(variable, inputPotential);
    }
    
    /**
     * Compares two numbers
     *
     * @param a {@code double}
     * @param b {@code double}
     *
     * @return {@code true} when a and b are close.
     */
    public static boolean almostEqual(double a, double b) {
        return (Math.abs(b - a) <= maxRoundErrorAllowed * Math.abs(a));
    }
    
    public static TablePotential createZeroUtilityPotential(ProbNet dan) {
        return TablePotentialFactory.createZeroUtilityPotential(dan);
    }

    public static TablePotential createUnityProbabilityPotential() {
        return TablePotentialFactory.createUnityProbabilityPotential();
    }

    public static TablePotential createOneValuePotential(PotentialRole role, double value) {
        return TablePotentialFactory.createOneValuePotential(role, value);
    }
    
    public static double sum(double[] values) {
        return TablePotentialArithmetic.sum(values);
    }
    
    /**
     * @param potentials List of potentials
     *
     * @return A potential that results from multiplying the product of probability
     * potentials and the sum of utility potentials
     */
    public static TablePotential matrixPotential(List<Potential> potentials) {
        return TablePotentialTransform.matrixPotential(potentials);
    }
    
    /**
     * @param inputPotentialsList     List of table potentials
     * @param decisionsTotallyOrdered List of decision totally ordered
     *
     * @return an order list of the potentials in 'inputPotentialsList', where the
     * potentials are ordered according to the total order in
     * 'decisionsTotallyOrdered'
     */
    public static List<TablePotential> orderPotentialsByTotalOrder(List<TablePotential> inputPotentialsList,
                                                                   List<Variable> decisionsTotallyOrdered) {
        return TablePotentialMerge.orderPotentialsByTotalOrder(inputPotentialsList, decisionsTotallyOrdered);
    }
    
    /**
     * @param decision   Decision variable
     * @param potentials List of table potentials
     *
     * @return TablePotential with decision as the first variable and the union of
     * the variables of the potentials.
     *
     */
    public static AbstractIndexedPotential merge(Variable decision, List<? extends AbstractIndexedPotential> potentials)
            throws PotentialOperationException.DifferentSizesInPotentialsAndStates {
        return TablePotentialMerge.merge(decision, potentials);
    }
    
    public static TablePotential createZeroProbabilityPotential() {
        return TablePotentialFactory.createZeroProbabilityPotential();
    }
    
    public static TablePotential imposeOtherDistributionWhenDistributionIsZero(TablePotential xNewPotential) {
        return TablePotentialTransform.imposeOtherDistributionWhenDistributionIsZero(xNewPotential);
    }
    
    public static TablePotential evaluateFunctionPotential(FunctionPotential utilityPotential,
                                                           List<TablePotential> potentials, List<Variable> utilityVariables)
            throws NonProjectablePotentialException.CannotEvaluate, NonProjectablePotentialException.CannotResolveVariable {
        return TablePotentialArithmetic.evaluateFunctionPotential(utilityPotential, potentials, utilityVariables);
    }
}
