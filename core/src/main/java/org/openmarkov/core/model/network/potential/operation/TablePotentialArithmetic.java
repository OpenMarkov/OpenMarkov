/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.operation;

import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.model.network.potential.StrategicTablePotential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Arithmetic operations on {@code TablePotential}s: multiply, sum, divide, and related helpers.
 * Methods are package-private so that {@code DiscretePotentialOperations} can delegate to them.
 *
 * @author Manuel Arias
 */
final class TablePotentialArithmetic {

    private TablePotentialArithmetic() {
    }

    /**
     * @param tablePotentials {@code ArrayList} of extends {@code Potential}.
     *
     * @return A {@code TablePotential} as result.
     */
    static TablePotential multiply(List<TablePotential> tablePotentials) {
        return multiply(tablePotentials, true);
    }

    /**
     * @param potentials {@code ArrayList} of extends {@code Potential}.
     *
     * @return A {@code TablePotential} as result.
     */
    static TablePotential multiply(TablePotential... potentials) {
        List<TablePotential> potentialsToMultiply = new ArrayList<>();
        Collections.addAll(potentialsToMultiply, potentials);
        return multiply(potentialsToMultiply);
    }

    /**
     * @param tablePotentials {@code ArrayList} of extends {@code Potential}.
     * @param reorder         Sorts or not the potentials prior to multiplication.
     *                        {@code boolean}.
     *
     * @return A {@code TablePotential} as result. //TODO
     */
    static TablePotential multiply(List<TablePotential> tablePotentials, boolean reorder) {
        int numPotentials = tablePotentials.size();

        // Special cases: one or zero potentials
        if (numPotentials < 2) {
            if (numPotentials == 1) {
                return tablePotentials.getFirst();
            }
            return buildConstantPotential(1.0, PotentialRole.CONDITIONAL_PROBABILITY);
        }
        // Find out if some potential has criterion. In that case, set that criterion in
        // the resulting potential
        Criterion criterion = findFirstNonNullCriterion(tablePotentials);

        List<TablePotential> potentials = new ArrayList<>(tablePotentials);

        // Sort the potentials according to the table size
        if (reorder) {
            Collections.sort(potentials);
        }

        // Gets constant factor: The product of constant potentials
        double constantFactor = AuxiliaryOperations.getConstantFactor(potentials);

        // get role
        PotentialRole role = getRole(potentials);

        potentials = AuxiliaryOperations.getNonConstantPotentials(potentials);
        if (potentials.isEmpty()) {
            return buildConstantPotential(constantFactor, role);
        }

        // Gets the union
        List<Variable> resultVariables = AuxiliaryOperations.getUnionVariables(potentials);

        int numVariables = resultVariables.size();

        // Gets the tables of each TablePotential
        numPotentials = potentials.size();
        double[][] tables = initializeFromValues(potentials);

        // Gets dimension
        int[] resultDimension = TablePotential.calculateDimensions(resultVariables);

        // Gets offset accumulate
        int[][] offsetAccumulate = AuxiliaryOperations.getAccumulatedOffsets(potentials, resultVariables);

        // Gets coordinate
        int[] resultCoordinate = initializeCoordinates(numVariables);

        // Position in each table potential
        int[] potentialsPositions = initializeToZero(numPotentials);

        // Multiply
        int incrementedVariable = 0;

        int[] offsets = TablePotential.calculateOffsets(resultDimension);
        int tableSize = numVariables > 0 ? resultDimension[numVariables - 1] * offsets[numVariables - 1] : 1;
        double[] resultValues = new double[tableSize];

        StrategicTablePotential potentialWithInterventions = findFirstPotentialWithInterventions(tablePotentials);
        boolean thereAreInterventions = (potentialWithInterventions != null);
        StrategyTree[] resultStrategyTrees = null;
        StrategyTree strategyTree = null;
        StrategyTree[] inputStrategyTrees = null;
        if (thereAreInterventions) {
            inputStrategyTrees = potentialWithInterventions.strategyTrees;
            resultStrategyTrees = new StrategyTree[tableSize];
            if (potentialWithInterventions.getVariables().isEmpty()) {
                // The interventions are in a constant potential
                strategyTree = inputStrategyTrees[0];
            }
        }

        int indexPotentialWithInterventions = potentials.indexOf(potentialWithInterventions);

        for (int resultPosition = 0; resultPosition < tableSize; resultPosition++) {
            double mulResult = constantFactor;

            incrementedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(resultDimension, resultCoordinate, incrementedVariable);

            // multiply
            for (int iPotential = 0; iPotential < numPotentials; iPotential++) {
                // multiply the numbers
                mulResult = mulResult * tables[iPotential][potentialsPositions[iPotential]];
                // Obtain the intervention
                if (thereAreInterventions && indexPotentialWithInterventions == iPotential) {
                    strategyTree = inputStrategyTrees[potentialsPositions[iPotential]];
                }
                // update the current position in each potential table
                potentialsPositions[iPotential] += offsetAccumulate[iPotential][incrementedVariable];
            }

            resultValues[resultPosition] = mulResult;
            if (thereAreInterventions) {
                resultStrategyTrees[resultPosition] = strategyTree;
            }

        }

        return buildResultPotential(criterion, role, resultVariables, resultValues,
                                                              thereAreInterventions, resultStrategyTrees);
    }

