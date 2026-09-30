/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ThereIsNoPotentialInNodeException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Propagating a network with a numeric chance node that has no potential fails naming that node.
 *
 * @author Manuel Arias
 */
public class ANumericChanceNodeWithoutPotentialIsNamedTest {

    /** A → X, where X is numeric and has no potential. */
    @Tag(TestSpeed.FAST)
    @Test public void whenTheNetworkIsPropagated() throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Node a = net.addNode(new Variable("A", "a0", "a1"), NodeType.CHANCE);
        a.setPotential(PotentialUtils.generateDefaultPotential(a));
        Variable x = new Variable("X");
        net.addNode(x, NodeType.CHANCE);
        net.addLink(a.getVariable(), x, true);

        UnrecoverableException thrown =
                assertThrows(UnrecoverableException.class, () -> new VEPropagation(net).getPosteriorValues());

        ThereIsNoPotentialInNodeException cause =
                assertInstanceOf(ThereIsNoPotentialInNodeException.class, thrown.getCause());
        assertEquals("X", cause.nodeName);
    }
}
