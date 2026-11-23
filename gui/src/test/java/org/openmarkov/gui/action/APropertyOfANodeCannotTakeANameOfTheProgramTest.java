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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.exception.OtherPropertyNameIsUsedByTheProgramException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The files keep the relevance and the purpose of a node, an obsolete title and a mark of the events among the
 * properties of the node, so the tab «Other properties» refuses those four names for a property of the user.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class APropertyOfANodeCannotTakeANameOfTheProgramTest {

    private ProbNet net;
    private Node node;

    @BeforeEach void aNodeWithAProperty() throws Exception {
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        node = net.addNode(new Variable("A", "no", "yes"), NodeType.CHANCE);
        new OtherPropertyEdit(node, "ADD", 0, new String[] { "Source", "a study" }).executeEdit();
    }

    @ParameterizedTest @ValueSource(strings = {"Relevance", "Purpose", "Title", "AlwaysAppend"})
    void renamingAPropertyToIt(String name) {
        OtherPropertyNameIsUsedByTheProgramException refusal = assertThrows(
                OtherPropertyNameIsUsedByTheProgramException.class,
                () -> new OtherPropertyEdit(node, "RENAME", 0, new String[] { name, "a study" }).executeEdit());

        assertEquals("A property cannot be named " + name
                     + ": the program keeps its own data under that name in the files.",
                     refusal.getExceptionMessage());
        assertEquals(Map.of("Source", "a study"), node.getAdditionalProperties());
    }

    @ParameterizedTest @ValueSource(strings = {"Relevance", "Purpose", "Title", "AlwaysAppend"})
    void addingAPropertyWithIt(String name) {
        assertThrows(OtherPropertyNameIsUsedByTheProgramException.class,
                     () -> new OtherPropertyEdit(node, "ADD", 0, new String[] { name, "x" }).executeEdit());

        assertEquals(Map.of("Source", "a study"), node.getAdditionalProperties());
    }

    @Test void anotherNameIsTaken() throws Exception {
        new OtherPropertyEdit(node, "RENAME", 0, new String[] { "relevance of the source", "high" }).executeEdit();

        assertEquals(Map.of("relevance of the source", "high"), node.getAdditionalProperties());
    }

    @Test void aPropertyOfTheNetworkMayHaveIt() throws Exception {
        new OtherPropertyEdit(net, "ADD", 0, new String[] { "Purpose", "teaching" }).executeEdit();

        assertEquals("teaching", net.getAdditionalProperties().get("Purpose"));
    }
}
