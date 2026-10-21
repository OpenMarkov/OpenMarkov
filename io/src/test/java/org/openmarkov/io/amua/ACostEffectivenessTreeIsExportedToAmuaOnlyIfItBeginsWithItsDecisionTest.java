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
import org.openmarkov.core.model.decisiontree.ProbAndCEP;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Amua compares the options of the first node of a cost-effectiveness tree as strategies, so the export is refused,
 * with a message that says why, when the decision «Therapy» is below the chance nodes «Test» or «Symptom», or when
 * there is no decision (error 199).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACostEffectivenessTreeIsExportedToAmuaOnlyIfItBeginsWithItsDecisionTest {

    private ProbNet net;
    private Variable symptom;
    private Variable test;
    private Variable therapy;
    private Variable utility;

    @BeforeEach
    void setUp() {
        symptom = new Variable("Symptom", "no", "yes");
        test = new Variable("Test", "negative", "positive");
        therapy = new Variable("Therapy", "none", "drug");
        utility = new Variable("U");
        net = new ProbNet();
        net.addNode(symptom, NodeType.CHANCE);
        net.addNode(test, NodeType.CHANCE);
        net.addNode(therapy, NodeType.DECISION);
        net.addNode(utility, NodeType.UTILITY);
        Criterion cost = new Criterion("Cost", "$");
        cost.setCECriterion(Criterion.CECriterion.Cost);
        Criterion effectiveness = new Criterion("Effectiveness", "QALY");
        effectiveness.setCECriterion(Criterion.CECriterion.Effectiveness);
        net.setDecisionCriteria(List.of(cost, effectiveness));
    }

    /** «Therapy»: «none» costs 0 and gives 5; «drug» costs 1000 and gives 8. */
    private DecisionTreeNode therapyNode() {
        return node(therapy, leaf(0, 5), leaf(1000, 8));
    }

    /** «Test»: «negative» ends in a leaf; «positive» leads to {@code positive}. */
    private DecisionTreeNode testNode(DecisionTreeNode positive) {
        return node(test, leaf(0, 9), positive);
    }

    /** «Symptom»: «no» ends in a leaf; «yes» leads to «Test», with «Therapy» under «positive». */
    private DecisionTreeNode symptomNode() {
        return node(symptom, leaf(0, 9), testNode(therapyNode()));
    }

    private DecisionTreeNode node(Variable variable, DecisionTreeNode first,
                                     DecisionTreeNode second) {
        DecisionTreeNode node = new DecisionTreeNode(variable, net);
        node.setEvaluation(new ProbAndCEP(1.0, null));
        State[] states = variable.getStates();
        double probability = net.getNode(variable).getNodeType() == NodeType.DECISION ? 1 : 0.5;
        node.addChild(branch(states[0], probability, first));
        node.addChild(branch(states[1], probability, second));
        return node;
    }

    private DecisionTreeBranch branch(State state, double probability, DecisionTreeNode child) {
        DecisionTreeBranch branch = new DecisionTreeBranch(state);
        branch.setBranchProbability(probability);
        branch.setChild(child);
        return branch;
    }

    private DecisionTreeNode leaf(double cost, double effectiveness) {
        DecisionTreeNode leaf = new DecisionTreeNode(utility, net);
        leaf.setEvaluation(new ProbAndCEP(1.0, new CEP.CEPBuilder().build(null, cost, effectiveness)));
        return leaf;
    }

    /** Returns the message shown to the user, or fails if the tree is accepted. */
    private static String refusal(DecisionTreeNode root) {
        AmuaExporter exporter = new AmuaExporter(root);
        assertFalse(exporter.isValidDTForAmua(), "The tree should not be exported to Amua");
        return exporter.getValidationErrorMessage();
    }

    @Test
    void theDecisionBelowTheFirstNodeIsRefused() {
        assertEquals("The decision \"Therapy\" must be the first node of the tree, but the tree begins with \"Test\". "
                     + "Amua compares the options of the first node as strategies.",
                     refusal(testNode(therapyNode())));
    }

    @Test
    void theDecisionTwoLevelsBelowTheFirstNodeIsRefused() {
        assertEquals("The decision \"Therapy\" must be the first node of the tree, but the tree begins with "
                     + "\"Symptom\". Amua compares the options of the first node as strategies.",
                     refusal(symptomNode()));
    }

    @Test
    void aTreeWithoutDecisionIsRefused() {
        assertEquals("The tree has no decision. Amua compares the options of the first node as strategies, "
                     + "so the tree must begin with a decision.",
                     refusal(testNode(leaf(1000, 8))));
    }

    @Test
    void aTreeThatBeginsWithItsDecisionIsAccepted() {
        assertTrue(new AmuaExporter(node(therapy, testNode(leaf(0, 5)), leaf(1000, 8)))
                           .isValidDTForAmua());
    }
}
