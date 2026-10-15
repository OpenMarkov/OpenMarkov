/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.DirichletFunction;
import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.modelUncertainty.NormalFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Disease (absent 0.9, present 0.1) is the parent of a numeric chance node N and of a utility U, worth 10 if the
 * disease is present and 0 if it is absent. Observing N has to change the probability of the disease, as observing
 * a test of finite states does.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AFindingOnANumericNodeTellsAboutItsParentsTest {

    private Variable disease;
    private Variable n;

    /** The network, with the relation of N given by a distribution and its parameters for absent and present. */
    private ProbNet network(Class<? extends ProbDensFunction> distribution, double... parameters) {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        disease = new Variable("Disease", "absent", "present");
        n = new Variable("N", false, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false, 0.01);
        Variable u = new Variable("U");
        Node diseaseNode = probNet.addNode(disease, NodeType.CHANCE);
        Node nNode = probNet.addNode(n, NodeType.CHANCE);
        Node uNode = probNet.addNode(u, NodeType.UTILITY);
        probNet.addLink(diseaseNode, nNode, true);
        probNet.addLink(diseaseNode, uNode, true);
        diseaseNode.setPotential(new TablePotential(List.of(disease), PotentialRole.CONDITIONAL_PROBABILITY,
                                                    new double[] { 0.9, 0.1 }));
        UnivariateDistrPotential relation = new UnivariateDistrPotential(List.of(n, disease), distribution,
                                                                         PotentialRole.CONDITIONAL_PROBABILITY);
        relation.setParameters(parameters);
        nNode.setPotential(relation);
        TablePotential utility = new TablePotential(List.of(disease), PotentialRole.UNSPECIFIED, new double[] { 0, 10 });
        utility.setCriterion(probNet.getDecisionCriteria().getFirst());
        uNode.setPotential(utility);
        return probNet;
    }

    private EvidenceCase observed(double value) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(n, value));
        return evidence;
    }

    private double[] probabilityOfTheDisease(ProbNet probNet, EvidenceCase evidence) throws Exception {
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(List.of(disease));
        propagation.setPreResolutionEvidence(evidence);
        return propagation.getPosteriorValues().get(probNet.getVariable("Disease")).getValues();
    }

    private double expectedUtility(ProbNet probNet, EvidenceCase evidence) throws Exception {
        VEEvaluation evaluation = new VEEvaluation(probNet);
        evaluation.setPreResolutionEvidence(evidence);
        return evaluation.getUtility().getValues()[0];
    }

    /** N is 3 if the disease is absent and 7 if it is present: observing 7 means the disease is present. */
    @Test void anExactRelation() throws Exception {
        ProbNet probNet = network(ExactFunction.class, 3, 7);
        assertArrayEquals(new double[] { 0, 1 }, probabilityOfTheDisease(probNet, observed(7)), 1e-12);
        assertEquals(10, expectedUtility(probNet, observed(7)), 1e-12);
    }

    /** N is normal with mean 5 if the disease is absent and 8 if it is present, and deviation 1. */
    @Test void aNormalDistribution() throws Exception {
        ProbNet probNet = network(NormalFunction.class, 5, 1, 8, 1);
        double absent = 0.9 * Math.exp(-4.5);
        double present = 0.1;
        double[] expected = { absent / (absent + present), present / (absent + present) };
        assertArrayEquals(expected, probabilityOfTheDisease(probNet, observed(8)), 1e-9);
        assertEquals(10 * expected[1], expectedUtility(probNet, observed(8)), 1e-9);
    }

    /** Neither 3 nor 7: the finding is impossible, as for a test of finite states. */
    @Test void aValueTheRelationCannotGiveIsImpossible() {
        ProbNet probNet = network(ExactFunction.class, 3, 7);
        assertThrows(IncompatibleEvidenceException.EvidenceIsImpossible.class,
                     () -> probabilityOfTheDisease(probNet, observed(5)));
    }

    /** In inference mode the finding comes after the resolution, and tells the same. */
    @Test void aFindingAfterTheResolution() throws Exception {
        ProbNet probNet = network(NormalFunction.class, 5, 1, 8, 1);
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(List.of(disease));
        propagation.setPostResolutionEvidence(observed(8));
        assertArrayEquals(probabilityOfTheDisease(probNet, observed(8)),
                          propagation.getPosteriorValues().get(probNet.getVariable("Disease")).getValues(), 1e-12);
    }

    /** A Dirichlet is a distribution of probabilities: it cannot say how likely a value of N is. */
    @Test void aDistributionWithoutDensityIsRefusedWithAMessage() {
        ProbNet probNet = network(DirichletFunction.class, 1, 1);
        assertThrows(NonProjectablePotentialException.DensityCannotBeCalculated.class,
                     () -> ProbNetOperations.convertNumericalVariablesToFS(probNet, observed(8)));
    }
}
