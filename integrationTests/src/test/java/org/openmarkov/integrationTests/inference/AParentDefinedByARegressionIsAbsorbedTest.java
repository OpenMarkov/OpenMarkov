/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.AbsorbParentsEdit;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.LinearCombinationPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A sum absorbs a parent defined by a regression as it absorbs one defined by an exact table with the
 * same numbers: the menu removes the parent, and inference counts its value once.
 *
 * @author Manuel Arias
 */
public class AParentDefinedByARegressionIsAbsorbedTest {

    /** ID-used-car-buyer.pgmx, whose expected utility is 32.96, with "Cost: Second Test" as a regression. */
    private static ProbNet withSecondTestCost(double constant, double slope) throws Exception {
        URL url = Networks.getNetworks().filter(u -> u.getPath().endsWith("/id/ID-used-car-buyer.pgmx"))
                          .findFirst().orElseThrow();
        ProbNet net = new PGMXReader().read(url).probNet();
        Node cost = net.getNode("Cost: Second Test");
        Variable decision = net.getVariable("Dec: Second Test");
        cost.setPotential(new LinearCombinationPotential(List.of(cost.getVariable(), decision),
                cost.getPotentials().getFirst().getPotentialRole(),
                new VariableExpression[] { VariableExpression.Common.CONSTANT,
                        new VariableExpression(List.of(decision), "{Dec: Second Test}") },
                new double[] { constant, slope }));
        return net;
    }

    private static double expectedUtility(ProbNet net) throws Exception {
        return new VEEvaluation(net).getUtility().getValues()[0];
    }

    /** The regression gives the numbers of the exact table of the network: 0 without the test, -4 with it. */
    @Tag(TestSpeed.MEDIUM)
    @Test public void theMenuRemovesTheParent() throws Exception {
        ProbNet net = withSecondTestCost(0, -4);
        Node total = net.getNode("Total");
        new AbsorbParentsEdit(net, total).executeEdit();
        assertNull(net.getNode("Cost: Second Test"));
        assertEquals(Set.of("Dec: First Test", "Dec: Second Test", "Dec: Purchase", "Car's Condition"),
                total.getParents().stream().map(Node::getName).collect(Collectors.toSet()));
        assertEquals(32.96, expectedUtility(net), 1E-9);
    }

    /** A cost of 4 whatever the decision takes 4 from the expected utility. */
    @Tag(TestSpeed.MEDIUM)
    @Test public void inferenceCountsItsValueOnce() throws Exception {
        assertEquals(28.96, expectedUtility(withSecondTestCost(-4, 0)), 1E-9);
    }
}
