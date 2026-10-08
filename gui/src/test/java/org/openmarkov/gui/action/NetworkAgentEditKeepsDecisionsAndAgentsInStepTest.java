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
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DECPOMDPType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * After removing, renaming or moving an agent of the network, and after undoing it, each decision has an
 * agent that is in the list, and the agents keep their properties.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class NetworkAgentEditKeepsDecisionsAndAgentsInStepTest {

    private ProbNet net;
    private Variable first;
    private Variable second;

    @BeforeEach
    void aNetworkWithTwoAgentsAndADecisionOfEach() {
        net = new ProbNet(DECPOMDPType.getUniqueInstance());
        StringWithProperties agent1 = new StringWithProperties("Agent 1");
        agent1.put("role", "doctor");
        net.setAgents(new ArrayList<>(List.of(agent1, new StringWithProperties("Agent 2"))));
        first = new Variable("D1", 2);
        second = new Variable("D2", 2);
        net.addNode(first, NodeType.DECISION);
        net.addNode(second, NodeType.DECISION);
        // A decision gets a new object with the name of the agent, as the properties of the node do
        first.setAgent(new StringWithProperties("Agent 1"));
        second.setAgent(new StringWithProperties("Agent 2"));
    }

    private List<String> agents() {
        return net.getAgents().stream().map(StringWithProperties::getString).toList();
    }

    private String roleOf(String agent) {
        return net.getAgents().stream().filter(a -> a.getString().equals(agent)).findFirst().orElseThrow()
                  .getAdditionalProperties().get("role").toString();
    }

    private void assertAsAtTheBeginning() {
        assertEquals(List.of("Agent 1", "Agent 2"), agents());
        assertEquals("doctor", roleOf("Agent 1"));
        assertEquals("Agent 1", first.getAgent().getString());
        assertEquals("Agent 2", second.getAgent().getString());
    }

    @Test
    void undoingTheRemovalOfAnAgentGivesItsDecisionsBack() throws Exception {
        NetworkAgentEdit edit = new NetworkAgentEdit(net, StateAction.REMOVE, "Agent 1", null);
        edit.executeEdit();

        assertEquals(List.of("Agent 2"), agents());
        assertNull(first.getAgent());
        assertEquals("Agent 2", second.getAgent().getString());

        edit.undo();
        assertAsAtTheBeginning();
    }

    @Test
    void renamingAnAgentRenamesItInItsDecisionsAndKeepsItsProperties() throws Exception {
        NetworkAgentEdit edit = new NetworkAgentEdit(net, StateAction.RENAME, "Agent 1",
                                                     new Object[][]{{"Doctor"}, {"Agent 2"}});
        edit.executeEdit();

        assertEquals(List.of("Doctor", "Agent 2"), agents());
        assertEquals("doctor", roleOf("Doctor"));
        assertEquals("Doctor", first.getAgent().getString());
        assertEquals("Agent 2", second.getAgent().getString());

        edit.undo();
        assertAsAtTheBeginning();
    }

    @Test
    void movingAnAgentKeepsThePropertiesAndTheDecisions() throws Exception {
        NetworkAgentEdit edit = new NetworkAgentEdit(net, StateAction.UP, "", new Object[][]{{"Agent 2"}, {"Agent 1"}});
        edit.executeEdit();

        assertEquals(List.of("Agent 2", "Agent 1"), agents());
        assertEquals("doctor", roleOf("Agent 1"));
        assertEquals("Agent 1", first.getAgent().getString());
        assertEquals("Agent 2", second.getAgent().getString());

        edit.undo();
        assertAsAtTheBeginning();

        NetworkAgentEdit down = new NetworkAgentEdit(net, StateAction.DOWN, "", new Object[][]{{"Agent 2"}, {"Agent 1"}});
        down.executeEdit();
        assertEquals(List.of("Agent 2", "Agent 1"), agents());
        assertEquals("doctor", roleOf("Agent 1"));
    }
}
