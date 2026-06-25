/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.variableElimination.operation;

import org.openmarkov.core.exception.CostEffectivenessException;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Pure algebraic operations on cost-effectiveness potentials ({@link GTablePotential}
 * of {@link CEP}s). Contains no network-level logic; see {@link CENetworkBuilder}
 * for operations that transform a {@link org.openmarkov.core.model.network.ProbNet}.
 *
 * @author Manuel Arias Calleja
 */
public class CEAlgebra {

    private static final int COST = 0;
    private static final int EFFECTIVENESS = 1;
    private static final int FIRST_VARIABLE_POSITION = 0;

    // -------------------------------------------------------------------------
    // Public operations
    // -------------------------------------------------------------------------

    /**
     * Multiplies a {@code GTablePotential} of {@code CEP}s and a {@code TablePotential}.
     *
     * @param potential  {@code TablePotential}.
     * @param gPotential {@code GTablePotential}.
     * @return A {@code GTablePotential} whose variables are the union of both inputs.
     */
    @SuppressWarnings("unchecked")
    public static GTablePotential multiply(TablePotential potential, GTablePotential gPotential) {
        ArrayList<Variable> gVariables = new ArrayList<>(gPotential.getVariables());
        ArrayList<Variable> pVariables = new ArrayList<>(potential.getVariables());
        HashSet<Variable> variablesSet = new LinkedHashSet<>(gVariables);
        variablesSet.addAll(pVariables);
        ArrayList<Variable> variablesResult = new ArrayList<>(variablesSet);

        int numVariablesResult = variablesResult.size();
        GTablePotential result = new GTablePotential(variablesResult, PotentialRole.UNSPECIFIED);

        int[] accOffGPot = result.getAccumulatedOffsets(gVariables);
        int[] accOffPPot = result.getAccumulatedOffsets(pVariables);
        int[] resultCoordinate = new int[numVariablesResult];
        int[] resultDimensions = result.getDimensions();
        int gPosition = 0;
        int pPosition = 0;
        int incrementedVariable = 0;

        while (incrementedVariable != numVariablesResult) {
            CEP partition = (CEP) gPotential.elementTable.get(gPosition);
            double probability = potential.getValues()[pPosition];
            partition.multiply(probability);
            result.elementTable.add(partition);
            for (incrementedVariable = 0; incrementedVariable < numVariablesResult; incrementedVariable++) {
                resultCoordinate[incrementedVariable]++;
                if (resultCoordinate[incrementedVariable] == resultDimensions[incrementedVariable]) {
                    resultCoordinate[incrementedVariable] = 0;
                } else {
                    break;
                }
            }
            if (incrementedVariable != numVariablesResult) {
                gPosition += accOffGPot[incrementedVariable];
                pPosition += accOffPPot[incrementedVariable];
                incrementedVariable = 0;
            }
        }
        return result;
    }

    /**
     * Divides a {@code GTablePotential} of {@code CEP}s by a {@code TablePotential}.
     *
     * @param utilityPotential     {@code GTablePotential}
     * @param probabilityPotential {@code TablePotential}
     * @return {@code GTablePotential} with variables equal to the union of both inputs.
     */
    @SuppressWarnings("unchecked")
    public static GTablePotential divide(GTablePotential utilityPotential, TablePotential probabilityPotential) {
        List<Variable> utilityVariables = utilityPotential.getVariables();
        List<Variable> probabilityVariables = probabilityPotential.getVariables();
        HashSet<Variable> setResultVariables = new LinkedHashSet<>(utilityVariables);
        setResultVariables.addAll(probabilityVariables);
        List<Variable> resultVariables = new ArrayList<>(setResultVariables);
        GTablePotential result = new GTablePotential(resultVariables, PotentialRole.UNSPECIFIED);

        int[] accOffsetsUtility = result.getAccumulatedOffsets(resultVariables);
        int[] accOffsetsProbability = result.getAccumulatedOffsets(probabilityVariables);
        int[] resultCoordinate = new int[resultVariables.size()];
        int increasedVariable = 0;
        int utilityPosition = 0;
        int probabilityPosition = 0;
        int numResultVariables = resultVariables.size();
        int[] resultDimensions = result.getDimensions();
        do {
            CEP partition = (CEP) utilityPotential.elementTable.get(utilityPosition);
            double probability = probabilityPotential.getValues()[probabilityPosition];
            partition.divide(probability);
            result.elementTable.add(partition);
            boolean more;
            do {
                more = false;
                resultCoordinate[increasedVariable]++;
                if (resultCoordinate[increasedVariable] == resultDimensions[increasedVariable]) {
                    resultCoordinate[increasedVariable++] = 0;
                    more = true;
                }
            } while (increasedVariable < numResultVariables && more);
            if (increasedVariable < numResultVariables) {
                utilityPosition += accOffsetsUtility[increasedVariable];
                probabilityPosition += accOffsetsProbability[increasedVariable];
                increasedVariable = 0;
            }
        } while (increasedVariable < numResultVariables);
        return result;
    }

