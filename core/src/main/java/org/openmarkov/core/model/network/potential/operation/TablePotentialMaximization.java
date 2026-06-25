/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.openmarkov.core.inference.Choice;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Maximization operations on {@link TablePotential}s:
 * multiply-and-maximize and related helpers.
 *
 * @author Manuel Arias
 */
final class TablePotentialMaximization {

    private TablePotentialMaximization() {
    }

    /**
     * Multiplies {@code tablePotentials} and maximizes out
     * {@code fSVariableToMaximize}, keeping {@code fSVariablesToKeep}.
     *
     * @param tablePotentials      potentials to multiply
     * @param fSVariablesToKeep    variables to keep in the result
     * @param fSVariableToMaximize variable to maximize out
     * @return array of two elements: [resulting TablePotential, GTablePotential of Choice]
     */
    @SuppressWarnings("unchecked")
    static Object[] multiplyAndMaximize(List<? extends Potential> tablePotentials,
                                        List<Variable> fSVariablesToKeep, Variable fSVariableToMaximize) {
        List<TablePotential> potentials = (ArrayList<TablePotential>) tablePotentials;
        List<Variable> variablesToKeep = fSVariablesToKeep;

        PotentialRole role = TablePotentialArithmetic.getRole(tablePotentials);

        TablePotential resultingPotential = new TablePotential(variablesToKeep, role);
        GTablePotential<Choice> gResult = new GTablePotential<>(variablesToKeep, role);
        int numStates = fSVariableToMaximize.getNumStates();
        int[] statesChoosed;
        Choice choice;

        double constantFactor = 1.0;
        List<TablePotential> properPotentials = new ArrayList<>();
        for (TablePotential potential : potentials) {
            if (potential.getNumVariables() != 0) {
                properPotentials.add(potential);
            } else {
                constantFactor *= potential.getValues()[potential.getInitialPosition()];
            }
        }

        int numProperPotentials = properPotentials.size();

        if (numProperPotentials == 0) {
            resultingPotential.getValues()[0] = constantFactor;
            return new Object[]{resultingPotential, gResult};
        }

        List<Variable> unionVariables = new ArrayList<>();
        unionVariables.add(fSVariableToMaximize);
        unionVariables.addAll(variablesToKeep);
        int numUnionVariables = unionVariables.size();

        int[] unionCoordinate = new int[numUnionVariables];
        int[] unionDimensions = TablePotential.calculateDimensions(unionVariables);

        double[][] tables = new double[numProperPotentials][];
        int[] initialPositions = new int[numProperPotentials];
        int[] currentPositions = new int[numProperPotentials];
        int[][] accumulatedOffsets = new int[numProperPotentials][];
        TablePotential unionPotential = new TablePotential(unionVariables, null);
        for (int i = 0; i < numProperPotentials; i++) {
            TablePotential potential = properPotentials.get(i);
            tables[i] = potential.getValues();
            initialPositions[i] = potential.getInitialPosition();
            currentPositions[i] = initialPositions[i];
            accumulatedOffsets[i] = unionPotential.getAccumulatedOffsets(potential.getVariables());
        }

        int resultSize = resultingPotential.getValues().length;
        int eliminationSize = fSVariableToMaximize.getNumStates();

        double multiplicationResult;
        double maxValue;
        int increasedVariable = 0;

        for (int outerIteration = 0; outerIteration < resultSize; outerIteration++) {
            multiplicationResult = constantFactor;
            for (int i = 0; i < numProperPotentials; i++) {
                multiplicationResult *= tables[i][currentPositions[i]];
            }
            statesChoosed = new int[numStates];
            statesChoosed[0] = 0;
            choice = new Choice(fSVariableToMaximize, statesChoosed);
            maxValue = multiplicationResult;
            choice.setValue(0);

            for (int innerIteration = 1; innerIteration < eliminationSize; innerIteration++) {
                increasedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(
                        unionDimensions, unionCoordinate, increasedVariable);

                for (int i = 0; i < numProperPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }

                multiplicationResult = constantFactor;
                for (int i = 0; i < numProperPotentials; i++) {
                    multiplicationResult = multiplicationResult * tables[i][currentPositions[i]];
                }

                if (multiplicationResult > (maxValue + DiscretePotentialOperations.maxRoundErrorAllowed)) {
                    choice.setValue(innerIteration);
                    maxValue = multiplicationResult;
                } else {
                    if ((multiplicationResult < (maxValue + DiscretePotentialOperations.maxRoundErrorAllowed))
                            && (multiplicationResult >= (maxValue - DiscretePotentialOperations.maxRoundErrorAllowed))) {
                        choice.addValue(innerIteration);
                    }
                }
            }

            if (outerIteration < resultSize - 1) {
                increasedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(
                        unionDimensions, unionCoordinate, increasedVariable);

                for (int i = 0; i < numProperPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }
            }

            resultingPotential.getValues()[outerIteration] = maxValue;
            gResult.elementTable.add(choice);
        }

        return new Object[]{resultingPotential, gResult};
    }

