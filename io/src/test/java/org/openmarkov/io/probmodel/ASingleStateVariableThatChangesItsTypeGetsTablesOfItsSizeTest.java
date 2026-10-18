/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openmarkov.core.action.core.VariableTypeEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * X → Y, X with a single state. Changing the type of X between finite states and discretized gives it the two states
 * a new variable has, and the tables of X and Y get the size of their variables, so the saved network opens again
 * (error 294).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ASingleStateVariableThatChangesItsTypeGetsTablesOfItsSizeTest {

    @ParameterizedTest
    @EnumSource(value = VariableType.class, names = { "FINITE_STATES", "DISCRETIZED" })
    void theTablesFitTheNewStatesAndTheNetworkOpensAgain(VariableType initialType, @TempDir Path folder)
            throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable x = new Variable("X", "only");
        x.setVariableType(initialType);
        Variable y = new Variable("Y", "y0", "y1");
        Node nodeX = net.addNode(x, NodeType.CHANCE);
        Node nodeY = net.addNode(y, NodeType.CHANCE);
        net.addLink(x, y, true);
        nodeX.setPotential(new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 1 }));
        nodeY.setPotential(new TablePotential(List.of(y, x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.2, 0.8 }));
        VariableType otherType = initialType == VariableType.FINITE_STATES ? VariableType.DISCRETIZED
                : VariableType.FINITE_STATES;

        new VariableTypeEdit(nodeX, otherType, true).executeEdit();

        assertEquals(2, x.getNumStates());
        assertEquals(2, nodeX.getPotential().getCPT().getValues().length);
        assertEquals(4, nodeY.getPotential().getCPT().getValues().length);
        Path file = folder.resolve("network.pgmx");
        new PGMXWriter_1_0().write(file.toString(), net, List.of());
        ProbNet reread = new PGMXReader().read(file.toUri().toURL()).probNet();
        assertEquals(2, reread.getVariable("X").getNumStates());
    }
}
