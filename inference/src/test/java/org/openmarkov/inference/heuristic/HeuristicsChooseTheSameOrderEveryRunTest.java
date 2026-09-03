package org.openmarkov.inference.heuristic;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.RemoveNodeEdit;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.heuristic.canoAndMoral.CanoMoralElimination;
import org.openmarkov.inference.heuristic.rollout.RolloutElimination;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.inference.testutils.TestNetworks;
import java.util.function.Supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The same query on the same network must be eliminated in the same order every time the
 * program runs. Each run builds the network again, so its nodes are new objects with new
 * identity hash codes; a heuristic that walks a hash-ordered collection gives itself away.
 *
 * @author Manuel Arias
 */
class HeuristicsChooseTheSameOrderEveryRunTest {

    private static final int RUNS = 20;

    @Tag(TestSpeed.FAST)
    @Test void rolloutAnswersTheSameOrder() {
        assertSameOrderEveryRun(RolloutElimination::new, TestNetworks::buildAsia);
        assertSameOrderEveryRun(RolloutElimination::new, () -> moralize(buildGrid(3, 3)));
    }

    @Tag(TestSpeed.FAST)
    @Test void canoAndMoralAnswersTheSameOrder() {
        assertSameOrderEveryRun(CanoMoralElimination::new, TestNetworks::buildAsia);
        assertSameOrderEveryRun(CanoMoralElimination::new, () -> moralize(buildGrid(3, 3)));
    }

    private static void assertSameOrderEveryRun(
            BiFunction<ProbNet, List<List<Variable>>, EliminationHeuristic> heuristic,
            Supplier<ProbNet> network) {
        List<String> first = null;
        for (int run = 0; run < RUNS; run++) {
            ProbNet net = network.get();
            List<List<Variable>> toEliminate = new ArrayList<>();
            toEliminate.add(new ArrayList<>(net.getVariables()));
            List<String> order = drain(net, heuristic.apply(net, toEliminate));
            if (first == null) {
                first = order;
            } else {
                assertEquals(first, order, "run " + run);
            }
        }
    }

    private static ProbNet buildGrid(int rows, int cols) {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("grid");
        Node[][] nodes = new Node[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                nodes[r][c] = net.addNode(new Variable("G" + r + "_" + c, "s0", "s1"), NodeType.CHANCE);
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r + 1 < rows) net.addLink(nodes[r][c], nodes[r + 1][c], true);
                if (c + 1 < cols) net.addLink(nodes[r][c], nodes[r][c + 1], true);
            }
        }
        return net;
    }

    private static ProbNet moralize(ProbNet net) {
        for (Node node : new ArrayList<>(net.getNodes())) {
            List<Node> parents = node.getParents();
            for (int i = 0; i < parents.size() - 1; i++) {
                for (int j = i + 1; j < parents.size(); j++) {
                    if (!parents.get(i).isNeighbor(parents.get(j))) {
                        net.addLink(parents.get(i), parents.get(j), false);
                    }
                }
            }
        }
        return net;
    }

    private static List<String> drain(ProbNet net, EliminationHeuristic heuristic) {
        List<String> order = new ArrayList<>();
        Variable variable = heuristic.getVariableToDelete();
        while (variable != null) {
            order.add(variable.getName());
            heuristic.afterEditExecutes(new RemoveNodeEdit(net, net.getNode(variable)));
            variable = heuristic.getVariableToDelete();
        }
        return order;
    }
}
