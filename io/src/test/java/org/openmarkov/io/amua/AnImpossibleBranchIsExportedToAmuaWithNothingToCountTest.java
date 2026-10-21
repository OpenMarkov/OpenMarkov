/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.amua;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.ProbAndCEP;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The scenario of an impossible branch, such as the result of a test that is not done, has no cost-effectiveness
 * partition: the evaluation leaves there the partition of probability zero, which has no costs nor effectiveness.
 * The exporter read them anyway and failed with a NullPointerException (error 321). Now that branch gets cost and
 * effectiveness 0, which Amua multiplies by its probability, 0 as well (it accepts a branch of probability 0, and
 * weights the values of each branch by its probability: {@code DecisionTree.checkModel} and
 * {@code TreeCohort}, Amua 0.3.6).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnImpossibleBranchIsExportedToAmuaWithNothingToCountTest {

    /**
     * «Therapy»: «none» leads to «Test», whose result «not done» has probability 1 (cost 0, effectiveness 5) and
     * «positive» probability 0; «drug» costs 1000 and gives 8.
     */
    @Test
    void theImpossibleBranchHasProbabilityCostAndEffectivenessZero(@TempDir Path folder) throws Exception {
        Variable test = new Variable("Test", "not done", "positive");
        Variable therapy = new Variable("Therapy", "none", "drug");
        Variable utility = new Variable("U");
        ProbNet net = new ProbNet();
        net.addNode(test, NodeType.CHANCE);
        net.addNode(therapy, NodeType.DECISION);
        net.addNode(utility, NodeType.UTILITY);
        Criterion cost = new Criterion("Cost", "$");
        cost.setCECriterion(Criterion.CECriterion.Cost);
        Criterion effectiveness = new Criterion("Effectiveness", "QALY");
        effectiveness.setCECriterion(Criterion.CECriterion.Effectiveness);
        net.setDecisionCriteria(List.of(cost, effectiveness));

        DecisionTreeNode testNode = node(net, test, 1.0);
        hang(testNode, test.getStates()[0], 1.0, leaf(net, utility, 0, 5));
        DecisionTreeNode impossible = new DecisionTreeNode(utility, net);
        impossible.setEvaluation(new ProbAndCEP(0.0, CEP.getZeroPartition()));
        hang(testNode, test.getStates()[1], 0.0, impossible);
        DecisionTreeNode root = node(net, therapy, 1.0);
        hang(root, therapy.getStates()[0], 1.0, testNode);
        hang(root, therapy.getStates()[1], 1.0, leaf(net, utility, 1000, 8));

        AmuaExporter exporter = new AmuaExporter(root);
        assertTrue(exporter.isValidDTForAmua(), exporter.getValidationErrorMessage());
        File file = folder.resolve("tree.amua").toFile();
        exporter.writeAmuaDT(file);

        String positive = nodeNamed(Files.readString(file.toPath()), "positive");
        assertTrue(positive.contains("<prob>0</prob>"), positive);
        assertEquals(2, occurrences(positive, "<payoff>0</payoff>"), positive);
    }

    private static DecisionTreeNode node(ProbNet net, Variable variable, double probability) {
        DecisionTreeNode node = new DecisionTreeNode(variable, net);
        node.setEvaluation(new ProbAndCEP(probability, null));
        return node;
    }

    private static DecisionTreeNode leaf(ProbNet net, Variable utility, double cost, double effectiveness) {
        DecisionTreeNode leaf = new DecisionTreeNode(utility, net);
        leaf.setEvaluation(new ProbAndCEP(1.0, new CEP.CEPBuilder().build(null, cost, effectiveness)));
        return leaf;
    }

    private static void hang(DecisionTreeNode parent, State state, double probability, DecisionTreeNode child) {
        DecisionTreeBranch branch = new DecisionTreeBranch(state);
        branch.setBranchProbability(probability);
        parent.addChild(branch);
        branch.setChild(child);
    }

    /** The XML of the node of the Amua file with the given name. */
    private static String nodeNamed(String xml, String name) {
        return Arrays.stream(xml.split("<Node>"))
                     .filter(node -> node.contains("<name>" + name + "</name>"))
                     .findFirst()
                     .orElseThrow(() -> new AssertionError("no node named " + name + " in\n" + xml));
    }

    private static int occurrences(String text, String part) {
        return (text.length() - text.replace(part, "").length()) / part.length();
    }
}
