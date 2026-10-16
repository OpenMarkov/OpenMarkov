/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Replacing the states of a node by a different number of them, which is what choosing a standard domain of another
 * size does, works on a node without relation, such as a decision without an imposed policy, and on a node one of
 * whose children has none; undoing it gives every node the relations it had (error 243 of the pending list).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheStatesOfANodeWithoutRelationCanBeReplacedTest {

    private static final double[] DISEASE = {0.86, 0.14};
    private static final double[] RESULT = {1, 0, 0, 1, 0, 0, 0, 0.9, 0.1, 0, 0.2, 0.8};

    private ProbNet net;
    private Node disease;
    private Node doTest;
    private Node result;
    private Node therapy;

    /** Disease → Result ← Do test?, and Result → Therapy. The two decisions have no relation. */
    @BeforeEach void anInfluenceDiagram() {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        net.getPNESupport().setWithUndo(true);
        Variable x = new Variable("Disease", "absent", "present");
        Variable t = new Variable("Do test?", "no", "yes");
        Variable y = new Variable("Result", "not done", "negative", "positive");
        Variable d = new Variable("Therapy", "no", "yes");
        disease = net.addNode(x, NodeType.CHANCE);
        doTest = net.addNode(t, NodeType.DECISION);
        result = net.addNode(y, NodeType.CHANCE);
        therapy = net.addNode(d, NodeType.DECISION);
        net.addLink(x, y, true);
        net.addLink(t, y, true);
        net.addLink(y, d, true);
        disease.setPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, DISEASE.clone()));
        result.setPotential(new TablePotential(List.of(y, x, t), PotentialRole.CONDITIONAL_PROBABILITY, RESULT.clone()));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"Therapy", "Result", "Do test?"})
    void oneStateMoreAndUndoingGivesTheRelationsBack(String name) throws Exception {
        Node node = net.getNode(name);
        int statesBefore = node.getVariable().getNumStates();
        State[] oneMore = IntStream.rangeClosed(1, statesBefore + 1).mapToObj(i -> new State("s" + i)).toArray(State[]::new);

        new NodeReplaceStatesEdit(node, oneMore).executeEdit();
        assertEquals(statesBefore + 1, node.getVariable().getNumStates());

        net.getPNESupport().undo();
        assertEquals(statesBefore, node.getVariable().getNumStates());
        assertTrue(therapy.getPotentials().isEmpty());
        assertTrue(doTest.getPotentials().isEmpty());
        assertArrayEquals(RESULT, ((TablePotential) result.getPotential()).getValues());
        assertArrayEquals(DISEASE, ((TablePotential) disease.getPotential()).getValues());
    }
}
