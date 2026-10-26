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
import org.openmarkov.core.inference.TemporalOptions.TransitionTime;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.TemporalNetOperations;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The network that «Expand Network» opens gives the evaluation and the cost-effectiveness analysis of the Markov model,
 * whatever the time of the transition (error 320). MID-hip-Briggs has a Weibull hazard without a time variable and
 * MID-CHAP-Ryan-Griffin one with a time variable: each cycle keeps its own probability.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class TheExpandedNetworkComputesAsTheModelTest {

    private static ProbNet read(String path, TransitionTime transition) throws Exception {
        ProbNet net = new PGMXReader().read(Networks.getNetworks().filter(u -> u.getPath().endsWith(path))
                                                    .findFirst().orElseThrow()).probNet();
        net.getInferenceOptions().getTemporalOptions().setTransition(transition);
        return net;
    }

    private static TemporalNetOperations.ExpandedNetwork expanded(String path, TransitionTime transition)
            throws Exception {
        return TemporalNetOperations.expandNetwork(read(path, transition), new EvidenceCase(), "expanded.pgmx");
    }

    private static double evaluationOfTheExpanded(String path, TransitionTime transition) throws Exception {
        TemporalNetOperations.ExpandedNetwork expanded = expanded(path, transition);
        VEEvaluation evaluation = new VEEvaluation(expanded.probNet());
        evaluation.setPreResolutionEvidence(expanded.evidence());
        return evaluation.getUtility().getValues()[0];
    }

    private static CEP analysisOfTheExpanded(String path, TransitionTime transition) throws Exception {
        TemporalNetOperations.ExpandedNetwork expanded = expanded(path, transition);
        VECEAnalysis analysis = new VECEAnalysis(expanded.probNet());
        analysis.setPreResolutionEvidence(expanded.evidence());
        return analysis.getCEP();
    }

    @ParameterizedTest
    @ValueSource(strings = { "/mid/MID-Chancellor.pgmx", "/mid/MID-hip-Briggs.pgmx", "/mid/MID-CHAP-Ryan-Griffin.pgmx" })
    void theExpandedNetworkGivesTheEvaluationAndTheAnalysisOfTheModel(String path) throws Exception {
        for (TransitionTime transition : TransitionTime.values()) {
            double model = new VEEvaluation(read(path, transition)).getUtility().getValues()[0];
            assertEquals(model, evaluationOfTheExpanded(path, transition), 1E-9 * Math.abs(model),
                         path + " with " + transition);

            CEP modelCEP = new VECEAnalysis(read(path, transition)).getCEP();
            CEP expandedCEP = analysisOfTheExpanded(path, transition);
            assertEquals(modelCEP.getNumIntervals(), expandedCEP.getNumIntervals(), path + " with " + transition);
            for (int i = 0; i < modelCEP.getNumIntervals(); i++) {
                int interval = i;
                assertAll(() -> assertEquals(modelCEP.getCost(interval), expandedCEP.getCost(interval),
                                             1E-9 * Math.abs(modelCEP.getCost(interval)), path + " with " + transition),
                          () -> assertEquals(modelCEP.getEffectiveness(interval),
                                             expandedCEP.getEffectiveness(interval),
                                             1E-9 * Math.abs(modelCEP.getEffectiveness(interval)),
                                             path + " with " + transition));
            }
        }
    }
}
