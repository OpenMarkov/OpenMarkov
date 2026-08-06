/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.openmarkov.core.model.network.Criterion;
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
        // Each eliminated variable roots its own contingent strategy tree, so when the inputs
        // carry trees, several variables are eliminated one at a time.
        if (variablesToEliminate.size() > 1 && anyCarriesStrategyTrees(tablePotentials)) {
            Collection<TablePotential> remaining = tablePotentials;
            TablePotential partial = null;
            for (int i = 0; i < variablesToEliminate.size(); i++) {
                List<Variable> stillToKeep = new ArrayList<>(variablesToKeep);
                stillToKeep.addAll(variablesToEliminate.subList(i + 1, variablesToEliminate.size()));
                partial = multiplyAndMarginalize(remaining, stillToKeep, List.of(variablesToEliminate.get(i)));
                remaining = List.of(partial);
            }
            return partial;
        }

        Criterion criterion = TablePotentialArithmetic.findFirstNonNullCriterion(new ArrayList<>(tablePotentials));
        double constantFactor = 1.0;
        StrategyTree treeOfTheConstants = null;
        List<TablePotential> nonConstantPotentials = new ArrayList<>();
        for (TablePotential potential : tablePotentials) {
            if (potential.getNumVariables() != 0) {
                nonConstantPotentials.add(potential);
            } else {
                constantFactor *= potential.getValues()[potential.getInitialPosition()];
                StrategyTree constantTree = firstTreeOf(potential);
                if (constantTree != null) {
                    // concatenate() writes into its receiver, so the receiver is cloned first.
                    treeOfTheConstants = (treeOfTheConstants == null) ? constantTree
                            : treeOfTheConstants.clone().concatenate(constantTree);
                }
            }
        }

        int numNonConstantPotentials = nonConstantPotentials.size();

        if (numNonConstantPotentials == 0) {
            TablePotential resultingPotential;
            if (treeOfTheConstants == null) {
                resultingPotential = new TablePotential(variablesToKeep,
                        TablePotentialArithmetic.getRole(tablePotentials));
            } else {
                StrategicTablePotential strategic = new StrategicTablePotential(variablesToKeep,
                        TablePotentialArithmetic.getRole(tablePotentials));
                strategic.strategyTrees = new StrategyTree[strategic.getValues().length];
                Arrays.fill(strategic.strategyTrees, treeOfTheConstants);
                resultingPotential = strategic;
            }
            resultingPotential.getValues()[0] = constantFactor;
            resultingPotential.setCriterion(criterion);
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

        // The trees of each carrier, or null in the position of a potential without them. The
        // gate of a configuration is the product of the potentials that carry no tree - the
        // probability part - and decides which states of the eliminated variable are possible.
        StrategyTree[][] carrierTrees = new StrategyTree[numNonConstantPotentials][];
        boolean thereAreTrees = treeOfTheConstants != null;
        for (int i = 0; i < numNonConstantPotentials; i++) {
            if (nonConstantPotentials.get(i) instanceof StrategicTablePotential strategic
                    && strategic.strategyTrees != null) {
                carrierTrees[i] = strategic.strategyTrees;
                thereAreTrees = true;
            }
        }
        Variable eliminatedVariable = variablesToEliminate.isEmpty() ? null : variablesToEliminate.get(0);
        double[] gates = thereAreTrees ? new double[eliminationSize] : null;
        StrategyTree[] configurationTrees = thereAreTrees ? new StrategyTree[eliminationSize] : null;
        StrategyTree[] resultTrees = thereAreTrees ? new StrategyTree[resultSize] : null;

        double multiplicationResult;
        double accumulator;
        int increasedVariable = 0;

        for (int outerIteration = 0; outerIteration < resultSize; outerIteration++) {
            multiplicationResult = constantFactor;
            for (int i = 0; i < numNonConstantPotentials; i++) {
                multiplicationResult *= tables[i][currentPositions[i]];
            }
            accumulator = multiplicationResult;
            if (thereAreTrees) {
                gates[0] = gateAt(constantFactor, tables, currentPositions, carrierTrees);
                configurationTrees[0] = treeAt(currentPositions, carrierTrees);
            }

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
                if (thereAreTrees) {
                    gates[innerIteration] = gateAt(constantFactor, tables, currentPositions, carrierTrees);
                    configurationTrees[innerIteration] = treeAt(currentPositions, carrierTrees);
                }
            }

            if (outerIteration < resultSize - 1) {
                increasedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(
                        unionDimensions, unionCoordinate, increasedVariable);

                for (int i = 0; i < numNonConstantPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }
            }

            resultValues[outerIteration] = accumulator;
            if (thereAreTrees) {
                StrategyTree cellTree = (eliminatedVariable == null) ? configurationTrees[0]
                        : StrategyTree.averageOfInterventions(eliminatedVariable, gates, configurationTrees);
                if (treeOfTheConstants != null) {
                    cellTree = (cellTree == null) ? treeOfTheConstants
                            : cellTree.clone().concatenate(treeOfTheConstants);
                }
                resultTrees[outerIteration] = cellTree;
            }
        }

        TablePotential result;
        if (thereAreTrees) {
            StrategicTablePotential strategic = new StrategicTablePotential(variablesToKeep,
                    TablePotentialArithmetic.getRole(tablePotentials), resultValues);
            strategic.strategyTrees = resultTrees;
            result = strategic;
        } else {
            result = new TablePotential(variablesToKeep, TablePotentialArithmetic.getRole(tablePotentials),
                    resultValues);
        }
        result.setCriterion(criterion);
        return result;
    }

    /**
     * Product of the potentials that carry no strategy tree at this configuration.
     */
    private static double gateAt(double constantFactor, double[][] tables, int[] positions,
                                 StrategyTree[][] carrierTrees) {
        double gate = constantFactor;
        for (int i = 0; i < tables.length; i++) {
            if (carrierTrees[i] == null) {
                gate *= tables[i][positions[i]];
            }
        }
        return gate;
    }

    /**
     * The trees of the carriers at this configuration, combined on a copy.
     */
    private static StrategyTree treeAt(int[] positions, StrategyTree[][] carrierTrees) {
        StrategyTree combined = null;
        for (int i = 0; i < carrierTrees.length; i++) {
            if (carrierTrees[i] != null) {
                StrategyTree tree = carrierTrees[i][positions[i]];
                if (tree != null) {
                    combined = (combined == null) ? tree : combined.clone().concatenate(tree);
                }
            }
        }
        return combined;
    }

    private static boolean anyCarriesStrategyTrees(Collection<TablePotential> potentials) {
        for (TablePotential potential : potentials) {
            if (potential instanceof StrategicTablePotential strategic && strategic.strategyTrees != null) {
                return true;
            }
        }
        return false;
    }

    private static StrategyTree firstTreeOf(TablePotential potential) {
        return (potential instanceof StrategicTablePotential strategic && strategic.strategyTrees != null
                && strategic.strategyTrees.length > 0) ? strategic.strategyTrees[0] : null;
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