    /**
     * Multiplies a {@code TablePotential} and a {@code GTablePotential} of {@code CEP}s,
     * marginalizing out {@code variableToRemove} by weighted average.
     *
     * @param probabilityPotential {@code TablePotential}
     * @param utilityPotential     {@code GTablePotential}
     * @param variableToRemove     variable to eliminate
     * @return {@code GTablePotential} without {@code variableToRemove}
     */
    @SuppressWarnings("unchecked")
    public static GTablePotential multiplyAndMarginalize(TablePotential probabilityPotential,
                                                         GTablePotential utilityPotential,
                                                         Variable variableToRemove) {
        List<Variable> utilityVariables = new ArrayList<>(utilityPotential.getVariables());
        List<Variable> probabilityVariables = new ArrayList<>(probabilityPotential.getVariables());
        HashSet<Variable> variablesResultSet = new LinkedHashSet<>(utilityVariables);
        variablesResultSet.addAll(probabilityVariables);
        variablesResultSet.remove(variableToRemove);

        List<Variable> referenceVariables = new ArrayList<>(variablesResultSet.size() + 1);
        referenceVariables.add(variableToRemove);
        referenceVariables.addAll(variablesResultSet);

        List<Variable> variablesResult = new ArrayList<>(variablesResultSet);
        GTablePotential result = new GTablePotential(variablesResult, PotentialRole.UNSPECIFIED);

        int[] accOffProbabilityPot = TablePotential.getAccumulatedOffsets(referenceVariables, probabilityVariables);
        int[] accOffUtilityPot = TablePotential.getAccumulatedOffsets(referenceVariables, utilityVariables);

        int numReferenceVariables = referenceVariables.size();
        int[] referenceVariablesCoordinate = new int[numReferenceVariables];
        int[] referenceVariablesDimensions = new int[numReferenceVariables];
        for (int i = 0; i < numReferenceVariables; i++) {
            referenceVariablesDimensions[i] = referenceVariables.get(i).getNumStates();
        }
        int utilityPosition = 0;
        int probabilityPosition = 0;
        int incrementedVariable = 0;

        int numStatesVariableToRemove = variableToRemove.getNumStates();
        List<CEP> partitions = new ArrayList<>(numStatesVariableToRemove);
        double[] probabilities = new double[numStatesVariableToRemove];

        int i = 0;
        while (incrementedVariable != numReferenceVariables) {
            CEP partition = (CEP) utilityPotential.elementTable.get(utilityPosition);
            partitions.add(partition);
            probabilities[i++] = probabilityPotential.getValues()[probabilityPosition];
            for (incrementedVariable = 0; incrementedVariable < numReferenceVariables; incrementedVariable++) {
                referenceVariablesCoordinate[incrementedVariable]++;
                if (referenceVariablesCoordinate[incrementedVariable] == referenceVariablesDimensions[incrementedVariable]) {
                    referenceVariablesCoordinate[incrementedVariable] = 0;
                } else {
                    break;
                }
            }
            if (incrementedVariable != numReferenceVariables) {
                utilityPosition += accOffUtilityPot[incrementedVariable];
                probabilityPosition += accOffProbabilityPot[incrementedVariable];
            }
            if (incrementedVariable != FIRST_VARIABLE_POSITION) {
                if (!checkZero(probabilities) && !allZeroPartition(partitions)) {
                    result.elementTable.add(CEBaseOperations.weightedAverage(partitions, variableToRemove, probabilities));
                } else {
                    result.elementTable.add(CEP.getZeroPartition());
                }
                i = 0;
                partitions.clear();
            }
        }
        result.setCriterion(utilityPotential.getCriterion());
        return result;
    }

