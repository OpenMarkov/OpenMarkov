/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InferenceStoppedException;
import org.openmarkov.core.inference.InferenceProgress;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The propagation of an influence diagram with a disease, a therapy and a utility that depends on both.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class APropagationCanBeFollowedAndStoppedTest {

    private VEPropagation propagation;

    @BeforeEach void build() throws Exception {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable disease = new Variable("Disease", "absent", "present");
        Variable therapy = new Variable("Therapy", "no", "yes");
        Variable utility = new Variable("Utility");
        Node diseaseNode = probNet.addNode(disease, NodeType.CHANCE);
        Node therapyNode = probNet.addNode(therapy, NodeType.DECISION);
        Node utilityNode = probNet.addNode(utility, NodeType.UTILITY);
        utility.setDecisionCriterion(probNet.getDecisionCriteria().getFirst());
        probNet.addLink(diseaseNode, utilityNode, true);
        probNet.addLink(therapyNode, utilityNode, true);
        diseaseNode.setPotential(new TablePotential(List.of(disease), PotentialRole.CONDITIONAL_PROBABILITY,
                                                    new double[]{ 0.7, 0.3 }));
        utilityNode.setPotential(new ExactDistrPotential(List.of(utility, disease, therapy), PotentialRole.UNSPECIFIED,
                                                         new double[]{ 10, 2, 8, 6 }));
        propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(probNet.getVariables());
        propagation.setPreResolutionEvidence(new EvidenceCase());
        propagation.setPostResolutionEvidence(new EvidenceCase());
    }

    @Test void itEndsInTheStageThatComputesTheValuesOfTheNodes() throws Exception {
        propagation.getPosteriorValues();

        assertEquals(InferenceProgress.Stage.PROPAGATION, propagation.getProgress().getStage());
    }

    @Test void askedToStopItDoesNotGoOn() {
        propagation.getProgress().stop();

        assertThrows(InferenceStoppedException.class, propagation::getPosteriorValues);
    }

    @Test void theProgressNeverGoesBack() {
        InferenceProgress progress = new InferenceProgress();
        progress.advanceTo(0.6);
        progress.advanceTo(0.4);

        assertEquals(0.6, progress.getFraction());
    }
}
