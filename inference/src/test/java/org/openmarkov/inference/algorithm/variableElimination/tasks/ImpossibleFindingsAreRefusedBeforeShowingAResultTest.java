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
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Disease (absent 0.9, present 0.1) → Result, which copies it without error; Result → Therapy, a decision; and
 * U(Disease, Therapy). The findings Disease = absent and Result = positive have probability zero. The propagation gave
 * its numbers when no chance node was left without a finding, and the window showed an expected utility computed as
 * if they were possible (error 428 of the pending list). Now the propagation always refuses them, and the window asks
 * {@link VEPropagation#refuseImpossibleFindings} before an evaluation, which takes them as any others because the
 * decision tree and other algorithms evaluate scenarios that cannot happen.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ImpossibleFindingsAreRefusedBeforeShowingAResultTest {

    private ProbNet net;
    private Variable disease;
    private Variable result;
    private Variable therapy;
    private Variable u;

    @BeforeEach void buildTheNetwork() {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        disease = new Variable("Disease", "absent", "present");
        result = new Variable("Result", "negative", "positive");
        therapy = new Variable("Therapy", "no", "yes");
        u = new Variable("U");
        Node diseaseNode = net.addNode(disease, NodeType.CHANCE);
        Node resultNode = net.addNode(result, NodeType.CHANCE);
        Node therapyNode = net.addNode(therapy, NodeType.DECISION);
        Node uNode = net.addNode(u, NodeType.UTILITY);
        net.addLink(diseaseNode, resultNode, true);
        net.addLink(resultNode, therapyNode, true);
        net.addLink(diseaseNode, uNode, true);
        net.addLink(therapyNode, uNode, true);
        diseaseNode.setPotential(new TablePotential(List.of(disease), PotentialRole.CONDITIONAL_PROBABILITY,
                                                    new double[] {0.9, 0.1}));
        resultNode.setPotential(new TablePotential(List.of(result, disease), PotentialRole.CONDITIONAL_PROBABILITY,
                                                   new double[] {1, 0, 0, 1}));
        TablePotential utility = new TablePotential(List.of(disease, therapy), PotentialRole.UNSPECIFIED,
                                                    new double[] {10, 2, 8, 6});
        utility.setCriterion(net.getDecisionCriteria().getFirst());
        uNode.setPotential(utility);
    }

    private EvidenceCase findings(boolean alsoDiseaseAbsent) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(result, result.getStateIndex("positive")));
        if (alsoDiseaseAbsent) {
            evidence.addFinding(new Finding(disease, disease.getStateIndex("absent")));
        }
        return evidence;
    }

    private VEPropagation propagation() throws Exception {
        VEPropagation propagation = new VEPropagation(net);
        propagation.setVariablesOfInterest(List.of(u));
        return propagation;
    }

    @Test void withoutFindingsTheEvaluationGivesTheExpectedUtility() throws Exception {
        assertEquals(9.6, new VEEvaluation(net).getUtility().getValues()[0], 1e-9);
    }

    @Test void theCheckOfTheWindowRefusesThem() {
        assertThrows(IncompatibleEvidenceException.EvidenceIsImpossible.class,
                     () -> VEPropagation.refuseImpossibleFindings(net, findings(true)));
    }

    @Test void theCheckOfTheWindowTakesPossibleFindingsAndNoFindings() throws Exception {
        VEPropagation.refuseImpossibleFindings(net, findings(false));
        VEPropagation.refuseImpossibleFindings(net, new EvidenceCase());
    }

    @Test void anEvaluationTakesThemAsAnyOthers() throws Exception {
        VEEvaluation evaluation = new VEEvaluation(net);
        evaluation.setPreResolutionEvidence(findings(true));

        assertEquals(0, evaluation.getProbability().getValues()[0], 1e-12);
    }

    @Test void thePropagationRefusesThemBeforeTheResolution() throws Exception {
        VEPropagation propagation = propagation();
        propagation.setPreResolutionEvidence(findings(true));

        assertThrows(IncompatibleEvidenceException.EvidenceIsImpossible.class, propagation::getPosteriorValues);
    }

    @Test void thePropagationRefusesThemAfterTheResolution() throws Exception {
        VEPropagation propagation = propagation();
        propagation.setPostResolutionEvidence(findings(true));

        assertThrows(IncompatibleEvidenceException.EvidenceIsImpossible.class, propagation::getPosteriorValues);
    }

    @Test void possibleFindingsGiveWhatTheyGave() throws Exception {
        VEEvaluation evaluation = new VEEvaluation(net);
        evaluation.setPreResolutionEvidence(findings(false));
        VEPropagation propagation = new VEPropagation(net);
        propagation.setVariablesOfInterest(List.of(disease));
        propagation.setPostResolutionEvidence(findings(false));

        assertEquals(6, evaluation.getUtility().getValues()[0], 1e-9);
        assertArrayEquals(new double[] {0, 1}, propagation.getPosteriorValues().get(disease).getValues(), 1e-9);
    }
}
