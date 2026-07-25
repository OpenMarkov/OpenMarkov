/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The leak variable of a canonical model must be told apart from the variable it is the leak of.
 * <p>
 * It used to be built as {@code new Variable(child.getName() + "-leaky", ...)}. That reads fine and
 * is wrong for temporal variables: Variable takes a trailing " [n]" as a time slice and rebuilds its
 * name out of base name and slice, so "Y [0]-leaky" came back as plain "Y [0]" - the "-leaky" was
 * swallowed by the parse. Every canonical model over a temporal node had a leak named exactly like
 * its own conditioned variable.
 *
 * @author Manuel Arias
 */
public class ICILeakVariableNameTest {

    @Test public void theLeakOfAPlainVariableIsMarkedAsSuch() {
        Variable child = new Variable("Y", 2);

        Variable leak = maxOver(child).getLeakyVariable();

        assertEquals("Y-leaky", leak.getName());
    }

    /** The one that was broken: the mark used to disappear into the brackets. */
    @Test public void theLeakOfATemporalVariableIsNotNamedLikeTheVariableItself() {
        Variable child = new Variable("Y [0]", 2);
        assertEquals(0, child.getTimeSlice(), "the fixture is meant to be a temporal variable");

        Variable leak = maxOver(child).getLeakyVariable();

        assertNotEquals(child.getName(), leak.getName(), "the leak took the name of its own child");
        assertEquals("Y-leaky", leak.getBaseName());
        assertEquals(0, leak.getTimeSlice(), "the leak lost the time slice of its child");
    }

    /** A copy builds its own leak, so it can lose the mark in the same way. */
    @Test public void theLeakOfACopiedModelKeepsItsName() {
        Variable child = new Variable("Y [0]", 2);
        MaxPotential potential = maxOver(child);

        Variable leak = new MaxPotential(potential).getLeakyVariable();

        assertNotEquals(child.getName(), leak.getName());
        assertEquals(potential.getLeakyVariable().getName(), leak.getName());
    }

    /** And so can a deep copy, which builds it from the conditioned variable of the destination. */
    @Test public void theLeakOfADeepCopiedModelKeepsItsName() {
        Variable child = new Variable("Y [0]", 2);
        Variable parent = new Variable("P [0]", 2);
        ProbNet network = new ProbNet();
        network.addNode(child, NodeType.CHANCE);
        network.addNode(parent, NodeType.CHANCE);
        network.getNode(child).setPotential(new MaxPotential(new ArrayList<>(List.of(child, parent))));

        ProbNet copy = network.deepCopy();
        ICIPotential copiedPotential = (ICIPotential) copy.getNode("Y [0]").getPotentials().getFirst();

        assertNotEquals("Y [0]", copiedPotential.getLeakyVariable().getName());
        assertEquals("Y-leaky", copiedPotential.getLeakyVariable().getBaseName());
    }

    private static MaxPotential maxOver(Variable child) {
        return new MaxPotential(new ArrayList<>(List.of(child, new Variable("P", 2))));
    }
}
