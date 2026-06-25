/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.bnEvaluation.measures;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class MeasureMatrixIndicatorsTest {

    @Test
    void perfectClassifierYieldsAccuracyOneAndUnitTpRates() {
        // Diagonal matrix → every prediction is correct.
        int[][] matrix = {
                {30, 0, 0},
                {0, 40, 0},
                {0, 0, 30}
        };

        MeasureMatrixIndicators indicators = new MeasureMatrixIndicators(matrix, 100);

        assertThat(indicators.getAccuracy()).isEqualTo(1.0);
        assertThat(indicators.getTpRates()[0]).isEqualTo(1.0);
        assertThat(indicators.getTpRates()[1]).isEqualTo(1.0);
        assertThat(indicators.getTpRates()[2]).isEqualTo(1.0);
        assertThat(indicators.getFpRates()[0]).isEqualTo(0.0);
        assertThat(indicators.getPrecisions()[0]).isEqualTo(1.0);
        assertThat(indicators.getFMeasures()[0]).isEqualTo(1.0);
    }

    @Test
    void binaryClassifierIndicatorsMatchTextbookFormulas() {
        // 2-class matrix:
        // TP_pos = 40, FN_pos = 10  (real "pos" class)
        // FP_pos = 20, TN_pos = 30  (real "neg" class)
        int[][] matrix = {
                {40, 10},
                {20, 30}
        };

        MeasureMatrixIndicators indicators = new MeasureMatrixIndicators(matrix, 100);

        // TP rate (row 0) = 40 / (40+10) = 0.8
        assertThat(indicators.getTpRates()[0]).isCloseTo(0.8, within(1e-9));
        // FP rate (row 0) = 20 / (50) = 0.4 (because column-sum minus diagonal over numCases - row total)
        assertThat(indicators.getFpRates()[0]).isCloseTo(0.4, within(1e-9));
        // Precision (row 0) = 40 / (40+20) = 0.6667
        assertThat(indicators.getPrecisions()[0]).isCloseTo(2.0 / 3.0, within(1e-9));
        // F1 = 2 · P · R / (P + R) ≈ 0.7273
        double precision = 2.0 / 3.0;
        double recall = 0.8;
        double expectedF1 = 2 * precision * recall / (precision + recall);
        assertThat(indicators.getFMeasures()[0]).isCloseTo(expectedF1, within(1e-9));
        // Accuracy = (40 + 30) / 100 = 0.7
        assertThat(indicators.getAccuracy()).isCloseTo(0.7, within(1e-9));
    }
}
