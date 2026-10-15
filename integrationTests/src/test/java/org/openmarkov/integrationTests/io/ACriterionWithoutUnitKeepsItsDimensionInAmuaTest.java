/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.DecompositionGenerateDecisionTree;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.amua.AmuaExporter;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A network file whose criterion «Effectiveness» has no unit is read with a null unit. Exported to Amua, the model
 * still has one symbol per dimension, the second one empty, because Amua counts the dimensions by the symbols
 * (error 244).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACriterionWithoutUnitKeepsItsDimensionInAmuaTest {

    @Test
    void theCriterionWithoutUnitGetsAnEmptySymbol() throws Exception {
        ProbNet net = new PGMXReader().read(
                IntegrationTest.class.getResource("/networks/id/ID-CEA-minimal.pgmx")).probNet();
        for (Criterion criterion : net.getDecisionCriteria()) {
            if (criterion.getCECriterion() == Criterion.CECriterion.Effectiveness) {
                criterion.setCriterionUnit(null);
            }
        }

        File file = File.createTempFile("CriterionWithoutUnit", ".amua");
        file.deleteOnExit();
        new AmuaExporter(new DecompositionGenerateDecisionTree(net).getDecisionTree()).writeAmuaDT(file);
        Element dimInfo = new SAXBuilder().build(file).getRootElement().getChild("DimInfo");

        assertEquals(List.of("Cost", "Effectiveness"), texts(dimInfo, "dimNames"));
        assertEquals(List.of("€", ""), texts(dimInfo, "dimSymbols"));
    }

    private static List<String> texts(Element parent, String name) {
        return parent.getChildren(name).stream().map(Element::getText).toList();
    }
}
