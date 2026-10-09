/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.sensitivityanalysis;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.sensitivityanalysis.dialog.MapDialog;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;

import javax.swing.JFrame;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * «Map (two-way analysis)» of ID-decide-test, with a variation of 25 % and 50 points in each parameter. With the
 * costs of the test and of the therapy, «Do test? = yes» wins in the whole map, and it is the second option.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialog needs a display")
class TheMapOfTwoParametersOpensTest {

    private static AxisVariation aQuarterAround() {
        AxisVariation variation = new AxisVariation();
        variation.setVariationType(DeterministicAxisVariationType.PORV);
        variation.setVariationValue(25);
        return variation;
    }

    private static List<UncertainParameter> parameter(ProbNet probNet, String name) {
        return List.of(SystematicSampling.getUncertainParameters(probNet).stream()
                                         .filter(parameter -> name.equals(parameter.getName()))
                                         .findFirst()
                                         .orElseThrow());
    }

    @ParameterizedTest
    @CsvSource({ "cost of test, cost of therapy, Do test?", "cost of test, cost of therapy, Therapy",
            "prevalence, sensitivity, Do test?", "cost of test, cost of therapy," })
    void whateverOptionWins(String horizontal, String vertical, String decision) throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/networks/id/ID-decide-test.pgmx"))
                          .findFirst()
                          .orElseThrow();
        ProbNet probNet = new PGMXReader().read(url).probNet();
        SensitivityAnalysisModel model = new SensitivityAnalysisModel();
        model.setSelectedUncertainParametersXAxis(parameter(probNet, horizontal));
        model.setSelectedUncertainParametersYAxis(parameter(probNet, vertical));
        model.setHorizontalAxisVariation(aQuarterAround());
        model.setVerticalAxisVariation(aQuarterAround());
        model.setNumberOfIterationsSimulations(50);
        if (decision != null) {
            model.setDecisionVariable(probNet.getVariable(decision));
        }

        assertDoesNotThrow(() -> new MapDialog(new JFrame(), probNet, new EvidenceCase(), model).dispose());
    }
}
