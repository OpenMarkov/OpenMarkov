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

/**
 * With the transitions at half cycle, the first and the last cycle count half, for every criterion, so the result is
 * the mean of "Beginning" and "End" whatever the relation of the utilities: a tree, an "Exact" distribution in format
 * 1.0 (error 233), a tree on a numeric age below a product (error 358). In a network without a horizon, the cycles are
 * those the network has (error 356).
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

    private static void assertHalfIsTheMeanOfBeginningAndEnd(String path) throws Exception {
        double beginning = evaluate(path, TransitionTime.BEGINNING);
        double end = evaluate(path, TransitionTime.END);

        assertEquals((beginning + end) / 2, evaluate(path, TransitionTime.HALF), 1E-6 * Math.abs(beginning));
    }

    @Test void aUtilityGivenByATreeCountsHalfInTheFirstAndLastCycle() throws Exception {
        assertHalfIsTheMeanOfBeginningAndEnd("/mid/MID-Chancellor.pgmx");
    }

    @Test void theFirstCycleCountsHalfToo() throws Exception {
        assertHalfIsTheMeanOfBeginningAndEnd("/mid/MID-hip-Briggs.pgmx");
    }

    @Test void anExactDistributionOfFormat1CountsAsItsTable() throws Exception {
        assertEquals(evaluate("/mid/MID-hip-Briggs.pgmx", TransitionTime.HALF),
                     evaluate("/mid/1-0/MID-hip-Briggs-1-0.pgmx", TransitionTime.HALF), 1E-9);
    }

    @Test void aTreeOnANumericAgeBelowAProductCountsOnce() throws Exception {
        assertHalfIsTheMeanOfBeginningAndEnd("/mid/MID-HPV.pgmx");
    }

    @Test void aNetworkWithoutAHorizonTakesItsOwnCycles() throws Exception {
        assertHalfIsTheMeanOfBeginningAndEnd("/mid/DMHEE/MID-dmhee-3.5.pgmx");

        assertNotEquals(evaluate("/mid/DMHEE/MID-dmhee-3.5.pgmx", TransitionTime.BEGINNING),
                        evaluate("/mid/DMHEE/MID-dmhee-3.5.pgmx", TransitionTime.END), 1E-6);
    }
}
