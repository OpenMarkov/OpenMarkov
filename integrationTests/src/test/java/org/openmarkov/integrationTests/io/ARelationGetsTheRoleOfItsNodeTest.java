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
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The relation of a utility node has the role of a utility, and a policy imposed on a decision that of a policy,
 * whatever the format of the file and the role it writes. The types that the dialog of a node offers depend on it.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class ARelationGetsTheRoleOfItsNodeTest {

    /** The types that the dialog offers for a utility with parents that is not a super-value node. */
    private static final Set<String> TYPES_OF_A_UTILITY = Set.of(
            "DistributionTable", "Exact", "Exponential", "Function", "Linear combination", "Tree/ADD", "Uniform",
            "UnivariateDistr");

    private static ProbNet read(URL url) throws Exception {
        return new PGMXReader().read(url).probNet();
    }

    private static ProbNet network(String endOfPath) throws Exception {
        return read(Networks.getNetworks().filter(url -> url.getPath().endsWith(endOfPath)).findFirst().orElseThrow());
    }

    private static Set<String> typesOffered(Node node) throws Exception {
        Set<String> types = new TreeSet<>();
        for (Class<? extends Potential> type : PotentialUtils.getFilteredPotentialClasses(node)) {
            types.add(PotentialUtils.getPotentialName(type));
        }
        return types;
    }

    private static void assertEveryUtilityHasItsRole(ProbNet net) {
        List<Node> utilities = net.getNodes(NodeType.UTILITY);
        assertFalse(utilities.isEmpty());
        for (Node utility : utilities) {
            assertEquals(PotentialRole.UNSPECIFIED, utility.getPotential().getPotentialRole(), utility.getName());
        }
    }

    @Test void theUtilitiesOfAFileInFormat1() throws Exception {
        ProbNet net = network("/id/1-0/ID-decide-test-1-0.pgmx");

        assertEveryUtilityHasItsRole(net);
        assertEquals(TYPES_OF_A_UTILITY, typesOffered(net.getNode("Health state")));
    }

    @Test void theUtilitiesOfAFileInFormat02ThatGivesThemTheRoleOfAProbability() throws Exception {
        assertEveryUtilityHasItsRole(network("/DAN-CE-3-test-problem.pgmx"));
    }

    @Test void theUtilitiesKeepTheirRoleWhenSavedInEitherFormat() throws Exception {
        ProbNet net = network("/id/ID-decide-test.pgmx");
        Path in1 = Files.createTempFile("format-1-0", ".pgmx");
        new PGMXWriter_1_0().write(in1.toString(), net, List.of());
        Path in02 = Files.createTempFile("format-0-2", ".pgmx");
        new PGMXWriter_0_2().write(in02.toString(), read(in1.toUri().toURL()), List.of());

        assertEveryUtilityHasItsRole(read(in02.toUri().toURL()));
    }

    @Test void aPolicyImposedThatIsSavedInFormat1() throws Exception {
        ProbNet net = read(getClass().getClassLoader().getResource("networks/mid/21-gene.pgmx"));
        assertEquals(PotentialRole.POLICY, net.getNode("Dec: 21g").getPotential().getPotentialRole());
        Path written = Files.createTempFile("format-1-0", ".pgmx");
        new PGMXWriter_1_0().write(written.toString(), net, List.of());

        Node decision = read(written.toUri().toURL()).getNode("Dec: 21g");

        assertEquals(PotentialRole.POLICY, decision.getPotential().getPotentialRole());
    }

    @Test void theRelationThatTheEditorGivesToANewUtility() throws Exception {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Node therapy = net.addNode(new Variable("Therapy", "no", "yes"), NodeType.DECISION);
        Node utility = net.addNode(new Variable("U"), NodeType.UTILITY);
        net.addLink(therapy, utility, true);

        utility.setPotential(PotentialUtils.generateDefaultPotential(utility));

        assertEquals(PotentialRole.UNSPECIFIED, utility.getPotential().getPotentialRole());
        assertEquals(TYPES_OF_A_UTILITY, typesOffered(utility));
    }

    @Test void aNewUtilityWithoutParentsIsNotOfferedATree() throws Exception {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Node utility = net.addNode(new Variable("U"), NodeType.UTILITY);

        utility.setPotential(PotentialUtils.generateDefaultPotential(utility));

        assertFalse(typesOffered(utility).contains("Tree/ADD"));
    }
}
