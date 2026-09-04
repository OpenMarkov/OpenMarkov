package org.openmarkov.inference.heuristic.hybridElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The size of a clique is the product of the number of states of its variables, and it is
 * answered as a {@code long}. A clique of more than two thousand million combinations used
 * to come back negative, and the heuristic keeps the smallest clique, so the largest one
 * looked like the best.
 *
 * @author Manuel Arias
 */
class CliqueSizeIsCountedIn64BitsTest {

    @Tag(TestSpeed.FAST)
    @Test void aCliqueTooBigForAnIntIsCountedAnyway() {
        assertEquals(1L << 31, HybridElimination.cliqueSize(binaryNodes(31)));
    }

    @Tag(TestSpeed.FAST)
    @Test void aSmallCliqueIsStillCountedRight() {
        assertEquals(8L, HybridElimination.cliqueSize(binaryNodes(3)));
    }

    private static List<Node> binaryNodes(int howMany) {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            nodes.add(net.addNode(new Variable("V" + i, "yes", "no"), NodeType.CHANCE));
        }
        return nodes;
    }
}
