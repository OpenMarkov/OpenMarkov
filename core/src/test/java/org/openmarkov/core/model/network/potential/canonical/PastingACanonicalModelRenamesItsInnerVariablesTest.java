/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A canonical model invents variables of its own -one per parent, one for the leak, and one more in
 * the MAX and MIN families- and names them after the child. Pasting a node replaces the child, so
 * all of them have to be rebuilt; otherwise the pasted model keeps the name of the node it was
 * copied from, and the two nodes end up with variables of the same name inside.
 *
 * @author Manuel Arias
 */
public class PastingACanonicalModelRenamesItsInnerVariablesTest {

    private static MaxPotential modelOver(Variable child, Variable parent) {
        MaxPotential potential = new MaxPotential(List.of(child, parent));
        potential.setNoisyParameters(parent, new double[]{1.0, 0.0, 0.2, 0.8});
        return potential;
    }

    @Tag(TestSpeed.FAST)
    @Test public void theLeakVariableFollowsTheNewChild() {
        MaxPotential potential = modelOver(new Variable("C", 2), new Variable("P", 2));

        potential.replaceVariable(0, new Variable("D", 2));

        assertEquals("D-leaky", potential.getLeakyVariable().getName());
    }

    @Tag(TestSpeed.FAST)
    @Test public void thePseudoVariableFollowsTheNewChild() {
        MaxPotential potential = modelOver(new Variable("C", 2), new Variable("P", 2));

        potential.replaceVariable(0, new Variable("D", 2));

        assertEquals("pseudo-D", potential.getPseudoVariable().getName());
    }

    @Tag(TestSpeed.FAST)
    @Test public void theVariableOfEachParentFollowsTheNewChild() {
        MaxPotential potential = modelOver(new Variable("C", 2), new Variable("P", 2));

        potential.replaceVariable(0, new Variable("D", 2));

        assertEquals("z_P_D", potential.getAuxiliaryVariables().iterator().next().getName());
    }

    @Tag(TestSpeed.FAST)
    @Test public void pastingTheWholeNodeLeavesNoNameOfTheOldOne() {
        Variable child = new Variable("C", 2);
        Variable parent = new Variable("P", 2);
        MaxPotential pasted = (MaxPotential) modelOver(child, parent).copy();

        // What pasting does: the child first, then each parent.
        pasted.replaceVariable(0, new Variable("C'", 2));
        pasted.replaceVariable(1, new Variable("P'", 2));

        assertEquals("C'-leaky", pasted.getLeakyVariable().getName());
        assertEquals("pseudo-C'", pasted.getPseudoVariable().getName());
        assertEquals("z_P'_C'", pasted.getAuxiliaryVariables().iterator().next().getName());
    }

    @Tag(TestSpeed.FAST)
    @Test public void theTableAlreadyComputedIsNotKept() throws Exception {
        Variable child = new Variable("C", 2);
        Variable parent = new Variable("P", 2);
        MaxPotential potential = modelOver(child, parent);

        HashMap<Variable, Integer> question = new HashMap<>();
        question.put(child, 0);
        question.put(parent, 0);
        potential.getProbability(question);

        Variable newChild = new Variable("D", 2);
        potential.replaceVariable(0, newChild);

        HashMap<Variable, Integer> newQuestion = new HashMap<>();
        newQuestion.put(newChild, 0);
        newQuestion.put(parent, 0);
        assertEquals(1.0, assertDoesNotThrow(() -> potential.getProbability(newQuestion)), 1e-12);
    }
}
