/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.State;
import java.util.ArrayList;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.io.ProbNetWriter;
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
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A cycle length shift is read before the cycle length of its network, and has to end up with it.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ACycleLengthShiftOpensWithTheCycleLengthOfItsNetworkTest {

    @ParameterizedTest @ValueSource(strings = {"0.2", "1.0"})
    void aCycleOfHalfAYearSurvivesSavingAndOpening(String format, @TempDir Path folder) throws Exception {
        ProbNetWriter writer = format.equals("0.2") ? new PGMXWriter_0_2() : new PGMXWriter_1_0();
        Path file = folder.resolve("network.pgmx");

        writer.write(file.toString(), network(new CycleLength(CycleLength.Unit.YEAR, 0.5)), List.of());
        ProbNet reread = new PGMXReader().read(file.toUri().toURL()).probNet();

        assertEquals(0.5, reread.getCycleLength().getValue());
        assertEquals(60.5, age1(reread));
    }

    @ParameterizedTest @ValueSource(strings = {"0.2", "1.0"})
    void theShiftsInTheLeavesOfATreeSurviveSavingAndOpening(String format, @TempDir Path folder) throws Exception {
        ProbNetWriter writer = format.equals("0.2") ? new PGMXWriter_0_2() : new PGMXWriter_1_0();
        Path file = folder.resolve("network.pgmx");

        writer.write(file.toString(), networkWithTheShiftsInATree(new CycleLength(CycleLength.Unit.YEAR, 0.5)),
                     List.of());
        ProbNet reread = new PGMXReader().read(file.toUri().toURL()).probNet();

        assertEquals(List.of(0.5, 0.5), yearsOfTheShifts(reread));
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