    /**
     * Multiplies {@code tablePotentials} and maximizes out
     * {@code variableToMaximize}, distributing probability uniformly among ties.
     *
     * @param tablePotentials    potentials to multiply
     * @param variablesToKeep    variables to keep
     * @param variableToMaximize variable to maximize out
     * @return array of two potentials: [value potential, policy potential]
     */
    static TablePotential[] multiplyAndMaximizeUniformly(List<TablePotential> tablePotentials,
                                                          List<Variable> variablesToKeep,
                                                          Variable variableToMaximize) {
        List<TablePotential> potentials = tablePotentials;

        PotentialRole roleResult = (isThereAUtilityPotential(tablePotentials)) ? PotentialRole.UNSPECIFIED
                : PotentialRole.CONDITIONAL_PROBABILITY;

        TablePotential resultingPotential = new TablePotential(variablesToKeep, roleResult);

        List<Variable> variablesPolicy = new ArrayList<>();
        variablesPolicy.add(variableToMaximize);
        variablesPolicy.addAll(variablesToKeep);

        TablePotential policy = new TablePotential(variablesPolicy, PotentialRole.CONDITIONAL_PROBABILITY);

        double constantFactor = 1.0;
        List<TablePotential> properPotentials = new ArrayList<>();
        for (TablePotential potential : potentials) {
            if (potential.getNumVariables() != 0) {
                properPotentials.add(potential);
            } else {
                constantFactor *= potential.getValues()[potential.getInitialPosition()];
            }
        }

        int numProperPotentials = properPotentials.size();

        if (numProperPotentials == 0) {
            resultingPotential.getValues()[0] = constantFactor;
            return new TablePotential[]{resultingPotential, policy};
        }

        List<Variable> unionVariables = new ArrayList<>();
        unionVariables.add(variableToMaximize);
        unionVariables.addAll(variablesToKeep);
        int numUnionVariables = unionVariables.size();

        int[] unionCoordinate = new int[numUnionVariables];
        int[] unionDimensions = TablePotential.calculateDimensions(unionVariables);

        double[][] tables = new double[numProperPotentials][];
        int[] initialPositions = new int[numProperPotentials];
        int[] currentPositions = new int[numProperPotentials];
        int[][] accumulatedOffsets = new int[numProperPotentials][];
        TablePotential unionPotential = new TablePotential(unionVariables, null);
        for (int i = 0; i < numProperPotentials; i++) {
            TablePotential potential = properPotentials.get(i);
            tables[i] = potential.getValues();
            initialPositions[i] = potential.getInitialPosition();
            currentPositions[i] = initialPositions[i];
            accumulatedOffsets[i] = unionPotential.getAccumulatedOffsets(potential.getVariables());
        }

        int resultSize = resultingPotential.getValues().length;
        int eliminationSize = variableToMaximize.getNumStates();

        double multiplicationResult;
        double accumulator;
        int increasedVariable = 0;
        List<Integer> statesTies;

        for (int outerIteration = 0; outerIteration < resultSize; outerIteration++) {
            multiplicationResult = constantFactor;
            for (int i = 0; i < numProperPotentials; i++) {
                multiplicationResult *= tables[i][currentPositions[i]];
            }
            statesTies = new ArrayList<>();
            statesTies.add(0);
            accumulator = multiplicationResult;
            //TODO: The array positionTies is never read.
            int[] positionTies = new int[eliminationSize];
            int numTies = 0;
            positionTies[numTies] = currentPositions[0];
            numTies++;

            for (int innerIteration = 1; innerIteration < eliminationSize; innerIteration++) {
                increasedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(
                        unionDimensions, unionCoordinate, increasedVariable);

                for (int i = 0; i < numProperPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }

                multiplicationResult = constantFactor;
                for (int i = 0; i < numProperPotentials; i++) {
                    multiplicationResult = multiplicationResult * tables[i][currentPositions[i]];
                }

                double diffWithAccumulator = multiplicationResult - accumulator;
                if (diffWithAccumulator > DiscretePotentialOperations.maxRoundErrorAllowed) {
                    statesTies = new ArrayList<>();
                    statesTies.add(innerIteration);
                    accumulator = multiplicationResult;
                    numTies = 0;
                    positionTies[numTies] = currentPositions[0];
                    numTies++;
                } else {
                    if (Math.abs(diffWithAccumulator) < DiscretePotentialOperations.maxRoundErrorAllowed) {
                        statesTies.add(innerIteration);
                        positionTies[numTies] = currentPositions[0];
                        numTies++;
                    }
                }
            }

            if (outerIteration < resultSize - 1) {
                increasedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(
                        unionDimensions, unionCoordinate, increasedVariable);

                for (int i = 0; i < numProperPotentials; i++) {
                    currentPositions[i] += accumulatedOffsets[i][increasedVariable];
                }
            }

            resultingPotential.getValues()[outerIteration] = accumulator;
            assignProbUniformlyInTies(policy, variableToMaximize.getNumStates(), statesTies,
                    resultingPotential.getConfiguration(outerIteration));
        }

        return new TablePotential[]{resultingPotential, policy};
    }

