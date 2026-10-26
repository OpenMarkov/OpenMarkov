/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.desnets;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EqualCriterion;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The table of the results of a simulation of two options, two discounted criteria and one series: each cell has the
 * mean or the discounted mean of the option and the criterion over it, also when the name of an option has a semicolon
 * (error 332).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheResultsTableShowsEachMeanUnderItsOptionTest {

    private static final EqualCriterion COST = new EqualCriterion("cost");
    private static final EqualCriterion EFFECTIVENESS = new EqualCriterion("effectiveness");

    /** The texts of the cells of {@code html} that match {@code cell}. */
    private static List<String> texts(String html, String cell) {
        Matcher matcher = Pattern.compile(cell).matcher(html);
        return matcher.results().map(result -> result.group(1)).toList();
    }

    @Test
    void anOptionWithASemicolonKeepsItsCells() {
        String[] options = { "none", "drug;10" };
        int[][] means = { { 100, 90, 2, 1 }, { 150, 140, 3, 4 } };
        List<EqualCriterion> criteria = List.of(COST, EFFECTIVENESS);
        Variable therapy = new Variable("Therapy", options);
        SimulationSummaryResults results = new SimulationSummaryResults(new InferenceOptions(), therapy, criteria);
        results.addEmptySeries();
        for (int i = 0; i < options.length; i++) {
            CriteriaValues values = new CriteriaValues(criteria);
            values.setValue(COST, means[i][0], means[i][1]);
            values.setValue(EFFECTIVENESS, means[i][2], means[i][3]);
            results.addCriteriaValues(0, therapy.getStates()[i], 0, values);
        }
        results.calculateStatisticalProperties();

        String table = DESResultsWindow.generateTable(results, new boolean[] { true, true });

        List<String> optionTitles = texts(table, "<th colspan=\"4\">Therapy: ([^<]*)</th>");
        assertEquals(2, optionTitles.size());
        assertEquals(Set.of(options), Set.copyOf(optionTitles));
        assertEquals(List.of("cost", "effectiveness", "cost", "effectiveness"),
                     texts(table, "<th colspan=\"2\">([^<]*)</th>"));
        List<String> cells = texts(table, "<td [^>]*>([^<]*)</td>");
        assertEquals(1 + 2 * 4, cells.size());
        assertEquals("0", cells.get(0));
        for (int column = 0; column < optionTitles.size(); column++) {
            int option = List.of(options).indexOf(optionTitles.get(column));
            for (int value = 0; value < 4; value++) {
                assertEquals(String.valueOf(means[option][value]), cells.get(1 + 4 * column + value),
                             optionTitles.get(column) + ", value " + value);
            }
        }
    }
}
