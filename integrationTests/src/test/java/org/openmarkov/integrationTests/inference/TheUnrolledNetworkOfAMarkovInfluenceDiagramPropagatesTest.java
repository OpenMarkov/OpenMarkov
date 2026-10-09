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
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.TemporalNetOperations;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The influence diagram that «Expand network» gives for the Markov influence diagram of Chancellor, which has a
 * numeric variable, «Time in treatment», that a tree of utilities depends on.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheUnrolledNetworkOfAMarkovInfluenceDiagramPropagatesTest {

    @Test void propagationGivesAValueToEveryNode() throws Exception {
        ProbNet markovInfluenceDiagram = new PGMXReader().read(
                Networks.getNetworks().filter(url -> url.getPath().endsWith("/mid/MID-Chancellor.pgmx"))
                        .findFirst().orElseThrow()).probNet();
        EvidenceCase evidence = new EvidenceCase();
        ProbNet unrolled = TemporalNetOperations.expandNetwork(markovInfluenceDiagram, evidence,
                                                               "MID-Chancellor_extended.pgmx");
        VEPropagation propagation = new VEPropagation(unrolled);
        propagation.setVariablesOfInterest(unrolled.getVariables());
        propagation.setPreResolutionEvidence(evidence);
        propagation.setPostResolutionEvidence(new EvidenceCase());

        assertEquals(unrolled.getVariables().size(), propagation.getPosteriorValues().size());
    }
}
