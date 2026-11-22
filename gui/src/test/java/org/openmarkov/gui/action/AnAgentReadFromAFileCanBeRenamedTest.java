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
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.StringWithProperties;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DECPOMDPType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * An agent without properties, of a network saved and opened again, can be renamed and can be given a
 * property (error 441).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnAgentReadFromAFileCanBeRenamedTest {

    @TempDir Path directory;

    private ProbNet net;

    @BeforeEach
    void aSavedNetworkWithTwoAgentsAndADecisionOfEachIsOpened() throws Exception {
        ProbNet saved = new ProbNet(DECPOMDPType.getUniqueInstance());
        saved.setAgents(new ArrayList<>(List.of(new StringWithProperties("Agent 1"),
                                                new StringWithProperties("Agent 2"))));
        Variable first = new Variable("D1", 2);
        Variable second = new Variable("D2", 2);
        saved.addNode(first, NodeType.DECISION);
        saved.addNode(second, NodeType.DECISION);
        first.setAgent(new StringWithProperties("Agent 1"));
        second.setAgent(new StringWithProperties("Agent 2"));
        Path file = directory.resolve("agents.pgmx");
        new PGMXWriter_1_0().write(file.toString(), saved, null);

        URL url = file.toUri().toURL();
        net = FormatManager.getInstance().getProbNetReader(url).read(url).probNet();
    }

    private String agentOf(String decision) throws Exception {
        return net.getVariable(decision).getAgent().getString();
    }

    @Test
    void renamingAnAgentRenamesItInTheListAndInItsDecisions() throws Exception {
        new NetworkAgentEdit(net, StateAction.RENAME, "Agent 1", new Object[][]{{"Boss"}, {"Agent 2"}}).executeEdit();

        assertEquals(List.of("Boss", "Agent 2"),
                     net.getAgents().stream().map(StringWithProperties::getString).toList());
        assertEquals("Boss", agentOf("D1"));
        assertEquals("Agent 2", agentOf("D2"));
    }

    @Test
    void anAgentCanBeGivenAProperty() {
        StringWithProperties agent = net.getAgents().getFirst();

        agent.put("speciality", "surgery");

        assertEquals("surgery", agent.getAdditionalProperties().get("speciality"));
    }
}
