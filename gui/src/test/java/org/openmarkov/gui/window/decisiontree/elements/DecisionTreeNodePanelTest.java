/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree.elements;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.ProbAndCEP;
import org.openmarkov.core.model.decisiontree.ProbAndUtilityAndStrategy;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.StrategyTree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The row of a node shows its utility whatever the type of the node, or its
 * cost-effectiveness partition; that its scenario is impossible instead, if it
 * is; and an ellipsis after it, with the node drawn darker, if the tree can
 * still go on from it.
 *
 * @author Manuel Arias
 */
class DecisionTreeNodePanelTest {

    @ParameterizedTest
    @EnumSource(value = NodeType.class, names = {"CHANCE", "DECISION", "UTILITY"})
    void everyKindOfNodeShowsItsUtility(NodeType type) {
        DecisionTreeNode node = new DecisionTreeNode(new Node(null, new Variable("X"), type));
        node.setEvaluation(new ProbAndUtilityAndStrategy(1.0, 9.72));

        assertTrue(description(new DecisionTreeNodePanel(node)).startsWith(" U=9.7200"));
    }

    @Test
    void aLeafTheTreeCanGoOnFromEndsWithAnEllipsisAndIsDrawnDarker() {
        DecisionTreeNodePanel panel = new DecisionTreeNodePanel(evaluated(NodeType.DECISION, 1.0));

        assertEquals(" U=9.7200  \u2026", description(panel));
        assertTrue(panel.canBeExpanded());
    }

    @Test
    void aNodeWithBranchesHasNoMark() {
        DecisionTreeNode node = evaluated(NodeType.CHANCE, 1.0);
        node.addChild(new DecisionTreeBranch(new State("present")));
        DecisionTreeNodePanel panel = new DecisionTreeNodePanel(node);

        assertEquals(" U=9.7200", description(panel));
        assertFalse(panel.canBeExpanded());
    }

    @Test
    void theUtilityNodeWhereTheTreeEndsHasNoMark() {
        DecisionTreeNodePanel panel = new DecisionTreeNodePanel(evaluated(NodeType.UTILITY, 1.0));

        assertEquals(" U=9.7200", description(panel));
        assertFalse(panel.canBeExpanded());
    }

    @Test
    void aScenarioThatCannotHappenSaysSoInsteadOfItsUtility() {
        DecisionTreeNodePanel panel = new DecisionTreeNodePanel(evaluated(NodeType.DECISION, 0.0));

        assertEquals(" impossible scenario", description(panel));
        assertFalse(panel.canBeExpanded());
    }

    private static DecisionTreeNode evaluated(NodeType type, double probability) {
        DecisionTreeNode node = new DecisionTreeNode(new Node(null, new Variable("X", "absent", "present"), type));
        node.setEvaluation(new ProbAndUtilityAndStrategy(probability, 9.72));
        return node;
    }

    @Test
    void aNodeNotYetEvaluatedShowsNothing() {
        DecisionTreeNode node =
                new DecisionTreeNode(new Node(null, new Variable("X"), NodeType.CHANCE));

        assertEquals("", description(new DecisionTreeNodePanel(node)));
    }

    @Test
    void aCostEffectivenessNodeShowsItsPartition() throws Exception {
        DecisionTreeNode node = new DecisionTreeNode(new Node(null, new Variable("X"), NodeType.CHANCE));
        node.setEvaluation(new ProbAndCEP(1.0,
                new CEP(new StrategyTree[]{null}, new double[]{10.0}, new double[]{2.0}, null, 0.0, 100.0)));

        String text = description(new DecisionTreeNodePanel(node));

        assertTrue(text.contains("Cost=10.0000"), text);
        assertTrue(text.contains("Effectiveness=2.0000"), text);
    }

    private static String description(DecisionTreeNodePanel panel) {
        panel.update(false, true, false, 0, false);
        return panel.descriptionLabel.getText();
    }
}
