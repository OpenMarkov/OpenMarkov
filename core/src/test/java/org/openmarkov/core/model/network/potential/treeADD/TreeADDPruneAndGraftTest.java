/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.treeADD;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link TreeADDPotential#pruneAndGraftNode}: the name it receives is a plain
 * variable name, not a search pattern, so the brackets of the temporal variables ---{@code X [1]}
 * and the like--- must mean nothing special.
 *
 * @author Manuel Arias
 */
class TreeADDPruneAndGraftTest {

    /** A tree rooted at {@code rootName} whose first branch holds a subtree rooted at Y. */
    private static TreeADDPotential treeRootedAt(String rootName) {
        Variable root = new Variable(rootName, "a", "b");
        Variable child = new Variable("Y", "c", "d");
        TreeADDPotential subtree = new TreeADDPotential(List.of(child), child,
                                                        PotentialRole.CONDITIONAL_PROBABILITY);
        TreeADDPotential tree = new TreeADDPotential(List.of(root, child), root,
                                                     PotentialRole.CONDITIONAL_PROBABILITY);
        tree.getBranches().get(0).setPotential(subtree);
        return tree;
    }

    /**
     * The brackets of a temporal variable name form, read as a pattern, a set of characters, so
     * the name never matched itself and the node was silently left in place.
     */
    @Test
    void aVariableNamedWithBracketsIsFoundAndGrafted() {
        TreeADDPotential tree = treeRootedAt("X [1]");
        tree.pruneAndGraftNode("x [1]");
        assertEquals("Y", tree.getRootVariable().getName(),
                     "the node named X [1] must be pruned and its subtree grafted");
    }

    /** An unclosed bracket, read as a pattern, used to blow up instead of simply not matching. */
    @Test
    void aNameWithAnUnclosedBracketDoesNotBlowUp() {
        TreeADDPotential tree = treeRootedAt("X");
        assertDoesNotThrow(() -> tree.pruneAndGraftNode("X ["));
        assertEquals("X", tree.getRootVariable().getName(), "nothing is named X [, so nothing changes");
    }
}
