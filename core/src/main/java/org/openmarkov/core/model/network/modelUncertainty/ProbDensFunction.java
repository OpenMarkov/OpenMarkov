/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import org.openmarkov.core.developmentStaticAnalysis.requirements.ImplementationRequirements;
import org.openmarkov.core.developmentStaticAnalysis.requirements.RequiredConstructor;
import org.openmarkov.core.exception.InvalidArgumentException;

import java.util.Random;

/**
 * Abstract base for every probability density function used to express model
 * uncertainty over a CPT entry or a numeric parameter. Concrete subclasses must
 * declare a {@link ProbDensFunctionType} annotation, expose a no-argument
 * constructor (enforced by {@link ImplementationRequirements}) and implement
 * parameter handling, validation, mean, sample and interval computation.
 */
@ImplementationRequirements(requiresOneOfTheseConstructors = @RequiredConstructor({}))
public abstract class ProbDensFunction {
	public abstract double[] getParameters();

	public abstract void setParameters(double[] args);

	//For Univariate
	public void verifyParameters(double[] parameters) {
		throw new InvalidArgumentException(parameters, "parameters", "verifyParameters is not implemented in " + this.getClass().getName());
	}
    
    public abstract void verifyParametersDomain(boolean isChanceVariable);

	public abstract double getMean();

	public final double getStandardDeviation() {
		return Math.sqrt(getVariance());
	}

	public abstract double getVariance();

	public abstract double getMaximum();

	public abstract double getMinimum();

	public abstract double getSample(Random randomGenerator);

	@Override public String toString() {
        String out = "";
		ProbDensFunctionType probDensAnnotation = getClass().getAnnotation(ProbDensFunctionType.class);
		if (probDensAnnotation != null) {
            out += probDensAnnotation.name() + " :";
		}
		for (double parameter : getParameters()) {
            out += parameter + " ";
		}
        return out;
	}

	public abstract DomainInterval getInterval(double p);

	public abstract ProbDensFunction copy();
}
