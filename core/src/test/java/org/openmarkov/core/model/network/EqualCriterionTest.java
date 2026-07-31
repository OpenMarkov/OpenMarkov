/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link EqualCriterion}: an equality question must always be answered, never
 * break, whatever it is asked about.
 *
 * @author Manuel Arias
 */
class EqualCriterionTest {

    @Test
    void twoCriteriaWithTheSameNameAreEqualAndShareTheNumber() {
        assertEquals(new EqualCriterion("cost"), new EqualCriterion("cost"));
        assertEquals(new EqualCriterion("cost").hashCode(), new EqualCriterion("cost").hashCode());
    }

    @Test
    void twoCriteriaWithDifferentNamesAreNotEqual() {
        assertNotEquals(new EqualCriterion("cost"), new EqualCriterion("effectiveness"));
    }

    @Test
    void comparedAgainstNothingItAnswersNoInsteadOfBreaking() {
        assertFalse(new EqualCriterion("cost").equals(null));
    }

    @Test
    void comparedAgainstAnotherKindOfObjectItAnswersNoInsteadOfBreaking() {
        assertFalse(new EqualCriterion("cost").equals("cost"));
    }

    /**
     * A plain {@link Criterion} compares by identity, so it never claims to equal an
     * {@code EqualCriterion}; the answer in the other direction must agree.
     */
    @Test
    void aPlainCriterionAndAnEqualCriterionDisagreeInNeitherDirection() {
        Criterion plain = new Criterion("cost");
        EqualCriterion comparable = new EqualCriterion("cost");
        assertEquals(plain.equals(comparable), comparable.equals(plain));
    }

    /**
     * A criterion cannot be built without a name, but it can be left without one afterwards,
     * through the name setter. Comparing must not break then either.
     */
    @Test
    void aCriterionWhoseNameIsMissingCanBeComparedWithoutBreaking() {
        EqualCriterion nameless = new EqualCriterion("cost");
        nameless.setCriterionName(null);
        assertNotEquals(nameless, new EqualCriterion("cost"));
        assertNotEquals(new EqualCriterion("cost"), nameless);
    }

    /**
     * The way the discrete-event simulation module uses it: a table from criterion to results,
     * looked up with an object equal to the key but not the very same one.
     */
    @Test
    void aTableFindsItsResultsUnderAnEqualCriterion() {
        HashMap<EqualCriterion, String> results = new HashMap<>();
        results.put(new EqualCriterion("cost"), "some results");
        assertTrue(results.containsKey(new EqualCriterion("cost")));
        assertEquals("some results", results.get(new EqualCriterion("cost")));
        assertFalse(results.containsKey(new EqualCriterion("effectiveness")));
    }
}
