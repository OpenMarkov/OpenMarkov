/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.StrategicTablePotential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Elimination operations on {@link TablePotential}s: marginalization,
 * multiply-and-marginalize, and multiply-and-eliminate.
 *
 * @author Manuel Arias
 */
final class TablePotentialElimination {

    private TablePotentialElimination() {
    }

    /**
     * Multiplies {@code tablePotentials} and marginalizes out
     * {@code variablesToEliminate}, keeping {@code variablesToKeep}.
     *
     * @param tablePotentials      potentials to multiply
     * @param variablesToKeep      variables that appear in the result
     * @param variablesToEliminate variables to sum out
     * @return resulting potential
     */
    static TablePotential multiplyAndMarginalize(Collection<TablePotential> tablePotentials,
                                                 List<Variable> variablesToKeep,
                                                 List<Variable> variablesToEliminate) {
        double constantFactor = 1.0;
        List<TablePotential> nonConstantPotentials = new ArrayList<>();
        for (TablePotential potential : tablePotentials) {
            if (potential.getNumVariables() != 0) {
                nonConstantPotentials.add(potential);
            } else {
                constantFactor *= potential.getValues()[potential.getInitialPosition()];
            }
        }

        int numNonConstantPotentials = nonConstantPotentials.size();

        if (numNonConstantPotentials == 0) {
            TablePotential resultingPotential = new TablePotential(variablesToKeep,
                    TablePotentialArithmetic.getRole(tablePotentials));
            resultingPotential.getValues()[0] = constantFactor;
            return resultingPotential;
        }

        List<Variable> unionVariables = new ArrayList<>(variablesToEliminate);
        unionVariables.addAll(variablesToKeep);
        int numUnionVariables = unionVariables.size();

        int[] unionCoordinate = new int[numUnionVariables];
        int[] unionDimensions = TablePotential.calculateDimensions(unionVariables);

        double[][] tables = new double[numNonConstantPotentials][];
        int[] initialPositions = new int[numNonConstantPotentials];
        int[] currentPositions = new int[numNonConstantPotentials];
        int[][] accumulatedOffsets = new int[numNonConstantPotentials][];
        for (int i = 0; i < numNonConstantPotentials; i++) {
            TablePotential potential = nonConstantPotentials.get(i);
            tables[i] = potential.getValues();
            initialPositions[i] = potential.getInitialPosition();
            currentPositions[i] = initialPositions[i];
            accumulatedOffsets[i] = TablePotential.getAccumulatedOffsets(unionVariables, potential.getVariables());
        }

        int resultSize = TablePotential.computeTableSize(variablesToKeep);
        double[] resultValues = new double[resultSize];
        int eliminationSize = 1;
        for (Variable variable : variablesToEliminate) {
            eliminationSize *= variable.getNumStates();
        }

        double multiplicationResult;
        double accumulator;
        int increasedVariable = 0;

        for (int outerIteration = 0; outerIteration < resultSize; outerIteration++) {
            multiplicationResult = constantFactor;
            for (int i = 0; i < numNonConstantPotentials; i++) {
                multiplicationResult *= tables[i][currentPositions[i]];
            }
            accumulator = multiplicationResult;

            for (int innerIteration = 1; innerIteration < eliminationSize; innerIteration++) {
                increasedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(
                        unionDimensions, unionCoordinate, increasedVariable);

                for (int i = 0; i < numNonConstantPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }

                multiplicationResult = constantFactor;
                for (int i = 0; i < numNonConstantPotentials; i++) {
                    multiplicationResult *= tables[i][currentPositions[i]];
                }

                accumulator += multiplicationResult;
            }

            if (outerIteration < resultSize - 1) {
                increasedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(
                        unionDimensions, unionCoordinate, increasedVariable);

                for (int i = 0; i < numNonConstantPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }
            }

            resultValues[outerIteration] = accumulator;
        }

        return new TablePotential(variablesToKeep, TablePotentialArithmetic.getRole(tablePotentials), resultValues);
    }

