/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * «Show optimal strategy» is offered for a network that has at least one decision.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheOptimalStrategyIsOfferedOnlyWithADecisionTest {

    @Test
    void aDecisionNetworkWithoutDecisionsDoesNotOfferIt() {
        ProbNet influenceDiagram = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        ProbNet decisionAnalysisNetwork = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
        influenceDiagram.addNode(new Variable("Disease", 2), NodeType.CHANCE);
        influenceDiagram.addNode(new Variable("U"), NodeType.UTILITY);

        assertFalse(MainPanelMenuAssistant.canShowOptimalStrategy(influenceDiagram));
        assertFalse(MainPanelMenuAssistant.canShowOptimalStrategy(decisionAnalysisNetwork));
    }

    @Test
    void aNetworkWithADecisionOffersIt() {
        ProbNet influenceDiagram = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        influenceDiagram.addNode(new Variable("D", 2), NodeType.DECISION);

        assertTrue(MainPanelMenuAssistant.canShowOptimalStrategy(influenceDiagram));
    }

    @Test
    void aBayesianNetworkDoesNotOfferIt() {
        ProbNet bayesianNetwork = new ProbNet(BayesianNetworkType.getUniqueInstance());
        bayesianNetwork.addNode(new Variable("A", 2), NodeType.CHANCE);

        assertFalse(MainPanelMenuAssistant.canShowOptimalStrategy(bayesianNetwork));
    }
}
