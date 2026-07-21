package org.openmarkov.learning.metric;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.InvertLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.learning.metric.aic.AICMetric;
import org.openmarkov.learning.metric.bayesian.BayesianMetric;
import org.openmarkov.learning.metric.bde.BDeMetric;
import org.openmarkov.learning.metric.entropy.EntropyMetric;
import org.openmarkov.learning.metric.k2.K2Metric;
import org.openmarkov.learning.metric.mdlm.MDLMetric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based generalization of the differential metric oracle: for a random directed
 * acyclic base structure and every applicable single edit, the incremental delta reported
 * by a metric must equal the difference of scoring the resulting structure and the base
 * structure from scratch. Checked for all six metrics on four variables of different arity.
 * <p>
 * Base structures are built from the six "forward" edges of a fixed topological order, so
 * they are always acyclic; candidate edits that would create a cycle are skipped.
 */
class MetricScoringPropertyTest {

    private static final int N = 4;
    private static final List<Variable> VARS = List.of(
            new Variable("V0", 2), new Variable("V1", 3), new Variable("V2", 2), new Variable("V3", 4));
    private static final int[][] CASES = buildCases();
    private static final double TOL = 1e-6;

    /** The six edges i->j with i < j (a fixed topological order), one per bit of the mask. */
    private static final int[][] FORWARD = { { 0, 1 }, { 0, 2 }, { 0, 3 }, { 1, 2 }, { 1, 3 }, { 2, 3 } };

    private static final Map<String, Supplier<Metric>> METRICS = new LinkedHashMap<>();

    static {
        METRICS.put("Entropy", EntropyMetric::new);
        METRICS.put("AIC", AICMetric::new);
        METRICS.put("MDL", MDLMetric::new);
        METRICS.put("K2", K2Metric::new);
        METRICS.put("Bayesian", () -> new BayesianMetric(1.0));
        METRICS.put("BDe", () -> new BDeMetric(1.0));
    }

    @Property(tries = 200)
    void incrementalDeltaMatchesRecompute(@ForAll @IntRange(min = 0, max = 63) int structureMask) {
        int[][] base = decode(structureMask);
        List<String> mismatches = new ArrayList<>();
        for (Map.Entry<String, Supplier<Metric>> metric : METRICS.entrySet()) {
            checkAllEdits(metric.getKey(), metric.getValue(), base, mismatches);
        }
        assertTrue(mismatches.isEmpty(),
                "structure=" + Arrays.deepToString(base) + " mismatches=" + mismatches);
    }

    private static void checkAllEdits(String name, Supplier<Metric> factory, int[][] base, List<String> mismatches) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i == j) {
                    continue;
                }
                if (contains(base, i, j)) {
                    check(name, factory, base, remove(base, i, j), new RemoveKind(i, j), mismatches);
                    check(name, factory, base, invert(base, i, j), new InvertKind(i, j), mismatches);
                } else {
                    check(name, factory, base, add(base, i, j), new AddKind(i, j), mismatches);
                }
            }
        }
    }

    private static void check(String name, Supplier<Metric> factory, int[][] base, int[][] result,
            EditKind kind, List<String> mismatches) {
        ProbNet baseNet = netOrNull(base);
        ProbNet resultNet = netOrNull(result);
        if (baseNet == null || resultNet == null) {
            return; // edit not applicable (would create a cycle)
        }
        Metric metric = factory.get();
        metric.init(baseNet, database());
        metric.getScore(); // populate caches before asking for a delta
        double incremental = metric.score(kind.edit(baseNet));

        double expected = fullScore(factory, resultNet) - fullScore(factory, baseNet);
        if (Math.abs(incremental - expected) > TOL) {
            mismatches.add(String.format("%s %s: incremental=%.6f recompute=%.6f", name, kind, incremental, expected));
        }
    }

    // --- edit kinds -----------------------------------------------------------------------------

    private interface EditKind {
        PNEdit edit(ProbNet net);
    }

    private record AddKind(int i, int j) implements EditKind {
        public PNEdit edit(ProbNet net) {
            return new AddLinkEdit(net, VARS.get(i), VARS.get(j), true);
        }
        public String toString() {
            return "add V" + i + "->V" + j;
        }
    }

    private record RemoveKind(int i, int j) implements EditKind {
        public PNEdit edit(ProbNet net) {
            return new RemoveLinkEdit(net, VARS.get(i), VARS.get(j), true);
        }
        public String toString() {
            return "remove V" + i + "->V" + j;
        }
    }

    private record InvertKind(int i, int j) implements EditKind {
        public PNEdit edit(ProbNet net) {
            return new InvertLinkEdit(net, VARS.get(i), VARS.get(j), true);
        }
        public String toString() {
            return "invert V" + i + "->V" + j;
        }
    }

    // --- structure helpers ----------------------------------------------------------------------

    private static int[][] decode(int mask) {
        List<int[]> links = new ArrayList<>();
        for (int bit = 0; bit < FORWARD.length; bit++) {
            if ((mask & (1 << bit)) != 0) {
                links.add(FORWARD[bit]);
            }
        }
        return links.toArray(new int[0][]);
    }

    private static boolean contains(int[][] links, int i, int j) {
        for (int[] l : links) {
            if (l[0] == i && l[1] == j) {
                return true;
            }
        }
        return false;
    }

    private static int[][] add(int[][] links, int i, int j) {
        List<int[]> out = new ArrayList<>(Arrays.asList(links));
        out.add(new int[] { i, j });
        return out.toArray(new int[0][]);
    }

    private static int[][] remove(int[][] links, int i, int j) {
        List<int[]> out = new ArrayList<>();
        for (int[] l : links) {
            if (!(l[0] == i && l[1] == j)) {
                out.add(l);
            }
        }
        return out.toArray(new int[0][]);
    }

    private static int[][] invert(int[][] links, int i, int j) {
        return add(remove(links, i, j), j, i);
    }

    // --- scoring helpers ------------------------------------------------------------------------

    private static CaseDatabase database() {
        return new CaseDatabase(new ArrayList<>(VARS), CASES);
    }

    /** Builds the net or returns null if any link is rejected (e.g. it would create a cycle). */
    private static ProbNet netOrNull(int[][] links) {
        try {
            ProbNet net = new ProbNet();
            for (Variable v : VARS) {
                net.addNode(v, NodeType.CHANCE);
            }
            for (int[] l : links) {
                new AddLinkEdit(net, VARS.get(l[0]), VARS.get(l[1]), true).executeEdit();
            }
            return net;
        } catch (Exception e) {
            return null;
        }
    }

    private static double fullScore(Supplier<Metric> factory, ProbNet net) {
        Metric metric = factory.get();
        metric.init(net, database());
        return metric.getScore();
    }

    private static int[][] buildCases() {
        int m = 24;
        int[][] c = new int[m][N];
        for (int r = 0; r < m; r++) {
            c[r][0] = r % 2;
            c[r][1] = r % 3;
            c[r][2] = (r / 2) % 2;
            c[r][3] = r % 4;
        }
        return c;
    }
}
