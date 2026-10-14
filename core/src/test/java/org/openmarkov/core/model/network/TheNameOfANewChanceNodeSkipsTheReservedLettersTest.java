/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A new chance node is named with the first free letter that is not reserved: D for decisions and U for utilities, and
 * E for events in a network that can have them. With A, B and C, the chance node used to be named «E» even there,
 * because only one letter was skipped (error 170).
 *
 * @author Manuel Arias
 */
class TheNameOfANewChanceNodeSkipsTheReservedLettersTest {

    private static String nextChanceNode(boolean eventsAllowed, String... existingNames) {
        return Util.getNextNodeName(NodeType.CHANCE, new HashSet<>(List.of(existingNames)), eventsAllowed);
    }

    /** The letters from A to T, without those reserved. */
    private static String[] fromAToT(String reservedLetters) {
        return "ABCDEFGHIJKLMNOPQRST".chars().filter(letter -> reservedLetters.indexOf(letter) < 0)
                                     .mapToObj(Character::toString).toArray(String[]::new);
    }

    @Test void afterABAndCTheLetterOfTheEventsIsSkippedWhereThereCanBeEvents() {
        assertEquals("F", nextChanceNode(true, "A", "B", "C"));
    }

    @Test void afterABAndCTheLetterEIsUsedWhereThereCannotBeEvents() {
        assertEquals("E", nextChanceNode(false, "A", "B", "C"));
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void anEmptyNetworkStartsWithA(boolean eventsAllowed) {
        assertEquals("A", nextChanceNode(eventsAllowed));
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void afterTTheLetterOfTheUtilitiesIsSkipped(boolean eventsAllowed) {
        assertEquals("V", nextChanceNode(eventsAllowed, fromAToT(eventsAllowed ? "DE" : "D")));
    }

    @Test void whenEveryFreeLetterIsTakenTheNumberingStarts() {
        String[] everyFreeLetter = "ABCFGHIJKLMNOPQRSTVWXYZ".chars().mapToObj(Character::toString)
                                                            .toArray(String[]::new);

        assertEquals("A1", nextChanceNode(true, everyFreeLetter));
        assertEquals("E", nextChanceNode(false, everyFreeLetter));
    }
}
