/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.modelUncertainty.BetaFunction;
import org.openmarkov.core.model.network.modelUncertainty.ComplementFunction;
import org.openmarkov.core.model.network.modelUncertainty.DirichletFunction;
import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunction;
import org.openmarkov.core.model.network.modelUncertainty.ProbDensFunctionType;
import org.openmarkov.core.model.network.modelUncertainty.RangeFunction;
import org.openmarkov.core.model.network.modelUncertainty.TriangularFunction;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Which distributions can share a column of probabilities, so that sampling them gives probabilities that add up
 * to one. A fixed value other than zero, a range and a triangular go with each other and need a complement; a
 * beta goes alone, with a complement; the Dirichlet go two or more, with nothing else but zeros.
 *
 * @author Manuel Arias
 */
public final class DistributionsOfAColumn {

    private DistributionsOfAColumn() {
    }

    /**
     * @param column the uncertain value of each state, none of them null
     * @param states the name of each state, in the same order
     * @return what stops the distributions of the column from going together, or null if nothing does
     */
    public static @Nullable String whatIsWrong(List<UncertainValue> column, List<String> states) {
        List<Integer> bounded = indexes(column, function -> isFixedAndNotZero(function)
                || function instanceof RangeFunction || function instanceof TriangularFunction);
        List<Integer> betas = indexes(column, BetaFunction.class::isInstance);
        List<Integer> dirichlets = indexes(column, DirichletFunction.class::isInstance);
        boolean hasComplement = !indexes(column, ComplementFunction.class::isInstance).isEmpty();
        if (!bounded.isEmpty()) {
            int first = bounded.getFirst();
            double sumOfMaxima = 0;
            for (int i = 0; i < column.size(); i++) {
                ProbDensFunction function = column.get(i).getProbDensFunction();
                if (function instanceof ComplementFunction) {
                    continue;
                }
                if (!(function instanceof ExactFunction || function instanceof RangeFunction
                        || function instanceof TriangularFunction)) {
                    return sentence(states, first, column) + " next to the " + kind(function) + " of state \""
                            + states.get(i) + "\".\nWith a fixed value other than 0, a Range or a Triangular, the "
                            + "other states can only be Exact, Range, Triangular or Complement.";
                }
                sumOfMaxima += function.getMaximum();
            }
            if (!hasComplement) {
                return sentence(states, first, column)
                        + ", so another state must be Complement, to take the rest of the probability.";
            }
            if (sumOfMaxima > 1.0) {
                return "The maxima of the states that are not Complement add up to " + number(sumOfMaxima)
                        + ", more than 1.";
            }
        }
        if (betas.size() > 1) {
            return "States \"" + states.get(betas.get(0)) + "\" and \"" + states.get(betas.get(1))
                    + "\" both have a Beta distribution.\nOnly one state can have a Beta; the others can only be "
                    + "Complement or the exact value 0.";
        }
        if (betas.size() == 1) {
            int beta = betas.getFirst();
            for (int i = 0; i < column.size(); i++) {
                ProbDensFunction function = column.get(i).getProbDensFunction();
                if (i != beta && !(function instanceof ComplementFunction) && !isFixedAtZero(function)) {
                    return sentence(states, i, column) + " next to the Beta distribution of state \""
                            + states.get(beta) + "\".\nWith a Beta, the other states can only be Complement or the "
                            + "exact value 0.";
                }
            }
            if (!hasComplement) {
                return sentence(states, beta, column)
                        + ", so another state must be Complement, to take the rest of the probability.";
            }
        }
        if (dirichlets.size() == 1) {
            return "State \"" + states.get(dirichlets.getFirst()) + "\" is the only one with a Dirichlet "
                    + "distribution.\nA Dirichlet needs at least another state with a Dirichlet.";
        }
        if (dirichlets.size() > 1) {
            for (int i = 0; i < column.size(); i++) {
                ProbDensFunction function = column.get(i).getProbDensFunction();
                if (!(function instanceof DirichletFunction) && !isFixedAtZero(function)) {
                    return sentence(states, i, column) + " next to the Dirichlet distributions of the column.\n"
                            + "With Dirichlet distributions, the other states can only be Dirichlet or the exact "
                            + "value 0.";
                }
            }
        }
        return null;
    }

    /**
     * @return one line per column of the probability tables of the network whose distributions cannot go
     * together, naming the node and the column
     */
    public static List<String> wrongIn(ProbNet probNet) {
        List<String> found = new ArrayList<>();
        for (Node node : probNet.getNodes()) {
            for (Potential potential : node.getPotentials()) {
                if (potential instanceof TablePotential table && table.isUncertain()
                        && ColumnsThatDoNotAddUpToOne.isProbabilityTable(table)) {
                    checkTable(node, table, found);
                }
            }
        }
        return found;
    }

    private static void checkTable(Node node, TablePotential table, List<String> found) {
        List<String> states = Arrays.stream(table.getVariable(0).getStates()).map(State::getName).toList();
        int numStates = states.size();
        int numCells = Math.min(table.getValues().length, table.getUncertainValues().length);
        for (int column = 0; (column + 1) * numStates <= numCells; column++) {
            List<UncertainValue> cells = table.getUncertainColumn(column * numStates, numStates);
            String wrong = cells.getFirst() == null ? null : whatIsWrong(cells, states);
            if (wrong != null) {
                String configuration = ColumnsThatDoNotAddUpToOne.configuration(table.getVariables(), column);
                String where = configuration.isEmpty() ? node.getName() : node.getName() + " (" + configuration + ")";
                found.add(where + ": " + wrong.replace('\n', ' '));
            }
        }
    }

    private static List<Integer> indexes(List<UncertainValue> column, Predicate<ProbDensFunction> kind) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < column.size(); i++) {
            if (kind.test(column.get(i).getProbDensFunction())) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    private static boolean isFixedAtZero(ProbDensFunction function) {
        return function instanceof ExactFunction && function.getMean() == 0.0;
    }

    private static boolean isFixedAndNotZero(ProbDensFunction function) {
        return function instanceof ExactFunction && function.getMean() != 0.0;
    }

    private static String sentence(List<String> states, int index, List<UncertainValue> column) {
        ProbDensFunction function = column.get(index).getProbDensFunction();
        return "State \"" + states.get(index) + "\" has " + (function instanceof ExactFunction ? "the " : "a ")
                + kind(function);
    }

    private static String kind(ProbDensFunction function) {
        if (function instanceof ExactFunction) {
            return "fixed value " + number(function.getMean());
        }
        return function.getClass().getAnnotation(ProbDensFunctionType.class).name() + " distribution";
    }

    private static String number(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
