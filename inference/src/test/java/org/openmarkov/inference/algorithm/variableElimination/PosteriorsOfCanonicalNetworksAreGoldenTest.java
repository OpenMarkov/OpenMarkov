/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.potential.canonical.MinPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Posteriors of small networks with canonical (ICI) nodes, frozen as constants. The expected
 * values come from exact enumeration of the noisy-MAX/MIN definition, computed outside OpenMarkov,
 * so every inference path — expanding the model to its table or keeping its factorization — must
 * reproduce them.
 *
 * @author Manuel Arias
 */
public class PosteriorsOfCanonicalNetworksAreGoldenTest {

    private static final double TOLERANCE = 1E-9;

    // ------------------------------------------------------------- the four reference networks

    /** Y = noisy-MAX(A, B) with a leak; Y has three states, A two and B three. */
    private static ProbNet maxNetwork() {
        ProbNet network = network("Y", 3, "A", 2, "B", 3);
        setPrior(network, "A", 0.6, 0.4);
        setPrior(network, "B", 0.5, 0.3, 0.2);
        network.getNode(network.getVariable("Y")).setPotential(maxOverAAndB(network, "Y"));
        return network;
    }

    /** The same graph as {@link #maxNetwork()} with a noisy-MIN child. */
    private static ProbNet minNetwork() {
        ProbNet network = network("Y", 3, "A", 2, "B", 3);
        setPrior(network, "A", 0.6, 0.4);
        setPrior(network, "B", 0.5, 0.3, 0.2);
        MinPotential min = new MinPotential(List.of(network.getVariable("Y"), network.getVariable("A"),
                                                    network.getVariable("B")));
        min.setNoisyParameters(network.getVariable("A"), new double[]{0.7, 0.2, 0.1, 0.1, 0.3, 0.6});
        min.setNoisyParameters(network.getVariable("B"),
                               new double[]{0.5, 0.3, 0.2, 0.2, 0.5, 0.3, 0.1, 0.1, 0.8});
        min.setLeakyParameters(new double[]{0.05, 0.15, 0.8});
        network.getNode(network.getVariable("Y")).setPotential(min);
        return network;
    }

    /** Y = noisy-OR(X1, X2, X3) with a leak, everything binary. */
    private static ProbNet noisyOrNetwork() {
        ProbNet network = network("Y", 2, "X1", 2, "X2", 2, "X3", 2);
        setPrior(network, "X1", 0.7, 0.3);
        setPrior(network, "X2", 0.6, 0.4);
        setPrior(network, "X3", 0.5, 0.5);
        MaxPotential or = new MaxPotential(List.of(network.getVariable("Y"), network.getVariable("X1"),
                                                   network.getVariable("X2"), network.getVariable("X3")));
        or.setNoisyParameters(network.getVariable("X1"), new double[]{1.0, 0.0, 0.1, 0.9});
        or.setNoisyParameters(network.getVariable("X2"), new double[]{1.0, 0.0, 0.2, 0.8});
        or.setNoisyParameters(network.getVariable("X3"), new double[]{1.0, 0.0, 0.3, 0.7});
        or.setLeakyParameters(new double[]{0.95, 0.05});
        network.getNode(network.getVariable("Y")).setPotential(or);
        return network;
    }

    /** Y1 = noisy-MAX(A, B) and Y2 = noisy-MAX(A, Y1): two canonical nodes sharing the parent A. */
    private static ProbNet chainedNetwork() {
        ProbNet network = network("Y1", 3, "A", 2, "B", 3);
        Variable y2 = new Variable("Y2", 3);
        network.addNode(y2, NodeType.CHANCE);
        network.addLink(network.getNode(network.getVariable("A")), network.getNode(y2), true);
        network.addLink(network.getNode(network.getVariable("Y1")), network.getNode(y2), true);
        setPrior(network, "A", 0.6, 0.4);
        setPrior(network, "B", 0.5, 0.3, 0.2);
        MaxPotential first = maxOverAAndB(network, "Y1");
        network.getNode(network.getVariable("Y1")).setPotential(first);
        MaxPotential second = new MaxPotential(List.of(y2, network.getVariable("A"),
                                                       network.getVariable("Y1")));
        second.setNoisyParameters(network.getVariable("A"),
                                  new double[]{0.9, 0.08, 0.02, 0.3, 0.4, 0.3});
        second.setNoisyParameters(network.getVariable("Y1"),
                                  new double[]{1.0, 0.0, 0.0, 0.2, 0.7, 0.1, 0.05, 0.15, 0.8});
        second.setLeakyParameters(new double[]{0.85, 0.1, 0.05});
        network.getNode(y2).setPotential(second);
        return network;
    }

    // ------------------------------------------------------------- the golden values

    @Tag(TestSpeed.FAST)
    @ParameterizedTest(name = "keeping the factorization: {0}")
    @ValueSource(booleans = {false, true})
    public void theMarginalsOfAMaxNetworkMatchTheEnumeration(boolean keepingTheFactorization) throws Exception {
        HashMap<Variable, TablePotential> posteriors = posteriors(maxNetwork(), keepingTheFactorization);
        assertMarginal(posteriors, "A", 0.6, 0.4);
        assertMarginal(posteriors, "B", 0.5, 0.3, 0.2);
        assertMarginal(posteriors, "Y", 0.12144, 0.31081, 0.56775);
    }

