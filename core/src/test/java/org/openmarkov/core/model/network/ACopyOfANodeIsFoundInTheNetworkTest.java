/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An influence diagram with the link P → X, where X was created as a decision. A copy of X, as the view of the table of
 * a canonical model makes, must be found in the network as X is. It used to have another hash code once the type of X
 * had changed, so the network gave it no parents (error 166).
 *
 * @author Manuel Arias
 */
class ACopyOfANodeIsFoundInTheNetworkTest {

    private ProbNet probNet;
    private Node p;
    private Node x;

    @BeforeEach void aDecisionXWithAParentP() {
        probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        p = probNet.addNode(new Variable("P", "no", "yes"), NodeType.CHANCE);
        x = probNet.addNode(new Variable("X", "no", "yes"), NodeType.DECISION);
        probNet.addLink(p, x, true);
    }

    private void assertTheCopyIsFound() {
        Node copy = new Node(x);
        Set<Node> nodes = new HashSet<>(Set.of(x));

        assertEquals(x, copy);
        assertEquals(x.hashCode(), copy.hashCode());
        assertEquals(List.of(p), probNet.getParents(copy));
        assertTrue(nodes.contains(copy));
    }

    @Test void afterChangingItsType() {
        x.setNodeType(NodeType.CHANCE);

        assertTheCopyIsFound();
    }

    @Test void afterChangingItsVariable() {
        x.setVariable(new Variable("X", "low", "high"));

        assertTheCopyIsFound();
    }
}
