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
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.learning.algorithm.hillclimbing.HillClimbingAlgorithm;
import org.openmarkov.learning.algorithm.pc.PCAlgorithmIntegrationTest;
import org.openmarkov.learning.core.LearningManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithm;
import org.openmarkov.learning.metric.annotation.MetricManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * «Finish» of the interactive learning right after the window opens goes on from the network as it is, and learns
 * the same network, links and tables, as the automatic learning, which initializes the algorithm again.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class FinishRightAfterOpeningLearnsTheAutomaticNetworkTest {

    /** The links and the tables of the network learnt with the algorithm, in a fixed order. */
    private static String learn(String name, boolean finish) throws Exception {
        List<Variable> variables = new ArrayList<>();
        CaseDatabase database = PCAlgorithmIntegrationTest.loadCsvDatabase("network/HeadToHead2.csv", variables);
        LearningManager manager = new LearningManager(database, LearningAlgorithm.class, null, null);
        ProbNet net = manager.getLearnedNet();
        LearningAlgorithm algorithm = name.equals("hill climbing")
                ? new HillClimbingAlgorithm(net, database, 0.5, new MetricManager().createInstance("K2", 0.5))
                : EachLinkFromTheClassIsLearntOnceTest.algorithm(name, net, database);
        algorithm.setClassVariableName("C");
        manager.init(algorithm);
        if (finish) {
            manager.learnFromTheCurrentState();
        } else {
            manager.learn();
        }
        List<String> links = new ArrayList<>();
        net.getLinks().forEach(link -> links.add(link.getFrom().getName() + "->" + link.getTo().getName()));
        links.sort(null);
        StringBuilder tables = new StringBuilder();
        List<Node> nodes = new ArrayList<>(net.getNodes());
        nodes.sort(Comparator.comparing(Node::getName));
        for (Node node : nodes) {
            TablePotential table = (TablePotential) node.getPotentials().getFirst();
            tables.append(table.getVariables().stream().map(Variable::getName).toList())
                  .append(Arrays.toString(table.getValues()));
        }
        return links + " " + tables;
    }

    @ParameterizedTest
    @ValueSource(strings = { "naive Bayes", "TAN", "FAN", "KDB", "selective, forward", "selective, backward",
            "superparent", "unique superparent", "hill climbing" })
    void withTheAlgorithm(String name) throws Exception {
        assertEquals(learn(name, false), learn(name, true));
    }
}
