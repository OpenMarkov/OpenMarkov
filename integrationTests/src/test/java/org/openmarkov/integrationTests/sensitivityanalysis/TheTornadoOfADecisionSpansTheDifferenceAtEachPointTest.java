/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.sensitivityanalysis;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VESensAnTornadoSpider;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.sensitivityanalysis.dialog.TornadoSpiderDialog;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * With decision scope, the bar of a parameter in the tornado goes from the least to the greatest difference between
 * the intervention and the reference at the same point of the sweep, as the spider draws it.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheTornadoOfADecisionSpansTheDifferenceAtEachPointTest {

    @Test void whenTheOptionsGoOppositeWays() {
        double[] values = { 0, -0.25, -0.5, -0.75, -1, 0, 0.25, 0.5, 0.75, 1 };

        double[] differences = TornadoSpiderDialog.differences(values, 4, 1, 0);

        assertArrayEquals(new double[]{ 0, 0.5, 1, 1.5, 2 }, differences);
    }

    /** ID-decide-test, «Do test? = yes» relative to «no», a variation of 25 % and 50 points. */
    @Test void inANetwork() throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/networks/id/ID-decide-test.pgmx"))
                          .findFirst()
                          .orElseThrow();
        ProbNet probNet = new PGMXReader().read(url).probNet();
        UncertainParameter utilityNotTreated = SystematicSampling.getUncertainParameters(probNet).stream()
                                                                 .filter(parameter -> "utility not treated".equals(parameter.getName()))
                                                                 .findFirst()
                                                                 .orElseThrow();
        AxisVariation variation = new AxisVariation();
        variation.setVariationType(DeterministicAxisVariationType.PORV);
        variation.setVariationValue(25);
        VESensAnTornadoSpider tornado = new VESensAnTornadoSpider(probNet, new EvidenceCase(), List.of(utilityNotTreated),
                                                                  variation, 50, probNet.getVariable("Do test?"));

        double[] differences = TornadoSpiderDialog.differences(
                tornado.getUncertainParametersPotentials().get(utilityNotTreated).getValues(), 50, 1, 0);

        assertEquals(-0.2000, Arrays.stream(differences).min().orElseThrow(), 5e-5);
        assertEquals(0.1991, Arrays.stream(differences).max().orElseThrow(), 5e-5);
    }
}
