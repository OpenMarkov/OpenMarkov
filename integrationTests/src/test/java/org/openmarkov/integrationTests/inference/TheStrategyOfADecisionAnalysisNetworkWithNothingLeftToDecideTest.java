/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * DAN-one-decision has a single decision, D. «Show optimal strategy» asks its evaluation for the strategy.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheStrategyOfADecisionAnalysisNetworkWithNothingLeftToDecideTest {

    private static StrategyTree strategyKnowing(String state) throws Exception {
        ProbNet probNet = new PGMXReader().read(
                IntegrationTest.class.getResource("/networks/dan/DAN-one-decision.pgmx")).probNet();
        EvidenceCase known = new EvidenceCase();
        if (state != null) {
            Variable decision = probNet.getVariable("D");
            known.addFinding(new Finding(decision, decision.getStateIndex(state)));
        }
        return new DANDecompositionIntoSymmetricDANsEvaluation(probNet, known).getOptimalStrategyTree();
    }

    @Test void withNothingKnownThereIsAStrategy() throws Exception {
        assertNotNull(strategyKnowing(null));
    }

    @Test void withTheDecisionKnownThereIsNoStrategyAndNoError() throws Exception {
        assertNull(strategyKnowing("yes"));
        assertNull(strategyKnowing("no"));
    }
}
