/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.DecompositionGenerateDecisionTree;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.amua.AmuaExporter;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The options of «Therapy» are «surgery», which costs 9000, and «medication», which costs 2000; both lead to the
 * chance node «Outcome». The base scenario written for Amua is the cheapest option, and of two equally cheap options,
 * the most effective (error 196).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheBaseScenarioOfAmuaIsTheCheapestOptionTest {

    private static ProbNet network() throws Exception {
        return new PGMXReader().read(
                IntegrationTest.class.getResource("/networks/id/ID-CEA-cheaper-option-second.pgmx")).probNet();
    }

    /** Exports the decision tree of the network, as the window of the decision tree does. */
    private static String baseScenario(ProbNet net) throws Exception {
        File file = File.createTempFile("BaseScenario", ".amua");
        file.deleteOnExit();
        new AmuaExporter(new DecompositionGenerateDecisionTree(net).getDecisionTree()).writeAmuaDT(file);
        return new SAXBuilder().build(file).getRootElement().getChild("DimInfo").getChildText("baseScenario");
    }

    @Test
    void theCheapestOptionIsTheBaseScenarioAlthoughItIsNotTheFirst() throws Exception {
        assertEquals("medication", baseScenario(network()));
    }

    /** Both cost 2000, and the outcome is better with medication: 0.9 instead of 0.6. */
    @Test
    void ofTwoEquallyCheapOptionsTheMostEffectiveIsTheBaseScenario() throws Exception {
        ProbNet net = network();
        ((ExactDistrPotential) net.getNode("Cost").getPotentials().getFirst()).setValues(new double[]{ 2000, 2000 });
        ((TablePotential) net.getNode("Outcome").getPotentials().getFirst()).setValues(new double[]{ 0.6, 0.4, 0.9, 0.1 });

        assertEquals("medication", baseScenario(net));
    }
}
