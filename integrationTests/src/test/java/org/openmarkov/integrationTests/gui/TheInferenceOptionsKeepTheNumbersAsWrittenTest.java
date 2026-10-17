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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openmarkov.core.inference.MulticriteriaOptions.Type.COST_EFFECTIVENESS;
import static org.openmarkov.core.inference.MulticriteriaOptions.Type.UNICRITERION;
import static org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog.CE_DISCOUNT_COLUMN;
import static org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog.CE_SCALE_COLUMN;
import static org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog.UNICRITERIA_DISCOUNT_COLUMN;
import static org.openmarkov.gui.dialog.inference.common.InferenceOptionsDialog.UNICRITERIA_SCALE_COLUMN;

/**
 * The scale and the discount that the user writes for a criterion in the dialog of inference options, or leaves as
 * they were, reach the network with all their figures. The criterion is «cost», in the first row of the table.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The dialog needs a display")
class TheInferenceOptionsKeepTheNumbersAsWrittenTest {

    private static ProbNet network(String name) throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith(name))
                          .findFirst()
                          .orElseThrow();
        return new PGMXReader().read(url).probNet();
    }

    private static JTable tableOf(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTable table) {
                return table;
            }
            if (component instanceof Container inner) {
                JTable table = tableOf(inner);
                if (table != null) {
                    return table;
                }
            }
        }
        return null;
    }

    /** Opens the dialog, edits the cell of the cost (writing the text, or leaving it as it was if null) and accepts. */
    private static Criterion costAfterEditing(ProbNet probNet, MulticriteriaOptions.Type type, int column, String text)
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
        SwingUtilities.invokeAndWait(() -> {
            JTable table = tableOf(shown.getContentPane());
            table.editCellAt(1, column);
            if (text != null) {
                ((JTextComponent) table.getEditorComponent()).setText(text);
            }
            table.getCellEditor().stopCellEditing();
            shown.getOKButton().doClick();
        });
        SwingUtilities.invokeAndWait(() -> { });
        return probNet.getDecisionCriteria().stream()
                      .filter(criterion -> criterion.getCriterionName().equals("cost"))
                      .findFirst()
                      .orElseThrow();
    }

    @ParameterizedTest
    @CsvSource({ "-3.3333333333333335E-5", "-0.0006", "0.0004" })
    void theScaleOfOneCriterion(String written) throws Exception {
        ProbNet probNet = network("/networks/id/ID-CEA-test-2therapies.pgmx");

        Criterion cost = costAfterEditing(probNet, UNICRITERION, UNICRITERIA_SCALE_COLUMN, written);

        assertEquals(Double.parseDouble(written), cost.getUnicriterizationScale());
    }

    @Test
    void theScaleOfOneCriterionLeftAsItWas() throws Exception {
        ProbNet probNet = network("/networks/id/ID-CEA-test-2therapies.pgmx");
        probNet.getDecisionCriteria().getFirst().setUnicriterizationScale(-1.0 / 30000);

        Criterion cost = costAfterEditing(probNet, UNICRITERION, UNICRITERIA_SCALE_COLUMN, null);

        assertEquals(-1.0 / 30000, cost.getUnicriterizationScale());
    }

    @Test
    void theScaleOfCostEffectiveness() throws Exception {
        ProbNet probNet = network("/networks/id/ID-CEA-test-2therapies.pgmx");

        Criterion cost = costAfterEditing(probNet, COST_EFFECTIVENESS, CE_SCALE_COLUMN, "0.000001");

        assertEquals(0.000001, cost.getCeScale());
    }

    @ParameterizedTest
    @CsvSource({ "UNICRITERION, 3.5555, 0.035555", "COST_EFFECTIVENESS, 3.5555, 0.035555", "UNICRITERION, 1.4, 0.014",
                 "COST_EFFECTIVENESS, 1.4, 0.014" })
    void theDiscount(MulticriteriaOptions.Type type, String written, double discount) throws Exception {
        ProbNet probNet = network("/networks/mid/MID-Chancellor.pgmx");
        int column = type == UNICRITERION ? UNICRITERIA_DISCOUNT_COLUMN : CE_DISCOUNT_COLUMN;

        Criterion cost = costAfterEditing(probNet, type, column, written);

        assertEquals(discount, cost.getDiscount());
    }

    @ParameterizedTest
    @CsvSource({ "UNICRITERION", "COST_EFFECTIVENESS" })
    void theDiscountLeftAsItWas(MulticriteriaOptions.Type type) throws Exception {
        ProbNet probNet = network("/networks/mid/MID-Chancellor.pgmx");
        probNet.getDecisionCriteria().stream()
               .filter(criterion -> criterion.getCriterionName().equals("cost"))
               .forEach(criterion -> criterion.setDiscount(0.014));
        int column = type == UNICRITERION ? UNICRITERIA_DISCOUNT_COLUMN : CE_DISCOUNT_COLUMN;

        Criterion cost = costAfterEditing(probNet, type, column, null);

        assertEquals(0.014, cost.getDiscount());
    }
}
