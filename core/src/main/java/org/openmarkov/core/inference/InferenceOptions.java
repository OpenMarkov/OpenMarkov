/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Stores attributes for use in {@code SimpleMarkovEvaluation}
 */
public class InferenceOptions {

	// Attributes

	/** */
	public Variable simulationIndexVariable;

	/** */
	public final double discountRate = 1.0;

	public ProbNet probNet;

	private MulticriteriaOptions multiCriteriaOptions;

	private TemporalOptions temporalOptions;
	
	private MonteCarloOptions monteCarloOptions;
	
	// Constructor
	public InferenceOptions(ProbNet probNet, Variable simulationIndexVariable) {
		this.probNet = probNet;
		this.simulationIndexVariable = simulationIndexVariable;
	}

	public InferenceOptions() {
		this.multiCriteriaOptions = new MulticriteriaOptions();
		this.temporalOptions = new TemporalOptions();
		this.monteCarloOptions = new MonteCarloOptions();
	}

	public InferenceOptions(InferenceOptions inferenceOptions) {
		this.multiCriteriaOptions = new MulticriteriaOptions(inferenceOptions.getMultiCriteriaOptions());
		this.temporalOptions = new TemporalOptions(inferenceOptions.getTemporalOptions());
		this.setMonteCarloOptions(new MonteCarloOptions(inferenceOptions.getMonteCarloOptions()));
	}

	/**
	 * Sets the attribute simulationIndexVariable and returns the variable.
	 * If numSimulations = 0, it returns null.
	 * @param numSimulations Number of simulations
	 * @return Variable
	 */
	public static Variable setNumSimulations(int numSimulations) {
		Variable newVariable;
		if (numSimulations == 0) {
			newVariable = null;
		} else {
			newVariable = new Variable("###SimulationIndexes###", numSimulations);
		}
		return newVariable;
	}

	public MulticriteriaOptions getMultiCriteriaOptions() {
		return multiCriteriaOptions;
	}

	public void setMultiCriteriaOptions(MulticriteriaOptions multiCriteriaOptions) {
		this.multiCriteriaOptions = multiCriteriaOptions;
	}

	public TemporalOptions getTemporalOptions() {
		return temporalOptions;
	}

	// Methods

	public void setTemporalOptions(TemporalOptions temporalOptions) {
		this.temporalOptions = temporalOptions;
	}

	/**
	 * Prints decision criteria, simulation indices and discount rate
	 */
	public String toString() {
		String out = "";
		if (simulationIndexVariable != null) {
			out += "Simulation indices: " + simulationIndexVariable.getName();
			if (simulationIndexVariable.getVariableType() != VariableType.NUMERIC) {
				out += "(" + Arrays.stream(simulationIndexVariable.getStates())
								   .map(State::getName)
								   .collect(Collectors.joining(", ")) + ")\n";
			} else {
				out += "Continuous variable!\n";
			}
		} else {
			out += "No simulation indices.\n";
		}
		out += "Discount rate = " + discountRate;
		return out;
	}
	
	public MonteCarloOptions getMonteCarloOptions() {
		return monteCarloOptions;
	}
	
	public void setMonteCarloOptions(MonteCarloOptions monteCarloOptions) {
		this.monteCarloOptions = monteCarloOptions;
	}
	
}
