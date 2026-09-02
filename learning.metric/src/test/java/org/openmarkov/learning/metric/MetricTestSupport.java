package org.openmarkov.learning.metric;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.InvertLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Shared fixture and a differential oracle for the metric tests.
 * <p>
 * The reference dataset uses three variables of <em>different</em> cardinalities
 * ({@code A}: 2 states, {@code B}: 3, {@code C}: 2) so that an inversion between
 * variables of different arity exercises the parent-cardinality bookkeeping that
 * the AIC/MDL dimension penalties depend on.
 * <p>
 * The oracle {@link #deltaMismatches} checks the core contract of an incremental
 * metric: the delta returned by the per-edit {@code score} methods must equal the
 * difference of scoring the resulting structure and the base structure from
 * scratch. It is the same idea as re-scoring the whole net after the edit, so no
 * hand-computed magic numbers are needed and any metric can be checked uniformly.
 */
public final class MetricTestSupport {
    
    private MetricTestSupport() {
    }
    
    public static final int A = 0;
    public static final int B = 1;
    public static final int C = 2;
    
    public static final List<Variable> VARIABLES = List.of(
            new Variable("A", 2), new Variable("B", 3), new Variable("C", 2));
    
    public static final int[][] CASES = {
            {0, 0, 0}, {0, 0, 1}, {0, 1, 0}, {0, 1, 1},
            {1, 1, 0}, {1, 1, 1}, {1, 2, 0}, {1, 2, 1},
            {0, 0, 0}, {1, 2, 1}, {0, 1, 0}, {1, 1, 1}
    };
    
    public static final double TOL = 1e-6;
    
    public static CaseDatabase database() {
        return new CaseDatabase(new ArrayList<>(VARIABLES), CASES);
    }
    
    /** Builds a fresh net over the reference variables with the given directed links. */
    public static ProbNet netWith(int[][] links) {
        ProbNet net = new ProbNet();
        for (Variable v : VARIABLES) {
            net.addNode(v, NodeType.CHANCE);
        }
        for (int[] link : links) {
            try {
                new AddLinkEdit(net, VARIABLES.get(link[0]), VARIABLES.get(link[1]), true).executeEdit();
            } catch (DoEditException e) {
                throw new UnreachableException(e);
            }
        }
        return net;
    }
    
    /** Total score of the given structure, computed from scratch. */
    public static double fullScore(Supplier<? extends Metric> factory, int[][] links) {
        Metric metric = factory.get();
        metric.init(netWith(links), database());
        return metric.getScore();
    }
    
    /** Incremental delta reported by the metric for {@code edit} applied to {@code baseLinks}. */
    private static double incrementalDelta(Supplier<? extends Metric> factory, int[][] baseLinks, PNEdit edit,
                                           ProbNet base) {
        Metric metric = factory.get();
        metric.init(base, database());
        metric.getScore(); // populate cachedNodeScores / dimensions before asking for a delta
        return metric.score(edit);
    }
    
    /**
     * Returns a description of every add/remove/invert whose incremental delta disagrees
     * with the full recompute, over all ordered variable pairs; empty when the metric's
     * incremental scoring is consistent.
     */
    public static List<String> deltaMismatches(Supplier<? extends Metric> factory) {
        List<String> mismatches = new ArrayList<>();
        int n = VARIABLES.size();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                check(factory, mismatches, "add", new int[][]{}, new int[][]{{i, j}}, i, j);
                check(factory, mismatches, "remove", new int[][]{{i, j}}, new int[][]{}, i, j);
                check(factory, mismatches, "invert", new int[][]{{i, j}}, new int[][]{{j, i}}, i, j);
            }
        }
        return mismatches;
    }
    
    private static void check(Supplier<? extends Metric> factory, List<String> mismatches, String kind,
                              int[][] baseLinks, int[][] resultLinks, int i, int j) {
        ProbNet base = netWith(baseLinks);
        Variable from = VARIABLES.get(i);
        Variable to = VARIABLES.get(j);
        PNEdit edit = switch (kind) {
            case "add" -> new AddLinkEdit(base, from, to, true);
            case "remove" -> new RemoveLinkEdit(base, from, to, true);
            default -> new InvertLinkEdit(base, from, to, true);
        };
        double incremental = incrementalDelta(factory, baseLinks, edit, base);
        double expected = fullScore(factory, resultLinks) - fullScore(factory, baseLinks);
        if (Math.abs(incremental - expected) > TOL) {
            mismatches.add(String.format("%s %s->%s: incremental=%.6f recompute=%.6f",
                                         kind, from.getName(), to.getName(), incremental, expected));
        }
    }
}
