/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A canonical model keeps its parameters in a map whose key is the parent, so replacing a parent
 * has to replace the key too. There are two methods that replace a variable, and only one of them
 * was doing it; the other is the one used when a numeric parent is turned into one of finite
 * states, which is a step of the preparation before an inference. A file can carry a canonical
 * model over a numeric parent, and after that step every reading of the parameters failed.
 *
 * @author Manuel Arias
 */
public class DiscretizingAParentKeepsTheCanonicalModelUsableTest {

    private static Variable numericVariable(String name) {
        Variable variable = new Variable(name, true, 0.0, 2.0, true, 1.0);
        variable.setVariableType(VariableType.NUMERIC);
        return variable;
    }

    private static MaxPotential modelOver(Variable child, Variable parent) {
        MaxPotential potential = new MaxPotential(List.of(child, parent));
        double[] parameters = new double[child.getNumStates() * parent.getNumStates()];
        for (int i = 0; i < parameters.length; i += child.getNumStates()) {
            parameters[i] = 1.0;
        }
        potential.setNoisyParameters(parent, parameters);
        return potential;
    }

    @Tag(TestSpeed.FAST)
    @Test public void theParametersOfTheParentAreStillReachable() {
        Variable child = new Variable("C", 2);
        Variable numericParent = numericVariable("N");
        MaxPotential potential = modelOver(child, numericParent);

        potential.replaceNumericVariable(new Variable("N", numericParent.getNumStates()));

        assertDoesNotThrow(potential::getNoisyPotentials, "the parameters of every parent");
        assertDoesNotThrow(potential::getSubpotentials, "the pieces the model breaks into");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theVariableOfTheParentIsRebuiltAroundTheNewOne() {
        Variable child = new Variable("C", 2);
        Variable numericParent = numericVariable("N");
        MaxPotential potential = modelOver(child, numericParent);

        Variable converted = new Variable("N", numericParent.getNumStates());
        potential.replaceNumericVariable(converted);

        assertSame(converted, potential.getVariable(1), "the new variable takes the place of the old one");
        assertEquals(1, potential.getNoisyPotentials().size());
        assertSame(converted, potential.getNoisyPotentials().getFirst().getVariable(1),
                "the parameters of the parent are given out against the new variable");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theWholeConversionOfANetworkLeavesTheModelUsable() throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable numericParent = numericVariable("N");
        Node parentNode = net.addNode(numericParent, NodeType.CHANCE);
        parentNode.setPotential(new TablePotential(List.of(numericParent), PotentialRole.CONDITIONAL_PROBABILITY));

        Variable child = new Variable("C", 2);
        Node childNode = net.addNode(child, NodeType.CHANCE);
        net.addLink(parentNode, childNode, true);
        childNode.setPotential(modelOver(child, numericParent));

        ProbNet converted = ProbNetOperations.convertNumericalVariablesToFS(net);

        MaxPotential model = (MaxPotential) converted.getNode("C").getPotential();
        assertEquals(VariableType.FINITE_STATES, model.getVariable(1).getVariableType());
        assertDoesNotThrow(model::getNoisyPotentials, "the parameters after the whole conversion");
    }
}
