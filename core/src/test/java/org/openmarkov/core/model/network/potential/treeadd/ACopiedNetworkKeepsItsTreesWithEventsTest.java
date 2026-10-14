/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.treeadd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.DESNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A node X whose parents are an event E and a chance node Y, with a tree with events or a tree with excluded events.
 * Copying these relations used to give nothing: the copy of the network was left with a node without relation, and
 * the editor of relations, which keeps a copy to compare and to restore, deleted the relation (error 22).
 *
 * @author Manuel Arias
 */
class ACopiedNetworkKeepsItsTreesWithEventsTest {

    private ProbNet probNet;
    private Variable x;
    private Variable e;
    private Variable y;

    @BeforeEach void anEventAndAChanceNodeAreParentsOfX() {
        probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        e = new Variable("E", "no", "yes");
        e.setVariableType(VariableType.EVENT);
        y = new Variable("Y", "y0", "y1");
        x = new Variable("X", "x0", "x1");
        Node eventNode = probNet.addNode(e, NodeType.EVENT);
        Node chanceNode = probNet.addNode(y, NodeType.CHANCE);
        Node node = probNet.addNode(x, NodeType.CHANCE);
        probNet.addLink(eventNode, node, true);
        probNet.addLink(chanceNode, node, true);
    }

    private Potential copyOfTheRelation(Potential relation, ProbNet[] copy) {
        probNet.getNode(x).setPotentials(List.of(relation));
        copy[0] = probNet.deepCopy();
        List<Potential> copied = copy[0].getNode("X").getPotentials();
        assertEquals(1, copied.size());
        return copied.getFirst();
    }

    /** The tree is another object, over the variables of the copy. */
    private static void assertCopiedInto(ProbNet copy, TreeADDPotential original, TreeADDPotential tree) {
        assertNotSame(original, tree);
        assertEquals(original.getVariables().stream().map(Variable::getName).toList(),
                     tree.getVariables().stream().map(Variable::getName).toList());
        for (Variable variable : tree.getVariables()) {
            assertSame(copy.getVariable(variable.getName()), variable);
        }
        assertEquals(original.getBranches().size(), tree.getBranches().size());
    }

    @Test void aTreeWithEventsIsCopiedWithTheTreeOfItsEvent() throws Exception {
        TreeWithEventsPotential relation = new TreeWithEventsPotential(List.of(x, e, y),
                                                                       PotentialRole.CONDITIONAL_PROBABILITY);
        ProbNet[] copy = new ProbNet[1];

        TreeWithEventsPotential copied = assertInstanceOf(TreeWithEventsPotential.class,
                                                          copyOfTheRelation(relation, copy));

        Variable copiedEvent = copy[0].getVariable("E");
        assertEquals(List.of(copiedEvent), copied.getEvents());
        assertEquals(List.of(copiedEvent), List.copyOf(copied.getTrees().keySet()));
        assertCopiedInto(copy[0], relation.getTree(e), copied.getTree(copiedEvent));
    }

    @Test void aTreeWithExcludedEventsIsCopiedWithItsTree() {
        TreeWithExcludedEventsPotential relation = new TreeWithExcludedEventsPotential(
                List.of(x, e, y), PotentialRole.CONDITIONAL_PROBABILITY);
        ProbNet[] copy = new ProbNet[1];

        TreeWithExcludedEventsPotential copied = assertInstanceOf(TreeWithExcludedEventsPotential.class,
                                                                  copyOfTheRelation(relation, copy));

        assertEquals(List.of(copy[0].getVariable("E")), copied.getEvents());
        assertCopiedInto(copy[0], relation.getNoEventTree(), copied.getNoEventTree());
    }
}
