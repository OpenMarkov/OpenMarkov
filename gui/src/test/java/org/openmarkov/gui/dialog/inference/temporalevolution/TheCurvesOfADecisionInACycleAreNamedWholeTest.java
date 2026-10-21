/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.inference.temporalevolution;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Each curve of the temporal evolution of a chance variable is named after its state and, in brackets, the option of
 * the decision it is conditioned on. The legend and the sum of the states took those names apart by their first
 * closing bracket, which for a decision in a cycle, such as «Therapy [0]», is that of the cycle: the options shared
 * one title in the legend, and the sum ended in an unexpected error (error 211).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.SLOW)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheCurvesOfADecisionInACycleAreNamedWholeTest {

    private static final StringDatabase strings = StringDatabase.getUniqueInstance();

    private static boolean ignoreStorage;

    private static NetworkEditorPanel tab;

    private TraceTemporalEvolutionDialog dialog;

    /** Health, healthy or sick, depends on the decision Therapy [0], and evolves along three cycles. */
    @BeforeAll static void openTheNetwork(@TempDir Path folder) throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        Path file = folder.resolve("MID-decision-in-a-cycle.pgmx");
        try (InputStream network = TheCurvesOfADecisionInACycleAreNamedWholeTest.class.getResourceAsStream(
                "/MID-decision-in-a-cycle.pgmx")) {
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

    @AfterEach void closeTheDialog() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            if (dialog != null) {
                dialog.dispose();
            }
        });
    }

    @Test void eachOptionHasItsOwnTitleInTheLegend() throws Exception {
        openTheEvolutionOfHealth();

        assertEquals(List.of("Therapy [0] = no", "healthy", "sick", "Therapy [0] = yes", "healthy", "sick"),
                     legend());
    }

    @Test void theSumOfTheStatesIsNamedAfterEachOption() throws Exception {
        openTheEvolutionOfHealth();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            try {
                radioButton("TemporalEvolutionResultDialog.Display.Summatory").doClick();
            } catch (Throwable e) {
                failure.set(e);
            }
        });

        assertNull(failure.get());
        assertEquals(List.of("Therapy [0] = no", "Therapy [0] = yes"), legend());
    }

    /** Opens the window as the menu does, conditioned on the decision, and waits for it to show. */
    private void openTheEvolutionOfHealth() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ProbNet net = tab.getProbNet();
            try {
                new TraceTemporalEvolutionDialog(MainGUI.INSTANCE, net.getNode("Health [0]"), new EvidenceCase(),
                                                 net.getVariable("Therapy [0]"));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        for (long end = System.currentTimeMillis() + 20_000; dialog == null && System.currentTimeMillis() < end; ) {
            Thread.sleep(50);
            for (Window window : Window.getWindows()) {
                if (window instanceof TraceTemporalEvolutionDialog shown && shown.isShowing()) {
                    dialog = shown;
                }
            }
        }
        assertNotNull(dialog, "the window of the temporal evolution did not show");
        // It is built in a thread of its own: what it still has to do goes on in the queue of events
        SwingUtilities.invokeAndWait(() -> { });
    }

    /** The texts of the legend, the titles of the options and the names of the curves, in order. */
    private List<String> legend() throws Exception {
        List<String> texts = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            JPanel legend = legendPanel(dialog.getContentPane());
            assertNotNull(legend, "there is no legend");
            for (Component component : legend.getComponents()) {
                if (component instanceof JLabel label) {
                    texts.add(label.getText());
                }
            }
        });
        return texts;
    }

    private static JPanel legendPanel(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel panel && panel.getBorder() instanceof TitledBorder border
                    && border.getTitle().equals(strings.getString("TemporalEvolutionResultDialog.Legend.Title"))) {
                return panel;
            }
            if (component instanceof Container inner) {
                JPanel found = legendPanel(inner);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private JRadioButton radioButton(String key) {
        List<JRadioButton> found = new ArrayList<>();
        collect(dialog.getContentPane(), found);
        return found.stream().filter(button -> button.getText().equals(strings.getString(key))).findFirst()
                    .orElseThrow();
    }

    private static void collect(Container container, List<JRadioButton> found) {
        for (Component component : container.getComponents()) {
            if (component instanceof JRadioButton button) {
                found.add(button);
            } else if (component instanceof Container inner) {
                collect(inner, found);
            }
        }
    }
}
