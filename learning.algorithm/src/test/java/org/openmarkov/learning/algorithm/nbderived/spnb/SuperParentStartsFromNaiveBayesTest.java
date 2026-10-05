package org.openmarkov.learning.algorithm.nbderived.spnb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
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

/** The superparent classifier adds links only if they beat the naive Bayes that it starts from. */
class SuperParentStartsFromNaiveBayesTest {

    private ProbNet probNet;
    private StubAccuracy metric;

    @BeforeEach
    void setUp() {
        probNet = new ProbNet();
        for (String name : new String[]{"Class", "F1", "F2", "F3"}) {
            probNet.addNode(new Variable(name, 2), NodeType.CHANCE);
        }
        metric = new StubAccuracy();
        metric.naiveBayes = 0.9;
    }

    @Test
    void noLinkIsAddedOneByOneWhenNoneBeatsTheNaiveBayes() {
        metric.setScore("Class", "F2", 0.8);
        metric.setScore("F2", "F1", 0.85);
        metric.setScore("F2", "F3", 0.7);

        assertThat(firstProposal(false)).isNull();
    }

    @Test
    void theSuperParentIsNotAppliedToAllWhenItDoesNotBeatTheNaiveBayes() {
        metric.setScore("Class", "F2", 0.8);

        assertThat(firstProposal(true)).isNull();
    }

    @Test
    void theSuperParentIsAppliedToAllWhenItBeatsTheNaiveBayes() {
        metric.setScore("Class", "F2", 0.95);

        assertThat(firstProposal(true)).isNotNull();
    }

    /** The superparent is chosen by its accuracy with all its children, but a single link is compared with the current classifier. */
    @Test
    void aLinkThatBeatsTheNaiveBayesIsAddedEvenIfTheSuperParentWithAllItsChildrenDoesNot() {
        metric.setScore("Class", "F2", 0.8);
        metric.setScore("F2", "F1", 0.93);
        metric.setScore("F2", "F3", 0.7);

        LearningEditProposal proposal = firstProposal(false);

        assertThat(proposal).isNotNull();
        AddLinkEdit edit = (AddLinkEdit) proposal.getEdit();
        assertThat(edit.getVariableFrom().getName()).isEqualTo("F2");
        assertThat(edit.getVariableTo().getName()).isEqualTo("F1");
    }

    private LearningEditProposal firstProposal(boolean sameSuperParent) {
        SuperParentNBAlgorithm algorithm = new SuperParentNBAlgorithm(probNet, null, metric, 1.0, sameSuperParent);
        algorithm.setClassVariableName("Class");
        algorithm.init(null);
        return algorithm.getBestEdit(true, true);
    }

    /** An accuracy with fixed values: one for the network as it is and one for each link. */
    private static class StubAccuracy extends Accuracy {
        private final Map<String, Double> scores = new HashMap<>();
        private double naiveBayes;

        void setScore(String from, String to, double score) {
            scores.put(from + ":" + to, score);
        }

        @Override
        public double getScore() {
            return naiveBayes;
        }

        @Override
        public double getScore(PNEdit edit) {
            BaseLinkEdit linkEdit = (BaseLinkEdit) edit;
            return scores.getOrDefault(linkEdit.getVariableFrom().getName() + ":" + linkEdit.getVariableTo().getName(), 0.0);
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
