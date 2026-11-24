/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.io;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;

import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * «Open», which lets several networks be chosen, completes a name typed without extension with the extension of the
 * chosen filter and with the folder it shows, as it did when it let only one be chosen (error 444).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class ANetworkTypedWithoutItsExtensionIsOpenedTest {

    private static boolean ignoreStorage;

    @TempDir Path directory;

    @BeforeAll static void doNotTouchThePreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreThePreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @BeforeEach void putTheNetworksInTheFolder() throws Exception {
        for (String name : List.of("BN-asia.pgmx", "BN-cancer.pgmx", "netAB.xml")) {
            Files.writeString(directory.resolve(name), "");
        }
    }

    /** With one name typed, the dialog chooses that name in the folder it shows, as it is written. */
    @Test void aNameWithoutExtensionGetsThatOfTheFilter() throws Exception {
        assertEquals(List.of(file("BN-asia.pgmx")),
                     chosenWith(chooser -> chooser.setSelectedFiles(new File[]{file("BN-asia")})));
    }

    /** With several names typed in quotes, the dialog chooses without folder those that are not in it as written. */
    @Test void severalNamesWithoutExtensionGetItAndTheFolderEach() throws Exception {
        assertEquals(List.of(file("BN-asia.pgmx"), file("BN-cancer.pgmx")), chosenWhenTyping("\"BN-asia\" \"BN-cancer\""));
    }

    @Test void namesWithAnExtensionOfTheFilterAreKept() throws Exception {
        assertEquals(List.of(file("BN-asia.pgmx"), file("netAB.xml")), chosenWhenTyping("\"BN-asia.pgmx\" \"netAB.xml\""));
    }

    /** One name typed and «Open» pressed, which needs a screen: the dialog asks for the keys that are pressed. */
    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "Typing one name needs a screen")
    @Test void oneNameTypedWithoutExtensionGetsThatOfTheFilter() throws Exception {
        assertEquals(List.of(file("BN-asia.pgmx")), chosenWhenTyping("BN-asia"));
    }

    private File file(String name) {
        return directory.resolve(name).toFile();
    }

    /** Types {@code typed} in the «Open» dialog and presses its «Open» button without showing it. */
    private List<File> chosenWhenTyping(String typed) throws Exception {
        return chosenWith(chooser -> {
            BasicFileChooserUI ui = (BasicFileChooserUI) chooser.getUI();
            ui.setFileName(typed);
            ui.getApproveSelectionAction().actionPerformed(new ActionEvent(chooser, ActionEvent.ACTION_PERFORMED, "Open"));
        });
    }

    /**
     * The files that «Open» reads after {@code choose}, in the «Open» dialog of networks that lets several be chosen,
     * in the folder of the test and with the filter of ProbModelXML, on the Swing thread.
     */
    private List<File> chosenWith(Consumer<NetworkOMFileChooser> choose) throws Exception {
        File[][] chosen = new File[1][];
        SwingUtilities.invokeAndWait(() -> {
            NetworkOMFileChooser chooser = new NetworkOMFileChooser();
            chooser.setMultiSelectionEnabled(true);
            chooser.setCurrentDirectory(directory.toFile());
            chooser.setFileFilter(probModelXMLFilter(chooser));
            choose.accept(chooser);
            chosen[0] = chooser.getSelectedFiles();
        });
        return Arrays.asList(chosen[0]);
    }

    private static FileFilter probModelXMLFilter(NetworkOMFileChooser chooser) {
        return Arrays.stream(chooser.getChoosableFileFilters())
                     .filter(filter -> filter instanceof FileFilterByExtension<?> byExtension
                             && byExtension.getExtensions().contains("pgmx"))
                     .findFirst()
                     .orElseThrow();
    }
}