    /**
     * Creates a {@code GTablePotential} of {@code CEP}s from the cost and effectiveness
     * {@code TablePotential}s.
     *
     * @param costPotential          {@code TablePotential}
     * @param effectivenessPotential {@code TablePotential}
     * @param lambdaMin              minimum willingness-to-pay threshold
     * @param lambdaMax              maximum willingness-to-pay threshold
     * @return combined {@code GTablePotential}
     */
    @SuppressWarnings("unchecked")
    public static GTablePotential getCEPotential(TablePotential costPotential, TablePotential effectivenessPotential,
                                                  double lambdaMin, double lambdaMax) {
        ArrayList<Variable> costVariables = new ArrayList<>(costPotential.getVariables());
        ArrayList<Variable> costAndEffectivenessVariables = new ArrayList<>(costVariables);
        for (Variable v : effectivenessPotential.getVariables()) {
            if (!costAndEffectivenessVariables.contains(v)) {
                costAndEffectivenessVariables.add(v);
            }
        }

        GTablePotential gPotential = new GTablePotential(new ArrayList<>(costAndEffectivenessVariables),
                                                          PotentialRole.UNSPECIFIED);
        int[] dimensionsResult = gPotential.getDimensions();
        int[] coordinate = new int[dimensionsResult.length];
        int[][] accumulatedOffsets = new int[2][];
        accumulatedOffsets[COST] = gPotential.getAccumulatedOffsets(costPotential.getVariables());
        accumulatedOffsets[EFFECTIVENESS] = gPotential.getAccumulatedOffsets(effectivenessPotential.getVariables());
        int[] positions = {costPotential.getInitialPosition(), effectivenessPotential.getInitialPosition()};
        int tableSize = gPotential.getTableSize();

        for (int i = 0; i < tableSize; i++) {
            double cost = costPotential.getValues()[positions[COST]];
            double effectiveness = effectivenessPotential.getValues()[positions[EFFECTIVENESS]];
            CEP partition = new CEP.CEPBuilder().thresholdBounds(lambdaMin, lambdaMax).build(null, cost, effectiveness);
            gPotential.elementTable.add(partition);
            int increasedVariable = 0;
            for (int j = 0; j < coordinate.length; j++) {
                coordinate[j]++;
                if (coordinate[j] < dimensionsResult[j]) {
                    increasedVariable = j;
                    break;
                }
                coordinate[j] = 0;
            }
            for (int j = 0; j < positions.length; j++) {
                positions[j] += accumulatedOffsets[j][increasedVariable];
            }
        }
        return gPotential;
    }

