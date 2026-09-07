/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.operation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Multiplying or adding up a list of one answers that very potential, which the javadoc of both
 * now says. The caller therefore holds the object it passed in, and anything that writes on the
 * result writes on what it passed. Whoever removes the shortcut has to remove those lines too.
 *
 * @author Manuel Arias
 */
public class AListOfOneIsAnsweredWithItselfTest {

    private final Variable a = new Variable("A", 2);

    private TablePotential aPotentialWorth(double... values) {
        return new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY, values);
    }

    @Tag(TestSpeed.FAST)
    @Test public void multiplyingOneAnswersIt() {
        TablePotential only = aPotentialWorth(2.0, 6.0);

        assertSame(only, DiscretePotentialOperations.multiply(List.of(only)), "a list of one");
        assertSame(only, DiscretePotentialOperations.multiply(only), "one argument");
        assertSame(only, DiscretePotentialOperations.multiply(List.of(only), true), "asked to reorder");
    }

    @Tag(TestSpeed.FAST)
    @Test public void addingUpOneAnswersIt() {
        TablePotential only = aPotentialWorth(2.0, 6.0);

        assertSame(only, DiscretePotentialOperations.sum(List.of(only)), "a list of one");
        assertSame(only, DiscretePotentialOperations.sum(only), "one argument");
    }

    @Tag(TestSpeed.FAST)
    @Test public void twoPotentialsAreAnsweredWithSomethingNew() {
        TablePotential one = aPotentialWorth(2.0, 6.0);
        TablePotential other = aPotentialWorth(1.0, 1.0);

        TablePotential result = DiscretePotentialOperations.multiply(List.of(one, other));

        assertArrayEquals(new double[] { 2.0, 6.0 }, one.getValues(), 1e-12, "the first was written on");
        assertArrayEquals(new double[] { 2.0, 6.0 }, result.getValues(), 1e-12, "the product itself");
    }
}
