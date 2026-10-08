/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * «Decision tree» is offered for influence diagrams and decision analysis networks, the networks whose tree
 * can be built.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheDecisionTreeIsOfferedOnlyForNetworksThatHaveOneTest {

    @Test
    void anInfluenceDiagramAndADecisionAnalysisNetworkOfferIt() {
        assertTrue(MainPanelMenuAssistant.canShowDecisionTree(new ProbNet(InfluenceDiagramType.getUniqueInstance())));
        assertTrue(MainPanelMenuAssistant.canShowDecisionTree(new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance())));
    }

    @Test
    void aMarkovInfluenceDiagramDoesNotOfferIt() {
        assertFalse(MainPanelMenuAssistant.canShowDecisionTree(new ProbNet(MIDType.getUniqueInstance())));
    }

    @Test
    void aBayesianNetworkDoesNotOfferIt() {
        assertFalse(MainPanelMenuAssistant.canShowDecisionTree(new ProbNet(BayesianNetworkType.getUniqueInstance())));
    }
}
