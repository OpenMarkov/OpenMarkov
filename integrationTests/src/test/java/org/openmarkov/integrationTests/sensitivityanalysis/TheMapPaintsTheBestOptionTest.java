/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.sensitivityanalysis;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.sensitivityanalysis.dialog.MapDialog;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The option that a cell of the map of two parameters paints, from the utilities of the options in that cell: its
 * index, or −1 when the greatest utility is shared.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheMapPaintsTheBestOptionTest {

    @ParameterizedTest
    @CsvSource({ "'1 5 3', 1", "'1 5 1', 1", "'1 1 5', 2", "'5 5 3', -1", "'3 5 5', -1", "'5 1 3', 0", "'1 2', 1",
            "'2 1', 0", "'2 2', -1" })
    void ofACell(String utilities, int bestOption) {
        double[] values = Arrays.stream(utilities.split(" ")).mapToDouble(Double::parseDouble).toArray();

        assertEquals(bestOption, MapDialog.bestOption(values));
    }
}
