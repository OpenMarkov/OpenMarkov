/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.desnets;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.model.network.EqualCriterion;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The window of results of a simulation gives the columns of each criterion, one or two depending on its discount,
 * in the order of the criteria of the network, and reads their values from this text in the same order.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheResultsOfASimulationFollowTheOrderOfTheCriteriaTest {

    @ParameterizedTest
    @CsvSource({ "cost, effectiveness", "effectiveness, cost", "Cost, Effectiveness", "Effectiveness, Cost" })
    void whateverTheirNames(String first, String second) {
        CriteriaValues values = new CriteriaValues(List.of(new EqualCriterion(first), new EqualCriterion(second)));

        String[] parts = values.toString().split(";");

        assertEquals(List.of(first, second), List.of(parts[0], parts[5]));
    }
}
