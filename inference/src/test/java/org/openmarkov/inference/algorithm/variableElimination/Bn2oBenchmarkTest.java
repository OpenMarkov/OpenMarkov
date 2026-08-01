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

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Memory and time of the two projection paths on noisy-OR networks of growing fan-in. The memory
 * figures are exact table sizes and are asserted; the times are single measurements, printed for
 * the record, not asserted. Every answer is checked against the closed form of the marginal.
 *
 * @author Manuel Arias
 */
public class Bn2oBenchmarkTest {

    private static final int[] PARENT_COUNTS = {5, 10, 15, 20};
    private static final double TOLERANCE = 1E-9;

    @Tag(TestSpeed.MEDIUM)
    @Test public void theFactorsGrowLinearlyWhereTheTableGrowsExponentially() throws Exception {
        System.out.println("  n | path        | largest table | total values | time (ms)");
        for (int numParents : PARENT_COUNTS) {
            for (boolean keepingTheFactorization : new boolean[]{false, true}) {
                run(numParents, keepingTheFactorization);
            }
        }
        // Where the expanded table (2^31 values) no longer fits in memory, the factors still do.
        run(30, true);
    }

    private void run(int numParents, boolean keepingTheFactorization) throws Exception {
        ProbNet network = Bn2oNetworks.bn2o(numParents);
        network.getInferenceOptions().setIciAwareVE(keepingTheFactorization);

        ProbNet markovNetwork = TaskUtilities.projectTablesAndBuildMarkovDecisionNetwork(network, new EvidenceCase());
        int largest = 0, total = 0;
        for (Potential potential : markovNetwork.getPotentials()) {
            int size = ((TablePotential) potential).getValues().length;
            largest = Math.max(largest, size);
            total += size;
        }

        long start = System.nanoTime();
        VEPropagation propagation = new VEPropagation(network);
        propagation.setVariablesOfInterest(List.of(network.getVariable("Y")));
        HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();
        double milliseconds = (System.nanoTime() - start) / 1e6;

        double pNegative = Bn2oNetworks.closedFormOfTheNegativeMarginal(numParents);
        assertArrayEquals(new double[]{pNegative, 1 - pNegative},
                     posteriors.get(network.getVariable("Y")).getValues(), TOLERANCE,
                     numParents + " parents, factorization " + keepingTheFactorization);
        if (keepingTheFactorization) {
            assertEquals(4, largest, "the largest factor of a binary noisy-OR");
            assertEquals(4 * numParents + 6 + 2 * numParents, total,
                         "factors of the model plus the priors of the parents");
        } else {
            assertEquals(2 << numParents, largest, "the expanded table");
        }

        System.out.printf("%3d | %-11s | %13d | %12d | %9.1f%n", numParents,
                          keepingTheFactorization ? "factorized" : "expanded", largest, total, milliseconds);
    }
}
