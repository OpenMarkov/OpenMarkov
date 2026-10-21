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
import org.openmarkov.core.model.decisiontree.CENodeEvaluation;
import org.openmarkov.core.model.decisiontree.UnicritNodeEvaluation;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.CEADecisionTreeNode;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.EvaluationDecisionTreeNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The row of a node shows its utility whatever the type of the node, or its
 * cost-effectiveness partition.
 *
 * @author Manuel Arias
 */
class DecisionTreeNodePanelTest {

    @ParameterizedTest
    @EnumSource(value = NodeType.class, names = {"CHANCE", "DECISION", "UTILITY"})
    void everyKindOfNodeShowsItsUtility(NodeType type) {
        EvaluationDecisionTreeNode node = new EvaluationDecisionTreeNode(new Node(null, new Variable("X"), type));
        node.setEvaluation(new UnicritNodeEvaluation(1.0, 9.72));

        assertEquals(" U=9.7200", description(new DecisionTreeNodePanel(node)));
    }

    @Test
    void aNodeNotYetEvaluatedShowsNothing() {
        EvaluationDecisionTreeNode node =
                new EvaluationDecisionTreeNode(new Node(null, new Variable("X"), NodeType.CHANCE));

        assertEquals("", description(new DecisionTreeNodePanel(node)));
    }

    @Test
    void aCostEffectivenessNodeShowsItsPartition() throws Exception {
        CEADecisionTreeNode node = new CEADecisionTreeNode(new Node(null, new Variable("X"), NodeType.CHANCE));
        node.setEvaluation(new CENodeEvaluation(1.0,
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
