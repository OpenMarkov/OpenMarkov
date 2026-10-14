/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.node;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithEventsPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithExcludedEventsPotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The editor of relations of a node X with a tree with events or a tree with excluded events, opened and closed
 * without touching anything. It used to delete the relation, on «Cancel» at once and on «OK» when undoing, because
 * the copy it keeps to compare and to restore was empty (error 22).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class OpeningTheEditorOfATreeWithEventsDoesNotDeleteItTest {

    /** X, whose parents are an event E and a chance node Y, with a tree with events or with excluded events. */
    private static Node aNodeWithATree(boolean excludedEvents) {
        ProbNet probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        Variable e = new Variable("E", "no", "yes");
        e.setVariableType(VariableType.EVENT);
        Variable y = new Variable("Y", "y0", "y1");
        Variable x = new Variable("X", "x0", "x1");
        Node eventNode = probNet.addNode(e, NodeType.EVENT);
        Node chanceNode = probNet.addNode(y, NodeType.CHANCE);
        Node node = probNet.addNode(x, NodeType.CHANCE);
        probNet.addLink(eventNode, node, true);
        probNet.addLink(chanceNode, node, true);
        List<Variable> variables = List.of(x, e, y);
        node.setPotentials(List.of(excludedEvents
                                   ? new TreeWithExcludedEventsPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY)
                                   : new TreeWithEventsPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY)));
        return node;
    }

    /** As the dialog of the relation does: the editor works inside a history of its own. */
    private static PotentialEditPanel open(Node node) throws Exception {
        node.getProbNet().getPNESupport().openNewSubEditHistory();
        return new PotentialEditPanel(node, false, true);
    }

    private static void assertKeepsARelationLike(Class<?> expected, Node node) {
        List<Potential> relations = node.getPotentials();
        assertEquals(1, relations.size(), "the node must keep its relation");
        assertSame(expected, relations.getFirst().getClass());
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void okWithoutChangesAndUndoKeepTheRelation(boolean excludedEvents) throws Exception {
        Node node = aNodeWithATree(excludedEvents);
        Class<?> relationClass = node.getPotential().getClass();

        open(node).commitChanges();
        node.getProbNet().getPNESupport().undo();

        assertKeepsARelationLike(relationClass, node);
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void cancelWithoutChangesKeepsTheRelation(boolean excludedEvents) throws Exception {
        Node node = aNodeWithATree(excludedEvents);
        Class<?> relationClass = node.getPotential().getClass();

        open(node).uncommitChanges();

        assertKeepsARelationLike(relationClass, node);
    }
}
