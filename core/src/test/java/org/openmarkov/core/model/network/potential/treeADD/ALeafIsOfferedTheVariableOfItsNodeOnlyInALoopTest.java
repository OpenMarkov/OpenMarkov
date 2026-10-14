/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.treeADD;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The variables that the tree editor offers to add to the leaf of a branch, in a tree of P(X | A) that branches by A
 * and has the leaf P(X). The variable X of the node is already in the leaf, so it is offered again only when the node
 * is a parent of itself, as in a network of events, where X is twice among the variables of the tree.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ALeafIsOfferedTheVariableOfItsNodeOnlyInALoopTest {

    private final Variable x = new Variable("X", "x1", "x2");
    private final Variable a = new Variable("A", "a1", "a2");
    private final Variable b = new Variable("B", "b1", "b2");

    private List<Variable> offeredToTheLeafIn(List<Variable> variablesOfTheTree) {
        TablePotential leaf = new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY);
        return new TreeADDBranch(List.of(a.getStates()[0]), a, leaf, variablesOfTheTree).getAddableVariables();
    }

    @Test void inAnOrdinaryTree() {
        assertEquals(List.of(b), offeredToTheLeafIn(List.of(x, a, b)));
    }

    @Test void whenTheNodeIsAParentOfItself() {
        assertEquals(List.of(b, x), offeredToTheLeafIn(List.of(x, a, b, x)));
    }
}
