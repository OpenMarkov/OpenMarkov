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
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECESensAnSpider;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The spider of cost-effectiveness, with a variation of 20 % and 4 intervals, sweeps each named uncertain parameter
 * in copies of the network: the network it receives keeps its potentials and their numbers, also when a table is
 * not the mean of the distribution of its parameter.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheCostEffectivenessSpiderLeavesTheNetworkAsItWasTest {

    private static ProbNet network(String name) throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith(name))
                          .findFirst()
                          .orElseThrow();
        return new PGMXReader().read(url).probNet();
    }

    private static Map<Node, TablePotential> tables(ProbNet probNet) {
        Map<Node, TablePotential> tables = new LinkedHashMap<>();
        for (Node node : probNet.getNodes()) {
            if (node.getPotential() instanceof TablePotential table) {
                tables.put(node, table);
            }
        }
        return tables;
    }

    private static void sweepTheSpider(ProbNet probNet, String decision) throws Exception {
        AxisVariation variation = new AxisVariation();
        variation.setVariationType(DeterministicAxisVariationType.PORV);
        variation.setVariationValue(20);
        List<UncertainParameter> named = SystematicSampling.getUncertainParameters(probNet).stream()
                                                           .filter(UncertainParameter::hasName)
                                                           .toList();
        VECESensAnSpider spider = new VECESensAnSpider(probNet);
        spider.setPreResolutionEvidence(new EvidenceCase());
        spider.setDecisionVariable(probNet.getVariable(decision));
        spider.setNumIterations(4);
        spider.setUncertainParameters(named);
        spider.setAxisVariation(variation);
        spider.getCEPotentials();
    }

    private static void assertUnchanged(ProbNet probNet, String decision) throws Exception {
        Map<Node, TablePotential> before = tables(probNet);
        Map<Node, double[]> numbers = new LinkedHashMap<>();
        before.forEach((node, table) -> numbers.put(node, table.getValues().clone()));

        sweepTheSpider(probNet, decision);

        Map<Node, TablePotential> after = tables(probNet);
        assertEquals(before.keySet(), after.keySet());
        for (Node node : before.keySet()) {
            assertSame(before.get(node), after.get(node), node.getName());
            assertArrayEquals(numbers.get(node), after.get(node).getValues(), node.getName());
        }
    }

    @Test void whenATableIsNotTheMeanOfItsDistribution() throws Exception {
        ProbNet probNet = network("/networks/id/ID-CEA-test-2therapies.pgmx");
        double[] disease = ((TablePotential) probNet.getNode("Disease").getPotential()).getValues();
        disease[0] = 0.8;
        disease[1] = 0.2;

        assertUnchanged(probNet, "Therapy");
    }

    @Test void withoutRoundingResidues() throws Exception {
        assertUnchanged(network("/networks/id/ID-decide-test.pgmx"), "Therapy");
    }
}
