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
import org.openmarkov.inference.heuristic.canoAndMoral.CanoMoralElimination;
import org.openmarkov.inference.heuristic.minimalCliqueSize.minimalCliqueSize;
import org.openmarkov.inference.heuristic.minimalFillIn.MinimalFillIn;
import org.openmarkov.inference.heuristic.simpleElimination.SimpleElimination;
import org.openmarkov.inference.heuristic.weightedMinFill.WeightedMinFill;
import org.openmarkov.inference.testutils.TestNetworks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.openmarkov.inference.heuristic.rollout.RolloutCostFunction.*;
import static org.openmarkov.inference.heuristic.rollout.RolloutCriterion.*;

/**
 * Benchmark that systematically explores RolloutElimination parameter combinations
 * on networks of varying size and structure. Prints comparative tables of treewidth
 * and junction-tree sum for all configurations.
 * <p>
 * This test always passes; its output is diagnostic only.
 *
 * @author Manuel Arias
 */
public class RolloutParameterTuningTest {

    private static final long TIMEOUT_MS = 120_000;

    // -------------------------------------------------------------------------
    // Configurations
    // -------------------------------------------------------------------------

    private record Config(String name,
                          BiFunction<ProbNet, List<List<Variable>>, EliminationHeuristic> factory) {}

    private static List<Config> buildConfigs() {
        List<Config> configs = new ArrayList<>();

        // Baselines
        configs.add(new Config("Simple", SimpleElimination::new));
        configs.add(new Config("MFill", MinimalFillIn::new));
        configs.add(new Config("WMFill", WeightedMinFill::new));
        configs.add(new Config("MCS", minimalCliqueSize::new));
        configs.add(new Config("CanoM", CanoMoralElimination::new));

        // Rollout: depth × costFunction × criterion
        for (int d : new int[]{1, 2}) {
            for (var cf : new Object[][]{{"WMF", WEIGHTED_MIN_FILL}, {"MCS", MIN_CLIQUE_SIZE}}) {
                for (var cr : new Object[][]{{"JTS", JUNCTION_TREE_SUM}, {"MCT", MAX_CLIQUE_TABLE_SIZE}}) {
                    String cfName = (String) cf[0];
                    RolloutCostFunction costFn = (RolloutCostFunction) cf[1];
                    String crName = (String) cr[0];
                    RolloutCriterion criterion = (RolloutCriterion) cr[1];
                    String name = "R(" + d + "," + cfName + "," + crName + ")";
                    int depth = d;
                    configs.add(new Config(name, (net, vars) ->
                            new RolloutElimination(net, vars, depth, 3, costFn, criterion)));
                }
            }
        }
        return configs;
    }

    // -------------------------------------------------------------------------
    // Main benchmark
    // -------------------------------------------------------------------------

    @Test
    public void tuneParameters() {
        Map<String, ProbNet> networks = new LinkedHashMap<>();
        networks.put("asia(8)", moralize(TestNetworks.buildAsia()));
        networks.put("grid4(16)", moralize(buildGrid(4, 4)));
        networks.put("grid5(25)", moralize(buildGrid(5, 5)));
        networks.put("grid6(36)", moralize(buildGrid(6, 6)));
        networks.put("grid7(49)", moralize(buildGrid(7, 7)));

        List<Config> configs = buildConfigs();
        int C = configs.size();
        int N = networks.size();
        String[] netNames = networks.keySet().toArray(new String[0]);
        ProbNet[] nets = networks.values().toArray(new ProbNet[0]);

        long[][] treewidths = new long[N][C];
        long[][] jtSums = new long[N][C];
        long[][] timesMs = new long[N][C];

        for (int i = 0; i < N; i++) {
            System.out.printf("Network %d/%d: %s%n", i + 1, N, netNames[i]);
            for (int j = 0; j < C; j++) {
                long t0 = System.currentTimeMillis();
                try {
                    int[] order = runHeuristic(nets[i], configs.get(j).factory);
                    long elapsed = System.currentTimeMillis() - t0;
                    if (elapsed > TIMEOUT_MS) {
                        treewidths[i][j] = -2;
                        jtSums[i][j] = -2;
                        timesMs[i][j] = elapsed;
                        continue;
                    }
                    double[] metrics = computeMetrics(nets[i], order);
                    treewidths[i][j] = (long) metrics[0];
                    jtSums[i][j] = (long) metrics[1];
                    timesMs[i][j] = elapsed;
                } catch (Exception e) {
                    treewidths[i][j] = -1;
                    jtSums[i][j] = -1;
                    timesMs[i][j] = System.currentTimeMillis() - t0;
                }
            }
        }

        System.out.println();
        printTable("TREEWIDTH (lower is better)", netNames, configs, treewidths, timesMs);
        System.out.println();
        printTable("JUNCTION-TREE SUM (lower is better)", netNames, configs, jtSums, timesMs);
        System.out.println();
        printRanking("RANKING by avg normalized JT-sum", netNames, configs, jtSums);
    }

