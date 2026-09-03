package org.openmarkov.integrationTests.io;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A policy imposed on a decision is stored next to the network, in a Policies element. The
 * writers used to leave it out, so writing a network and reading it again gave back the
 * decisions with no policy.
 *
 * @author Manuel Arias
 */
class ImposedPoliciesSurviveARoundTripTest {

    @Tag(TestSpeed.FAST)
    @ParameterizedTest
    @ValueSource(strings = {
            "networks/id/ID-only-imposed-uniform-dec-no-util.pgmx",
            "networks/id/ID-only-imposed-decision-and-chance-no-utility.pgmx" })
    void theDecisionsKeepTheirPolicies(String network) throws Exception {
        ProbNet original = new PGMXReader().read(
                getClass().getClassLoader().getResource(network)).probNet();
        List<String> decisionsBefore = decisionsWithPolicy(original);
        assertFalse(decisionsBefore.isEmpty(), "the network must carry an imposed policy");

        Path written = Files.createTempDirectory("roundTrip").resolve("written.pgmx");
        new PGMXWriter_1_0().write(written.toString(), original, List.of());

        ProbNet reread = new PGMXReader().read(written.toUri().toURL()).probNet();
        assertEquals(decisionsBefore, decisionsWithPolicy(reread));
        assertEquals(original.getPotentials().size(), reread.getPotentials().size());
    }

    private static List<String> decisionsWithPolicy(ProbNet probNet) {
        List<String> names = new ArrayList<>();
        for (Node node : probNet.getNodes(NodeType.DECISION)) {
            if (node.hasPolicy()) {
                names.add(node.getName());
            }
        }
        names.sort(null);
        return names;
    }
}
