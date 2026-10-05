package org.openmarkov.learning.algorithm.nbderived.snb;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.learning.metric.cmi.accuracy.Accuracy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** The selective naive Bayes chooses its features with the smoothing (alpha) that the user sets. */
class SelectiveNBSmoothingTest {

    private static final String CLASS = "Class";
    private static final int NUM_CASES = 150;
    private static final int NUM_FEATURES = 5;
    private static final int NUM_STATES = 10;

    @Test
    void theMetricReceivesTheAlphaOfTheAlgorithm() {
        Accuracy metric = new Accuracy();
        List<Variable> variables = variables();
        SelectiveNBAlgorithm algorithm = new SelectiveNBAlgorithm(net(variables), database(variables), metric, 0.5, true);
        algorithm.setClassVariableName(CLASS);

        algorithm.init(null);

        assertThat(metric.getAlpha()).isEqualTo(0.5);
    }

    /**
     * Features with many states and few cases: without smoothing, a value never seen with a class
     * rules that class out, so the features look useless and are removed.
     */
    @Test
    void theAlphaChangesTheFeaturesThatAreKept() {
        Set<String> withoutSmoothing = featuresKeptGoingBackwards(0.0);
        Set<String> withSmoothing = featuresKeptGoingBackwards(0.5);

        assertThat(withSmoothing).isNotEqualTo(withoutSmoothing);
        assertThat(withSmoothing).hasSizeGreaterThan(withoutSmoothing.size());
    }

    private static Set<String> featuresKeptGoingBackwards(double alpha) {
        List<Variable> variables = variables();
        ProbNet net = net(variables);
        SelectiveNBAlgorithm algorithm = new SelectiveNBAlgorithm(net, database(variables), new Accuracy(), alpha, false);
        algorithm.setClassVariableName(CLASS);
        try {
            algorithm.run(null);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
        return net.getNode(CLASS).getChildren().stream().map(Node::getName).collect(Collectors.toSet());
    }

    private static List<Variable> variables() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new Variable(CLASS, 2));
        for (int f = 0; f < NUM_FEATURES; f++) {
            variables.add(new Variable("F" + f, NUM_STATES));
        }
        return variables;
    }

    private static ProbNet net(List<Variable> variables) {
        ProbNet net = new ProbNet();
        variables.forEach(variable -> net.addNode(variable, NodeType.CHANCE));
        return net;
    }

    /** The lower half of the states of each feature goes with class 0 and the upper half with class 1, 85% of the times. */
    private static CaseDatabase database(List<Variable> variables) {
        Random random = new Random(7);
        int[][] cases = new int[NUM_CASES][NUM_FEATURES + 1];
        for (int[] row : cases) {
            row[0] = random.nextInt(2);
            for (int f = 1; f <= NUM_FEATURES; f++) {
                int half = random.nextDouble() < 0.85 ? row[0] : 1 - row[0];
                row[f] = half * NUM_STATES / 2 + random.nextInt(NUM_STATES / 2);
            }
        }
        return new CaseDatabase(variables, cases);
    }
}