    @Tag(TestSpeed.FAST)
    @ParameterizedTest(name = "keeping the factorization: {0}")
    @ValueSource(booleans = {false, true})
    public void theMarginalsOfAMinNetworkMatchTheEnumeration(boolean keepingTheFactorization) throws Exception {
        HashMap<Variable, TablePotential> posteriors = posteriors(minNetwork(), keepingTheFactorization);
        assertMarginal(posteriors, "Y", 0.65629, 0.25971, 0.084);
    }

    @Tag(TestSpeed.FAST)
    @ParameterizedTest(name = "keeping the factorization: {0}")
    @ValueSource(booleans = {false, true})
    public void theMarginalsOfANoisyOrNetworkMatchTheEnumeration(boolean keepingTheFactorization) throws Exception {
        HashMap<Variable, TablePotential> posteriors = posteriors(noisyOrNetwork(), keepingTheFactorization);
        assertMarginal(posteriors, "Y", 0.306527, 0.693473);
    }

    @Tag(TestSpeed.FAST)
    @ParameterizedTest(name = "keeping the factorization: {0}")
    @ValueSource(booleans = {false, true})
    public void theMarginalsOfTwoChainedMaxNodesWithASharedParentMatchTheEnumeration(boolean keepingTheFactorization)
            throws Exception {
        HashMap<Variable, TablePotential> posteriors = posteriors(chainedNetwork(), keepingTheFactorization);
        assertMarginal(posteriors, "Y1", 0.12144, 0.31081, 0.56775);
        assertMarginal(posteriors, "Y2", 0.1401052875, 0.2991406455, 0.560754067);
    }

    /** The same numbers again from the materialized table: P(y) = Σ CPT(y|a,b)·P(a)·P(b). */
    @Tag(TestSpeed.FAST)
    @Test public void theGoldenMarginalAlsoComesOutOfTheMaterializedTable() {
        ProbNet network = maxNetwork();
        double[] table = ((MaxPotential) network.getNode(network.getVariable("Y")).getPotentials()
                .getFirst()).getCPT().getValues();
        double[] priorA = {0.6, 0.4};
        double[] priorB = {0.5, 0.3, 0.2};
        double[] marginal = new double[3];
        for (int b = 0; b < 3; b++) {
            for (int a = 0; a < 2; a++) {
                for (int y = 0; y < 3; y++) {
                    marginal[y] += table[y + 3 * a + 6 * b] * priorA[a] * priorB[b];
                }
            }
        }
        assertArrayEquals(new double[]{0.12144, 0.31081, 0.56775}, marginal, TOLERANCE);
    }

    // ------------------------------------------------------------- the golden values, with evidence

    @Tag(TestSpeed.FAST)
    @ParameterizedTest(name = "keeping the factorization: {0}")
    @ValueSource(booleans = {false, true})
    public void evidenceOnAParentReachesTheChild(boolean keepingTheFactorization) throws Exception {
        ProbNet network = maxNetwork();
        HashMap<Variable, TablePotential> posteriors =
                posteriors(network, evidence(network, "A", 1), keepingTheFactorization);
        assertMarginal(posteriors, "Y", 0.0264, 0.2206, 0.753);
        assertMarginal(posteriors, "B", 0.5, 0.3, 0.2);
    }

    @Tag(TestSpeed.FAST)
    @ParameterizedTest(name = "keeping the factorization: {0}")
    @ValueSource(booleans = {false, true})
    public void evidenceOnTheChildReachesTheParents(boolean keepingTheFactorization) throws Exception {
        ProbNet network = maxNetwork();
        HashMap<Variable, TablePotential> posteriors =
                posteriors(network, evidence(network, "Y", 2), keepingTheFactorization);
        assertMarginal(posteriors, "A", 0.4694848084544252, 0.5305151915455747);
        assertMarginal(posteriors, "B", 0.4121532364597092, 0.2824306472919419, 0.3054161162483487);
    }

    @Tag(TestSpeed.FAST)
    @ParameterizedTest(name = "keeping the factorization: {0}")
    @ValueSource(booleans = {false, true})
    public void evidenceOnEveryParentLeavesOneColumnOfTheTable(boolean keepingTheFactorization) throws Exception {
        ProbNet network = maxNetwork();
        EvidenceCase evidence = evidence(network, "A", 1);
        evidence.addFinding(new Finding(network.getVariable("B"), 0));
        HashMap<Variable, TablePotential> posteriors = posteriors(network, evidence, keepingTheFactorization);
        assertMarginal(posteriors, "Y", 0.04, 0.264, 0.696);
    }

    @Tag(TestSpeed.FAST)
    @ParameterizedTest(name = "keeping the factorization: {0}")
    @ValueSource(booleans = {false, true})
    public void evidenceOnTheChildOfANoisyOrReachesItsParents(boolean keepingTheFactorization) throws Exception {
        ProbNet network = noisyOrNetwork();
        HashMap<Variable, TablePotential> posteriors =
                posteriors(network, evidence(network, "Y", 1), keepingTheFactorization);
        assertMarginal(posteriors, "X1", 0.5855599280721817, 0.4144400719278184);
    }

