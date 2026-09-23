/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Probability columns that do not add up to one are found, naming node, column and sum.
 *
 * @author Manuel Arias
 */
public class ColumnsThatDoNotAddUpToOneTest {

    private final ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
    private final Variable x = new Variable("X", 2);
    private final Variable y = new Variable("Y", 3);
    private final Node nodeX = net.addNode(x, NodeType.CHANCE);
    private final Node nodeY = net.addNode(y, NodeType.CHANCE);

    {
        net.addLink(x, y, true);
        nodeX.setPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7 }));
    }

    @Tag(TestSpeed.FAST)
    @Test public void aTableColumnIsFoundWithItsConfiguration() {
        nodeY.setPotential(new TablePotential(List.of(y, x), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[] { 0.05, 0.03, 0.02, 0.2, 0.3, 0.5 }));

        assertEquals(List.of("Y (X = " + x.getStates()[0].getName() + "): 0.10000000"), ColumnsThatDoNotAddUpToOne.in(net));
    }

    @Tag(TestSpeed.FAST)
    @Test public void theParametersOfACanonicalModelAreChecked() {
        MaxPotential max = new MaxPotential(List.of(y, x));
        max.setNoisyParameters(x, new double[] { 0.05, 0.03, 0.02, 0, 0, 1 });
        max.setLeakyParameters(new double[] { 0.5, 0.2, 0.2 });
        nodeY.setPotential(max);

        assertEquals(List.of("Y (X = " + x.getStates()[0].getName() + "): 0.10000000", "Y (leak): 0.90000000"),
                ColumnsThatDoNotAddUpToOne.in(net));
    }

    @Tag(TestSpeed.FAST)
    @Test public void aColumnOfZerosLeftByALinkRestrictionIsNotReported() {
        nodeY.setPotential(new TablePotential(List.of(y, x), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[] { 0, 0, 0, 0.2, 0.3, 0.5 }));

        assertEquals(List.of(), ColumnsThatDoNotAddUpToOne.in(net));
    }

    @Tag(TestSpeed.FAST)
    @Test public void roundingWithinTheToleranceIsNotReported() {
        nodeY.setPotential(new TablePotential(List.of(y, x), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[] { 0.1, 0.2, 0.7 + 1E-9, 0.2, 0.3, 0.5 }));

        assertEquals(List.of(), ColumnsThatDoNotAddUpToOne.in(net));
    }

    @Tag(TestSpeed.FAST)
    @Test public void aColumnWithinTheToleranceIsNormalizedInACopyAndTheOriginalIsKept() {
        TablePotential original = new TablePotential(List.of(y, x), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[] { 0.2, 0.3, 0.4995, 0.05, 0.03, 0.02 });
        nodeY.setPotential(original);
        ProbNet copy = net.copy();

        ColumnsThatDoNotAddUpToOne.normalizeWithinTolerance(copy);

        double[] normalized = ((TablePotential) copy.getNode(y).getPotential()).getValues();
        assertArrayEquals(new double[] { 0.2 / 0.9995, 0.3 / 0.9995, 0.4995 / 0.9995, 0.05, 0.03, 0.02 }, normalized, 1E-15,
                "the first column is normalized and the one beyond the tolerance is left for the warning");
        assertArrayEquals(new double[] { 0.2, 0.3, 0.4995, 0.05, 0.03, 0.02 }, original.getValues(), 0.0);
        assertSame(original, nodeY.getPotential(), "the original network keeps its potential");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theParametersOfACanonicalModelWithinTheToleranceAreNormalized() {
        MaxPotential max = new MaxPotential(List.of(y, x));
        max.setNoisyParameters(x, new double[] { 1, 0, 0, 0.2, 0.3, 0.4995 });
        max.setLeakyParameters(new double[] { 0.9995, 0, 0 });
        nodeY.setPotential(max);
        ProbNet copy = net.copy();

        ColumnsThatDoNotAddUpToOne.normalizeWithinTolerance(copy);

        MaxPotential normalized = (MaxPotential) copy.getNode(y).getPotential();
        assertArrayEquals(new double[] { 1, 0, 0, 0.2 / 0.9995, 0.3 / 0.9995, 0.4995 / 0.9995 },
                normalized.getNoisyParameters(x), 1E-15);
        assertArrayEquals(new double[] { 1, 0, 0 }, normalized.getLeakyParameters(), 1E-15);
        assertArrayEquals(new double[] { 1, 0, 0, 0.2, 0.3, 0.4995 }, max.getNoisyParameters(x), 0.0);
    }

    @Tag(TestSpeed.FAST)
    @Test public void aNetworkThatAddsUpToOneIsLeftAlone() {
        TablePotential original = new TablePotential(List.of(y, x), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[] { 0.1, 0.2, 0.7, 0.2, 0.3, 0.5 });
        nodeY.setPotential(original);
        ProbNet copy = net.copy();

        ColumnsThatDoNotAddUpToOne.normalizeWithinTolerance(copy);

        assertSame(original, copy.getNode(y).getPotential());
    }
}
