/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.io.database;

import org.openmarkov.core.exception.ParsingSourceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * The byte order mark of UTF-8: the three bytes EF BB BF, which some programs, such as older versions of Windows
 * Notepad, write before the text of a file. The formats of case databases in text do not allow it, and Weka refuses
 * it, so their readers refuse a file that starts with one; the windows that open those files offer to remove it
 * (error 250 of the pending list).
 *
 * @author Manuel Arias
 */
public final class ByteOrderMark {

    private static final byte[] BYTES = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

    private ByteOrderMark() {
    }

    private static boolean startsWithOne(byte[] bytes) {
        return bytes.length >= BYTES.length && Arrays.equals(bytes, 0, BYTES.length, BYTES, 0, BYTES.length);
    }

    /** @return whether the file starts with the mark */
    public static boolean startsWithOne(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            return startsWithOne(input.readNBytes(BYTES.length));
        }
    }

    /** Refuses a file that starts with the mark, with a message that names it. */
    public static void refuse(File file) throws IOException, ParsingSourceException.StartsWithAByteOrderMark {
        if (startsWithOne(file)) {
            throw new ParsingSourceException.StartsWithAByteOrderMark(file.getName());
        }
    }

    /** Overwrites the file without the mark at its start; the rest of its bytes stay as they are. */
    public static void removeFrom(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        if (startsWithOne(bytes)) {
            Files.write(file.toPath(), Arrays.copyOfRange(bytes, BYTES.length, bytes.length));
        }
    }
}
