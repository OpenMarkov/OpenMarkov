/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.TemporalOptions.TransitionTime;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * With the transitions at half cycle, the utilities of each cycle are averaged with those of the previous one whatever
 * their relation: a tree in MID-Chancellor, an "Exact" distribution in format 1.0 (error 233). In a network without a
 * horizon, the cycles are those the network has (error 356).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class TheHalfCycleTransitionEvaluatesEveryUtilityTest {

    private static double evaluate(String path, TransitionTime transition) throws Exception {
        ProbNet net = new PGMXReader().read(Networks.getNetworks().filter(u -> u.getPath().endsWith(path))
                                                    .findFirst().orElseThrow()).probNet();
        net.getInferenceOptions().getTemporalOptions().setTransition(transition);
        return new VEEvaluation(net).getUtility().getValues()[0];
    }

    @Test void aUtilityGivenByATreeIsAveraged() throws Exception {
        double beginning = evaluate("/mid/MID-Chancellor.pgmx", TransitionTime.BEGINNING);
        double half = evaluate("/mid/MID-Chancellor.pgmx", TransitionTime.HALF);
        double end = evaluate("/mid/MID-Chancellor.pgmx", TransitionTime.END);

        assertTrue(beginning < half && half < end, beginning + " < " + half + " < " + end);
    }

    @Test void anExactDistributionOfFormat1IsAveragedAsItsTable() throws Exception {
        assertEquals(evaluate("/mid/MID-hip-Briggs.pgmx", TransitionTime.HALF),
                     evaluate("/mid/1-0/MID-hip-Briggs-1-0.pgmx", TransitionTime.HALF), 1E-9);
    }

    @Test void aNetworkWithoutAHorizonTakesItsOwnCycles() throws Exception {
        evaluate("/mid/DMHEE/MID-dmhee-3.5.pgmx", TransitionTime.HALF);

        assertNotEquals(evaluate("/mid/DMHEE/MID-dmhee-3.5.pgmx", TransitionTime.BEGINNING),
                        evaluate("/mid/DMHEE/MID-dmhee-3.5.pgmx", TransitionTime.END), 1E-6);
    }
}
