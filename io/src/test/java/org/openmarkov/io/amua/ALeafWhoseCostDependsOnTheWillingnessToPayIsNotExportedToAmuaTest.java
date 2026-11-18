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
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A cost-effectiveness tree cut at a decision, whose best option changes with the willingness to pay, is not
 * exported: Amua would take only the cost and the effectiveness of the first interval.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ALeafWhoseCostDependsOnTheWillingnessToPayIsNotExportedToAmuaTest {

    private ProbNet net;
    private Variable doTest;
    private Variable therapy;
    private Variable utility;

    @BeforeEach
    void setUp() {
        doTest = new Variable("Do test?", "no", "yes");
        therapy = new Variable("Therapy", "no", "yes");
        utility = new Variable("U");
        net = new ProbNet();
        net.addNode(doTest, NodeType.DECISION);
        net.addNode(therapy, NodeType.DECISION);
        net.addNode(utility, NodeType.UTILITY);
        Criterion cost = new Criterion("Cost", "$");
        cost.setCECriterion(Criterion.CECriterion.Cost);
        Criterion effectiveness = new Criterion("Effectiveness", "QALY");
        effectiveness.setCECriterion(Criterion.CECriterion.Effectiveness);
        net.setDecisionCriteria(List.of(cost, effectiveness));
    }

    /** «Do test?»: «no» ends in a leaf; «yes» ends in «Therapy», cut, with the partition {@code cep}. */
    private DecisionTreeNode tree(CEP cep) {
        DecisionTreeNode cutTherapy = new DecisionTreeNode(therapy, net);
        cutTherapy.setEvaluation(new ProbAndCEP(1.0, cep));
        DecisionTreeNode leaf = new DecisionTreeNode(utility, net);
        leaf.setEvaluation(new ProbAndCEP(1.0, new CEP.CEPBuilder().build(null, 0, 8.768)));
        DecisionTreeNode root = new DecisionTreeNode(doTest, net);
        root.setEvaluation(new ProbAndCEP(1.0, null));
        root.addChild(branch(0, leaf));
        root.addChild(branch(1, cutTherapy));
        return root;
    }

    private DecisionTreeBranch branch(int state, DecisionTreeNode child) {
        DecisionTreeBranch branch = new DecisionTreeBranch(doTest.getStates()[state]);
        branch.setBranchProbability(1);
        branch.setChild(child);
        return branch;
    }

    @Test void aCutDecisionWhoseBestOptionChangesWithTheWillingnessToPayIsRefused() throws Exception {
        CEP twoIntervals = new CEP(new StrategyTree[2], new double[]{150, 3874}, new double[]{8.768, 9.11478},
                                   new double[]{10739}, 0, Double.POSITIVE_INFINITY);
        AmuaExporter exporter = new AmuaExporter(tree(twoIntervals));

        assertFalse(exporter.isValidDTForAmua());
        assertEquals("The node \"Therapy\" ends the tree, but its cost and its effectiveness depend on the "
                     + "willingness to pay, and Amua takes only one of each. Expand the tree down to it.",
                     exporter.getValidationErrorMessage());
    }

    @Test void aCutDecisionWithOneIntervalIsStillExported() {
        assertTrue(new AmuaExporter(tree(new CEP.CEPBuilder().build(null, 150, 8.768))).isValidDTForAmua());
    }
}
