/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.TemporalNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.AugmentedProbTablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A Markov influence diagram whose transition is a table of formulas with numbers in its cells gives, once expanded
 * and propagated, the same probabilities as the same model with a plain table. The copies of the table in the later
 * cycles were worked out on the variables of the cycle before, and the propagation died with an index out of bounds
 * (error 259 of the pending list).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AFormulaTableInTheCyclesIsPropagatedLikeAPlainTableTest {

    private static final double[] TRANSITION = {0.9, 0.1, 0.2, 0.8};

    @Test void everyCycleHasTheSameProbabilities() throws Exception {
        Map<String, double[]> withFormulas = posteriorsOf(model(AFormulaTableInTheCyclesIsPropagatedLikeAPlainTableTest::formulaTable));
        Map<String, double[]> withATable = posteriorsOf(model(AFormulaTableInTheCyclesIsPropagatedLikeAPlainTableTest::plainTable));

        assertEquals(withATable.keySet(), withFormulas.keySet());
        for (String name : withATable.keySet()) {
            assertArrayEquals(withATable.get(name), withFormulas.get(name), 1e-6, name);
        }
        assertArrayEquals(new double[]{0.6095, 0.3905}, withFormulas.get("State [3]"), 1e-6);
    }

    /** A two-cycle model, State [0] → State [1], with the given transition, expanded to four cycles. */
    private static ProbNet model(Function<List<Variable>, Potential> transition) {
        ProbNet net = new ProbNet(MIDType.getUniqueInstance());
        Variable state0 = state(0);
        Variable state1 = state(1);
        Node node0 = net.addNode(state0, NodeType.CHANCE);
        Node node1 = net.addNode(state1, NodeType.CHANCE);
        net.addLink(state0, state1, true);
        node0.setPotential(new TablePotential(new ArrayList<>(List.of(state0)), PotentialRole.CONDITIONAL_PROBABILITY,
                                              new double[]{0.5, 0.5}));
        node1.setPotential(transition.apply(List.of(state1, state0)));
        net.getInferenceOptions().getTemporalOptions().setHorizon(3);
        return TemporalNetOperations.expandNetwork(net);
    }

    private static Potential plainTable(List<Variable> variables) {
        return new TablePotential(new ArrayList<>(variables), PotentialRole.CONDITIONAL_PROBABILITY, TRANSITION.clone());
    }

    private static Potential formulaTable(List<Variable> variables) {
        AugmentedProbTablePotential table =
                new AugmentedProbTablePotential(new ArrayList<>(variables), PotentialRole.CONDITIONAL_PROBABILITY);
        VariableExpression[] cells = table.getAugmentedProbTable().getFunctionValues();
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new VariableExpression(variables, String.valueOf(TRANSITION[i]));
        }
        return table;
    }

    private static Map<String, double[]> posteriorsOf(ProbNet net) throws Exception {
        VEPropagation propagation = new VEPropagation(net);
        propagation.setVariablesOfInterest(net.getVariables());
        propagation.setPreResolutionEvidence(new EvidenceCase());
        propagation.setPostResolutionEvidence(new EvidenceCase());
        Map<String, double[]> posteriors = new TreeMap<>();
        for (Map.Entry<Variable, TablePotential> entry : propagation.getPosteriorValues().entrySet()) {
            posteriors.put(entry.getKey().getName(), entry.getValue().getValues());
        }
        return posteriors;
    }

    private static Variable state(int cycle) {
        Variable variable = new Variable("State", new State[]{new State("well"), new State("ill")});
        variable.setTimeSlice(cycle);
        return variable;
    }
}