    /**
     * @param arrayListPotentials list of potentials
     * @return true if there is a utility potential in the list
     */
    static boolean isThereAUtilityPotential(List<TablePotential> arrayListPotentials) {
        boolean isThere = false;
        for (int i = 0; (i < arrayListPotentials.size()) && !isThere; i++) {
            // isThere = arrayListPotentials.get(i).getPotentialRole() == PotentialRole.UTILITY;
        }
        return isThere;
    }

    /**
     * Multiplies {@code potentialsVariable} and maximizes out
     * {@code variableToMaximize}.
     */
    static Object[] multiplyAndMaximize(List<? extends Potential> potentialsVariable,
                                        Variable variableToMaximize) {
        HashSet<Variable> addedVariables = new HashSet<>();
        for (Potential potential : potentialsVariable) {
            addedVariables.addAll(potential.getVariables());
        }
        List<Variable> variablesToKeep = new ArrayList<>(addedVariables);
        variablesToKeep.remove(variableToMaximize);
        return multiplyAndMaximize(potentialsVariable, variablesToKeep, variableToMaximize);
    }

    /**
     * Multiplies {@code potentialsVariable} and maximizes out
     * {@code variableToMaximize}, distributing probability uniformly among ties.
     */
    static TablePotential[] multiplyAndMaximizeUniformly(List<TablePotential> potentialsVariable,
                                                          Variable variableToMaximize) {
        HashSet<Variable> addedVariables = new HashSet<>();
        for (TablePotential potential : potentialsVariable) {
            addedVariables.addAll(potential.getVariables());
        }
        List<Variable> variablesToKeep = new ArrayList<>(addedVariables);
        variablesToKeep.remove(variableToMaximize);
        return multiplyAndMaximizeUniformly(potentialsVariable, variablesToKeep, variableToMaximize);
    }

    /**
     * Maximizes out {@code variableToMaximize} from a single potential.
     */
    static Object[] maximize(Potential potential, Variable variableToMaximize) {
        List<Potential> potentialsVariable = new ArrayList<>();
        potentialsVariable.add(potential);
        List<Variable> variablesToKeep = new ArrayList<>(potential.getVariables());
        variablesToKeep.remove(variableToMaximize);
        return multiplyAndMaximize(potentialsVariable, variablesToKeep, variableToMaximize);
    }

    /**
     * Returns the element-wise maximum over a collection of potentials defined
     * on the same variables.
     */
    static TablePotential maximize(Collection<TablePotential> potentials) {
        TablePotential result;
        Collection<TablePotential> setPot;

        if (potentials == null) {
            result = null;
        } else {
            int numPotentials = potentials.size();
            if (numPotentials == 0) {
                result = null;
            } else {
                Iterator<TablePotential> iterPotentials = potentials.iterator();
                TablePotential potFirst = potentials.iterator().next();
                List<Variable> variablesFirst = potFirst.getVariables();
                setPot = new HashSet<>();
                setPot.add(potFirst);
                while (iterPotentials.hasNext()) {
                    setPot.add((TablePotential) iterPotentials.next().reorder(variablesFirst));
                }
                int lengthValues = potFirst.getValues().length;
                double[] newValues = new double[lengthValues];
                for (int i = 0; i < lengthValues; i++) {
                    double max = Double.NEGATIVE_INFINITY;
                    for (TablePotential pot : setPot) {
                        max = Math.max(pot.getValues()[i], max);
                    }
                    newValues[i] = max;
                }

                result = new TablePotential(variablesFirst, potFirst.getPotentialRole(), newValues);
                if (result.isAdditive()) {
                    result.setCriterion(potFirst.getCriterion());
                }
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void assignProbUniformlyInTies(TablePotential tp, int numStatesVariable,
                                                   List<Integer> statesTies, int[] policyDomainConfiguration) {
        int numStatesTies = statesTies.size();
        double probTies = 1.0 / numStatesTies;

        int lenghtPolicyDomainConfiguration = policyDomainConfiguration.length;
        int[] tPConfiguration = new int[lenghtPolicyDomainConfiguration + 1];
        for (int j = 0; j < lenghtPolicyDomainConfiguration; j++) {
            tPConfiguration[j + 1] = policyDomainConfiguration[j];
        }
        for (int i = 0; i < numStatesVariable; i++) {
            tPConfiguration[0] = i;
            int posTPConfiguration = tp.getPosition(tPConfiguration);
            double iProb = (statesTies.contains(i)) ? probTies : 0.0;
            tp.getValues()[posTPConfiguration] = iProb;
        }
    }
}
