package org.openmarkov.learning.algorithm.nbderived.treeaugmentednb;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.learning.metric.cmi.mutualInformation.MutualInformationMetric;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tree augmented naive Bayes learns from a database in which only the class is left, as the rest of the family
 * does, and gives the class alone; with no feature to direct the tree from, it used to fail with a
 * NoSuchElementException (error 240).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheTreeAugmentedNBWithOnlyTheClassTest {

    private static final String CLASS = "Class";

    @Test
    void aDatabaseWithOnlyTheClassGivesTheClassAlone() throws Exception {
        Variable theClass = new Variable(CLASS, 2);
        ProbNet net = new ProbNet();
        net.addNode(theClass, NodeType.CHANCE);
        TreeAugmentedNBAlgorithm algorithm = new TreeAugmentedNBAlgorithm(net,
                new CaseDatabase(new ArrayList<>(List.of(theClass)), new int[][]{{0}, {1}, {0}, {1}, {1}, {0}}),
                new MutualInformationMetric(), 0.5);
        algorithm.setClassVariableName(CLASS);

        algorithm.run(null);

        assertThat(net.getNodes()).hasSize(1);
        assertThat(net.getLinks()).isEmpty();
    }
}
