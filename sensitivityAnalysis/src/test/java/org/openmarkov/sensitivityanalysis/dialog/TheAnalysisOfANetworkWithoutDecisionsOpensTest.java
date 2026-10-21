/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.sensitivityanalysis.dialog;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.sensitivityanalysis.model.ScopeType;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisController;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The sensitivity analysis of a network without decisions opens, with the global scope, instead of ending in an
 * unexpected error; and an analysis that is made for a decision says that the network has none (error 92).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.SLOW)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheAnalysisOfANetworkWithoutDecisionsOpensTest {

    private static boolean ignoreStorage;

    private static NetworkEditorPanel tab;

    /** A chance node, a utility and two named uncertain parameters, without decisions. */
    @BeforeAll static void openTheNetwork(@TempDir Path folder) throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        Path file = folder.resolve("ID-one-chance-sa.pgmx");
        try (InputStream network = TheAnalysisOfANetworkWithoutDecisionsOpensTest.class.getResourceAsStream(
                "/ID-one-chance-sa.pgmx")) {
            Files.copy(network, file);
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                MainGUI.INSTANCE.mainPanel.openNetwork(file.toString());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            tab = MainGUI.INSTANCE.mainPanel.getMainPanelListenerAssistant().getCurrentNetworkEditorPanel();
        });
    }

    @AfterAll static void closeTheNetwork() throws Exception {
        SwingUtilities.invokeAndWait(() -> tab.close());
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    @Test void eachAnalysisOfOneCriterionOpensWithTheGlobalScope() throws Exception {
        Opened opened = open(true);

        assertNull(opened.failure.get(), "building the dialog failed");
        assertEquals(List.of(), opened.messages);
        AtomicReference<String> problem = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                JComboBox<?> types = first(opened.dialog.get(), JComboBox.class);
                for (int type = 0; type < types.getItemCount(); type++) {
                    types.setSelectedIndex(type);
                    if (opened.controller.get().getSensitivityAnalysisModel().getScopeType() != ScopeType.GLOBAL
                            || !opened.dialog.get().getOKButton().isEnabled()) {
                        problem.compareAndSet(null, types.getItemAt(type) + " is not ready with the global scope");
                    }
                }
            } catch (RuntimeException e) {
                problem.compareAndSet(null, e.toString());
            } finally {
                opened.dialog.get().dispose();
            }
        });
        assertNull(problem.get());
    }

    @Test void anAnalysisOfCostEffectivenessSaysThatTheNetworkHasNoDecision() throws Exception {
        Opened opened = open(false);
        SwingUtilities.invokeAndWait(() -> opened.dialog.get().dispose());

        assertNull(opened.failure.get(), "building the dialog failed");
        assertEquals(1, opened.messages.size(), opened.messages.toString());
        assertTrue(opened.messages.stream().allMatch(message -> message.contains("is made for a decision")),
                   opened.messages.toString());
        assertFalse(opened.okEnabled.get(), "OK is enabled");
    }

    private record Opened(AtomicReference<SensitivityAnalysisDialog> dialog,
                          AtomicReference<SensitivityAnalysisController> controller,
                          AtomicReference<Throwable> failure, AtomicBoolean okEnabled, List<String> messages) {
    }

    /**
     * Builds the dialog on the network as the menu does, for an analysis of one criterion or of cost-effectiveness,
     * and closes the messages that it shows, keeping their texts.
     */
    private static Opened open(boolean unicriterion) throws Exception {
        Opened opened = new Opened(new AtomicReference<>(), new AtomicReference<>(), new AtomicReference<>(),
                                   new AtomicBoolean(), new ArrayList<>());
        AtomicBoolean built = new AtomicBoolean();
        // A message is modal, so the dialog is not waited for
        SwingUtilities.invokeLater(() -> {
            try {
                SensitivityAnalysisController controller = new SensitivityAnalysisController(MainGUI.INSTANCE);
                controller.getConfiguration().setIsUnicriterion(unicriterion);
                opened.controller.set(controller);
                opened.dialog.set(new SensitivityAnalysisDialog(MainGUI.INSTANCE, controller));
                opened.okEnabled.set(opened.dialog.get().getOKButton().isEnabled());
            } catch (Throwable e) {
                opened.failure.set(e);
            } finally {
                built.set(true);
            }
        });
        Set<Window> closed = new HashSet<>();
        for (long end = System.currentTimeMillis() + 20_000; !built.get() && System.currentTimeMillis() < end; ) {
            Thread.sleep(20);
            for (Window window : Window.getWindows()) {
                if (window.isShowing() && window instanceof JDialog message
                        && !(window instanceof SensitivityAnalysisDialog) && closed.add(window)) {
                    JOptionPane pane = first(message, JOptionPane.class);
                    opened.messages.add(message.getTitle() + ": " + (pane == null ? "" : pane.getMessage()));
                    SwingUtilities.invokeLater(message::dispose);
                }
            }
        }
        assertTrue(built.get(), "the dialog was not built");
        if (opened.failure.get() == null) {
            assertNotNull(opened.dialog.get());
        }
        return opened;
    }

    private static <T> T first(Container container, Class<T> type) {
        for (Component component : container.getComponents()) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
            if (component instanceof Container inner) {
                T found = first(inner, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
