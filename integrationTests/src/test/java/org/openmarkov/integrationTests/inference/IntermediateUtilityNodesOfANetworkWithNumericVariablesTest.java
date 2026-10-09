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
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The Markov influence diagram of the human papillomavirus. Its utility nodes «AR QoL [0]» and «HSR QoL [0]» are
 * added by «QoL [0]», and the age of each cycle is a numeric variable whose value is known.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class IntermediateUtilityNodesOfANetworkWithNumericVariablesTest {

    @Test void propagationGivesAValueToEveryNode() throws Exception {
        ProbNet probNet = new PGMXReader().read(
                Networks.getNetworks().filter(url -> url.getPath().endsWith("/mid/MID-HPV.pgmx"))
                        .findFirst().orElseThrow()).probNet();
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(probNet.getVariables());
        propagation.setPreResolutionEvidence(new EvidenceCase());
        propagation.setPostResolutionEvidence(new EvidenceCase());

        Map<Variable, TablePotential> values = propagation.getPosteriorValues();

        assertNotNull(values.get(probNet.getVariable("AR QoL [0]")));
        assertNotNull(values.get(probNet.getVariable("HSR QoL [0]")));
        assertEquals(probNet.getVariables().size(), values.size());
    }
}
