package org.openmarkov.learning.algorithm.nbderived.snb;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.BaseLinkEdit;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.learning.core.util.LearningEditProposal;
import org.openmarkov.learning.metric.cmi.accuracy.Accuracy;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** The selective naive Bayes going forwards chooses the same feature in every run. */
class SelectiveNBIsRepeatableTest {

    private static final int NUM_FEATURES = 20;

    /** Many features, so that an order that depends on where the variables are in memory seldom gets it right. */
    @Test
    void theFirstFeatureOfTheNetworkWinsATie() {
        StubAccuracy metric = new StubAccuracy();

        assertThat(firstFeatureAdded(metric, true)).isEqualTo("F0");
    }

    @Test
    void theBestFeatureWinsWhereverItIs() {
        StubAccuracy metric = new StubAccuracy();
        metric.scores.put("F7", 0.9);

        assertThat(firstFeatureAdded(metric, true)).isEqualTo("F7");
    }

    /** When the changes that do not improve are also wanted, the proposal is still the best one. */
    @Test
    void theBestFeatureIsProposedAlsoWhenChangesThatDoNotImproveAreWanted() {
        StubAccuracy metric = new StubAccuracy();
        metric.scores.put("F7", 0.9);

        assertThat(firstFeatureAdded(metric, false)).isEqualTo("F7");
    }

    private static String firstFeatureAdded(StubAccuracy metric, boolean onlyPositiveEdits) {
        ProbNet net = new ProbNet();
        net.addNode(new Variable("Class", 2), NodeType.CHANCE);
        for (int f = 0; f < NUM_FEATURES; f++) {
            net.addNode(new Variable("F" + f, 2), NodeType.CHANCE);
        }
        SelectiveNBAlgorithm algorithm = new SelectiveNBAlgorithm(net, null, metric, 0.5, true);
        algorithm.setClassVariableName("Class");
        algorithm.init(null);
        LearningEditProposal proposal = algorithm.getBestEdit(false, onlyPositiveEdits);
        return ((BaseLinkEdit) proposal.getEdit()).getVariableTo().getName();
    }

    /** Every feature gives the same accuracy, except those given another one. */
    private static class StubAccuracy extends Accuracy {
        private final Map<String, Double> scores = new HashMap<>();

        @Override
        public double getScore(PNEdit edit) {
            return scores.getOrDefault(((BaseLinkEdit) edit).getVariableTo().getName(), 0.5);
        }

        @Override
        public double score(TablePotential nodePotential) {
            return 0;
        }

        @Override
        protected void initCache() {
            // Nothing to prepare: there is no case database
        }
    }
}
