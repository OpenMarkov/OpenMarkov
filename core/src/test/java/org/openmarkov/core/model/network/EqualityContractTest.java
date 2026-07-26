/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The equality contract for the model classes outside the potential hierarchy that used to define
 * equals without hashCode: {@link State} and {@link PartitionedInterval}.
 * <p>
 * Unlike the potentials, where a value hash had to be kept away from collections that meant identity,
 * these two are the opposite case. Every collection of states in the code means value - the states of
 * one variable, told apart by name - and one of them was quietly broken by the identity hash it got
 * from Object: {@code TreeADDPotential.reorder} looks its branches up in a {@code Map<State,...>} with
 * the State objects the caller passed in, and dropped any branch whose states were equal but not the
 * same objects.
 *
 * @author Manuel Arias
 */
public class EqualityContractTest {

    // ------------------------------------------------------------------ State

    @Test public void twoStatesWithTheSameNameAreEqualAndHashAlike() {
        State one = new State("yes");
        State other = new State("yes");

        assertEquals(one, other);
        assertEquals(one.hashCode(), other.hashCode(), "equal states must hash alike");
    }

    @Test public void statesWithDifferentNamesAreNotEqual() {
        assertNotEquals(new State("yes"), new State("no"));
    }

    @Test public void aStateIsNotEqualToNullNorToSomethingElse() {
        State state = new State("yes");

        assertNotEquals(null, state);
        assertNotEquals("yes", state);
    }

    /**
     * The point of the whole fix: a set of states behaves as a set of states, not as a set of objects
     * that happen to be states. Before the hashCode this kept both.
     */
    @Test public void aSetOfStatesTellsThemApartByName() {
        Set<State> states = new HashSet<>();

        states.add(new State("yes"));
        states.add(new State("yes"));
        states.add(new State("no"));

        assertEquals(2, states.size());
        assertTrue(states.contains(new State("no")), "a set of states must find a state by its name");
    }

    /** How TreeADDPotential.reorder uses it: look a branch up with a state that is not the same object. */
    @Test public void aStateFoundInAMapNeedNotBeTheSameObject() {
        Map<State, String> byState = new HashMap<>();
        byState.put(new State("positive"), "the branch for positive");

        assertEquals("the branch for positive", byState.get(new State("positive")));
    }

    // ------------------------------------------------- PartitionedInterval

    @Test public void twoEqualPartitionedIntervalsHashAlike() {
        PartitionedInterval one = new PartitionedInterval(new double[]{0.0, 1.0, 2.0},
                                                          new boolean[]{false, true, true});
        PartitionedInterval other = new PartitionedInterval(new double[]{0.0, 1.0, 2.0},
                                                            new boolean[]{false, true, true});

        assertEquals(one, other);
        assertEquals(one.hashCode(), other.hashCode(), "equal intervals must hash alike");
    }

    @Test public void intervalsThatDifferInALimitAreNotEqual() {
        PartitionedInterval one = new PartitionedInterval(new double[]{0.0, 1.0},
                                                          new boolean[]{false, true});
        PartitionedInterval other = new PartitionedInterval(new double[]{0.0, 2.0},
                                                            new boolean[]{false, true});

        assertNotEquals(one, other);
    }

    /**
     * The trap in hashing doubles: equals compares the limits with {@code ==}, and {@code ==} says
     * -0.0 equals 0.0 while {@code Double.hashCode} gives them different numbers. Two intervals that
     * equals calls equal must hash alike anyway.
     */
    @Test public void minusZeroAndZeroAreEqualLimitsAndMustHashAlike() {
        PartitionedInterval withMinusZero = new PartitionedInterval(new double[]{-0.0, 1.0},
                                                                    new boolean[]{false, true});
        PartitionedInterval withZero = new PartitionedInterval(new double[]{0.0, 1.0},
                                                               new boolean[]{false, true});

        assertEquals(withMinusZero, withZero, "the fixture is meant to be an equal pair");
        assertEquals(withMinusZero.hashCode(), withZero.hashCode());
    }

    @Test public void aPartitionedIntervalIsNotEqualToNull() {
        assertNotEquals(null, new PartitionedInterval(new double[]{0.0, 1.0}, new boolean[]{false, true}));
    }
}
