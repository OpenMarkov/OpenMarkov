/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.rollout;

/**
 * Local cost functions for greedy variable selection during rollout.
 * <p>
 * In the rollout algorithm, the <em>cost function</em> plays two roles:
 * <ol>
 *   <li><strong>Base policy:</strong> at the leaves of the search tree (and during
 *       the greedy completion phase), the variable with the lowest cost is
 *       eliminated next. The cost function thus acts as the greedy heuristic
 *       that completes the elimination.</li>
 *   <li><strong>Beam selection:</strong> at non-leaf expansion levels, the
 *       {@code b} best candidates (those with lowest cost) are retained for
 *       further exploration; the rest are pruned.</li>
 * </ol>
 * <p>
 * Each function evaluates the <em>immediate, local</em> cost of eliminating a
 * single variable from an undirected graph represented as a {@code boolean[][]}
 * adjacency matrix. It answers the question: "how expensive is it to eliminate
 * this variable <em>right now</em>?" — without considering what happens
 * afterwards. That longer-horizon evaluation is the job of the
 * {@link RolloutCriterion criterion}, which scores the complete elimination.
 * <p>
 * These functions are lightweight, adjacency-matrix equivalents of the full
 * {@link org.openmarkov.core.inference.heuristic.EliminationHeuristic} subclasses.
 * They compute the same cost metric but operate on arrays instead of
 * {@code ProbNet} objects, which makes them suitable for the thousands of
 * simulated eliminations that rollout performs internally.
 *
 * <h2>Choosing a cost function</h2>
 * <p>
 * Empirical benchmarks on networks from 8 to 111 variables show that
 * {@link #WEIGHTED_MIN_FILL} is the strongest base policy for rollout,
 * consistently producing the smallest junction trees. {@link #MIN_CLIQUE_SIZE}
 * is a reasonable alternative when speed matters more than optimality.
 * {@link #MIN_FILL} is included for completeness but generally underperforms
 * the weighted variant.
 *
 * @see RolloutElimination
 * @see RolloutCriterion
 */
public enum RolloutCostFunction {

    /**
     * Weighted min-fill: the cost of eliminating variable X is
     * <pre>
     *   cost(X) = Σ  card(Y) · card(Z)
     *            (Y,Z) ∈ neighbors(X), Y-Z not in E
     * </pre>
     * where the sum ranges over every pair of neighbors of X that are not yet
     * connected. This captures both the <em>number</em> of fill-in edges and
     * their <em>weight</em> (the table size they would create).
     * <p>
     * Equivalent to the standalone heuristic
     * {@link org.openmarkov.inference.heuristic.weightedMinFill.WeightedMinFill
     * WeightedMinFill}. This is the recommended cost function for rollout: it
     * produces the best junction trees in empirical benchmarks.
     */
    WEIGHTED_MIN_FILL {
        @Override
        public double cost(boolean[][] adj, boolean[] active, int x, int[] cardinalities) {
            int n = adj.length;
            long c = 0;
            for (int i = 0; i < n - 1; i++) {
                if (!adj[x][i] || !active[i]) continue;
                long cardI = cardinalities[i];
                for (int j = i + 1; j < n; j++) {
                    if (adj[x][j] && active[j] && !adj[i][j]) {
                        c += cardI * cardinalities[j];
                    }
                }
            }
            return c;
        }
    },

    /**
     * Min clique size: the cost of eliminating variable X is simply the number
     * of variables in the clique that its elimination would form:
     * <pre>
     *   cost(X) = 1 + |neighbors(X)|
     * </pre>
     * This is the cheapest function to compute ({@code O(n)} per evaluation)
     * and favors eliminating variables with few neighbors, which tends to keep
     * cliques small.
     * <p>
     * Equivalent to the standalone heuristic
     * {@link org.openmarkov.inference.heuristic.minimalCliqueSize.minimalCliqueSize
     * minimalCliqueSize} (Kjaerulff, 1993).
     */
    MIN_CLIQUE_SIZE {
        @Override
        public double cost(boolean[][] adj, boolean[] active, int x, int[] cardinalities) {
            int n = adj.length;
            int count = 1;
            for (int i = 0; i < n; i++) {
                if (adj[x][i] && active[i]) count++;
            }
            return count;
        }
    },

    /**
     * Min fill (unweighted): the cost of eliminating variable X is the number
     * of fill-in edges that would need to be added:
     * <pre>
     *   cost(X) = |{ (Y,Z) : Y,Z ∈ neighbors(X), Y-Z not in E }|
     * </pre>
     * Unlike {@link #WEIGHTED_MIN_FILL}, this does not account for the
     * cardinalities of the variables involved. It is the classical min-fill
     * heuristic.
     * <p>
     * Equivalent to the standalone heuristic
     * {@link org.openmarkov.inference.heuristic.minimalFillIn.MinimalFillIn
     * MinimalFillIn}.
     */
    MIN_FILL {
        @Override
        public double cost(boolean[][] adj, boolean[] active, int x, int[] cardinalities) {
            int n = adj.length;
            int fill = 0;
            for (int i = 0; i < n - 1; i++) {
                if (!adj[x][i] || !active[i]) continue;
                for (int j = i + 1; j < n; j++) {
                    if (adj[x][j] && active[j] && !adj[i][j]) {
                        fill++;
                    }
                }
            }
            return fill;
        }
    };

    /**
     * Computes the immediate cost of eliminating variable {@code x} from the graph.
     *
     * @param adj           adjacency matrix (not mutated)
     * @param active        which indices are still present in the graph
     * @param x             index of the variable to evaluate
     * @param cardinalities number of states per variable index
     * @return the cost (lower = better candidate for elimination)
     */
    public abstract double cost(boolean[][] adj, boolean[] active, int x, int[] cardinalities);
}
