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
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.action.core.NodeStateEdit;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.window.edition.SelectedContent;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A pasted node has its own states: renaming a state of the pasted node, or of the original, leaves the other one
 * alone, and the relations of the pasted nodes follow the rename of their own states.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class APastedNodeHasItsOwnStatesTest {

    private ProbNet net;
    private Node p;
    private Node x;
    private Node y;

    @BeforeEach void setUp() {
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        p = net.addNode(new Variable("P", "absent", "present"), NodeType.CHANCE);
        x = net.addNode(new Variable("X", "no", "yes"), NodeType.CHANCE);
        net.addLink(p, x, true);
        x.setPotential(new TreeADDPotential(List.of(x.getVariable(), p.getVariable()), p.getVariable(),
                                            PotentialRole.CONDITIONAL_PROBABILITY));
        Variable yVariable = new Variable("Y", "no", "yes");
        y = net.addNode(yVariable, NodeType.CHANCE);
        y.setPotential(new DeltaPotential(List.of(yVariable), PotentialRole.CONDITIONAL_PROBABILITY, yVariable.getStates()[1]));
        net.getPNESupport().setWithUndo(true);
    }

    /** Pastes the nodes named {@code names} as the window does: taken from a deep copy of the network. */
    private void paste(String... names) throws Exception {
        ProbNet copy = net.deepCopy();
        List<Node> nodes = Arrays.stream(names).map(copy::getNode).toList();
        List<Link<Node>> links = copy.getLinks().stream()
                                     .filter(link -> nodes.contains(link.getFrom()) && nodes.contains(link.getTo()))
                                     .toList();
        new PasteEdit(net, new SelectedContent(nodes, links), null).executeEdit();
    }

    /** Renames the state {@code oldName} of {@code node} as the domain tab of its properties does. */
    private static void rename(Node node, String oldName, String newName) throws Exception {
        Variable variable = node.getVariable();
        int rowInTheTab = variable.getNumStates() - 1 - variable.getStateIndex(oldName);
        new NodeStateEdit(node, StateAction.RENAME, rowInTheTab, newName).executeEdit();
    }

    private static List<String> stateNames(Node node) {
        return Arrays.stream(node.getVariable().getStates()).map(State::getName).toList();
    }

    /** @return the names of the states of the branches of the tree of {@code node}, in alphabetical order */
    private static List<String> branchNames(Node node) {
        return ((TreeADDPotential) node.getPotential()).getBranches().stream()
                                                       .map(TreeADDBranch::getBranchStates)
                                                       .flatMap(List::stream)
                                                       .map(State::getName)
                                                       .sorted()
                                                       .toList();
    }

    @Test void renamingAStateOfThePastedNodeLeavesTheOriginalAlone() throws Exception {
        paste("X");

        rename(net.getNode("X'"), "yes", "renamed");

        assertEquals(List.of("no", "renamed"), stateNames(net.getNode("X'")));
        assertEquals(List.of("no", "yes"), stateNames(x));
    }

    @Test void renamingAStateOfTheOriginalLeavesThePastedNodeAlone() throws Exception {
        paste("X");

        rename(x, "yes", "renamed");

        assertEquals(List.of("no", "renamed"), stateNames(x));
        assertEquals(List.of("no", "yes"), stateNames(net.getNode("X'")));
    }

    @Test void undoingARenameGivesTheNameBackOnlyToTheNodeRenamed() throws Exception {
        paste("X");
        rename(x, "yes", "original");
        rename(net.getNode("X'"), "yes", "pasted");

        net.getPNESupport().undo();

        assertEquals(List.of("no", "yes"), stateNames(net.getNode("X'")));
        assertEquals(List.of("no", "original"), stateNames(x));
    }

    @Test void theTreeOfAPastedChildFollowsARenameOfItsPastedParent() throws Exception {
        paste("P", "X");

        rename(net.getNode("P'"), "present", "renamed");

        assertEquals(List.of("absent", "renamed"), branchNames(net.getNode("X'")));
        assertEquals(List.of("absent", "present"), branchNames(x));
    }

    @Test void theFixedStateOfAPastedNodeFollowsARenameOfIt() throws Exception {
        paste("Y");

        rename(net.getNode("Y'"), "yes", "renamed");

        assertEquals("renamed", ((DeltaPotential) net.getNode("Y'").getPotential()).getState().getName());
        assertEquals("yes", ((DeltaPotential) y.getPotential()).getState().getName());
    }
}
