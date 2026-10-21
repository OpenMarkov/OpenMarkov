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
import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.gui.window.edition.networkEditorPanel.NetworkEditorPanel;
import org.openmarkov.sensitivityanalysis.model.AnalysisType;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisController;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * «OK» of the sensitivity analysis, with a variation that spans no interval, says so and runs nothing, instead of
 * ending in an unexpected error; and it reads the interval written in its text fields also without Enter
 * (error 256).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.SLOW)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The main window needs a screen")
class TheVariationIsCheckedOnOkTest {

    private static final StringDatabase strings = StringDatabase.getUniqueInstance();

    /** Places of the panels of the axes in an {@link AxisVariationPanel}. */
    private static final int HORIZONTAL = 0, VERTICAL = 1;

    private static boolean ignoreStorage;

    private static NetworkEditorPanel tab;

    @BeforeAll static void openTheNetwork(@TempDir Path folder) throws Exception {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        Path file = folder.resolve("ID-decide-test-sensan.pgmx");
        try (InputStream network = TheVariationIsCheckedOnOkTest.class.getResourceAsStream(
                "/ID-decide-test-sensan.pgmx")) {
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

    /** The horizontal axis starts with the percentage of the 2nd order probability. */
    @Test void aPercentageOfZeroIsRefused() throws Exception {
        List<String> messages = messagesOnOk(dialog -> parameterControl(dialog, HORIZONTAL, JComboBox.class, 0)
                .setSelectedItem(0.0), null);
        assertEquals(List.of(invalid("POPP")), messages);
    }

    @Test void aPercentageThatIsNotANumberIsRefused() throws Exception {
        List<String> messages = messagesOnOk(dialog -> parameterControl(dialog, HORIZONTAL, JComboBox.class, 0)
                .setSelectedItem("abc"), null);
        assertEquals(List.of(invalid("POPP")), messages);
    }

    @Test void anIntervalWrittenWithoutEnterIsChecked() throws Exception {
        List<String> messages = messagesOnOk(dialog -> writeTheInterval(dialog, HORIZONTAL, "1", "0"), null);
        assertEquals(List.of(invalid("UDIN")), messages);
    }

    @Test void anIntervalWrittenWithoutEnterIsUsed() throws Exception {
        AtomicReference<AxisVariation> used = new AtomicReference<>();
        List<String> messages = messagesOnOk(dialog -> writeTheInterval(dialog, HORIZONTAL, "0.2", "0.6"),
                                             controller -> used.set(controller.getSensitivityAnalysisModel()
                                                                              .getHorizontalAxisVariation()));
        assertFalse(messages.contains(invalid("UDIN")), messages.toString());
        assertArrayEquals(new double[]{0.2, 0.6}, used.get().getVariationBounds());
    }

    @Test void theMapChecksItsVerticalAxisToo() throws Exception {
        List<String> messages = messagesOnOk(dialog -> {
            first(dialog, JComboBox.class).setSelectedItem(strings.getString(AnalysisType.MAP.toString()));
            writeTheInterval(dialog, VERTICAL, "0.5", "0.5");
        }, null);
        assertEquals(List.of(strings.getString("SensitivityAnalysis.Axis.Invalid") + ": "
                             + strings.getFormattedString("SensitivityAnalysis.Axis.Invalid.Vertical",
                                                          strings.getString("SensitivityAnalysis.Axis.Invalid.UDIN"))),
                     messages);
    }

    private static String invalid(String type) {
        return strings.getString("SensitivityAnalysis.Axis.Invalid") + ": "
                + strings.getString("SensitivityAnalysis.Axis.Invalid." + type);
    }

    private static void writeTheInterval(SensitivityAnalysisDialog dialog, int axis, String lower, String upper) {
        JComboBox<?> types = (JComboBox<?>) axis(dialog, axis).getComponent(1);
        types.setSelectedItem(strings.getString(DeterministicAxisVariationType.UDIN.toString()));
        // Written as by the keyboard, but without Enter
        parameterControl(dialog, axis, JTextField.class, 0).setText(lower);
        parameterControl(dialog, axis, JTextField.class, 1).setText(upper);
    }

    private static JPanel axis(SensitivityAnalysisDialog dialog, int axis) {
        return (JPanel) first(dialog, AxisVariationPanel.class).getComponent(axis);
    }

    /** The control number {@code index} of class {@code type} among the parameters of {@code axis}. */
    private static <T extends Component> T parameterControl(SensitivityAnalysisDialog dialog, int axis, Class<T> type,
                                                            int index) {
        JPanel parameters = (JPanel) axis(dialog, axis).getComponent(2);
        List<T> controls = new ArrayList<>();
        for (Component component : parameters.getComponents()) {
            if (type.isInstance(component)) {
                controls.add(type.cast(component));
            }
        }
        return controls.get(index);
    }

    /**
     * Opens the dialog of the sensitivity analysis on the network, lets {@code write} change the axes, presses «OK»
     * and closes whatever it opens.
     *
     * @return the title and the text of each message shown
     */
    private static List<String> messagesOnOk(Consumer<SensitivityAnalysisDialog> write,
                                             Consumer<SensitivityAnalysisController> afterOk) throws Exception {
        AtomicReference<SensitivityAnalysisDialog> dialog = new AtomicReference<>();
        AtomicReference<SensitivityAnalysisController> controller = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            controller.set(new SensitivityAnalysisController(MainGUI.INSTANCE));
            dialog.set(new SensitivityAnalysisDialog(MainGUI.INSTANCE, controller.get()));
            write.accept(dialog.get());
        });
        AtomicBoolean pressed = new AtomicBoolean();
        // A refusal shows a modal message, so «OK» is not waited for
        SwingUtilities.invokeLater(() -> {
            try {
                dialog.get().getOKButton().doClick();
            } finally {
                pressed.set(true);
            }
        });
        List<String> messages = new ArrayList<>();
        Set<Window> closed = new HashSet<>();
        for (long end = System.currentTimeMillis() + 20_000; !pressed.get() && System.currentTimeMillis() < end; ) {
            Thread.sleep(20);
            closeTheWindowsOpened(closed, messages);
        }
        SwingUtilities.invokeAndWait(() -> {
            if (afterOk != null) {
                afterOk.accept(controller.get());
            }
            controller.get().closeAllPlots();
            dialog.get().dispose();
        });
        closeTheWindowsOpened(closed, messages);
        assertTrue(pressed.get(), "«OK» did not end");
        return messages;
    }

    private static void closeTheWindowsOpened(Set<Window> closed, List<String> messages) {
        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window instanceof JDialog opened
                    && !(window instanceof SensitivityAnalysisDialog) && closed.add(window)) {
                JOptionPane message = first(opened, JOptionPane.class);
                if (message != null) {
                    messages.add(opened.getTitle() + ": " + message.getMessage());
                }
                SwingUtilities.invokeLater(opened::dispose);
            }
        }
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
