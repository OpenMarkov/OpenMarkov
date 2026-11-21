/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.State;
import java.util.ArrayList;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.CycleLengthEdit;
import org.openmarkov.core.model.network.CycleLength;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Changing the cycle length of a network in «Temporal options» reaches the cycle length shifts it already has.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACycleLengthShiftFollowsTheCycleLengthOfItsNetworkTest {

    @Test void editingTheCycleLengthReachesTheShiftAndUndoingGivesTheOldOneBack() throws Exception {
        ProbNet network = network(new CycleLength(CycleLength.Unit.YEAR, 1));
        network.getPNESupport().setWithUndo(true);

        new CycleLengthEdit(network, new CycleLength(CycleLength.Unit.YEAR, 0.5)).executeEdit();
        assertEquals(60.5, age1(network));

        network.getPNESupport().undo();
        assertEquals(61, age1(network));

        network.getPNESupport().redo();
        assertEquals(60.5, age1(network));
    }

    /** Error 433: the shifts in the leaves of a tree follow the cycle length too. */
    @Test void theShiftsInTheLeavesOfATreeFollowItToo() {
        ProbNet network = networkWithTheShiftsInATree(new CycleLength(CycleLength.Unit.YEAR, 1));
        assertEquals(List.of(1.0, 1.0), yearsOfTheShifts(network));

        network.setCycleLength(new CycleLength(CycleLength.Unit.YEAR, 0.5));

        assertEquals(List.of(0.5, 0.5), yearsOfTheShifts(network));
    }

    /** The same network with «Age [1]» as a tree on «Sex» whose two leaves are cycle length shifts. */
    static ProbNet networkWithTheShiftsInATree(CycleLength cycleLength) {
        ProbNet network = network(cycleLength);
        Variable age0 = network.getVariable("Age [0]");
        Variable age1 = network.getVariable("Age [1]");
        Variable sex = new Variable("Sex", "man", "woman");
        network.addNode(sex, NodeType.CHANCE)
               .setPotential(new UniformPotential(List.of(sex), PotentialRole.CONDITIONAL_PROBABILITY));
        network.addLink(sex, age1, true);
        List<Variable> variables = List.of(age1, age0, sex);
        List<TreeADDBranch> branches = new ArrayList<>();
        for (State state : sex.getStates()) {
            branches.add(new TreeADDBranch(List.of(state), sex,
                                           new CycleLengthShift(List.of(age1, age0), network.getCycleLength()),
                                           variables));
        }
        network.getNode(age1).setPotential(
                new TreeADDPotential(variables, sex, PotentialRole.CONDITIONAL_PROBABILITY, branches));
        return network;
    }

    /** The cycle lengths, in years, of the cycle length shifts in the relation of «Age [1]». */
    static List<Double> yearsOfTheShifts(ProbNet network) {
        return CycleLengthShift.allIn(network.getNode("Age [1]").getPotential()).stream()
                               .map(shift -> shift.getCycleLength().getValue())
                               .toList();
    }

    /** A Markov influence diagram whose «Age [1]» is a cycle length shift of «Age [0]», which is 60. */
    static ProbNet network(CycleLength cycleLength) {
        ProbNet network = new ProbNet(MIDType.getUniqueInstance());
        network.setCycleLength(cycleLength);
        Variable age0 = new Variable("Age", true, 0, Double.POSITIVE_INFINITY, false, 0.01);
        Variable age1 = new Variable("Age", true, 0, Double.POSITIVE_INFINITY, false, 0.01);
        age0.setTimeSlice(0);
        age1.setTimeSlice(1);
        network.addNode(age0, NodeType.CHANCE)
               .setPotential(new DeltaPotential(List.of(age0), PotentialRole.CONDITIONAL_PROBABILITY, 60));
        network.addNode(age1, NodeType.CHANCE);
        network.addLink(age0, age1, true);
        network.getNode(age1).setPotential(new CycleLengthShift(List.of(age1, age0), network.getCycleLength()));
        return network;
    }

    /** The value of the cycle length shift of «Age [1]» when «Age [0]» is 60. */
    static double age1(ProbNet network) throws Exception {
        Variable age0 = network.getVariable("Age [0]");
        Potential shift = network.getNode("Age [1]").getPotential();
        return shift.tableProject(new EvidenceCase(List.of(new Finding(age0, 60.0))), null, List.of()).getValues()[0];
    }
}
