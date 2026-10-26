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
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * In MID-dmhee-2.5, «Duration of treatment [0]» is a delta of value 0. A finding of 2 on it is refused, with a message
 * that says the relation fixes the value, both before and after the resolution; a finding of 0 is accepted (error 194).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class AFindingAgainstAFixedValueIsRefusedTest {

    private final ProbNet net = read();
    private final Variable duration = net.getVariable("Duration of treatment [0]");

    private static ProbNet read() {
        try {
            return new PGMXReader().read(Networks.getNetworks().filter(u -> u.getPath().endsWith("/mid/DMHEE/MID-dmhee-2.5.pgmx"))
                                                 .findFirst().orElseThrow()).probNet();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private EvidenceCase finding(double value) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(duration, value));
        return evidence;
    }

    private void propagate(EvidenceCase preResolution, EvidenceCase postResolution) throws Exception {
        VEPropagation propagation = new VEPropagation(net);
        propagation.setVariablesOfInterest(List.of(net.getVariable("State [1]")));
        propagation.setPreResolutionEvidence(preResolution);
        propagation.setPostResolutionEvidence(postResolution);
        propagation.getPosteriorValues();
    }

    @Test void beforeTheResolution() throws Exception {
        EvidenceCase two = finding(2);
        IncompatibleEvidenceException.FindingIsNotTheFixedValue e = assertThrows(
                IncompatibleEvidenceException.FindingIsNotTheFixedValue.class, () -> propagate(two, new EvidenceCase()));

        assertAll(() -> assertEquals(duration, e.variable),
                  () -> assertEquals("2", e.observed),
                  () -> assertEquals("0", e.fixed),
                  () -> assertEquals("Duration of treatment [0] cannot be observed as 2: its relation fixes its value at 0. "
                                     + "To study another value, change the relation of the node.", e.localize()));
    }

    @Test void afterTheResolution() throws Exception {
        EvidenceCase two = finding(2);
        assertThrows(IncompatibleEvidenceException.FindingIsNotTheFixedValue.class,
                     () -> propagate(new EvidenceCase(), two));
    }

    @Test void theFixedValueIsAccepted() throws Exception {
        EvidenceCase zero = finding(0);
        assertAll(() -> assertDoesNotThrow(() -> propagate(zero, new EvidenceCase())),
                  () -> assertDoesNotThrow(() -> propagate(new EvidenceCase(), zero)));
    }
}
