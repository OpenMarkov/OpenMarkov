/*
 * Copyright (c) CISIAD, UNED, Spain, 2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.canonical;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Differential tests for the ICI canonical potentials ({@link MinPotential}, {@link MaxPotential},
 * {@link TuningPotential}).
 * <p>
 * The oracle is a from-scratch reference implementation of the ICI expansion
 * ({@link #referenceCpt}): P(y | x) = sum over (z_0, z_1, ..., z_n) with f(z_0..z_n) = y of
 * leaky[z_0] * prod_i noisy_i[z_i | x_i], where z_0 is the leak and z_i the noisy contribution of
 * parent i. It depends on nothing in the production expansion machinery, and it has been checked by
 * hand against getCPT for MinPotential.
 * <p>
 * Two independent production outputs are compared against that reference:
 * <ul>
 *   <li>{@code getCPT()} — the path used by exact inference;</li>
 *   <li>the EM expansion — {@code getSubpotentials()} multiplied and marginalised, which is what
 *       EMAlgorithm relies on (it uses {@code getFFunctionPotential()} directly). This is where
 *       B-FFunc lived.</li>
 * </ul>
 *
 * @author Manuel Arias
 */
class ICIPotentialDifferentialTest {

    private static final double TOLERANCE = 1.0e-9;

    // ------------------------------------------------------------------ the f functions

    /** f over the combined (leak + z) states. */
    private interface FFunction {
        int apply(int[] combinedStates, int numChildStates);
    }

    private static final FFunction MIN = (s, k) -> {
        int m = s[0];
        for (int v : s) m = Math.min(m, v);
        return m;
    };
    private static final FFunction MAX = (s, k) -> {
        int m = s[0];
        for (int v : s) m = Math.max(m, v);
        return m;
    };
    private static final FFunction TUNING = (s, k) -> {
        int netIncrement = 0;
        for (int v : s) netIncrement += v - 1;      // 3 states: down=0, st.quo=1, up=2 -> -1/0/+1
        return netIncrement < 0 ? 0 : (netIncrement == 0 ? 1 : 2);
    };

    // ------------------------------------------------------------------ reference expansion

    /**
     * Builds the reference CPT as a flat table over [child, parent_1, ..., parent_n] with the child
     * varying fastest, exactly matching the layout OpenMarkov uses.
     *
     * @param f            the deterministic combination function
     * @param childStates  number of states of the conditioned (child) variable
     * @param parentStates number of states of each parent
     * @param noisy        noisy[i] is P(Z_i | X_i), laid out z fastest: noisy[i][z + childStates*x]
     * @param leaky        P(Z_0), length childStates
     */
    private static double[] referenceCpt(FFunction f, int childStates, int[] parentStates,
                                         double[][] noisy, double[] leaky) {
        int numParents = parentStates.length;
        int numColumns = 1;
        for (int ps : parentStates) numColumns *= ps;
        double[] cpt = new double[childStates * numColumns];

        for (int column = 0; column < numColumns; column++) {
            // Decode parent states for this column (parent_1 fastest, matching the table layout).
            int[] x = new int[numParents];
            int rest = column;
            for (int i = 0; i < numParents; i++) {
                x[i] = rest % parentStates[i];
                rest /= parentStates[i];
            }
            int base = column * childStates;
            // Enumerate (z_0 leak, z_1, ..., z_n), each with childStates values.
            int numCombinations = (int) Math.pow(childStates, numParents + 1);
            int[] z = new int[numParents + 1];
            for (int combo = 0; combo < numCombinations; combo++) {
                int c = combo;
                for (int k = 0; k <= numParents; k++) {
                    z[k] = c % childStates;
                    c /= childStates;
                }
                double weight = leaky[z[0]];
                for (int i = 0; i < numParents; i++) {
                    weight *= noisy[i][z[i + 1] + childStates * x[i]];
                }
                if (weight != 0.0) {
                    cpt[base + f.apply(z, childStates)] += weight;
                }
            }
        }
        return cpt;
    }

    // ------------------------------------------------------------------ builders

