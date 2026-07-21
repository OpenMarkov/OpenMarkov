/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.heuristic.rollout;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.RemoveNodeEdit;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RolloutElimination}, focusing on the depth=0 degenerate case.
 *
 * @author Manuel Arias
 */
public class RolloutEliminationTest {

    // -------------------------------------------------------------------------
    // depth=0: criterion must be irrelevant
    // -------------------------------------------------------------------------

    /**
     * With depth=0, only the cost function is used — the criterion plays no role.
     * Rollout(0, X, JTS) must produce exactly the same elimination order as
     * Rollout(0, X, MCT) for any cost function X and any network.
     */
    @Test
    public void depthZeroCriterionIsIrrelevant() {
        for (ProbNet net : testNetworks()) {
            for (RolloutCostFunction cf : RolloutCostFunction.values()) {
                List<String> orderJTS = eliminationOrder(net.copy(),
                        0, 3, cf, RolloutCriterion.JUNCTION_TREE_SUM);
                List<String> orderMCT = eliminationOrder(net.copy(),
                        0, 3, cf, RolloutCriterion.MAX_CLIQUE_TABLE_SIZE);
                assertEquals(orderJTS, orderMCT,
                        "depth=0 with " + cf + " on " + net.getName()
                                + ": criterion should not affect the order");
            }
        }
    }

    // -------------------------------------------------------------------------
    // depth=0 must differ from depth=1 on non-trivial networks
    // -------------------------------------------------------------------------

    /**
     * On a non-trivial network, depth=1 should (often) produce a different order
     * than depth=0, because it evaluates full greedy completions. If both orders
     * were always identical, depth=0 would still be running the greedy simulation
     * (the bug we fixed).
     */
    @Test
    public void depthZeroDiffersFromDepthOne() {
        ProbNet net = moralize(buildGrid(4, 4));
        List<String> order0 = eliminationOrder(net.copy(),
                0, 3, RolloutCostFunction.WEIGHTED_MIN_FILL, RolloutCriterion.JUNCTION_TREE_SUM);
        List<String> order1 = eliminationOrder(net.copy(),
                1, 3, RolloutCostFunction.WEIGHTED_MIN_FILL, RolloutCriterion.JUNCTION_TREE_SUM);
        assertNotEquals(order0, order1,
                "On a 4x4 grid, depth=0 and depth=1 should produce different orders");
    }

    // -------------------------------------------------------------------------
    // depth=0 must be fast (no simulation)
    // -------------------------------------------------------------------------

    /**
     * With depth=0, no greedy completion is performed, so it should be
     * significantly faster than depth=1 on a large enough network.
     */
    @Test
    public void depthZeroIsFasterThanDepthOne() {
        ProbNet net = moralize(buildGrid(6, 6)); // 36 nodes

        // Warm up
        eliminationOrder(net.copy(), 0, 3,
                RolloutCostFunction.WEIGHTED_MIN_FILL, RolloutCriterion.JUNCTION_TREE_SUM);
        eliminationOrder(net.copy(), 1, 3,
                RolloutCostFunction.WEIGHTED_MIN_FILL, RolloutCriterion.JUNCTION_TREE_SUM);

        long t0 = System.nanoTime();
        for (int i = 0; i < 5; i++) {
            eliminationOrder(net.copy(), 0, 3,
                    RolloutCostFunction.WEIGHTED_MIN_FILL, RolloutCriterion.JUNCTION_TREE_SUM);
        }
        long elapsed0 = System.nanoTime() - t0;

        long t1 = System.nanoTime();
        for (int i = 0; i < 5; i++) {
            eliminationOrder(net.copy(), 1, 3,
                    RolloutCostFunction.WEIGHTED_MIN_FILL, RolloutCriterion.JUNCTION_TREE_SUM);
        }
        long elapsed1 = System.nanoTime() - t1;

        assertTrue(elapsed0 < elapsed1,
                "depth=0 (" + elapsed0 / 1_000_000 + "ms) should be faster than depth=1 ("
                        + elapsed1 / 1_000_000 + "ms)");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static List<String> eliminationOrder(
            ProbNet net,
            int depth, int beam,
            RolloutCostFunction costFn, RolloutCriterion criterion) {
        List<List<Variable>> variablesToEliminate = new ArrayList<>();
        variablesToEliminate.add(new ArrayList<>(net.getVariables()));
        EliminationHeuristic heuristic = new RolloutElimination(
                net, variablesToEliminate, depth, beam, costFn, criterion);
        return drain(net, heuristic);
    }

    private static List<String> eliminationOrder(
            ProbNet net,
            BiFunction<ProbNet, List<List<Variable>>, EliminationHeuristic> factory) {
        List<List<Variable>> variablesToEliminate = new ArrayList<>();
        variablesToEliminate.add(new ArrayList<>(net.getVariables()));
        EliminationHeuristic heuristic = factory.apply(net, variablesToEliminate);
        return drain(net, heuristic);
    }

    private static List<String> drain(ProbNet net, EliminationHeuristic heuristic) {
        List<String> order = new ArrayList<>();
        Variable v = heuristic.getVariableToDelete();
        while (v != null) {
            order.add(v.getName());
            RemoveNodeEdit edit = new RemoveNodeEdit(net, net.getNode(v));
            heuristic.afterEditExecutes(edit);
            v = heuristic.getVariableToDelete();
        }
        return order;
    }

    private static List<ProbNet> testNetworks() {
        return List.of(
                TestNetworks.buildAsia(),
                TestNetworks.buildDiamond(),
                TestNetworks.buildChain3(),
                moralize(buildGrid(3, 3))
        );
    }

    private static ProbNet buildGrid(int rows, int cols) {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("grid" + rows + "x" + cols);
        Node[][] nodes = new Node[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Variable var = new Variable("G" + r + "_" + c, "s0", "s1");
                nodes[r][c] = net.addNode(var, NodeType.CHANCE);
            }
        }
        net.makeLinksExplicit(false);
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
}
