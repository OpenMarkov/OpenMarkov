/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.TemporalNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * When a Markov influence diagram is expanded, the table of formulas of a node is copied to the next cycle. The copy
 * has to be worked out on the variables of that cycle: the two lists it sorts its variables into, its inner table and
 * the names in its formulas (error 259 of the pending list).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AFormulaTableCopiedToTheNextCycleFollowsItsVariablesTest {

    @Test void theCopyIsWorkedOutOnTheVariablesOfTheNextCycle() throws Exception {
        ProbNet net = new ProbNet(MIDType.getUniqueInstance());
        Variable state0 = state(0);
        Variable state1 = state(1);
        Variable age1 = new Variable("Age");
        age1.setTimeSlice(1);
        net.addNode(state0, NodeType.CHANCE);
        Node node1 = net.addNode(state1, NodeType.CHANCE);
        net.addNode(age1, NodeType.CHANCE);
        net.addLink(state0, state1, true);
        net.addLink(age1, state1, true);
        List<Variable> variables = List.of(state1, state0, age1);
        AugmentedProbTablePotential table =
                new AugmentedProbTablePotential(new ArrayList<>(variables), PotentialRole.CONDITIONAL_PROBABILITY);
        VariableExpression[] cells = table.getAugmentedProbTable().getFunctionValues();
        cells[0] = new VariableExpression(variables, "1-{Age [1]}/100");
        cells[1] = new VariableExpression(variables, "{Age [1]}/100");
        cells[2] = new VariableExpression(variables, "{Age [1]}/100");
        cells[3] = new VariableExpression(variables, "1-{Age [1]}/100");
        node1.setPotential(table);
        net.getInferenceOptions().getTemporalOptions().setHorizon(2);

        ProbNet expanded = TemporalNetOperations.expandNetwork(net);

        AugmentedProbTablePotential next = (AugmentedProbTablePotential) expanded.getNode(expanded.getVariable("State [2]"))
                                                                                  .getPotentials().getFirst();
        assertEquals(List.of("State [2]", "State [1]", "Age [2]"), names(next.getVariables()));
        assertEquals(List.of("State [2]", "State [1]"), names(next.getFiniteStatesVariables()));
        assertEquals(List.of("Age [2]"), names(next.getParameterVariables()));
        assertEquals(List.of("State [2]", "State [1]"), names(next.getAugmentedProbTable().getVariables()));
        assertEquals("{Age [2]}/100", next.getAugmentedProbTable().getFunctionValues()[1].asStringExpression());
        EvidenceCase aPersonOf40 = new EvidenceCase();
        aPersonOf40.addFinding(new Finding(expanded.getVariable("Age [2]"), 40.0));
        TablePotential projected = next.tableProject(aPersonOf40, null, new ArrayList<>());
        assertEquals(List.of("State [2]", "State [1]"), names(projected.getVariables()));
        // The cells are worked out in single precision (AugmentedProbTableInference), hence the tolerance
        assertArrayEquals(new double[]{0.6, 0.4, 0.4, 0.6}, projected.getValues(), 1e-6);
    }

    private static Variable state(int cycle) {
        Variable variable = new Variable("State", new State[]{new State("well"), new State("ill")});
        variable.setTimeSlice(cycle);
        return variable;
    }

    private static List<String> names(List<Variable> variables) {
        return variables.stream().map(Variable::getName).toList();
    }
}