    /**
     * Multiplies a probability potential and a utility potential, then
     * marginalizes out {@code variableToEliminate}.
     *
     * @param probPotential       probability potential
     * @param utilityPotential    utility potential
     * @param variableToEliminate variable to eliminate
     * @return resulting utility potential
     */
    static TablePotential multiplyAndMarginalize(TablePotential probPotential, TablePotential utilityPotential,
                                                 Variable variableToEliminate) {
        if (probPotential.getVariables().isEmpty()) {
            double prob = probPotential.getValues()[0];
            if (prob == 1) {
                return utilityPotential;
            }
            TablePotential result = (TablePotential) utilityPotential.copy();
            for (int i = 0; i < result.getValues().length; i++) {
                result.getValues()[i] *= prob;
            }
            return result;
        }

        List<Variable> allVariables = probPotential.getVariables();
        for (Variable variable : utilityPotential.getVariables()) {
            if (!allVariables.contains(variable)) {
                allVariables.add(variable);
            }
        }
        List<Variable> variablesToKeep = new ArrayList<>(allVariables);
        variablesToKeep.remove(variableToEliminate);

        boolean thereAreInterventions = utilityPotential instanceof StrategicTablePotential;
        StrategicTablePotential strategicUtil    = thereAreInterventions ? (StrategicTablePotential) utilityPotential : null;
        StrategicTablePotential strategicResult  = null;
        TablePotential resultPotential;
        if (thereAreInterventions) {
            strategicResult = new StrategicTablePotential(variablesToKeep, PotentialRole.UNSPECIFIED);
            strategicResult.strategyTrees = new StrategyTree[strategicResult.getValues().length];
            resultPotential = strategicResult;
        } else {
            resultPotential = new TablePotential(variablesToKeep, PotentialRole.UNSPECIFIED);
        }

        int[] coordinates = new int[allVariables.size()];
        int[] dimensions = TablePotential.calculateDimensions(allVariables);

        int currentPositionProb = 0;
        int[] accumulatedOffsetsProb = TablePotential.getAccumulatedOffsets(allVariables,
                probPotential.getVariables());
        int currentPositionUtil = 0;
        int[] accumulatedOffsetsUtil = TablePotential.getAccumulatedOffsets(allVariables,
                utilityPotential.getVariables());

        double accumulator;
        int increasedVariable = 0;
        double[] probValues = probPotential.getValues();
        double[] utilValues = utilityPotential.getValues();
        double[] probs = new double[variableToEliminate.getNumStates()];
        StrategyTree[] strategyTrees = new StrategyTree[variableToEliminate.getNumStates()];

        for (int outerIteration = 0; outerIteration < resultPotential.getValues().length; outerIteration++) {
            accumulator = 0;

            for (int stateIndex = 0; stateIndex < variableToEliminate.getNumStates(); stateIndex++) {
                if (stateIndex != 0) {
                    increasedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(
                            dimensions, coordinates, increasedVariable);
                    currentPositionProb += accumulatedOffsetsProb[increasedVariable];
                    currentPositionUtil += accumulatedOffsetsUtil[increasedVariable];
                }

                accumulator += probValues[currentPositionProb] * utilValues[currentPositionUtil];
                probs[stateIndex] = probValues[currentPositionProb];

                if (thereAreInterventions) {
                    strategyTrees[stateIndex] = strategicUtil.strategyTrees[currentPositionUtil];
                }
            }

            resultPotential.getValues()[outerIteration] = accumulator;

            if (thereAreInterventions) {
                strategicResult.strategyTrees[outerIteration] = StrategyTree.averageOfInterventions(
                        variableToEliminate, probs, strategyTrees);
            }

            if (outerIteration < resultPotential.getValues().length - 1) {
                increasedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(
                        dimensions, coordinates, increasedVariable);
                currentPositionProb += accumulatedOffsetsProb[increasedVariable];
                currentPositionUtil += accumulatedOffsetsUtil[increasedVariable];
            }
        }
        resultPotential.setCriterion(utilityPotential.getCriterion());
        return resultPotential;
    }

