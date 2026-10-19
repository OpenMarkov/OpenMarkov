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
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.action.PasteEdit;
import org.openmarkov.gui.window.edition.EditorPanelClipboardAssistant;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A Markov influence diagram with «Health [0]» → «Health [1]». A copy of a node of a cycle pasted in its own network
 * takes the apostrophe after its base name, «Health' [1]», and stays in its cycle; it used to be refused, because the
 * apostrophe written after the cycle was dropped and the copy took the name of the original (error 271).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ATemporalNodeIsPastedInItsOwnNetworkTest {

    private ProbNet net;
    private VisualNetwork visualNetwork;
    private EditorPanelClipboardAssistant clipboard;

    @BeforeEach void aMarkovInfluenceDiagram() {
        net = new ProbNet(MIDType.getUniqueInstance());
        Variable health0 = new Variable("Health", "good", "bad");
        health0.setTimeSlice(0);
        Variable health1 = new Variable("Health", "good", "bad");
        health1.setTimeSlice(1);
        net.addNode(health0, NodeType.CHANCE);
        net.addNode(health1, NodeType.CHANCE);
        net.addLink(health0, health1, true);
        visualNetwork = new VisualNetwork(net, null);
        clipboard = new EditorPanelClipboardAssistant();
    }

    private Set<String> names() {
        return net.getVariables().stream().map(Variable::getName).collect(Collectors.toSet());
    }

    @Test void aNodeOfACycleIsPastedInTheSameCycle() throws Exception {
        visualNetwork.setSelectedNode("Health [1]", true);
        visualNetwork.exportToClipboard(false, clipboard);
        new PasteEdit(net, clipboard.paste(), null).executeEdit();

        assertEquals(Set.of("Health [0]", "Health [1]", "Health' [1]"), names());
        assertEquals(1, net.getVariable("Health' [1]").getTimeSlice());
    }

    @Test void twoCyclesOfANodeArePastedAsTwoCyclesOfTheCopy() throws Exception {
        visualNetwork.setSelectedAllObjects(true);
        visualNetwork.exportToClipboard(false, clipboard);
        new PasteEdit(net, clipboard.paste(), null).executeEdit();

        assertEquals(Set.of("Health [0]", "Health [1]", "Health' [0]", "Health' [1]"), names());
        assertNotNull(net.getLink(net.getNode("Health' [0]"), net.getNode("Health' [1]"), true));
    }
}
