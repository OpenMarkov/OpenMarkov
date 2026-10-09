/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import networks.Networks;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Tag;
import org.openmarkov.core.inference.TemporalOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The propagation of the Markov influence diagram of Chancellor. In cycle 0 every patient is alive, in state A,
 * and the optimal therapy is the combination one.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheUtilityOfACycleDoesNotDependOnTheMomentOfTheTransitionsTest {

    @ParameterizedTest @EnumSource(value = TemporalOptions.TransitionTime.class, names = { "BEGINNING", "END" })
    void theUtilityNodesOfCycle0HaveTheValueOfThatCycle(TemporalOptions.TransitionTime moment) throws Exception {
        ProbNet probNet = new PGMXReader().read(
                Networks.getNetworks().filter(url -> url.getPath().endsWith("/mid/MID-Chancellor.pgmx"))
                        .findFirst().orElseThrow()).probNet();
        probNet.getInferenceOptions().getTemporalOptions().setTransition(moment);
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(probNet.getVariables());
        propagation.setPreResolutionEvidence(new EvidenceCase());
        propagation.setPostResolutionEvidence(new EvidenceCase());

        Map<Variable, TablePotential> values = propagation.getPosteriorValues();

        assertEquals(1.0, values.get(probNet.getVariable("Life years [0]")).getValues()[0], 1E-9);
        assertEquals(1701.0, values.get(probNet.getVariable("Direct medical cost [0]")).getValues()[0], 1E-6);
        assertEquals(2085.50445, values.get(probNet.getVariable("Cost lamivudine [0]")).getValues()[0], 1E-5);
    }
}
