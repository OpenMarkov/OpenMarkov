/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.elvira;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A file of Elvira that ends inside a list, as one cut short does, is refused saying which sign is missing and where
 * the list starts. The reader read each list up to the sign that closes it, without looking at the end of the file,
 * and there it read nothing again and again: four of the lists filled the memory, the numbers of a table ended in a
 * NullPointerException, and what goes between brackets in a table never ended (error 447 of the pending list). The
 * time limit makes the test fail in seconds, not hang, if that comes back.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnElviraFileCutShortIsRefusedTest {

    private static final Charset ELVIRA = Charset.forName("windows-1252");

    @TempDir Path folder;

    @ParameterizedTest(name = "{0} cut after «{1}»")
    @CsvSource(delimiter = '|', ignoreLeadingAndTrailingWhitespace = false, value = {
            "asia.elv|default node states = (yes,|)|14",
            "asia.elv|states = (yes |)|62",
            "asia.elv|relation T A |{|125",
            "asia.elv|[yes,yes]=0.05,|)|127",
            "asia.elv|[yes|]|112",
            "puerta-or.elv|values= table (0.1 |)|60",
            "puerta-or.elv|Or(CA,CB,|)|74"})
    void aFileCutInsideAListIsRefusedSayingWhereTheListStarts(String network, String cutAfter, String closing,
                                                              int startLine) throws Exception {
        String whole = new String(getClass().getClassLoader().getResourceAsStream(network).readAllBytes(), ELVIRA);
        int cut = whole.indexOf(cutAfter);
        assertTrue(cut >= 0, "«" + cutAfter + "» is not in " + network);
        File cutShort = folder.resolve("cut-" + network).toFile();
        Files.writeString(cutShort.toPath(), whole.substring(0, cut + cutAfter.length()), ELVIRA);

        ProbNetParserException.ListNotClosed refusal = assertTimeoutPreemptively(Duration.ofSeconds(10), () ->
                assertThrows(ProbNetParserException.ListNotClosed.class,
                             () -> new ElviraParser().read(cutShort.toURI().toURL())));

        assertEquals(closing, refusal.closing);
        assertEquals(startLine, refusal.startLine);
        assertTrue(refusal.getExceptionMessage().startsWith("The file ends before the «" + closing
                                                            + "» that closes the list that starts at line "
                                                            + startLine + "."), refusal.getExceptionMessage());
    }

    @Test void theWholeFilesAreStillRead() {
        assertEquals(8, assertDoesNotThrow(() -> new ElviraParser().read(
                getClass().getClassLoader().getResource("asia.elv")).probNet()).getNumNodes());
        assertDoesNotThrow(() -> new ElviraParser().read(getClass().getClassLoader().getResource("puerta-or.elv")));
    }
}
