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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The life table of an event node, with a stretch from time 0 and another from time 5, refuses a time or a probability
 * that is not a number, and a data file with two rows that start at the same time; the table keeps what it had.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheLifeTableRefusesWhatItCannotSaveTest {

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

        @Override protected void warnRepeatedTime() {
            warnings.add("repeated time");
        }

        @Override protected void warnNotANumber() {
            warnings.add("not a number");
        }

        @Override protected void warnRepeatedTimeInFile(double time) {
            warnings.add("repeated time in the file: " + time);
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

    private TreeMap<Double, Double> saved() {
        return ((PiecewiseExponentialPotential) node.getPotentials().getFirst()).getPiecewiseTable();
    }

    /** Writes in a cell of the visible columns, time 0 and probability 1, as the user does. */
    private void type(int row, int column, String value) {
        table.getValuesTable().onScrollableTable(scrollable -> {
            assertTrue(scrollable.editCellAt(row, column));
            ((JTextField) scrollable.getEditorComponent()).setText(value);
            assertTrue(scrollable.getCellEditor().stopCellEditing());
        });
    }

    private void load(String text) throws Exception {
        table.dataFile = Files.writeString(folder.resolve("life-table.csv"), text).toFile();
        table.getLoadButton().doClick();
    }

    @Test void aTimeThatIsNotANumberIsRefused() throws Exception {
        type(1, 0, "abc");

        assertEquals(List.of("not a number"), table.warnings);
        panel.saveChanges();
        assertEquals(new TreeMap<>(Map.of(0.0, 0.1, 5.0, 0.2)), saved());
    }

    @Test void aProbabilityThatIsNotANumberIsRefused() throws Exception {
        type(1, 1, "1,5");

        assertEquals(List.of("not a number"), table.warnings);
        panel.saveChanges();
        assertEquals(new TreeMap<>(Map.of(0.0, 0.1, 5.0, 0.2)), saved());
    }

    @Test void aFileWithARepeatedTimeIsNotLoaded() throws Exception {
        load("0;0.3\n5;0.4\n5;0.5\n");

        assertEquals(List.of("repeated time in the file: 5.0"), table.warnings);
        panel.saveChanges();
        assertEquals(new TreeMap<>(Map.of(0.0, 0.1, 5.0, 0.2)), saved());
    }

    @Test void aFileWithoutRepeatedTimesIsLoaded() throws Exception {
        load("0;0.3\n2;0.4\n");

        assertEquals(List.of(), table.warnings);
        panel.saveChanges();
        assertEquals(new TreeMap<>(Map.of(0.0, 0.3, 2.0, 0.4)), saved());
    }
}
