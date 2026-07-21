/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.util.List;

/**
 * Family of {@link UncertainValue}s whose probability density function is a
 * {@link ComplementFunction}. All members of the family share the same
 * remaining probability mass: each sampled value equals that mass weighted by
 * its {@code nu} parameter.
 */
public class ComplementFamily extends FamilyDistribution {

	/**
	 * Probability mass for this family
	 */
	private double probMass;

	public ComplementFamily(List<UncertainValue> uncertainValues) {
		super(filterByFunction(ComplementFunction.class, uncertainValues));
	}

	public ComplementFamily() {
		// super(TypeProbDensityFunction.COMPLEMENT);
	}

	public double getProbMass() {
		return probMass;
	}

	public void setProbMass(double probMass) {
		this.probMass = probMass;
	}

	@Override public double[] getMean() {

		int sizeFamily = family.size();
		double[] nu = new double[sizeFamily];

		for (int i = 0; i < sizeFamily; i++) {
			nu[i] = ((ComplementFunction) (family.get(i).getProbDensFunction())).getNu();
		}
		return Tools.normalize(nu, probMass);
	}

	public double[] getSample() {

		int sizeFamily = family.size();
		double[] nu = new double[sizeFamily];

		for (int i = 0; i < sizeFamily; i++) {
			nu[i] = ((ComplementFunction) (family.get(i).getProbDensFunction())).getNu();
		}
		return Tools.normalize(nu, probMass);
	}

}
