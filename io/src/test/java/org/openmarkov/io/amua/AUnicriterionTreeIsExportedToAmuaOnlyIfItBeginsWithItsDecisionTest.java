/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.amua;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.ProbAndUtilityAndStrategy;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Amua compares the options of the first node of a tree as strategies, so a unicriterion tree, like a
 * cost-effectiveness one, is exported only if it begins with its decision.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AUnicriterionTreeIsExportedToAmuaOnlyIfItBeginsWithItsDecisionTest {

    private ProbNet net;
    private Variable chance;
    private Variable therapy;
    private Variable utility;

    @BeforeEach
    void setUp() {
        chance = new Variable("A", "absent", "present");
        therapy = new Variable("Therapy", "none", "drug");
        utility = new Variable("U");
        net = new ProbNet();
        net.addNode(chance, NodeType.CHANCE);
        net.addNode(therapy, NodeType.DECISION);
        net.addNode(utility, NodeType.UTILITY);
    }

    private DecisionTreeNode node(Variable variable, DecisionTreeNode first, DecisionTreeNode second) {
        DecisionTreeNode node = new DecisionTreeNode(variable, net);
        node.setEvaluation(new ProbAndUtilityAndStrategy(1.0, 0.0));
        State[] states = variable.getStates();
        double probability = net.getNode(variable).getNodeType() == NodeType.DECISION ? 1 : 0.5;
        node.addChild(branch(states[0], probability, first));
        node.addChild(branch(states[1], probability, second));
        return node;
    }

    private static DecisionTreeBranch branch(State state, double probability, DecisionTreeNode child) {
        DecisionTreeBranch branch = new DecisionTreeBranch(state);
        branch.setBranchProbability(probability);
        branch.setChild(child);
        return branch;
    }

    private DecisionTreeNode leaf(double utilityValue) {
        DecisionTreeNode leaf = new DecisionTreeNode(utility, net);
        leaf.setEvaluation(new ProbAndUtilityAndStrategy(1.0, utilityValue));
        return leaf;
    }

    private static String refusal(DecisionTreeNode root) {
        AmuaExporter exporter = new AmuaExporter(root);
        assertFalse(exporter.isValidDTForAmua(), "The tree should not be exported to Amua");
        return exporter.getValidationErrorMessage();
    }

    @Test void aTreeThatBeginsWithAChanceNodeAndHasNoDecisionIsRefused() {
        assertEquals("The tree has no decision. Amua compares the options of the first node as strategies, "
                     + "so the tree must begin with a decision.",
                     refusal(node(chance, leaf(90), leaf(20))));
    }

    @Test void aTreeThatIsOnlyAUtilityIsRefused() {
        assertEquals("The tree has no decision. Amua compares the options of the first node as strategies, "
                     + "so the tree must begin with a decision.",
                     refusal(leaf(83.7)));
    }

    @Test void aDecisionBelowAChanceNodeIsRefused() {
        assertEquals("The decision \"Therapy\" must be the first node of the tree, but the tree begins with \"A\". "
                     + "Amua compares the options of the first node as strategies.",
                     refusal(node(chance, leaf(90), node(therapy, leaf(20), leaf(60)))));
    }

    @Test void aTreeThatBeginsWithItsDecisionIsAccepted() {
        assertTrue(new AmuaExporter(node(therapy, node(chance, leaf(90), leaf(20)), leaf(60))).isValidDTForAmua());
    }
}
