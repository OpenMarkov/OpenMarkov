/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.canonical.MaxPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.ArrayList;
import java.util.List;

/**
 * Noisy-OR test networks with one binary child ("Y") over many binary parents ("X1"…), each parent
 * with its own prior and inhibitor, plus a leak — and the closed form of the child's marginal,
 * which is what makes them verifiable at scales where enumeration is out of reach.
 */
final class Bn2oNetworks {

    private Bn2oNetworks() {
    }

    /** A noisy-OR (with leak) over {@code numParents} binary parents with distinct parameters. */
    static ProbNet bn2o(int numParents) {
        ProbNet network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable child = new Variable("Y", 2);
        network.addNode(child, NodeType.CHANCE);
        List<Variable> variables = new ArrayList<>(List.of(child));
        for (int i = 1; i <= numParents; i++) {
            Variable parent = new Variable("X" + i, 2);
            network.addNode(parent, NodeType.CHANCE);
            network.addLink(network.getNode(parent), network.getNode(child), true);
            TablePotential prior = new TablePotential(List.of(parent), PotentialRole.CONDITIONAL_PROBABILITY);
            prior.setValues(new double[]{1 - presence(i), presence(i)});
            network.getNode(parent).setPotential(prior);
            variables.add(parent);
        }
        MaxPotential or = new MaxPotential(variables);
        for (int i = 1; i <= numParents; i++) {
            or.setNoisyParameters(variables.get(i),
                                  new double[]{1.0, 0.0, inhibitor(i), 1 - inhibitor(i)});
        }
        or.setLeakyParameters(new double[]{0.95, 0.05});
        network.getNode(child).setPotential(or);
        return network;
    }

    /** P(Y = 0): the leak holds and every parent is absent or inhibited. */
    static double closedFormOfTheNegativeMarginal(int numParents) {
        double pAllInhibited = 0.95;
        for (int i = 1; i <= numParents; i++) {
            pAllInhibited *= (1 - presence(i)) + presence(i) * inhibitor(i);
        }
        return pAllInhibited;
    }

    private static double presence(int parent) {
        return 0.15 + 0.05 * (parent % 10);
    }

    private static double inhibitor(int parent) {
        return 0.10 + 0.08 * (parent % 8);
    }
}
