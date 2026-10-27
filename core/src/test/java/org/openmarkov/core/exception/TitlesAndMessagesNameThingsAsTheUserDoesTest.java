/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.exception;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The title of the window of an error is made of the name of its exception, and a series of capitals stays together:
 * letter by letter, they gave «Invalid limit in tree a d d» and «X m l invalid» (error 182 of the pending list). And
 * the message of two incompatible findings names the variable and the two states: it said «The new evidence
 * Disease:present(1) is not compatible with an old evidence…» (error 186).
 *
 * @author Manuel Arias
 */
class TitlesAndMessagesNameThingsAsTheUserDoesTest {

    private static final class XMLInvalidException {
    }

    private static final class TreeADDWithoutPotentialOrReference {
    }

    private static final class OnlyDESNetsAllowedException {
    }

    private static final class WrongVersion {
    }

    @Test void aSeriesOfCapitalsStaysTogether() {
        assertEquals("XML invalid", IBundledOpenMarkovException.titlifyExceptionName(new XMLInvalidException()));
        assertEquals("Tree ADD without potential or reference",
                     IBundledOpenMarkovException.titlifyExceptionName(new TreeADDWithoutPotentialOrReference()));
        assertEquals("Only DES nets allowed",
                     IBundledOpenMarkovException.titlifyExceptionName(new OnlyDESNetsAllowedException()));
        assertEquals("Wrong version", IBundledOpenMarkovException.titlifyExceptionName(new WrongVersion()));
    }

    @Test void twoIncompatibleFindingsAreNamedByTheirStates() {
        Variable disease = new Variable("Disease", "absent", "present");

        String message = new IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther(
                new Finding(disease, 1), new Finding(disease, 0)).toString();

        assertEquals("The finding Disease = present contradicts the finding Disease = absent, already in the evidence.",
                     message);
    }
}
