/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree.elements;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.ProbAndUtilityAndStrategy;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The row of a branch shows its state and, under a chance node, its probability.
 * The utility belongs to the row of the node the branch leads to.
 *
 * @author Manuel Arias
 */
class DecisionTreeBranchPanelTest {

    private static final String BEST_DECISION_MARK = "bgcolor=#3FD111";

    @Test
    void aBranchOfAChanceNodeShowsItsStateAndProbabilityButNotTheUtility() {
        Variable disease = new Variable("Disease (X)", "absent", "present");
        DecisionTreeNode chance = node(disease, NodeType.CHANCE);
        chance.setEvaluation(new ProbAndUtilityAndStrategy(1.0, 9.72));
        DecisionTreeBranch absent = branch(chance, disease, 0, 0.86, 10.0);

        String html = new DecisionTreeBranchPanel(absent).getBranchDescriptiontHTML();

        assertTrue(html.contains("Disease (X)=absent /  P=0.8600"), html);
        assertFalse(html.contains("U="), html);
    }

    @Test
    void aBranchOfADecisionNodeShowsItsStateAndMarksTheBestOne() {
        Variable therapy = new Variable("Therapy (T)", "no", "yes");
        DecisionTreeNode decision = node(therapy, NodeType.DECISION);
        decision.setEvaluation(new ProbAndUtilityAndStrategy(1.0, 10.0));
        DecisionTreeBranch no = branch(decision, therapy, 0, 1.0, 8.0);
        DecisionTreeBranch yes = branch(decision, therapy, 1, 1.0, 10.0);

        String htmlNo = new DecisionTreeBranchPanel(no).getBranchDescriptiontHTML();
        String htmlYes = new DecisionTreeBranchPanel(yes).getBranchDescriptiontHTML();

        assertTrue(htmlYes.contains(BEST_DECISION_MARK), htmlYes);
        assertFalse(htmlNo.contains(BEST_DECISION_MARK), htmlNo);
        assertTrue(htmlYes.contains("Therapy (T)=yes"), htmlYes);
        assertFalse(htmlYes.contains("P="), htmlYes);
        assertFalse(htmlYes.contains("U="), htmlYes);
    }

    @Test
    void theRootBranchShowsNothing() {
        DecisionTreeBranch root = new DecisionTreeBranch();
        DecisionTreeNode first = node(new Variable("Disease (X)", "absent", "present"), NodeType.CHANCE);
        first.setEvaluation(new ProbAndUtilityAndStrategy(1.0, 9.72));
        root.setChild(first);

        String html = new DecisionTreeBranchPanel(root).getBranchDescriptiontHTML();

        assertTrue(html.contains("<td align=center border=0></td>"), html);
    }

    private static DecisionTreeNode node(Variable variable, NodeType type) {
        return new DecisionTreeNode(new Node(null, variable, type));
    }

    /**
     * Hangs from the node, whose scenario has probability 1, a branch for the given state that leads to a
     * utility leaf with the given value.
     */
    private static DecisionTreeBranch branch(DecisionTreeNode parent, Variable variable, int state,
                                                     double probability, double utility) {
        DecisionTreeBranch branch = new DecisionTreeBranch(variable.getStates()[state]);
        branch.setBranchProbability(probability);
        parent.addChild(branch);
        DecisionTreeNode leaf = node(new Variable("Utility"), NodeType.UTILITY);
        leaf.setEvaluation(new ProbAndUtilityAndStrategy(probability, utility));
        branch.setChild(leaf);
        return branch;
    }
}
