/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.openmarkov.core.exception.PotentialOperationException;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.AbstractIndexedPotential;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.StrategicTablePotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements the {@code merge} operation and its supporting helpers.
 * <p>
 * Merge combines a list of {@link TablePotential}s (one per state of a
 * decision variable) into a single potential whose first variable is the
 * decision variable.
 *
 * @author Manuel Arias
 */
final class TablePotentialMerge {

    private TablePotentialMerge() {
    }

    /**
     * Merges a list of potentials (one per state of {@code decision}) into a
     * single potential whose first variable is {@code decision}.
     *
     * @param decision   Decision variable (its number of states must equal the
     *                   number of potentials)
     * @param potentials List of potentials, one per state of {@code decision}
     * @return merged {@link TablePotential}
     * @throws PotentialOperationException.DifferentSizesInPotentialsAndStates if sizes differ
     */
    @SuppressWarnings("unchecked")
    static AbstractIndexedPotential merge(Variable decision, List<? extends AbstractIndexedPotential> potentials)
            throws PotentialOperationException.DifferentSizesInPotentialsAndStates {
        throwExceptionIfNecessaryInMergeOperation(decision, potentials);

        // Gets merged potential variables: decision variable + union of potentials' variables
        List<Variable> potentialsVariables = AuxiliaryOperations.getUnionVariables(potentials);
        List<Variable> mergedVariables = new ArrayList<>(potentialsVariables.size() + 1);
        mergedVariables.add(decision);
        mergedVariables.addAll(potentialsVariables);

        int numMergedVariables = mergedVariables.size();

        int[] mergedDimension = TablePotential.calculateDimensions(mergedVariables);
        int[][] offsetAccumulate = AuxiliaryOperations.getAccumulatedOffsets(potentials, mergedVariables);
        int[] offsets = TablePotential.calculateOffsets(mergedDimension);
        int tableSize = mergedDimension[numMergedVariables - 1] * offsets[numMergedVariables - 1];
        double[] mergedValues = new double[tableSize];
        int numPotentials = potentials.size();

        // Interventions (strategy trees)
        boolean thereArePotentialsWithInterventions = thereArePotentialsWithInterventions(potentials);
        StrategyTree[] mergedInterventions = thereArePotentialsWithInterventions ? new StrategyTree[tableSize] : null;
        boolean[] potentialsHaveInterventions = thereArePotentialsWithInterventions
                ? getPotentialsHaveInterventions(potentials) : null;
        StrategyTree[][] potentialsInterventions = thereArePotentialsWithInterventions
                ? new StrategyTree[numPotentials][] : null;

        // Uncertain values
        boolean thereArePotentialsWithUncertainValues = thereArePotentialsWithUncertainValues(potentials);
        UncertainValue[] mergedUncertainValues = thereArePotentialsWithUncertainValues
                ? new UncertainValue[tableSize] : null;
        boolean[] potentialsHaveUncertainValues = thereArePotentialsWithUncertainValues
                ? getPotentialsHaveUncertainValues(potentials) : null;
        UncertainValue[][] potentialsUncertainValues = thereArePotentialsWithUncertainValues
                ? new UncertainValue[numPotentials][] : null;

        // GTablePotential subtypes
        boolean thereAreGTablePotentials = thereAreGTablePotentials(potentials);
        List<CEP> mergedElementsTable = null;
        boolean[] potentialsAreGTablePotentials = null;
        List<List<CEP>> elementsTables = null;
        if (thereAreGTablePotentials) {
            mergedElementsTable = new ArrayList<>(tableSize);
            for (int i = 0; i < tableSize; i++) {
                mergedElementsTable.add(null);
            }
            potentialsAreGTablePotentials = getBooleanArrayOfPotentialsThatAreGTablePotentials(potentials,
                    numPotentials);
            elementsTables = new ArrayList<>(numPotentials);
            for (int i = 0; i < numPotentials; i++) {
                elementsTables.add(null);
            }
        }

        // Populate tables, interventions and uncertain values from each potential
        double[][] tables = new double[numPotentials][];
        for (int indexPotential = 0; indexPotential < numPotentials; indexPotential++) {
            AbstractIndexedPotential potential = potentials.get(indexPotential);
            if (potential instanceof GTablePotential<?> gtp) {
                // GTablePotential has no values[] array; use a zero-filled placeholder.
                // mergedValues is only used when there are no GTablePotentials in the result.
                tables[indexPotential] = new double[gtp.getTableSize()];
                elementsTables.set(indexPotential, ((GTablePotential<CEP>) gtp).elementTable);
            } else {
                TablePotential tp = (TablePotential) potential;
                tables[indexPotential] = tp.getValues();
                if (thereArePotentialsWithInterventions) {
                    potentialsInterventions[indexPotential] = tp instanceof StrategicTablePotential stp
                            ? stp.strategyTrees : null;
                }
                if (thereArePotentialsWithUncertainValues) {
                    potentialsUncertainValues[indexPotential] = tp.getUncertainValues();
                }
            }
        }

        int[] mergedCoordinate = new int[Math.max(1, numMergedVariables)];
        int[] potentialsPositions = new int[numPotentials];
        int indexIncrementedVariable = 0;

        for (int mergedPosition = 0; mergedPosition < tableSize; mergedPosition++) {
            int indexActualPotential = mergedCoordinate[0];
            int indexInTableOfActualPotential = potentialsPositions[indexActualPotential];
            mergedValues[mergedPosition] = tables[indexActualPotential][indexInTableOfActualPotential];
            if (thereArePotentialsWithInterventions) {
                mergedInterventions[mergedPosition] = potentialsHaveInterventions[indexActualPotential]
                        ? potentialsInterventions[indexActualPotential][indexInTableOfActualPotential] : null;
            }
            if (thereArePotentialsWithUncertainValues) {
                mergedUncertainValues[mergedPosition] = potentialsHaveUncertainValues[indexActualPotential]
                        ? potentialsUncertainValues[indexActualPotential][indexInTableOfActualPotential] : null;
            }
            if (thereAreGTablePotentials) {
                CEP auxCEP = potentialsAreGTablePotentials[indexActualPotential]
                        ? elementsTables.get(indexActualPotential).get(indexInTableOfActualPotential) : null;
                mergedElementsTable.set(mergedPosition, auxCEP);
            }

            indexIncrementedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(
                    mergedDimension, mergedCoordinate, indexIncrementedVariable);

            for (int indexPotential = 0; indexPotential < numPotentials; indexPotential++) {
                potentialsPositions[indexPotential] += offsetAccumulate[indexPotential][indexIncrementedVariable];
            }
        }

        PotentialRole role = potentials.get(0).getPotentialRole();
        AbstractIndexedPotential mergedPotential;
        if (thereAreGTablePotentials) {
            mergedPotential = new GTablePotential<>(mergedVariables, role, mergedElementsTable);
            // Note: GTablePotential does not support uncertain values
        } else if (thereArePotentialsWithInterventions) {
            StrategicTablePotential stp = new StrategicTablePotential(mergedVariables, role, mergedValues);
            stp.strategyTrees = mergedInterventions;
            mergedPotential = stp;
            // Note: when there are also uncertain values alongside interventions (rare), they are not propagated
        } else if (thereArePotentialsWithUncertainValues) {
            UncertainTablePotential utp = new UncertainTablePotential(mergedVariables, role, mergedValues);
            utp.uncertainValues = mergedUncertainValues;
            mergedPotential = utp;
        } else {
            mergedPotential = new TablePotential(mergedVariables, role, mergedValues);
        }
        return mergedPotential;
    }

