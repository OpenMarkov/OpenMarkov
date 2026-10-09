/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.sensitivityanalysis;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.sensitivityanalysis.dialog.MapDialog;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The scale of utilities of the map of two parameters. The chart does not accept a scale that begins and ends in
 * the same number.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheScaleOfAMapWithTheSameUtilityEverywhereTest {

    @Test void utilitiesThatDifferGiveTheirOwnRange() {
        assertArrayEquals(new double[]{ -2, 7 }, MapDialog.scaleRange(-2, 7));
    }

    @Test void theSameUtilityEverywhereGivesARangeAroundIt() {
        for (double utility : new double[]{ 7, 0, -7 }) {
            double[] range = MapDialog.scaleRange(utility, utility);

            assertTrue(range[0] < utility && utility < range[1], "utility " + utility);
        }
    }
}
