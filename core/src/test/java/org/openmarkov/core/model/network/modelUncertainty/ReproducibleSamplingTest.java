/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.modelUncertainty;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sampling must come from the generator it is handed, and the beta parameters must be checked the way
 * round their own message says.
 * <p>
 * A probabilistic sensitivity analysis is only worth publishing if it can be run again and give the
 * same answer, and the way to arrange that is to seed the generator. That only works if the generator
 * is the one used: the gamma sampler ignored the one it was given and drew from Colt's static one
 * instead, so two runs seeded alike came out different and no published analysis could be reproduced.
 * Nor was it safe to share between threads, the static generator having no owner.
 * <p>
 * Beta and Dirichlet reach the same code - a Dirichlet samples gammas, and a beta samples a Dirichlet -
 * so the three were affected by one line.
 *
 * @author Manuel Arias
 */
public class ReproducibleSamplingTest {

    private static final long SEED = 20260727L;

    @Test public void twoGammaSamplesFromEquallySeededGeneratorsAgree() {
        GammaFunction gamma = new GammaFunction(2.0, 3.0);

        double first = gamma.getSample(new Random(SEED));
        double second = gamma.getSample(new Random(SEED));

        assertEquals(first, second, 0.0, "the same seed must give the same sample");
    }

    @Test public void aDirichletSamplesFromTheGeneratorItIsGiven() {
        DirichletFunction dirichlet = new DirichletFunction(3.0);

        assertEquals(dirichlet.getSample(new Random(SEED)), dirichlet.getSample(new Random(SEED)), 0.0);
    }

    @Test public void aBetaSamplesFromTheGeneratorItIsGiven() {
        BetaFunction beta = new BetaFunction(2.0, 5.0);

        assertEquals(beta.getSample(new Random(SEED)), beta.getSample(new Random(SEED)), 0.0);
    }

    /** And different seeds must still give different samples, or the fix would be a constant. */
    @Test public void differentSeedsGiveDifferentSamples() {
        GammaFunction gamma = new GammaFunction(2.0, 3.0);

        assertNotEquals(gamma.getSample(new Random(1L)), gamma.getSample(new Random(2L)));
    }

    /** The sample still has to be a gamma: positive, and near the mean over many draws. */
    @Test public void theSamplesStillFollowTheDistribution() {
        double shape = 2.0;
        double scale = 3.0;
        GammaFunction gamma = new GammaFunction(shape, scale);
        Random random = new Random(SEED);

        double total = 0;
        int draws = 20000;
        for (int i = 0; i < draws; i++) {
            double sample = gamma.getSample(random);
            assertTrue(sample >= 0.0, "a gamma sample cannot be negative: " + sample);
            total += sample;
        }

        assertEquals(shape * scale, total / draws, 0.2, "the mean of the samples is off");
    }

    // ------------------------------------------------- the check that was the wrong way round

    /**
     * {@code BetaAlphaNFunction} is written in terms of alpha and N, where N counts the observations
     * and alpha how many of them were successes, so N is the larger of the two. The check threw when
     * that held and let the impossible case through - while saying "N should be greater than alpha".
     */
    @Test public void nGreaterThanAlphaIsAccepted() {
        BetaAlphaNFunction function = new BetaAlphaNFunction(2.0, 5.0);

        assertDoesNotThrow(() -> function.verifyParameters(new double[]{2.0, 5.0}));
    }

    @Test public void nSmallerThanAlphaIsRejected() {
        BetaAlphaNFunction function = new BetaAlphaNFunction(2.0, 5.0);

        assertThrows(InvalidArgumentException.class, () -> function.verifyParameters(new double[]{5.0, 2.0}));
    }
}
