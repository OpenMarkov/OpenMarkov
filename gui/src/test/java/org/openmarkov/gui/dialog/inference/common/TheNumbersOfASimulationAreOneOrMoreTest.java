/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.inference.common;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The options of a simulation do not take a number of individuals or of series that is not an integer, 1 or more:
 * the dialog says which one, stays open and leaves the network as it was (error 442).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheNumbersOfASimulationAreOneOrMoreTest {

    private static ProbNet network() {
        ProbNet network = new ProbNet(DESNetworkType.getUniqueInstance());
        MonteCarloOptions options = network.getInferenceOptions().getMonteCarloOptions();
        options.setNumSimulations(250);
        options.setNumSeries(3);
        return network;
    }

    private static JTextField fieldNamed(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTextField field && name.equals(field.getName())) {
                return field;
            }
            if (component instanceof Container inner) {
                JTextField field = fieldNamed(inner, name);
                if (field != null) {
                    return field;
                }
            }
        }
        return null;
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({"numSimulationsTextField, Number of individuals, -5", "numSeriesTextField, Number of series, -2",
                "numSimulationsTextField, Number of individuals, 0", "numSeriesTextField, Number of series, 0",
                "numSimulationsTextField, Number of individuals, abc", "numSeriesTextField, Number of series, 2.5"})
    void thePanelRejectsTheNumberAndNamesIt(String field, String label, String written) {
        MonteCarloOptionsPanel panel = new MonteCarloOptionsPanel(network());
        fieldNamed(panel, field).setText(written);

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, panel::getMonteCarloOptions);

        assertEquals("Argument " + label + " (" + written + ") is invalid because it must be a whole number, 1 or more.",
                     exception.getMessage());
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({"numSimulationsTextField, -5", "numSeriesTextField, 0", "numSimulationsTextField, abc"})
    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialog needs a screen")
    void theDialogStaysOpenAndTheNetworkKeepsItsNumbers(String field, String written) throws Exception {
        ProbNet network = network();
        SwingUtilities.invokeLater(
                () -> new InferenceOptionsDialog(network, null, MulticriteriaOptions.Type.UNICRITERION));
        InferenceOptionsDialog dialog = null;
        for (int attempt = 0; attempt < 100 && dialog == null; attempt++) {
            Thread.sleep(100);
            for (Window window : Window.getWindows()) {
                if (window instanceof InferenceOptionsDialog shown && shown.isShowing()) {
                    dialog = shown;
                }
            }
        }
        InferenceOptionsDialog shown = dialog;
        AtomicReference<RuntimeException> thrown = new AtomicReference<>();
        AtomicReference<Boolean> stillOpen = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                fieldNamed(shown.getContentPane(), field).setText(written);
                try {
                    shown.getOKButton().doClick();
                } catch (RuntimeException exception) {
                    thrown.set(exception);
                }
                stillOpen.set(shown.isShowing());
            });
        } finally {
            SwingUtilities.invokeAndWait(shown::dispose);
        }

        assertInstanceOf(UnrecoverableException.class, thrown.get());
        assertInstanceOf(InvalidArgumentException.class, thrown.get().getCause());
        assertTrue(stillOpen.get());
        assertEquals(250, network.getInferenceOptions().getMonteCarloOptions().getNumSimulations());
        assertEquals(3, network.getInferenceOptions().getMonteCarloOptions().getNumSeries());
    }
}