    private static Criterion findFirstNonNullCriterion(List<TablePotential> tablePotentials) {
        Criterion criterion = null;
        for (int i = 0; i < tablePotentials.size() && criterion == null; i++) {
            criterion = tablePotentials.get(i).getCriterion();
        }
        return criterion;
    }

    private static int[] initializeToZero(int numPotentials) {
        return new int[numPotentials];
    }

    /**
     * @param tablePotentials List of TablePotential
     *
     * @return First potential with interventions, or {@code null} if none
     */
    private static StrategicTablePotential findFirstPotentialWithInterventions(List<TablePotential> tablePotentials) {
        for (TablePotential auxPotential : tablePotentials) {
            if (auxPotential instanceof StrategicTablePotential stp) {
                return stp;
            }
        }
        return null;
    }

    /**
     * @param tablePotentials {@code List} of {@code TablePotential}s.
     *
     * @return {@code TablePotential}
     */
    static TablePotential sum(List<TablePotential> tablePotentials) {
        if (tablePotentials == null || tablePotentials.isEmpty()) {
            return new TablePotential(null, PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.0});
        }
        if (tablePotentials.size() == 1) {
            return tablePotentials.getFirst();
        }

        // list of non-constant potentials
        List<TablePotential> potentials = new ArrayList<>(tablePotentials);

        // Leave out the constant potentials
        List<TablePotential> constantPotentials = new ArrayList<>();
        for (TablePotential auxPotential : tablePotentials) {
            if (auxPotential.getVariables().isEmpty()) {
                potentials.remove(auxPotential);
                constantPotentials.add(auxPotential);
            }
        }

        // Calculate the sum of constant potentials
        double sumConstantPotentials = 0.0;
        StrategyTree constantPotentialsStrategyTree = null;
        int numConstantPotentials = constantPotentials.size();
        for (TablePotential constantPotential : constantPotentials) {
            sumConstantPotentials += constantPotential.getValues()[0];
            StrategyTree[] iConstantPotentialStrategyTrees = constantPotential instanceof StrategicTablePotential stp
                    ? stp.strategyTrees : null;
            if (iConstantPotentialStrategyTrees != null) {
                StrategyTree onlyStrategyTreeIConstantPotential = iConstantPotentialStrategyTrees[0];
                constantPotentialsStrategyTree = (constantPotentialsStrategyTree == null)
                        ? onlyStrategyTreeIConstantPotential
                        : constantPotentialsStrategyTree.concatenate(onlyStrategyTreeIConstantPotential);
            }
        }

        // From here operate only with non constant potentials
        int numPotentials = potentials.size();

        // Gets the union
        List<Variable> resultVariables = AuxiliaryOperations.getUnionVariables(potentials);
        int numVariables = resultVariables.size();

        double[][] tables = initializeFromValues(potentials);

        // Gets the interventions if necessary
        boolean thereAreInterventions = areThereInterventions(potentials);

        StrategyTree[][] strategyTrees = initializeFromStrategyTrees(potentials, thereAreInterventions);

        // Gets the dimensions
        int[] resultDimensions = TablePotential.calculateDimensions(resultVariables);

        // Gets the accumulated offsets
        int[][] accumulatedOffsets = AuxiliaryOperations.getAccumulatedOffsets(potentials, resultVariables);

