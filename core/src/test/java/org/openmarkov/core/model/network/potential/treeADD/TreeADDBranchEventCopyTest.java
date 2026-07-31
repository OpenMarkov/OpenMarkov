/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.treeADD;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link TreeADDBranch#copy()} on branches whose root variable is of type EVENT:
 * the copy contemplated that type in one of its three cases only, and answered null ---no branch
 * at all--- in the other two.
 *
 * @author Manuel Arias
 */
class TreeADDBranchEventCopyTest {

    private static Variable eventVariable() {
        Variable variable = new Variable("E", "not happened", "happened");
        variable.setVariableType(VariableType.EVENT);
        return variable;
    }

    @Test
    void aBranchByReferenceOnAnEventVariableSurvivesTheCopy() {
        Variable event = eventVariable();
        TreeADDBranch branch = new TreeADDBranch(List.of(event.getStates()[0]), event, "a label",
                                                 List.of());
        TreeADDBranch copy = branch.copy();
        assertNotNull(copy, "a branch by reference on an EVENT variable must not vanish when copied");
        assertEquals(branch.getReference(), copy.getReference());
    }

    @Test
    void anInterventionBranchOnAnEventVariableSurvivesTheCopy() {
        Variable event = eventVariable();
        TreeADDBranch branch = new TreeADDBranch(List.of(event.getStates()[0]), event,
                                                 (Potential) null, List.of());
        TreeADDBranch copy = branch.copy();
        assertNotNull(copy, "an intervention branch on an EVENT variable must not vanish when copied");
        assertNull(copy.getPotential());
    }
}
