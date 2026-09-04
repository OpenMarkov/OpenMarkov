/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Handing a canonical model the parameters that parameter learning obtained must leave it
 * answering with those parameters, and must not tie it to the tables the caller keeps.
 *
 * @author Manuel Arias
 */
public class LearnedNoisyParametersAreTheOnesAnsweredTest {

    private static final double PRECISION = 1e-12;

    private final Variable child = new Variable("C", 2);
    private final Variable parent = new Variable("P", 2);

    private MaxPotential aModelAskedOnceForItsProbability() throws Exception {
        MaxPotential potential = new MaxPotential(List.of(child, parent));
        potential.setNoisyParameters(parent, new double[] { 1.0, 0.0, 0.1, 0.9 });
        assertEquals(0.9, potential.getProbability(theCaseOfBothSecondStates()), PRECISION,
                "the model must start out answering with its own parameters");
        return potential;
    }

    private HashMap<Variable, Integer> theCaseOfBothSecondStates() {
        HashMap<Variable, Integer> evidence = new HashMap<>();
        evidence.put(child, 1);
        evidence.put(parent, 1);
        return evidence;
    }

    @Tag(TestSpeed.FAST)
    @Test public void theNewParametersReachTheProbability() throws Exception {
        MaxPotential potential = aModelAskedOnceForItsProbability();

        TablePotential learned = potential.getNoisyPotentials().getFirst();
        learned.setValues(new double[] { 1.0, 0.0, 0.8, 0.2 });
        potential.setNoisyPotentials(List.of(learned));

        assertEquals(0.2, potential.getProbability(theCaseOfBothSecondStates()), PRECISION,
                "the probability must come from the parameters just assigned, not from the table "
                        + "computed before them");
    }

    @Tag(TestSpeed.FAST)
    @Test public void theModelDoesNotShareTheArrayOfTheCaller() {
        MaxPotential potential = new MaxPotential(List.of(child, parent));
        TablePotential learned = new TablePotential(List.of(new Variable("z", 2), parent),
                PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 1.0, 0.0, 0.5, 0.5 });

        potential.setNoisyPotentials(List.of(learned));
        learned.getValues()[0] = 99.0;

        double[] parameters = potential.getNoisyParameters(parent);
        assertArrayEquals(new double[] { 1.0, 0.0, 0.5, 0.5 }, parameters, PRECISION,
                "writing on the caller's table must not reach the model, and it did: "
                        + Arrays.toString(parameters));
    }

    @Tag(TestSpeed.FAST)
    @Test public void aRowOfTheWrongLengthIsRefused() {
        MaxPotential potential = new MaxPotential(List.of(child, parent));
        TablePotential tooLong = new TablePotential(List.of(new Variable("z", 3), parent),
                PotentialRole.CONDITIONAL_PROBABILITY,
                new double[] { 1.0, 0.0, 0.0, 0.4, 0.3, 0.3 });

        assertThrows(UnrecoverableException.class, () -> potential.setNoisyPotentials(List.of(tooLong)),
                "a row that does not measure the child's states by the parent's must be refused");
    }

    @Tag(TestSpeed.FAST)
    @Test public void aPotentialOverSomethingElseIsRefused() {
        MaxPotential potential = new MaxPotential(List.of(child, parent));
        Variable stranger = new Variable("S", 2);
        TablePotential alien = new TablePotential(List.of(new Variable("z", 2), stranger),
                PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 1.0, 0.0, 0.5, 0.5 });

        assertThrows(UnrecoverableException.class, () -> potential.setNoisyPotentials(List.of(alien)),
                "a potential over a variable that is not a parent must be refused");
    }
}
