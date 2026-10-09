/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.InvertLinkEdit;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A network of events with the chance nodes «Ill», «Mood» and «Pain» and the event «Relapse».
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class EveryCycleOfAnEventNetworkHasAnEventTest {

    private ProbNet probNet;
    private Node ill;
    private Node mood;
    private Node pain;
    private Node relapse;

    @BeforeEach void build() {
        probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        ill = probNet.addNode(new Variable("Ill", "no", "yes"), NodeType.CHANCE);
        mood = probNet.addNode(new Variable("Mood", "low", "high"), NodeType.CHANCE);
        pain = probNet.addNode(new Variable("Pain", "no", "yes"), NodeType.CHANCE);
        relapse = probNet.addNode(new Variable("Relapse", "no", "yes"), NodeType.EVENT);
    }

    private AddLinkEdit link(Node parent, Node child) {
        return new AddLinkEdit(probNet, parent.getVariable(), child.getVariable(), true);
    }

    @Test void aLinkThatClosesACycleOfChanceNodesIsRefused() throws Exception {
        link(ill, mood).executeEdit();
        link(mood, pain).executeEdit();

        assertThrows(DoEditException.class, link(pain, ill)::executeEdit);
    }

    @Test void aCycleThroughAnEventIsAllowed() throws Exception {
        link(ill, relapse).executeEdit();
        link(relapse, mood).executeEdit();

        link(mood, ill).executeEdit();

        assertDoesNotThrow(() -> probNet.checkConstraints());
    }

    @Test void aLinkFromAChanceNodeToItselfIsAllowed() {
        assertDoesNotThrow(() -> link(ill, ill).executeEdit());
    }

    @Test void invertingALinkIsRefusedWhenItWouldCloseACycleOfChanceNodes() throws Exception {
        link(ill, mood).executeEdit();
        link(ill, pain).executeEdit();
        link(pain, mood).executeEdit();

        assertThrows(DoEditException.class,
                     new InvertLinkEdit(probNet, ill.getVariable(), mood.getVariable(), true)::executeEdit);
    }

    @Test void aNetworkThatAlreadyHasACycleOfChanceNodesBreaksTheConstraint() {
        probNet.addLink(ill, mood, true);
        probNet.addLink(mood, ill, true);

        assertThrows(ConstraintViolatedException.class, probNet::checkConstraints);
    }
}
