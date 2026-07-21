/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.exception.InvalidArgumentException;

import java.util.Random;

/**
 * Probability density function representing a complement: the value receives
 * a fraction {@code nu} of the probability mass left over by the rest of the
 * uncertain values in its row. Used to ensure that the row of a CPT keeps
 * adding up to one when several entries are uncertain.
 */
@ProbDensFunctionType(name = "Complement", isValidForNumeric = false, parameters = "nu")
public class ComplementFunction extends ProbDensFunction {
	private double nu;

	public ComplementFunction() {
		this.nu = 0;
	}

	public ComplementFunction(double nu) {
		this.nu = nu;
	}

	public ComplementFunction(ComplementFunction complementFunction) {
		super();
		this.nu = complementFunction.getNu();
	}

	public double getNu() {
		return nu;
	}
    
    @Override public void verifyParametersDomain(boolean isChanceVariable) {
        if (nu <= 0) {
            throw new InvalidArgumentException(nu, "nu", "should be a number bigger than 0");
        }
        ;
	}

	@Override public double[] getParameters() {
		return new double[] { nu };
	}

	@Override public void setParameters(double[] params) {
		nu = params[0];
	}

	@Override public double getMaximum() {
		return 1;
	}

	@Override public double getMean() {
		return nu;
	}

	@Override public double getSample(Random randomGenerator) {
		return nu;
	}

	@Override public double getVariance() {
		return 0;
	}

	@Override public double getMinimum() {
		return 0;
	}

	@Override public DomainInterval getInterval(double p) {
		return new DomainInterval(nu, nu);
	}

	@Override public ProbDensFunction copy() {
		return new ComplementFunction(this);
	}
}
