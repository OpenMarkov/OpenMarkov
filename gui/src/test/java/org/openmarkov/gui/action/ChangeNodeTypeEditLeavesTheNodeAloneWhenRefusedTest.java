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
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A Bayesian network admits none but chance nodes. When it refuses a change of node type, the node
 * has to stay as it was.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ChangeNodeTypeEditLeavesTheNodeAloneWhenRefusedTest {

    private ProbNet probNet;

    @BeforeEach
    void setUp() {
        probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.addNode(new Variable("Disease", "absent", "present"), NodeType.CHANCE);
    }

    @Test
    void turningAChanceNodeIntoADecisionOneIsRefused() {
        ChangeNodeTypeEdit edit = new ChangeNodeTypeEdit(probNet.getNode("Disease"), NodeType.DECISION);

        assertThrows(DoEditException.class, edit::executeEdit);
    }

    @Test
    void theRefusedNodeKeepsItsType() {
        ChangeNodeTypeEdit edit = new ChangeNodeTypeEdit(probNet.getNode("Disease"), NodeType.DECISION);

        assertThrows(DoEditException.class, edit::executeEdit);

        assertEquals(NodeType.CHANCE, probNet.getNode("Disease").getNodeType(),
                "The refused change left the node with the new type");
    }
}
