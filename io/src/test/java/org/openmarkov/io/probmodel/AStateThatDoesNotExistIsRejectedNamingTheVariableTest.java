/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.io.format.annotation.FormatManager;
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
 * A state its variable does not have, in a «Delta» or in a branch of a tree, is rejected with a message that names
 * the variable by its name, not with its whole description (error 430).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AStateThatDoesNotExistIsRejectedNamingTheVariableTest {

    private static final String NETWORK = "MID-mammography.pgmx";

    @TempDir Path directory;

    @ParameterizedTest(name = "{2}")
    @CsvSource(delimiter = '|', value = {
            "<State>healthy</State> | <State>maybe</State> | State [0]",
            "(<TopVariable name=\"Death \\(OC\\)\" timeSlice=\"1\" />\\s*<Branches>\\s*<Branch>\\s*<States>\\s*)<State name=\"yes\" />"
            + " | $1<State name=\"maybe\" /> | Death (OC) [1]"})
    void theMessageNamesTheVariable(String regex, String replacement, String variable) throws Exception {
        String text = Files.readString(Path.of(getClass().getClassLoader().getResource(NETWORK).toURI()));
        assertTrue(Pattern.compile(regex).matcher(text).find(), NETWORK + " does not contain " + regex);
        URL url = Files.writeString(directory.resolve(NETWORK), text.replaceFirst(regex, replacement)).toUri().toURL();

        OpenMarkovException exception = assertThrows(PGMXParserException.FoundUnknownState.class,
                () -> FormatManager.getInstance().getProbNetReader(url).read(url));
        assertEquals("Variable " + variable + " does not have state maybe.", exception.getExceptionMessage());
    }
}
