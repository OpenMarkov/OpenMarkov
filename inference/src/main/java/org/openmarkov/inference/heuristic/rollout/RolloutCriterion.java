/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.rollout;

/**
 * Global criteria for evaluating the quality of a complete variable elimination order.
 * <p>
 * While the {@link RolloutCostFunction cost function} evaluates the local cost of
 * eliminating a <em>single</em> variable (and thus drives the greedy selection), the
 * <em>criterion</em> evaluates the quality of an <em>entire</em> elimination order.
 * It is the criterion that rollout uses to compare two candidate choices: "if I
 * eliminate X first and complete greedily, the total score is S₁; if I eliminate Y
 * first and complete greedily, the total score is S₂" — the candidate with the
 * lower score wins.
 *
 * <h2>Incremental computation</h2>
 * <p>
 * Each criterion is computed incrementally during the simulated elimination.
 * When variable X is eliminated, the resulting clique has a <em>table size</em>:
 * <pre>
 *   tableSize(X) = ∏  |states(v)|     for v ∈ {X} ∪ neighbors(X)
 * </pre>
 * This table size is fed into {@link #combine(double, double)}, which updates
 * the running score. There is no need to store the full order and re-evaluate
 * it afterwards.
 *
 * <h2>Choosing a criterion</h2>
 * <p>
 * Empirical benchmarks show that {@link #JUNCTION_TREE_SUM} consistently
 * produces better overall junction trees than {@link #MAX_CLIQUE_TABLE_SIZE}.
 * The reason is that minimizing the sum optimizes the <em>total</em>
 * computational effort of Hugin propagation, while minimizing the maximum
 * clique only addresses the single worst bottleneck — potentially at the
 * expense of many moderately large cliques elsewhere.
 *
 * @see RolloutElimination
 * @see RolloutCostFunction
 */
public enum RolloutCriterion {

    /**
     * Maximum clique table size: retains only the largest clique table produced
     * during the entire elimination.
     * <pre>
     *   score = max { tableSize(Xᵢ) : i = 1, …, n }
     * </pre>
     * Measures the <em>memory bottleneck</em> of inference: the single largest
     * table that must fit in memory during junction-tree propagation. Useful
     * when memory is the binding constraint rather than total computation time.
     */
    MAX_CLIQUE_TABLE_SIZE {
        @Override
        public double combine(double accumulated, double cliqueCost) {
            return Math.max(accumulated, cliqueCost);
        }
    },

    /**
     * Junction-tree sum: the sum of all clique table sizes formed during the
     * elimination.
     * <pre>
     *   score = Σ  tableSize(Xᵢ)     for i = 1, …, n
     * </pre>
     * Measures the <em>total computational cost</em> of inference: the sum is
     * proportional to the total number of arithmetic operations during Hugin
     * propagation. This is the recommended criterion for rollout, as it
     * produces the best overall junction trees in empirical benchmarks.
     */
    JUNCTION_TREE_SUM {
        @Override
        public double combine(double accumulated, double cliqueCost) {
            return accumulated + cliqueCost;
        }
    };

    /**
     * Combines the accumulated criterion score with the cost of a new clique.
     *
     * @param accumulated score accumulated so far
     * @param cliqueCost  table size of the clique formed by the current elimination
     * @return the updated accumulated score
     */
    public abstract double combine(double accumulated, double cliqueCost);

    /**
     * @return the identity element for {@link #combine}: 0.0 for both sum and max.
     */
    public double identity() {
        return 0.0;
    }
}
