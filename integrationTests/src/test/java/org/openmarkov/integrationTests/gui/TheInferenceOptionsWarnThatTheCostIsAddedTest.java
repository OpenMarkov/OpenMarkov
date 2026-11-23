/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.gui;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.inference.tasks.Evaluation;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openmarkov.core.inference.MulticriteriaOptions.Type.COST_EFFECTIVENESS;
import static org.openmarkov.core.inference.MulticriteriaOptions.Type.UNICRITERION;
import static org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog.UNICRITERIA_SCALE_COLUMN;

/**
 * The dialog of inference options says, under the scales of the analysis with a single criterion, that the cost is
 * added when no scale is negative; and a network whose cost has the scale -1 subtracts it (error 375 of the pending
 * list).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class TheInferenceOptionsWarnThatTheCostIsAddedTest {

    private static ProbNet network(String name) throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith(name))
                          .findFirst()
                          .orElseThrow();
        return new PGMXReader().read(url).probNet();
    }

    private static <T extends Component> T find(Container container, Class<T> type, String name) {
        for (Component component : container.getComponents()) {
            if (type.isInstance(component) && (name == null || name.equals(component.getName()))) {
                return type.cast(component);
            }
            if (component instanceof Container inner) {
                T found = find(inner, type, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Opens the dialog and says whether the warning is shown, first as the dialog opens and then, if a scale is given,
     * after writing it for the first criterion.
     */
    private static List<Boolean> warningShown(ProbNet probNet, MulticriteriaOptions.Type type, String scale)
            throws Exception {
        SwingUtilities.invokeLater(() -> new InferenceOptionsDialog(probNet, null, type));
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
        List<Boolean> answers = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            Component warning = find(shown.getContentPane(), Component.class, "costIsAddedWarning");
            answers.add(warning.isVisible());
            if (scale != null) {
                JTable table = find(shown.getContentPane(), JTable.class, null);
                table.editCellAt(1, UNICRITERIA_SCALE_COLUMN);
                ((JTextComponent) table.getEditorComponent()).setText(scale);
                table.getCellEditor().stopCellEditing();
                answers.add(warning.isVisible());
            }
            shown.getOKButton().doClick();
        });
        SwingUtilities.invokeAndWait(() -> { });
        return answers;
    }

    @Test
    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialog needs a display")
    void theWarningIsShownUntilTheCostHasANegativeScale() throws Exception {
        assertEquals(List.of(true, false),
                     warningShown(network("/networks/mid/MID-Chancellor.pgmx"), UNICRITERION, "-1"));
    }

    @Test
    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialog needs a display")
    void thereIsNoWarningWhenTheCostHasANegativeScale() throws Exception {
        assertEquals(List.of(false), warningShown(network("/networks/id/ID-CEA-minimal.pgmx"), UNICRITERION, null));
    }

    @Test
    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialog needs a display")
    void thereIsNoWarningInACostEffectivenessAnalysis() throws Exception {
        assertEquals(List.of(false),
                     warningShown(network("/networks/mid/MID-Chancellor.pgmx"), COST_EFFECTIVENESS, null));
    }

    /** Treating costs 14 000 and gives 1.3 QALY, worth 30 000 each; not treating gives 0.8. */
    @Test
    void theNetworkWithItsCostAtMinusOneSubtractsIt() throws Exception {
        Evaluation evaluation = new VEEvaluation(network("/networks/id/ID-CEA-minimal.pgmx"));

        assertEquals(25000.0, evaluation.getUtility().getValues()[0], 1e-6);
    }
}
