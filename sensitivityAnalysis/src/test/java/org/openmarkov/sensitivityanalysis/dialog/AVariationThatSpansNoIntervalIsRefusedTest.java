/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.sensitivityanalysis.dialog;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType.POPP;
import static org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType.PORV;
import static org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType.RORV;
import static org.openmarkov.core.model.network.modelUncertainty.DeterministicAxisVariationType.UDIN;

/**
 * A variation of 0 %, or an interval whose ends are equal or reversed, left the plots of the sensitivity analysis
 * without an axis, and they ended in an unexpected error (error 256): the check that «OK» makes refuses them.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AVariationThatSpansNoIntervalIsRefusedTest {

    private static AxisVariation variation(DeterministicAxisVariationType type, double value) {
        AxisVariation variation = new AxisVariation();
        variation.setVariationType(type);
        variation.setVariationValue(value);
        return variation;
    }

    private static AxisVariation interval(double lower, double upper) {
        AxisVariation variation = new AxisVariation();
        variation.setVariationType(UDIN);
        variation.setVariationBounds(new double[]{lower, upper});
        return variation;
    }

    @Test void variationsWithoutWidthAreRefused() {
        assertAll(() -> assertNotNull(AxisVariationPanel.problemWith(variation(PORV, 0))),
                  () -> assertNotNull(AxisVariationPanel.problemWith(variation(PORV, -25))),
                  () -> assertNotNull(AxisVariationPanel.problemWith(variation(PORV, Double.NaN))),
                  () -> assertNotNull(AxisVariationPanel.problemWith(variation(RORV, 1))),
                  () -> assertNotNull(AxisVariationPanel.problemWith(variation(RORV, 0.5))),
                  () -> assertNotNull(AxisVariationPanel.problemWith(variation(POPP, 0))),
                  () -> assertNotNull(AxisVariationPanel.problemWith(variation(POPP, 150))),
                  () -> assertNotNull(AxisVariationPanel.problemWith(interval(0.5, 0.5))),
                  () -> assertNotNull(AxisVariationPanel.problemWith(interval(1, 0))),
                  () -> assertNotNull(AxisVariationPanel.problemWith(interval(Double.NaN, 1))),
                  () -> assertNotNull(AxisVariationPanel.problemWith(interval(0, Double.POSITIVE_INFINITY))));
    }

    @Test void variationsWithWidthAreAccepted() {
        assertAll(() -> assertNull(AxisVariationPanel.problemWith(variation(PORV, 25))),
                  () -> assertNull(AxisVariationPanel.problemWith(variation(RORV, 1.25))),
                  () -> assertNull(AxisVariationPanel.problemWith(variation(POPP, 90))),
                  () -> assertNull(AxisVariationPanel.problemWith(variation(POPP, 100))),
                  () -> assertNull(AxisVariationPanel.problemWith(interval(0, 1))),
                  () -> assertNull(AxisVariationPanel.problemWith(interval(-2, 3))));
    }

    @Test void theProblemNamesTheTypeOfVariation() {
        assertEquals("SensitivityAnalysis.Axis.Invalid.PORV", AxisVariationPanel.problemWith(variation(PORV, 0)));
        assertEquals("SensitivityAnalysis.Axis.Invalid.UDIN", AxisVariationPanel.problemWith(interval(1, 0)));
    }

    @Test void textThatIsNotANumberIsReadAsNaN() {
        assertAll(() -> assertEquals(Double.NaN, AxisVariationPanel.numberOf("abc")),
                  () -> assertEquals(Double.NaN, AxisVariationPanel.numberOf("0,5")),
                  () -> assertEquals(Double.NaN, AxisVariationPanel.numberOf("")),
                  () -> assertEquals(0.5, AxisVariationPanel.numberOf(" 0.5")),
                  () -> assertEquals(25.0, AxisVariationPanel.numberOf(25.0)));
    }
}
