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
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.constraint.OnlyOneUtilityNode;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Turning an existing node into a utility node is the other way of giving a network two of them.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ChangeNodeTypeEditRespectsOnlyOneUtilityNodeTest {

    private ProbNet probNet;

    @BeforeEach
    void setUp() {
        probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        probNet.addConstraint(new OnlyOneUtilityNode());
        probNet.addNode(new Variable("cost"), NodeType.UTILITY);
        probNet.addNode(new Variable("Disease", "absent", "present"), NodeType.CHANCE);
    }

    @Test
    void turningAChanceNodeIntoASecondUtilityNodeIsRefused() {
        ChangeNodeTypeEdit edit = new ChangeNodeTypeEdit(probNet.getNode("Disease"), NodeType.UTILITY);

        assertFalse(edit.constraintsWillBeMet(), "The editor would have let a second utility node in");
    }

    @Test
    void theRefusalNamesTheUtilityNodeAlreadyThere() {
        ChangeNodeTypeEdit edit = new ChangeNodeTypeEdit(probNet.getNode("Disease"), NodeType.UTILITY);
        ConstraintViolatedException complaint = assertThrows(ConstraintViolatedException.class,
                edit::tryConstraintsWillBeMet);

        assertTrue(complaint.toString().contains("cost"),
                "The refusal does not name the utility node already there: " + complaint);
    }

    /**
     * The only utility node the network has may be re-declared a utility node: it is not its own
     * second one.
     */
    @Test
    void keepingTheUtilityNodeAUtilityNodeIsAllowed() {
        ChangeNodeTypeEdit edit = new ChangeNodeTypeEdit(probNet.getNode("cost"), NodeType.UTILITY);

        assertTrue(edit.constraintsWillBeMet());
    }

    @Test
    void turningAChanceNodeIntoADecisionNodeIsAllowed() {
        ChangeNodeTypeEdit edit = new ChangeNodeTypeEdit(probNet.getNode("Disease"), NodeType.DECISION);

        assertTrue(edit.constraintsWillBeMet());
    }
}
