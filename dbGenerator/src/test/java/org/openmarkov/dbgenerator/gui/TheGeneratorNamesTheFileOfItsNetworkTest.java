/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.dbgenerator.gui;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.dialog.io.NetworkOMFileChooser;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import javax.swing.AbstractButton;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * In the case database generator, after opening a network and then, with «Open», a file that is not one, the box
 * still names the file of the network that is used. The refusal is a checked exception marked as expected, which the
 * window of errors shows with its own title and message.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheGeneratorNamesTheFileOfItsNetworkTest {

    private static boolean ignoreStorage;

    @BeforeAll static void doNotTouchThePreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreThePreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    /** A file chooser that answers with the file it is given, without showing itself. */
    private static final class AnsweringChooser extends NetworkOMFileChooser {
        private File answer;

        @Override public int showOpenDialog(Component parent) {
            setSelectedFile(answer);
            return JFileChooser.APPROVE_OPTION;
        }
    }

    @Test void aFileThatIsNotANetworkLeavesTheNameOfTheNetworkUsed(@TempDir Path folder) throws Exception {
        ProbNet network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        network.addNode(new Variable("A", "absent", "present"), NodeType.CHANCE);
        File fromFile = folder.resolve("fromFile.pgmx").toFile();
        new PGMXWriter_1_0().write(fromFile.getPath(), network, List.of());
        File broken = Files.writeString(folder.resolve("broken.pgmx"), "This is not a network").toFile();
        AnsweringChooser chooser = new AnsweringChooser();
        SwingUtilities.invokeAndWait(() -> {
            DBGeneratorGUI window = new DBGeneratorGUI(MainGUI.INSTANCE);
            try {
                set(window, "netFileChooser", chooser);
                get(window, "fromFileRadioButton", AbstractButton.class).doClick();
                AbstractButton open = get(window, "loadNetButton", AbstractButton.class);
                chooser.answer = fromFile;
                open.doClick();
                ProbNet used = get(window, "net", ProbNet.class);

                chooser.answer = broken;
                var refused = assertThrows(UnrecoverableException.class, open::doClick);

                assertAll(() -> assertEquals("fromFile.pgmx",
                                             get(window, "netFilePathTextPane", JTextComponent.class).getText()),
                          () -> assertSame(used, get(window, "net", ProbNet.class)),
                          () -> assertFalse(refused.getCause() instanceof RuntimeException, "a checked exception"));
            } finally {
                window.dispose();
            }
        });
    }

    private static <T> T get(DBGeneratorGUI window, String name, Class<T> type) {
        try {
            Field field = DBGeneratorGUI.class.getDeclaredField(name);
            field.setAccessible(true);
            return type.cast(field.get(window));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void set(DBGeneratorGUI window, String name, Object value) {
        try {
            Field field = DBGeneratorGUI.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(window, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