    @Tag(TestSpeed.FAST)
    @ParameterizedTest(name = "keeping the factorization: {0}")
    @ValueSource(booleans = {false, true})
    public void evidenceOnTheSecondChildTravelsThroughTheSharedParent(boolean keepingTheFactorization)
            throws Exception {
        ProbNet network = chainedNetwork();
        HashMap<Variable, TablePotential> posteriors =
                posteriors(network, evidence(network, "Y2", 2), keepingTheFactorization);
        assertMarginal(posteriors, "Y1", 0.019952276155315, 0.1275192837789976, 0.8525284400656874);
        assertMarginal(posteriors, "A", 0.4648164361864537, 0.5351835638135461);
    }

    /** The pseudo variables of the factorization are working material, not answers. */
    @Tag(TestSpeed.FAST)
    @Test public void thePseudoVariablesStayOutOfThePosteriors() throws Exception {
        HashMap<Variable, TablePotential> posteriors = posteriors(chainedNetwork(), true);

        assertTrue(posteriors.keySet().stream().noneMatch(variable -> variable.getName().startsWith("pseudo-")),
                   "a pseudo variable of the factorization leaked into the posteriors");
    }

    /** Below the default minimum of parents the switch changes nothing, including the answers. */
    @Tag(TestSpeed.FAST)
    @Test public void withTheDefaultMinimumOfParentsASmallModelAnswersTheSame() throws Exception {
        ProbNet network = maxNetwork();
        network.getInferenceOptions().setIciAwareVE(true);

        VEPropagation propagation = new VEPropagation(network);
        propagation.setVariablesOfInterest(network.getVariables());
        HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

        assertMarginal(posteriors, "Y", 0.12144, 0.31081, 0.56775);
    }

    // ------------------------------------------------------------- helpers

    /** A Bayesian network whose first named variable is the child of all the following ones. */
    private static ProbNet network(Object... namesAndNumbersOfStates) {
        ProbNet network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable child = null;
        for (int i = 0; i < namesAndNumbersOfStates.length; i += 2) {
            Variable variable = new Variable((String) namesAndNumbersOfStates[i],
                                             (Integer) namesAndNumbersOfStates[i + 1]);
            network.addNode(variable, NodeType.CHANCE);
            if (child == null) {
                child = variable;
            } else {
                network.addLink(network.getNode(variable), network.getNode(child), true);
            }
        }
        return network;
    }

    private static void setPrior(ProbNet network, String variableName, double... values) {
        Variable variable = network.getVariable(variableName);
        TablePotential prior = new TablePotential(List.of(variable), PotentialRole.CONDITIONAL_PROBABILITY);
        prior.setValues(values);
        network.getNode(variable).setPotential(prior);
    }

    /** The noisy-MAX of {@link #maxNetwork()}, shared with {@link #chainedNetwork()} for its Y1. */
    private static MaxPotential maxOverAAndB(ProbNet network, String childName) {
        Variable child = network.getVariable(childName);
        MaxPotential max = new MaxPotential(List.of(child, network.getVariable("A"),
                                                    network.getVariable("B")));
        max.setNoisyParameters(network.getVariable("A"), new double[]{0.7, 0.2, 0.1, 0.1, 0.3, 0.6});
        max.setNoisyParameters(network.getVariable("B"),
                               new double[]{0.5, 0.3, 0.2, 0.2, 0.5, 0.3, 0.1, 0.1, 0.8});
        max.setLeakyParameters(new double[]{0.8, 0.15, 0.05});
        return max;
    }

    private static HashMap<Variable, TablePotential> posteriors(ProbNet network,
            boolean keepingTheFactorization) throws Exception {
        return posteriors(network, null, keepingTheFactorization);
    }

    private static HashMap<Variable, TablePotential> posteriors(ProbNet network, EvidenceCase evidence,
            boolean keepingTheFactorization) throws Exception {
        if (keepingTheFactorization) {
            network.getInferenceOptions().setIciAwareVE(true);
            network.getInferenceOptions().setIciMinParentsToFactorize(0);
        }
        VEPropagation propagation = new VEPropagation(network);
        propagation.setVariablesOfInterest(network.getVariables());
        if (evidence != null) {
            propagation.setPostResolutionEvidence(evidence);
        }
        return propagation.getPosteriorValues();
    }

    private static EvidenceCase evidence(ProbNet network, String variableName, int state) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(network.getVariable(variableName), state));
        return evidence;
    }

    private static void assertMarginal(HashMap<Variable, TablePotential> posteriors, String variableName,
                                       double... expected) {
        TablePotential posterior = posteriors.entrySet().stream()
                .filter(entry -> entry.getKey().getName().equals(variableName))
                .findFirst().orElseThrow().getValue();
        assertArrayEquals(expected, posterior.getValues(), TOLERANCE,
                          "the marginal of " + variableName + " must match the enumeration");
    }
}
