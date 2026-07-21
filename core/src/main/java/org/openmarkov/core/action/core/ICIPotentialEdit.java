/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.core;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.canonical.ICIPotential;
import org.openmarkov.core.action.base.PNEdit;

/**
 * Edit that modifies the noisy or leaky parameters of an {@link ICIPotential}
 * (e.g., noisy-OR, noisy-MAX canonical models).
 */
@SuppressWarnings("serial") public class ICIPotentialEdit extends PNEdit {
    
    private final ICIPotential potential;
	private Variable variable = null;
	private double[] noisyParameters = null;
	private double[] oldNoisyParameters = null;
	private double[] leakyParameters = null;
	private double[] oldLeakyParameters = null;
	private final boolean isNoisyParameter;

	public ICIPotentialEdit(ProbNet probNet, ICIPotential potential, Variable variable, double[] noisyParameters) {
		super(probNet);
		this.potential = potential;
		this.variable = variable;
		this.oldNoisyParameters = potential.getNoisyParameters(variable);
		this.noisyParameters = noisyParameters;
		this.isNoisyParameter = true;
	}

	public ICIPotentialEdit(ProbNet probNet, ICIPotential potential, double[] leakyParameters) {
		super(probNet);
		this.potential = potential;
		this.oldLeakyParameters = potential.getLeakyParameters();
		this.leakyParameters = leakyParameters;
		this.isNoisyParameter = false;
	}
	
	@Override protected void doEdit() {
		if (isNoisyParameter) {
			potential.setNoisyParameters(variable, noisyParameters);
		} else {
			potential.setLeakyParameters(leakyParameters);
		}
	}
    
    @Override public void undo() {
		super.undo();
		if (isNoisyParameter) {
			potential.setNoisyParameters(variable, oldNoisyParameters);
		} else {
			potential.setLeakyParameters(oldLeakyParameters);
		}
	}

	public ICIPotential getPotential() {
		return potential;
	}

	public Variable getVariable() {
		return variable;
	}

	public double[] getNoisyParameters() {
		return noisyParameters;
	}

	public double[] getLeakyParameters() {
		return leakyParameters;
	}

	public boolean isNoisyParameter() {
		return isNoisyParameter;
	}

}
