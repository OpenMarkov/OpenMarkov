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
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.TemporalNetOperations;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * «Expand Network» on the Markov model of Walker, whose age at the start is uniform, with the age of cycle 0 observed
 * in the model: the network expands, and the finding is on the age of the expanded network.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ExpandNetworkKeepsTheFindingsOfTheModelTest {

    @Test void theAgeObservedInTheModelIsObservedInTheExpandedNetwork() throws Exception {
        ProbNet model = new PGMXReader().read(
                Networks.getNetworks().filter(url -> url.getPath().endsWith("/mid/MID-CHD-Walker.pgmx"))
                        .findFirst().orElseThrow()).probNet();
        model.getInferenceOptions().getTemporalOptions().setHorizon(2);
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(model.getVariable("Age", 0), 50.0));

        TemporalNetOperations.ExpandedNetwork expanded =
                TemporalNetOperations.expandNetwork(model, evidence, "MID-CHD-Walker_expanded.pgmx");

        assertEquals(1, expanded.evidence().getNumberOfFindings());
        Finding age = expanded.evidence().getFindings().getFirst();
        assertTrue(expanded.probNet().getVariables().contains(age.getVariable()), "the age is of the expanded network");
        assertEquals("Age |0|", age.getVariable().getName());
        assertEquals("50.0", age.getState(), "the age, observed, has the value as its only state");
    }
}
