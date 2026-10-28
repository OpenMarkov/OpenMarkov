/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A network that breaks the constraints of its type, as one read from a file may, opens so that it can be corrected,
 * but no algorithm evaluates it until then, and the refusal says why (error 37 of the pending list).
 *
 * @author Manuel Arias
 */
class ANetworkThatBreaksItsConstraintsIsNotEvaluatedTest {

    @Test void aBayesianNetworkWithACycleIsRefused() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable a = new Variable("A", "no", "yes");
        Variable b = new Variable("B", "no", "yes");
        net.addNode(a, NodeType.CHANCE);
        net.addNode(b, NodeType.CHANCE);
        net.addLink(a, b, true);
        net.addLink(b, a, true);
        for (Node node : net.getNodes()) {
            List<Variable> variables = new ArrayList<>();
            variables.add(node.getVariable());
            node.getParents().forEach(parent -> variables.add(parent.getVariable()));
            node.setPotential(new TablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY));
        }

        ConstraintViolatedException refusal = assertTimeoutPreemptively(
                Duration.ofSeconds(20), () -> assertThrows(ConstraintViolatedException.class, () -> new VEPropagation(net)));

        assertTrue(refusal.toString().contains("The link A → B closes a cycle."), refusal.toString());
    }
}