    private ICIPotential buildMinMax(boolean max, int childStates, int[] parentStates,
                                     double[][] noisy, double[] leaky) {
        List<Variable> vars = new ArrayList<>();
        Variable child = new Variable("Y", childStates);
        vars.add(child);
        for (int i = 0; i < parentStates.length; i++) {
            vars.add(new Variable("P" + i, parentStates[i]));
        }
        ICIPotential potential = max ? new MaxPotential(vars) : new MinPotential(vars);
        for (int i = 0; i < parentStates.length; i++) {
            potential.setNoisyParameters(vars.get(i + 1), noisy[i]);
        }
        potential.setLeakyParameters(leaky);
        return potential;
    }

    private ICIPotential buildTuning(int numParents, double[][] noisy, double[] leaky) {
        List<Variable> vars = new ArrayList<>();
        vars.add(new Variable("Y", 3));
        for (int i = 0; i < numParents; i++) vars.add(new Variable("P" + i, 3));
        TuningPotential potential = new TuningPotential(vars);
        for (int i = 0; i < numParents; i++) potential.setNoisyParameters(vars.get(i + 1), noisy[i]);
        potential.setLeakyParameters(leaky);
        return potential;
    }

    // ------------------------------------------------------------------ assertions

    private void assertAgreesWithReference(ICIPotential potential, FFunction f, int childStates,
                                           int[] parentStates, double[][] noisy, double[] leaky)
            throws NonProjectablePotentialException {
        double[] reference = referenceCpt(f, childStates, parentStates, noisy, leaky);

        List<Variable> vars = potential.getVariables();
        TablePotential cpt = potential.getCPT();
        TablePotential emExpansion = DiscretePotentialOperations
                .multiplyAndMarginalize(potential.getSubpotentials(), vars);

        int numColumns = reference.length / childStates;
        for (int column = 0; column < numColumns; column++) {
            int[] parentIdx = new int[parentStates.length];
            int rest = column;
            for (int i = 0; i < parentStates.length; i++) {
                parentIdx[i] = rest % parentStates[i];
                rest /= parentStates[i];
            }
            for (int y = 0; y < childStates; y++) {
                int[] config = new int[vars.size()];
                config[0] = y;
                System.arraycopy(parentIdx, 0, config, 1, parentStates.length);
                double expected = reference[column * childStates + y];
                assertThat(cpt.getValue(vars, config))
                        .as("getCPT at column %d, child %d", column, y)
                        .isCloseTo(expected, org.assertj.core.data.Offset.offset(TOLERANCE));
                assertThat(emExpansion.getValue(vars, config))
                        .as("EM expansion at column %d, child %d", column, y)
                        .isCloseTo(expected, org.assertj.core.data.Offset.offset(TOLERANCE));
            }
        }
    }

    // ------------------------------------------------------------------ tests

    @Test void minAgreesWithReference_asymmetric() throws Exception {
        int[] ps = {3, 2};
        double[][] noisy = {
                {0.0, 0.0, 1.0, 0.0, 0.2, 0.8, 0.7, 0.3, 0.0},
                {0.0, 0.0, 1.0, 0.6, 0.3, 0.1}
        };
        double[] leaky = {0.01, 0.1, 0.89};
        assertAgreesWithReference(buildMinMax(false, 3, ps, noisy, leaky), MIN, 3, ps, noisy, leaky);
    }

    @Test void maxAgreesWithReference_asymmetric() throws Exception {
        int[] ps = {3, 2};
        double[][] noisy = {
                {0.7, 0.2, 0.1, 0.1, 0.3, 0.6, 0.0, 0.0, 1.0},
                {1.0, 0.0, 0.0, 0.2, 0.3, 0.5}
        };
        double[] leaky = {0.8, 0.15, 0.05};
        assertAgreesWithReference(buildMinMax(true, 3, ps, noisy, leaky), MAX, 3, ps, noisy, leaky);
    }

