/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.costeffectiveness;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEPSA;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Ten simulations of the probabilistic cost-effectiveness analysis of a network whose parameters have uncertainty.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheProbabilisticAnalysisCanBeRepeatedTest {

    /** The cost and the effectiveness of each option in each simulation. */
    private static List<Double> analysisWithSeed(long seed) throws Exception {
        ProbNet probNet = new PGMXReader().read(
                Networks.getNetworks().filter(url -> url.getPath().endsWith("/id/ID-CEA-test-2therapies.pgmx"))
                        .findFirst().orElseThrow()).probNet();
        VECEPSA analysis = new VECEPSA(probNet);
        analysis.setDecisionVariable(probNet.getNodes(NodeType.DECISION).getFirst().getVariable());
        analysis.setPreResolutionEvidence(new EvidenceCase());
        analysis.setNumSimulations(10);
        analysis.setUseMultithreading(false);
        analysis.setSeed(seed);
        List<Double> numbers = new ArrayList<>();
        for (GTablePotential<?> simulation : analysis.getCEPPotentials()) {
            for (Object option : simulation.elementTable) {
                numbers.add(((CEP) option).getCost(0));
                numbers.add(((CEP) option).getEffectiveness(0));
            }
        }
        return numbers;
    }

    @Test void theSameSeedGivesTheSameNumbers() throws Exception {
        assertEquals(analysisWithSeed(2026), analysisWithSeed(2026));
    }

    @Test void anotherSeedGivesOtherNumbers() throws Exception {
        assertNotEquals(analysisWithSeed(2026), analysisWithSeed(2027));
    }
}
