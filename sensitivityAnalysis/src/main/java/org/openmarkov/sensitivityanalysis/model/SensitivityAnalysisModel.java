/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.model;

import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.AxisVariation;
import org.openmarkov.core.model.network.modelUncertainty.UncertainParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Parameters of the sensitivity analysis
 *
 * @author jperez-martin
 */
public class SensitivityAnalysisModel extends Observable {

	/**
	 * Type of analysis
	 */
	private AnalysisType analysisType;

	/**
	 * Selected X axis parameters
	 */
	private List<UncertainParameter> selectedUncertainParametersXAxis;

	/**
	 * Selected Y axis parameters
	 */
	private List<UncertainParameter> selectedUncertainParametersYAxis;

	/**
	 * Variation for X axis
	 */
	private AxisVariation horizontalAxisVariation;

	/**
	 * Variation for Y axis
	 */
	private AxisVariation verticalAxisVariation;

	/**
	 * Configuration of the analysis type
	 */
	private SensitivityAnalysisConfiguration configuration;

	/**
	 * Selected scope type
	 */
	private ScopeType scopeType;

	/**
	 * Selected decision variable
	 */
	private Variable decisionVariable;

	/**
	 * Selected scenario
	 */
	private List<Finding> selectedScenario;

	/**
	 * Selected flag
	 */
	private boolean throwErrorMessageIfProbAboveOne;

	/**
	 * Selected number of iterations/simulations
	 */
	private int numberOfIterationsSimulations;

	/**
	 * Select if the analysis must use more than one thread
	 */
	private boolean multithreading;

	/**
	 * Constructor
	 */
	public SensitivityAnalysisModel() {
		analysisType = AnalysisType.TORNADO_SPIDER;
		selectedUncertainParametersXAxis = new ArrayList<>();
		selectedUncertainParametersYAxis = new ArrayList<>();
		horizontalAxisVariation = new AxisVariation();
		verticalAxisVariation = new AxisVariation();
		scopeType = ScopeType.GLOBAL;
		selectedScenario = new ArrayList<>();
		throwErrorMessageIfProbAboveOne = false;
		multithreading = true;
	}

	public AnalysisType getAnalysisType() {
		return analysisType;
	}

	/**
	 * Set the analysis type and notify to observers
	 *
	 * @param analysisType analysis type
	 */
	public void setAnalysisType(AnalysisType analysisType) {
		this.analysisType = analysisType;
		this.setChanged();
		this.notifyObservers(analysisType);
	}

	public List<UncertainParameter> getSelectedUncertainParametersXAxis() {
		return selectedUncertainParametersXAxis;
	}

	/**
	 * Set the selected uncertain parameters of X axis and notify to observers
	 *
	 * @param selectedUncertainParametersXAxis Selected uncertain parameters of X axis
	 */
	public void setSelectedUncertainParametersXAxis(List<UncertainParameter> selectedUncertainParametersXAxis) {
		this.selectedUncertainParametersXAxis = selectedUncertainParametersXAxis;
		this.setChanged();
		this.notifyObservers(selectedUncertainParametersXAxis);
	}

	public List<UncertainParameter> getSelectedUncertainParametersYAxis() {
		return selectedUncertainParametersYAxis;
	}

	/**
	 * Set the selected uncertainty parameters of Y axis and notify to observers
	 *
	 * @param selectedUncertainParametersYAxis Selected uncertain parameters of Y axis
	 */
	public void setSelectedUncertainParametersYAxis(List<UncertainParameter> selectedUncertainParametersYAxis) {
		this.selectedUncertainParametersYAxis = selectedUncertainParametersYAxis;
		this.setChanged();
		this.notifyObservers(selectedUncertainParametersYAxis);
	}

	public AxisVariation getHorizontalAxisVariation() {
		return horizontalAxisVariation;
	}

	public void setHorizontalAxisVariation(AxisVariation horizontalAxisVariation) {
		this.horizontalAxisVariation = horizontalAxisVariation;
	}

	public AxisVariation getVerticalAxisVariation() {
		return verticalAxisVariation;
	}

	public void setVerticalAxisVariation(AxisVariation verticalAxisVariation) {
		this.verticalAxisVariation = verticalAxisVariation;
	}

	public SensitivityAnalysisConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(SensitivityAnalysisConfiguration configuration) {
		this.configuration = configuration;
	}

	public ScopeType getScopeType() {
		return scopeType;
	}

	public void setScopeType(ScopeType scopeType) {
		this.scopeType = scopeType;
	}

	public Variable getDecisionVariable() {
		return decisionVariable;
	}

	public void setDecisionVariable(Variable decisionVariable) {
		this.decisionVariable = decisionVariable;
	}

	public List<Finding> getSelectedScenario() {
		return selectedScenario;
	}

	public void setSelectedScenario(List<Finding> selectedScenario) {
		this.selectedScenario = selectedScenario;
	}

	public boolean isThrowErrorMessageIfProbAboveOne() {
		return throwErrorMessageIfProbAboveOne;
	}

	public void setThrowErrorMessageIfProbAboveOne(boolean throwErrorMessageIfProbAboveOne) {
		this.throwErrorMessageIfProbAboveOne = throwErrorMessageIfProbAboveOne;
	}

	public int getNumberOfIterationsSimulations() {
		return numberOfIterationsSimulations;
	}

	/**
	 * Set the number of iterations/simulations and notify to observers
	 *
	 * @param numberOfIterationsSimulations number of iterations/simulatios
	 */
	public void setNumberOfIterationsSimulations(int numberOfIterationsSimulations) {
		this.numberOfIterationsSimulations = numberOfIterationsSimulations;
		this.setChanged();
		this.notifyObservers(numberOfIterationsSimulations);
	}

	public boolean isMultithreading() {
		return multithreading;
	}

	public void setMultithreading(boolean multithreading) {
		this.multithreading = multithreading;
	}
}
