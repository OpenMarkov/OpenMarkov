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
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A scale is applied once to a utility that combines other utilities: a product or a formula is
 * scaled itself, and its parents are not; a sum leaves it to its parents.
 *
 * @author Manuel Arias
 */
public class ACombinationOfUtilitiesIsScaledOnceTest {

    private double formula(String formula, double scale) throws Exception {
        String text;
        try (InputStream in = getClass().getResourceAsStream("/networks/dan/DAN-two-utility-and-chance-function-sv.pgmx")) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        Path file = Files.createTempFile("formula", ".pgmx");
        Files.writeString(file, text.replace("abs({U1})*{U2}", formula));
        ProbNet net = new PGMXReader().read(file.toUri().toURL()).probNet();
        for (Criterion criterion : net.getDecisionCriteria()) {
            criterion.setUnicriterizationScale(scale);
        }
        return new VEEvaluation(net).getUtility().getValues()[0];
    }

    private void assertScaledOnce(String formula) throws Exception {
        double unscaled = formula(formula, 1);
        assertEquals(2 * unscaled, formula(formula, 2), 1E-9, formula);
        assertEquals(0.001 * unscaled, formula(formula, 0.001), 1E-12, formula);
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void aFormulaThatMultipliesUtilities() throws Exception {
        assertScaledOnce("abs({U1})*{U2}");
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void aFormulaWithAConstantTerm() throws Exception {
        assertScaledOnce("{U1}+100");
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void aFormulaThatMultipliesAndAdds() throws Exception {
        assertScaledOnce("{U1}*{U2}+{U1}");
    }

    private double[] effectivenessOfHPV(double effectivenessScale) throws Exception {
        URL url = Networks.getNetworks().filter(u -> u.getPath().endsWith("/mid/MID-HPV.pgmx")).findFirst().orElseThrow();
        ProbNet net = new PGMXReader().read(url).probNet();
        for (Criterion criterion : net.getDecisionCriteria()) {
            if (criterion.getCECriterion() == Criterion.CECriterion.Effectiveness) {
                criterion.setCeScale(effectivenessScale);
            }
        }
        VECEAnalysis analysis = new VECEAnalysis(net);
        analysis.setDecisionVariable(net.getNodes(NodeType.DECISION).getFirst().getVariable());
        analysis.setPreResolutionEvidence(new EvidenceCase());
        GTablePotential<?> result = analysis.getUtility();
        double[] effectiveness = new double[result.elementTable.size()];
        for (int i = 0; i < effectiveness.length; i++) {
            effectiveness[i] = ((CEP) result.elementTable.get(i)).getEffectiveness(0);
        }
        return effectiveness;
    }

    /** "QoL [t]" is the product of two utilities of effectiveness. */
    @Tag(TestSpeed.MEDIUM)
    @Test public void aProductOfUtilitiesInHPV() throws Exception {
        double[] unscaled = effectivenessOfHPV(1);
        double[] doubled = effectivenessOfHPV(2);
        for (int i = 0; i < unscaled.length; i++) {
            assertEquals(2 * unscaled[i], doubled[i], 1E-9);
        }
    }
}
