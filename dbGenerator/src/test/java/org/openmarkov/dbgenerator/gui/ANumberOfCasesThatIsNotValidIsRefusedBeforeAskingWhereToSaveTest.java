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
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the case database generator, «Generate» with «abc» typed as the number of cases is refused as expected, without
 * asking where to save, and the window stays in view.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class ANumberOfCasesThatIsNotValidIsRefusedBeforeAskingWhereToSaveTest {

    private static boolean ignoreStorage;

    @BeforeAll static void doNotTouchThePreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreThePreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Test void isRefused() throws Exception {
        AtomicInteger asked = new AtomicInteger();
        JFileChooser cancelling = new JFileChooser() {
            @Override public int showSaveDialog(Component parent) {
                asked.incrementAndGet();
                return CANCEL_OPTION;
            }
        };
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.setName("open");
        Variable a = new Variable("A", "absent", "present");
        net.addNode(a, NodeType.CHANCE)
           .setPotential(new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.5, 0.5}));
        AtomicReference<NetworkEditorPanel> tab = new AtomicReference<>();
        AtomicReference<DBGeneratorGUI> window = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            tab.set(MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().createNewFrame(net));
            window.set(new DBGeneratorGUI(MainGUI.INSTANCE));
            set(window.get(), "caseDBFileChooser", cancelling);
            ((AbstractButton) get(window.get(), "fromOpenMarkovRadioButton")).doClick();
            ((JComboBox<?>) get(window.get(), "caseNumber")).setSelectedItem("abc");
        });
        // The window is modal: it is shown without waiting for it
        SwingUtilities.invokeLater(() -> window.get().setVisible(true));
        for (long end = System.currentTimeMillis() + 10_000;
             !window.get().isShowing() && System.currentTimeMillis() < end; ) {
            Thread.sleep(20);
        }
        AtomicReference<RuntimeException> refused = new AtomicReference<>();
        AtomicBoolean visibleAfterRefusing = new AtomicBoolean();
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    ((AbstractButton) get(window.get(), "generateButton")).doClick();
                } catch (RuntimeException e) {
                    refused.set(e);
                }
                visibleAfterRefusing.set(window.get().isVisible());
            });
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                window.get().dispose();
                tab.get().close();
            });
        }

        UnrecoverableException expected = assertInstanceOf(UnrecoverableException.class, refused.get());
        assertInstanceOf(InvalidArgumentException.class, expected.getCause());
        assertEquals(0, asked.get(), "times asked where to save");
        assertTrue(visibleAfterRefusing.get(), "the window is in view");
    }

    private static Object get(DBGeneratorGUI window, String name) {
        try {
            Field field = DBGeneratorGUI.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(window);
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
