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
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.StrategicTablePotential;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Single-potential transformation operations: normalization, projection,
 * and distribution fixup.
 *
 * @author Manuel Arias
 */
final class TablePotentialTransform {

    private TablePotentialTransform() {
    }

    /**
     * Normalizes {@code potential} in place.
     *
     * @param potential the potential to normalize
     * @return the same potential, normalized
     * @throws CannotNormalizePotentialException if all values are zero
     */
    static TablePotential normalize(TablePotential potential) throws CannotNormalizePotentialException {
        TablePotential tablePotential = potential;
        if (Arrays.stream(tablePotential.getValues()).allMatch(value -> value == 0.0)) {
            throw new CannotNormalizePotentialException(tablePotential);
        }
        List<Variable> variables = tablePotential.getVariables();
        if ((variables != null) && (!variables.isEmpty())) {
            if (potential.getPotentialRole() == PotentialRole.CONDITIONAL_PROBABILITY) {
                int numStates = variables.get(0).getNumStates();
                double normalizationFactor;
                for (int i = 0; i < tablePotential.getValues().length; i += numStates) {
                    normalizationFactor = 0.0;
                    for (int j = 0; j < numStates; j++) {
                        normalizationFactor += tablePotential.getValues()[i + j];
                    }
                    for (int j = 0; j < numStates; j++) {
                        tablePotential.getValues()[i + j] /= normalizationFactor;
                    }
                }
            } else if (potential.getPotentialRole() == PotentialRole.JOINT_PROBABILITY) {
                double normalizationFactor = 0.0;
                for (int i = 0; i < tablePotential.getValues().length; i++) {
                    normalizationFactor += tablePotential.getValues()[i];
                }
                for (int i = 0; i < tablePotential.getValues().length; i++) {
                    tablePotential.getValues()[i] /= normalizationFactor;
                }
            }
        }
        if (potential instanceof StrategicTablePotential stp && stp.strategyTrees != null && stp.strategyTrees.length > 0) {
            stp.strategyTrees = stp.strategyTrees.clone();
        }
        return tablePotential;
    }

    /**
     * Removes {@code variable} from {@code inputPotential} by projecting onto
     * the first state of that variable.
     *
     * @param variable       the decision variable to project out
     * @param inputPotential the potential from which to project
     * @return the projected potential
     */
    static TablePotential projectOutVariable(Variable variable, TablePotential inputPotential)
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        EvidenceCase evi = new EvidenceCase();
        evi.addFinding(new Finding(variable, variable.getStates()[0]));
        return inputPotential.tableProject(evi, null);
    }

    /**
     * For each configuration of the conditioning variables where all values are
     * zero, replaces them with a uniform distribution over the first state.
     *
     * @param xNewPotential potential to fix
     * @return the same potential, fixed in place
     */
    static TablePotential imposeOtherDistributionWhenDistributionIsZero(TablePotential xNewPotential) {
        List<Variable> variables = xNewPotential.getVariables();
        if (variables == null || variables.isEmpty() || xNewPotential.getValues() == null
                || xNewPotential.getValues().length <= 1) {
            return xNewPotential;
        }
        Variable firstVariable = variables.get(0);
        int numStatesFirstVariable = firstVariable.getNumStates();
        int numOuterIterations = xNewPotential.getValues().length / numStatesFirstVariable;
        int numConfiguration = 0;
        for (int i = 0; i < numOuterIterations; i++) {
            boolean allZeros = true;
            int startConfiguration = numConfiguration;
            for (int j = 0; j < numStatesFirstVariable; j++) {
                allZeros &= DiscretePotentialOperations.almostEqual(0.0, xNewPotential.getValues()[startConfiguration++]);
            }
            if (allZeros) {
                startConfiguration = numConfiguration;
                xNewPotential.getValues()[startConfiguration++] = 1.0;
                for (int j = 1; j < numStatesFirstVariable; j++) {
                    xNewPotential.getValues()[startConfiguration++] = 0.0;
                }
            }
            numConfiguration += numStatesFirstVariable;
        }
        return xNewPotential;
    }

    /**
     * Combines a mixed list of probability and utility potentials into a single
     * potential: product of probabilities multiplied by sum of utilities.
     *
     * @param potentials list containing both probability and utility potentials
     * @return combined potential
     */
    static TablePotential matrixPotential(List<Potential> potentials) {
        List<TablePotential> probs = new ArrayList<>();
        List<TablePotential> utils = new ArrayList<>();
        for (Potential potential : potentials) {
            if (potential.isAdditive()) {
                utils.add((TablePotential) potential);
            } else {
                probs.add((TablePotential) potential);
            }
        }
        return DiscretePotentialOperations.multiply(
                !probs.isEmpty() ? DiscretePotentialOperations.multiply(probs)
                        : DiscretePotentialOperations.createUnityProbabilityPotential(),
                DiscretePotentialOperations.sum(utils));
    }
}
