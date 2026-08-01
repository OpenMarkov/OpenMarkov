/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.tasks.TaskUtilities;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.potential.canonical.MinPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CataractNet through both projection paths. It is a real medical network — cataract surgery,
 * built by the group — and the only one at hand where canonical models carry most of the
 * quantitative part: 34 of its 65 nodes are ICI models, of the MAX and the MIN families. Keeping
 * their factorization must not move a single posterior.
 *
 * @author Manuel Arias
 */
public class CataractNetKeepsItsFactorizationTest {

    private static final String NETWORK = "networks/bn/BN-catarnet.pgmx";
    private static final double TOLERANCE = 1E-9;

    @Tag(TestSpeed.MEDIUM)
    @Test public void everyPosteriorIsTheSameWhicheverPathTheCanonicalModelsTake() throws Exception {
        HashMap<Variable, TablePotential> expanded = posteriors(false, null);
        HashMap<Variable, TablePotential> factorized = posteriors(true, null);

        assertSamePosteriors(expanded, factorized);
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void andTheSameUnderEvidenceOnAFindingAndOnADisease() throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(load().getVariable("tipo_catarata"), 1));
        evidence.addFinding(new Finding(load().getVariable("camara_estrecha"), 1));

        assertSamePosteriors(posteriors(false, evidence), posteriors(true, evidence));
    }

    /** Keeping the factorization is what the network is projected for, so it must cost less. */
    @Tag(TestSpeed.MEDIUM)
    @Test public void theFactorizedProjectionHoldsFewerValues() throws Exception {
        assertTrue(valuesInTheProjection(true) < valuesInTheProjection(false),
                   "the factorized projection of CataractNet was expected to be the smaller one");
    }

    /** What makes this network worth testing: most of its quantitative part is canonical. */
    @Tag(TestSpeed.FAST)
    @Test public void mostOfTheNetworkIsCanonical() throws Exception {
        ProbNet network = load();
        int canonical = 0, maxFamily = 0, minFamily = 0, largestFanIn = 0;
        for (Node node : network.getNodes()) {
            for (Potential potential : node.getPotentials()) {
                if (potential instanceof ICIPotential ici) {
                    canonical++;
                    largestFanIn = Math.max(largestFanIn, ici.getVariables().size() - 1);
                    if (ici instanceof MaxPotential) {
                        maxFamily++;
                    } else if (ici instanceof MinPotential) {
                        minFamily++;
                    }
                }
            }
        }
        assertEquals(34, canonical, "ICI models in CataractNet");
        assertEquals(27, maxFamily, "of the OR/MAX family");
        assertEquals(7, minFamily, "of the AND/MIN family");
        assertEquals(8, largestFanIn, "parents of the largest canonical family");
    }

    // ------------------------------------------------------------- helpers

    private static ProbNet load() throws Exception {
        PGMXReader.NetworkAndEvidence info = new PGMXReader_0_2()
                .read(CataractNetKeepsItsFactorizationTest.class.getClassLoader().getResource(NETWORK));
        return info.probNet();
    }

    private static HashMap<Variable, TablePotential> posteriors(boolean keepingTheFactorization,
            EvidenceCase evidence) throws Exception {
        ProbNet network = load();
        network.getInferenceOptions().setIciAwareVE(keepingTheFactorization);
        VEPropagation propagation = new VEPropagation(network);
        propagation.setVariablesOfInterest(network.getNodes(NodeType.CHANCE).stream()
                                                   .map(Node::getVariable).toList());
        if (evidence != null) {
            EvidenceCase onThisNetwork = new EvidenceCase();
            for (Finding finding : evidence.getFindings()) {
                onThisNetwork.addFinding(new Finding(network.getVariable(finding.getVariable().getName()),
                                                     finding.getStateIndex()));
            }
            propagation.setPostResolutionEvidence(onThisNetwork);
        }
        return propagation.getPosteriorValues();
    }

    private static long valuesInTheProjection(boolean keepingTheFactorization) throws Exception {
        ProbNet network = load();
        network.getInferenceOptions().setIciAwareVE(keepingTheFactorization);
        ProbNet markovNetwork =
                TaskUtilities.projectTablesAndBuildMarkovDecisionNetwork(network, new EvidenceCase());
        long values = 0;
        for (Potential potential : markovNetwork.getPotentials()) {
            values += ((TablePotential) potential).getValues().length;
        }
        return values;
    }

    private static void assertSamePosteriors(HashMap<Variable, TablePotential> expanded,
            HashMap<Variable, TablePotential> factorized) {
        assertEquals(expanded.size(), factorized.size(), "both paths must answer for the same variables");
        for (Variable variable : expanded.keySet()) {
            TablePotential fromTheFactors = factorized.entrySet().stream()
                    .filter(entry -> entry.getKey().getName().equals(variable.getName()))
                    .findFirst().orElseThrow(() -> new AssertionError(
                            "the factorized path answered nothing for " + variable.getName()))
                    .getValue();
            assertArrayEquals(expanded.get(variable).getValues(), fromTheFactors.getValues(), TOLERANCE,
                              "the posterior of " + variable.getName() + " changed with the factorization");
        }
    }
}
