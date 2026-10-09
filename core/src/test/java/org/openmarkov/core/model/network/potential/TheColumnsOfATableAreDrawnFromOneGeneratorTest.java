/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.BetaFunction;
import org.openmarkov.core.model.network.modelUncertainty.ComplementFunction;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.modelUncertainty.XORShiftRandom;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P(X | Y) with two columns; in each, the first cell follows a Beta(3, 7) and the second is its complement.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheColumnsOfATableAreDrawnFromOneGeneratorTest {

    private UncertainTablePotential table;

    @BeforeEach void build() {
        Variable x = new Variable("X", "x0", "x1");
        Variable y = new Variable("Y", "y0", "y1");
        table = new UncertainTablePotential(List.of(x, y), PotentialRole.CONDITIONAL_PROBABILITY,
                                            new double[]{ 0.3, 0.7, 0.3, 0.7 });
        table.setUncertainValues(new UncertainValue[]{
                new UncertainValue(new BetaFunction(3, 7)), new UncertainValue(new ComplementFunction(1)),
                new UncertainValue(new BetaFunction(3, 7)), new UncertainValue(new ComplementFunction(1)) });
    }

    private static Random withSeed(long seed) {
        Random randomGenerator = new XORShiftRandom();
        randomGenerator.setSeed(seed);
        return randomGenerator;
    }

    @Test void theSameSeedGivesTheSameTable() {
        double[] first = ((TablePotential) table.sample(withSeed(2026))).getValues();
        double[] second = ((TablePotential) table.sample(withSeed(2026))).getValues();

        assertArrayEquals(first, second);
    }

    /** The correlation of two independent columns stays within three standard errors of zero. */
    @Test void theTwoColumnsAreIndependent() {
        int draws = 200_000;
        Random randomGenerator = withSeed(2026);
        double sumFirst = 0, sumSecond = 0, sumFirstSquared = 0, sumSecondSquared = 0, sumProduct = 0;
        for (int draw = 0; draw < draws; draw++) {
            double[] sampled = ((TablePotential) table.sample(randomGenerator)).getValues();
            sumFirst += sampled[0];
            sumSecond += sampled[2];
            sumFirstSquared += sampled[0] * sampled[0];
            sumSecondSquared += sampled[2] * sampled[2];
            sumProduct += sampled[0] * sampled[2];
        }
        double covariance = sumProduct / draws - sumFirst / draws * sumSecond / draws;
        double varianceFirst = sumFirstSquared / draws - Math.pow(sumFirst / draws, 2);
        double varianceSecond = sumSecondSquared / draws - Math.pow(sumSecond / draws, 2);
        double correlation = covariance / Math.sqrt(varianceFirst * varianceSecond);

        assertTrue(Math.abs(correlation) < 3 / Math.sqrt(draws), "correlation " + correlation);
    }
}
