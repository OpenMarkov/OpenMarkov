/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.learning.algorithm.nbderived;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.learning.algorithm.naivebayes.NaiveBayesAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.fanb.ForestAugmentedNBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.kdb.KDBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.snb.SelectiveNBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.spnb.SuperParentNBAlgorithm;
import org.openmarkov.learning.algorithm.nbderived.treeaugmentednb.TreeAugmentedNBAlgorithm;
import org.openmarkov.learning.algorithm.pc.PCAlgorithmIntegrationTest;
import org.openmarkov.learning.core.LearningManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.metric.annotation.MetricManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The automatic learning with a classifier, which initializes the algorithm when the learning window opens and again
 * when it learns, leaves a single link from the class to each feature that keeps it, and that feature has the class
 * as a parent.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class EachLinkFromTheClassIsLearntOnceTest {

    private static final String CLASS = "C";

    static LearningAlgorithm algorithm(String name, ProbNet net, CaseDatabase database) {
        MetricManager metrics = new MetricManager();
        return switch (name) {
            case "naive Bayes" -> new NaiveBayesAlgorithm(net, database, 0.5);
            case "TAN" -> new TreeAugmentedNBAlgorithm(net, database,
                                                       metrics.createInstance("ConditionalMutualInformation"), 0.5);
            case "FAN" -> new ForestAugmentedNBAlgorithm(net, database,
                                                         metrics.createInstance("ConditionalMutualInformation"),
                                                         metrics.createInstance("MutualInformation"), 0.5);
            case "KDB" -> new KDBAlgorithm(net, database, metrics.createInstance("ConditionalMutualInformation"),
                                           metrics.createInstance("MutualInformation"), 0.5, 1);
            case "selective, forward" -> new SelectiveNBAlgorithm(net, database, metrics.createInstance("Accuracy"),
                                                                  0.5, true);
            case "selective, backward" -> new SelectiveNBAlgorithm(net, database, metrics.createInstance("Accuracy"),
                                                                   0.5, false);
            case "superparent" -> new SuperParentNBAlgorithm(net, database, metrics.createInstance("Accuracy"), 0.5,
                                                             false);
            case "unique superparent" -> new SuperParentNBAlgorithm(net, database, metrics.createInstance("Accuracy"),
                                                                    0.5, true);
            default -> throw new IllegalArgumentException(name);
        };
    }

    @ParameterizedTest
    @ValueSource(strings = { "naive Bayes", "TAN", "FAN", "KDB", "selective, forward", "selective, backward",
            "superparent", "unique superparent" })
    void withTheClassifier(String name) throws Exception {
        List<Variable> variables = new ArrayList<>();
        CaseDatabase database = PCAlgorithmIntegrationTest.loadCsvDatabase("network/HeadToHead2.csv", variables);
        LearningManager manager = new LearningManager(database, LearningAlgorithm.class, null, null);
        LearningAlgorithm algorithm = algorithm(name, manager.getLearnedNet(), database);
        algorithm.setClassVariableName(CLASS);
        manager.init(algorithm);

        manager.learn();

        ProbNet net = manager.getLearnedNet();
        Node root = net.getNode(CLASS);
        List<String> fromTheClass = new ArrayList<>();
        for (Link<Node> link : net.getLinks()) {
            if (link.getFrom() == root) {
                fromTheClass.add(link.getTo().getName());
            }
        }
        fromTheClass.sort(null);
        List<String> childrenOfTheClass = new ArrayList<>(root.getChildren().stream().map(Node::getName).toList());
        childrenOfTheClass.sort(null);
        assertEquals(childrenOfTheClass, fromTheClass);
        assertEquals(fromTheClass.stream().distinct().toList(), fromTheClass);
    }
}