    // -------------------------------------------------------------------------
    // Heuristic execution
    // -------------------------------------------------------------------------

    /**
     * Runs the heuristic on a copy of the network and returns the elimination order
     * as an array of variable indices (matching the order of net.getVariables()).
     */
    private static int[] runHeuristic(ProbNet net,
                                      BiFunction<ProbNet, List<List<Variable>>, EliminationHeuristic> factory) {
        List<Variable> allVars = net.getVariables();
        Map<Variable, Integer> varToIdx = new HashMap<>(allVars.size() * 2);
        for (int i = 0; i < allVars.size(); i++) {
            varToIdx.put(allVars.get(i), i);
        }

        List<List<Variable>> variablesToEliminate = new ArrayList<>();
        variablesToEliminate.add(new ArrayList<>(allVars));
        EliminationHeuristic heuristic = factory.apply(net, variablesToEliminate);

        List<Integer> order = new ArrayList<>();
        Variable v = heuristic.getVariableToDelete();
        while (v != null) {
            order.add(varToIdx.get(v));
            RemoveNodeEdit edit = new RemoveNodeEdit(net, net.getNode(v));
            heuristic.afterEditExecutes(edit);
            v = heuristic.getVariableToDelete();
        }
        return order.stream().mapToInt(Integer::intValue).toArray();
    }

    // -------------------------------------------------------------------------
    // Metrics computation (on moral adjacency matrix)
    // -------------------------------------------------------------------------

    /**
     * Simulates the elimination on the moral adjacency matrix and returns
     * {@code [treewidth, junctionTreeSum]}.
     */
    private static double[] computeMetrics(ProbNet net, int[] order) {
        List<Variable> allVars = net.getVariables();
        int n = allVars.size();
        Map<Variable, Integer> varToIdx = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            varToIdx.put(allVars.get(i), i);
        }
        int[] cardinalities = new int[n];
        for (int i = 0; i < n; i++) {
            cardinalities[i] = Math.max(1, allVars.get(i).getNumStates());
        }

        boolean[][] adj = buildMoralAdj(net, n, varToIdx);
        boolean[] active = new boolean[n];
        Arrays.fill(active, true);

        double maxCliqueVars = 0;
        double sum = 0;

