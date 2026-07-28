/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.Random;

/**
 * Pseudo-random number generator based on Marsaglia's XORShift algorithm.
 * It is markedly faster than {@link Random} and good enough for the
 * Monte-Carlo simulations performed in sensitivity analysis. Note that the
 * implementation is not thread-safe.
 */
@SuppressWarnings("serial") public class XORShiftRandom extends Random {

	private long seed = System.nanoTime();


	public XORShiftRandom() {
	}

    @Override
    public void setSeed(long seed) {
        // Zero is the classic fixed point of the XORShift algorithm: the shifts
        // keep the state at zero forever, so every draw comes out 0.0 and the
        // distributions sampled by inversion degenerate (an Exponential answers
        // 0, a Uniform its minimum, a standard Normal falls into log(0)). Zero
        // is also the most natural seed a caller can pick, so it is mapped to a
        // fixed non-zero constant: seeding with 0 stays reproducible, it just
        // is not the degenerate generator.
        this.seed = (seed == 0) ? 0x9E3779B97F4A7C15L : seed;
    }

    @Override protected int next(int nbits) {
		// TODO N.B. Not thread-safe!
		long x = this.seed;
		x ^= (x << 21);
		x ^= (x >>> 35);
		x ^= (x << 4);
		this.seed = x;
		x &= ((1L << nbits) - 1);
		return (int) x;
	}
}
