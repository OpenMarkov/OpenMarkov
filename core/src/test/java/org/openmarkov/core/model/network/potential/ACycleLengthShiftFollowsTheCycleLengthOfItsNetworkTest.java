/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

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
