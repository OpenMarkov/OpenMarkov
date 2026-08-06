/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * How long multiplying and marginalizing take on the four CPCS networks, the largest at hand.
 * The work measured is a round of variable elimination: for each variable, the potentials that
 * contain it are multiplied and the variable is summed out. The results are not fed back, so the
 * load is the same every time.
 *
 * <p>There is no threshold: time thresholds are brittle. The numbers are printed so that they can
 * be compared by hand before and after a change to these operations.
 *
 * @author Manuel Arias
 */
public class OperationsReferenceTimesTest {

    private static final List<String> NETWORKS = List.of("networks/bn/BN-cpcs54.pgmx",
                                                         "networks/bn/BN-cpcs179.pgmx",
                                                         "networks/bn/BN-cpcs360b.pgmx",
                                                         "networks/bn/BN-cpcs422b.pgmx");

    private static final int WARM_UP_ROUNDS = 1;
    private static final int MEASURED_ROUNDS = 5;

    /**
     * Largest product measured, in cells. A variable of these networks may belong to twenty
     * families at once, and their product does not fit in memory: those variables are left out of
     * the load and counted, because a measurement that runs out of memory measures nothing.
     */
    private static final long LARGEST_PRODUCT = 1L << 20;

    /**
     * One variable, the potentials that contain it, and how many cells their product has.
     */
    private record Group(Variable variable, List<TablePotential> potentials, long cells) {
    }

    @Tag(TestSpeed.SLOW)
    @Test public void multiplyingAndMarginalizingOverTheCpcsNetworks() throws Exception {
        System.out.printf("%n%-12s %8s %10s %13s %15s %14s%n",
                "network", "groups", "left out", "result cells", "multiply (ms)", "and sum (ms)");

        for (String file : NETWORKS) {
            ProbNet network = load(file);
            List<Group> groups = new ArrayList<>();
            int leftOut = 0;
            for (Group group : groupsOf(network)) {
                if (group.cells() <= LARGEST_PRODUCT) {
                    groups.add(group);
                } else {
                    leftOut++;
                }
            }
            long cells = groups.stream().mapToLong(Group::cells).sum();

            long multiplying = medianOf(() -> {
                for (Group group : groups) {
                    DiscretePotentialOperations.multiply(group.potentials());
                }
            });
            long summingOut = medianOf(() -> {
                for (Group group : groups) {
                    DiscretePotentialOperations.multiplyAndMarginalize(group.potentials(), group.variable());
                }
            });

            System.out.printf("%-12s %8d %10d %13d %15.1f %14.1f%n",
                    name(file), groups.size(), leftOut, cells, multiplying / 1E6, summingOut / 1E6);

            assertTrue(groups.size() > 10, "too few groups to measure anything in " + name(file));
            for (Group group : groups) {
                TablePotential result = DiscretePotentialOperations.multiplyAndMarginalize(group.potentials(),
                        group.variable());
                assertFalse(result.getVariables().contains(group.variable()),
                        "the eliminated variable is still in the result, in " + name(file));
            }
        }
    }

    /**
     * For each variable of the network, the table potentials that contain it. A variable that is
     * in a single potential is left out: multiplying one potential measures nothing.
     */
    private static List<Group> groupsOf(ProbNet network) {
        List<TablePotential> tables = new ArrayList<>();
        for (Node node : network.getNodes()) {
            for (Potential potential : node.getPotentials()) {
                if (potential instanceof TablePotential table) {
                    tables.add(table);
                }
            }
        }
        List<Group> groups = new ArrayList<>();
        for (Node node : network.getNodes()) {
            Variable variable = node.getVariable();
            List<TablePotential> containing = tables.stream()
                                                    .filter(table -> table.getVariables().contains(variable))
                                                    .toList();
            if (containing.size() > 1) {
                groups.add(new Group(variable, containing, cellsOfTheProduct(containing)));
            }
        }
        return groups;
    }

    /**
     * How many cells the product of these potentials would have, or {@link Long#MAX_VALUE} when
     * the number overflows.
     */
    private static long cellsOfTheProduct(List<TablePotential> potentials) {
        Set<Variable> variables = new LinkedHashSet<>();
        potentials.forEach(potential -> variables.addAll(potential.getVariables()));
        long cells = 1;
        for (Variable variable : variables) {
            if (cells > Long.MAX_VALUE / variable.getNumStates()) {
                return Long.MAX_VALUE;
            }
            cells *= variable.getNumStates();
        }
        return cells;
    }

    /**
     * Nanoseconds of the middle round, after the warm-up ones.
     */
    private static long medianOf(Runnable work) {
        for (int round = 0; round < WARM_UP_ROUNDS; round++) {
            work.run();
        }
        List<Long> times = new ArrayList<>();
        for (int round = 0; round < MEASURED_ROUNDS; round++) {
            long start = System.nanoTime();
            work.run();
            times.add(System.nanoTime() - start);
        }
        return times.stream().sorted().toList().get(MEASURED_ROUNDS / 2);
    }

    private static String name(String file) {
        return file.substring(file.lastIndexOf('/') + 1, file.lastIndexOf('.'));
    }

    private static ProbNet load(String file) throws Exception {
        return new PGMXReader()
                .read(OperationsReferenceTimesTest.class.getClassLoader().getResource(file))
                .probNet();
    }
}