    /**
     * Multiplies {@code potentials} and marginalizes out all variables not in
     * {@code variablesOfInterest}.
     *
     * @param potentials          potentials to multiply
     * @param variablesOfInterest variables to keep
     * @return resulting potential
     */
    static TablePotential multiplyAndMarginalize(List<TablePotential> potentials,
                                                 List<Variable> variablesOfInterest) {
        List<Variable> unionVariables = AuxiliaryOperations.getUnionVariables(potentials);
        List<Variable> variablesToKeep = new ArrayList<>();
        List<Variable> variablesToEliminate = new ArrayList<>();
        for (Variable variable : unionVariables) {
            if (variablesOfInterest.contains(variable)) {
                variablesToKeep.add(variable);
            } else {
                variablesToEliminate.add(variable);
            }
        }
        return multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }

    /**
     * Multiplies {@code potentials} and marginalizes out {@code variableToEliminate}.
     *
     * @param potentials          potentials to multiply
     * @param variableToEliminate variable to eliminate
     * @return resulting potential
     */
    static TablePotential multiplyAndMarginalize(List<TablePotential> potentials, Variable variableToEliminate) {
        List<Variable> variablesToKeep = AuxiliaryOperations.getUnionVariables(potentials);
        variablesToKeep.remove(variableToEliminate);
        return multiplyAndMarginalize(potentials, variablesToKeep, Arrays.asList(variableToEliminate));
    }

    /**
     * Marginalizes out {@code variableToEliminate} from {@code potential}.
     *
     * @param potential           potential to marginalize
     * @param variableToEliminate variable to eliminate
     * @return marginalized potential
     */
    static TablePotential marginalize(TablePotential potential, Variable variableToEliminate) {
        List<Variable> variablesToKeep = new ArrayList<>(potential.getVariables());
        variablesToKeep.remove(variableToEliminate);
        List<Variable> variablesToEliminate = new ArrayList<>();
        variablesToEliminate.add(variableToEliminate);
        List<TablePotential> potentials = new ArrayList<>();
        potentials.add(potential);
        return multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }

    /**
     * Marginalizes {@code potential} keeping only {@code variablesOfInterest}.
     *
     * @param potential           potential to marginalize
     * @param variablesOfInterest variables to keep
     * @return marginalized potential
     */
    static TablePotential marginalize(TablePotential potential, List<Variable> variablesOfInterest) {
        List<Variable> variables = potential.getVariables();
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
        potentials.add(potential);
        return multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }

    /**
     * Marginalizes {@code potential} given explicit keep and eliminate sets.
     *
     * @param potential            potential to marginalize
     * @param variablesToKeep      variables to keep
     * @param variablesToEliminate variables to eliminate
     * @return marginalized potential
     */
    static Potential marginalize(TablePotential potential, List<Variable> variablesToKeep,
                                 List<Variable> variablesToEliminate) {
        List<TablePotential> potentials = new ArrayList<>();
        potentials.add(potential);
        return multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }

    /**
     * Multiplies {@code potentials} and eliminates {@code variablesToEliminate}.
     *
     * @param potentials           potentials to multiply
     * @param variablesToEliminate variables to eliminate
     * @return resulting potential
     */
    static Potential multiplyAndEliminate(List<TablePotential> potentials, List<Variable> variablesToEliminate) {
        List<Variable> variablesToKeep = AuxiliaryOperations.getUnionVariables(potentials);
        variablesToKeep.removeAll(variablesToEliminate);
        return multiplyAndMarginalize(potentials, variablesToKeep, variablesToEliminate);
    }
}
