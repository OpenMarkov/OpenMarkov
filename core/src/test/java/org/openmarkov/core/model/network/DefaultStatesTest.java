/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the catalogue of ready-made sets of states, {@link StandardDomain}, and for the
 * access by position to it that {@link DefaultStates} gives the menus.
 *
 * @author Manuel Arias
 */
class DefaultStatesTest {

    /** The catalogue, in the order the user sees it. */
    private static final List<List<String>> EXPECTED = List.of(
            List.of("absent", "present"),
            List.of("no", "yes"),
            List.of("negative", "positive"),
            List.of("absent", "mild", "moderate", "severe"),
            List.of("low", "medium", "high"),
            List.of("not happened", "happened"));

    private static State[] statesNamed(List<String> names) {
        return names.stream().map(State::new).toArray(State[]::new);
    }

    // -----------------------------------------------------------------------
    // The catalogue itself
    // -----------------------------------------------------------------------

    @Test
    void theCatalogueHoldsEveryDomainInOrder() {
        assertEquals(EXPECTED, Arrays.stream(StandardDomain.values()).map(StandardDomain::getStateNames).toList());
    }

    @Test
    void accessByPositionSeesTheSameCatalogue() {
        for (int index = 0; index < EXPECTED.size(); index++) {
            assertEquals(EXPECTED.get(index), List.of(DefaultStates.getByIndex(index)));
        }
    }

    @Test
    void thereIsNoDomainBeyondTheCatalogue() {
        assertThrows(IndexOutOfBoundsException.class, () -> DefaultStates.getByIndex(EXPECTED.size()));
        assertThrows(IndexOutOfBoundsException.class, () -> DefaultStates.getByIndex(-1));
    }

    /**
     * The catalogue is shared by everyone who asks for it, so no caller may alter it.
     */
    @Test
    void aDomainDoesNotHandOutAListThatCanBeAltered() {
        assertThrows(UnsupportedOperationException.class, () -> StandardDomain.LOW_MEDIUM_HIGH
                .getStateNames().add("very high"));
    }

    /**
     * The states, unlike their names, are handed out as new objects: whoever receives them gives
     * them to a variable and may rename them.
     */
    @Test
    void theStatesAreHandedOutAsFreshCopies() {
        State[] first = StandardDomain.LOW_MEDIUM_HIGH.getStates();
        State[] second = StandardDomain.LOW_MEDIUM_HIGH.getStates();
        assertNotSame(first[0], second[0]);
        first[0].setName("very low");
        assertEquals("low", StandardDomain.LOW_MEDIUM_HIGH.getStates()[0].getName());
    }

    // -----------------------------------------------------------------------
    // Looking a set of states up in the catalogue
    // -----------------------------------------------------------------------

    @Test
    void everyDomainIsFoundBackFromItsOwnStates() {
        for (StandardDomain domain : StandardDomain.values()) {
            assertEquals(domain, StandardDomain.withStates(domain.getStates()).orElseThrow());
            assertEquals(domain.ordinal(), DefaultStates.getIndex(domain.getStates()),
                         () -> "the catalogue does not find its own entry " + domain.getStateNames());
        }
    }

    @Test
    void anUnknownSetOfStatesIsNotInTheCatalogue() {
        assertTrue(StandardDomain.withStates(statesNamed(List.of("green", "amber", "red"))).isEmpty());
    }

    /**
     * Documented behaviour, unchanged: a set that is not in the catalogue answers with the last
     * index. Pinned here so that adding a domain does not silently move the fallback.
     */
    @Test
    void anUnknownSetOfStatesFallsBackToTheLastDomain() {
        assertEquals(EXPECTED.size() - 1, DefaultStates.getIndex(statesNamed(List.of("green", "amber", "red"))));
    }

    /**
     * The states must be in the same order to be the same domain.
     */
    @Test
    void theOrderOfTheStatesMatters() {
        assertTrue(StandardDomain.withStates(statesNamed(List.of("present", "absent"))).isEmpty());
    }

    // -----------------------------------------------------------------------
    // States given to a node according to its type
    // -----------------------------------------------------------------------

    @Test
    void aDecisionNodeGetsTheNoYesDomain() {
        State[] states = DefaultStates.getStatesNodeType(NodeType.DECISION, null);
        assertEquals(StandardDomain.NO_YES.getStateNames(),
                     Arrays.stream(states).map(State::getName).toList());
    }

    @Test
    void aChanceNodeGetsTheDefaultStatesOfItsNetwork() {
        State[] networkDefaultStates = statesNamed(List.of("low", "medium", "high"));
        assertEquals(List.of("low", "medium", "high"),
                     Arrays.stream(DefaultStates.getStatesNodeType(NodeType.CHANCE, networkDefaultStates))
                           .map(State::getName).toList());
    }
}
