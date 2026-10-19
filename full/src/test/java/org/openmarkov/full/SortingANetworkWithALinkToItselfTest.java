/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.full;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.graphic.VisualNetwork;
import org.openmarkov.gui.graphic.VisualNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A network of events with the links «Checkup» → «Other» and «Checkup» → «Checkup», which such a network allows: a
 * checkup that schedules the next one. «Sort probnet» used to fail with «loops not allowed» when it built the graph
 * to lay the nodes out, and moved none (error 110).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class SortingANetworkWithALinkToItselfTest {

    private static Variable event(String name) {
        Variable variable = new Variable(name, "no", "yes");
        variable.setVariableType(VariableType.EVENT);
        return variable;
    }

    @Test void everyNodeGetsAPosition() throws Exception {
        ProbNet net = new ProbNet(DESNetworkType.getUniqueInstance());
        Variable checkup = event("Checkup");
        Variable other = event("Other");
        net.addNode(checkup, NodeType.EVENT);
        net.addNode(other, NodeType.EVENT);
        new AddLinkEdit(net, checkup, other, true).executeEdit();
        new AddLinkEdit(net, checkup, checkup, true).executeEdit();
        VisualNetwork drawing = new VisualNetwork(net, null);

        var sorted = SortProbnetPlugin.getSortedCoordinates(drawing, 400, 300);

        assertEquals(2, sorted.graph().vertexSet().size());
        for (VisualNode node : sorted.graph().vertexSet()) {
            assertNotNull(sorted.model().get(node), node.getNode().getName());
        }
        assertEquals(1, sorted.graph().edgeSet().size());
    }
}
