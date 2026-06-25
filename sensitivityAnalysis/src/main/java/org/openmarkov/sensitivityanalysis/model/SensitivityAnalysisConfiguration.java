/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.model;

/**
 * Group of flags that models the analysis type
 *
 * @author jperez-martin
 */
public class SensitivityAnalysisConfiguration {
	private boolean isUnicriterion;
	private boolean isDeterministic;
	private boolean isBiaxial;
	private ParameterType parameterType;
	private boolean canBeGlobal;
	private boolean canBeDecision;

	public boolean isUnicriterion() {
		return isUnicriterion;
	}

	public void setIsUnicriterion(boolean isUnicriterion) {
		this.isUnicriterion = isUnicriterion;
	}

	public boolean isDeterministic() {
		return isDeterministic;
	}

	public void setIsDeterministic(boolean isDeterministic) {
		this.isDeterministic = isDeterministic;
	}

	public boolean isBiaxial() {
		return isBiaxial;
	}

	public void setIsBiaxial(boolean isBiaxial) {
		this.isBiaxial = isBiaxial;
	}

	public ParameterType getParameterType() {
		return this.parameterType;
	}

	public void setParameterType(ParameterType parameterType) {
		this.parameterType = parameterType;
	}

	public boolean isCanBeGlobal() {
		return canBeGlobal;
	}

	public void setCanBeGlobal(boolean canBeGlobal) {
		this.canBeGlobal = canBeGlobal;
	}

	public boolean isCanBeDecision() {
		return canBeDecision;
	}

	public void setCanBeDecision(boolean canBeDecision) {
		this.canBeDecision = canBeDecision;
	}

}
