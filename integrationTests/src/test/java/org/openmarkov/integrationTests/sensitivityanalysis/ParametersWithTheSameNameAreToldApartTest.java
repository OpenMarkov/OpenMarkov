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
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.sensitivityanalysis.dialog.TornadoSpiderDialog;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisController;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisModel;

import javax.swing.SwingUtilities;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * In ID-decide-test, the parameter of «Cost of therapy» is renamed «cost of test», the name of the parameter of
 * «Cost of test», as a file can bring it. The sensitivity analysis used to keep one of the two, so the other could not
 * be analysed, and the tornado failed on the repeated name (error 308 of the pending list). Now each one is offered
 * with the node of its relation.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class ParametersWithTheSameNameAreToldApartTest {

    private static ProbNet withARepeatedName() throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/networks/id/ID-decide-test.pgmx"))
                          .findFirst()
                          .orElseThrow();
        ProbNet probNet = new PGMXReader().read(url).probNet();
        ExactDistrPotential costOfTherapy = (ExactDistrPotential) probNet.getNode("Cost of therapy").getPotential();
        for (UncertainValue value : costOfTherapy.getTablePotential().getUncertainValues()) {
            if (value != null && "cost of therapy".equals(value.getName())) {
                value.setName("cost of test");
            }
        }
        return probNet;
    }

    @Test void eachIsOfferedWithItsNode() throws Exception {
        List<UncertainParameter> parameters = SystematicSampling.getUncertainParameters(withARepeatedName());

        LinkedHashMap<String, UncertainParameter> byLabel = SensitivityAnalysisController.byLabel(parameters);

        UncertainParameter ofTheTest = byLabel.get("cost of test [Cost of test]");
        UncertainParameter ofTheTherapy = byLabel.get("cost of test [Cost of therapy]");
        assertNotSame(ofTheTest, ofTheTherapy);
        assertEquals("Cost of test", ofTheTest.getPotential().getVariable(0).getName());
        assertEquals("Cost of therapy", ofTheTherapy.getPotential().getVariable(0).getName());
        assertEquals(parameters.stream().filter(UncertainParameter::hasName).count(), byLabel.size());
        // The names that are not repeated stay as they are
        assertEquals("prevalence", byLabel.get("prevalence").getLabel());
    }

    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The tornado is a window")
    @Test void theTornadoOfEveryParameterOpens() throws Exception {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            ProbNet probNet = withARepeatedName();
            SensitivityAnalysisModel model = new SensitivityAnalysisModel();
            model.setSelectedUncertainParametersXAxis(new ArrayList<>(
                    SensitivityAnalysisController.byLabel(SystematicSampling.getUncertainParameters(probNet)).values()));
            AxisVariation variation = new AxisVariation();
            variation.setVariationType(DeterministicAxisVariationType.PORV);
            variation.setVariationValue(25);
            model.setHorizontalAxisVariation(variation);
            model.setNumberOfIterationsSimulations(10);
            SwingUtilities.invokeAndWait(() -> {
                try {
                    new TornadoSpiderDialog(MainGUI.INSTANCE, probNet, new EvidenceCase(), model).dispose();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
        } finally {
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }
}
