/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.stochasticPropagationOutput;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.huginPropagation.HuginPropagation;
import org.openmarkov.inference.algorithm.likelihoodWeighting.LikelihoodWeighting;
import org.openmarkov.inference.algorithm.likelihoodWeighting.LogicSampling;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A Bayesian network P → N whose chance node N is numeric, with the relation «Exact». Neither the sampling nor the
 * exact propagation can take it: they used to fail when calculating, some with an unexpected error. Now the
 * algorithms refuse it when they are built, and the tool «Export stochastic propagation data» when it is opened
 * (error 108).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANumericChanceNodeIsRefusedBeforeCalculatingTest {

    private static ProbNet aNumericChild() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable p = new Variable("P", "absent", "present");
        Variable n = new Variable("N", true, 0, 100, true, 0.01);
        Node pNode = net.addNode(p, NodeType.CHANCE);
        Node nNode = net.addNode(n, NodeType.CHANCE);
        net.addLink(pNode, nNode, true);
        pNode.setPotential(new TablePotential(List.of(p), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7 }));
        nNode.setPotential(new ExactDistrPotential(new ArrayList<>(List.of(n, p)), PotentialRole.CONDITIONAL_PROBABILITY,
                                                   new double[] { 3, 7 }));
        return net;
    }

    /** The message names the variable, not its type and states, as it used to. */
    private static final String REFUSAL = "Could not meet the constraint: All variables must be discrete\nVariable N must be discrete.";

    private static void refusesN(Executable building) {
        var refused = assertThrows(ConstraintViolatedException.class, building);
        assertEquals(REFUSAL, refused.getExceptionMessage());
    }

    @Test void theAlgorithmsRefuseIt() {
        ProbNet net = aNumericChild();

        refusesN(() -> new LikelihoodWeighting(net));
        refusesN(() -> new LogicSampling(net));
        refusesN(() -> new HuginPropagation(net));
    }

    @Test void theToolRefusesItWhenOpened() {
        Exception error = StochasticPropagationOutputFrame.boundError(aNumericChild());

        assertInstanceOf(ConstraintViolatedException.class, error);
        assertEquals(REFUSAL, ((ConstraintViolatedException) error).getExceptionMessage());
    }

    @Test void aNetworkOfFiniteStatesIsStillAccepted() {
        ProbNet asia = ExampleNets.buildBN_asia_java();

        assertDoesNotThrow(() -> new LikelihoodWeighting(asia));
        assertDoesNotThrow(() -> new LogicSampling(asia));
        assertDoesNotThrow(() -> new HuginPropagation(asia));
        assertNull(StochasticPropagationOutputFrame.boundError(asia));
    }
}
