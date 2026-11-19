/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.io;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The «Open» dialog returns the chosen file without reading it, so the reader is the one that reports what is wrong
 * with a file that is not a valid network (error 391).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheOpenDialogLeavesTheChosenFileToTheReaderTest {

    private static boolean ignoreStorage;

    @TempDir Path directory;

    @BeforeAll static void doNotTouchThePreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreThePreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    /** An «Open» dialog whose «Open» button is pressed without showing it. */
    private static final class ApprovingChooser extends NetworkOMFileChooser {
        @Override public int showDialog(Component parent, String approveButtonText) {
            return JFileChooser.APPROVE_OPTION;
        }
    }

    static Stream<Arguments> filesThatAreNotValidNetworks() {
        return Stream.of(
                Arguments.of("empty.pgmx", ""),
                Arguments.of("cut.pgmx", "<ProbModelXML formatVersion=\"0.2.0\">\n  <ProbNet type=\"BayesianNetwork\">"),
                Arguments.of("withoutVersion.pgmx", "<ProbModelXML/>"),
                Arguments.of("versionOne.pgmx", "<ProbModelXML formatVersion=\"1\"/>"),
                Arguments.of("xmlbif.xml",
                        "<BIF VERSION=\"0.3\"><NETWORK><NAME>AB</NAME></NETWORK></BIF>"),
                Arguments.of("doctype.pgmx",
                        "<!DOCTYPE ProbModelXML SYSTEM \"local.dtd\">\n<ProbModelXML formatVersion=\"0.2.0\"/>"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filesThatAreNotValidNetworks")
    void theFileIsReturned(String name, String content) throws Exception {
        Files.writeString(directory.resolve("local.dtd"), "<!ATTLIST ProbModelXML formatVersion CDATA \"1.0.0\">");
        File file = Files.writeString(directory.resolve(name), content).toFile();

        assertEquals(file, chosenWith(file));
    }

    /** Selects the file and presses «Open» on the Swing thread, as the application does; the file returned, or null. */
    private static File chosenWith(File file) throws Exception {
        File[] chosen = new File[1];
        SwingUtilities.invokeAndWait(() -> {
            NetworkOMFileChooser chooser = new ApprovingChooser();
            chooser.setSelectedFile(file);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                chosen[0] = chooser.getSelectedFile();
            }
        });
        return chosen[0];
    }
}
