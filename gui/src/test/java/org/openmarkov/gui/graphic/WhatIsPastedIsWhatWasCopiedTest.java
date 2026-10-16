/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.graphic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.action.core.NodeStateEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.action.PasteEdit;
import org.openmarkov.gui.window.edition.EditorPanelClipboardAssistant;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * What is pasted is what the copied nodes were when they were copied or cut, whatever has been done to
 * the network since.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class WhatIsPastedIsWhatWasCopiedTest {

    private ProbNet net;
    private VisualNetwork visualNetwork;
    private EditorPanelClipboardAssistant clipboard;

    /** A → C, with P(A) = (0.3, 0.7) and P(C | A) = (0.3, 0.7; 0.6, 0.4). */
    @BeforeEach void aBayesianNetwork() {
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("clipboard");
        Variable a = new Variable("A", "a0", "a1");
        Variable c = new Variable("C", "c0", "c1");
        Node nodeA = net.addNode(a, NodeType.CHANCE);
        Node nodeC = net.addNode(c, NodeType.CHANCE);
        net.addLink(a, c, true);
        nodeA.setPotential(new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7 }));
        nodeC.setPotential(new TablePotential(List.of(c, a), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7, 0.6, 0.4 }));
        visualNetwork = new VisualNetwork(net, null);
        clipboard = new EditorPanelClipboardAssistant();
    }

    private double[] valuesOf(String node) {
        return ((TablePotential) net.getNode(node).getPotential()).getValues();
    }

    @Test void aCutChildKeepsTheParentInItsRelation() throws Exception {
        visualNetwork.setSelectedAllObjects(true);
        visualNetwork.exportToClipboard(true, clipboard);
        new PasteEdit(net, clipboard.paste(), null).executeEdit();

        assertEquals(List.of(net.getVariable("C"), net.getVariable("A")), net.getNode("C").getPotential().getVariables());
        assertArrayEquals(new double[] { 0.3, 0.7, 0.6, 0.4 }, valuesOf("C"));
    }

    @Test void aTableChangedAfterCopyingIsNotPasted() throws Exception {
        visualNetwork.setSelectedNode("A", true);
        visualNetwork.exportToClipboard(false, clipboard);
        Variable a = net.getVariable("A");
        net.getNode("A").setPotential(new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.9, 0.1 }));
        new PasteEdit(net, clipboard.paste(), null).executeEdit();

        assertArrayEquals(new double[] { 0.3, 0.7 }, valuesOf("A'"));
    }

    @Test void aStateAddedAfterCopyingIsNotPasted() throws Exception {
        visualNetwork.setSelectedNode("A", true);
        visualNetwork.exportToClipboard(false, clipboard);
        new NodeStateEdit(net.getNode("A"), StateAction.ADD, 0, "a2").executeEdit();
        new PasteEdit(net, clipboard.paste(), null).executeEdit();

        assertEquals(2, net.getVariable("A'").getNumStates());
        assertArrayEquals(new double[] { 0.3, 0.7 }, valuesOf("A'"));
    }
}