    /**
     * Marginalizes a {@code GTablePotential} of {@code CEP}s over {@code variableToDelete}.
     *
     * @param cepsPotential                 {@code GTablePotential}
     * @param variableToDelete              variable to eliminate
     * @param chanceVariableDistribution    probability distribution of {@code variableToDelete}
     * @return marginalized {@code GTablePotential}
     */
    @SuppressWarnings("unchecked")
    public static GTablePotential marginalize(GTablePotential cepsPotential, Variable variableToDelete,
                                               double[] chanceVariableDistribution)
            throws CostEffectivenessException.PartitionsAndProbabilitiesHaveDifferentSizes {
        ArrayList<Variable> potentialVariables = new ArrayList<>(cepsPotential.getVariables());
        int numVariables = potentialVariables.size();
        potentialVariables.remove(variableToDelete);
        GTablePotential marginalized = new GTablePotential(potentialVariables, cepsPotential.getPotentialRole());

        ArrayList<Variable> fictitiousVariables = new ArrayList<>();
        fictitiousVariables.add(variableToDelete);
        fictitiousVariables.addAll(potentialVariables);
        potentialVariables = new ArrayList<>(cepsPotential.getVariables());

        int variablePosition = potentialVariables.indexOf(variableToDelete);
        int[] potentialDimensions = cepsPotential.getDimensions();
        int numVariableOptions = potentialDimensions[variablePosition];

        TablePotential fictitious = new TablePotential(fictitiousVariables, null);
        int[] offsetsAccPotential = fictitious.getAccumulatedOffsets(potentialVariables);
        int potentialPosition = 0;
        int[] fictCoordinate = new int[numVariables];
        int[] fictDimensions = fictitious.getDimensions();

        int incremented;
        do {
            ArrayList<CEP> partitions = new ArrayList<>();
            for (int i = 0; i < numVariableOptions; i++) {
                partitions.add((CEP) cepsPotential.elementTable.get(potentialPosition));
                if (i < numVariableOptions - 1) {
                    potentialPosition += offsetsAccPotential[0];
                }
            }
            CEP marginalization = addMarginalize(partitions, variableToDelete, chanceVariableDistribution);
            marginalized.elementTable.add(marginalization);
            for (incremented = 1; incremented < numVariables; incremented++) {
                fictCoordinate[incremented]++;
                if (fictCoordinate[incremented] == fictDimensions[incremented]) {
                    fictCoordinate[incremented] = 0;
                } else {
                    break;
                }
            }
            if (incremented != numVariables) {
                potentialPosition += offsetsAccPotential[incremented];
            }
        } while (incremented != numVariables);
        return marginalized;
    }

