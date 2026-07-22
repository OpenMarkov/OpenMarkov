/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.view;

import javax.swing.table.AbstractTableModel;

/**
 * Read-only {@link javax.swing.table.TableModel} backed by a fixed grid of pre-formatted strings.
 *
 * <p>It is the common base of the view adapters that turn the {@code measures} model classes into
 * Swing tables ({@link ScoresTableModel}, {@link ConfusionMatrixTableModel},
 * {@link IndicatorsTableModel}, {@link IndividualProbabilityTableModel}). Keeping the string layout
 * here — and out of the model — is what lets the model classes stay free of any {@code javax.swing}
 * dependency (model-view separation). Results are display-only, so cells are never editable.</p>
 *
 * @author Manuel Arias
 */
public abstract class ReadOnlyStringTableModel extends AbstractTableModel {

    /** A ready-to-display table: its column headers and its already-formatted cell strings. */
    protected record Grid(String[] columnNames, String[][] cells) {
    }

    private final String[] columnNames;
    private final String[][] cells;

    protected ReadOnlyStringTableModel(Grid grid) {
        this.columnNames = grid.columnNames();
        this.cells = grid.cells();
    }

    @Override
    public int getRowCount() {
        return cells.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return cells[rowIndex][columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
