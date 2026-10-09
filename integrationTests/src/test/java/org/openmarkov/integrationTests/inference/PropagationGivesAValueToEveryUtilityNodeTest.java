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
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.SumPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Influence diagrams whose utility nodes are added or multiplied by other utility nodes. Inference mode asks the
 * propagation for every variable of the network.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class PropagationGivesAValueToEveryUtilityNodeTest {

    private static ProbNet read(String fileName) throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/networks/id/" + fileName))
                          .findFirst()
                          .orElseThrow();
        return new PGMXReader().read(url).probNet();
    }

    private static Map<Variable, TablePotential> valuesOfEveryVariable(ProbNet probNet, EvidenceCase found)
            throws Exception {
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(probNet.getVariables());
        propagation.setPreResolutionEvidence(new EvidenceCase());
        propagation.setPostResolutionEvidence(found);
        return propagation.getPosteriorValues();
    }

    private static double value(Map<Variable, TablePotential> values, Node node) {
        TablePotential value = values.get(node.getVariable());
        assertNotNull(value, node.getName());
        return value.getValues()[0];
    }

    /** Every utility node has a value, and a node that adds others has the sum of theirs. */
    private static void assertTheValuesAddUp(ProbNet probNet, Map<Variable, TablePotential> values) {
        for (Node node : probNet.getNodes(NodeType.UTILITY)) {
            double itsValue = value(values, node);
            if (node.getPotential() instanceof SumPotential) {
                double sum = node.getParents().stream().mapToDouble(parent -> value(values, parent)).sum();
                assertEquals(sum, itsValue, 1E-9 * Math.max(1, Math.abs(sum)), node.getName());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "ID-arthronet.pgmx", "ID-mediastinet.pgmx", "ID-used-car-buyer.pgmx",
            "1-0/ID-mediastinet-1-0.pgmx" })
    void withNothingFound(String fileName) throws Exception {
        ProbNet probNet = read(fileName);

        Map<Variable, TablePotential> values = valuesOfEveryVariable(probNet, new EvidenceCase());

        assertTheValuesAddUp(probNet, values);
        Node last = probNet.getNodes(NodeType.UTILITY).stream()
                           .filter(node -> node.getChildren().isEmpty()).findFirst().orElseThrow();
        assertEquals(new VEEvaluation(read(fileName)).getUtility().getValues()[0], value(values, last), 1E-9);
    }

    @Test void withTheConditionOfTheCarFound() throws Exception {
        ProbNet probNet = read("ID-used-car-buyer.pgmx");
        EvidenceCase found = new EvidenceCase();
        found.addFinding(new Finding(probNet.getVariable("Car's Condition"), 1));

        assertTheValuesAddUp(probNet, valuesOfEveryVariable(probNet, found));
    }

    /** ID-arthronet-ce is the same model without the nodes that add and multiply. */
    @Test void theValuesAreThoseOfTheSameModelWithoutNodesThatAddOrMultiply() throws Exception {
        ProbNet probNet = read("ID-arthronet.pgmx");
        ProbNet twin = read("ID-arthronet-ce.pgmx");
        Map<Variable, TablePotential> values = valuesOfEveryVariable(probNet, new EvidenceCase());
        Map<Variable, TablePotential> valuesOfTheTwin = valuesOfEveryVariable(twin, new EvidenceCase());

        int compared = 0;
        for (Node node : twin.getNodes(NodeType.UTILITY)) {
            Node sameNode = probNet.getNode(node.getName());
            if (sameNode != null) {
                assertEquals(value(valuesOfTheTwin, node), value(values, sameNode), 1E-9, node.getName());
                compared++;
            }
        }
        assertTrue(compared >= 8, "nodes compared: " + compared);
    }

    /** Disease -> N, where N is a numeric chance node that nobody has observed: 3 without the disease, 7 with it. */
    @Test void aNumericChanceNodeWithoutFindingDoesNotStopTheOthers() throws Exception {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable disease = new Variable("Disease", "absent", "present");
        Variable number = new Variable("N");
        Node diseaseNode = probNet.addNode(disease, NodeType.CHANCE);
        Node numberNode = probNet.addNode(number, NodeType.CHANCE);
        probNet.addLink(diseaseNode, numberNode, true);
        diseaseNode.setPotential(new TablePotential(List.of(disease), PotentialRole.CONDITIONAL_PROBABILITY,
                                                    new double[]{ 0.9, 0.1 }));
        numberNode.setPotential(new ExactDistrPotential(List.of(number, disease),
                                                        PotentialRole.CONDITIONAL_PROBABILITY, new double[]{ 3, 7 }));

        Map<Variable, TablePotential> values = valuesOfEveryVariable(probNet, new EvidenceCase());

        assertArrayEquals(new double[]{ 0.9, 0.1 }, values.get(disease).getValues(), 1E-9);
    }
}
