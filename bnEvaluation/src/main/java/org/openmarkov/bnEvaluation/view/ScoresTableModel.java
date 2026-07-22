/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.view;

import org.openmarkov.bnEvaluation.measures.ScoresRow;

import java.util.List;

/**
 * View adapter that renders the structured Scores rows of a {@code MeasuresSet} as a two-column
 * table. Section rows carry an empty value column, which {@link ScoresTableStyler} interprets as a
 * heading; data rows carry the measure value formatted with three decimals.
 *
 * @author Manuel Arias
 */
public final class ScoresTableModel extends ReadOnlyStringTableModel {

    public ScoresTableModel(List<ScoresRow> rows) {
        super(build(rows));
    }

    private static Grid build(List<ScoresRow> rows) {
        String[][] cells = new String[rows.size()][2];
        for (int i = 0; i < rows.size(); i++) {
            ScoresRow row = rows.get(i);
            if (row instanceof ScoresRow.Section section) {
                cells[i][0] = section.title();
                cells[i][1] = "";
            } else if (row instanceof ScoresRow.Data dataRow) {
                cells[i][0] = dataRow.label();
                cells[i][1] = String.format("%.3f", dataRow.value());
            }
        }
        return new Grid(new String[] { "", "" }, cells);
    }
}