        // Gets the coordinates
        int[] resultCoordinates = initializeCoordinates(numVariables);

        // Position in each table potential
        int[] potentialsPositions = initializeToZero(numPotentials);

        // Sum
        int incrementedVariable = 0;
        boolean resultVariablesNotEmpty = !resultVariables.isEmpty();
        int[] dimensions = resultVariablesNotEmpty ? TablePotential.calculateDimensions(resultVariables) : new int[0];
        int[] offsets = resultVariablesNotEmpty ? TablePotential.calculateOffsets(dimensions) : new int[0];
        int tableSize = 1; // If numVariables == 0 the potential is a constant
        if (numVariables > 0) {
            tableSize = dimensions[numVariables - 1] * offsets[numVariables - 1];
        }
        double[] resultValues = new double[tableSize];
        StrategyTree[] resultStrategyTrees = (thereAreInterventions || constantPotentialsStrategyTree != null)
                ? new StrategyTree[tableSize]
                : null;

        if (!potentials.isEmpty()) {
            double sum;
            for (int resultPosition = 0; resultPosition < tableSize; resultPosition++) {
                incrementedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(resultDimensions, resultCoordinates, incrementedVariable);

                // sum
                sum = 0;
                StrategyTree resultStrategyTree = null;
                for (int iPotential = 0; iPotential < numPotentials; iPotential++) {
                    // sum the numbers
                    sum = sum + tables[iPotential][potentialsPositions[iPotential]];
                    if (thereAreInterventions && strategyTrees[iPotential] != null) {
                        StrategyTree auxIStrategyTree = strategyTrees[iPotential][potentialsPositions[iPotential]];
                        resultStrategyTree = (resultStrategyTree == null) ? auxIStrategyTree
                                : resultStrategyTree.concatenate(auxIStrategyTree);
                    }

                    // update the current position in each potential table
                    potentialsPositions[iPotential] += accumulatedOffsets[iPotential][incrementedVariable];
                }
                resultValues[resultPosition] = sum;
                if (thereAreInterventions) {
                    resultStrategyTrees[resultPosition] = resultStrategyTree;
                }
            }
        }
        // Sum constant potentials to the result
        if ((numConstantPotentials > 0) && (sumConstantPotentials != 0.0 || constantPotentialsStrategyTree != null)) {
            for (int i = 0; i < resultValues.length; i++) {
                resultValues[i] = resultValues[i] + sumConstantPotentials;
                if (constantPotentialsStrategyTree != null) {
                    if (resultStrategyTrees[i] == null) {
                        resultStrategyTrees[i] = constantPotentialsStrategyTree;
                    } else {
                        resultStrategyTrees[i].concatenate(constantPotentialsStrategyTree);
                    }
                }
            }
        }
        TablePotential result;
        if (resultStrategyTrees != null) {
            StrategicTablePotential stp = new StrategicTablePotential(resultVariables, getRole(tablePotentials), resultValues);
            stp.strategyTrees = resultStrategyTrees;
            result = stp;
        } else {
            result = new TablePotential(resultVariables, getRole(tablePotentials), resultValues);
        }
        if (!potentials.isEmpty()) {
            result.setCriterion(potentials.getFirst().getCriterion());
        }

