/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.configuration;

import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.ShellAPI;
import io.github.jorgericovivas.rust_essentials.tuples.Tuple2Record;
import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * Utility class to store the last open files
 *
 * @author jlgozalo
 * @version 1.0 25 Jul 2009
 */
public class LastOpenFiles {
    
    /**
     * maximum number of last open files per OPENMARKOV session
     */
    // TODO to be configured by an external configuration file
    public static final int MAX_LAST_OPEN_FILES = 9;
    
    /**
     * retrieves the name of the file that is located in the position index
     *
     * @param index - the position of file in the list of last open files
     *
     * @return the fileName or empty
     */
    public static NetworkEditorPanel.NetworkSource getSourceAt(int index) {
        return UserPreferences.LAST_OPEN_NETWORKS_FILES.get().get(index);
    }
    
    /**
     * reorder the list of last open files considering that if the file was
     * already open, only some of the files must be reorder
     *
     * @param source - name of the file to find
     */
    public static void setLastSource(NetworkEditorPanel.NetworkSource source) {
        UserPreferences.LAST_OPEN_NETWORKS_FILES.use(latestOpenFiles -> {
            var index = getIndexForSource(source);
            if (index.isEmpty()) {
                latestOpenFiles.addFirst(source);
                while (latestOpenFiles.size() > MAX_LAST_OPEN_FILES) {
                    latestOpenFiles.removeLast();
                }
            } else {
                latestOpenFiles.remove(index.getAsInt());
                latestOpenFiles.addFirst(source);
            }
        });
        
        if (UserPreferences.UPDATE_RECENTS_ON_OPEN_NETWORK.get() && source instanceof NetworkEditorPanel.NetworkSource.SourceFile sourceFile) {
            switch (OperatingSystem.CURRENT_OS) {
                case WINDOWS -> {
                    WString unicodeStringPath = new WString(sourceFile.file().getPath());
                    // 3 is for null-terminated Unicode string
                    Shell32.INSTANCE.SHAddToRecentDocs(3, unicodeStringPath);
                }
                case LINUX, OTHER -> {
                }
            }
        }
    }
    
    private interface Shell32 extends ShellAPI {
        Shell32 INSTANCE = Native.load("shell32", Shell32.class);
        
        void SHAddToRecentDocs(int flags, WString file);
    }
    
    /**
     * retrieves the position of a specific file in the list of last open files
     *
     * @param fileName - name of the file to find the position
     *
     * @return index for the filename if exist; otherwise, return -1
     */
    public static OptionalInt getIndexForSource(NetworkEditorPanel.NetworkSource source) {
        return IntStream.range(0, UserPreferences.LAST_OPEN_NETWORKS_FILES.get().size())
                        .filter(index -> {
                            return switch (Tuples.record(source, UserPreferences.LAST_OPEN_NETWORKS_FILES.get()
                                                                                                         .get(index))) {
                                case Tuple2Record(
                                        NetworkEditorPanel.NetworkSource.SourceFile s1,
                                        NetworkEditorPanel.NetworkSource.SourceFile s2
                                ) -> s1.file().equals(s2.file());
                                case Tuple2Record(
                                        NetworkEditorPanel.NetworkSource.SourceURL s1,
                                        NetworkEditorPanel.NetworkSource.SourceURL s2
                                ) -> s1.url().equals(s2.url());
                                default -> false;
                            };
                        })
                        .findFirst();
    }
    
    /**
     * @return true if there are some last open files; false otherwise
     */
    public static boolean existLastOpenFiles() {
        return !UserPreferences.LAST_OPEN_NETWORKS_FILES.get().isEmpty();
    }
    
    /**
     * @return index the index for the oldest open file
     */
    public static int getOldestOpenFileIndex() {
        return UserPreferences.LAST_OPEN_NETWORKS_FILES.get().size() - 1;
    }
}
