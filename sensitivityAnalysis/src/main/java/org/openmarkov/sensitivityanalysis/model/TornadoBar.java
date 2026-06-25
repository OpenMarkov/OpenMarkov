/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.model;

import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;

/**
 * Represents a single bar in a tornado diagram, storing the uncertain parameter
 * and the minimum/maximum values obtained by varying it. Bars are ordered by
 * their variation range (largest first).
 *
 * @author jperez-martin
 */
public class TornadoBar implements Comparable<TornadoBar> {

	/**
     * {@code UncertainParameter }
	 */
	private UncertainParameter uncertainParameter;

	/**
	 * Minimum value obtained
	 */
	private double minValue;

	/**
	 * Maximum value obtained
	 */
	private double maxValue;

	public TornadoBar(UncertainParameter uncertainParameter, double minValue, double maxValue) {
		this.uncertainParameter = uncertainParameter;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public UncertainParameter getUncertainParameter() {
		return uncertainParameter;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	@Override public int compareTo(TornadoBar otherBar) {
		double variationRange = Math.abs(maxValue - minValue);
		double otherBarVariationRange = Math.abs(otherBar.maxValue - otherBar.minValue);

		// Checks what TornadoBar has the great variation range
        return Double.compare(otherBarVariationRange, variationRange);
	}
}
