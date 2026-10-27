/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.io;

import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.EmptyDatabaseException;
import org.openmarkov.core.exception.ParsingSourceException;
import org.openmarkov.core.io.database.ByteOrderMark;
import org.openmarkov.core.io.database.CaseDatabaseReader;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.database.CaseDatabase;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

/**
 * Opens case databases from the windows of OpenMarkov. The readers of text refuse a file that starts with a byte
 * order mark; here the user is offered to save the file without it, overwriting it, and then it is read (error 250
 * of the pending list).
 *
 * @author Manuel Arias
 */
public final class CaseDatabaseLoading {

    private CaseDatabaseLoading() {
    }

    /**
     * @return the database of the file, or null if it starts with a byte order mark and the user does not want it
     * removed
     */
    public static @Nullable CaseDatabase load(Component parent, CaseDatabaseReader reader, File file)
            throws IOException, ParsingSourceException, EmptyDatabaseException {
        return load(reader, file, markedFile -> userAgreesToRemoveTheMark(parent, markedFile));
    }

    /** @param removeTheMark whether the user agrees to save the file without its byte order mark */
    static @Nullable CaseDatabase load(CaseDatabaseReader reader, File file, Predicate<File> removeTheMark)
            throws IOException, ParsingSourceException, EmptyDatabaseException {
        try {
            return reader.load(file);
        } catch (ParsingSourceException.StartsWithAByteOrderMark mark) {
            if (!removeTheMark.test(file)) {
                return null;
            }
            ByteOrderMark.removeFrom(file);
            return reader.load(file);
        }
    }

    /**
     * For the windows that read the file off the event-dispatch thread: asks the user and, if they agree, saves the
     * file without its byte order mark.
     *
     * @return whether the mark was removed, so the file can be read again
     */
    public static boolean removeTheMarkIfTheUserAgrees(Component parent, File file) throws IOException {
        if (!userAgreesToRemoveTheMark(parent, file)) {
            return false;
        }
        ByteOrderMark.removeFrom(file);
        return true;
    }

    private static boolean userAgreesToRemoveTheMark(Component parent, File file) {
        StringDatabase stringDatabase = StringDatabase.getUniqueInstance();
        return JOptionPane.showConfirmDialog(parent,
                                             stringDatabase.getString("ByteOrderMark.Text").formatted(file.getName()),
                                             stringDatabase.getString("ByteOrderMark.Title"),
                                             JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
               == JOptionPane.YES_OPTION;
    }
}
