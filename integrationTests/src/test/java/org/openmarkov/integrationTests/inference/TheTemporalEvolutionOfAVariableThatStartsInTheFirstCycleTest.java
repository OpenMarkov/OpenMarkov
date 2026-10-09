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
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.temporalevaluation.tasks.MIDTemporalEvolution;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MID-Chancellor-corrected defines its costs, its life years and two of its chance variables from cycle 1 on, and
 * evaluates 20 cycles. The task is called as the window calls it, with the decision of the network.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class TheTemporalEvolutionOfAVariableThatStartsInTheFirstCycleTest {

    @ParameterizedTest
    @ValueSource(strings = { "Cost AZT [1]", "Cost lamivudine [1]", "Direct medical cost [1]",
            "Community care cost [1]", "Life years [1]", "Therapy applied [1]", "Transition inhibited [1]" })
    void hasAValueInEveryCycleFromTheFirst(String name) throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/mid/MID-Chancellor-corrected.pgmx"))
                          .findFirst()
                          .orElseThrow();
        ProbNet probNet = new PGMXReader().read(url).probNet();
        MIDTemporalEvolution evolution = new MIDTemporalEvolution(probNet, probNet.getVariable(name));
        evolution.setPreResolutionEvidence(new EvidenceCase());
        evolution.setDecisionVariable(probNet.getVariable("Therapy choice"));

        Map<Variable, TablePotential> values = evolution.getTemporalEvolution();

        List<Integer> cycles = values.keySet().stream().map(Variable::getTimeSlice).sorted().toList();
        assertEquals(IntStream.rangeClosed(1, 20).boxed().toList(), cycles);
    }
}
