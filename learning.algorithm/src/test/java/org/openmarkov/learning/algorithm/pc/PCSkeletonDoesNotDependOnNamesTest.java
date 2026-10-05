package org.openmarkov.learning.algorithm.pc;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.learning.algorithm.pc.independencetester.CrossEntropyIndependenceTester;
import org.openmarkov.learning.core.util.LearningEditProposal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/** The links that the PC algorithm keeps do not depend on the names of the variables. */
class PCSkeletonDoesNotDependOnNamesTest {

    private static final double SIGNIFICANCE = 0.05;

    /**
     * Cases sampled from F -> A, A -> C, E -> C, C -> B, E -> B. Only {C, E}, neighbors of B but not
     * of A, separates A from B, so the link A - B is removed only if the neighbors of B are tried.
     */
    @Test
    void renamingAVariableDoesNotChangeTheSkeleton() throws Exception {
        Set<String> expected = Set.of("F-X", "C-X", "C-E", "B-C", "B-E");

        // Before B in alphabetical order, and after it
        assertThat(skeleton("A")).isEqualTo(expected);
        assertThat(skeleton("G")).isEqualTo(expected);
    }

    /** Links of the learned network, with the renamed variable written as X. */
    private static Set<String> skeleton(String nameOfA) throws Exception {
        List<Variable> variables = new ArrayList<>();
        for (String name : new String[]{"F", nameOfA, "E", "C", "B"}) {
            variables.add(new Variable(name, 2));
        }
        ProbNet net = new ProbNet();
        List<Node> nodes = new ArrayList<>();
        for (Variable variable : variables) {
            nodes.add(net.addNode(variable, NodeType.CHANCE));
        }
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                net.addLink(nodes.get(i), nodes.get(j), false);
            }
        }
        PCAlgorithm pc = new PCAlgorithm(net, new CaseDatabase(variables, cases()), 0.5,
                new CrossEntropyIndependenceTester(), SIGNIFICANCE, null);

        for (int step = 0; step < 1000; step++) {
            LearningEditProposal proposal = pc.getBestEdit(true, true);
            if (proposal == null) {
                break;
            }
            PNEdit edit = proposal.getEdit();
            edit.executeEdit();
            pc.afterEditExecutes(edit);
        }

        Set<String> skeleton = new TreeSet<>();
        for (Link<Node> link : net.getLinks()) {
            String from = link.getFrom().getName().equals(nameOfA) ? "X" : link.getFrom().getName();
            String to = link.getTo().getName().equals(nameOfA) ? "X" : link.getTo().getName();
            skeleton.add(from.compareTo(to) < 0 ? from + "-" + to : to + "-" + from);
        }
        return skeleton;
    }

    /** Columns: F, A, E, C, B. */
    private static int[][] cases() {
        Random random = new Random(7);
        int[][] cases = new int[20000][];
        for (int i = 0; i < cases.length; i++) {
            int f = random.nextInt(2);
            int a = random.nextDouble() < 0.8 ? f : 1 - f;
            int e = random.nextInt(2);
            int c = random.nextDouble() < new double[]{0.1, 0.6, 0.5, 0.95}[a + 2 * e] ? 1 : 0;
            int b = random.nextDouble() < new double[]{0.1, 0.7, 0.4, 0.9}[c + 2 * e] ? 1 : 0;
            cases[i] = new int[]{f, a, e, c, b};
        }
        return cases;
    }
}