    @Test void minWithTwoBinaryParents() throws Exception {
        int[] ps = {2, 2};
        double[][] noisy = {
                {0.9, 0.1, 0.3, 0.7},
                {0.8, 0.2, 0.25, 0.75}
        };
        double[] leaky = {0.6, 0.4};
        assertAgreesWithReference(buildMinMax(false, 2, ps, noisy, leaky), MIN, 2, ps, noisy, leaky);
    }

    @Test void tuningAgreesWithReference_asymmetric() throws Exception {
        int[] ps = {3, 3};
        double[][] noisy = {
                {0.7, 0.2, 0.1, 0.2, 0.6, 0.2, 0.1, 0.2, 0.7},
                {0.8, 0.15, 0.05, 0.1, 0.8, 0.1, 0.05, 0.15, 0.8}
        };
        double[] leaky = {0.2, 0.5, 0.3};
        assertAgreesWithReference(buildTuning(2, noisy, leaky), TUNING, 3, ps, noisy, leaky);
    }

    // ------------------------------------------------------------------ property-based coverage

    /** A normalised distribution of length n. */
    private static double[] randomDistribution(java.util.Random r, int n) {
        double[] d = new double[n];
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            d[i] = r.nextDouble() + 1.0e-6;   // avoid an all-zero column
            sum += d[i];
        }
        for (int i = 0; i < n; i++) d[i] /= sum;
        return d;
    }

    /** P(Z | X) laid out z fastest: one normalised child-states column per parent state. */
    private static double[] randomNoisy(java.util.Random r, int childStates, int parentStates) {
        double[] noisy = new double[childStates * parentStates];
        for (int x = 0; x < parentStates; x++) {
            double[] column = randomDistribution(r, childStates);
            System.arraycopy(column, 0, noisy, x * childStates, childStates);
        }
        return noisy;
    }

    @Property(tries = 300)
    void minMaxAgreeWithReferenceOverRandomConfigs(
            @ForAll long seed,
            @ForAll boolean max,
            @ForAll @IntRange(min = 1, max = 3) int numParents,
            @ForAll @IntRange(min = 2, max = 4) int childStates) throws NonProjectablePotentialException {
        java.util.Random r = new java.util.Random(seed);
        int[] parentStates = new int[numParents];
        double[][] noisy = new double[numParents][];
        for (int i = 0; i < numParents; i++) {
            parentStates[i] = 2 + r.nextInt(3);   // 2..4
            noisy[i] = randomNoisy(r, childStates, parentStates[i]);
        }
        double[] leaky = randomDistribution(r, childStates);
        assertAgreesWithReference(buildMinMax(max, childStates, parentStates, noisy, leaky),
                max ? MAX : MIN, childStates, parentStates, noisy, leaky);
    }

    @Property(tries = 200)
    void tuningAgreesWithReferenceOverRandomConfigs(
            @ForAll long seed,
            @ForAll @IntRange(min = 1, max = 3) int numParents) throws NonProjectablePotentialException {
        java.util.Random r = new java.util.Random(seed);
        int[] parentStates = new int[numParents];
        double[][] noisy = new double[numParents][];
        for (int i = 0; i < numParents; i++) {
            parentStates[i] = 3;                   // Tuning requires 3 states
            noisy[i] = randomNoisy(r, 3, 3);
        }
        double[] leaky = randomDistribution(r, 3);
        assertAgreesWithReference(buildTuning(numParents, noisy, leaky), TUNING, 3, parentStates, noisy, leaky);
    }

    @Property(tries = 200)
    void reorderingParentsPreservesTheCpt(
            @ForAll long seed,
            @ForAll boolean max,
            @ForAll @IntRange(min = 2, max = 3) int numParents,
            @ForAll @IntRange(min = 2, max = 4) int childStates) throws NonProjectablePotentialException {
        java.util.Random r = new java.util.Random(seed);
        int[] parentStates = new int[numParents];
        double[][] noisy = new double[numParents][];
        for (int i = 0; i < numParents; i++) {
            parentStates[i] = 2 + r.nextInt(3);
            noisy[i] = randomNoisy(r, childStates, parentStates[i]);
        }
        double[] leaky = randomDistribution(r, childStates);
        ICIPotential potential = buildMinMax(max, childStates, parentStates, noisy, leaky);

        // Reorder: keep the child first, reverse the parents.
        List<Variable> vars = potential.getVariables();
        List<Variable> reordered = new ArrayList<>();
        reordered.add(vars.get(0));
        for (int i = vars.size() - 1; i >= 1; i--) reordered.add(vars.get(i));

        ICIPotential reorderedPotential = (ICIPotential) potential.reorder(reordered);
        TablePotential cptOriginal = potential.getCPT();
        TablePotential cptReordered = reorderedPotential.getCPT();

        // The distribution must be identical; getValue is order-independent.
        int numColumns = 1;
        for (int ps : parentStates) numColumns *= ps;
        for (int column = 0; column < numColumns; column++) {
            int[] parentIdx = new int[numParents];
            int rest = column;
            for (int i = 0; i < numParents; i++) {
                parentIdx[i] = rest % parentStates[i];
                rest /= parentStates[i];
            }
            for (int y = 0; y < childStates; y++) {
                int[] config = new int[vars.size()];
                config[0] = y;
                System.arraycopy(parentIdx, 0, config, 1, numParents);
                assertThat(cptReordered.getValue(vars, config))
                        .as("reordered CPT at column %d, child %d", column, y)
                        .isCloseTo(cptOriginal.getValue(vars, config),
                                org.assertj.core.data.Offset.offset(TOLERANCE));
            }
        }
    }

    // ------------------------------------------------------------------ B-ICIcopy: deepCopy immutability

    @Test void mutatingACopyDoesNotAffectTheOriginalMin() throws Exception {
        int[] ps = {3, 2};
        double[][] noisy = {
                {0.0, 0.0, 1.0, 0.0, 0.2, 0.8, 0.7, 0.3, 0.0},
                {0.0, 0.0, 1.0, 0.6, 0.3, 0.1}
        };
        double[] leaky = {0.01, 0.1, 0.89};
        ICIPotential original = buildMinMax(false, 3, ps, noisy, leaky);
        double[] originalCptBefore = original.getCPT().getValues().clone();

        ICIPotential copy = (ICIPotential) original.copy();
        // Change the copy's noisy parameters for its first parent.
        copy.setNoisyParameters(copy.getVariables().get(1),
                new double[]{1.0, 0.0, 0.0, 0.5, 0.5, 0.0, 0.0, 0.0, 1.0});

        assertThat(original.getCPT().getValues())
                .as("mutating the copy's noisy parameters must not change the original's CPT")
                .containsExactly(originalCptBefore, org.assertj.core.data.Offset.offset(TOLERANCE));
    }

    /**
     * B-ICIcopy: {@code deepCopy} used {@code double[][].clone()} (shallow) for the noisy
     * parameters, sharing the inner rows with the original. deepCopy must produce an independent
     * copy, so each parent's noisy-parameter array must be a distinct object.
     */
    @Test void deepCopyOfANetDoesNotShareNoisyParameterRows() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable child = new Variable("Y", 3);
        Variable parent = new Variable("P0", 3);
        net.addNode(child, NodeType.CHANCE);
        net.addNode(parent, NodeType.CHANCE);
        net.addLink(parent, child, true);

        MinPotential potential = new MinPotential(new ArrayList<>(List.of(child, parent)));
        potential.setNoisyParameters(parent, new double[]{0.0, 0.0, 1.0, 0.0, 0.2, 0.8, 0.7, 0.3, 0.0});
        potential.setLeakyParameters(new double[]{0.01, 0.1, 0.89});
        net.getNode(child).setPotential(potential);

        ProbNet copyNet = net.deepCopy();
        ICIPotential copiedPotential = (ICIPotential) copyNet.getNode(copyNet.getVariable("Y"))
                .getPotentials().getFirst();
        Variable copiedParent = copyNet.getVariable("P0");

        assertThat(copiedPotential.getNoisyParameters(copiedParent))
                .as("deepCopy must not share the noisy-parameter row with the original")
                .isNotSameAs(potential.getNoisyParameters(parent));
    }
}
