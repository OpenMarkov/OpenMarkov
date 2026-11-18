/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.jdom2.Verifier;
import org.jetbrains.annotations.Nullable;

/**
 * The characters that a network file, written in XML 1.0, cannot keep: the control characters other than the tab,
 * the line feed and the carriage return, such as the manual line break of Word, and a few others.
 *
 * @author Manuel Arias
 */
public final class FileCharacters {

    private FileCharacters() {
    }

    /** @return the first character of {@code text} that a network file cannot keep, as U+000B, or {@code null} */
    public static @Nullable String firstThatCannotBeKept(@Nullable String text) {
        if (text == null) {
            return null;
        }
        return text.codePoints().filter(c -> !Verifier.isXMLCharacter(c)).mapToObj(c -> String.format("U+%04X", c))
                   .findFirst().orElse(null);
    }

    /** @return {@code text} without the characters that a network file cannot keep */
    public static String withoutThoseThatCannotBeKept(String text) {
        StringBuilder kept = new StringBuilder();
        text.codePoints().filter(Verifier::isXMLCharacter).forEach(kept::appendCodePoint);
        return kept.toString();
    }
}
