/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.io;

import networks.Networks;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A file with a number that is not written as the reader expects, or without a piece of data, is refused telling the
 * line, the element and the cause, for whatever the reader does not check by itself.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AFileThatCannotBeReadTellsWhereAndWhyTest {

    private static final String NETWORK = "BN-asia.pgmx";

    @TempDir Path folder;

    private static String normalized(String text) {
        return text.replace("\r\n", "\n");
    }

    /** A copy of the test network with the first {@code written} replaced, and the line where it was. */
    private Changed changed(String written, String replacement) throws Exception {
        URL network = Networks.getNetworks().filter(url -> url.getPath().endsWith("/bn/" + NETWORK)).findFirst()
                              .orElseThrow();
        String text = normalized(Files.readString(Path.of(network.toURI())));
        int position = text.indexOf(written);
        assertNotEquals(-1, position, written);
        Path file = folder.resolve(NETWORK);
        Files.writeString(file, text.substring(0, position) + replacement + text.substring(position + written.length()));
        return new Changed(file.toUri().toURL(), text.substring(0, position).split("\n", -1).length);
    }

    private record Changed(URL url, int line) {
    }

    /** The line where the element that begins with {@code start} and holds the given line begins. */
    private int lineOfTheElement(Changed changed, String start) throws Exception {
        List<String> lines = normalized(Files.readString(Path.of(changed.url().toURI()))).lines().toList();
        int line = changed.line();
        while (!lines.get(line - 1).contains(start)) {
            line--;
        }
        return line;
    }

    private static String refusal(Changed changed) {
        return assertThrows(PGMXParserException.PartCannotBeRead.class, () -> new PGMXReader().read(changed.url()))
                .getExceptionMessage();
    }

    @Test void aCoordinateWithDecimals() throws Exception {
        Changed changed = changed("<Coordinates x=\"", "<Coordinates x=\"0.5");
        int line = lineOfTheElement(changed, "<Variable ");
        String name = Files.readAllLines(Path.of(changed.url().toURI())).get(line - 1).replaceAll(".*name=\"([^\"]*)\".*", "$1");

        String message = refusal(changed);

        assertEquals("The file cannot be read, line " + line + ", element Variable of " + name
                     + ": a number is not written as expected. For input string: \"0.5",
                     message.substring(0, message.indexOf("0.5") + 3));
    }

    @Test void aTableWithDecimalCommas() throws Exception {
        Changed changed = changed("<Values>0.95 0.05 0.02 0.98</Values>", "<Values>0,95 0,05 0,02 0,98</Values>");
        int line = lineOfTheElement(changed, "<Potential ");

        assertEquals("The file cannot be read, line " + line + ", element Potential of X-ray: "
                     + "a number is not written as expected. For input string: \"0,95\".", refusal(changed));
    }

    @Test void aLinkWithoutItsDirection() throws Exception {
        Changed changed = changed("<Link directed=\"true\">", "<Link>");

        assertEquals("The file cannot be read, line " + changed.line() + ", element Link: a piece of data is missing.",
                     refusal(changed));
    }
}
