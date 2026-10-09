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
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The cost-effectiveness analysis of two Markov influence diagrams whose initial age has a uniform distribution.
 * In MID-CHD-Walker, besides, no utility node has numbers.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANetworkThatLacksWhatItNeedsToBeEvaluatedSaysWhatTest {

    private static VECEAnalysis analysis(String network, Double age) throws Exception {
        ProbNet probNet = new PGMXReader().read(
                Networks.getNetworks().filter(url -> url.getPath().endsWith(network)).findFirst().orElseThrow())
                                          .probNet();
        EvidenceCase evidence = new EvidenceCase();
        if (age != null) {
            evidence.addFinding(new Finding(probNet.getVariable("Age [0]"), age));
        }
        VECEAnalysis analysis = new VECEAnalysis(probNet);
        analysis.setPreResolutionEvidence(evidence);
        return analysis;
    }

    @Test void withoutTheAgeItAsksForAFindingOnIt() throws Exception {
        VECEAnalysis analysis = analysis("/mid/MID-mammography.pgmx", null);

        NonProjectablePotentialException.NumericVariableNeedsAFinding asked = assertThrows(
                NonProjectablePotentialException.NumericVariableNeedsAFinding.class, analysis::getUtility);

        assertEquals("Age [0]", asked.variable.getName());
        assertEquals("The numeric variable Age [0] has a uniform distribution, so it has no value to compute with. "
                             + "Set a finding on it in edition mode and try again.", asked.getExceptionMessage());
    }

    @Test void withAFindingOnTheAgeItIsEvaluated() throws Exception {
        CEP result = (CEP) analysis("/mid/MID-mammography.pgmx", 50.0).getUtility().elementTable.getFirst();

        assertTrue(result.getNumIntervals() >= 1);
    }

    @Test void withoutNumbersInAUtilityItSaysWhichOne() throws Exception {
        VECEAnalysis analysis = analysis("/mid/MID-CHD-Walker.pgmx", 50.0);

        NonProjectablePotentialException.UtilityIsNotDefined notDefined = assertThrows(
                NonProjectablePotentialException.UtilityIsNotDefined.class, analysis::getUtility);

        assertEquals("The utility of the node " + notDefined.utilityVariable.getName()
                             + " is not defined: its relation is still the uniform one it was created with.",
                     notDefined.getExceptionMessage());
    }
}
