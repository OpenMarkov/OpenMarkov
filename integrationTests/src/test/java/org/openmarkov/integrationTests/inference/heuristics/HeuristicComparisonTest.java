/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.heuristics;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.inference.algorithm.huginPropagation.ClusterOfVariables;
import org.openmarkov.inference.algorithm.huginPropagation.HuginForest;
import org.openmarkov.integrationTests.inference.util.Util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Compares elimination heuristics on the largest Bayesian networks in the repository.
 * <p>
 * For each network/heuristic pair, reports treewidth (max clique variables - 1) and
 * junction-tree sum (total size of all clique tables). Printed as two separate tables.
 * <p>
 * Heuristics are parallelized per network row: all H heuristics for one network run
 * simultaneously, then results are collected before loading the next network. This limits
 * peak memory to H copies of one network at a time. The thread pool is sized to the
 * number of available CPU cores; the per-pair timeout is set generously because cores
 * compete for cache and memory.
 * <p>
 * This test always passes; its output is diagnostic only.
 */
public class HeuristicComparisonTest {

    private static final int TIMEOUT_MINUTES = 5;
    private static final int MAX_NETWORKS = 10;

    /** Sentinel: heuristic failed with an exception. */
    private static final long ERROR = Long.MIN_VALUE;
    /** Sentinel: heuristic exceeded the timeout. */
    private static final long TIMEOUT = Long.MIN_VALUE + 1;

    private static final String[] HEURISTIC_CLASS_NAMES = {
            "org.openmarkov.inference.heuristic.simpleElimination.SimpleElimination",
            "org.openmarkov.inference.heuristic.minimalFillIn.MinimalFillIn",
            "org.openmarkov.inference.heuristic.minimalCliqueSize.minimalCliqueSize",
            "org.openmarkov.inference.heuristic.canoAndMoral.CanoMoralElimination",
            "org.openmarkov.inference.heuristic.weightedMinFill.WeightedMinFill",
            "org.openmarkov.inference.heuristic.rollout.RolloutElimination"
    };

    @Test
    public void compareHeuristicsOnLargeNetworks() {
        List<ProbNet> networks = loadLargestBayesianNetworks();
        if (networks.isEmpty()) {
            System.out.println("No networks available for comparison.");
            return;
        }

        int H = HEURISTIC_CLASS_NAMES.length;
        int N = networks.size();
        long[][] treewidths = new long[N][H];
        long[][] sums       = new long[N][H];

        int numCores = Runtime.getRuntime().availableProcessors();
        System.out.println("Using " + numCores + " threads (available processors).");

        // Process one network at a time: all H heuristics for network[i] run in parallel,
        // then we collect before moving to network[i+1]. This caps peak memory at H copies
        // of one network instead of all N*H copies simultaneously.
        ExecutorService executor = Executors.newFixedThreadPool(numCores);
        try {
            for (int i = 0; i < N; i++) {
                System.out.println("Network " + (i + 1) + "/" + N + ": "
                        + getNetworkName(networks.get(i))
                        + " (" + networks.get(i).getVariables().size() + " vars)");
                @SuppressWarnings("unchecked")
                Future<long[]>[] rowFutures = new Future[H];
                for (int j = 0; j < H; j++) {
                    rowFutures[j] = submitHeuristic(executor, networks.get(i), HEURISTIC_CLASS_NAMES[j]);
                }
                for (int j = 0; j < H; j++) {
                    long[] result = collectResult(rowFutures[j]);
                    treewidths[i][j] = result[0];
                    sums[i][j]       = result[1];
                }
            }
        } finally {
            executor.shutdownNow();
        }

        String[] networkNames = new String[N];
        int[] varCounts = new int[N];
        for (int i = 0; i < N; i++) {
            networkNames[i] = getNetworkName(networks.get(i));
            varCounts[i]    = networks.get(i).getVariables().size();
        }

        System.out.println();
        printTable("Treewidth (lower is better)", networkNames, varCounts, treewidths);
        System.out.println();
        printTable("Junction-tree sum (lower is better)", networkNames, varCounts, sums);
        System.out.println();
    }

    // -------------------------------------------------------------------------

