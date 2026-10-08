/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.node;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The selector of agents in the properties of a decision marks the agent that has the name of the agent of
 * the decision, and its empty entry when the network has no agent with that name.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheAgentOfADecisionIsFoundByItsNameTest {

    private static final String[] AGENTS = {"", "Agent 1", "Agent 2"};

    @Test
    void anAgentWithTheSameNameIsFoundThoughItIsAnotherObject() {
        assertEquals(1, NodeDefinitionPanel.indexOfAgent(new String("Agent 1"), AGENTS));
        assertEquals(2, NodeDefinitionPanel.indexOfAgent(new String("Agent 2"), AGENTS));
    }

    @Test
    void anAgentThatIsNotInTheListLeavesTheSelectorEmpty() {
        assertEquals(0, NodeDefinitionPanel.indexOfAgent("Doctor", AGENTS));
    }
}
