/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.sensitivityanalysis;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.sensitivityanalysis.dialog.PlotDialog;
import org.openmarkov.sensitivityanalysis.dialog.TornadoSpiderDialog;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The bar of the tornado goes from the least to the greatest value of the sweep, and the vertical axis of «Plot» and
 * of the spider holds every value, whatever their sign.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheChartsOfASensitivityAnalysisHoldNegativeValuesTest {

    @Test void theBarOfTheTornado() {
        assertArrayEquals(new double[]{ -1010, -990 }, TornadoSpiderDialog.range(new double[]{ -1010, -1000, -990 }));
    }

    @ParameterizedTest
    @CsvSource({ "-1010, -990", "-7, -7", "0, 0", "-0.2, 0.2", "990, 1010" })
    void theVerticalAxis(double min, double max) {
        double[] axis = PlotDialog.axisRange(min, max);

        assertTrue(axis[0] < min && max < axis[1], axis[0] + " to " + axis[1]);
    }
}
