/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A finding saved with the network on a state its variable does not have, without its state or its value, or with a
 * value that is not a number, is rejected with a message that says so (error 402).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ASavedFindingThatCannotBeReadIsRejectedTest {

    private static final String NETWORK = "MID-mammography.pgmx";
    private static final String FINDING = "<EvidenceCase variable=\"Family history\" state=\"no\" />";

    @TempDir Path directory;

    /** A copy of the test network with one of its findings replaced by {@code finding}. */
    private URL copyWith(String finding) throws Exception {
        String content = Files.readString(Path.of(getClass().getClassLoader().getResource(NETWORK).toURI()));
        assertTrue(content.contains(FINDING), NETWORK + " does not contain " + FINDING);
        Path copy = Files.writeString(directory.resolve(NETWORK), content.replaceFirst(Pattern.quote(FINDING), finding));
        return copy.toUri().toURL();
    }

    private static EvidenceCase evidenceOf(URL url) throws Exception {
        ProbNetInfo read = FormatManager.getInstance().getProbNetReader(url).read(url);
        return read.evidence().getFirst();
    }

    @Test void theUnchangedFileIsReadWithItsFindings() throws Exception {
        EvidenceCase evidence = evidenceOf(copyWith(FINDING));

        assertEquals(3, evidence.getFindings().size());
        assertEquals("no", evidence.getFinding(evidence.getVariables().getLast()).getState());
    }

    @Test void aNumericFindingIsRead() throws Exception {
        EvidenceCase evidence = evidenceOf(copyWith("<EvidenceCase variable=\"Age [0]\" numericValue=\"50\" />"));

        assertEquals(50.0, evidence.getFinding(evidence.getVariables().getLast()).getNumericalValue());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "<EvidenceCase variable=\"Family history\" state=\"No\" /> | State No is invalid for variable Family history.",
            "<EvidenceCase variable=\"Family history\" state=\"\" />   | The file cannot be read: EvidenceCase has no state.",
            "<EvidenceCase variable=\"Family history\" />              | The file cannot be read: EvidenceCase has no state.",
            "<EvidenceCase variable=\"Age [0]\" numericValue=\"abc\" /> | The file cannot be read: EvidenceCase has the numericValue abc, which is not valid.",
            "<EvidenceCase variable=\"Age [0]\" />                     | The file cannot be read: EvidenceCase has no numericValue."})
    void aFindingThatCannotBeRead(String finding, String expected) throws Exception {
        URL url = copyWith(finding);

        OpenMarkovException exception = assertThrows(PGMXParserException.class, () -> evidenceOf(url));
        assertEquals(expected, exception.getExceptionMessage());
    }
}
