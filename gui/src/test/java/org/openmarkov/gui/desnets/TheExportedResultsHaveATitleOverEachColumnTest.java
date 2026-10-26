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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * «Export as CSV» of the results of a simulation of two options, two criteria and one series: the row of titles and
 * the row of the series have the same columns, the number of the series, and the mean and discounted mean of each
 * criterion for each option (error 103). A mean written with an exponent, or an option whose name starts with a
 * digit, keeps its own cell (error 252), and so does an option with a semicolon in its name (error 332).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheExportedResultsHaveATitleOverEachColumnTest {

    private static final EqualCriterion COST = new EqualCriterion("cost");
    private static final EqualCriterion EFFECTIVENESS = new EqualCriterion("effectiveness");

    /** The expected value of each column of the CSV, by its title. */
    private final Map<String, Double> expected = new HashMap<>();

    /**
     * The rows of the CSV of one series of a simulation of {@code options}. Each option has, in order, the mean and
     * the discounted mean of the cost and then of the effectiveness.
     */
    private String[] csvRows(String[] options, double[]... means) {
        List<EqualCriterion> criteria = List.of(COST, EFFECTIVENESS);
        Variable therapy = new Variable("Therapy", options);
        SimulationSummaryResults results = new SimulationSummaryResults(new InferenceOptions(), therapy, criteria);
        results.addEmptySeries();
        for (int i = 0; i < options.length; i++) {
            CriteriaValues values = new CriteriaValues(criteria);
            values.setValue(COST, means[i][0], means[i][1]);
            values.setValue(EFFECTIVENESS, means[i][2], means[i][3]);
            results.addCriteriaValues(0, therapy.getStates()[i], 0, values);
            expected.put("Mean cost for " + options[i], means[i][0]);
            expected.put("Disc. Mean cost for " + options[i], means[i][1]);
            expected.put("Mean effectiveness for " + options[i], means[i][2]);
            expected.put("Disc. Mean effectiveness for " + options[i], means[i][3]);
        }
        results.calculateStatisticalProperties();
        return DESResultsWindow.csv(results).split("\n");
    }

    /** Each cell of the series is the mean that its title names. */
    private void assertEachCellUnderItsTitle(String[] rows) {
        String[] titles = rows[0].split(",", -1);
        String[] series = rows[1].split(",", -1);
        assertEquals(titles.length, series.length);
        for (int column = 1; column < titles.length; column++) {
            assertEquals(expected.get(titles[column]), Double.parseDouble(series[column]), titles[column]);
        }
    }

    @Test
    void andTheFirstOneIsTheSeries() {
        String[] rows = csvRows(new String[] { "none", "drug" }, new double[] { 100, 90, 2, 2 },
                                new double[] { 100, 90, 2, 2 });

        String[] titles = rows[0].split(",", -1);
        String[] series = rows[1].split(",", -1);
        assertEquals("SERIES", titles[0]);
        assertEquals("0", series[0]);
        assertEquals(9, titles.length);
        assertEquals(9, series.length);
    }

    @Test
    void aMeanWrittenWithAnExponentKeepsItsCell() {
        String[] rows = csvRows(new String[] { "none", "drug" }, new double[] { 12345678.9, 11111111.01, 0.0005, 0.0005 },
                                new double[] { 100, 90, 2, 2 });

        assertEachCellUnderItsTitle(rows);
    }

    @Test
    void anOptionWhoseNameStartsWithADigitKeepsItsCell() {
        String[] rows = csvRows(new String[] { "none", "10 mg" }, new double[] { 100, 90, 2, 2 },
                                new double[] { 150, 140, 3, 3 });

        assertEachCellUnderItsTitle(rows);
    }

    @Test
    void anOptionWithASemicolonKeepsItsCells() {
        String[] rows = csvRows(new String[] { "none", "drug;10" }, new double[] { 100, 90, 2, 2 },
                                new double[] { 150, 140, 3, 3 });

        assertEachCellUnderItsTitle(rows);
    }
}