        return result;
    }

    private static StrategyTree[][] initializeFromStrategyTrees(List<TablePotential> potentials,
                                                                boolean thereAreInterventions) {
        int numPotentials = potentials.size();
        StrategyTree[][] strategyTrees = null;
        if (thereAreInterventions) {
            strategyTrees = new StrategyTree[numPotentials][];
            for (int i = 0; i < numPotentials; i++) {
                strategyTrees[i] = potentials.get(i) instanceof StrategicTablePotential stp
                        ? stp.strategyTrees : null;
            }
        }
        return strategyTrees;
    }

    /**
     * @param potentials List of table potentials
     *
     * @return if there is at least one potential with interventions.
     */
    private static boolean areThereInterventions(List<TablePotential> potentials) {
        return findFirstPotentialWithInterventions(potentials) != null;
    }

    /**
     * @param utilityPotentials {@code List} of {@code TablePotential}s.
     *
     * @return A TablePotential for each criterion
     */
    static List<TablePotential> sumByCriterion(List<TablePotential> utilityPotentials) {
        // create the set of criteria
        Set<Criterion> criteria = new HashSet<>();
        for (TablePotential potential : utilityPotentials) {
            criteria.add(potential.getCriterion());
        }

        // create an empty list for each criterion
        Map<Criterion, List<TablePotential>> potentialsByCriterion = new HashMap<>();
        for (Criterion criterion : criteria) {
            List<TablePotential> criterionList = new ArrayList<>();
            potentialsByCriterion.put(criterion, criterionList);
        }

        // put each potential in its list
        for (TablePotential potential : utilityPotentials) {
            potentialsByCriterion.get(potential.getCriterion()).add(potential);
        }

        // sum the potentials for each criterion
        List<TablePotential> utilityPotentialsByCriterion = new ArrayList<>(criteria.size());
        for (Criterion criterion : criteria) {
            TablePotential outputUtilityPotentialByCriterion = sum(potentialsByCriterion.get(criterion));
            outputUtilityPotentialByCriterion.setCriterion(criterion);
            utilityPotentialsByCriterion.add(outputUtilityPotentialByCriterion);
        }
        return utilityPotentialsByCriterion;
    }

    private static int[] initializeCoordinates(int numVariables) {
        return new int[Math.max(1, numVariables)];
    }

    static TablePotential sum(TablePotential... tablePotentials) {
        List<TablePotential> potentialList = new ArrayList<>(tablePotentials.length);
        Collections.addAll(potentialList, tablePotentials);
        return sum(potentialList);
    }

    /**
     * @param potentials Collection of potentials
     *
     * @return The potential role
     */
    static PotentialRole getRole(Collection<? extends Potential> potentials) {
        PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY; // Default value
        boolean atLeastOneUtility = false;
        boolean atLeastOneJoinProb = false;
        for (Potential potential : potentials) {
            atLeastOneUtility = atLeastOneUtility || potential.isAdditive();
            atLeastOneJoinProb = atLeastOneJoinProb || potential.getPotentialRole() == PotentialRole.JOINT_PROBABILITY;
        }
        if (atLeastOneUtility) {
            role = PotentialRole.UNSPECIFIED;
        } else {
            if (atLeastOneJoinProb) {
                role = PotentialRole.JOINT_PROBABILITY;
            }
        }
        return role;
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
    static TablePotential divide(Potential numerator, Potential denominator) {
        // Get variables and create quotient potential.
        // Quotient potential variables = numerator potential variables union
        // denominator potential variables
        TablePotential tNumerator = (TablePotential) numerator;
        TablePotential tDenominator = (TablePotential) denominator;
        List<Variable> numeratorVariables = new ArrayList<>(tNumerator.getVariables());
        List<Variable> denominatorVariables = new ArrayList<>(tDenominator.getVariables());
        int numNumeratorVariables = numeratorVariables.size();
        int numDenominatorVariables = denominatorVariables.size();
        denominatorVariables.removeAll(numeratorVariables);
        numeratorVariables.addAll(denominatorVariables);
        TablePotential quotient = new TablePotential(numeratorVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        if ((numNumeratorVariables == 0) || (numDenominatorVariables == 0)) {
            return divide(tNumerator, tDenominator, quotient, numNumeratorVariables, numDenominatorVariables);
        }

        int numVariables = quotient.getNumVariables();

        // Gets the tables of each TablePotential
        //TODO: The array positionTies is never read.
        double[][] tables = new double[2][];
        tables[0] = tNumerator.getValues();
        tables[1] = tDenominator.getValues();

        // Gets dimension
        int[] quotientDimension = quotient.getDimensions();

        // Gets offset accumulate
        List<TablePotential> potentials = new ArrayList<>();
        potentials.add(tNumerator);
        potentials.add(tDenominator);
        int[][] offsetAccumulate = AuxiliaryOperations.getAccumulatedOffsets(potentials, quotient);

        // Gets coordinate
        int[] quotientCoordinate = initializeCoordinates(numVariables);

        // Position in each table potential
        int[] potentialsPositions = initializeToZero(2);

        // Divide
        int incrementedVariable = 0;
        int[] offset = quotient.getOffsets();
        int tamTable = 1; // If numVariables == 0 the potential is a constant
        if (numVariables > 0) {
            tamTable = quotientDimension[numVariables - 1] * offset[numVariables - 1];
        }

        for (int quotientPosition = 0; quotientPosition < tamTable; quotientPosition++) {
            incrementedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(quotientDimension, quotientCoordinate, incrementedVariable);

            // divide
            if (tDenominator.getValues()[potentialsPositions[1]] == 0.0) {
                quotient.getValues()[quotientPosition] = 0.0;
            } else {
                quotient.getValues()[quotientPosition] = tNumerator.getValues()[potentialsPositions[0]]
                        / tDenominator.getValues()[potentialsPositions[1]];
            }
            for (int iPotential = 0; iPotential < 2; iPotential++) {
                // update the current position in each potential table
                potentialsPositions[iPotential] += offsetAccumulate[iPotential][incrementedVariable];
            }
        }

        return quotient;
    }

    /**
     * Divide two potentials when one of them has any variable
     *
     * @param numerator               {@code TablePotential}
     * @param denominator             {@code TablePotential}
     * @param quotient                {@code TablePotential}
     * @param numNumeratorVariables   {@code int}
     * @param numDenominatorVariables {@code int}
     *
     * @return quotient The {@code TablePotential} received with its table.
     */
    private static TablePotential divide(TablePotential numerator, TablePotential denominator, TablePotential quotient,
                                         int numNumeratorVariables, int numDenominatorVariables) {
        if (numNumeratorVariables == 0) {
            int sizeTableDenominator = denominator.getValues().length;
            double dNumerator = numerator.getValues()[0];
            for (int i = 0; i < sizeTableDenominator; i++) {
                quotient.getValues()[i] = dNumerator / denominator.getValues()[i];
            }
        } else {
            int sizeTableNumerator = numerator.getValues().length;
            double dDenominator = denominator.getValues()[0];
            for (int i = 0; i < sizeTableNumerator; i++) {
                quotient.getValues()[i] = numerator.getValues()[i] / dDenominator;
            }
        }
        quotient.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY);
        return quotient;
    }

    /**
     * @param numerator   <tt>Potential</tt>
     * @param denominator <tt>Potential</tt>
     *
     * @return The quotient
     */
    static Potential dividePotentials(Potential numerator, Potential denominator) {
        // parameter correct type verification before calling right method
        if (!(numerator instanceof TablePotential) || !(denominator instanceof TablePotential)) {
            String errMsg = "";
            errMsg = errMsg + "Unsupported operation: " + "divide can only manage potentials of type TablePotential.\n";
            if (numerator == null) {
                errMsg = errMsg + "Numerator = null\n";
            } else {
                if (!(numerator instanceof TablePotential)) {
                    errMsg = errMsg + "Numerator class is " + numerator.getClass().getName() + "\n";
                }
            }
            if (denominator == null) {
                errMsg = errMsg + "Denominator = null\n";
            } else {
                if (!(denominator instanceof TablePotential)) {
                    errMsg = errMsg + "Denominator class is " + denominator.getClass().getName() + "\n";
                }
            }
            throw new NotSupportedOperationException(errMsg);
        }

        return divide(numerator, denominator);
    }

    /**
     * @param values double array
     *
     * @return The sum of values in the array
     */
    static double sum(double[] values) {
        double result = 0.0;
        for (double value : values) {
            result += value;
        }
        return result;
    }

    static TablePotential evaluateFunctionPotential(FunctionPotential utilityPotential,
                                                    List<TablePotential> potentials, List<Variable> utilityVariables)
            throws NonProjectablePotentialException.CannotEvaluate, NonProjectablePotentialException.CannotResolveVariable {
        int numPotentials = potentials.size();

        Criterion criterion = findFirstNonNullCriterion(potentials);
        PotentialRole role = getRole(potentials);
        List<Variable> resultVariables = AuxiliaryOperations.getUnionVariables(potentials);
        int numVariables = resultVariables.size();
        boolean thereAreVariables = numVariables > 0;
        double[][] tables = initializeFromValues(potentials);
        int[] resultDimension = thereAreVariables ? TablePotential.calculateDimensions(resultVariables) : null;
        int[][] offsetAccumulate = AuxiliaryOperations.getAccumulatedOffsets(potentials, resultVariables);
        int[] resultCoordinate = initializeCoordinates(numVariables);
        int[] potentialsPositions = initializeToZero(numPotentials);
        int incrementedVariable = 0;

        int[] offsets = thereAreVariables ? TablePotential.calculateOffsets(resultDimension) : null;
        int tableSize = thereAreVariables ? resultDimension[numVariables - 1] * offsets[numVariables - 1] : 1;
        double[] resultValues = new double[tableSize];

        StrategicTablePotential potentialWithInterventions = findFirstPotentialWithInterventions(potentials);
        boolean thereAreInterventions = (potentialWithInterventions != null);
        StrategyTree[] resultStrategyTrees = null;
        StrategyTree strategyTree = null;
        StrategyTree[] inputStrategyTrees = null;
        if (thereAreInterventions) {
            inputStrategyTrees = potentialWithInterventions.strategyTrees;
            resultStrategyTrees = new StrategyTree[tableSize];
            if (potentialWithInterventions.getVariables().isEmpty()) {
                // The interventions are in a constant potential
                strategyTree = inputStrategyTrees[0];
            }
        }

        int indexPotentialWithInterventions = potentials.indexOf(potentialWithInterventions);

        List<String> utilityVariablesNames = utilityVariables.stream()
                                                             .map(Variable::getName)
                                                             .toList();

        // utilityPotential.

        for (int resultPosition = 0; resultPosition < tableSize; resultPosition++) {
            if (thereAreVariables) {
                incrementedVariable = AuxiliaryOperations.findNextConfigurationAndIndexIncreasedVariable(resultDimension, resultCoordinate, incrementedVariable);
            }

            Map<Variable, String> assignment = new HashMap<>();
            // multiply
            for (int iPotential = 0; iPotential < numPotentials; iPotential++) {
                int potentialsPositionIPotential = potentialsPositions[iPotential];
                String varNameInExpressionToEvaluate = "U" + (iPotential + 1);
                // String varNameInExpressionToEvaluate = utilityVariablesNames.get(iPotential);
                assignment.put(
                        utilityPotential.getVariables()
                                        .stream()
                                        .filter(variable -> variable.getName().equals(varNameInExpressionToEvaluate))
                                        .findFirst()
                                        .get()
                        , "" + tables[iPotential][potentialsPositionIPotential]);
                // Obtain the intervention
                if (thereAreInterventions && indexPotentialWithInterventions == iPotential) {
                    strategyTree = inputStrategyTrees[potentialsPositionIPotential];
                }
                // update the current position in each potential table
                if (thereAreVariables) {
                    potentialsPositions[iPotential] += offsetAccumulate[iPotential][incrementedVariable];
                }
            }
            resultValues[resultPosition] = Double.parseDouble(utilityPotential.getCovariates()[0].evaluateWith(assignment));
            if (thereAreInterventions) {
                resultStrategyTrees[resultPosition] = strategyTree;
            }

        }

        TablePotential resultPotential = buildResultPotential(criterion, role, resultVariables, resultValues,
                                                              thereAreInterventions, resultStrategyTrees);
        return resultPotential;
    }

    private static double[][] initializeFromValues(List<TablePotential> potentials) {
        int numPotentials = potentials.size();
        double[][] tables = new double[numPotentials][];
        for (int i = 0; i < numPotentials; i++) {
            tables[i] = potentials.get(i).getValues();
        }
        return tables;
    }

    private static TablePotential buildConstantPotential(double constantFactor, PotentialRole role) {
        TablePotential constantTablePotential = new TablePotential(null, role);
        constantTablePotential.getValues()[0] = constantFactor;
        return constantTablePotential;
    }

    private static TablePotential buildResultPotential(Criterion criterion, PotentialRole role,
                                                       List<Variable> resultVariables, double[] resultValues, boolean thereAreInterventions,
                                                       StrategyTree[] resultStrategyTrees) {
        if (thereAreInterventions) {
            StrategicTablePotential resultPotential = new StrategicTablePotential(resultVariables, role, resultValues);
            if (criterion != null) resultPotential.setCriterion(criterion);
            resultPotential.strategyTrees = resultStrategyTrees;
            return resultPotential;
        } else {
            TablePotential resultPotential = new TablePotential(resultVariables, role, resultValues);
            if (criterion != null) resultPotential.setCriterion(criterion);
            return resultPotential;
        }
    }
}
