/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.treeadd;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.OpenMarkovException;
import org.openmarkov.core.inference.InferenceOptions;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.DESSimulablePotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialType;
import org.openmarkov.core.model.network.type.DESNetworkType;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Potential which consists on a TreeADDPotential where conditioned variable has type event and
 * computed TTE only depends on its chance parents.
 * @author cmyago
 * @version 1 14/03/2023; Created for Cochlear implant
 */
@PotentialType(names = "Tree with Excluded Events")
public class TreeWithExcludedEventsPotential extends Potential implements DESSimulablePotential {

	// Attributes
	/**
	 *List of parent Variable with VariableType.EVENT
	 */
	private List<Variable> events;

	/**
	 *List of parent Variable with isn't VariableType.EVENT
	 */
	private List<Variable> noEvents;

	/**
	 * TreeADDPotential without events
	 */
	private TreeADDPotential noEventTree;


	/**
	 * Creates a new TreeWithEventsPotential with variables as its list of Variable and role as PotentialRole
	 * @param variables potential variables
	 * @param role potential PotentialRole
	 */
	public TreeWithExcludedEventsPotential(List<Variable> variables, PotentialRole role) {
		super(variables,role);
		events = variables.subList(1,variables.size()).stream().filter(variable -> variable.getVariableType() == VariableType.EVENT).collect(Collectors.toList());
		noEvents = variables.subList(1,variables.size()).stream().filter(variable -> variable.getVariableType() !=VariableType.EVENT ).collect(Collectors.toList());
		List<Variable> eventTreeVariables = new ArrayList<>();
		eventTreeVariables.add(getConditionedVariable());
		eventTreeVariables.addAll(noEvents);
		noEventTree= new TreeADDPotential(eventTreeVariables, role);
	}

	/**
	 * Creates a new TreeWithEventsPotential copying treeWithExcludedEvents
	 *
	 * @param treeWithExcludedEvents TreeWithEventsPotential to be created
	 */
	public TreeWithExcludedEventsPotential(TreeWithExcludedEventsPotential treeWithExcludedEvents) {
		this(treeWithExcludedEvents.getVariables(),treeWithExcludedEvents.role);
		TreeADDPotential treesToCopy =(TreeADDPotential) treeWithExcludedEvents.getNoEventTree().copy();
	}

	/**
	 * Returns if an instance of a certain Potential type makes sense given the
	 * variables and the potential role
	 *
	 * @param variables
	 * @param role
	 */
	public static boolean validate(Node node, List<Variable> variables, PotentialRole role) {
		if (!(node.getProbNet().getNetworkType() instanceof DESNetworkType)) return false;

//		boolean validate =(variables.get(0).getVariableType() ==VariableType.EVENT) && variables.subList(1,variables.size()).stream().anyMatch(variable -> variable.getVariableType() ==VariableType.EVENT);
		boolean validate = variables.subList(1,variables.size()).stream().anyMatch(variable -> variable.getVariableType() ==VariableType.EVENT);

		return validate;
	}


	/**
	 * Returns a List<Variable> with the parent variables with VariableType.EVENT
	 * @return  List<Variable> with the parent variables with VariableType.EVENT
	 */
	public List<Variable> getEvents() {
		return events;
	}

	//14/08/2022 refactored for avoiding nuisance variance
	@Override
	public double sampleConditionedVariable(double[] randomNumbers, EvidenceCase parents) throws OpenMarkovException {

		double result =0;
		List<Variable> eventVariables = parents.getVariables().stream().filter(variable ->variable.getVariableType() ==VariableType.EVENT).collect(Collectors.toList());
		//19/03/2023 - only one event in parents - FIXME throw exception without changing method signature; consider to add exception
		//31/05/2024 - treewithoutevents can be used with chance nodes and, therefore some of the configurations may not have events (see inEurHeart=
//		if ( eventVariables.size()!=1) throw new RuntimeException("TreeWithEventsPotential#sampleConditionedVariable: There must be exactly one event in parents configuration");

		try
		{
			parents.removeFinding(eventVariables.get(0));
			result = noEventTree.sampleConditionedVariable(randomNumbers, parents);

		} catch(Exception e) {
			//FIXME when completed this method coding this catch will be removed

			JOptionPane.showMessageDialog(null,"TreeWithExcludeEventsPotential:Getting sample exception: " + this.getConditionedVariable());
			throw new RuntimeException(e);
		}
		return result;
	}
	@Override
	public void resetSimulation()  {
		noEventTree.resetSimulation();
	}

	@Override
	public int numRandomNumbersNeeded() {
		return noEventTree.numRandomNumbersNeeded();
	}

	@Override public Potential copy() {
		return new TreeWithExcludedEventsPotential(this);
	}


	@Override public boolean isUncertain() {
	//TODO
		return false;
	}

	/**
	 * Generates a sampled potential
	 */
	@Override public Potential sample() {
		//TODO
		return null;
	}



	@Override public Collection<Finding> getInducedFindings(EvidenceCase evidenceCase) {
		//TODO
		return null;
	}


	@Override public void replaceVariable(int position, Variable variable) {
		//TODO
	}

	/**
	 * @param evidenceCase               <code>EvidenceCase</code>
	 * @param inferenceOptions
	 * @param alreadyProjectedPotentials <code>List</code> of already projected potentials
	 */
	@Override
	public @NotNull TablePotential tableProject(EvidenceCase evidenceCase, InferenceOptions inferenceOptions, List<TablePotential> alreadyProjectedPotentials) throws NonProjectablePotentialException {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	@Override public Potential project(EvidenceCase evidenceCase) throws NonProjectablePotentialException {
		throw new NonProjectablePotentialException.PotentialCannotBeConvertedToATable(this);
	}
	
	@Override public void replaceNumericVariable(Variable convertedParentVariable) {
		//TODO
	}

	@Override public void scalePotential(double scale) {
		//TODO

	}

	@Override public Potential deepCopy(ProbNet copyNet) {
		//TODO
			return null;
	}
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	@Override
	public Potential reorder(List<Variable> newOrderOfVariables) {
		return null;
	}
	//03/01/2023; added after merge because it was added to Potential as an abstract method
	@Override
	public Potential reorder(Variable variable, State[] newOrder) {
		return null;
	}


	// Methods for toString()
	public String toString() {
		//TODO
		return "";
	}

	/**
	 * TreeADDPotential without events
	 */
	public TreeADDPotential getNoEventTree() {
		return noEventTree;
	}

	public void setNoEventTree(TreeADDPotential noEventTree) {
		this.noEventTree = noEventTree;
	}
}
