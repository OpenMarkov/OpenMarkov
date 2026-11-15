/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DynamicLimidType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A copied variable has its own states, and the links, trees and fixed-value relations of a copied network point at
 * the states of the copied variables, so that renaming a state reaches them and nothing else.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACopiedVariableHasItsOwnStatesTest {

    private ProbNet net;

    @BeforeEach void setUp() {
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Node p = net.addNode(new Variable("P", "absent", "present"), NodeType.CHANCE);
        Node x = net.addNode(new Variable("X", "no", "yes"), NodeType.CHANCE);
        Link<Node> link = net.addLink(p, x, true);
        link.addRevealingState(p.getVariable().getStates()[1]);
        x.setPotential(new TreeADDPotential(List.of(x.getVariable(), p.getVariable()), p.getVariable(),
                                            PotentialRole.CONDITIONAL_PROBABILITY));
        Variable y = new Variable("Y", "no", "yes");
        net.addNode(y, NodeType.CHANCE).setPotential(
                new DeltaPotential(List.of(y), PotentialRole.CONDITIONAL_PROBABILITY, y.getStates()[1]));
    }

    @Test void renamingAStateOfACopiedVariableLeavesTheOriginalAlone() {
        Variable original = net.getVariable("X");
        Variable copy = new Variable(original);

        copy.renameState(copy.getStates()[1], "renamed");

        assertEquals("yes", original.getStates()[1].getName());
    }

    @Test void theRevealingStatesOfACopiedLinkAreTheOnesOfTheCopiedParent() {
        ProbNet copy = net.deepCopy();
        Link<Node> link = copy.getLink(copy.getNode("P"), copy.getNode("X"), true);

        assertSame(copy.getVariable("P").getStates()[1], link.getRevealingStates().getFirst());
    }

    @Test void theBranchesOfACopiedTreeHaveTheStatesOfTheCopiedVariable() {
        ProbNet copy = net.deepCopy();
        List<TreeADDBranch> branches = ((TreeADDPotential) copy.getNode("X").getPotential()).getBranches();
        Variable copiedP = copy.getVariable("P");

        assertEquals(2, branches.size());
        for (TreeADDBranch branch : branches) {
            State state = branch.getBranchStates().getFirst();
            assertSame(copiedP.getStates()[copiedP.getStateIndex(state)], state);
        }
    }

    @Test void theFixedStateOfACopiedRelationIsTheOneOfTheCopiedVariable() {
        ProbNet copy = net.deepCopy();

        assertSame(copy.getVariable("Y").getStates()[1], ((DeltaPotential) copy.getNode("Y").getPotential()).getState());
    }

    @Test void theNodeOfTheNextSliceHasItsOwnStates() {
        ProbNet dbn = new ProbNet(DynamicLimidType.getUniqueInstance());
        Node original = dbn.addNode(new Variable("X [0]", "no", "yes"), NodeType.CHANCE);
        Node shifted = dbn.addShiftedNode(original, 1, 50.0, 0.0);

        shifted.getVariable().renameState(shifted.getVariable().getStates()[1], "renamed");

        assertEquals("yes", original.getVariable().getStates()[1].getName());
    }
}
