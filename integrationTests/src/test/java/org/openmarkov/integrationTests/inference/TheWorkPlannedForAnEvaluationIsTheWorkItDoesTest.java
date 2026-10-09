/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * An evaluation whose progress is followed works out beforehand the work of its eliminations, from the variables
 * of the tables alone, and measures its progress as the part of that work it has done.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class TheWorkPlannedForAnEvaluationIsTheWorkItDoesTest {

    @ParameterizedTest
    @ValueSource(strings = { "/mid/MID-Chancellor.pgmx", "/mid/MID-hip-Briggs.pgmx", "/id/ID-arthronet-ce.pgmx",
            "/id/ID-decide-test.pgmx", "/dan/DAN-reactor.pgmx" })
    void atTheEndAllThePlannedWorkIsDone(String network) throws Exception {
        ProbNet probNet = new PGMXReader().read(
                Networks.getNetworks().filter(url -> url.getPath().endsWith(network)).findFirst().orElseThrow())
                                          .probNet();
        VEEvaluation evaluation = new VEEvaluation(probNet);
        evaluation.getProgress().follow();
        evaluation.setPreResolutionEvidence(new EvidenceCase());

        evaluation.getOptimalPolicies();

        assertEquals(1.0, evaluation.getProgress().getFraction(), 1E-9);
    }
}
