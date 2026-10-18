/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.sensitivityanalysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.BetaFunction;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.DirichletFunction;
import org.openmarkov.core.model.network.modelUncertainty.DomainInterval;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VESensAnTornadoSpider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * X has a Dirichlet(2, 3, 5), so its probabilities are 0.2, 0.3 and 0.5, and U(X) is 1 for X = a and 0 otherwise, so
 * the expected utility is the probability of a. Varying the parameter of a varies that probability, and the other two
 * keep their proportion (error 309).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AParameterOfADirichletIsVariedAsAProbabilityTest {

    private ProbNet net;
    private UncertainParameter pa;

    @BeforeEach void buildTheNetwork() {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable x = new Variable("X", "a", "b", "c");
        Variable u = new Variable("U");
        net.addNode(x, NodeType.CHANCE);
        net.addNode(u, NodeType.UTILITY);
        u.setDecisionCriterion(new Criterion());
        net.addLink(x, u, true);
        TablePotential probabilities = new UncertainTablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY,
                                                                   new double[] { 0.2, 0.3, 0.5 });
        probabilities.setUncertainValues(new UncertainValue[] { new UncertainValue(new DirichletFunction(2), "pa"),
                new UncertainValue(new DirichletFunction(3), "pb"), new UncertainValue(new DirichletFunction(5), "pc") });
        net.getNode(x).setPotential(probabilities);
        net.getNode(u).setPotential(new ExactDistrPotential(List.of(u, x), PotentialRole.UNSPECIFIED,
                                                            new double[] { 1, 0, 0 }));
        pa = SystematicSampling.getUncertainParameters(net).stream().filter(p -> "pa".equals(p.getName()))
                               .findFirst().orElseThrow();
    }

    /** The expected utility at the five points of the tornado. */
    private double[] tornado(AxisVariation variation) throws Exception {
        return new VESensAnTornadoSpider(net, new EvidenceCase(), List.of(pa), variation, 4, null)
                .getUncertainParametersPotentials().get(pa).getValues();
    }

    private static AxisVariation variation(DeterministicAxisVariationType type, double value) {
        AxisVariation variation = new AxisVariation();
        variation.setVariationType(type);
        variation.setVariationValue(value);
        return variation;
    }

    @Test void itsReferenceValueIsItsProbability() {
        assertEquals(0.2, pa.getBaseLineValue(), 1e-12);
    }

    @Test void aPercentageVariesTheProbability() throws Exception {
        assertArrayEquals(new double[] { 0.16, 0.18, 0.2, 0.22, 0.24 },
                          tornado(variation(DeterministicAxisVariationType.PORV, 20)), 1e-9);
    }

    @Test void aTypedIntervalIsTheProbabilityItSays() throws Exception {
        AxisVariation variation = variation(DeterministicAxisVariationType.UDIN, 0);
        variation.setVariationBounds(new double[] { 0.1, 0.3 });

        assertArrayEquals(new double[] { 0.1, 0.15, 0.2, 0.25, 0.3 }, tornado(variation), 1e-9);
    }

    @Test void theSecondOrderProbabilityIsThatOfItsBeta() throws Exception {
        DomainInterval beta = new BetaFunction(2, 8).getInterval(0.9);

        double[] values = tornado(variation(DeterministicAxisVariationType.POPP, 90));

        assertEquals(beta.min(), values[0], 1e-9);
        assertEquals(beta.max(), values[4], 1e-9);
    }
}