    /** Submits one (network, heuristic) pair to the pool without waiting. */
    private Future<long[]> submitHeuristic(ExecutorService executor, ProbNet net,
                                           String heuristicClassName) {
        return executor.submit(() -> {
            ProbNet netCopy = net.copy();
            Class<?> hClass = Class.forName(heuristicClassName);
            List<List<Variable>> variablesToEliminate = new ArrayList<>();
            variablesToEliminate.add(netCopy.getVariables());
            Constructor<?> ctor = hClass.getConstructor(ProbNet.class, List.class);
            EliminationHeuristic heuristic =
                    (EliminationHeuristic) ctor.newInstance(netCopy, variablesToEliminate);
            HuginForest forest = new HuginForest(netCopy.copy(), heuristic);
            return new long[]{ computeTreewidth(forest), computeSum(forest) };
        });
    }

    /**
     * Waits for a future with a per-pair timeout.
     *
     * @return {@code long[2]} = {treewidth, sum}, or sentinels {@link #ERROR}/{@link #TIMEOUT}.
     */
    private static long[] collectResult(Future<long[]> future) {
        try {
            return future.get(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            future.cancel(true);
            return new long[]{ TIMEOUT, TIMEOUT };
        } catch (ExecutionException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return new long[]{ ERROR, ERROR };
        }
    }

    private long computeTreewidth(HuginForest forest) {
        return forest.getNodes().stream()
                .mapToInt(c -> c.getVariables().size())
                .max()
                .orElse(0) - 1;
    }

    private long computeSum(HuginForest forest) {
        long sum = 0;
        for (ClusterOfVariables cluster : forest.getNodes()) {
            sum += cluster.size();
        }
        return sum;
    }

    // -------------------------------------------------------------------------

    private void printTable(String title, String[] networkNames, int[] varCounts,
                            long[][] values) {
        int nameWidth = 30;
        int varsWidth = 6;
        int colWidth  = 13;
        int H = HEURISTIC_CLASS_NAMES.length;

        System.out.println("=== " + title + " ===");
        System.out.println();

        // Header row: heuristic short names
        StringBuilder header = new StringBuilder();
        header.append(padRight("Network", nameWidth));
        header.append(padRight("Vars", varsWidth));
        for (String className : HEURISTIC_CLASS_NAMES) {
            String name = className.substring(className.lastIndexOf('.') + 1);
            header.append(padRight(abbreviate(name, colWidth - 1), colWidth));
        }
        System.out.println(header);
        System.out.println("-".repeat(nameWidth + varsWidth + colWidth * H));

        // Data rows
        for (int i = 0; i < networkNames.length; i++) {
            StringBuilder row = new StringBuilder();
            row.append(padRight(networkNames[i], nameWidth));
            row.append(padRight(String.valueOf(varCounts[i]), varsWidth));
            for (int j = 0; j < H; j++) {
                row.append(padRight(formatValue(values[i][j]), colWidth));
            }
            System.out.println(row);
        }
    }

    private static String formatValue(long v) {
        if (v == ERROR)   return "ERROR";
        if (v == TIMEOUT) return "TIMEOUT";
        if (v >= 1_000_000) return String.format("%.2fM", v / 1_000_000.0);
        if (v >= 1_000)     return String.format("%.1fK", v / 1_000.0);
        return String.valueOf(v);
    }

    // -------------------------------------------------------------------------
    
    private List<ProbNet> loadLargestBayesianNetworks() {
        List<ProbNet> all = Util.readProbNetsDB(BayesianNetworkType.getUniqueInstance());
        all = Util.filterNonPureTablePotentialProbNets(all);
        // Keep only networks whose actual type is BayesianNetwork (some files in the
        // "bn" directory may have a different network type once parsed)
        BayesianNetworkType bnType = BayesianNetworkType.getUniqueInstance();
        all = all.stream()
                 .filter(n -> n.getNetworkType() == bnType)
                 // Skip "-1-0" versioned duplicates; keep the canonical version only
                 .filter(n -> n.getName() == null || !n.getName().contains("-1-0"))
                 .collect(Collectors.toList());
        all.sort(Comparator.comparingInt((ProbNet n) -> n.getVariables().size()).reversed());
        return all.subList(0, Math.min(MAX_NETWORKS, all.size()));
    }

    private String getNetworkName(ProbNet net) {
        String name = net.getName();
        if (name == null || name.isBlank()) name = "unnamed";
        return abbreviate(name, 29);
    }

    private static String abbreviate(String s, int maxLen) {
        return s.length() > maxLen ? s.substring(0, maxLen) : s;
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width - 1) + " ";
        return s + " ".repeat(width - s.length());
    }
}
