/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.graphic.VisualNetwork;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Removing a selection of nodes and links, and undoing it, has to leave the network as it was.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RemoveSelectedEditTest {

    private ProbNet probNet;
    private VisualNetwork visualNetwork;

    /**
     * A → B, with B surviving the removal of A.
     */
    @BeforeEach
    void setUp() {
        probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.addNode(new Variable("A", 2), NodeType.CHANCE);
        probNet.addNode(new Variable("B", 2), NodeType.CHANCE);
        probNet.addLink(probNet.getVariable("A"), probNet.getVariable("B"), true);

        visualNetwork = new VisualNetwork(probNet, null);
        visualNetwork.setSelectedNode("A", true);
    }

    @Test
    void undoingTheRemovalOfANodeLeavesOneLinkWhereThereWasOne() throws Exception {
        RemoveSelectedEdit edit = new RemoveSelectedEdit(visualNetwork);

        edit.executeEdit();
        edit.undo();

        assertEquals(1, probNet.getLinks().size(),
                "Undoing left the network with a different number of links");
    }

    @Test
    void removingTheNodeTakesItsLinkAway() throws Exception {
        RemoveSelectedEdit edit = new RemoveSelectedEdit(visualNetwork);

        edit.executeEdit();

        assertEquals(0, probNet.getLinks().size(), "The link of the removed node stayed");
    }

    /**
     * A link selected on its own, between two nodes that stay, is still the business of this edit.
     */
    @Test
    void aSelectedLinkBetweenNodesThatStayIsRemovedAndComesBack() throws Exception {
        probNet.addNode(new Variable("C", 2), NodeType.CHANCE);
        probNet.addLink(probNet.getVariable("B"), probNet.getVariable("C"), true);
        VisualNetwork network = new VisualNetwork(probNet, null);
        network.setSelectedLink(probNet.getLink(probNet.getNode("B"), probNet.getNode("C"), true), true);

        RemoveSelectedEdit edit = new RemoveSelectedEdit(network);
        edit.executeEdit();

        assertEquals(1, probNet.getLinks().size(), "The selected link was not removed");

        edit.undo();

        assertEquals(2, probNet.getLinks().size(), "Undoing did not bring the selected link back");
    }
}
