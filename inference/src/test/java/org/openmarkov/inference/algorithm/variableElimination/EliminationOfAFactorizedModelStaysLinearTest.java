/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.tasks.TaskUtilities;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.inference.heuristic.rollout.RolloutElimination;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The point of keeping the factorization: on a noisy-OR with many parents, no step of the
 * elimination may rebuild the exponential table the factors avoid (Rish–Dechter: that happens
 * when the child or its pseudo variable is eliminated before the parents). The default heuristic
 * is watched by a spy that records the largest table alive in the Markov network at each step.
 *
 * @author Manuel Arias
 */
public class EliminationOfAFactorizedModelStaysLinearTest {

    private static final int PARENTS = 12;
    private static final double TOLERANCE = 1E-9;

    /** Watches the network the elimination runs on, remembering the largest table it ever holds. */
    private static final class LargestFactorSpy extends RolloutElimination {

        private final ProbNet markovNetwork;
        private int largestSeen;

        private LargestFactorSpy(ProbNet markovNetwork, List<List<Variable>> variablesToEliminate) {
            super(markovNetwork, variablesToEliminate);
            this.markovNetwork = markovNetwork;
        }

        @Override public Variable getVariableToDelete() {
            sweep();
            return super.getVariableToDelete();
        }

        private void sweep() {
            for (Potential potential : markovNetwork.getPotentials()) {
                if (potential instanceof TablePotential table) {
                    largestSeen = Math.max(largestSeen, table.getValues().length);
                }
            }
        }
    }

    @Tag(TestSpeed.FAST)
    @Test public void noStepOfTheEliminationRebuildsTheExponentialTable() throws Exception {
        ProbNet network = Bn2oNetworks.bn2o(PARENTS);
        network.getInferenceOptions().setIciAwareVE(true);

        ProbNet markovNetwork = TaskUtilities.projectTablesAndBuildMarkovDecisionNetwork(network, new EvidenceCase());
        List<Variable> variablesToEliminate = markovNetwork.getChanceAndDecisionVariables();
        variablesToEliminate.remove(markovNetwork.getVariable("Y"));
        LargestFactorSpy spy = new LargestFactorSpy(markovNetwork, List.of(variablesToEliminate));

        new VariableEliminationCore(markovNetwork, spy, true);
        spy.sweep();

        assertTrue(spy.largestSeen <= 16,
                   "a step of the elimination built a table of " + spy.largestSeen + " values, where the"
                           + " factors of " + PARENTS + " parents never need more than 16; the expanded"
                           + " table would hold " + (2 << PARENTS));
    }

    @Tag(TestSpeed.FAST)
    @Test public void theExpandedPathMaterializesTheExponentialTable() throws Exception {
        ProbNet network = Bn2oNetworks.bn2o(PARENTS);

        ProbNet markovNetwork = TaskUtilities.projectTablesAndBuildMarkovDecisionNetwork(network, new EvidenceCase());

        int largest = 0;
        for (Potential potential : markovNetwork.getPotentials()) {
            largest = Math.max(largest, ((TablePotential) potential).getValues().length);
        }
        assertTrue(largest >= (2 << PARENTS),
                   "without the switch the projection was expected to pay the full table, but its"
                           + " largest potential holds " + largest + " values");
    }

    /** At this scale the enumeration is out of reach, but the noisy-OR marginal has a closed form. */
    @Tag(TestSpeed.FAST)
    @Test public void theMarginalAtThisScaleMatchesTheClosedForm() throws Exception {
        ProbNet network = Bn2oNetworks.bn2o(PARENTS);
        network.getInferenceOptions().setIciAwareVE(true);

        VEPropagation propagation = new VEPropagation(network);
        propagation.setVariablesOfInterest(List.of(network.getVariable("Y")));
        HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

        double pAllInhibited = Bn2oNetworks.closedFormOfTheNegativeMarginal(PARENTS);
        assertArrayEquals(new double[]{pAllInhibited, 1 - pAllInhibited},
                          posteriors.get(network.getVariable("Y")).getValues(), TOLERANCE);
    }
}
