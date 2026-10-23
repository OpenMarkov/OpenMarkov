/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.util;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.ProbAndUtilityAndStrategy;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * In the Graphviz drawing of a decision tree, a node whose scenario cannot happen says so instead of its utility.
 *
 * @author Manuel Arias
 */
class TreeNodeToDotTest {

    @Test
    void aScenarioThatCannotHappenSaysSoInsteadOfItsUtility() {
        assertEquals(" impossible scenario", new TreeNodeToDot().formatUtility(node(0.0)));
    }

    private static DecisionTreeNode node(double probability) {
        DecisionTreeNode node = new DecisionTreeNode(new Node(null, new Variable("Therapy"), NodeType.DECISION));
        node.setEvaluation(new ProbAndUtilityAndStrategy(probability, 9.72));
        return node;
    }
}
