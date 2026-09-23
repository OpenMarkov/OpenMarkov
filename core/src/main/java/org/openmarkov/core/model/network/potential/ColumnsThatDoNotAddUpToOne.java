/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Columns of probability tables and of the parameters of canonical models that do not add up to
 * one. A network read from a file may bring them; sampling and exact inference then disagree.
 * Those beyond {@link #TOLERANCE} are reported; those within it, which come from rounding, are
 * normalized before inference. A column of zeros is left alone: link restrictions leave it for an
 * impossible configuration of the parents.
 *
 * @author Manuel Arias
 */
public final class ColumnsThatDoNotAddUpToOne {

    public static final double TOLERANCE = 1E-3;

    /** Below this, the difference is floating point noise and the column is left untouched. */
    private static final double NOISE = 1E-9;

    private ColumnsThatDoNotAddUpToOne() {
    }

    /**
     * @return one line per column beyond the tolerance, naming the node, the column and its sum
     */
    public static List<String> in(ProbNet probNet) {
        List<String> found = new ArrayList<>();
        for (Node node : probNet.getNodes()) {
            for (Potential potential : node.getPotentials()) {
                switch (potential) {
                    case ICIPotential ici -> checkCanonicalModel(node, ici, found);
                    case TablePotential table -> checkTable(node, table, found);
                    default -> {
                    }
                }
            }
        }
        return found;
    }

    /**
     * Replaces, in the nodes of {@code probNet}, each potential with columns within the tolerance
     * by a normalized copy. The potentials themselves are not changed, so a shallow copy of a
     * network can be normalized without touching the original.
     */
    public static void normalizeWithinTolerance(ProbNet probNet) {
        for (Node node : probNet.getNodes()) {
            List<Potential> potentials = node.getPotentials();
            boolean changed = false;
            for (int i = 0; i < potentials.size(); i++) {
                Potential normalized = switch (potentials.get(i)) {
                    case ICIPotential ici -> normalizedCanonicalModel(ici);
                    case TablePotential table -> normalizedTable(table);
                    case null, default -> null;
                };
                if (normalized != null) {
                    potentials.set(i, normalized);
                    changed = true;
                }
            }
            if (changed) {
                node.setPotentials(potentials);
            }
        }
    }

    private static boolean isProbabilityTable(TablePotential table) {
        PotentialRole role = table.getPotentialRole();
        List<Variable> variables = table.getVariables();
        return (role == PotentialRole.CONDITIONAL_PROBABILITY || role == PotentialRole.POLICY) && !variables.isEmpty()
                && variables.getFirst().getVariableType() != VariableType.NUMERIC;
    }

    private static void checkTable(Node node, TablePotential table, List<String> found) {
        if (!isProbabilityTable(table)) {
            return;
        }
        double[] values = table.getValues();
        int numStates = table.getVariables().getFirst().getNumStates();
        for (int column = 0; column * numStates < values.length; column++) {
            double sum = sumOfColumn(values, column * numStates, numStates);
            if (isBeyondTolerance(sum)) {
                found.add(describe(node, configuration(table.getVariables(), column), sum));
            }
        }
    }

    private static void checkCanonicalModel(Node node, ICIPotential ici, List<String> found) {
        List<Variable> variables = ici.getVariables();
        int numStates = variables.getFirst().getNumStates();
        for (Variable parent : variables.subList(1, variables.size())) {
            double[] parameters = ici.getNoisyParameters(parent);
            if (parameters == null) {
                continue;
            }
            for (int state = 0; state < parent.getNumStates(); state++) {
                double sum = sumOfColumn(parameters, state * numStates, numStates);
                if (isBeyondTolerance(sum)) {
                    found.add(describe(node, parent.getName() + " = " + parent.getStates()[state].getName(), sum));
                }
            }
        }
        double[] leak = ici.getLeakyParameters();
        if (leak != null) {
            double sum = sumOfColumn(leak, 0, numStates);
            if (isBeyondTolerance(sum)) {
                found.add(describe(node, "leak", sum));
            }
        }
    }

    /** @return a normalized copy, or null if nothing has to be normalized */
    private static TablePotential normalizedTable(TablePotential table) {
        if (!isProbabilityTable(table)) {
            return null;
        }
        int numStates = table.getVariables().getFirst().getNumStates();
        if (!needsNormalizing(table.getValues(), numStates)) {
            return null;
        }
        Potential copy = table.copy();
        if (copy.getClass() != table.getClass()) {
            return null; // the copy would lose what the subclass carries
        }
        TablePotential normalized = (TablePotential) copy;
        normalizeColumns(normalized.getValues(), numStates);
        return normalized;
    }

    /** @return a normalized copy, or null if nothing has to be normalized */
    private static ICIPotential normalizedCanonicalModel(ICIPotential ici) {
        List<Variable> variables = ici.getVariables();
        int numStates = variables.getFirst().getNumStates();
        List<Variable> parents = variables.subList(1, variables.size());
        double[] leak = ici.getLeakyParameters();
        boolean needed = leak != null && needsNormalizing(leak, numStates);
        for (Variable parent : parents) {
            double[] parameters = ici.getNoisyParameters(parent);
            needed |= parameters != null && needsNormalizing(parameters, numStates);
        }
        if (!needed) {
            return null;
        }
        ICIPotential normalized = (ICIPotential) ici.copy();
        for (Variable parent : parents) {
            double[] parameters = ici.getNoisyParameters(parent);
            if (parameters != null) {
                double[] copy = parameters.clone();
                normalizeColumns(copy, numStates);
                normalized.setNoisyParameters(parent, copy);
            }
        }
        if (leak != null) {
            double[] copy = leak.clone();
            normalizeColumns(copy, numStates);
            normalized.setLeakyParameters(copy);
        }
        return normalized;
    }

    private static boolean needsNormalizing(double[] values, int numStates) {
        for (int start = 0; start < values.length; start += numStates) {
            if (isWithinTolerance(sumOfColumn(values, start, numStates))) {
                return true;
            }
        }
        return false;
    }

    private static void normalizeColumns(double[] values, int numStates) {
        for (int start = 0; start < values.length; start += numStates) {
            double sum = sumOfColumn(values, start, numStates);
            if (isWithinTolerance(sum)) {
                for (int i = start; i < start + numStates && i < values.length; i++) {
                    values[i] /= sum;
                }
            }
        }
    }

    private static double sumOfColumn(double[] values, int start, int length) {
        double sum = 0;
        for (int i = start; i < start + length && i < values.length; i++) {
            sum += values[i];
        }
        return sum;
    }

    private static boolean isBeyondTolerance(double sum) {
        return sum != 0 && Math.abs(sum - 1) > TOLERANCE;
    }

    private static boolean isWithinTolerance(double sum) {
        double difference = Math.abs(sum - 1);
        return difference > NOISE && difference <= TOLERANCE;
    }

    /** The states of the conditioning variables in this column; the first one changes fastest. */
    private static String configuration(List<Variable> variables, int column) {
        List<String> parts = new ArrayList<>();
        int rest = column;
        for (Variable variable : variables.subList(1, variables.size())) {
            int states = variable.getNumStates();
            parts.add(variable.getName() + " = " + variable.getStates()[rest % states].getName());
            rest /= states;
        }
        return String.join(", ", parts);
    }

    private static String describe(Node node, String column, double sum) {
        String where = column.isEmpty() ? node.getName() : node.getName() + " (" + column + ")";
        return where + ": " + String.format(Locale.ROOT, "%.8g", sum);
    }
}
