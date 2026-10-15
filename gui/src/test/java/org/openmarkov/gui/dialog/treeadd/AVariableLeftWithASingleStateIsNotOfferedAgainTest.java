/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.treeadd;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;

import javax.swing.tree.TreePath;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tree of X asks first for A, a variable with states or a discretized one. In a branch below, the editor offers
 * A again only if the branches of A above have left it more than one state, and a new subtree of A has the states of
 * the nearest of them. A discretized A used to be offered again under a branch with a single state of it, and adding
 * its subtree died (error 160 of the pending list).
 *
 * @author Manuel Arias
 */
class AVariableLeftWithASingleStateIsNotOfferedAgainTest {

    private Variable x;
    private Variable a;
    private Variable b;
    private Variable c;

    private void variables(VariableType typeOfA, String... statesOfA) {
        x = new Variable("X", "no", "yes");
        a = new Variable("A", statesOfA);
        a.setVariableType(typeOfA);
        b = new Variable("B", "b1", "b2");
        c = new Variable("C", "c1", "c2");
    }

    private TreeADDPotential tree(Variable top) {
        return new TreeADDPotential(List.of(x, a, b, c), top, PotentialRole.CONDITIONAL_PROBABILITY);
    }

    /** A branch of {@code variable} with the states named. */
    private static TreeADDBranch branch(Variable variable, String... states) {
        List<State> branchStates = Arrays.stream(states).map(variable::getState).toList();
        return new TreeADDBranch(branchStates, variable, List.of());
    }

    private static List<String> names(List<State> states) {
        return states.stream().map(State::getName).toList();
    }

    /** Three states: the tree asks for A, {a1} or {a2, a3}, and then for B. */
    @ParameterizedTest
    @EnumSource(value = VariableType.class, names = { "FINITE_STATES", "DISCRETIZED" })
    void underTwoStatesOfAItIsOfferedWithThem(VariableType typeOfA) {
        variables(typeOfA, "a1", "a2", "a3");
        TreeADDPotential root = tree(a);

        TreePath underA2A3 = new TreePath(new Object[] { root, branch(a, "a2", "a3"), tree(b), branch(b, "b1") });
        assertFalse(TreeADDEditorPanel.variablesLeftWithASingleState(underA2A3, root).contains(a));
        assertEquals(List.of("a2", "a3"), names(TreeADDEditorPanel.statesLeftAbove(underA2A3, a, root)));

        TreePath underA1 = new TreePath(new Object[] { root, branch(a, "a1"), tree(b), branch(b, "b1") });
        assertTrue(TreeADDEditorPanel.variablesLeftWithASingleState(underA1, root).contains(a));
        // And if a subtree of A were asked for there, it would have the state left, instead of dying
        assertEquals(List.of("a1"), names(TreeADDEditorPanel.statesLeftAbove(underA1, a, root)));
    }

    /**
     * Four states and two levels of grouping: A, {a1} or {a2, a3, a4}; then B; then A again, {a2} or {a3, a4}; then
     * C. Under {a3, a4} A is offered with those two, the nearest branch, and under {a2} it is not offered.
     */
    @ParameterizedTest
    @EnumSource(value = VariableType.class, names = { "FINITE_STATES", "DISCRETIZED" })
    void theStatesOfferedAreThoseOfTheNearestBranch(VariableType typeOfA) {
        variables(typeOfA, "a1", "a2", "a3", "a4");
        TreeADDPotential root = tree(a);
        TreeADDBranch a2a3a4 = branch(a, "a2", "a3", "a4");
        TreeADDPotential treeOfB = tree(b);
        TreeADDBranch b1 = branch(b, "b1");
        TreeADDPotential treeOfAAgain = tree(a);

        TreePath underA3A4 = new TreePath(new Object[] { root, a2a3a4, treeOfB, b1, treeOfAAgain,
                branch(a, "a3", "a4"), tree(c), branch(c, "c1") });
        assertFalse(TreeADDEditorPanel.variablesLeftWithASingleState(underA3A4, root).contains(a));
        assertEquals(List.of("a3", "a4"), names(TreeADDEditorPanel.statesLeftAbove(underA3A4, a, root)));

        TreePath underA2 = new TreePath(new Object[] { root, a2a3a4, treeOfB, b1, treeOfAAgain,
                branch(a, "a2"), tree(c), branch(c, "c1") });
        assertTrue(TreeADDEditorPanel.variablesLeftWithASingleState(underA2, root).contains(a));
    }
}
