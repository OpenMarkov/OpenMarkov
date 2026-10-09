/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import networks.Networks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Markov influence diagram MID-CHAP-Ryan-Griffin. In each cycle the age at which the patient entered the state
 * is a linear combination of the age and the time in the state, and two costs are given by a tree with an
 * exponential model in one of its branches.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class ANetworkWithARegressionInItsCyclesIsEvaluatedTest {

    private ProbNet probNet;

    @BeforeEach void read() throws Exception {
        probNet = new PGMXReader().read(
                Networks.getNetworks().filter(url -> url.getPath().endsWith("/mid/MID-CHAP-Ryan-Griffin.pgmx"))
                        .findFirst().orElseThrow()).probNet();
    }

    @Test void itsCostEffectivenessAnalysisGivesAResult() throws Exception {
        VECEAnalysis analysis = new VECEAnalysis(probNet);
        analysis.setPreResolutionEvidence(new EvidenceCase());

        CEP result = (CEP) analysis.getUtility().elementTable.getFirst();

        assertTrue(result.getNumIntervals() >= 1);
        assertTrue(Arrays.stream(result.getCosts()).allMatch(Double::isFinite));
        assertTrue(Arrays.stream(result.getEffectivities()).allMatch(Double::isFinite));
    }

    @Test void propagationGivesTheProbabilitiesOfTheSecondCycle() throws Exception {
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(probNet.getVariables());
        propagation.setPreResolutionEvidence(new EvidenceCase());
        propagation.setPostResolutionEvidence(new EvidenceCase());

        Map<Variable, TablePotential> values = propagation.getPosteriorValues();

        double[] stateInTheSecondCycle = values.entrySet().stream()
                                               .filter(entry -> entry.getKey().getName().equals("State [1]"))
                                               .findFirst().orElseThrow().getValue().getValues();
        assertEquals(1.0, Arrays.stream(stateInTheSecondCycle).sum(), 1E-9);
    }
}
