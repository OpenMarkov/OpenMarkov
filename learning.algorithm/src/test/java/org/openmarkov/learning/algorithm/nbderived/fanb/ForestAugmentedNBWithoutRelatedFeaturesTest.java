package org.openmarkov.learning.algorithm.nbderived.fanb;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.learning.metric.cmi.conditional.ConditionalMutualInformationMetric;
import org.openmarkov.learning.metric.cmi.mutualInformation.MutualInformationMetric;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The forest augmented naive Bayes learns without failing when no feature is related to the class. */
class ForestAugmentedNBWithoutRelatedFeaturesTest {

    private static final String CLASS = "Class";

    @Test
    void aDatabaseWithOnlyTheClassGivesTheClassAlone() throws Exception {
        ProbNet net = learn(List.of(new Variable(CLASS, 2)), new int[][]{{0}, {1}, {0}, {1}, {1}, {0}});

        assertThat(net.getLinks()).isEmpty();
    }

    @Test
    void aFeatureThatNeverChangesGivesTheNaiveBayes() throws Exception {
        // The state of the feature is the same in every case, so it tells nothing about the class
        ProbNet net = learn(List.of(new Variable(CLASS, 2), new Variable("Constant", 2)),
                new int[][]{{0, 0}, {1, 0}, {0, 0}, {1, 0}, {1, 0}, {0, 0}});

        assertThat(net.getLinks()).hasSize(1);
        assertThat(net.getLinks().get(0).getFrom().getName()).isEqualTo(CLASS);
    }

    private static ProbNet learn(List<Variable> variables, int[][] cases) throws Exception {
        ProbNet net = new ProbNet();
        variables.forEach(variable -> net.addNode(variable, NodeType.CHANCE));
        ForestAugmentedNBAlgorithm algorithm = new ForestAugmentedNBAlgorithm(net,
                new CaseDatabase(new ArrayList<>(variables), cases),
                new ConditionalMutualInformationMetric(), new MutualInformationMetric(), 0.5);
        algorithm.setClassVariableName(CLASS);
        algorithm.run(null);
        return net;
    }
}
