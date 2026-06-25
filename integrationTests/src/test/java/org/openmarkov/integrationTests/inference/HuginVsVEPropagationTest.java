/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.inference.algorithm.huginPropagation.ClusterPropagation;
import org.openmarkov.inference.algorithm.huginPropagation.HuginPropagation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that verifies HuginPropagation (junction tree) and
 * VEPropagation (variable elimination) produce identical posterior
 * probabilities on large Bayesian networks.
 *
 * <p>Both algorithms are exact, so their posteriors must agree within
 * floating-point tolerance. Any discrepancy indicates a bug in one of the
 * two inference engines or in the shared preprocessing steps.</p>
 *
 * <p>Tested on BN-hepar (70 variables, Onisko 2003) — a large enough network
 * to exercise non-trivial elimination orders and clique structures.</p>
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HuginVsVEPropagationTest {

    private static final double TOLERANCE = 1.0e-4;

    private static final String HEPAR = "networks/bn/BN-hepar.pgmx";

    @Test
    public void noEvidence_huginAndVEProduceSamePosteriors() throws Exception {
        ProbNet probNet = loadNetwork(HEPAR);
        List<Variable> chanceVars = getChanceVariables(probNet);

        HashMap<Variable, TablePotential> huginPosteriors = runHugin(probNet, new EvidenceCase());
        HashMap<Variable, TablePotential> vePosteriors = runVE(probNet, chanceVars, new EvidenceCase());

        assertPosteriorsMatch(huginPosteriors, vePosteriors, chanceVars);
    }

    @Test
    public void withEvidence_huginAndVEProduceSamePosteriors() throws Exception {
        ProbNet probNet = loadNetwork(HEPAR);
        List<Variable> chanceVars = getChanceVariables(probNet);

        // Set evidence on the first two variables (deterministic selection for reproducibility)
        EvidenceCase evidence = new EvidenceCase();
        int count = 0;
        for (Variable v : chanceVars) {
            if (count >= 2) break;
            evidence.addFinding(new Finding(v, 0));
            count++;
        }

        List<Variable> unobserved = chanceVars.stream()
                .filter(v -> !evidence.getVariables().contains(v))
                .collect(Collectors.toList());

        HashMap<Variable, TablePotential> huginPosteriors = runHugin(probNet, evidence);
        HashMap<Variable, TablePotential> vePosteriors = runVE(probNet, unobserved, evidence);

        assertPosteriorsMatch(huginPosteriors, vePosteriors, unobserved);
    }

    // -----------------------------------------------------------------------
    // Inference runners
    // -----------------------------------------------------------------------

    private HashMap<Variable, TablePotential> runHugin(ProbNet probNet, EvidenceCase evidence)
            throws Exception {
        ClusterPropagation propagation = new HuginPropagation(probNet);
        propagation.compilePriorPotentials();
        propagation.setPreResolutionEvidence(evidence);
        return propagation.getPosteriorValues();
    }

    private HashMap<Variable, TablePotential> runVE(ProbNet probNet, List<Variable> variablesOfInterest,
                                                     EvidenceCase evidence) throws Exception {
        VEPropagation vePropagation = new VEPropagation(probNet);
        vePropagation.setVariablesOfInterest(variablesOfInterest);
        vePropagation.setPostResolutionEvidence(evidence);
        return vePropagation.getPosteriorValues();
    }

    // -----------------------------------------------------------------------
    // Assertions
    // -----------------------------------------------------------------------

    private void assertPosteriorsMatch(HashMap<Variable, TablePotential> huginPosteriors,
                                       HashMap<Variable, TablePotential> vePosteriors,
                                       List<Variable> variables) {
        for (Variable variable : variables) {
            TablePotential huginPotential = huginPosteriors.get(variable);
            TablePotential vePotential = vePosteriors.get(variable);

            assertNotNull(huginPotential, "Hugin posterior missing for: " + variable.getName());
            assertNotNull(vePotential, "VE posterior missing for: " + variable.getName());

            double[] huginValues = huginPotential.getValues();
            double[] veValues = vePotential.getValues();

            assertEquals(huginValues.length, veValues.length,
                    "Different number of states for variable: " + variable.getName());

            for (int i = 0; i < huginValues.length; i++) {
                assertEquals(huginValues[i], veValues[i], TOLERANCE,
                        String.format("Mismatch for variable '%s' state %d: Hugin=%.8f, VE=%.8f",
                                variable.getName(), i, huginValues[i], veValues[i]));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ProbNet loadNetwork(String resourcePath) throws Exception {
        PGMXReader_0_2 reader = new PGMXReader_0_2();
        PGMXReader.NetworkAndEvidence info = reader.read(getClass().getClassLoader().getResource(resourcePath));
        return info.probNet();
    }

    private List<Variable> getChanceVariables(ProbNet probNet) {
        return probNet.getNodes(NodeType.CHANCE).stream()
                .map(Node::getVariable)
                .collect(Collectors.toList());
    }
}
