/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A column that misses one only by rounding is normalized in the copy the algorithm works on:
 * the posteriors add up to one, and the network of the user keeps its numbers.
 *
 * @author Manuel Arias
 */
public class RoundedColumnsAreNormalizedBeforeInferenceTest {

    @Tag(TestSpeed.FAST)
    @Test public void thePosteriorOfARoundedTableAddsUpToOne() throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable y = new Variable("Y", 2);
        net.addNode(y, NodeType.CHANCE);
        TablePotential rounded = new TablePotential(List.of(y), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[] { 0.3333, 0.6666 });
        net.getNode(y).setPotential(rounded);

        VEPropagation propagation = new VEPropagation(net);
        propagation.setVariablesOfInterest(List.of(y));
        double[] posterior = propagation.getPosteriorValues().get(y).getValues();

        assertArrayEquals(new double[] { 0.3333 / 0.9999, 0.6666 / 0.9999 }, posterior, 1E-12);
        assertSame(rounded, net.getNode(y).getPotential());
        assertArrayEquals(new double[] { 0.3333, 0.6666 }, rounded.getValues(), 0.0);
    }
}
