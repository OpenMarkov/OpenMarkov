/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.treeadd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A tree with excluded events is split on the parents that are not events, so it is offered only to a node that has
 * one (error 23).
 *
 * @author Manuel Arias
 */
class TreeWithExcludedEventsNeedsAParentThatIsNotAnEventTest {

    private ProbNet probNet;
    private Node node;
    private Variable x;

    @BeforeEach void xIsAChanceNodeOfANetworkOfEvents() {
        probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        x = new Variable("X", "x0", "x1");
        node = probNet.addNode(x, NodeType.CHANCE);
    }

    private Variable parent(String name, VariableType type) {
        Variable variable = new Variable(name, "no", "yes");
        variable.setVariableType(type);
        probNet.addLink(probNet.addNode(variable, type == VariableType.EVENT ? NodeType.EVENT : NodeType.CHANCE),
                        node, true);
        return variable;
    }

    private boolean offered() throws Exception {
        return PotentialUtils.getFilteredPotentialClasses(node).contains(TreeWithExcludedEventsPotential.class);
    }

    @Test void itIsNotOfferedWhenAllTheParentsAreEvents() throws Exception {
        parent("E1", VariableType.EVENT);
        parent("E2", VariableType.EVENT);

        assertFalse(offered());
    }

    @Test void itIsOfferedAndBuiltWhenAParentIsNotAnEvent() throws Exception {
        Variable e = parent("E", VariableType.EVENT);
        Variable y = parent("Y", VariableType.FINITE_STATES);

        assertTrue(offered());
        assertDoesNotThrow(() -> new TreeWithExcludedEventsPotential(List.of(x, e, y),
                                                                     PotentialRole.CONDITIONAL_PROBABILITY));
    }
}
