/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The tuning model is offered under the same conditions as OR / MAX and AND / MIN, plus
 * three states in every variable.
 *
 * @author Manuel Arias
 */
public class TheTuningModelIsOfferedWhereTheOtherCanonicalModelsAreTest {

    private static List<Class<? extends Potential>> offeredFor(Node node) throws Exception {
        return PotentialUtils.getFilteredPotentialClasses(node);
    }

    private static Node childWithAParent(NetworkType type) {
        ProbNet net = new ProbNet(type);
        Variable parent = new Variable("A", 3);
        Variable child = new Variable("B", 3);
        net.addNode(parent, NodeType.CHANCE);
        Node node = net.addNode(child, NodeType.CHANCE);
        net.addLink(parent, child, true);
        node.setPotential(new TablePotential(List.of(child, parent), PotentialRole.CONDITIONAL_PROBABILITY));
        return node;
    }

    @Tag(TestSpeed.FAST)
    @Test public void aDecisionWithoutParentsAndAnImposedPolicyIsNotOfferedIt() throws Exception {
        ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable decision = new Variable("D", 3);
        Node node = net.addNode(decision, NodeType.DECISION);
        node.setPotential(new TablePotential(List.of(decision), PotentialRole.POLICY));

        List<Class<? extends Potential>> offered = offeredFor(node);

        assertFalse(offered.contains(TuningPotential.class));
        assertFalse(offered.contains(MaxPotential.class));
    }

    @Tag(TestSpeed.FAST)
    @Test public void aNodeOfADiscreteEventNetworkIsNotOfferedIt() throws Exception {
        List<Class<? extends Potential>> offered = offeredFor(childWithAParent(DESNetworkType.getUniqueInstance()));

        assertFalse(offered.contains(TuningPotential.class));
        assertFalse(offered.contains(MaxPotential.class));
    }

    @Tag(TestSpeed.FAST)
    @Test public void aChanceNodeOfThreeStatesWithAParentOfThreeIsOfferedIt() throws Exception {
        List<Class<? extends Potential>> offered = offeredFor(childWithAParent(BayesianNetworkType.getUniqueInstance()));

        assertTrue(offered.contains(TuningPotential.class));
        assertTrue(offered.contains(MaxPotential.class));
        assertTrue(offered.contains(MinPotential.class));
    }
}