    /**
     * Computes the optimal decision by eliminating {@code decision} from
     * a {@code GTablePotential} of {@code CEP}s.
     *
     * @param potential {@code GTablePotential}
     * @param decision  decision variable to eliminate
     * @return {@code GTablePotential} without {@code decision}
     */
    @SuppressWarnings("unchecked")
    public static GTablePotential ceMaximize(GTablePotential potential, Variable decision) {
        ArrayList<Variable> resultPotentialVariables = new ArrayList<>(potential.getVariables());
        resultPotentialVariables.remove(decision);
        GTablePotential resultPotential = new GTablePotential(resultPotentialVariables, PotentialRole.UNSPECIFIED);

        ArrayList<Variable> potentialVariables = new ArrayList<>(potential.getVariables());
        int numVariables = potentialVariables.size();
        ArrayList<Variable> orderVariables = new ArrayList<>(numVariables);
        orderVariables.add(decision);
        orderVariables.addAll(resultPotentialVariables);
        TablePotential orderPotential = new TablePotential(orderVariables, null);

        int[] accOffsetsPotential = orderPotential.getAccumulatedOffsets(potentialVariables);
        int potentialPosition = 0;
        int[] orderCoordinate = new int[orderVariables.size()];
        int[] orderDimensions = orderPotential.getDimensions();
        int numDecisionOptions = decision.getNumStates();

        int incrementedVariable;
        do {
            ArrayList<CEP> partitions = new ArrayList<>();
            for (int i = 0; i < numDecisionOptions; i++) {
                partitions.add((CEP) potential.elementTable.get(potentialPosition));
                if (i < numDecisionOptions - 1) {
                    potentialPosition += accOffsetsPotential[0];
                }
            }
            CEP maximizedPartition = CEBaseOperations.optimalCEP(decision, partitions);
            resultPotential.elementTable.add(maximizedPartition);
            incrementedVariable = 1;
            if (incrementedVariable < numVariables) {
                boolean more;
                do {
                    orderCoordinate[incrementedVariable]++;
                    if (orderCoordinate[incrementedVariable] == orderDimensions[incrementedVariable]) {
                        orderCoordinate[incrementedVariable++] = 0;
                        more = true;
                    } else {
                        more = false;
                    }
                } while (incrementedVariable < numVariables && more);
            }
            if (incrementedVariable < numVariables) {
                potentialPosition += accOffsetsPotential[incrementedVariable];
            }
        } while (incrementedVariable < numVariables);
        resultPotential.setCriterion(potential.getCriterion());
        return resultPotential;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static boolean allZeroPartition(List<? extends CEP> partitions) {
        return partitions.stream().allMatch(CEP::isZero);
    }

    private static boolean checkZero(double[] probabilities) {
        int i;
        for (i = 0; i < probabilities.length && probabilities[i] == 0.0; i++) ;
        return i == probabilities.length;
    }

    //TODO: Adapt to CEPBuilder if it comes to be used in the code.
    private static CEP addMarginalize(ArrayList<CEP> partitions, Variable variableToDelete, double[] probabilities)
            throws CostEffectivenessException.PartitionsAndProbabilitiesHaveDifferentSizes {
        int numPartitions = partitions.size();
        if (numPartitions != probabilities.length) {
            throw new CostEffectivenessException.PartitionsAndProbabilitiesHaveDifferentSizes(partitions, probabilities);
        }
        if (checkZero(probabilities)) {
            return CEP.getZeroPartition();
        }
        boolean[] takeIntoAccount = new boolean[probabilities.length];
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] != 0.0) {
                takeIntoAccount[i] = true;
            }
        }
        double[] thresholds = CEBaseOperations.getUnionThresholds(partitions, null);
        int numIntervals = thresholds.length + 1;
        double[] costs = new double[numIntervals];
        double[] effectivities = new double[numIntervals];
        StrategyTree[] strategyTrees = new StrategyTree[numIntervals];

        for (int interval = 0; interval < numIntervals; interval++) {
            double cost = 0;
            double effectiveness = 0;
            Potential[] interventionsInterval = new Potential[numPartitions];
            for (int partitionIndex = 0; partitionIndex < numPartitions; partitionIndex++) {
                CEP partition = partitions.get(partitionIndex);
                double medium;
                if (interval == 0) {
                    medium = (partition.getMinThreshold() + thresholds[0]) / 2;
                } else if (interval == thresholds.length) {
                    medium = (thresholds[thresholds.length - 1] + partition.getMaxThreshold()) / 2;
                } else {
                    medium = (thresholds[interval - 1] + thresholds[interval]) / 2;
                }
                cost += partition.getCost(medium);
                effectiveness += partition.getEffectiveness(medium);
                interventionsInterval[partitionIndex] = partition.getIntervention(medium);
            }
            costs[interval] = cost;
            effectivities[interval] = effectiveness;

            List<State> states = null;
            StrategyTree intervalTree = null;
            for (int partitionIndex = 0; partitionIndex < numPartitions; partitionIndex++) {
                if (takeIntoAccount[partitionIndex]) {
                    states = new ArrayList<>();
                    states.add(variableToDelete.getStates()[partitionIndex]);
                    Potential intervention = strategyTrees[partitionIndex];
                    for (int j = partitionIndex + 1; j < numPartitions; j++) {
                        if ((intervention != null && interventionsInterval[j] != null) ?
                                intervention.equals(interventionsInterval[j]) :
                                intervention == null && interventionsInterval[j] == null) {
                            takeIntoAccount[j] = false;
                            states.add(variableToDelete.getStates()[j]);
                        }
                    }
                    if (intervalTree == null) {
                        intervalTree = new StrategyTree(variableToDelete);
                    }
                    //TODO: parentVariables is null.
                    intervalTree.addBranch(new TreeADDBranch(states, variableToDelete, intervention, null));
                }
            }
            strategyTrees[interval] = intervalTree;
        }
        var cepBuilder = new CEP.CEPBuilder();
        for (int i = 0; i < thresholds.length; i++) {
            cepBuilder.addRow(strategyTrees[i], costs[i], effectivities[i], thresholds[i]);
        }
        return cepBuilder.build(strategyTrees[thresholds.length], costs[thresholds.length], effectivities[thresholds.length]);
    }
}
