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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Replacing a parent of a canonical model rebuilds its auxiliary variable, which must take the
 * states of the child, not those of the parent. Pasting a canonical model is what replaces its
 * variables, so a wrong auxiliary variable surfaced when pasting a node whose parent and child
 * have a different number of states.
 *
 * @author Manuel Arias
 */
public class ICIReplaceVariableTest {

    @Tag(TestSpeed.FAST)
    @Test public void theRebuiltAuxiliaryVariableTakesTheStatesOfTheChild() {
        Variable child = new Variable("C", 2);
        Variable parent = new Variable("P", 3);
        MaxPotential potential = new MaxPotential(List.of(child, parent));

        Variable newParent = new Variable("Q", 3);
        potential.replaceVariable(parent, newParent);

        Variable auxiliary = potential.getAuxiliaryVariables().iterator().next();
        assertEquals(child.getNumStates(), auxiliary.getNumStates(),
                "the auxiliary variable must have the states of the child");
        assertTrue(auxiliary.getName().equals("z_Q_C"),
                "the auxiliary variable is named after parent then child, and was: " + auxiliary.getName());
    }
}
