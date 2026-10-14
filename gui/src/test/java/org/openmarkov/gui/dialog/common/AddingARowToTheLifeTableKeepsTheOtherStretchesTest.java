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
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PiecewiseExponentialPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The life table of an event node, with a stretch from time 0 and another from time 5. Adding a row used to put it at
 * time 0, so saving replaced the first stretch with probability 0, and a time could be repeated by hand (error 152).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AddingARowToTheLifeTableKeepsTheOtherStretchesTest {

    private Node node;
    private PiecewiseExponentialPanel panel;
    private WatchedTablePanel table;

    /** The table of the panel, with the warning dialog silenced and counted. */
    private static final class WatchedTablePanel extends PiecewiseExponentialTablePanel {
        private int warnings;

        private WatchedTablePanel(Object[][] data) {
            super(data);
        }

        @Override protected void warnRepeatedTime() {
            warnings++;
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

    private static List<String> buttonNames(Container container) {
        List<String> names = new ArrayList<>();
        for (Component component : container.getComponents()) {
            if (component instanceof JButton button && button.isVisible()) {
                names.add(button.getName());
            }
            if (component instanceof Container inner) {
                names.addAll(buttonNames(inner));
            }
        }
        return names;
    }

    private TreeMap<Double, Double> saved() {
        return ((PiecewiseExponentialPotential) node.getPotentials().getFirst()).getPiecewiseTable();
    }

    /** Writes a time in a cell, as the user does: opens its editor, types and confirms. */
    private void typeTime(int row, String time) {
        table.getValuesTable().onScrollableTable(scrollable -> {
            assertTrue(scrollable.editCellAt(row, 0));
            ((JTextField) scrollable.getEditorComponent()).setText(time);
            assertTrue(scrollable.getCellEditor().stopCellEditing());
        });
    }

    @Test void addingARowAndSavingKeepsTheStretchThatStartsAtZero() throws Exception {
        table.getAddValueButton().doClick();
        panel.saveChanges();

        assertEquals(new TreeMap<>(Map.of(0.0, 0.1, 5.0, 0.2, 6.0, 0.0)), saved());
    }

    @Test void twoAddedRowsGetDifferentTimes() throws Exception {
        table.getAddValueButton().doClick();
        table.getAddValueButton().doClick();
        panel.saveChanges();

        assertEquals(new TreeMap<>(Map.of(0.0, 0.1, 5.0, 0.2, 6.0, 0.0, 7.0, 0.0)), saved());
    }

    @Test void aTimeThatAnotherRowHasIsRefusedWithAWarning() throws Exception {
        table.getAddValueButton().doClick();
        typeTime(2, "5");
        typeTime(1, "0.0");

        assertEquals(2, table.warnings);
        assertEquals(6.0, table.getValuesTable().getValueAt(2, 1, null), "the cell keeps its time");
        panel.saveChanges();
        assertEquals(new TreeMap<>(Map.of(0.0, 0.1, 5.0, 0.2, 6.0, 0.0)), saved());
    }

    @Test void aTimeThatNoOtherRowHasIsAccepted() throws Exception {
        table.getAddValueButton().doClick();
        typeTime(2, "3");
        typeTime(1, "5");

        assertEquals(0, table.warnings);
        panel.saveChanges();
        assertEquals(new TreeMap<>(Map.of(0.0, 0.1, 3.0, 0.0, 5.0, 0.2)), saved());
    }

    /** The stretches are saved in the order of their times, so moving a row would not be kept. */
    @Test void thereAreNoButtonsToMoveARow() {
        List<String> buttons = buttonNames(table);

        assertFalse(buttons.contains("KeyTablePanel.upValueButton"), buttons.toString());
        assertFalse(buttons.contains("KeyTablePanel.downValueButton"), buttons.toString());
    }
}
