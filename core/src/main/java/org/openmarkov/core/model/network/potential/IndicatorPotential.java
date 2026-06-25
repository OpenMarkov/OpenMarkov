/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.*;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;

import java.util.List;

/**
 * Potential for an event variable in a discrete-event simulation (DESnet) that
 * models whether the event occurs and, if so, when. It is characterised by a
 * probability of occurrence and a time-to-event, and is sampled rather than
 * projected to a table.
 */
@PotentialType(names = "Indicator")
public class IndicatorPotential extends Potential implements DESSimulablePotential {

	/**
	 * Time-to-event
	 */
	private double tte = 0;
	/**
	 * Probability of the event happening
	 */
	private double pOccurrence =1;

	public IndicatorPotential(List<Variable> variables, PotentialRole role, double tte, double pOccurrence) {
		this(variables, role);
		this.tte = tte;
		this.pOccurrence = pOccurrence;
	}

	public IndicatorPotential(List<Variable> variables, PotentialRole role) {
		super(variables, role);
	}


	public IndicatorPotential(IndicatorPotential potential) {
		super(potential);
		this.tte = potential.getTte();
		this.pOccurrence = potential.getpOccurrence();
	}

	/**
	 * Returns whether this type of Potential is suitable for the list of
	 * variables and the potential role given.
	 *
	 * @param node      . {@code Node}
	 * @param variables . {@code List} of {@code Variable}.
	 * @param role      . {@code PotentialRole}.
	 * @return True the node is an event
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		return (node.getNodeType() == NodeType.EVENT);
	}

	/**
	 * Samples whether the event occurs: returns the time-to-event when the random
	 * number falls within the probability of occurrence, and {@code Double.NaN}
	 * (rather than {@code Double.MAX_VALUE}) when the event does not occur.
	 */
	@Override
	public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {

		if (randomNumbers[0] <= pOccurrence) {

			return tte;
		}

		else {

			return Double.NaN;
		}
	}

	@Override
	public void resetSimulation() {
	}



	@Override
	public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	@Override public Potential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	@Override public Potential copy() {
		return new IndicatorPotential(this);
	}

	@Override public boolean isUncertain() {
		return false;
	}

	/**
	 * No-op: scaling has no effect on this potential, so it does not throw
	 * {@code UnsupportedOperationException} as the base implementation does.
	 */
	@Override
	public void scalePotential(double scale) {

	}

	@Override public String toString()  {
		return variables.get(0) + "; probability = " + pOccurrence + "; TTE = "+ tte;
	}

	@Override public Potential deepCopy(ProbNet copyNet) {
		IndicatorPotential potential = (IndicatorPotential) super.deepCopy(copyNet);
		potential.pOccurrence =this.pOccurrence;
		potential.tte = this.tte;
		return potential;
	}

	/**
	 * Not implemented: returns {@code null} instead of throwing
	 * {@code UnsupportedOperationException} as the base implementation does.
	 */
	@Override
	public Potential reorder(List<Variable> newOrderOfVariables) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented: returns {@code null} instead of throwing
	 * {@code UnsupportedOperationException} as the base implementation does.
	 */
	@Override
	public Potential reorder(Variable variable, State[] newOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * probability of the event happening
	 */
	public double getpOccurrence() {
		return pOccurrence;
	}

	public void setpOccurrence(double pOccurrence) {
		//"Probability of ocurrence has to be in [0,1]"
		if (pOccurrence <0 || pOccurrence >1) {
			throw new InvalidArgumentException("Occurrence out of range 0<=pOccurrence <=1");
		};
		this.pOccurrence = pOccurrence;
	}

	/**
	 * Time-to-event
	 */
	public double getTte() {
		return tte;
	}

	public void setTte(double tte) {
		if (tte <0) {
			throw new InvalidArgumentException("Time to event  out of range 0..");
		};
		this.tte = tte;
	}


}
