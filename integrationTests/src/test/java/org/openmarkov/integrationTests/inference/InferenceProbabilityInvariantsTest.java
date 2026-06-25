/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sanity invariant tests for Variable Elimination inference over Bayesian Networks.
 *
 * <p>These tests complement the exact-value golden-output tests (e.g.
 * {@code bnCatarnetTests}, {@code bnHeparTests}) by checking mathematical
 * validity properties for <em>every</em> variable in each network, not just
 * the 2–3 variables that golden tests cover.  They are designed to catch
 * regressions from the Rediseño-Opción-3 refactoring, where a subtle change
 * to index-offset calculations in {@code TablePotential.tableProject} or
 * {@code DiscretePotentialOperations.multiplyAndMarginalize} would produce
 * numerically wrong posteriors without throwing any exception.</p>
 *
 * <h3>Invariants checked for each variable X in the network</h3>
 * <ol>
 *   <li>P(X=i | e) ≥ 0 for all states i (no negative probabilities).</li>
 *   <li>∑_i P(X=i | e) ≈ 1.0 (posterior sums to 1).</li>
 *   <li>No NaN or Infinity values in the posterior.</li>
 * </ol>
 *
 * <h3>Networks tested</h3>
 * <ul>
 *   <li>BN-catarnet  — small, 5 nodes</li>
 *   <li>BN-two-diseases — small, 7 nodes</li>
 *   <li>BN-hepar     — large, 70 nodes</li>
 * </ul>
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InferenceProbabilityInvariantsTest {

    /** Tolerance for the sum-to-one check. */
    private static final double SUM_DELTA = 1.0e-4;

    // -----------------------------------------------------------------------
    // Parametrised test: all chance variables have valid posteriors (no evidence)
    // -----------------------------------------------------------------------

    /**
     * For each listed BN, running VEPropagation without evidence and requesting
     * posteriors for ALL chance variables must yield valid probability distributions.
     */
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
        "networks/bn/BN-catarnet.pgmx",
        "networks/bn/BN-two-diseases.pgmx",
        "networks/bn/BN-hepar.pgmx"
    })
    public void vePropagation_noEvidence_allVariablesHaveValidPosteriors(String resourcePath)
            throws Exception {
        ProbNet probNet = loadNetwork(resourcePath);

        List<Variable> chanceVars = probNet.getNodes(NodeType.CHANCE).stream()
                .map(n -> n.getVariable())
                .collect(Collectors.toList());

        assertFalse(chanceVars.isEmpty(), "Network must have at least one chance variable");

        VEPropagation vePropagation = new VEPropagation(probNet);
        vePropagation.setVariablesOfInterest(chanceVars);
        vePropagation.setPostResolutionEvidence(new EvidenceCase());

        HashMap<Variable, TablePotential> posteriors = vePropagation.getPosteriorValues();

        for (Variable variable : chanceVars) {
            TablePotential posterior = posteriors.get(variable);
            assertNotNull(posterior, "Posterior must be computed for variable: " + variable.getName());
            assertValidDistribution(posterior.getValues(), variable.getName());
        }
    }

    // -----------------------------------------------------------------------
    // Hepar with evidence — all variables remain valid
    // -----------------------------------------------------------------------

    /**
     * VEPropagation on hepar with three evidence findings must still yield
     * valid distributions for ALL chance variables.
     *
     * <p>The three findings (alt=normal, ascites=no, carcinoma=no) cover different
     * parts of the graph, exercising multiple paths through
     * {@code multiplyAndMarginalize}.</p>
     */
    @Test
    public void vePropagation_heparWithEvidence_allVariablesHaveValidPosteriors()
            throws Exception {
        ProbNet probNet = loadNetwork("networks/bn/BN-hepar.pgmx");

        List<Variable> chanceVars = probNet.getNodes(NodeType.CHANCE).stream()
                .map(n -> n.getVariable())
                .collect(Collectors.toList());

        // Build evidence: three observed variables
        EvidenceCase evidence = new EvidenceCase();
        addFindingIfPresent(evidence, probNet, "alt",       0);  // alt = normal
        addFindingIfPresent(evidence, probNet, "ascites",   0);  // ascites = no
        addFindingIfPresent(evidence, probNet, "carcinoma", 0);  // carcinoma = no

        // Variables of interest: all unobserved chance variables
        List<Variable> observed = evidence.getFindings().stream()
                .map(Finding::getVariable)
                .collect(Collectors.toList());
        List<Variable> variablesOfInterest = chanceVars.stream()
                .filter(v -> !observed.contains(v))
                .collect(Collectors.toList());

        VEPropagation vePropagation = new VEPropagation(probNet);
        vePropagation.setVariablesOfInterest(variablesOfInterest);
        vePropagation.setPostResolutionEvidence(evidence);

        HashMap<Variable, TablePotential> posteriors = vePropagation.getPosteriorValues();

        for (Variable variable : variablesOfInterest) {
            TablePotential posterior = posteriors.get(variable);
            assertNotNull(posterior, "Posterior missing for: " + variable.getName());
            assertValidDistribution(posterior.getValues(), variable.getName());
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

    /**
     * Adds a finding to {@code evidence} if the variable exists in the network
     * and the state index is valid.  Silently skips if not found (so tests are
     * robust to network-structure changes).
     */
    private void addFindingIfPresent(EvidenceCase evidence, ProbNet probNet,
                                     String variableName, int stateIndex) {
        try {
            Variable v = probNet.getVariable(variableName);
            if (v != null && stateIndex < v.getNumStates()) {
                evidence.addFinding(new Finding(v, stateIndex));
            }
        } catch (Exception ignored) {
            // Variable not in this network — skip silently
        }
    }

    /**
     * Asserts that {@code values} forms a valid discrete probability distribution:
     * <ul>
     *   <li>No NaN or Infinity.</li>
     *   <li>All values ≥ 0.</li>
     *   <li>Sum ≈ 1.0 (within {@link #SUM_DELTA}).</li>
     * </ul>
     */
    private void assertValidDistribution(double[] values, String variableName) {
        assertNotNull(values, "Posterior values array is null for: " + variableName);
        assertTrue(values.length > 0, "Posterior must have at least one state: " + variableName);

        double sum = 0.0;
        for (double v : values) {
            assertFalse(Double.isNaN(v),
                    String.format("NaN posterior value for variable '%s'", variableName));
            assertFalse(Double.isInfinite(v),
                    String.format("Infinite posterior value for variable '%s'", variableName));
            assertTrue(v >= -SUM_DELTA,
                    String.format("Negative posterior %.6f for variable '%s'", v, variableName));
            sum += v;
        }
        assertEquals(1.0, sum, SUM_DELTA,
                String.format("Posterior for '%s' sums to %.8f instead of 1.0", variableName, sum));
    }
}
