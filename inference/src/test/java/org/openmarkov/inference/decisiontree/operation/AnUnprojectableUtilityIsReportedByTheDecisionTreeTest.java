/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.decisiontree.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.testutils.TestNetworks;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A utility the decision tree cannot project is reported as such, as the evaluation does.
 *
 * @author Manuel Arias
 */
class AnUnprojectableUtilityIsReportedByTheDecisionTreeTest {

    @Tag(TestSpeed.FAST)
    @Test
    void aUniformUtilityOfADecisionIsReportedAsNotProjectable() {
        ProbNet id = TestNetworks.buildSimpleID();
        Variable decision = id.getVariable("Decision");
        Variable cost = new Variable("Cost");
        cost.setDecisionCriterion(new Criterion());
        Node costNode = id.addNode(cost, NodeType.UTILITY);
        id.addLink(id.getNode(decision), costNode, true);
        costNode.setPotential(new UniformPotential(PotentialRole.UNSPECIFIED, cost, decision));

        assertThrows(NonProjectablePotentialException.class,
                () -> new DecisionTreeManagerImpl().buildDecisionTree(id, 5));
    }
}