    /**
     * Returns a list of potentials from {@code inputPotentialsList} ordered
     * according to the total order given by {@code decisionsTotallyOrdered}.
     *
     * @param inputPotentialsList      list of potentials to order
     * @param decisionsTotallyOrdered  total order of decision variables
     * @return ordered list of potentials
     */
    static List<TablePotential> orderPotentialsByTotalOrder(List<TablePotential> inputPotentialsList,
                                                            List<Variable> decisionsTotallyOrdered) {
        List<TablePotential> orderedListOfPotentials = new ArrayList<>();
        if (decisionsTotallyOrdered != null) {
            Set<TablePotential> inputPotentialsSet = new HashSet<>(inputPotentialsList);
            Set<TablePotential> potentialsWithoutIntervention = new HashSet<>();
            for (TablePotential auxPot : inputPotentialsSet) {
                if (!auxPot.hasInterventions()) {
                    potentialsWithoutIntervention.add(auxPot);
                }
            }
            for (Variable dec : decisionsTotallyOrdered) {
                Set<TablePotential> potentialsWithDecisionInIntervention = getPotentialsWithDecisionInIntervention(dec,
                        inputPotentialsSet);
                inputPotentialsSet.removeAll(potentialsWithDecisionInIntervention);
                orderedListOfPotentials.addAll(potentialsWithDecisionInIntervention);
            }
            inputPotentialsSet.removeAll(potentialsWithoutIntervention);
            orderedListOfPotentials.addAll(potentialsWithoutIntervention);
            orderedListOfPotentials.addAll(inputPotentialsList);
        } else {
            orderedListOfPotentials.addAll(inputPotentialsList);
        }
        return orderedListOfPotentials;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void throwExceptionIfNecessaryInMergeOperation(Variable decision,
                                                                   Collection<? extends AbstractIndexedPotential> potentials)
            throws PotentialOperationException.DifferentSizesInPotentialsAndStates {
        if (potentials == null) {
            potentials = new ArrayList<>();
        }
        if (decision.getStates().length != potentials.size()) {
            throw new PotentialOperationException.DifferentSizesInPotentialsAndStates(decision, potentials);
        }
    }

    private static boolean thereAreGTablePotentials(Collection<? extends Potential> potentials) {
        return findFirstGTablePotential(potentials) != null;
    }

    private static Potential findFirstGTablePotential(Collection<? extends Potential> potentials) {
        for (Potential potential : potentials) {
            if (potential instanceof GTablePotential) {
                return potential;
            }
        }
        return null;
    }

    private static boolean[] getBooleanArrayOfPotentialsThatAreGTablePotentials(
            List<? extends AbstractIndexedPotential> potentials, int numPotentials) {
        boolean[] result = new boolean[numPotentials];
        int i = 0;
        for (AbstractIndexedPotential potential : potentials) {
            result[i++] = potential instanceof GTablePotential;
        }
        return result;
    }

    private static boolean[] getPotentialsHaveInterventions(List<? extends AbstractIndexedPotential> potentials) {
        boolean[] result = new boolean[potentials.size()];
        int i = 0;
        for (AbstractIndexedPotential potential : potentials) {
            result[i++] = potential instanceof StrategicTablePotential stp && stp.strategyTrees != null;
        }
        return result;
    }

    private static boolean[] getPotentialsHaveUncertainValues(List<? extends AbstractIndexedPotential> potentials) {
        boolean[] result = new boolean[potentials.size()];
        int i = 0;
        for (AbstractIndexedPotential potential : potentials) {
            result[i++] = potential instanceof UncertainTablePotential utp && utp.uncertainValues != null;
        }
        return result;
    }

    private static boolean thereArePotentialsWithUncertainValues(Collection<? extends AbstractIndexedPotential> potentials) {
        for (AbstractIndexedPotential potential : potentials) {
            if (potential instanceof UncertainTablePotential utp && utp.uncertainValues != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean thereArePotentialsWithInterventions(Collection<? extends AbstractIndexedPotential> potentials) {
        for (AbstractIndexedPotential potential : potentials) {
            if (potential instanceof StrategicTablePotential stp && stp.strategyTrees != null) {
                return true;
            }
        }
        return false;
    }

    private static Set<TablePotential> getPotentialsWithDecisionInIntervention(Variable decision,
                                                                                Set<TablePotential> inputPotentials) {
        Set<TablePotential> result = new HashSet<>();
        for (TablePotential auxPot : inputPotentials) {
            if (auxPot.hasInterventionForDecision(decision)) {
                result.add(auxPot);
            }
        }
        return result;
    }
}
