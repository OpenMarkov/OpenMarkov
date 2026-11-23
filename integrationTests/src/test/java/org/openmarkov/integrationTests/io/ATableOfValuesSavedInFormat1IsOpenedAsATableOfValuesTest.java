/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.modelUncertainty.SystematicSampling;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Format 1.0 writes a table of values as an "Exact" distribution with numbers. It is opened as the table of
 * values it was, with its uncertainty and its comment, whatever its parents and the type of its network.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class ATableOfValuesSavedInFormat1IsOpenedAsATableOfValuesTest {

    private static ProbNet read(URL url) throws Exception {
        return new PGMXReader().read(url).probNet();
    }

    private static ProbNet network(String endOfPath) throws Exception {
        return read(Networks.getNetworks().filter(url -> url.getPath().endsWith(endOfPath)).findFirst().orElseThrow());
    }

    private static ProbNet savedInFormat1AndOpened(ProbNet net) throws Exception {
        Path written = Files.createTempFile("format-1-0", ".pgmx");
        new PGMXWriter_1_0().write(written.toString(), net, List.of());
        return read(written.toUri().toURL());
    }

    private static ProbNet savedInFormat02AndOpened(ProbNet net) throws Exception {
        Path written = Files.createTempFile("format-0-2", ".pgmx");
        new PGMXWriter_0_2().write(written.toString(), net, List.of());
        return read(written.toUri().toURL());
    }

    private static int uncertainParameters(ProbNet net) {
        return SystematicSampling.getUncertainParameters(net).size();
    }

    /** The comment of each relation that has one, by the name of its first variable. */
    private static Map<String, String> comments(ProbNet net) {
        Map<String, String> comments = new TreeMap<>();
        for (Node node : net.getNodes()) {
            for (Potential potential : node.getPotentials()) {
                if (potential.getComment() != null && !potential.getComment().isBlank()) {
                    comments.put(node.getName(), potential.getComment());
                }
            }
        }
        return comments;
    }

    @Test void theTablesOfUtilitiesKeepTheirUncertainty() throws Exception {
        ProbNet original = network("/id/ID-decide-test.pgmx");
        assertEquals(13, uncertainParameters(original));

        ProbNet once = savedInFormat1AndOpened(original);
        assertEquals(13, uncertainParameters(once));
        assertEquals(13, uncertainParameters(savedInFormat1AndOpened(once)));
    }

    @Test void theOfficialCopyInFormat1HasTheUncertaintyOfTheOneInFormat02() throws Exception {
        assertEquals(uncertainParameters(network("/id/ID-decide-test.pgmx")),
                     uncertainParameters(network("/id/1-0/ID-decide-test-1-0.pgmx")));
    }

    @Test void noRelationOfAUtilityIsOpenedAsADistribution() throws Exception {
        ProbNet net = network("/id/1-0/ID-decide-test-1-0.pgmx");
        List<Node> utilities = net.getNodes(NodeType.UTILITY);
        assertFalse(utilities.isEmpty());
        for (Node utility : utilities) {
            assertFalse(utility.getPotentials().getFirst() instanceof UnivariateDistrPotential, utility.getName());
        }
    }

    @Test void theCommentsOfTheRelationsReachFormat02() throws Exception {
        ProbNet net = network("/mid/1-0/MID-Cochlear-1-0.pgmx");
        Map<String, String> comments = comments(net);
        assertTrue(comments.containsKey("CS: Surgery"), comments.keySet().toString());

        assertEquals(comments, comments(savedInFormat02AndOpened(net)));
    }

    @Test void aTableOfUtilitiesWithANumericParentIsEvaluated() throws Exception {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable treat = new Variable("Treat", "no", "yes");
        Variable age = new Variable("Age");
        age.setVariableType(VariableType.NUMERIC);
        Variable cost = new Variable("Cost");
        cost.setDecisionCriterion(net.getDecisionCriteria().getFirst());
        Node treatNode = net.addNode(treat, NodeType.DECISION);
        Node ageNode = net.addNode(age, NodeType.CHANCE);
        Node costNode = net.addNode(cost, NodeType.UTILITY);
        net.addLink(treatNode, costNode, true);
        net.addLink(ageNode, costNode, true);
        ageNode.setPotential(new DeltaPotential(List.of(age), PotentialRole.CONDITIONAL_PROBABILITY, 60));
        costNode.setPotential(new ExactDistrPotential(List.of(cost, treat, age), PotentialRole.UNSPECIFIED,
                                                      new double[] { -100, -2500 }));
        assertEquals(-100, new VEEvaluation(net).getUtility().getValues()[0], 1E-9);

        ProbNet opened = savedInFormat1AndOpened(net);

        assertInstanceOf(ExactDistrPotential.class, opened.getNode("Cost").getPotentials().getFirst());
        assertEquals(-100, new VEEvaluation(opened).getUtility().getValues()[0], 1E-9);
        assertEquals(-100, new VEEvaluation(savedInFormat1AndOpened(opened)).getUtility().getValues()[0], 1E-9);
    }

    @Test void aNumericChanceNodeOfANetworkOfEventsKeepsItsValue() throws Exception {
        ProbNet net = new ProbNet(DESNetworkType.getUniqueInstance());
        Variable dose = new Variable("Dose");
        dose.setVariableType(VariableType.NUMERIC);
        net.addNode(dose, NodeType.CHANCE).setPotential(
                new ExactDistrPotential(List.of(dose), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 7.5 }));

        ProbNet opened = savedInFormat1AndOpened(net);

        Potential relation = opened.getNode("Dose").getPotentials().getFirst();
        assertInstanceOf(ExactDistrPotential.class, relation);
        assertEquals(7.5, ((ExactDistrPotential) relation).getValues()[0], 0.0);
        assertInstanceOf(ExactDistrPotential.class,
                         savedInFormat1AndOpened(opened).getNode("Dose").getPotentials().getFirst());
    }
}
