/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */
package org.openmarkov.gui.layout.bayesian;

import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.Point2D;
import org.openmarkov.core.model.network.ProbNet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Stress-majorization layout with a directional bias for Bayesian DAGs.
 *
 * <p>Standard SM minimises
 * &nbsp;&nbsp;&nbsp;&nbsp;Σ<sub>i&lt;j</sub> w<sub>ij</sub> (‖x<sub>i</sub>−x<sub>j</sub>‖ − d<sub>ij</sub>)²
 * with d<sub>ij</sub> = graph distance · {@link StressLayoutOptions#idealEdgeLength}
 * and w<sub>ij</sub> = 1/d<sub>ij</sub>².
 *
 * <p>To preserve causal readability ("parent above child") an extra
 * quadratic term is added on directed links (p, c):
 * &nbsp;&nbsp;&nbsp;&nbsp;λ · (y<sub>c</sub> − y<sub>p</sub> − δ)²
 * which is folded into the y-axis Jacobi update. The horizontal axis is
 * left to plain SM, so layouts stay continuous (no discrete layers).
 */
public final class StressLayout {

    private static final double EPS = 1e-9;

    public Map<String, Point2D.Double> compute(ProbNet probNet) {
        return compute(probNet, new StressLayoutOptions());
    }

    public Map<String, Point2D.Double> compute(ProbNet probNet,
                                               StressLayoutOptions opts) {
        List<Node> nodes = probNet.getNodes();
        int n = nodes.size();
        Map<String, Point2D.Double> out = new HashMap<>(n * 2);
        if (n == 0) return out;
        if (n == 1) {
            out.put(nodes.get(0).getName(),
                    new Point2D.Double(opts.originX, opts.originY));
            return out;
        }

        Map<Node, Integer> idx = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) idx.put(nodes.get(i), i);

        int[][] hops = shortestPathHops(probNet, nodes, idx);

        // Compute targets d_ij and weights w_ij.
        double[][] d = new double[n][n];
        double[][] w = new double[n][n];
        int diameter = 0;
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                if (hops[i][j] != Integer.MAX_VALUE && hops[i][j] > diameter)
                    diameter = hops[i][j];
        int cap = Math.max(1, opts.maxHopsForIdealDistance);
        double disconnectedHops = Math.max(1,
                Math.min(cap, diameter)) * opts.disconnectedDistanceMultiplier;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double hijk;
                if (hops[i][j] == Integer.MAX_VALUE) hijk = disconnectedHops;
                else hijk = Math.min(cap, hops[i][j]);
                d[i][j] = d[j][i] = opts.idealEdgeLength * hijk;
                double inv = 1.0 / (d[i][j] * d[i][j]);
                w[i][j] = w[j][i] = inv;
            }
        }

        // Directed edge list (parent index, child index) for the bias term.
        List<int[]> directed = new ArrayList<>();
        for (Link<Node> link : probNet.getLinks()) {
            if (!link.isDirected()) continue;
            Integer p = idx.get(link.getFrom());
            Integer c = idx.get(link.getTo());
            if (p == null || c == null || p.equals(c)) continue;
            directed.add(new int[] { p, c });
        }

        // Seed positions: keep current coordinates if any node has been
        // placed already (caller may have run placeNodesInCircle first); a
        // tiny deterministic perturbation breaks symmetries that would
        // otherwise stall majorization at a saddle point.
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            Node node = nodes.get(i);
            x[i] = node.getCoordinateX() + 0.001 * i;
            y[i] = node.getCoordinateY() + 0.001 * (i % 7);
        }

        double prevStress = stress(x, y, d, w, directed,
                                   opts.lambdaDirectional,
                                   opts.directionalDelta);
        for (int it = 0; it < opts.maxIterations; it++) {
            jacobiSweep(x, y, d, w, directed,
                        opts.lambdaDirectional, opts.directionalDelta);
            double s = stress(x, y, d, w, directed,
                              opts.lambdaDirectional,
                              opts.directionalDelta);
            double rel = (prevStress > EPS) ? (prevStress - s) / prevStress : 0;
            prevStress = s;
            if (rel >= 0 && rel < opts.convergenceEpsilon) break;
        }

        // Translate so the bounding box starts at (originX, originY).
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            if (x[i] < minX) minX = x[i];
            if (y[i] < minY) minY = y[i];
        }
        double dx = opts.originX - minX, dy = opts.originY - minY;
        for (int i = 0; i < n; i++) { x[i] += dx; y[i] += dy; }

        if (opts.minBoxSeparation > 0) removeOverlaps(x, y, opts);

        for (int i = 0; i < n; i++)
            out.put(nodes.get(i).getName(), new Point2D.Double(x[i], y[i]));
        return out;
    }

    // ------------------------------------------------------------------
    // Shortest-path hop counts (BFS from each node — O(n·(n+m)), simpler
    // and faster than Floyd-Warshall for the sparse graphs we handle).
    // ------------------------------------------------------------------
    private int[][] shortestPathHops(ProbNet probNet, List<Node> nodes,
                                     Map<Node, Integer> idx) {
        int n = nodes.size();
        // Build undirected adjacency once: for layout purposes a parent-
        // child link should pull both ends together, regardless of arrow
        // direction. The directional bias on y is handled separately.
        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (Link<Node> link : probNet.getLinks()) {
            Integer a = idx.get(link.getFrom());
            Integer b = idx.get(link.getTo());
            if (a == null || b == null || a.equals(b)) continue;
            adj.get(a).add(b);
            adj.get(b).add(a);
        }
        int[][] hops = new int[n][n];
        for (int[] row : hops) Arrays.fill(row, Integer.MAX_VALUE);
        for (int s = 0; s < n; s++) {
            hops[s][s] = 0;
            LinkedList<Integer> q = new LinkedList<>();
            q.add(s);
            while (!q.isEmpty()) {
                int u = q.poll();
                for (int v : adj.get(u)) {
                    if (hops[s][v] == Integer.MAX_VALUE) {
                        hops[s][v] = hops[s][u] + 1;
                        q.add(v);
                    }
                }
            }
        }
        return hops;
    }

    // ------------------------------------------------------------------
    // One Jacobi-style majorization sweep.
    //
    // For SM, the update for each node i comes from minimising the local
    // quadratic majorant: x_i ← (Σ_j w_ij·(x_j + d_ij·u_ij_x)) / Σ_j w_ij,
    // where u_ij is the unit vector from j to i at the current iterate.
    //
    // The directional term adds, for every directed edge (p, c):
    //   contribution to y_c numerator: λ·(y_p + δ),    denominator: λ
    //   contribution to y_p numerator: λ·(y_c − δ),    denominator: λ
    // Nothing on x.
    // ------------------------------------------------------------------
    private void jacobiSweep(double[] x, double[] y,
                             double[][] d, double[][] w,
                             List<int[]> directed,
                             double lambda, double delta) {
        int n = x.length;
        double[] nx = new double[n], ny = new double[n];
        double[] dx = new double[n], dy = new double[n];

        for (int i = 0; i < n; i++) {
            double sumW = 0;
            double accX = 0, accY = 0;
            for (int j = 0; j < n; j++) {
                if (j == i) continue;
                double dxij = x[i] - x[j];
                double dyij = y[i] - y[j];
                double dist = Math.sqrt(dxij * dxij + dyij * dyij);
                if (dist < EPS) dist = EPS;
                double wij = w[i][j];
                accX += wij * (x[j] + d[i][j] * dxij / dist);
                accY += wij * (y[j] + d[i][j] * dyij / dist);
                sumW += wij;
            }
            nx[i] = accX;
            ny[i] = accY;
            dx[i] = sumW;
            dy[i] = sumW;
        }
        if (lambda > 0) {
            for (int[] e : directed) {
                int p = e[0], c = e[1];
                // y_c should be y_p + delta; y_p should be y_c - delta.
                ny[c] += lambda * (y[p] + delta);
                dy[c] += lambda;
                ny[p] += lambda * (y[c] - delta);
                dy[p] += lambda;
            }
        }
        for (int i = 0; i < n; i++) {
            if (dx[i] > EPS) x[i] = nx[i] / dx[i];
            if (dy[i] > EPS) y[i] = ny[i] / dy[i];
        }
    }

    private double stress(double[] x, double[] y,
                          double[][] d, double[][] w,
                          List<int[]> directed,
                          double lambda, double delta) {
        int n = x.length;
        double s = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double dxij = x[i] - x[j];
                double dyij = y[i] - y[j];
                double dist = Math.sqrt(dxij * dxij + dyij * dyij);
                double r = dist - d[i][j];
                s += w[i][j] * r * r;
            }
        }
        if (lambda > 0) {
            for (int[] e : directed) {
                double r = y[e[1]] - y[e[0]] - delta;
                s += lambda * r * r;
            }
        }
        return s;
    }

    // ------------------------------------------------------------------
    // Overlap removal: simple iterative pair-pushing along the shorter
    // overlap axis. Bounded by maxOverlapPasses.
    // ------------------------------------------------------------------
    private void removeOverlaps(double[] x, double[] y,
                                StressLayoutOptions opts) {
        int n = x.length;
        double bw = opts.defaultBoxWidth + opts.minBoxSeparation;
        double bh = opts.defaultBoxHeight + opts.minBoxSeparation;
        for (int pass = 0; pass < opts.maxOverlapPasses; pass++) {
            boolean moved = false;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double dxij = x[j] - x[i];
                    double dyij = y[j] - y[i];
                    double overlapX = bw - Math.abs(dxij);
                    double overlapY = bh - Math.abs(dyij);
                    if (overlapX <= 0 || overlapY <= 0) continue;
                    if (overlapX < overlapY) {
                        double push = overlapX / 2.0 + EPS;
                        if (dxij >= 0) { x[j] += push; x[i] -= push; }
                        else            { x[j] -= push; x[i] += push; }
                    } else {
                        double push = overlapY / 2.0 + EPS;
                        if (dyij >= 0) { y[j] += push; y[i] -= push; }
                        else            { y[j] -= push; y[i] += push; }
                    }
                    moved = true;
                }
            }
            if (!moved) break;
        }
    }
}
