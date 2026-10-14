/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The uncertain parameters of a network, which the sensitivity analysis lists, come out node by node and, within a
 * node, in the order of the cells. They used to come out within each node in an order that depended on where each
 * parameter was in memory, so the list, and the parameter the dialog proposes, changed every time it was opened
 * (error 93). The network is built many times, because a single one could come out in the right order by chance.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class UncertainParametersComeOutInTheOrderOfTheCellsTest {

    private static final int RUNS = 50;
    private static final int STATES = 10;

    /** A node whose cells are uncertain parameters named with the name of the node and the number of the cell. */
    private static void addNode(ProbNet probNet, String name, List<String> expected) {
        String[] states = new String[STATES];
        for (int state = 0; state < STATES; state++) {
            states[state] = "s" + state;
        }
        Variable variable = new Variable(name, states);
        Node node = probNet.addNode(variable, NodeType.CHANCE);
        UncertainTablePotential table = new UncertainTablePotential(List.of(variable),
                                                                    PotentialRole.CONDITIONAL_PROBABILITY);
        UncertainValue[] values = new UncertainValue[STATES];
        for (int cell = 0; cell < STATES; cell++) {
            values[cell] = new UncertainValue(new BetaFunction(3, 7), name + cell);
            expected.add(name + cell);
        }
        table.setUncertainValues(values);
        node.setPotential(table);
    }

    @Test void theParametersComeOutNodeByNodeInTheOrderOfTheCells() {
        for (int run = 0; run < RUNS; run++) {
            ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
            List<String> expected = new ArrayList<>();
            addNode(probNet, "A", expected);
            addNode(probNet, "B", expected);

            List<String> names = SystematicSampling.getUncertainParameters(probNet).stream()
                                                   .map(UncertainParameter::getName).toList();

            assertEquals(expected, names);
        }
    }
}
