/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.java.regexUtils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link SplitByRegex#splitAll}, which cuts a text into the stretches that match a
 * pattern and the stretches between them. The class had none.
 *
 * @author Manuel Arias
 */
public class SplitByRegexTest {

    private static final Pattern REFERENCE = Pattern.compile("\\{([^\\{]+?)\\}");

    @Test
    public void matchesAndTheTextBetweenThemComeOutInOrder() {
        assertEquals(List.of("{a}", " + ", "{b}"),
                     SplitByRegex.splitAll(REFERENCE, "{a} + {b}"));
    }

    @Test
    public void aTextWithNoMatchComesOutWhole() {
        assertEquals(List.of("1 + 2"), SplitByRegex.splitAll(REFERENCE, "1 + 2"));
    }

    @Test
    public void aTextThatIsAllMatchComesOutAlone() {
        assertEquals(List.of("{a}"), SplitByRegex.splitAll(REFERENCE, "{a}"));
    }

    /**
     * Two adjacent matches leave a zero-width gap between them; the split used to hand that gap
     * out as an empty string, a piece that says nothing, and its consumer turned it into a
     * spurious empty fragment of the expression.
     */
    @Test
    public void twoAdjacentMatchesLeaveNoEmptyPieceBetweenThem() {
        assertEquals(List.of("{a}", "{b}"), SplitByRegex.splitAll(REFERENCE, "{a}{b}"));
    }

    /** The pieces, joined back, are the text: nothing is lost and nothing is invented. */
    @Test
    public void joiningThePiecesGivesTheTextBack() {
        String input = "{a}{b} + 2*{c}";
        assertEquals(input, String.join("", SplitByRegex.splitAll(REFERENCE, input)));
    }
}