        for (int x : order) {
            if (!active[x]) continue;
            double product = cardinalities[x];
            int cliqueVars = 1;
            for (int i = 0; i < n; i++) {
                if (adj[x][i] && active[i]) {
                    product *= cardinalities[i];
                    cliqueVars++;
                }
            }
            maxCliqueVars = Math.max(maxCliqueVars, cliqueVars);
            sum += product;

            // Fill-in + remove
            for (int i = 0; i < n; i++) {
                if (!adj[x][i] || !active[i]) continue;
                for (int j = i + 1; j < n; j++) {
                    if (adj[x][j] && active[j]) {
                        adj[i][j] = true;
                        adj[j][i] = true;
                    }
                }
            }
            for (int i = 0; i < n; i++) {
                adj[x][i] = false;
                adj[i][x] = false;
            }
            active[x] = false;
        }
        return new double[]{maxCliqueVars - 1, sum}; // treewidth = max clique - 1
    }

    private static boolean[][] buildMoralAdj(ProbNet net, int n, Map<Variable, Integer> varToIdx) {
        boolean[][] adj = new boolean[n][n];
        for (Node node : net.getNodes()) {
            Integer i = varToIdx.get(node.getVariable());
            if (i == null) continue;
            for (Node neighbor : node.getNeighbors()) {
                Integer j = varToIdx.get(neighbor.getVariable());
                if (j != null) {
                    adj[i][j] = true;
                    adj[j][i] = true;
                }
            }
        }
        return adj;
    }

    // -------------------------------------------------------------------------
    // Network builders
    // -------------------------------------------------------------------------

    private static ProbNet buildGrid(int rows, int cols) {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
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

    /**
     * Moralizes a Bayesian network in place: adds undirected edges between
     * co-parents of every node.
     */
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

    // -------------------------------------------------------------------------
    // Output formatting
    // -------------------------------------------------------------------------

    private void printTable(String title, String[] netNames, List<Config> configs,
                            long[][] values, long[][] timesMs) {
        int nameW = 14;
        int colW = 18;
        int C = configs.size();

        System.out.println("=== " + title + " ===");
        System.out.println();

        // Header
        StringBuilder hdr = new StringBuilder();
        hdr.append(pad("Network", nameW));
        for (Config c : configs) {
            hdr.append(pad(c.name, colW));
        }
        System.out.println(hdr);
        System.out.println("-".repeat(nameW + colW * C));

        // Data rows
        for (int i = 0; i < netNames.length; i++) {
            StringBuilder row = new StringBuilder();
            row.append(pad(netNames[i], nameW));
            for (int j = 0; j < C; j++) {
                String val;
                if (values[i][j] == -1) val = "ERR";
                else if (values[i][j] == -2) val = "T/O";
                else val = values[i][j] + " (" + timesMs[i][j] + "ms)";
                row.append(pad(val, colW));
            }
            System.out.println(row);
        }
    }

    private void printRanking(String title, String[] netNames, List<Config> configs,
                              long[][] jtSums) {
        int C = configs.size();
        int N = netNames.length;

        System.out.println("=== " + title + " ===");
        System.out.println();

        // For each network, normalize JT-sum relative to the best
        double[] avgNormalized = new double[C];
        int[] validNets = new int[C];

        for (int i = 0; i < N; i++) {
            long best = Long.MAX_VALUE;
            for (int j = 0; j < C; j++) {
                if (jtSums[i][j] > 0 && jtSums[i][j] < best) {
                    best = jtSums[i][j];
                }
            }
            if (best == Long.MAX_VALUE) continue;

            for (int j = 0; j < C; j++) {
                if (jtSums[i][j] > 0) {
                    avgNormalized[j] += (double) jtSums[i][j] / best;
                    validNets[j]++;
                }
            }
        }

        // Sort by average normalized score
        Integer[] indices = new Integer[C];
        for (int j = 0; j < C; j++) {
            indices[j] = j;
            if (validNets[j] > 0) avgNormalized[j] /= validNets[j];
            else avgNormalized[j] = Double.MAX_VALUE;
        }
        Arrays.sort(indices, (a, b) -> Double.compare(avgNormalized[a], avgNormalized[b]));

        System.out.printf("  %-4s  %-22s  %s%n", "Rank", "Configuration", "Avg normalized JT-sum");
        System.out.println("  " + "-".repeat(55));
        for (int rank = 0; rank < C; rank++) {
            int j = indices[rank];
            String score = avgNormalized[j] == Double.MAX_VALUE ? "N/A"
                    : String.format("%.4f", avgNormalized[j]);
            System.out.printf("  %-4d  %-22s  %s%n", rank + 1, configs.get(j).name, score);
        }
    }

    private static String pad(String s, int width) {
        if (s.length() >= width) return s.substring(0, width - 1) + " ";
        return s + " ".repeat(width - s.length());
    }
}
