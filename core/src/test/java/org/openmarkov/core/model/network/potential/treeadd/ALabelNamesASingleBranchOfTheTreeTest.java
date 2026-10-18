/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.treeadd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tree of D asks for A: under «a0» it asks for B, and «b0», with the label «outer», asks for C, whose branch «c0»
 * has the label «inner»; «a1» references «outer» and «a2», «inner». A copy put in another branch of the tree does not
 * bring along the labels the tree already has, and a label cannot be given to a second branch (error 292).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ALabelNamesASingleBranchOfTheTreeTest {

    private Variable d;
    private TreeADDPotential root;
    private TreeADDBranch a0;
    private TreeADDBranch a1;
    private TreeADDBranch a2;
    private TreeADDBranch b0;
    private TreeADDBranch b1;
    private TreeADDBranch c0;

    @BeforeEach void setUp() {
        d = new Variable("D", "d0", "d1");
        Variable a = new Variable("A", "a0", "a1", "a2");
        Variable b = new Variable("B", "b0", "b1");
        Variable c = new Variable("C", "c0", "c1");
        List<Variable> variables = List.of(d, a, b, c);
        c0 = new TreeADDBranch(states(c, 0), c, table(0.9, 0.1), variables);
        c0.setLabel("inner");
        TreeADDBranch c1 = new TreeADDBranch(states(c, 1), c, table(0.2, 0.8), variables);
        TreeADDPotential treeOfC = new TreeADDPotential(variables, c, PotentialRole.CONDITIONAL_PROBABILITY,
                                                        List.of(c0, c1));
        b0 = new TreeADDBranch(states(b, 0), b, treeOfC, variables);
        b0.setLabel("outer");
        b1 = new TreeADDBranch(states(b, 1), b, table(0.3, 0.7), variables);
        TreeADDPotential treeOfB = new TreeADDPotential(variables, b, PotentialRole.CONDITIONAL_PROBABILITY,
                                                        List.of(b0, b1));
        a0 = new TreeADDBranch(states(a, 0), a, treeOfB, variables);
        a1 = new TreeADDBranch(states(a, 1), a, "outer", variables);
        a2 = new TreeADDBranch(states(a, 2), a, "inner", variables);
        root = new TreeADDPotential(variables, a, PotentialRole.CONDITIONAL_PROBABILITY, List.of(a0, a1, a2));
    }

    private TablePotential table(double... values) {
        return new TablePotential(List.of(d), PotentialRole.CONDITIONAL_PROBABILITY, values);
    }

    private static List<State> states(Variable variable, int index) {
        return List.of(variable.getStates()[index]);
    }

    /** @return how many branches of {@code tree}, at any depth, have {@code label} */
    private static int branchesWithTheLabel(Potential tree, String label) {
        int count = 0;
        Deque<TreeADDPotential> subtrees = new ArrayDeque<>(List.of((TreeADDPotential) tree));
        while (!subtrees.isEmpty()) {
            for (TreeADDBranch branch : subtrees.pop().getBranches()) {
                if (label.equals(branch.getLabel())) {
                    count++;
                }
                if (!branch.isReference() && branch.getPotential() instanceof TreeADDPotential subtree) {
                    subtrees.push(subtree);
                }
            }
        }
        return count;
    }

    @Test void removingALabelGivesTheReferenceACopyWithoutTheLabelsTheTreeHas() {
        b0.setLabel(null);

        root.copyThePotentialOfReferencesWithoutLabel();

        assertFalse(a1.isReference());
        assertEquals(1, branchesWithTheLabel(root, "inner"));
        // A change in the copy that «a1» got does not reach «a2», neither here nor in a copy of the tree
        TreeADDPotential copyOfC = (TreeADDPotential) a1.getPotential();
        ((TablePotential) copyOfC.getBranches().getFirst().getPotential()).setValues(new double[] { 0.5, 0.5 });
        TreeADDPotential copyOfTheTree = (TreeADDPotential) root.copy();
        assertArrayEquals(new double[] { 0.9, 0.1 },
                          ((TablePotential) copyOfTheTree.getBranches().get(2).getPotential()).getValues(), 0);
    }

    @Test void removingTheSubtreeWithTheLabelsLeavesNoReferenceWithoutItsLabel() {
        a0.setPotential(table(0.5, 0.5));

        root.copyThePotentialOfReferencesWithoutLabel();

        assertTrue(root.getReferencesWithoutLabel().isEmpty());
        assertFalse(a1.isReference());
        assertTrue(branchesWithTheLabel(root, "inner") <= 1);
        assertArrayEquals(new double[] { 0.9, 0.1 }, ((TablePotential) a2.getPotential()).getValues(), 0);
    }

    @Test void aLabelThatAnotherBranchHasIsNotFree() {
        assertFalse(root.isLabelFreeFor("inner", b1));
        assertTrue(root.isLabelFreeFor("inner", c0));
        assertTrue(root.isLabelFreeFor("new", b1));
    }

    @Test void aCopyForTheTreeLeavesOutTheLabelsTheTreeHas() {
        Potential copy = root.copyForThisTree(b0.getPotential());

        assertEquals(0, branchesWithTheLabel(copy, "inner"));
        assertEquals(1, branchesWithTheLabel(root, "inner"));
    }
}
