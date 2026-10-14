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
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * «Export as CSV» of the results of a simulation of two options, two criteria and one series: the row of titles and
 * the row of the series have the same columns, the number of the series, and the mean and discounted mean of each
 * criterion for each option.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheExportedResultsHaveATitleOverEachColumnTest {

    @Test
    void andTheFirstOneIsTheSeries() {
        EqualCriterion cost = new EqualCriterion("cost");
        EqualCriterion effectiveness = new EqualCriterion("effectiveness");
        List<EqualCriterion> criteria = List.of(cost, effectiveness);
        Variable therapy = new Variable("Therapy", "none", "drug");
        SimulationSummaryResults results = new SimulationSummaryResults(new InferenceOptions(), therapy, criteria);
        results.addEmptySeries();
        for (State option : therapy.getStates()) {
            CriteriaValues values = new CriteriaValues(criteria);
            values.setValue(cost, 100, 90);
            values.setValue(effectiveness, 2, 2);
            results.addCriteriaValues(0, option, 0, values);
        }
        results.calculateStatisticalProperties();

        String[] rows = DESResultsWindow.csv(results.toString(), List.of("none", "drug"),
                                             List.of("cost", "effectiveness")).split("\n");

        String[] titles = rows[0].split(",", -1);
        String[] series = rows[1].split(",", -1);
        assertEquals("SERIES", titles[0]);
        assertEquals("0", series[0]);
        assertEquals(9, titles.length);
        assertEquals(9, series.length);
    }
}
