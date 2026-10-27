/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.database;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.io.database.ByteOrderMark;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.io.database.excel.CSVDataBaseIO;
import org.openmarkov.io.database.weka.ArffDataBaseIO;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A file of Weka or of comma-separated values that starts with a byte order mark, the three bytes EF BB BF that some
 * programs write before the text, is refused with a message that names the mark, as Weka refuses it; the Weka reader
 * used to say it had found «@relation r», which is what it expects, and the reader of comma-separated values took the
 * mark as part of the name of the first column. Once the mark is removed, the file is read as the same file without
 * it (error 250 of the pending list).
 *
 * @author Manuel Arias
 */
class AByteOrderMarkIsRefusedTest {

    private static final byte[] MARK = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

    private static final String WEKA = """
            @relation r
            @attribute X {a,b}
            @data
            a
            b
            """;

    private static final String COMMA_SEPARATED = "X,Y\na,c\nb,d\n";

    private static File file(String name, byte[] bytes) throws IOException {
        Path file = Files.createTempDirectory("byteOrderMark").resolve(name);
        Files.write(file, bytes);
        file.toFile().deleteOnExit();
        return file.toFile();
    }

    private static File withTheMark(String name, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] marked = Arrays.copyOf(MARK, MARK.length + bytes.length);
        System.arraycopy(bytes, 0, marked, MARK.length, bytes.length);
        return file(name, marked);
    }

    private static void assertRefused(CaseDatabaseReader reader, String name, String text) throws IOException {
        File file = withTheMark(name, text);

        ParsingSourceException.StartsWithAByteOrderMark thrown = assertThrows(
                ParsingSourceException.StartsWithAByteOrderMark.class, () -> reader.load(file));

        assertEquals(name, thrown.source, "The message does not name the file");
        assertTrue(thrown.toString().contains("byte order mark"), thrown.toString());
    }

    private static List<String> columns(CaseDatabase database) {
        return database.getVariables().stream()
                       .map(variable -> variable.getName() + Arrays.stream(variable.getStates()).map(State::getName)
                                                                   .toList())
                       .toList();
    }

    /** Removes the mark and checks that the file reads as the same file without it. */
    private static void assertReadWithoutTheMark(CaseDatabaseReader reader, String name, String text)
            throws Exception {
        File marked = withTheMark(name, text);

        ByteOrderMark.removeFrom(marked);

        CaseDatabase expected = reader.load(file(name, text.getBytes(StandardCharsets.UTF_8)));
        CaseDatabase read = reader.load(marked);
        assertEquals(columns(expected), columns(read));
        assertArrayEquals(expected.getCases(), read.getCases());
    }

    @Test
    void aWekaFileWithTheMarkIsRefusedWithAMessageThatNamesIt() throws IOException {
        assertRefused(new ArffDataBaseIO(), "cases.arff", WEKA);
    }

    @Test
    void aCommaSeparatedFileWithTheMarkIsRefusedWithAMessageThatNamesIt() throws IOException {
        assertRefused(new CSVDataBaseIO(), "cases.csv", COMMA_SEPARATED);
    }

    @Test
    void removingTheMarkLeavesTheRestOfTheFileAsItWas() throws IOException {
        File file = withTheMark("cases.arff", WEKA);
        byte[] withoutTheMark = WEKA.getBytes(StandardCharsets.UTF_8);

        ByteOrderMark.removeFrom(file);
        assertArrayEquals(withoutTheMark, Files.readAllBytes(file.toPath()));

        // A file without the mark is left as it is
        ByteOrderMark.removeFrom(file);
        assertArrayEquals(withoutTheMark, Files.readAllBytes(file.toPath()));
    }

    @Test
    void withoutTheMarkAWekaFileIsReadAsTheSameFile() throws Exception {
        assertReadWithoutTheMark(new ArffDataBaseIO(), "cases.arff", WEKA);
    }

    @Test
    void withoutTheMarkACommaSeparatedFileIsReadAsTheSameFile() throws Exception {
        CSVDataBaseIO reader = new CSVDataBaseIO();
        assertReadWithoutTheMark(reader, "cases.csv", COMMA_SEPARATED);
        File marked = withTheMark("cases.csv", COMMA_SEPARATED);
        ByteOrderMark.removeFrom(marked);
        Variable first = reader.load(marked).getVariables().getFirst();
        assertEquals("X", first.getName(), "The mark is still part of the name of the first column");
    }
}
