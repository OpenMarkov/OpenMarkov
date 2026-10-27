/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PiecewiseExponentialPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.Component;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The life table of an event node, with the probabilities 0.1 from time 0 and 0.2 from time 5. Its box «Use rates»
 * was always disabled, and a file that marked the values as rates was simulated as if they were probabilities (error
 * 100 of the pending list). Now the box can be ticked, the column is titled after what the values are, and the
 * table refuses a value out of range (error 359): a probability must be between 0 and 1, and a rate cannot be below 0.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheLifeTableTakesRatesTest {

    @TempDir Path folder;

    private Node node;
    private PiecewiseExponentialPanel panel;
    private WatchedTablePanel table;

    /** The table of the panel, with its warnings silenced and recorded, and a file chooser that gives one file. */
    private static final class WatchedTablePanel extends PiecewiseExponentialTablePanel {
        private final List<String> warnings = new ArrayList<>();
        private File dataFile;

        private WatchedTablePanel(Object[][] data) {
            super(data);
            chooser = new JFileChooser() {
                @Override public int showOpenDialog(Component parent) {
                    return APPROVE_OPTION;
                }

                @Override public File getSelectedFile() {
                    return dataFile;
                }
            };
        }

        @Override protected void warnValueOutOfRange(Object time, double value, boolean rates) {
            warnings.add((rates ? "rate " : "probability ") + value + " at " + time);
        }
    }

    @BeforeEach void anEventWithTwoStretches() throws Exception {
        ProbNet probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        Variable death = new Variable("Death", "no", "yes");
        death.setVariableType(VariableType.EVENT);
        node = probNet.addNode(death, NodeType.EVENT);
        PiecewiseExponentialPotential potential =
                new PiecewiseExponentialPotential(List.of(death), PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setPiecewiseTable(new TreeMap<>(Map.of(0.0, 0.1, 5.0, 0.2)));
        node.setPotentials(List.of(potential));

        panel = new PiecewiseExponentialPanel(node) {
            @Override protected PiecewiseExponentialTablePanel createTablePanel(Object[][] data) {
                table = new WatchedTablePanel(data);
                return table;
            }
        };
    }

    private PiecewiseExponentialPotential saved() {
        return (PiecewiseExponentialPotential) node.getPotentials().getFirst();
    }

    /** Writes in a cell of the visible columns, time 0 and value 1, as the user does. */
    private void type(int row, int column, String value) {
        table.getValuesTable().onScrollableTable(scrollable -> {
            assertTrue(scrollable.editCellAt(row, column));
            ((JTextField) scrollable.getEditorComponent()).setText(value);
            assertTrue(scrollable.getCellEditor().stopCellEditing());
        });
    }

    private String valueColumnTitle() {
        List<JTable> tables = new ArrayList<>();
        table.getValuesTable().onScrollableTable(tables::add);
        return tables.getFirst().getColumnModel().getColumn(1).getHeaderValue().toString();
    }

    @Test void theValuesCanBeMarkedAsRates() throws Exception {
        assertTrue(panel.ratesCheck.isEnabled());
        assertEquals("Probability", valueColumnTitle());

        panel.ratesCheck.doClick();
        type(1, 1, "1.5");
        panel.saveChanges();

        assertEquals("Rate", valueColumnTitle());
        assertEquals(List.of(), table.warnings);
        assertTrue(saved().isUseRates());
        assertEquals(Map.of(0.0, 0.1, 5.0, 1.5), saved().getPiecewiseTable());
    }

    @Test void aProbabilityAboveOneIsRefused() throws Exception {
        type(1, 1, "1.5");

        assertEquals(List.of("probability 1.5 at 5.0"), table.warnings);
        panel.saveChanges();
        assertFalse(saved().isUseRates());
        assertEquals(Map.of(0.0, 0.1, 5.0, 0.2), saved().getPiecewiseTable());
    }

    @Test void aNegativeRateIsRefused() throws Exception {
        panel.ratesCheck.doClick();
        type(0, 1, "-0.2");

        assertEquals(List.of("rate -0.2 at 0.0"), table.warnings);
        panel.saveChanges();
        assertEquals(Map.of(0.0, 0.1, 5.0, 0.2), saved().getPiecewiseTable());
    }

    @Test void ratesThatAreNotProbabilitiesStayRates() throws Exception {
        panel.ratesCheck.doClick();
        type(1, 1, "1.5");
        panel.ratesCheck.doClick();

        assertEquals(List.of("probability 1.5 at 5.0"), table.warnings);
        assertTrue(panel.ratesCheck.isSelected());
        assertEquals("Rate", valueColumnTitle());
        panel.saveChanges();
        assertTrue(saved().isUseRates());
    }

    @Test void aFileWithAProbabilityAboveOneIsNotLoaded() throws Exception {
        table.dataFile = Files.writeString(folder.resolve("life-table.csv"), "0;0.3\n2;1.4\n").toFile();
        table.getLoadButton().doClick();

        assertEquals(List.of("probability 1.4 at 2.0"), table.warnings);
        panel.saveChanges();
        assertEquals(Map.of(0.0, 0.1, 5.0, 0.2), saved().getPiecewiseTable());
    }

    @Test void aLifeTableOfRatesOpensAsRates() throws Exception {
        saved().setUseRates(true);
        saved().setPiecewiseTable(new TreeMap<>(Map.of(0.0, 2.0)));
        panel = new PiecewiseExponentialPanel(node) {
            @Override protected PiecewiseExponentialTablePanel createTablePanel(Object[][] data) {
                table = new WatchedTablePanel(data);
                return table;
            }
        };

        assertTrue(panel.ratesCheck.isSelected());
        assertEquals("Rate", valueColumnTitle());
    }
}
