/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.io;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.io.database.plugin.CaseDatabaseManager;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.State;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The windows that open a case database ask what to do with a file of Weka that starts with a byte order mark, which
 * the reader refuses: if the user agrees, the file is saved without the mark and read; if not, it is left as it was
 * and nothing is read (error 250 of the pending list). The answer of the user is simulated.
 *
 * @author Manuel Arias
 */
class AFileWithAByteOrderMarkIsOfferedToBeSavedWithoutItTest {

    private static final byte[] TEXT = """
            @relation r
            @attribute X {a,b}
            @data
            a
            b
            """.getBytes(StandardCharsets.UTF_8);

    private static final byte[] MARKED_TEXT = new byte[3 + TEXT.length];

    static {
        MARKED_TEXT[0] = (byte) 0xEF;
        MARKED_TEXT[1] = (byte) 0xBB;
        MARKED_TEXT[2] = (byte) 0xBF;
        System.arraycopy(TEXT, 0, MARKED_TEXT, 3, TEXT.length);
    }

    private static File file(byte[] bytes) throws Exception {
        Path file = Files.createTempDirectory("byteOrderMark").resolve("cases.arff");
        Files.write(file, bytes);
        file.toFile().deleteOnExit();
        return file.toFile();
    }

    private static CaseDatabaseReader reader() throws Exception {
        return new CaseDatabaseManager().getReader("arff");
    }

    @Test
    void ifTheUserAgreesTheFileIsSavedWithoutTheMarkAndRead() throws Exception {
        File file = file(MARKED_TEXT);

        CaseDatabase database = CaseDatabaseLoading.load(reader(), file, asked -> true);

        assertNotNull(database);
        assertEquals("X", database.getVariables().getFirst().getName());
        assertEquals(List.of("a", "b"),
                     Arrays.stream(database.getVariables().getFirst().getStates()).map(State::getName).toList());
        assertArrayEquals(new int[][]{ { 0 }, { 1 } }, database.getCases());
        assertArrayEquals(TEXT, Files.readAllBytes(file.toPath()), "The file is not the same without the mark");
    }

    @Test
    void ifTheUserDoesNotAgreeTheFileIsLeftAsItWasAndNothingIsRead() throws Exception {
        File file = file(MARKED_TEXT);

        assertNull(CaseDatabaseLoading.load(reader(), file, asked -> false));
        assertArrayEquals(MARKED_TEXT, Files.readAllBytes(file.toPath()), "The file was changed");
    }

    @Test
    void aFileWithoutTheMarkIsReadWithoutAsking() throws Exception {
        CaseDatabase database = CaseDatabaseLoading.load(reader(), file(TEXT), asked -> fail("The user was asked"));

        assertNotNull(database);
        assertEquals(2, database.getCases().length);
    }
}
