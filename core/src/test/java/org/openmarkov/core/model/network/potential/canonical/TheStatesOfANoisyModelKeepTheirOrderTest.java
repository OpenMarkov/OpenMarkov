/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.NodeStateEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NotSupportedOperationException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The function that combines the influences of a noisy model is a maximum or a minimum over the
 * indices of the states of the conditioned variable, so putting those states in another order
 * would change the distribution instead of renaming it. The potential refuses, and the operation
 * that reorders the states of a node asks before it changes anything.
 *
 * <p>The states of a parent are another matter: they index the row of parameters and never reach
 * the combination function, so reordering them is a genuine renaming and goes on working.
 *
 * @author Manuel Arias
 */
class TheStatesOfANoisyModelKeepTheirOrderTest {

    private final Variable child = new Variable("Y", "y0", "y1");
    private final Variable parent = new Variable("X", "x0", "x1");

    private MaxPotential noisyMax() {
        MaxPotential max = new MaxPotential(List.of(child, parent));
        max.setNoisyParameters(parent, new double[] { 1.0, 0.0, 0.2, 0.8 });
        max.setLeakyParameters(new double[] { 1.0, 0.0 });
        return max;
    }

    private static State[] reversed(Variable variable) {
        State[] states = variable.getStates();
        return new State[] { states[1], states[0] };
    }

    @Tag(TestSpeed.FAST)
    @Test void theConditionedVariableRefuses() {
        MaxPotential max = noisyMax();
        assertThrows(NotSupportedOperationException.class, () -> max.reorder(child, reversed(child)));
    }

    @Tag(TestSpeed.FAST)
    @Test void theStatesOfAParentAreStillReordered() throws Exception {
        MaxPotential max = noisyMax();
        ICIPotential reordered = (ICIPotential) max.reorder(parent, reversed(parent));

        // The parameters of the parent come back with its two columns swapped.
        assertArrayEquals(new double[] { 0.2, 0.8, 1.0, 0.0 }, reordered.getNoisyParameters(parent));
        // And that is a renaming: the table is the old one with its columns swapped.
        assertArrayEquals(new double[] { 0.2, 0.8, 1.0, 0.0 }, reordered.getCPT().getValues());
    }

    @Tag(TestSpeed.FAST)
    @Test void aRefusedReorderLeavesTheNetworkAsItWas() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.addNode(child, NodeType.CHANCE);
        net.addNode(parent, NodeType.CHANCE);
        net.addLink(parent, child, true);
        net.getNode(child).setPotential(noisyMax());
        State[] statesBefore = child.getStates();

        // The edit turns the refusal into the exception the window knows how to show, and it
        // carries the reason the potential gave.
        NodeStateEdit edit = new NodeStateEdit(net.getNode(child), StateAction.UP, 1, "");
        DoEditException refused = assertThrows(DoEditException.class, edit::executeEdit);
        assertTrue(refused.getExceptionMessage().contains("depends on the order of its states"),
                   "the message must say why: " + refused.getExceptionMessage());

        assertArrayEquals(statesBefore, child.getStates(), "the states must not have moved");
        assertEquals(MaxPotential.class, net.getNode(child).getPotentials().getFirst().getClass(),
                     "the potential must be the one it was");
        assertEquals("[1.0, 0.0, 0.2, 0.8]",
                     Arrays.toString(((ICIPotential) net.getNode(child).getPotentials().getFirst())
                                             .getNoisyParameters(parent)),
                     "the parameters must not have moved either");
    }
}
