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
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.costEffectiveness.CEDecisionResults;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The cost-effectiveness analysis of the decision «Dec: Test», whose options are «no» and «yes», in a network
 * where the test is known to be not done. That cannot happen with «yes».
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnOptionThatIsImpossibleWithTheEvidenceTest {

    private Variable decision;

    private CEP[] resultOfEachOption() throws Exception {
        ProbNet probNet = new PGMXReader().read(
                Networks.getNetworks()
                        .filter(url -> url.getPath().endsWith("/id/ID-CEA-test-2therapies-new-test.pgmx"))
                        .findFirst().orElseThrow()).probNet();
        Variable test = probNet.getVariable("Test");
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(test, test.getStateIndex("not done")));
        decision = probNet.getVariable("Dec: Test");
        VECEAnalysis analysis = new VECEAnalysis(probNet);
        analysis.setPreResolutionEvidence(evidence);
        analysis.setDecisionVariable(decision);
        List<?> results = analysis.getUtility().elementTable;
        return results.toArray(new CEP[0]);
    }

    @Test void itsRowOfTheTableSaysSoAndHasNoValues() throws Exception {
        CEP[] results = resultOfEachOption();

        Object[][] rows = CEDecisionResults.analysisRows(decision, results, 0, 3, false, "impossible");

        assertArrayEquals(new Object[]{ "yes (impossible)", "-", "-" }, rows[1]);
        assertEquals("no", rows[0][0]);
        assertEquals(results[0].getCost(0), rows[0][1]);
    }

    @Test void itAddsNoThresholds() throws Exception {
        CEP[] results = resultOfEachOption();

        assertFalse(CEDecisionResults.thresholdsOf(results).isEmpty());
        assertEquals(CEDecisionResults.thresholdsOf(new CEP[]{ results[0] }), CEDecisionResults.thresholdsOf(results));
    }

    @Test void theAnalysisIsRefusedOnlyWhenEveryOptionIsImpossible() throws Exception {
        CEP[] results = resultOfEachOption();

        assertDoesNotThrow(() -> CEDecisionResults.checkSomeOptionIsPossible(results));
        assertThrows(IncompatibleEvidenceException.EvidenceIsImpossible.class,
                     () -> CEDecisionResults.checkSomeOptionIsPossible(new CEP[]{ results[1], results[1] }));
    }
}
