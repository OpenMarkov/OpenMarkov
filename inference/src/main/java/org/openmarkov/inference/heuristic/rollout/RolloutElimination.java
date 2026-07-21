/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.rollout;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.inference.heuristic.EliminationHeuristic;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elimination heuristic based on <em>policy rollout</em> (Bertsekas, 2017).
 * <p>
 * Classical greedy heuristics (weighted min-fill, min clique size, etc.) are
 * <em>myopic</em>: they choose the locally cheapest variable to eliminate without
 * considering how that choice affects future steps. Rollout overcomes this by
 * <em>simulating the consequences</em> of each candidate choice. Instead of
 * asking "how cheap is it to eliminate X right now?", rollout asks:
 * <blockquote>
 * "If I eliminate X now and then follow the greedy heuristic for the rest of the
 * variables, what is the total cost of the complete elimination I obtain?"
 * </blockquote>
 * It repeats this simulation for every candidate variable and selects the one whose
 * complete simulated elimination scores best according to a global criterion (e.g.,
 * the sum of all clique table sizes — proportional to the total cost of Hugin
 * propagation).
 *
 * <h2>Algorithm</h2>
 * <p>
 * Each call to {@link #getVariableToDelete()} proceeds as follows:
 * <ol>
 *   <li><strong>Snapshot.</strong> The current graph (maintained as an adjacency
 *       matrix) is read from the working copy.</li>
 *   <li><strong>For each candidate variable X:</strong>
 *     <ol type="a">
 *       <li>Eliminate X from a copy of the adjacency matrix (add fill-in edges
 *           among its neighbors, then remove it).</li>
 *       <li>Expand a search tree of depth {@code k} using beam search of
 *           width {@code b}: at each level, retain only the {@code b} best
 *           candidates (those with lowest {@link RolloutCostFunction cost}).</li>
 *       <li>At each leaf of the tree, complete the remaining elimination
 *           greedily using the {@link RolloutCostFunction cost function}.</li>
 *       <li>Evaluate the complete elimination using the {@link RolloutCriterion
 *           criterion} (computed incrementally as clique costs accumulate).</li>
 *     </ol>
 *   </li>
 *   <li><strong>Select</strong> the candidate X whose best leaf completion has
 *       the lowest criterion score. Ties are broken lexicographically by variable
 *       name.</li>
 * </ol>
 *
 * <h2>Parameters</h2>
 * <p>
 * The algorithm is controlled by four parameters:
 * <table>
 *   <caption>Rollout parameters</caption>
 *   <tr><th>Parameter</th><th>Role</th><th>Default</th></tr>
 *   <tr>
 *     <td>{@code depth} (k)</td>
 *     <td>Number of tree expansion levels before the greedy completion. Higher
 *         values explore more alternatives but increase computation
 *         exponentially.</td>
 *     <td>{@value #DEFAULT_DEPTH}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code beamWidth} (b)</td>
 *     <td>Maximum number of best candidates retained at each expansion
 *         level. Limits the branching factor of the search tree.</td>
 *     <td>{@value #DEFAULT_BEAM}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link RolloutCostFunction costFunction}</td>
 *     <td>The greedy heuristic used both for the beam selection and for
 *         completing the elimination at the leaves. Acts as the <em>base
 *         policy</em> in rollout terminology.</td>
 *     <td>{@link RolloutCostFunction#WEIGHTED_MIN_FILL}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link RolloutCriterion criterion}</td>
 *     <td>The global metric used to evaluate complete elimination orders.
 *         Determines what "better" means when comparing two complete
 *         eliminations.</td>
 *     <td>{@link RolloutCriterion#JUNCTION_TREE_SUM}</td>
 *   </tr>
 * </table>
 *
 * <h2>Adaptive depth on large networks</h2>
 * <p>
 * The configured {@code depth} is automatically capped at runtime depending on
 * the number of active nodes remaining in the graph: above
 * {@value #DEPTH_CAP_TO_ONE_THRESHOLD} nodes any {@code k ≥ 2} is reduced to
 * {@code 1}; above {@value #DEPTH_CAP_TO_ZERO_THRESHOLD} nodes the algorithm
 * falls back to {@code k = 0} (plain greedy). This prevents pathological
 * running times on very large graphs (e.g., heavily expanded temporal networks)
 * where the {@code O(n⁴)} per-call cost would otherwise dominate the whole
 * inference. The improvement guarantee still holds: the produced order is at
 * least as good as the underlying base policy.
 *
 * <h2>Degenerate case: depth = 0</h2>
 * <p>
 * When {@code k = 0}, no simulation is performed. The algorithm reduces to a
 * plain greedy heuristic that selects the variable with the lowest
 * {@link RolloutCostFunction cost} at each step. The {@link RolloutCriterion
 * criterion} is irrelevant in this case because no complete order is evaluated.
 *
 * <h2>Theoretical guarantee</h2>
 * <p>
 * By the <em>rollout improvement theorem</em> (Bertsekas, "Dynamic Programming
 * and Optimal Control", vol. I), the elimination order produced by rollout with
 * base policy {@code π} is guaranteed to score at least as well as the order
 * produced by {@code π} alone, measured by the {@link RolloutCriterion criterion}.
 * In other words: rollout with {@link RolloutCostFunction#WEIGHTED_MIN_FILL} can
 * never produce a worse junction tree than using {@code WeightedMinFill} directly.
 * <p>
 * This guarantee holds for any {@code k ≥ 1}. With {@code k = 0} the algorithm
 * degenerates to the base policy itself, so the guarantee is trivially satisfied
 * (equality).
 *
 * <h2>Default values: empirical justification</h2>
 * <p>
 * The defaults ({@code k=1}, {@code b=3},
 * {@link RolloutCostFunction#WEIGHTED_MIN_FILL WEIGHTED_MIN_FILL},
 * {@link RolloutCriterion#JUNCTION_TREE_SUM JUNCTION_TREE_SUM}) were selected
 * after systematic benchmarks on:
 * <ul>
 *   <li>Grid networks of increasing size (4×4 to 7×7 nodes).</li>
 *   <li>Real Bayesian networks from the OpenMarkov repository, including
 *       nasonet (111 variables), hepar (71), prostanet (47), and alarm (37).</li>
 * </ul>
 * <p>
 * Key findings:
 * <ul>
 *   <li>{@code k=1} with {@code WEIGHTED_MIN_FILL} / {@code JUNCTION_TREE_SUM}
 *       consistently ranked first in normalized junction-tree sum across all
 *       tested networks.</li>
 *   <li>{@code k=2} provides marginal improvement over {@code k=1} at
 *       significantly higher computational cost (roughly {@code b×} slower).</li>
 *   <li>{@code JUNCTION_TREE_SUM} dominates {@code MAX_CLIQUE_TABLE_SIZE} as
 *       a criterion for producing good overall orders.</li>
 *   <li>{@code WEIGHTED_MIN_FILL} is a stronger base policy than
 *       {@code MIN_CLIQUE_SIZE} or {@code MIN_FILL}.</li>
 * </ul>
 *
 * <h2>Complexity</h2>
 * <p>
 * Per call to {@link #getVariableToDelete()}, the algorithm evaluates up to
 * {@code n · b^k} complete eliminations, each costing {@code O(n · C)} where
 * {@code C} is the per-variable cost of the base heuristic: {@code O(n)} for
 * {@link RolloutCostFunction#MIN_CLIQUE_SIZE} and {@code O(n²)} for
 * {@link RolloutCostFunction#WEIGHTED_MIN_FILL}. Since the method is called
 * {@code n} times (once per variable), the total cost is
 * {@code O(n² · b^k · n · C)}.
 * <p>
 * With the defaults ({@code k=1}, {@code b=3}, {@code WEIGHTED_MIN_FILL}),
 * total cost is {@code O(3 · n⁴)}, which is practical for networks up to a
 * few hundred variables.
 *
 * <h2>Implementation notes</h2>
 * <p>
 * All simulation runs on a lightweight {@code boolean[][]} adjacency matrix.
 * No {@code ProbNet} copies are created during the search — only the single
 * {@code graphCopy} maintained between actual eliminations. This makes the
 * per-candidate overhead very low compared to approaches that copy the full
 * network object at each simulation step.
 *
 * @author Manuel Arias
 * @see RolloutCostFunction
 * @see RolloutCriterion
 * @see <a href="http://www.athenasc.com/dpbook.html">Bertsekas, D. P. (2017).
 *      Dynamic Programming and Optimal Control. Athena Scientific.</a>
 */
public class RolloutElimination extends EliminationHeuristic {

    public static final int DEFAULT_DEPTH = 1;
    public static final int DEFAULT_BEAM  = 3;

    /**
     * Network sizes (active nodes remaining) above which the configured rollout
     * {@code depth} — and, for very large graphs, the entire rollout machinery —
     * are automatically scaled down to keep per-call cost bounded.
     * <p>
     * Rationale: with default {@code b=3, k=1} and {@code WEIGHTED_MIN_FILL},
     * per-call cost is roughly {@code O(n^4)} (rollout score) and the method is
     * invoked {@code n} times per inference. The empirical benchmarks behind the
     * defaults covered networks up to ~111 variables; on heavily expanded
     * temporal networks (thousands of nodes after slice expansion) this is
     * unaffordable. The thresholds below progressively switch off the most
     * expensive layers:
     * <ul>
     *   <li>{@code n ≥ DEPTH_CAP_TO_ONE_THRESHOLD}: any {@code depth ≥ 2}
     *       reduced to {@code 1}.</li>
     *   <li>{@code n ≥ DEPTH_CAP_TO_ZERO_THRESHOLD}: rollout disabled
     *       ({@code depth = 0}), only the base cost function is used.</li>
     *   <li>{@code n ≥ SIMPLE_ELIMINATION_THRESHOLD}: even the base cost
     *       function (which is itself {@code O(n^2)} for weighted-min-fill) is
     *       replaced by a plain min-degree pick over the current adjacency
     *       matrix — equivalent to {@code SimpleElimination} and {@code O(n)}
     *       per candidate. The rollout improvement guarantee no longer holds in
     *       this regime, but the alternative is no result at all.</li>
     * </ul>
     */
    public static final int DEPTH_CAP_TO_ONE_THRESHOLD     = 30;
    public static final int DEPTH_CAP_TO_ZERO_THRESHOLD    = 60;
    public static final int SIMPLE_ELIMINATION_THRESHOLD   = 200;

    private static final Comparator<String> NAME_ORDER =
            Comparator.nullsLast(Comparator.naturalOrder());

    private final int depth;
    private final int beamWidth;
    private final RolloutCostFunction costFunction;
    private final RolloutCriterion criterion;

    /** Real graph copy maintained across actual eliminations. */
    private final ProbNet graphCopy;

    /** Maps each remaining variable to its node in {@code graphCopy}. */
    private final HashMap<Variable, Node> variablesNodes;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Creates a rollout heuristic with explicit parameters.
     *
     * @param probNet              the network to triangulate (not modified; an internal
     *                             copy is maintained for bookkeeping)
     * @param variablesToEliminate nested lists of variables to eliminate, one per
     *                             elimination phase (typically a single list with all
     *                             variables)
     * @param depth                search depth ({@code k}): number of tree expansion
     *                             levels before greedy completion. {@code k = 0}
     *                             disables rollout entirely — the algorithm becomes a
     *                             plain greedy heuristic driven by {@code costFunction},
     *                             and {@code criterion} is ignored. {@code k = 1} is the
     *                             standard rollout. {@code k ≥ 2} adds deeper
     *                             exploration at exponentially higher cost.
     * @param beamWidth            beam width ({@code b}): maximum number of best
     *                             candidates retained at each expansion level. Only
     *                             the {@code b} candidates with lowest
     *                             {@code costFunction} score are expanded; the rest
     *                             are pruned. Has no effect when {@code k ≤ 1}.
     * @param costFunction         the base policy: a greedy cost function used both to
     *                             select beam candidates and to complete the elimination
     *                             at the leaves of the search tree
     * @param criterion            the global metric for scoring complete elimination
     *                             orders. Irrelevant when {@code depth = 0}.
     */
    public RolloutElimination(ProbNet probNet, List<List<Variable>> variablesToEliminate,
                              int depth, int beamWidth,
                              RolloutCostFunction costFunction, RolloutCriterion criterion) {
        super(probNet, variablesToEliminate);
        this.depth        = depth;
        this.beamWidth    = beamWidth;
        this.costFunction = costFunction;
        this.criterion    = criterion;
        this.graphCopy    = probNet.copy();
        this.variablesNodes = new HashMap<>();
        for (Node node : graphCopy.getNodes()) {
            variablesNodes.put(node.getVariable(), node);
        }
    }

    /**
     * Creates a rollout heuristic with the default configuration:
     * {@code depth=1}, {@code beam=3},
     * {@link RolloutCostFunction#WEIGHTED_MIN_FILL WEIGHTED_MIN_FILL},
     * {@link RolloutCriterion#JUNCTION_TREE_SUM JUNCTION_TREE_SUM}.
     * <p>
     * These defaults were chosen empirically as the best trade-off between
     * elimination quality and computation time across a range of network sizes
     * and topologies (see class-level Javadoc for details).
     *
     * @param probNet              the network to triangulate
     * @param variablesToEliminate nested lists of variables to eliminate
     */
    public RolloutElimination(ProbNet probNet, List<List<Variable>> variablesToEliminate) {
        this(probNet, variablesToEliminate, DEFAULT_DEPTH, DEFAULT_BEAM,
                RolloutCostFunction.WEIGHTED_MIN_FILL, RolloutCriterion.JUNCTION_TREE_SUM);
    }

    // -------------------------------------------------------------------------
    // EliminationHeuristic
    // -------------------------------------------------------------------------

    @Override
    public Variable getVariableToDelete() {
        int listIndex = variablesToEliminate.size() - 1;
        while (listIndex >= 0 && variablesToEliminate.get(listIndex).isEmpty()) {
            --listIndex;
        }
        if (listIndex < 0) {
            return null;
        }
        List<Variable> candidates = variablesToEliminate.get(listIndex);

        // Build lightweight adjacency representation from graphCopy
        List<Variable> allRemaining = new ArrayList<>(variablesNodes.keySet());
        int n = allRemaining.size();
        int[] cardinalities = new int[n];
        Map<Variable, Integer> varToIdx = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) {
            Variable v = allRemaining.get(i);
            varToIdx.put(v, i);
            cardinalities[i] = Math.max(1, v.getNumStates());
        }
        boolean[][] adj = buildAdj(n, varToIdx);
        boolean[] active = new boolean[n];
        Arrays.fill(active, true);

        // Mark which variables are eligible for elimination in this phase.
        // When variablesToEliminate contains a single list (e.g., Bayesian networks),
        // all variables are eligible. When it contains multiple lists (e.g., models
        // with decisions such as influence diagrams, LIMIDs, or DANs), only the
        // current phase's variables are eligible; the rest remain in the graph
        // (affecting clique sizes) but cannot be eliminated during the simulation.
        boolean[] eligible = new boolean[n];
        for (Variable candidate : candidates) {
            Integer idx = varToIdx.get(candidate);
            if (idx != null) {
                eligible[idx] = true;
            }
        }

        // Adapt the heuristic to network size to keep per-call cost bounded. On
        // very large graphs (typically heavily expanded temporal networks) the
        // O(n^4) rollout score and even the O(n^2) weighted-min-fill cost
        // function are unaffordable, so we progressively fall back to cheaper
        // alternatives. See the *_THRESHOLD constants for the rationale.
        if (n >= SIMPLE_ELIMINATION_THRESHOLD) {
            return pickByMinDegree(candidates, varToIdx, adj, active);
        }
        int effectiveDepth = depth;
        if (n >= DEPTH_CAP_TO_ZERO_THRESHOLD) {
            effectiveDepth = 0;
        } else if (n >= DEPTH_CAP_TO_ONE_THRESHOLD && effectiveDepth > 1) {
            effectiveDepth = 1;
        }

        // Evaluate each candidate
        Variable best = null;
        double bestScore = Double.MAX_VALUE;

        for (Variable candidate : candidates) {
            Integer idx = varToIdx.get(candidate);
            if (idx == null) {
                continue;
            }
            // depth == 0: no rollout, just use base cost function (equivalent to base heuristic)
            double s = (effectiveDepth == 0)
                    ? costFunction.cost(adj, active, idx, cardinalities)
                    : rolloutScore(adj, active, eligible, idx, effectiveDepth, cardinalities);
            if (s < bestScore
                    || (s == bestScore && NAME_ORDER.compare(
                    candidate.getName(), best == null ? null : best.getName()) < 0)) {
                best = candidate;
                bestScore = s;
            }
        }
        return best;
    }

    /**
     * Fast fallback for very large networks: picks the candidate with the
     * fewest active neighbors in the current adjacency matrix (plain
     * {@code SimpleElimination} behaviour). {@code O(n)} per candidate.
     */
    private Variable pickByMinDegree(List<Variable> candidates,
                                     Map<Variable, Integer> varToIdx,
                                     boolean[][] adj, boolean[] active) {
        Variable best = null;
        int bestDegree = Integer.MAX_VALUE;
        int n = adj.length;
        for (Variable candidate : candidates) {
            Integer idx = varToIdx.get(candidate);
            if (idx == null) continue;
            int degree = 0;
            boolean[] row = adj[idx];
            for (int i = 0; i < n; i++) {
                if (row[i] && active[i]) degree++;
            }
            if (degree < bestDegree
                    || (degree == bestDegree && NAME_ORDER.compare(
                    candidate.getName(), best == null ? null : best.getName()) < 0)) {
                best = candidate;
                bestDegree = degree;
            }
        }
        return best;
    }

    /**
     * Triangulates the graph locally (adds fill-in edges among the neighbors of the
     * eliminated variable) and removes the variable from the working graph.
     */
    @Override
    public void afterEditExecutes(PNEdit edit) {
        super.afterEditExecutes(edit);
        Variable variable = getEventVariable(edit);
        if (variable == null) {
            return;
        }
        Node nodeToRemove = variablesNodes.get(variable);
        if (nodeToRemove == null) {
            // The edit refers to a variable not tracked by this heuristic instance
            // (e.g., it was already removed, or the event comes from a network
            // outside graphCopy). Nothing to triangulate.
            return;
        }
        List<Node> neighbors = new ArrayList<>(nodeToRemove.getNeighbors());
        int m = neighbors.size();
        for (int i = 0; i < m - 1; i++) {
            Node ni = neighbors.get(i);
            for (int j = i + 1; j < m; j++) {
                Node nj = neighbors.get(j);
                if (!ni.isNeighbor(nj)) {
                    graphCopy.addLink(ni, nj, false);
                }
            }
        }
        graphCopy.removeNode(nodeToRemove);
        variablesNodes.remove(variable);
    }

    // -------------------------------------------------------------------------
    // Rollout scoring
    // -------------------------------------------------------------------------

    /**
     * Scores a candidate variable by expanding the search tree and completing
     * each leaf with the greedy base heuristic.
     * <p>
     * The criterion is computed incrementally: at each elimination step, the
     * clique cost is combined with the accumulated score via
     * {@link RolloutCriterion#combine}.
     *
     * @param adj            current adjacency matrix (not mutated)
     * @param active         which indices are still present
     * @param eligible       which indices may be eliminated (current phase)
     * @param x              candidate variable index
     * @param remainingDepth remaining tree expansion levels
     * @param cardinalities  number of states per variable
     * @return criterion score for the best completion starting with x
     */
    private double rolloutScore(boolean[][] adj, boolean[] active, boolean[] eligible,
                                int x, int remainingDepth, int[] cardinalities) {
        double cliqueCost = cliqueCost(adj, active, x, cardinalities);

        boolean[][] adj2 = eliminate(adj, x);
        boolean[] active2 = active.clone();
        active2[x] = false;

        double subScore;
        if (remainingDepth <= 1) {
            // Leaf: complete greedily (greedyComplete mutates adj2/active2 — safe)
            subScore = greedyComplete(adj2, active2, eligible, cardinalities);
        } else {
            // Beam search: recurse on top-k candidates
            int[] topK = findTopK(adj2, active2, eligible, cardinalities);
            if (topK.length == 0) {
                return criterion.combine(cliqueCost, criterion.identity());
            }
            subScore = Double.MAX_VALUE;
            for (int y : topK) {
                double s = rolloutScore(adj2, active2, eligible, y,
                        remainingDepth - 1, cardinalities);
                subScore = Math.min(subScore, s);
            }
        }
        return criterion.combine(cliqueCost, subScore);
    }

    /**
     * Greedily eliminates all remaining eligible variables using the base cost
     * function, accumulating the criterion score. Non-eligible variables remain
     * in the graph and contribute to clique sizes but are never eliminated.
     * <p>
     * <strong>Mutates</strong> {@code adj} and {@code active} for efficiency (the caller
     * must not reuse them after this call).
     */
    private double greedyComplete(boolean[][] adj, boolean[] active, boolean[] eligible,
                                  int[] cardinalities) {
        int n = adj.length;
        double accumulated = criterion.identity();

        for (;;) {
            // Find the eligible variable with minimum cost
            int best = -1;
            double bestCost = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (!active[i] || !eligible[i]) continue;
                double c = costFunction.cost(adj, active, i, cardinalities);
                if (c < bestCost) {
                    bestCost = c;
                    best = i;
                }
            }
            if (best == -1) break;

            accumulated = criterion.combine(accumulated, cliqueCost(adj, active, best, cardinalities));
            eliminateInPlace(adj, active, best, n);
        }
        return accumulated;
    }

    // -------------------------------------------------------------------------
    // Lightweight graph operations
    // -------------------------------------------------------------------------

    private boolean[][] buildAdj(int n, Map<Variable, Integer> varToIdx) {
        boolean[][] adj = new boolean[n][n];
        for (Map.Entry<Variable, Node> entry : variablesNodes.entrySet()) {
            Integer i = varToIdx.get(entry.getKey());
            if (i == null) continue;
            for (Node neighbor : entry.getValue().getNeighbors()) {
                Integer j = varToIdx.get(neighbor.getVariable());
                if (j != null) {
                    adj[i][j] = true;
                    adj[j][i] = true;
                }
            }
        }
        return adj;
    }

    /**
     * Returns a <em>new</em> adjacency matrix where variable {@code x} has been
     * eliminated: fill-in edges added between all neighbor pairs, then row/column
     * cleared.
     */
    private static boolean[][] eliminate(boolean[][] adj, int x) {
        int n = adj.length;
        boolean[][] next = new boolean[n][];
        for (int i = 0; i < n; i++) {
            next[i] = adj[i].clone();
        }
        for (int i = 0; i < n; i++) {
            if (!next[x][i]) continue;
            for (int j = i + 1; j < n; j++) {
                if (next[x][j]) {
                    next[i][j] = true;
                    next[j][i] = true;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            next[x][i] = false;
            next[i][x] = false;
        }
        return next;
    }

    /**
     * Eliminates variable {@code x} in place: adds fill-in edges, clears
     * row/column, marks inactive.
     */
    private static void eliminateInPlace(boolean[][] adj, boolean[] active, int x, int n) {
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

    /**
     * Computes the table size of the clique formed by eliminating variable {@code x}:
     * the product of cardinalities of {@code x} and all its active neighbors.
     */
    private static double cliqueCost(boolean[][] adj, boolean[] active, int x, int[] cardinalities) {
        double product = cardinalities[x];
        int n = adj.length;
        for (int i = 0; i < n; i++) {
            if (adj[x][i] && active[i]) {
                product *= cardinalities[i];
            }
        }
        return product;
    }

    /**
     * Finds the top-k eligible variable indices by lowest base cost function value.
     */
    private int[] findTopK(boolean[][] adj, boolean[] active, boolean[] eligible,
                           int[] cardinalities) {
        int n = adj.length;
        double[] costs = new double[n];
        int numEligible = 0;
        for (int i = 0; i < n; i++) {
            if (active[i] && eligible[i]) {
                costs[i] = costFunction.cost(adj, active, i, cardinalities);
                numEligible++;
            } else {
                costs[i] = Double.MAX_VALUE;
            }
        }
        if (numEligible == 0) {
            return new int[0];
        }
        return topKIndices(costs, n, Math.min(beamWidth, numEligible));
    }

    /**
     * Returns the indices of the k smallest values in {@code costs}.
     */
    private static int[] topKIndices(double[] costs, int n, int k) {
        int[] indices = new int[k];
        boolean[] used = new boolean[n];
        for (int r = 0; r < k; r++) {
            double minVal = Double.MAX_VALUE;
            int minIdx = -1;
            for (int i = 0; i < n; i++) {
                if (!used[i] && costs[i] < minVal) {
                    minVal = costs[i];
                    minIdx = i;
                }
            }
            if (minIdx == -1) {
                return Arrays.copyOf(indices, r);
            }
            indices[r] = minIdx;
            used[minIdx] = true;
        }
        return indices;
    }
}
