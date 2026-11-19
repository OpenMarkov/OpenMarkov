/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.LinearCombinationPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Age, a numeric node fixed at 60, is the parent of Dose, a numeric node whose «Exact» relation gives 7.5; in the
 * influence diagram, Cost = -10 Dose.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnExactRelationWithANumericParentTest {

    private static ProbNet network(NetworkType type) {
        ProbNet net = new ProbNet(type);
        Variable age = new Variable("Age");
        Variable dose = new Variable("Dose");
        Node ageNode = net.addNode(age, NodeType.CHANCE);
        Node doseNode = net.addNode(dose, NodeType.CHANCE);
        net.addLink(ageNode, doseNode, true);
        ageNode.setPotential(new DeltaPotential(List.of(age), PotentialRole.CONDITIONAL_PROBABILITY, 60));
        doseNode.setPotential(new ExactDistrPotential(List.of(dose, age), PotentialRole.CONDITIONAL_PROBABILITY,
                                                      new double[]{ 7.5 }));
        return net;
    }

    private static ProbNet influenceDiagram() {
        ProbNet net = network(InfluenceDiagramType.getUniqueInstance());
        net.addNode(new Variable("Therapy", "no", "yes"), NodeType.DECISION);
        Variable dose = net.getVariable("Dose");
        Variable cost = new Variable("Cost");
        cost.setDecisionCriterion(net.getDecisionCriteria().getFirst());
        Node costNode = net.addNode(cost, NodeType.UTILITY);
        net.addLink(net.getNode(dose), costNode, true);
        costNode.setPotential(new LinearCombinationPotential(List.of(cost, dose), PotentialRole.UNSPECIFIED,
                new VariableExpression[]{ VariableExpression.Common.CONSTANT,
                        new VariableExpression(List.of(dose), "{Dose}") },
                new double[]{ 0, -10 }));
        return net;
    }

    private static double expectedUtility(ProbNet net, EvidenceCase found) throws Exception {
        VEEvaluation evaluation = new VEEvaluation(net);
        evaluation.setPreResolutionEvidence(found);
        return evaluation.getUtility().getValues()[0];
    }

    @Test void theBayesianNetworkIsPropagated() throws Exception {
        ProbNet net = network(BayesianNetworkType.getUniqueInstance());
        VEPropagation propagation = new VEPropagation(net);
        propagation.setVariablesOfInterest(net.getVariables());
        propagation.setPreResolutionEvidence(new EvidenceCase());

        assertDoesNotThrow(propagation::getPosteriorValues);
    }

    @Test void theInfluenceDiagramIsEvaluated() throws Exception {
        assertEquals(-75, expectedUtility(influenceDiagram(), new EvidenceCase()), 1E-9);
    }

    @Test void theInfluenceDiagramIsEvaluatedWithTheDoseFound() throws Exception {
        ProbNet net = influenceDiagram();
        EvidenceCase found = new EvidenceCase();
        found.addFinding(new Finding(net.getVariable("Dose"), 7.5));

        assertEquals(-75, expectedUtility(net, found), 1E-9);
    }

    @Test void theInfluenceDiagramIsEvaluatedAfterSavingItAndOpeningIt(@TempDir Path folder) throws Exception {
        Path file = folder.resolve("dose.pgmx");
        ProbNet net = influenceDiagram();
        net.setName(file.getFileName().toString());
        new PGMXWriter_0_2().write(file.toString(), net, List.of(new EvidenceCase()));
        ProbNet opened = new PGMXReader().read(file.toUri().toURL()).probNet();

        assertEquals(-75, expectedUtility(opened, new EvidenceCase()), 1E-9);
    }
}
