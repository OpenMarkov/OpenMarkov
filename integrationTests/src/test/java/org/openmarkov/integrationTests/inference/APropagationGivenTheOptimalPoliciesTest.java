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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.inference.InferenceProgress;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A network is propagated, a finding is set on a state that was possible but not certain, and it is propagated
 * again, once computing the optimal policies and once with the ones the first propagation gave.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class APropagationGivenTheOptimalPoliciesTest {

    private static VEPropagation propagation(ProbNet probNet, VEPropagation givesThePolicies, EvidenceCase findings)
            throws Exception {
        VEPropagation propagation = givesThePolicies == null ? new VEPropagation(probNet)
                : new VEPropagation(probNet, givesThePolicies.getOptimalPolicies());
        propagation.setVariablesOfInterest(probNet.getVariables());
        propagation.setPreResolutionEvidence(new EvidenceCase());
        propagation.setPostResolutionEvidence(findings);
        return propagation;
    }

    private static Finding aStateThatIsPossibleButNotCertain(ProbNet probNet, Map<Variable, TablePotential> values) {
        for (Node node : probNet.getNodes(NodeType.CHANCE)) {
            TablePotential probabilities = values.get(node.getVariable());
            if (node.getVariable().getVariableType() == VariableType.FINITE_STATES && probabilities != null) {
                double[] ofEachState = probabilities.getValues();
                for (int state = 0; state < ofEachState.length; state++) {
                    if (ofEachState[state] > 0.05 && ofEachState[state] < 0.95) {
                        return new Finding(node.getVariable(), state);
                    }
                }
            }
        }
        throw new IllegalStateException("no such state in the network");
    }

    private static Map<String, double[]> byName(Map<Variable, TablePotential> values) {
        Map<String, double[]> byName = new TreeMap<>();
        values.forEach((variable, potential) -> byName.put(variable.getName(), potential.getValues()));
        return byName;
    }

    @ParameterizedTest
    @ValueSource(strings = { "/mid/MID-Chancellor.pgmx", "/mid/MID-hip-Briggs.pgmx", "/id/ID-decide-test.pgmx",
            "/id/ID-arthronet-ce.pgmx", "/dan/DAN-reactor.pgmx" })
    void givesTheSameValuesWithoutComputingThemAgain(String network) throws Exception {
        ProbNet probNet = new PGMXReader().read(
                Networks.getNetworks().filter(url -> url.getPath().endsWith(network)).findFirst().orElseThrow())
                                          .probNet();
        VEPropagation first = propagation(probNet, null, new EvidenceCase());
        Map<Variable, TablePotential> withoutFindings = first.getPosteriorValues();
        EvidenceCase findings = new EvidenceCase();
        findings.addFinding(aStateThatIsPossibleButNotCertain(probNet, withoutFindings));

        Map<String, double[]> computingThem = byName(propagation(probNet, null, findings).getPosteriorValues());
        VEPropagation given = propagation(probNet, first, findings);
        Map<String, double[]> givenThem = byName(given.getPosteriorValues());

        assertEquals(computingThem.keySet(), givenThem.keySet());
        for (String variable : computingThem.keySet()) {
            assertArrayEquals(computingThem.get(variable), givenThem.get(variable), 1E-12, variable);
        }
        assertEquals(InferenceProgress.Stage.PROPAGATION, given.getProgress().getStage());
    }

    /**
     * A disease, a therapy and a utility of 10 and 2 without the therapy (healthy and ill) and 8 and 6 with it. The
     * patient is ill with probability 0.3, so the best is not to treat (7.6 against 7.4).
     */
    @Test void usesThePoliciesItIsGiven() throws Exception {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable disease = new Variable("Disease", "absent", "present");
        Variable therapy = new Variable("Therapy", "no", "yes");
        Variable utility = new Variable("Utility");
        Node diseaseNode = probNet.addNode(disease, NodeType.CHANCE);
        Node therapyNode = probNet.addNode(therapy, NodeType.DECISION);
        Node utilityNode = probNet.addNode(utility, NodeType.UTILITY);
        utility.setDecisionCriterion(probNet.getDecisionCriteria().getFirst());
        probNet.addLink(diseaseNode, utilityNode, true);
        probNet.addLink(therapyNode, utilityNode, true);
        diseaseNode.setPotential(new TablePotential(List.of(disease), PotentialRole.CONDITIONAL_PROBABILITY,
                                                    new double[]{ 0.7, 0.3 }));
        utilityNode.setPotential(new ExactDistrPotential(List.of(utility, disease, therapy), PotentialRole.UNSPECIFIED,
                                                         new double[]{ 10, 2, 8, 6 }));
        HashMap<Variable, Potential> alwaysTreat = new HashMap<>();
        alwaysTreat.put(therapy, new TablePotential(List.of(therapy), PotentialRole.POLICY, new double[]{ 0, 1 }));
        VEPropagation propagation = new VEPropagation(probNet, alwaysTreat);
        propagation.setVariablesOfInterest(probNet.getVariables());
        propagation.setPreResolutionEvidence(new EvidenceCase());
        propagation.setPostResolutionEvidence(new EvidenceCase());

        Map<String, double[]> values = byName(propagation.getPosteriorValues());

        assertArrayEquals(new double[]{ 0, 1 }, values.get("Therapy"), 1E-12);
        assertArrayEquals(new double[]{ 7.4 }, values.get("Utility"), 1E-9);
    }
}
