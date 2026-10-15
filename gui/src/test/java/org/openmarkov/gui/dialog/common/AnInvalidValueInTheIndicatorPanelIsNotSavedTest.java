/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.IndicatorPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JSpinner;
import java.awt.Component;
import java.awt.Container;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * An event node whose relation is an indicator: time to the event 2 and probability of occurrence 0.25. In its panel
 * the user writes the two numbers.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnInvalidValueInTheIndicatorPanelIsNotSavedTest {

    private Node node;
    private IndicatorPotentialPanel panel;
    private JSpinner tteSpinner;
    private JSpinner pOccurrenceSpinner;
    private Locale formatLocale;

    /** Numbers are written in English, as OpenMarkov sets when it starts. */
    @BeforeEach void anEventNodeWithAnIndicator() {
        formatLocale = Locale.getDefault(Locale.Category.FORMAT);
        Locale.setDefault(Locale.Category.FORMAT, Locale.ENGLISH);
        ProbNet probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        Variable e = new Variable("E", "no", "yes");
        e.setVariableType(VariableType.EVENT);
        node = probNet.addNode(e, NodeType.EVENT);
        node.setPotentials(List.of(new IndicatorPotential(List.of(e), PotentialRole.CONDITIONAL_PROBABILITY, 2, 0.25)));
        openPanel();
    }

    @AfterEach void restoreTheLocale() {
        Locale.setDefault(Locale.Category.FORMAT, formatLocale);
    }

    private void openPanel() {
        panel = new IndicatorPotentialPanel(node);
        List<JSpinner> spinners = new ArrayList<>();
        findSpinners(panel, spinners);
        tteSpinner = spinners.get(0);
        pOccurrenceSpinner = spinners.get(1);
    }

    private static void findSpinners(Container container, List<JSpinner> spinners) {
        for (Component component : container.getComponents()) {
            if (component instanceof JSpinner spinner) {
                spinners.add(spinner);
            } else if (component instanceof Container inner) {
                findSpinners(inner, spinners);
            }
        }
    }

    private static void write(JSpinner spinner, String text) {
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setText(text);
    }

    private static String written(JSpinner spinner, double value) {
        return ((JSpinner.NumberEditor) spinner.getEditor()).getFormat().format(value);
    }

    private IndicatorPotential indicator() {
        return (IndicatorPotential) node.getPotentials().getFirst();
    }

    /** Before, saving threw an unexpected exception and the tab of the relation was left empty. */
    @ParameterizedTest
    @CsvSource({ "abc, abc", "-1, 1.5", "-0.5, -0.5", "'3,5', '0,5'", "3.5abc, 0.5abc" })
    void anInvalidValueIsNotSavedAndTheRelationKeepsItsValues(String tte, String pOccurrence) throws Exception {
        write(tteSpinner, tte);
        write(pOccurrenceSpinner, pOccurrence);
        panel.saveChanges();
        assertEquals(2, indicator().getTte());
        assertEquals(0.25, indicator().getpOccurrence());
    }

    /**
     * «NaN» and «∞» are how the English format writes them, and «1E400» does not fit in a double: the box of the time
     * took the three, and with any of them the event never happened (error 265 of the pending list).
     */
    @ParameterizedTest
    @ValueSource(strings = { "NaN", "∞", "1E400" })
    void aTimeThatIsNotAFiniteNumberIsNotSaved(String tte) throws Exception {
        write(tteSpinner, tte);
        panel.saveChanges();
        assertEquals(2, indicator().getTte());
        assertEquals(0.25, indicator().getpOccurrence());
    }

    /** The relation itself refuses them too, whoever gives them. */
    @Test void theRelationRefusesNumbersThatAreNotValid() {
        IndicatorPotential indicator = indicator();
        assertThrows(InvalidArgumentException.class, () -> indicator.setTte(Double.NaN));
        assertThrows(InvalidArgumentException.class, () -> indicator.setTte(Double.POSITIVE_INFINITY));
        assertThrows(InvalidArgumentException.class, () -> indicator.setpOccurrence(Double.NaN));
        assertEquals(2, indicator.getTte());
        assertEquals(0.25, indicator.getpOccurrence());
    }

    @Test void aValidValueIsSaved() throws Exception {
        write(tteSpinner, written(tteSpinner, 3.5));
        write(pOccurrenceSpinner, written(pOccurrenceSpinner, 0.0001));
        panel.saveChanges();
        assertEquals(3.5, indicator().getTte());
        assertEquals(0.0001, indicator().getpOccurrence());
    }

    /** The default editor of a spinner would show 0.0001 as 0. */
    @Test void theBoxesShowAllTheDecimals() throws Exception {
        node.setPotentials(List.of(new IndicatorPotential(List.of(node.getVariable()),
                                                          PotentialRole.CONDITIONAL_PROBABILITY, 1.125, 0.0001)));
        openPanel();
        DecimalFormat format = ((JSpinner.NumberEditor) pOccurrenceSpinner.getEditor()).getFormat();
        assertEquals(1.125, format.parse(((JSpinner.DefaultEditor) tteSpinner.getEditor()).getTextField().getText())
                                  .doubleValue());
        assertEquals(0.0001, format.parse(((JSpinner.DefaultEditor) pOccurrenceSpinner.getEditor()).getTextField()
                                                                                                  .getText())
                                   .doubleValue());
    }
}
