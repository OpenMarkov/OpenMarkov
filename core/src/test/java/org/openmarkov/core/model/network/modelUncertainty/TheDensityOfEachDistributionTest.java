/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The density of each distribution that the relation of a numeric node can have, worked out by hand, with the
 * parameters in the order the relation gives them.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheDensityOfEachDistributionTest {

    @ParameterizedTest(name = "{0} {1} at {2}")
    @CsvSource(delimiter = ';', value = {
            // 1 / sqrt(2 pi)
            "Normal; 5 1; 5; 0.3989422804014327",
            "NormalMuStandardFunction; 5 1; 5; 0.3989422804014327",
            "LogNormal; 0 1; 1; 0.3989422804014327",
            // x e^(-x/theta) / theta^2, with k = 2 and theta = 3
            "Gamma; 2 3; 3; 0.12262648039048077",
            // mean 6 and variance 18 are k = 2 and theta = 3
            "Gamma-mv; 6 4.242640687119285; 3; 0.12262648039048077",
            // 6 x (1 - x)
            "Beta; 2 2; 0.5; 1.5",
            "BetaAlphaNFunction; 2 4; 0.5; 1.5",
            // minimum 0, maximum 4, mode 1
            "Triangular; 0 4 1; 1; 0.5",
            "Triangular; 0 4 1; 2; 0.3333333333333333",
            "Triangular; 0 4 1; 5; 0",
            "Range; 2 6; 3; 0.25",
            "Range; 2 6; 7; 0",
            // distributions of probabilities, not of a numeric value
            "Dirichlet; 1; 0.5; NaN",
            "Complement; 1; 0.5; NaN" })
    void density(String distribution, String parameters, double x, double density) {
        double[] values = Arrays.stream(parameters.split(" ")).mapToDouble(Double::parseDouble).toArray();
        ProbDensFunction function = ProbDensFunctionManager.getUniqueInstance().newInstance(distribution, values);
        assertEquals(density, function.getDensity(x), 1e-12);
    }
}
